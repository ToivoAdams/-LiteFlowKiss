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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;

import lite.flow.api.activity.Output;
import lite.flow.api.flow.define.Component;

public class ComponentUtil {

	public static void injectOutput(String outputName, Output<?> output, Object componentInstance) throws IllegalArgumentException, IllegalAccessException {

		Class<?> componentClazz = componentInstance.getClass();

		// find activity all Output type fields
		for (Field field : FieldUtils.getAllFields(componentClazz)) {
			Class<?> decl = field.getType();
			if (Output.class.isAssignableFrom(decl)) {
				
				String name = field.getName();
				if (name!=null && name.equals(outputName)) {
					field.setAccessible(true);
					field.set(componentInstance, output);
					return;
				}
			}
		}

		throw new IllegalArgumentException(format("Class '%s' do not contain output '%s'", componentClazz.getName(), outputName));
	}
	
	public static Object newInstance(Component component, FlowExecutionContext executionContext) throws ReflectiveOperationException {
		
		Class<?> componentClazz = component.componentClazz;
		Map<String,Object> resources = executionContext.getResources();
		Map<String,Object> parameters = component.parameters;

		Constructor<?> constructor = pickConstructor(componentClazz, resources, parameters);
		Object args[] = buildConstructorArgs(constructor, resources, parameters);
		
		Object componentInstance = constructor.newInstance(args);
		
		return componentInstance;
	}

	private static Object[] buildConstructorArgs(Constructor<?> constructor, Map<String, Object> resources, Map<String, Object> parameters) {
		requireNonNull(resources, "ComponentUtil.buildConstructorArgs resources should not be null");
		requireNonNull(parameters, "ComponentUtil.buildConstructorArgs parameters should not be null");
		
		constructor.getParameterTypes();
		
		Parameter[] consParameters = constructor.getParameters();
		if (consParameters==null || consParameters.length<1)
			return null;
		
		Object[] args = new Object[consParameters.length];
		for (int i = 0; i < consParameters.length; i++) {
			Parameter consParam = consParameters[i];
			Object consParamvalue = getConsParamvalue(consParam, resources, parameters);
			args[i] = consParamvalue;
		}
		
		return args;
	}

	private static Object getConsParamvalue(Parameter consParam, Map<String, Object> resources, Map<String, Object> parameters) {
		String name = consParam.getName();
		
		if (resources.containsKey(name))
			return resources.get(name);
		
		if (parameters.containsKey(name))
			return parameters.get(name);
		
		throw new IllegalArgumentException("No resource or parameter is available by name: " + name);
	}

	private static Constructor<?> pickConstructor(Class<?> componentClazz, Map<String, Object> resources, Map<String, Object> parameters) {
		
		Constructor<?>[] constructors = componentClazz.getConstructors();
		if (constructors==null || constructors.length<1)
			throw new IllegalArgumentException(format("Class '%s' don't have public constructors", componentClazz.getName()));
			
		return constructors[0];
	}


}
