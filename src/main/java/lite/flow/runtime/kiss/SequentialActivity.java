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

import static java.util.Objects.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import lite.flow.runtime.kiss.data.DCMessage;
import lite.flow.runtime.kiss.data.DataMessage;
import lite.log.api.ExecutionContext;
import lite.log.api.Log;
import lite.log.api.LogFactory;

/**
 * 	Actor like activity which executes all requests (messages) sequentially.
 * This should avoid thread synchronizations problems.
 * SequentialActivity subclasses can be thread safe. 
 * 
 * @author ToivoAdams
 *
 */
abstract public class SequentialActivity implements RunnableActivity {

	protected final Integer 					inputQueueLength;
	protected final BlockingQueue<DCMessage> 	inputQueue;
	protected final ExecutionContext			executionContext;
	protected final LogFactory 					logFactory;


	public SequentialActivity(Integer inputQueueLength, ExecutionContext executionContext, LogFactory logFactory) {
		super();
		this.inputQueueLength = inputQueueLength;
	//	this.inputQueue = new LinkedTransferQueue<>();
		this.inputQueue = new ArrayBlockingQueue<DCMessage>(inputQueueLength);
		this.executionContext = executionContext;
		this.logFactory = logFactory;
	}

	/* (non-Javadoc)
	 * @see lite.flow.runtime.kiss.simplest.Consumer#enqueue(lite.flow.runtime.kiss.data.DCMessage)
	 */
	@Log		// NB!! Method cannot be final, ByteBuddy can't intercept it
	@Override
	public boolean enqueue(DCMessage dcmsg) {
		requireNonNull(dcmsg, "SequentialActivity.enqueue dcmsg should not be null");		
		boolean result = inputQueue.offer(dcmsg);
		return result;
	}

	/* (non-Javadoc)
	 * @see lite.flow.runtime.kiss.simplest.Consumer#canBeEnqueued()
	 */
	@Override
	public boolean canBeEnqueued() {
		// current estimation is very imprecise
		if (inputQueue.remainingCapacity()>3)
			return true;
		return false;
	}

	@Log
	@Override
	public void run() {
		while( true ) {
			try {
				DCMessage dcmsg = inputQueue.poll(1900, TimeUnit.MILLISECONDS);
				processMessage(dcmsg);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Log
	public final void processMessage(DCMessage dcmsg) {
		
		if (dcmsg==null)
			return;

		switch (dcmsg.getDCType()) {
		case Data:
			DataMessage<?> dataMessage = (DataMessage<?>) dcmsg;
			processDataMessage(dataMessage);
			break;

		case Command:
			break;

		default:			
			break;
		}		
	}
	
	abstract public Object processDataMessage(DataMessage<?> dataMessage);
}
