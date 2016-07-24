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
package lite.flow.runtime.kiss.data;

import java.util.concurrent.atomic.AtomicLong;

import lite.flow.api.activity.RequestContext;
import lite.flow.util.UniqueId;


/**
 * Very simple context for testing. 
 * 
 * @author ToivoAdams
 */
public class SimpleRequestContext implements RequestContext {

	static public final AtomicLong idgen = new AtomicLong(0);
	
	public final UniqueId requestId;

	public SimpleRequestContext() {
		super();
		this.requestId = new LongUniqueId(idgen.incrementAndGet());
	}

	@Override
	public UniqueId getRequestId() {
		return requestId;
	}

	@Override
	public RequestContext getSubRequestContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
//		return "rctx[requestId=" + requestId + "]";
		return "rctx[rid=" + requestId.getIdValue() + "]";
	}
	
}
