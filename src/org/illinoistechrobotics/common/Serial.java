/*
Copyright 2013 (c) Illinois Tech Robotics <robotics.iit@gmail.com>

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;


public class Serial extends Communication implements SerialPortEventListener {

	private SerialPort serialPort = null;
	private CommPortIdentifier comId = null;
	private InputStream input = null;
	private OutputStream output = null;
	private boolean isOpen = false;
	private String serialPortName;
	private int baudRate = 0;
	
	private byte[] buf = new byte[1024];

	public Serial(Queue r){
		super(r);
	}
	
	/**
	 * Opens the Serial port with the baud and port_name given
	 */
	public boolean OpenSerial(int baud, String port){
		serialPortName = port;
		baudRate = baud;
		comId = null;
		Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
		
		while (portEnum.hasMoreElements()){
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			if(currPortId.getName().equals(port)){
				comId = currPortId;		
				break;
			}
		}
		
		if(comId == null){
			System.err.println("Can not open serial port");
			return false;
		}
		
		try{
			serialPort = (SerialPort) comId.open(this.getClass().getName(),2000);
			
			serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
			
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			Thread.sleep(1500);
			isOpen = true;
		}
		catch(Exception e){
			return false;
		}
		
		return true;
	}
	
	public static List<CommPortIdentifier> getSerialPorts(){
		try{
			List<CommPortIdentifier> commPortList = new ArrayList<CommPortIdentifier>();
			Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
			while(portEnum.hasMoreElements()){
				CommPortIdentifier port = (CommPortIdentifier)portEnum.nextElement();
				commPortList.add(port);
			}
			return commPortList;
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
		return null;
	}
	
	public synchronized void closeSerial() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
			isOpen = false;
		}
	}
		
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {		
			int length = 0;
			try {
				length = input.read(buf, 0, 1024);
			} catch (IOException e) {
			}
			
			StringBuffer received = new StringBuffer();
			received.append(new String(buf,0, length));
			parseMessage(received);
		}
	}
	
	public boolean isOpen(){
		return isOpen;
	}
	
	public String getPortName(){
		return serialPortName;
	}
	
	public int getBaudRate(){
		return baudRate;
	}
	
	public CommPortIdentifier getCommPortIdentifier(){
		return comId;
	}
	
	public synchronized void sendEvent(Event ev){
		try{
			output.write(ev.toStringSend().getBytes());   //write needs a byte array instead of a string
		}
		catch(Exception e){
		}
	}	
}
