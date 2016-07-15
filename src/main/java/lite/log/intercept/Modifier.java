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

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Constructor;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import lite.log.api.ExecutionContext;
import lite.log.api.Log;
import lite.log.api.LogFactory;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author ToivoAdams
 *
 */
public class Modifier<T> {

	private Class<T> modifiedClazz;
	
	public Modifier(Class<T> modifiedClazz) {
		super();
		this.modifiedClazz = modifiedClazz;
	}

	@SuppressWarnings("unchecked")
	static public <T> Modifier<T> addLogging(Class<T> clazz, ExecutionContext executionContext, LogFactory logFactory) {
		
		Class<T> dynamicType = (Class<T>) new ByteBuddy()
	    		  .subclass(clazz)
	    		  .method(any().and(isAnnotatedWith(Log.class)))
	    		  .intercept(MethodDelegation.to(new Interceptor(executionContext, logFactory)))
	    		  
	    		  .make()
	    		  .load(clazz.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
	    		  .getLoaded();
		
		return new Modifier<T>(dynamicType);
	}

	@SuppressWarnings("unchecked")
	static public <T> Modifier<T> addLoggingBusiness(Class<T> clazz, ExecutionContext executionContext, LogFactory logFactory) {
		
		Class<T> dynamicType = (Class<T>) new ByteBuddy()
	    		  .subclass(clazz)
 		    	  .method(ElementMatchers.named("tranform"))
	    		  .intercept(MethodDelegation.to(new InterceptorBusiness(executionContext, logFactory)))
	    		  
	    		  .make()
	    		  .load(clazz.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
	    		  .getLoaded();
		
		return new Modifier<T>(dynamicType);
	}

	
	public T newInstance(Object ... initargs) throws ReflectiveOperationException {
		if (initargs==null)
			return modifiedClazz.newInstance();

		Class<?>[] parameterTypes = new Class[initargs.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypes[i] = initargs[i].getClass();
		}
		
		Constructor<T> constructor = ConstructorUtils.getMatchingAccessibleConstructor(modifiedClazz, parameterTypes);
		return constructor.newInstance(initargs);
	}
	
}
