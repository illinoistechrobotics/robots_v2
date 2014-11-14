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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Ethernet extends Communication{
	
	private DatagramSocket sockSend;
	private DatagramSocket sockRevc;
    private volatile boolean run = false;
    private boolean isConnected = false;
    private String ipAddress;
    private int portNumber;
    private int portListen;
    
    private byte[] buf = new byte[1600]; 
	private DatagramPacket packet = new DatagramPacket(buf, buf.length);
    
	public Ethernet(Queue r){
		super(r);
	}
	
	public boolean connect(String ip, int port, int portL) {
		boolean connect = true;
		ipAddress = ip;
		portNumber = port;
		portListen = portL;
		
		try {
			sockSend = new DatagramSocket();
			sockRevc = new DatagramSocket(portListen);
			isConnected = true;
			this.start();
		} catch (IOException ioe) {
			System.out.println("Can not connect");
			connect = false;
		}
		return connect;
	}
	
	public void stopThread() {
        if (run != false) {
            run = false;
            this.interrupt();
            sockSend.close();
            sockRevc.close();
        }
        isConnected = false;
    }
	
	@Override
	public void run() {
        run = true;	
        while (run){
        	try {
        		sockRevc.receive(packet);
        		
        		String r = new String(packet.getData(), 0, packet.getLength());
        		StringBuffer received = new StringBuffer(r);
        		parseMessage(received);
        		
            	try {
            		Thread.sleep(0,100);
            	} 
            	catch (InterruptedException ie) {
            	}
        	} catch(Exception e){
				System.out.println(e.toString());
        	}
        }
    }
	
	public String getIPAddress(){
		return ipAddress;
	}
	
	public int getPortNumber(){
		return portNumber;
	}
	
	public int getListeningPort(){
		return portListen;
	}
	
	public boolean isConnected(){
		return isConnected;
	}
    
	@Override
	public synchronized void sendEvent(Event ev) {
        try {
        	String s = ev.toStringSend();
            DatagramPacket packet = new DatagramPacket(s.getBytes(), s.length(), InetAddress.getByName(ipAddress), portNumber);
            sockRevc.send(packet);
        } 
        catch (Exception e) {
        }

    }
}
