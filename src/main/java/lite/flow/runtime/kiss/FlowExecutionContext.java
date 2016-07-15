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

import lite.flow.api.flow.define.Activity;
import lite.flow.api.flow.define.Flow;
import lite.log.api.ExecutionContext;

/**
 * Very simple context for testing. 
 * 
 * @author ToivoAdams
 */
public class FlowExecutionContext implements ExecutionContext {

	public final Flow flow;
	public final String activityName;


	public FlowExecutionContext(Flow flow, String activityName) {
		super();
		this.flow = flow;
		this.activityName = activityName;
	}


	@Override
	public ExecutionContext addProperty(String name, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExecutionContext forActivity(Activity activity) {
		return new FlowExecutionContext(flow, activity.name);
	}

	@Override
	public String toString() {
		String flowName = flow==null ? null : flow.flowName; 
		return "ectx[flow=" + flowName + ", activity=" + activityName + "]";
//		return "FlowExecutionContext[flow=" + flow + ", activity=" + activityName + "]";
	}

}
