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

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import lite.flow.api.util.UniqueId;
import lite.flow.api.util.ActivityInspector.EntryPoint;
import lite.flow.runtime.kiss.data.DataMessage;

/**
 * 	Encapsulates all data needed for method invocation.
 * 	
 * @author ToivoAdams
 *
 */
public class CorrelatedEntryPointArguments {

	public final Object[] 	inputArgs;
	public final boolean[] 	hasValues;
	public final long 		createTime = System.currentTimeMillis();
	public final UniqueId 	dcid;	// data correlation id
	public final EntryPoint entryPoint;
	
	public CorrelatedEntryPointArguments(UniqueId dcid, EntryPoint entryPoint) {
		super();
		requireNonNull(dcid, "CollectedMethodParametersRow() argNames should not be null");
		requireNonNull(entryPoint, "CollectedMethodParametersRow() entryPoint should not be null");
		this.dcid = dcid;
		this.inputArgs = new Object[entryPoint.inputNames.length];
		this.hasValues = new boolean[entryPoint.inputNames.length];
		this.entryPoint = entryPoint;
	}
	
	public final boolean hasAllValues() {
		for (boolean hasValue : hasValues)
			if (hasValue==false)
				return false;
		return true;
	}

	public final void put(DataMessage<?> dmsg) {
		int index = findParameterIndex(dmsg.dataName);
		if (index<0) {
			throw new IllegalArgumentException("incoming data name does not match with any argument name, incoming="
					+ dmsg + " argNames=" + Arrays.toString(entryPoint.inputNames));        		
		}
		if (hasValues[index]) {
			throw new IllegalArgumentException("has value already, rejecting, incoming="
					+ dmsg + " argNames=" + Arrays.toString(entryPoint.inputNames) + " inputParams=" + Arrays.toString(inputArgs) + " hasValues=" + Arrays.toString(hasValues));        					
		}

		inputArgs[index] = dmsg.data;
		hasValues[index] = true;
	}

	private final int findParameterIndex(String dataName) {
		for (int i = 0; i < entryPoint.inputNames.length; i++) {
			String argName = entryPoint.inputNames[i];
			if (argName!=null && argName.equals(dataName))
				return i;
		}
		
		return -1;
	}

	@Override
	public String toString() {
		return "CorrelatedEntryPointArguments[" + ", inputParams="
				+ Arrays.toString(inputArgs) + ", hasValues=" + Arrays.toString(hasValues) + ", createTime="
				+ createTime + ", dcid=" + dcid + ", entryPoint=" + entryPoint + "]";
	}
}
