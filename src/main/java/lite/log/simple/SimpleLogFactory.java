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
package lite.log.simple;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import lite.flow.api.util.UniqueId;
import lite.flow.runtime.kiss.data.LongUniqueId;
import lite.log.api.LogFactory;

/**
 * 	Simple factory which create simple Long type eventCorrelationId-s.
 * Handy for testing or low volume production.  
 * 
 * @author ToivoAdams
 *
 */
@Deprecated // correct method name can't be found this way
public class SimpleLogFactory implements LogFactory {

	
	
	private volatile AtomicLong idgen = new AtomicLong(0);
//	private final Context context;
	
	public final Logger log = Logger.getLogger("XLogger");	
	
	public SimpleLogFactory() {
		super();
//		this.context = context;
	}

	@Override
	public UniqueId newCid() {
		return new LongUniqueId(idgen.incrementAndGet());
	}
	
	@Override
	public final Logger logger() {
		return log;
	}

}
