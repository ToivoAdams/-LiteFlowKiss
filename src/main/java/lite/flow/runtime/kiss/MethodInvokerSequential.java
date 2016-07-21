/**
 * Copyright 2016 ToivoAdams
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lite.flow.runtime.kiss;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import lite.flow.api.activity.RequestContext;
import lite.flow.api.flow.define.Component;
import lite.flow.api.util.UniqueId;
import lite.flow.runtime.kiss.DistributorOutput.Destination;
import lite.flow.runtime.kiss.data.DataMessage;
import lite.log.api.ExecutionContext;
import lite.log.api.Log;
import lite.log.api.LogFactory;
import lite.log.api.event.EndEvent;
import lite.log.api.event.StartEvent;

/**
 * 	Executed method may be not Thread Safe.
 * MethodInvokerSequential calls component transform methods only after previous method calls are finished.
 * This is simple implementation and component transform methods should not run very long time. 
 * 
 * @author ToivoAdams
 *
 */
public class MethodInvokerSequential extends SequentialActivity {

	public final ExecutorService executorService; 
	public final AtomicInteger nrOfActiveCalls = new AtomicInteger(0);
	private final Class<?> componentClazz;
	private final Object componentInstance;
	private final boolean withoutExplicitOutputPort;
	private final Map<String,DistributorOutput<?>> actvityOutputs = new HashMap<>(); 
	
	private final RequestContextCarrier rcc = new RequestContextCarrier();
	
	public MethodInvokerSequential(int inputQueueLength, FlowExecutionContext executionContext, LogFactory logFactory
			, ExecutorService executorService, Component component, boolean withoutExplicitOutputPort) throws ReflectiveOperationException {
		super(inputQueueLength, executionContext, logFactory);
		requireNonNull(executionContext, "MethodInvokerSequential executionContext should not be null");
		requireNonNull(logFactory, 		 "MethodInvokerSequential logFactory should not be null");
		requireNonNull(executorService,  "MethodInvokerSequential executorService should not be null");		
		requireNonNull(component,   	 "MethodInvokerSequential component should not be null");		

		this.executorService = executorService;
		this.componentClazz = component.componentClazz;
		this.withoutExplicitOutputPort = withoutExplicitOutputPort;
		try {
	//		this.componentInstance = Modifier.addLoggingBusiness(componentClazz, executionContext, logFactory)
	//		.newInstance();
	//		this.componentInstance = componentClazz.newInstance();
			this.componentInstance = ComponentUtil.newInstance(component, executionContext);
	//		ActivityInspector.injectEmitters(new SimpleEmitter(), componentInstance);			

		} catch (InstantiationException | IllegalAccessException e) {
			EndEvent endEvent = new EndEvent(Level.FINE, "create component instance failed", null, executionContext, logFactory.newCid());
			endEvent.setThrown(e);
			logFactory.logger().log(endEvent);
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see lite.flow.runtime.kiss.simplest.SequentialActivity#processMessage(lite.flow.runtime.kiss.data.DataMessage)
	 */
	@Log
	@Override
	public Object processDataMessage(DataMessage<?> dataMessage) {
		requireNonNull(dataMessage, "MethodInvokerSequential.processMessage dataMessage should not be null");
		
		if (dataMessage.data instanceof CorrelatedEntryPointArguments) {
			CorrelatedEntryPointArguments collectedMethodParametersRow = (CorrelatedEntryPointArguments) dataMessage.data;
			nrOfActiveCalls.incrementAndGet();
			Invoker invoker = new Invoker(collectedMethodParametersRow, dataMessage.context, executionContext);
			// Note!!! This will block!!!
			invoker.call();
			return collectedMethodParametersRow;
		} else
			throw new IllegalArgumentException("MethodInvokerSequential.processMessage dataMessage.data should be CorrelatedEntryPointArguments, but is " + dataMessage.data);
	}

	private class Invoker implements Callable<Object> {

		private final CorrelatedEntryPointArguments collectedMethodParametersRow;
		private final RequestContext   requestContext;
		private final ExecutionContext executionContext;
		
		public Invoker(CorrelatedEntryPointArguments collectedMethodParametersRow, RequestContext requestContext, ExecutionContext executionContext) {
			super();
			this.collectedMethodParametersRow = collectedMethodParametersRow;
			this.requestContext = requestContext;
			this.executionContext = executionContext;
		}

		@Override
		public Object call() {
			UniqueId cid = logFactory.newCid();
			Method componentMethod = collectedMethodParametersRow.entryPoint.method;
			StartEvent startEvent = new StartEvent(Level.INFO, "", requestContext, executionContext, cid, collectedMethodParametersRow.entryPoint.inputNames, collectedMethodParametersRow.inputArgs);
			startEvent.setSourceClassName(componentClazz.getName());
			startEvent.setSourceMethodName(componentMethod.getName());

	//		StartEvent startEvent = new StartEvent(Level.INFO, 
	//				"invoke component method " + componentMethod.getName() + " , using: " + collectedMethodParametersRow, requestContext, executionContext, cid);
			logFactory.logger().log(startEvent);
			Object result = null;
			try {
		    	rcc.setRequestContext(requestContext);
				result = componentMethod.invoke(componentInstance, collectedMethodParametersRow.inputArgs);
				if (withoutExplicitOutputPort) {
					// Component is without explicit Output port!
					// We must send output value here.
					String outputName = collectedMethodParametersRow.entryPoint.outputName;
					if (actvityOutputs.containsKey(outputName)==false)
						throw new IllegalArgumentException("Activity " + componentClazz.getName() + " output " + outputName + " not created");
					
					DistributorOutput<Object> output = (DistributorOutput<Object>) actvityOutputs.get(outputName);
					output.emit(result, requestContext);
					
				}
			} catch (Exception e) {
				EndEvent endEvent = new EndEvent(Level.WARNING, "", startEvent);
				endEvent.setSourceClassName(componentClazz.getName());
				endEvent.setSourceMethodName(componentMethod.getName());
				endEvent.setThrown(e);
				logFactory.logger().log(endEvent);
			}
			EndEvent endEvent = new EndEvent(Level.INFO, "", startEvent, "result", result);
			endEvent.setSourceClassName(componentClazz.getName());
			endEvent.setSourceMethodName(componentMethod.getName());
	//		EndEvent endEvent = new EndEvent(Level.INFO, "invoke component  method " + componentMethod.getName() + " result: " + result, startEvent);
			logFactory.logger().log(endEvent);

			nrOfActiveCalls.decrementAndGet();
			return null;
		}
	}

	/** 
	 * 	MethodInvokerInterleaved does not have its own destinations.
	 * Instead it has component outputs as its own outputs.
	 * 
	 * When we need separately MethodInvokerInterleaved own destinations we can create separate destinations for both.
	 */
	@Log
	@Override
	public void addDestination(String outputName, Consumer consumer, String destinationName) {
		DistributorOutput<?> output = getOrPut(outputName);
		output.addDestination(new Destination(destinationName, consumer));
		
		try {
			if (withoutExplicitOutputPort) {
				// Component is without explicit Output port!

			} else
				ComponentUtil.injectOutput(outputName, output, componentInstance);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    public final DistributorOutput<?> getOrPut(String outputName) {
    	if (actvityOutputs.containsKey(outputName))
    		return actvityOutputs.get(outputName);

    	DistributorOutput<?> distributorOutput = new DistributorOutput<>(outputName, rcc);
    	actvityOutputs.put(outputName, distributorOutput);
    	return distributorOutput;
    }

	
	@Override
	public String toString() {
		return "MethodInvokerInterleaved [nrOfActiveCalls=" + nrOfActiveCalls
				+ ", componentClazz=" + componentClazz + ", componentInstance=" + componentInstance + "]";
	}

}
