/*
Copyright 2014 (c) Illinois Tech Robotics <robotics.iit@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.illinoistechrobotics.common;

public abstract class Communication extends Thread{

	private Queue recv = null;
	
	private final String DEBUG_HEADER = "#";
	private final String HEADER = "$";
	private final String CHECK_SUM = "*";
	private final String FOOTER = "\n";
	private StringBuilder sBuf = new StringBuilder();
	private ReadState state = ReadState.LOOKING_FOR_HEADER;

	public Communication(Queue r){
		this.recv = r;
	}
	
	private enum ReadState{
		LOOKING_FOR_HEADER,
		READING_DATA,
		CALCULATE_CHECKSUM,
		DEBUG;
	}
	
	protected void parseMessage(StringBuffer sb){
		while(sb.length()>0){
			String st = sb.substring(0, 1);
			sBuf.append(st);
			sb.delete(0, 1);
			
			switch(state){
			case LOOKING_FOR_HEADER:
				if(sBuf.indexOf(HEADER) != -1){
					sBuf.delete(0, sBuf.indexOf(HEADER)); //remove data in front of header
					state = ReadState.READING_DATA;
				}
				else if(sBuf.indexOf(DEBUG_HEADER) != -1){
					sBuf.delete(0, sBuf.indexOf(DEBUG_HEADER)); //remove data in front of header
					state = ReadState.DEBUG;
				}
				else{
					sBuf.delete(0, sBuf.length()); //remove everything since no valid data
				}
				break;
			case READING_DATA:
				if(sBuf.indexOf(FOOTER) != -1){
					state = ReadState.CALCULATE_CHECKSUM;
					//continue to CALCULATE_CHECKSUM;
				}
				else if(sBuf.indexOf(HEADER,1)>-1)
				{
					//new header "start over"
					sBuf.delete(0,sBuf.indexOf(HEADER,1));
					break;
				}
				else
				{
					break;
				}
				//break left out
			case CALCULATE_CHECKSUM:
				state = ReadState.LOOKING_FOR_HEADER; //Moved to top in case of exception will go back to the begining
				
				Event ev = new Event();
				String fullMessage = sBuf.substring(0, sBuf.indexOf(FOOTER));
				sBuf.delete(0,sBuf.indexOf(FOOTER));
				
				String checksumMessage = fullMessage.substring(1, fullMessage.indexOf(CHECK_SUM));
				
				int checksum = 0;
				for(int i = 0; i < checksumMessage.length(); i++) //checksum not include the start byte
				{
					checksum = checksum^(((int)checksumMessage.charAt(i))&0xFF);
				}


				String[] token = fullMessage.split("["+HEADER+",\\*"+FOOTER+"\r]");
				

				if(Integer.parseInt(token[5],16) == checksum )
				{
					ev.setCommand(EventEnum.getEvent(Integer.parseInt(token[1],16))); //command
					ev.setIndex((short)Integer.parseInt(token[2],16)); //index
					Event.ValueType type = Event.ValueType.getType(Integer.parseInt(token[4]));
					switch(type){
					case BYTE:
						ev.setBValue((short)Integer.parseInt(token[3],16));
						break;
					case INTEGER:
						ev.setValue(Integer.parseInt(token[3],16));
						break;
					case LONG:
						ev.setLValue(Long.parseLong(token[3],16));
						break;
					case FLOAT:
						ev.setFValue(Float.intBitsToFloat((int)Long.parseLong(token[3],16))); //since float and long same amount of bits need to set both
						break;
					case STRING:
						ev.setSValue(token[3]);
						break;
					default:
						ev.setSValue(token[3]);
						break;
					}

					try{
						recv.put(ev);
					}
					catch(Exception e){
					}
				}
				break;
			case DEBUG:
				if(sBuf.indexOf(FOOTER) != -1){
					state = ReadState.LOOKING_FOR_HEADER;
					String debugMessage = sBuf.substring(1, sBuf.indexOf(FOOTER));
					sBuf.delete(0,sBuf.indexOf(FOOTER));
					System.out.println(debugMessage);
				}
				break;
			default:
				break;
			}
		}
	}
	
	public abstract void sendEvent(Event ev);
	
}
