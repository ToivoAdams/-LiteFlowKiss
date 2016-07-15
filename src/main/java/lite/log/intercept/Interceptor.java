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
package lite.log.intercept;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import lite.flow.api.activity.RequestContext;
import lite.log.api.ExecutionContext;
import lite.log.api.LogFactory;
import lite.log.api.event.EndEvent;
import lite.log.api.event.StartEvent;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

@SuppressWarnings("rawtypes")
public class Interceptor {

	private final ExecutionContext 	executionContext;
	private final LogFactory 			logFactory;

	public Interceptor(ExecutionContext executionContext, LogFactory logFactory) {
		super();
		this.executionContext = executionContext;
		this.logFactory = logFactory;
	}

	@RuntimeType
	public Object intercept( @SuperCall Callable<?> callable, @AllArguments Object[] allArguments, @Origin Method method, @Origin Class clazz) throws Exception {
	    long startTime = System.currentTimeMillis();
	    
//	    System.out.println("---> " + callable + " " + clazz);
//	    System.out.println("---> " + clazz.getName() + " " + method.getName() + " " + allArguments);
	    
	    String[] argNames = new String[allArguments.length];
	 
	    int i = 0;
	    for(Parameter p : method.getParameters())
	    	argNames[i++] = p.getName();

	    RequestContext requestContext = null;
		StartEvent startEvent = new StartEvent(Level.FINE, "", requestContext, executionContext, logFactory.newCid(), argNames, allArguments);
		startEvent.setSourceMethodName(method.getName());
		startEvent.setSourceClassName(clazz.getName());
		logFactory.logger().log(startEvent);

	    try{
	    	Object result = callable.call();
			EndEvent endEvent = new EndEvent(Level.FINE, "result: " + result, startEvent);
			endEvent.setSourceMethodName(method.getName());
			endEvent.setSourceClassName(clazz.getName());
			logFactory.logger().log(endEvent);
		    return result;
	    }
	    catch(Exception e) {
	 //       System.out.println("Exception occurred in method call: " + methodName(clazz, method, allArguments) + " Exception = " + e);
			EndEvent endEvent = new EndEvent(Level.WARNING, "failed: ", startEvent);
			endEvent.setThrown(e);
			endEvent.setSourceMethodName(method.getName());
			endEvent.setSourceClassName(clazz.getName());
			logFactory.logger().log(endEvent);
	        throw e;
	    }
	    finally{
	    	
	 //       System.out.println(executionContext);
	    }
	}

}