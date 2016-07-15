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

/**
 * 	Any activity which consumes input and produces output.
 * 
 *  Simplest possible.
 * Everything this is not absolutely necessary should be leave out.
 * 
 * @author ToivoAdams
 *
 */
public interface RunnableActivity extends Consumer, Runnable {

	/**
	 * 	Add destinations to output port.
	 * 
	 * @param ouputName
	 * @param consumer
	 * @param destinationName	destination may have different input name
	 */
	public void addDestination(String ouputName, Consumer consumer, String destinationName);
	
//	public String getName();
}
