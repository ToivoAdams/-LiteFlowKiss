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

import static lite.flow.api.util.ActivityInspector.inspect;

import java.util.concurrent.ExecutorService;

import lite.flow.api.util.ActivityInspector.EntryPoint;
import lite.flow.api.util.ActivityInspector.InspectResult;
import lite.flow.runtime.kiss.data.DataMessage;
import lite.log.api.ExecutionContext;
import lite.log.api.Log;
import lite.log.api.LogFactory;
import lite.log.intercept.Modifier;

/**
 * Simple java component runner.
 * 
 * 
 * @author ToivoAdams
 *
 */
public class RunnableComponent extends SequentialActivity {

	private final Correlator[] correlators;
	private final MethodInvokerSequential methodInvoker;
	
	/**
	 * @param inputQueueLength
	 * @param executionContext
	 * @param logFactory
	 * @throws ReflectiveOperationException 
	 */
	public RunnableComponent(Integer inputQueueLength, ExecutionContext executionContext, LogFactory logFactory, Class<?> componentClazz, ExecutorService executorService) 
			throws ReflectiveOperationException {
		super(inputQueueLength, executionContext, logFactory);
		
		InspectResult inspectResult = inspect(componentClazz);
		
		this.methodInvoker = Modifier.addLogging(MethodInvokerSequential.class, executionContext, logFactory)
				.newInstance(inputQueueLength, executionContext, logFactory, executorService, componentClazz, inspectResult.withoutExplicitOutputPort);

		correlators = new Correlator[inspectResult.entryPoints.length];
		int i = 0;
		for (EntryPoint entryPoint : inspectResult.entryPoints) {
			
			Correlator correlator = Modifier.addLogging(Correlator.class, executionContext, logFactory)
					.newInstance(inputQueueLength, executionContext, entryPoint, logFactory);
			
			correlator.addDestination("collectedMethodParametersRow", methodInvoker, "collectedMethodParametersRow");
			correlators[i++] = correlator;
		}
		
//		Class<Correlator> correlatorClass = Modifier.addLogging0(Correlator.class, executionContext, logFactory);
//		Constructor<Correlator> correlatorConstrutor =  correlatorClass.getConstructor(Integer.TYPE, ExecutionContext.class, Method.class, LogFactory.class);
//		this.correlator = correlatorConstrutor.newInstance(inputQueueLength, executionContext, componentMethod, logFactory);
//		this.correlator = new Correlator(inputQueueLength, executionContext, componentMethod, logFactory);

//		this.methodInvokerInterleaved = new MethodInvokerInterleaved(inputQueueLength, executionContext, logFactory, executorService, componentClazz);
		
		// build "flow"
//		correlator.calc(11, 7);
		
		executorService.execute(methodInvoker);
		
		for (Correlator correlator : correlators) {
			executorService.execute(correlator);
			
		}
	}

	/* (non-Javadoc)
	 * @see lite.flow.runtime.kiss.simplest.SequentialActivity#processMessage(lite.flow.runtime.kiss.data.DataMessage)
	 */
	@Override
	public final Object processDataMessage(DataMessage<?> dataMessage) {
//    	System.out.println("===============> RunnableComponent.processMessage");
		
		for (Correlator correlator : correlators)
			correlator.enqueue(dataMessage);
		return dataMessage;
	}

	/* (non-Javadoc)
	 * @see lite.flow.runtime.kiss.simplest.RunnableActivity#addDestination(java.lang.String, lite.flow.runtime.kiss.simplest.Consumer)
	 */
	@Log
	@Override
	public final void addDestination(String ouputName, Consumer consumer, String destinationName) {
		methodInvoker.addDestination(ouputName, consumer, destinationName);
	}

}
