package org.illinoistechrobotics.jaguar;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;

public class Jaguar {
	
	private static final int READ_TIME = 100; //millisecond
	
	private SerialPort serialPort = null;
	private CommPortIdentifier comId = null;
	private InputStream input = null;
	private OutputStream output = null;

	public void open(String port){
		
		Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
		
		while (portEnum.hasMoreElements()){
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			if(currPortId.getName().equals(port)){
				comId = currPortId;		
				break;
			}
		}
		
		if(comId == null){
			System.err.println("Can not open serial port " + port + ".");
		}
		
		try{
			//Register the name the name and timeout of the port
			serialPort = (SerialPort) comId.open(this.getClass().getName(),2000);
			
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
			
			serialPort.notifyOnDataAvailable(true);
		}
		catch(Exception e){
			System.out.println("Can not open serial port " + port + ".");
		}
		
	}
	
	public void close(){
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}
	
	private void send(Message message){
		
		byte[] out = new byte[CAN.MAX_MSG_BYTES + CAN.HEADER_SIZE];
		
		out[0] = (byte)(CAN.START_OF_FRAME & 0xFF);
		out[1] = (byte)((CAN.CAN_ID_SIZE + message.getData_size()) & 0xFF);
	
		out[2] = (byte)(message.getDevice() & 0xFF);
		out[2] |= (byte)((message.getApi_index() << 6) & 0xFF);
		out[3] = (byte)((message.getApi_index() >> 2) & 0xFF);
		out[3] |= (byte)((message.getApi_class() << 2) & 0xFF);
		out[4] = (byte)(message.getManufacturer() & 0xFF);
		out[5] = (byte)(message.getDevice_type() & 0xFF);
		
		int pad = 0;
		int data[] = message.getData();
		for(int i=0; i<message.getData_size() && data != null; i++){
			if(data[i] == CAN.START_OF_FRAME){
				out[CAN.HEADER_SIZE + i + pad] = (byte)(CAN.ENCODE_BYTE_A & 0xFF);
				pad++;
				out[CAN.HEADER_SIZE + i + pad] = (byte)(CAN.ENCODE_BYTE_A & 0xFF);
			}
			else if(data[i] == CAN.ENCODE_BYTE_A){
				out[CAN.HEADER_SIZE + i + pad] = (byte)(CAN.ENCODE_BYTE_A & 0xFF);
				pad++;
				out[CAN.HEADER_SIZE + i + pad] = (byte)(CAN.ENCODE_BYTE_B & 0xFF);
			
			}
			else{
				out[CAN.HEADER_SIZE + i + pad] = (byte)(data[i] & 0xFF);
			}
		}
		
		try {
			output.write(out, 0, CAN.HEADER_SIZE + message.getData_size() + pad);
		} catch (IOException e) {
			System.out.println("Can not write to Jagurs");
		}
		
	}
	
	private Message read(){	
		
		while(true){
			int startByte = tryRead(); 
			if(startByte == CAN.START_OF_FRAME){
				break;
			}
			else if(startByte == -1){
				return null;
			}
			
		}
		
		int size = tryRead();
		if(size == CAN.START_OF_FRAME || size == -1){
			return null;
		}
		
		int data[] = new int[size];
		
		for(int i=0; i<size; i++){
			int d = tryRead();
			if(d == CAN.START_OF_FRAME){
				return null;
			}
			if(d == CAN.ENCODE_BYTE_A){
				d = tryRead();
				if(d == CAN.ENCODE_BYTE_A){
					d = CAN.START_OF_FRAME;
				}
				else if(d == CAN.ENCODE_BYTE_B){
					d = CAN.ENCODE_BYTE_A;
				}
				else{
					return null;
				}
			}
			data[i] = d;	
		}
				
		return new Message(data[2] & 0x3F, 
				(data[2] >> 6) | ((data[3] & 0x3) << 2), 
				data[3] >>2, 
				data[4], 
				data[5], 
				size - CAN.CAN_ID_SIZE, 
				Arrays.copyOfRange(data, CAN.HEADER_SIZE, data.length));
	}
	
	private int tryRead(){
		long startTime = System.currentTimeMillis();
		byte[] buf = new byte[1];
		int length = 0;
		try{
			while(length != 0 && (System.currentTimeMillis() - startTime) > READ_TIME){
				length = input.read(buf, 0, 1);
				if(length == 0){
					Thread.sleep(10);
				}
			}
			return (int)buf[0] & 0xFF; //treat as unsigned byte, need this trick
		}
		catch(Exception e){
			return -1;
		}
	}
	
	private Message getValidSysReply(){
		Message m = new Message();
		m.setManufacturer(CAN.MANUFACTURER_SYS);
		m.setDevice_type(CAN.DEVTYPE_SYS);
		m.setApi_class(CAN.API_SYS);
		return m;
	}
	
	private Message getInitJaguarMessage(){
		Message m = new Message();
		m.setManufacturer(CAN.MANUFACTURER_TI);
		m.setDevice_type(CAN.DEVTYPE_MOTORCTRL);
		return m;
	}
	
	public void sysHeartBeat(int device){
		Message m = this.getValidSysReply(); 
		m.setApi_index(CAN.SYS_HEARTBEAT);
		m.setDevice(device);
		
		this.send(m);
	}
	
	public void sysSyncUpdate(int mask){
		Message m = this.getValidSysReply(); 
		m.setApi_index(CAN.SYS_SYNC_UPDATE);
		m.setDevice(0);
		m.setDataAsShort(mask);
		
		this.send(m);
	}
	
	public void sysHalt(int device){
		Message m = this.getValidSysReply(); 
		m.setApi_index(CAN.SYS_HALT);
		m.setDevice(device);
		
		this.send(m);
	}
	
	public void sysReset(int device){
		Message m = this.getValidSysReply(); 
		m.setApi_index(CAN.SYS_RESET);
		m.setDevice(device);
		
		this.send(m);
	}
	
	public void sysResume(int device){
		Message m = this.getValidSysReply(); 
		m.setApi_index(CAN.SYS_RESUME);
		m.setDevice(device);
		
		this.send(m);
	}
	
	public int statusOutputPercent(int device){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_STATUS);
		m.setApi_index(CAN.STATUS_OUTPUT_PERCENT);
		m.setDevice(device);
		
		this.send(m);
		Message reply = this.read();
		Message ack = this.read();
		
		if(m.isValidAck(ack) && m.isValidJaguarReply(reply)){
			return m.getDataAsInt();
		}
		else{
			return -1;
		}
	}
	
	public int statusTemperature(int device){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_STATUS);
		m.setApi_index(CAN.STATUS_TEMPERATURE);
		m.setDevice(device);
		
		this.send(m);
		Message reply = this.read();
		Message ack = this.read();
		
		if(m.isValidAck(ack) && m.isValidJaguarReply(reply)){
			return m.getDataAsInt();
		}
		else{
			return -1;
		}
	}
	
	public long statusPosition(int device){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_STATUS);
		m.setApi_index(CAN.STATUS_POSITION);
		m.setDevice(device);
		
		this.send(m);
		Message reply = this.read();
		Message ack = this.read();
		
		if(m.isValidAck(ack) && m.isValidJaguarReply(reply)){
			return m.getDataAsLong();
		}
		else{
			return -1;
		}
	}
	
	public long statusMode(int device){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_STATUS);
		m.setApi_index(CAN.STATUS_MODE);
		m.setDevice(device);
		
		this.send(m);
		Message reply = this.read();
		Message ack = this.read();
		
		if(m.isValidAck(ack) && m.isValidJaguarReply(reply)){
			return m.getDataAsShort();
		}
		else{
			return -1;
		}
	}
	
	public boolean voltageEnable(int device){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_VOLTAGE);
		m.setApi_index(CAN.VOLTAGE_ENABLE);
		m.setDevice(device);
		
		this.send(m);
		Message ack = this.read();
		
		return m.isValidAck(ack);
	}
	
	public boolean voltageDisable(int device){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_VOLTAGE);
		m.setApi_index(CAN.VOLTAGE_DISABLE);
		m.setDevice(device);
		
		this.send(m);
		Message ack = this.read();
		
		return m.isValidAck(ack);
	}
	
	public boolean voltageSet(int device, int voltage){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_VOLTAGE);
		m.setApi_index(CAN.VOLTAGE_SET);
		m.setDevice(device);
		m.setDataAsInt(voltage);
		
		this.send(m);
		Message ack = this.read();
		
		return m.isValidAck(ack);
	}
	
	public boolean voltageSetSync(int device, int voltage, int group){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_VOLTAGE);
		m.setApi_index(CAN.VOLTAGE_SET);
		m.setDevice(device);
		m.setDataAsInt(voltage);
		m.getData()[2] = (byte)(group & 0xFF);
		m.setData_size(3);
		
		this.send(m);
		Message ack = this.read();
		
		return m.isValidAck(ack);
	}
	
	public int voltageGet(int device){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_VOLTAGE);
		m.setApi_index(CAN.VOLTAGE_SET);
		m.setDevice(device);
		
		this.send(m);
		Message reply = this.read();
		Message ack = this.read();
		
		if(m.isValidAck(ack) && m.isValidJaguarReply(reply)){
			return m.getDataAsInt();
		}
		else{
			return -1;
		}
	}
	
	public boolean voltageRamp(int device, int ramp){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_VOLTAGE);
		m.setApi_index(CAN.VOLTAGE_SET);
		m.setDevice(device);
		m.setDataAsInt(ramp);
		
		this.send(m);
		Message ack = this.read();
		
		return m.isValidAck(ack);
	}
	
	public boolean positionEnable(int device, int position){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_POSITION);
		m.setApi_index(CAN.POSITION_ENABLE);
		m.setDevice(device);
		m.setDataAsLong(position);
		
		this.send(m);
		//Message ack = this.read();
		
		return true;// m.isValidAck(ack);
	}
	
	public boolean positionDisable(int device){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_POSITION);
		m.setApi_index(CAN.POSITION_DISABLE);
		m.setDevice(device);
		
		this.send(m);
		Message ack = this.read();
		
		return m.isValidAck(ack);
	}
	
	public boolean positionSet(int device, int position){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_POSITION);
		m.setApi_index(CAN.POSITION_SET);
		m.setDevice(device);
		m.setDataAsLong(position);
		
		this.send(m);
		//Message ack = this.read();
		
		return true;// m.isValidAck(ack);
	}
	
	public boolean positionSetSync(int device, int position, int group){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_POSITION);
		m.setApi_index(CAN.POSITION_SET);
		m.setDevice(device);
		m.setDataAsLong(position);
		m.getData()[4] = (byte)(group & 0xFF);
		m.setData_size(5);
		
		this.send(m);
		Message ack = this.read();
		
		return m.isValidAck(ack);
	}
	
	public long positionGet(int device){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_POSITION);
		m.setApi_index(CAN.POSITION_SET);
		m.setDevice(device);
		
		this.send(m);
		Message reply = this.read();
		Message ack = this.read();
		
		if(m.isValidAck(ack) && m.isValidJaguarReply(reply)){
			return m.getDataAsLong();
		}
		else{
			return -1;
		}
	}
	
	public boolean positionRefEncoder(int device){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_POSITION);
		m.setApi_index(CAN.POSITION_REF);
		m.setDevice(device);
		m.setDataAsShort(CAN.POSITION_ENCODER);
		
		this.send(m);
		Message ack = this.read();
		
		return m.isValidAck(ack);
	}
	
	public boolean positionP(int device, int p){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_POSITION);
		m.setApi_index(CAN.POSITION_P);
		m.setDevice(device);
		m.setDataAsLong(p);
		
		this.send(m);
		//Message ack = this.read();
		
		return true;//m.isValidAck(ack);
	}
	
	public boolean positionI(int device, int i){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_POSITION);
		m.setApi_index(CAN.POSITION_I);
		m.setDevice(device);
		m.setDataAsLong(i);
		
		this.send(m);
		//Message ack = this.read();
		
		return true; //m.isValidAck(ack);
	}
	
	public boolean positionD(int device, int d){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_POSITION);
		m.setApi_index(CAN.POSITION_D);
		m.setDevice(device);
		m.setDataAsLong(d);
		
		this.send(m);
		//Message ack = this.read();
		
		return true;//m.isValidAck(ack);
	}
	
	public boolean positionPID(int device, int p, int i, int d){
		return this.positionP(device, p) 
				&& this.positionI(device, i) 
				&& this.positionD(device, d);
	}
	
	public boolean configEncoderLines(int device, int lines){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_CONFIG);
		m.setApi_index(CAN.CONFIG_ENCODER_LINES);
		m.setDevice(device);
		m.setDataAsInt(lines);
		
		this.send(m);
		//Message ack = this.read();
		
		return true; //m.isValidAck(ack);
	}
	
	public int getEncoderLines(int device){
		Message m = this.getInitJaguarMessage();
		m.setApi_class(CAN.API_CONFIG);
		m.setApi_index(CAN.CONFIG_ENCODER_LINES);
		m.setDevice(device);
		
		this.send(m);
		Message reply = this.read();
		Message ack = this.read();
		
		if(m.isValidAck(ack) && m.isValidJaguarReply(reply)){
			return m.getDataAsInt();
		}
		else{
			return -1;
		}
	}
	
}
