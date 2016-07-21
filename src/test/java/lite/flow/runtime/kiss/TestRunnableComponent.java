package lite.flow.runtime.kiss;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import lite.flow.api.flow.define.Component;
import lite.flow.example.component.Adder;
import lite.flow.example.component.StringSplitter;
import lite.flow.runtime.kiss.data.DCMessage;
import lite.flow.runtime.kiss.data.DataMessage;
import lite.flow.runtime.kiss.data.SimpleRequestContext;
import lite.log.simple.SimpleLogFactory;
import lite.log.simple.StructFormatter;

public class TestRunnableComponent {

	static SimpleLogFactory logFactory = new SimpleLogFactory();
	static Logger log = logFactory.logger(); 
	
	@BeforeClass
	static public void setup() {
		log.setLevel(Level.FINEST);
		Handler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.FINEST);
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
		
		public TestConsumer(String outputName) {
			super();
			this.outputName = outputName;
		}

		@Override
		public boolean enqueue(DCMessage dcmsg) {
			System.out.println("result " + outputName + " = " + dcmsg);
			return true;
		}

		@Override
		public boolean canBeEnqueued() {
			return true;
		}	
	}
	
	@Test
	public void testOutput() throws ReflectiveOperationException, InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(6);
		FlowExecutionContext executionContext = new FlowExecutionContext(null, "stringSplitter");
		SimpleRequestContext ctx = new SimpleRequestContext();

		Component component = new Component(StringSplitter.class, "StringSplitter", 0, 0);
		RunnableComponent runComp = new RunnableComponent(20, executionContext, logFactory, component, executorService);
		runComp.addDestination("outA", new TestConsumer("outA"), "outA");
		runComp.addDestination("outB", new TestConsumer("outB"), "outB");
		
		executorService.execute(runComp);
		boolean result = runComp.enqueue(new DataMessage<>(ctx, "str", "11;8"));
		assertTrue("enqueue result should be true", result);

		// give some time to RunnableComponent
		Thread.sleep(700);
	}

	@Test
	public void testComponentWithoutExplicitOutput() throws ReflectiveOperationException, InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(6);
		FlowExecutionContext executionContext = new FlowExecutionContext(null, "Adder");
		SimpleRequestContext ctx = new SimpleRequestContext();

		Component component = new Component(Adder.class, "Adder", 0, 0);
		RunnableComponent runComp = new RunnableComponent(20, executionContext, logFactory, component, executorService);
		runComp.addDestination("number", new TestConsumer("number"), "number");
		
		executorService.execute(runComp);
		boolean result 	= runComp.enqueue(new DataMessage<>(ctx, "a", 7));
		assertTrue("enqueue result should be true", result);
		result 			= runComp.enqueue(new DataMessage<>(ctx, "b", 9));
		assertTrue("enqueue result should be true", result);

		// give some time to RunnableComponent
		Thread.sleep(700);
	}


}
