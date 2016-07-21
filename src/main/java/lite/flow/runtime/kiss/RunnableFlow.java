/**
 * 
 */
package lite.flow.runtime.kiss;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lite.flow.api.flow.define.Activity;
import lite.flow.api.flow.define.Component;
import lite.flow.api.flow.define.Connection;
import lite.flow.api.flow.define.Flow;
import lite.flow.runtime.kiss.data.DCMessage;
import lite.flow.runtime.kiss.data.DataMessage;
import lite.log.api.ExecutionContext;
import lite.log.api.LogFactory;

/**
 * @author ToivoAdams
 *
 */
public class RunnableFlow extends SequentialActivity {

	protected final Flow flow;
	protected final Map<String,RunnableActivity> runnableActivities;
	protected final ExecutorService executorService;
	
	// Note!!!! initially only one input and output connector
	protected final InputConnector inputConnector;
	protected final OutputConnector outputConnector;

	/**
	 * @param inputQueueLength
	 * @param executionContext
	 * @param logFactory
	 * @throws ReflectiveOperationException 
	 */
	public RunnableFlow(Integer inputQueueLength, ExecutionContext executionContext, LogFactory logFactory, Flow flow) throws ReflectiveOperationException {
		super(inputQueueLength, executionContext, logFactory);
		this.flow = flow;
		this.executorService = Executors.newFixedThreadPool(50);
		this.runnableActivities = createRunnableActivities(flow);
		addConnections(runnableActivities, flow);
		this.inputConnector = createInputConnector(flow);
		this.outputConnector = createOutputConnector(flow);

		startAllActivities(runnableActivities, executorService);
	}

	private void startAllActivities(Map<String, RunnableActivity> runnableActivities, ExecutorService executorService) {
		for (Map.Entry<String, RunnableActivity> entry : runnableActivities.entrySet()) {
			executorService.execute(entry.getValue());
		}
	}

	protected InputConnector createInputConnector(Flow flow) {
		if (flow.flowInputs==null || flow.flowInputs.length<1) {
			logFactory.logger().warning("Flow " + flow.flowName + " does not have any flowInputs defined");
			return null;
		}
			
		RunnableActivity to = findActivity(flow.flowInputs[0].to.name);		
		return new InputConnector(to, flow.flowInputs[0].toPort);
	}
	
	protected OutputConnector createOutputConnector(Flow flow) {
		if (flow.flowOutputs==null || flow.flowOutputs.length<1) {
			logFactory.logger().warning("Flow " + flow.flowName + " does not have any flowOutputs defined");
			return null;
		}

		RunnableActivity from = findActivity(flow.flowOutputs[0].from.name);
		return new OutputConnector(from, flow.flowInputs[0].toPort);
	}

	protected void addConnections(Map<String, RunnableActivity> runnableActivities, Flow flow) {
		for (Connection connection : flow.connections) {
			RunnableActivity from = findActivity(connection.from.name);
			RunnableActivity to = findActivity(connection.to.name);
			from.addDestination(connection.fromPort, to, connection.toPort);
		}
	}

	protected Map<String,RunnableActivity> createRunnableActivities(Flow flow) throws ReflectiveOperationException {
		Map<String,RunnableActivity> runnableActivities = new HashMap<>();
		for (Activity activity : flow.activities) {
			if (activity instanceof Component) {
				Component component = (Component) activity;
				ExecutionContext activityExecutionContext = executionContext.forActivity(activity);
				RunnableComponent runnableComponent = new RunnableComponent(inputQueueLength, activityExecutionContext, logFactory, component, executorService);
				runnableActivities.put(activity.name, runnableComponent);
			}
		}

		return runnableActivities;
	}

	private RunnableActivity findActivity(String activityName) {
		requireNonNull(activityName, "Activity name cannot be null");

		for (Map.Entry<String, RunnableActivity> entry : runnableActivities.entrySet())
			if (activityName.equals(entry.getKey()))
					return entry.getValue();

		throw new IllegalArgumentException("Activity '" + activityName + "' does not exist");
	}

	/* (non-Javadoc)
	 * @see lite.flow.runtime.kiss.simplest.RunnableActivity#addDestination(java.lang.String, lite.flow.runtime.kiss.simplest.Consumer)
	 */
	@Override
	public void addDestination(String ouputName, Consumer consumer, String destinationName) {
		// when flow definition does not contains any flow outputs, we can't add destination 
		if (flow.flowOutputs==null || flow.flowOutputs.length<1)
			return;

		RunnableActivity from = findActivity(flow.flowOutputs[0].from.name);
		from.addDestination(ouputName, consumer, destinationName);
	}

	/* (non-Javadoc)
	 * @see lite.flow.runtime.kiss.simplest.SequentialActivity#processDataMessage(lite.flow.runtime.kiss.data.DataMessage)
	 */
	@Override
	public Object processDataMessage(DataMessage<?> dataMessage) {
		// route to input connector
//		System.out.println("¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ RunnableFlow processDataMessage got " + dataMessage);
		
		return inputConnector.enqueue(dataMessage);
	}

	static class InputConnector implements Consumer {
		public final RunnableActivity 	destination;
		public final String 			destinationName;
		
		public InputConnector(RunnableActivity destination, String destinationName) {
			super();
			this.destination = destination;
			this.destinationName = destinationName;
		}
		
		@Override
		public boolean enqueue(DCMessage dcmsg) {
			// Note!!! we don't send same DCMessage we received, we create new DCMessage using destinationName 
			return destination.enqueue( dcmsg.cloneChangeDestinationName(destinationName));
		}
		
		@Override
		public boolean canBeEnqueued() {
			return destination.canBeEnqueued();
		}
	}
	
	/**
	 * 	Current implementation is merely placeholder.
	 *
	 */
	static class OutputConnector {
		public final RunnableActivity 				from;
		public final String 						fromPort;		

		public OutputConnector(RunnableActivity from, String fromPort) {
			super();
			this.from = from;
			this.fromPort = fromPort;
		}
	}
	
/**** need modifications
 
 	static class OutputConnector implements Consumer {
		public final RunnableActivity 				from;
		public final String 						fromPort;
//		public final Map<String,Consumer> destinations = new HashMap<>();
		private final Map<String,DistributorOutput<?>> connectorOutputs = new HashMap<>(); 
		protected final LogFactory 					logFactory;
				
		public OutputConnector(RunnableActivity from, String fromPort, LogFactory logFactory) {
			super();
			this.from = from;
			this.fromPort = fromPort;
			this.logFactory = logFactory;
		}
		
		//	This is probably wrong. 
		// Each output by name should have its own OutputConnector.
		public void addDestination(String outputName, Consumer consumer, String destinationName) {
			DistributorOutput<?> output = getOrPut(outputName);
			output.addDestination(new Destination(destinationName, consumer));
		}

	    public final DistributorOutput<?> getOrPut(String outputName) {
	    	if (connectorOutputs.containsKey(outputName))
	    		return connectorOutputs.get(outputName);

	    	DistributorOutput<?> distributorOutput = new DistributorOutput<>(outputName);
	    	connectorOutputs.put(outputName, distributorOutput);
	    	return distributorOutput;
	    }

		@Override
		public boolean enqueue(DCMessage dcmsg) {
			if (dcmsg==null) {
				logFactory.logger().warning("OutputConnector.enqueue dcmsg is null, cannot enqueue");
				return false;
				
			}
			for (Map.Entry<String,DistributorOutput<?>> outputEntry : connectorOutputs.entrySet()) {
				// Note!!! we don't send same DCMessage we received, we create new DCMessage using destinationName 
				outputEntry.getValue().emit(dcmsg, dcmsg.getContext());				
			}
		}
		
		@Override
		public boolean canBeEnqueued() {
			return destination.canBeEnqueued();
		}
	}
******/	
}
