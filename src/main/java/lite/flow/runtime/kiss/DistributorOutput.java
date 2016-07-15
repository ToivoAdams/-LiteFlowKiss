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

import java.util.ArrayList;
import java.util.List;

import lite.flow.runtime.kiss.data.DataMessage;
import lite.flow.api.activity.RequestContext;
import lite.flow.runtime.kiss.Consumer;

public class DistributorOutput<T> extends NamedOutput<T> {

	public static class Destination {
		public final String destinationName;
		public final Consumer consumer;
		public Destination(String destinationName, Consumer consumer) {
			super();
			this.destinationName = destinationName;
			this.consumer = consumer;
		}
	}

	private final List<Destination> destinations = new ArrayList<>();
	public final RequestContextCarrier rcc;
	
	public DistributorOutput(String outputName, RequestContextCarrier rcc) {
		super(outputName);
		this.rcc = rcc;
	}

	public DistributorOutput(String outputName) {
		super(outputName);
		this.rcc = null;
	}

	public final void addDestination(Destination destination) {
		destinations.add(destination);
	}

	@Override
	public void emit(T data) {
		if (rcc==null)
			throw new IllegalArgumentException("Distributor: Cannot emit because RequestContextCarrier is null.");
		
		emit(data, rcc.getRequestContext());
	}

	@Override
	public void emit(T data, RequestContext requestContext) {
		distribute(data, requestContext);
	}

	public void distribute(T data, RequestContext requestContext) {
		for (Destination destination : destinations) {
			DataMessage<?> outDataMessage = new DataMessage<>(rcc.getRequestContext(), destination.destinationName, data);		
			destination.consumer.enqueue(outDataMessage);
		}
	}
}
