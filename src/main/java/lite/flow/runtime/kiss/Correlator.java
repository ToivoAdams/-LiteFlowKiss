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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import lite.flow.util.UniqueId;
import lite.flow.util.ActivityInspector.EntryPoint;
import lite.flow.runtime.kiss.DistributorOutput.Destination;
import lite.flow.runtime.kiss.data.DataMessage;
import lite.log.api.ExecutionContext;
import lite.log.api.Log;
import lite.log.api.LogFactory;
import lite.log.api.event.MiddleEvent;

/**
 * @author ToivoAdams
 *
 */
public class Correlator extends SequentialActivity {

	// Output is extended Output type
	DistributorOutput<CorrelatedEntryPointArguments> correlatedInputs = new DistributorOutput<>("correlatedInputs");

	private final EntryPoint entryPoint;
	private final String[] argNames;
	
	public Correlator(Integer inputQueueLength, ExecutionContext executionContext, EntryPoint entryPoint, LogFactory logFactory) {
		super(inputQueueLength, executionContext, logFactory);
		this.entryPoint = entryPoint;
		this.argNames = entryPoint.inputNames;
	}

	@Log
	@Override
	public Object processDataMessage(DataMessage<?> dataMessage) {
//    	System.out.println("===============> Correlator.processMessage");
    	
    	CorrelatedEntryPointArguments row = getOrPut(argNames, dataMessage.context.getRequestId(), entryPoint);
    	row.put(dataMessage);

    	if (row.hasAllValues()) {
    //		DataMessage<?> outDataMessage = new DataMessage<CollectedMethodParametersRow>(dataMessage.context, "correlatedInputs", row);
    //		rcc.setRequestContext(dataMessage.context);
    		correlatedInputs.emit(row, dataMessage.context);
    		// we should remove row from map because we are done with this row
    		// Note!!! Actually we should not remove row!!!
    		// We want catch invalid duplicates and CollectedMethodParametersRow will do this!
    		// rowsHolder.remove(dataMessage.context.getRequestId());
    	}

    	return row;
	}

	public static int MAX_PARAMETER_WAIT_TIME = 20000; // milliseconds 
	
	private final Map<UniqueId, CorrelatedEntryPointArguments> rowsHolder = new LinkedHashMap<UniqueId, CorrelatedEntryPointArguments>(70, 0.7f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UniqueId, CorrelatedEntryPointArguments> eldest) {
        	
        	long currentTime = System.currentTimeMillis();
        	if ((eldest.getValue().createTime+MAX_PARAMETER_WAIT_TIME)<currentTime) {
        		// too old, inform manager and remove
				MiddleEvent middleEvent = new MiddleEvent(Level.WARNING, "remove old CollectedMethodParametersRow=" + eldest, null, executionContext, logFactory.newCid());
				logFactory.logger().log(middleEvent);
                return true; 
        	}
        	return false;
        }        
    };

    public final CorrelatedEntryPointArguments getOrPut(String[] argNames, UniqueId dcid, EntryPoint entryPoint) {
    	if (rowsHolder.containsKey(dcid))
    		return rowsHolder.get(dcid);

    	CorrelatedEntryPointArguments collectedMethodParametersRow = new CorrelatedEntryPointArguments(dcid, entryPoint);
    	rowsHolder.put(dcid, collectedMethodParametersRow);
    	return collectedMethodParametersRow;
    }

	@Log
	@Override
	public void addDestination(String ouputName, Consumer consumer, String destinationName) {
		// Correlator has only one output, so we don't use ouputName here!
		correlatedInputs.addDestination(new Destination(destinationName, consumer));
	}

}
