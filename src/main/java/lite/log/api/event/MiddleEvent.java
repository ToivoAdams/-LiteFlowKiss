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
import lite.flow.api.util.UniqueId;
import lite.log.api.ExecutionContext;

/**
 * @author ToivoAdams
 *
 */
public class MiddleEvent extends EventsBase {

	private static final long serialVersionUID = 1L;

	public final RequestContext 	requestContext;
	public final ExecutionContext 	executionContext;
	
	public MiddleEvent(Level level, String msg, RequestContext requestContext, ExecutionContext executionContext, UniqueId eventCorrelationId) {
		super(level, msg, eventCorrelationId);
		this.requestContext = requestContext;
		this.executionContext = executionContext;
	}

	public MiddleEvent(Level level, String msg, StartEvent startEvent) {
		super(level, msg, startEvent.eventCorrelationId);
		this.requestContext = startEvent.requestContext;
		this.executionContext = startEvent.executionContext;
	}


	
}
