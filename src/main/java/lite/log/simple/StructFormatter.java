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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import lite.log.api.event.EndEvent;
import lite.log.api.event.MiddleEvent;
import lite.log.api.event.StartEvent;

public class StructFormatter extends SimpleFormatter {

	protected final Date date = new Date();
	protected final String format = "{0,date} {0,time}";
	protected final MessageFormat formatter = new MessageFormat(format);

    protected Object args[] = new Object[1];
	private final String lineSeparator = System.getProperty("line.separator");

	@Override
	public synchronized String format(LogRecord record) {
		StringBuilder sb = new StringBuilder(100);
		
		date.setTime(record.getMillis());
//		args[0] = date;
//		StringBuffer text = new StringBuffer();

//		formatter.format(args, text, null);
		
		SimpleDateFormat datef = new SimpleDateFormat("dd-MMM HH:mm:ss.SSS", Locale.US);
		String datetime = datef.format(date);
		
		sb.append(datetime);
		sb.append(" ");
		
		sb.append( String.format("%1$7s", record.getLevel().getName()) );
		sb.append(" ");
		
		sb.append(shortClassName(record));
		sb.append(" ");
		sb.append( String.format("%1$12s", record.getSourceMethodName()));
		sb.append(" ");

		typeSpecific(sb, record);
		sb.append( lineSeparator);
		
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
            sb.append(throwable);
        }

		return sb.toString();
	}

	private String shortClassName(LogRecord record) {
		String format = "%1$24s";
		String name = record.getSourceClassName();
		if (name==null)
			return String.format(format, name);
		
		String last = name.substring(name.lastIndexOf('.') + 1);
		return String.format(format, last);
	}

	private void typeSpecific(StringBuilder sb, LogRecord record) {
		if (record instanceof StartEvent) {
			StartEvent startEvent = (StartEvent) record;
			sb.append( String.format("%1$10s", startEvent.executionContext) );
			sb.append(" ");
			sb.append( String.format("%1$10s", startEvent.requestContext) );
			sb.append(" Start");
			sb.append( startEvent.eventCorrelationId.getIdValue());
			sb.append(" ");
//			sb.append( String.format("%1$5s ", startEvent.eventCorrelationId));
			sb.append( inputArgsString(startEvent));
			sb.append( record.getMessage());
		} else if (record instanceof MiddleEvent) {
			MiddleEvent middleEvent = (MiddleEvent) record;
			sb.append( String.format("%1$10s", middleEvent.executionContext) );
			sb.append(" ");
			sb.append( String.format("%1$10s", middleEvent.requestContext) );
			sb.append("      ");
			sb.append( middleEvent.eventCorrelationId.getIdValue());
			sb.append(" ");
			sb.append( record.getMessage());			
		} else if (record instanceof EndEvent) {
			EndEvent endEvent = (EndEvent) record;
			sb.append( String.format("%1$10s", endEvent.executionContext) );
			sb.append(" ");
			sb.append( String.format("%1$10s", endEvent.requestContext) );
			sb.append("   End");
			sb.append(endEvent.eventCorrelationId.getIdValue());
			sb.append(" ");
			sb.append( outputString(endEvent));
			sb.append( record.getMessage());			
		} else
			sb.append( record.getMessage());
		
	}

	private Object outputString(EndEvent endEvent) {
		if (endEvent.outputNames==null || endEvent.outputValues==null)
			return "NA";
		
		StringBuilder sb = new StringBuilder(100);
		for (int i = 0; i < endEvent.outputNames.length; i++) {
			if (i>=endEvent.outputValues.length)
				break;
			String name = endEvent.outputNames[i];
			Object value = endEvent.outputValues[i];
			sb.append(name+"="+value+" ");
		}
		
		return sb.toString();
	}

	private String inputArgsString(StartEvent startEvent) {
		if (startEvent.argNames==null || startEvent.argValues==null)
			return "NA";
		
		StringBuilder sb = new StringBuilder(100);
		for (int i = 0; i < startEvent.argNames.length; i++) {
			if (i>=startEvent.argValues.length)
				break;
			String name = startEvent.argNames[i];
			Object value = startEvent.argValues[i];
			sb.append(name+"="+value+" ");
		}
		
		return sb.toString();
	}

}
