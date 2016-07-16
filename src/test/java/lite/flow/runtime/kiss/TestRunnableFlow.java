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

import static org.junit.Assert.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Test;

import lite.flow.api.flow.define.Flow;
import lite.flow.example.flow.ConvertAddNumbersFlow;
import lite.flow.runtime.kiss.data.DCMessage;
import lite.flow.runtime.kiss.data.DataMessage;
import lite.flow.runtime.kiss.data.SimpleRequestContext;
import lite.log.simple.SimpleLogFactory;
import lite.log.simple.StructFormatter;

public class TestRunnableFlow {

	static SimpleLogFactory logFactory = new SimpleLogFactory();
	static Logger log = logFactory.logger(); 
	
	static public void setupLogging(Level level) {
		log.setLevel(level);
		Handler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(level);
		consoleHandler.setFormatter(new StructFormatter());
		log.addHandler(consoleHandler);
		log.setUseParentHandlers(false);
	}

	@After
	public void separate() {
		log.fine("-------");
	}

	class TestConsumer implements Consumer {

		final String outputName;
		final BlockingQueue<DCMessage> resultQueue;
		
		public TestConsumer(String outputName, BlockingQueue<DCMessage> resultQueue) {
			super();
			this.outputName = outputName;
			this.resultQueue = resultQueue;
		}

		@Override
		public boolean enqueue(DCMessage dcmsg) {
//			System.out.println("result " + outputName + " = " + dcmsg);
			resultQueue.offer(dcmsg);
			return true;
		}

		@Override
		public boolean canBeEnqueued() {
			return true;
		}	
	}
	
	@Test
	public void testFlow() throws ReflectiveOperationException, InterruptedException {
		setupLogging(Level.FINE);
		ExecutorService executorService = Executors.newFixedThreadPool(6);

		Flow flow = ConvertAddNumbersFlow.flow;
		FlowExecutionContext executionContext = new FlowExecutionContext(flow, "");
		RunnableFlow runnableFlow = new RunnableFlow(20, executionContext, logFactory, flow);

		BlockingQueue<DCMessage> resultQueue = new ArrayBlockingQueue<>(5);		
		TestConsumer resultReceiver = new TestConsumer("numResult", resultQueue);
		runnableFlow.addDestination("number", resultReceiver, "numResult");

		executorService.execute(runnableFlow);
		
	    long startNano = System.nanoTime();
		
		SimpleRequestContext ctx = new SimpleRequestContext();
		boolean result = runnableFlow.enqueue(new DataMessage<>(ctx, "str", "11;8"));
		assertTrue("enqueue result should be true", result);
		
		DataMessage<?> resultMessage = (DataMessage<?>) resultQueue.poll(900, TimeUnit.MILLISECONDS);
		assertNotNull("flow resultMessage sould not be null", resultMessage);
		
		assertEquals("flow result should be", 19, resultMessage.data);
		
		long totalNanos = (System.nanoTime() - startNano);
		double totalNanoToMillis = totalNanos / 1000000.0;
		System.out.println("Flow run completed in " +  totalNanoToMillis + " milliseconds");
		
//		Thread.sleep(90000);
	}

	@Test
	public void testStressFlow() throws ReflectiveOperationException, InterruptedException {
		setupLogging(Level.WARNING);
		ExecutorService executorService = Executors.newFixedThreadPool(6);

		Flow flow = ConvertAddNumbersFlow.flow;
		FlowExecutionContext executionContext = new FlowExecutionContext(flow, "");
		RunnableFlow runnableFlow = new RunnableFlow(20, executionContext, logFactory, flow);

		BlockingQueue<DCMessage> resultQueue = new ArrayBlockingQueue<>(5);		
		TestConsumer resultReceiver = new TestConsumer("numResult", resultQueue);
		runnableFlow.addDestination("number", resultReceiver, "numResult");

		executorService.execute(runnableFlow);
		
	    long startNano = System.nanoTime();
		// warm up
		for (int i = 0; i < 8550000; i++) {
			SimpleRequestContext ctx = new SimpleRequestContext();
			runnableFlow.enqueue(new DataMessage<>(ctx, "str", ""+i+";"+i+3));
		}
		
		long totalNanos = (System.nanoTime() - startNano);
		double totalNanoToMillis = totalNanos / 1000000.0;
		System.out.println("Warn phase completed in " +  totalNanoToMillis + " milliseconds");
		
		Thread.sleep(900);
		resultQueue.clear();
		
	    startNano = System.nanoTime();
		
		SimpleRequestContext ctx = new SimpleRequestContext();
		boolean result = runnableFlow.enqueue(new DataMessage<>(ctx, "str", "11;8"));
		assertTrue("enqueue result should be true", result);
		
		DataMessage<?> resultMessage = (DataMessage<?>) resultQueue.poll(900, TimeUnit.MILLISECONDS);
		assertNotNull("flow resultMessage sould not be null", resultMessage);
		
		assertEquals("flow result should be", 19, resultMessage.data);
		
		totalNanos = (System.nanoTime() - startNano);
		totalNanoToMillis = totalNanos / 1000000.0;
		System.out.println("Flow run completed in " +  totalNanoToMillis + " milliseconds");
	}


}
