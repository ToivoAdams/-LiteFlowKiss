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
package lite.log.api;

import lite.flow.api.flow.define.Activity;

public interface ExecutionContext {

	/**
	 * 	Creates new ExecutionContext which includes additional property
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public ExecutionContext addProperty(String name, Object value);

	/**
	 * 	Creates new execution context for given activity.
	 * 
	 * @param activity
	 * @return
	 */
	public ExecutionContext forActivity(Activity activity);
}
