package org.illinoistechrobotics.jaguar;

import java.util.Arrays;

public class Message {
	private int device;
	private int api_class;
	private int api_index;
	private int manufacturer;
	private int device_type;
	private int data_size;
	private int data[] = new int[CAN.MAX_DATA_BYTES];
	
	public Message(int device, int api_class, int api_index, int manufacturer, int device_type, int data_size, int[] data){
		this.device = device;
		this.api_class = api_class;
		this.api_index = api_index;
		this.manufacturer = manufacturer;
		this.device_type = device_type;
		this.data_size = data_size;
		this.data = data;
	}
	
	public Message(int[] data){
		this.data_size = data.length;
		this.data = data;
	}
	
	public Message(){
		
	}
	
	public boolean isValidSysReply(Message other){
		if (other == null)
			return false;
		if (manufacturer != CAN.MANUFACTURER_SYS)
			return false;
		if (device_type != CAN.DEVTYPE_SYS)
			return false;
		if (api_class != CAN.API_SYS)
			return false;
		if (api_index != other.api_index)
			return false;
		if (device != other.device)
			return false;
		
		return true;
	}
	
	public boolean isValidJaguarReply(Message other){
		if (other == null)
			return false;
		if (manufacturer != CAN.MANUFACTURER_TI)
			return false;
		if (device_type != CAN.DEVTYPE_MOTORCTRL)
			return false;
		if (api_class != other.api_class)
			return false;
		if (api_index != other.api_index)
			return false;
		if (device != other.device)
			return false;
		
		return true;
	}
	
	public boolean isValidAck(Message other){
		if (other == null)
			return false;
		if (manufacturer != CAN.MANUFACTURER_TI)
			return false;
		if (device_type != CAN.DEVTYPE_MOTORCTRL)
			return false;
		if (api_class != CAN.API_ACK)
			return false;
		if (device != other.device)
			return false;
		
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (api_class != other.api_class)
			return false;
		if (api_index != other.api_index)
			return false;
		if (!Arrays.equals(data, other.data))
			return false;
		if (data_size != other.data_size)
			return false;
		if (device != other.device)
			return false;
		if (device_type != other.device_type)
			return false;
		if (manufacturer != other.manufacturer)
			return false;
		return true;
	}

	public int getDevice() {
		return device;
	}
	public void setDevice(int device) {
		this.device = device;
	}
	public int getApi_class() {
		return api_class;
	}
	public void setApi_class(int api_class) {
		this.api_class = api_class;
	}
	public int getApi_index() {
		return api_index;
	}
	public void setApi_index(int api_index) {
		this.api_index = api_index;
	}
	public int getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(int manufacturer) {
		this.manufacturer = manufacturer;
	}
	public int getDevice_type() {
		return device_type;
	}
	public void setDevice_type(int device_type) {
		this.device_type = device_type;
	}
	public int getData_size() {
		return data_size;
	}
	public void setData_size(int data_size) {
		this.data_size = data_size;
	}
	public int[] getData() {
		return data;
	}
	public void setData(int[] data) {
		this.data = data;
	}
	
	public short getDataAsShort(){
		return (short) ((short)data[0] & 0xFF);
	}
	public void setDataAsShort(int v){
		this.data[0] = (byte)(v & 0xFF);
		this.data_size = 1;
	}
	
	public int getDataAsInt(){
		return ((int)data[0] & 0xFF) | (((int)data[1] & 0xFF) << 8);
	}
	public void setDataAsInt(int v){
		this.data[0] = (byte)(v & 0xFF);
		this.data[1] = (byte)((v >> 8) & 0xFF);
		this.data_size = 2;
	}
	
	public long getDataAsLong(){
		return ((long)data[0] & 0xFF) | (((long)data[1] & 0xFF) << 8)
				| (((long)data[2] & 0xFF) << 16) | (((long)data[3] & 0xFF) << 24);
	}
	public void setDataAsLong(long v){
		this.data[0] = (byte)(v & 0xFF);
		this.data[1] = (byte)((v >> 8) & 0xFF);
		this.data[2] = (byte)((v >> 16) & 0xFF);
		this.data[3] = (byte)((v >> 24) & 0xFF);
		this.data_size = 4;
	}
}
