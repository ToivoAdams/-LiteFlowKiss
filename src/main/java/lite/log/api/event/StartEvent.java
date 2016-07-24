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
package lite.log.api.event;

import java.util.logging.Level;

import lite.flow.api.activity.RequestContext;
import lite.flow.util.UniqueId;
import lite.log.api.ExecutionContext;

/**
 * 	Includes method start (invoke) information, context and input values.
 * 
 * @author ToivoAdams
 *
 */
public class StartEvent extends EventsBase {

	private static final long serialVersionUID = 1L;

	public final RequestContext 	requestContext;
	public final ExecutionContext 	executionContext;
	
	public final String[]			argNames;		// input argument names
	public final Object[]			argValues;		// input argument values
	

	public StartEvent(Level level, String msg, RequestContext requestContext, ExecutionContext executionContext, UniqueId eventCorrelationId
			, String[] argNames, Object[]	argValues) {
		super(level, msg, eventCorrelationId);
		this.requestContext = requestContext;
		this.executionContext = executionContext;
		this.argNames = argNames;
		this.argValues = argValues;
	}

	public StartEvent(Level level, String msg, RequestContext requestContext, ExecutionContext executionContext, UniqueId eventCorrelationId
			, String argName, Object argValue) {
		super(level, msg, eventCorrelationId);
		this.requestContext = requestContext;
		this.executionContext = executionContext;
		this.argNames = new String[1];
		this.argNames[0] = argName;
		this.argValues = new Object[1];
		this.argValues[0] = argValue;
	}

	
	@Deprecated // use constructor with input arguments
	public StartEvent(Level level, String msg, RequestContext requestContext, ExecutionContext executionContext, UniqueId eventCorrelationId) {
		super(level, msg, eventCorrelationId);
		this.requestContext = requestContext;
		this.executionContext = executionContext;
		this.argNames = null;
		this.argValues = null;
	}


	
}
