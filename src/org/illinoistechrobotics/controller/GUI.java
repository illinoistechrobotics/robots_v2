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
package org.illinoistechrobotics.controller;

import gnu.io.CommPortIdentifier;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.illinoistechrobotics.common.Communication;
import org.illinoistechrobotics.common.Ethernet;
import org.illinoistechrobotics.common.Event;
import org.illinoistechrobotics.common.EventEnum;
import org.illinoistechrobotics.common.Joystick;
import org.illinoistechrobotics.common.Keyboard;
import org.illinoistechrobotics.common.Queue;
import org.illinoistechrobotics.common.RobotEnum;
import org.illinoistechrobotics.common.Serial;
import org.illinoistechrobotics.common.Timer.TimerEnum;

public class GUI extends Thread{

	public JFrame frmIllinoisTechRobotics;
	public JButton btnGeneralStatus;
	private JRadioButton rdbtnXbee;
	private JRadioButton rdbtnWifi;
	private JCheckBox chckbxJoystick;
	private JCheckBox chckbxKeyboard;
	private JTextField txtIPAddress;
	private JTextField txtPortNumber;
	private JTextField txtListeningPort;
	private JComboBox<String> comboBox_SerialPort;
	private JComboBox<Integer> comboBox_BaudRate;
	private JComboBox<String> comboBox_JoyStick;
	private JTabbedPane tabbedPane;
	private JTextField txtMessage;
	private JTextArea textArea;
	
	private JButton[] btnBut = new JButton[12];
	private JButton[] btnD_Pad = new JButton[9];
	private JSlider sldX1;
	private JSlider sldX2;
	private JSlider sldY1;
	private JSlider sldY2;
	
	public JButton btnPenguinConnected;
	public JToggleButton tglbtnConnectToPenguin;
	public JSlider sldRoll;
	public JSlider sldPitch;
	public JButton btnUpdatePid;
	public JSlider sldMot1;
	public JSlider sldMot2;
	public JSlider sldMot3;
	public JSlider sldMot4;
	public JLabel lblMot1;
	public JLabel lblMot2;
	public JLabel lblMot3;
	public JLabel lblMot4;
	public JLabel lblPitch;
	public JLabel lblRoll;
	public JLabel lblYaw;
	
	public JButton btnModulusConnected;
	public JToggleButton tglbtnConnectToModulus;
	
	//TODO:
	//Public decelerations for button and toggle button and other GUI objects for each robot
	public JButton btnSampleConnected;
	public JToggleButton tglbtnConnectToSample;
	
	private Timer trSerialCommChecker;
	private Timer trStanbyQueueReading;
	private Queue queue = new Queue(1000);
	private org.illinoistechrobotics.common.Timer timer = new org.illinoistechrobotics.common.Timer(queue);
	private Serial serial = new Serial(queue);
	private Ethernet ethernet = new Ethernet(queue);
	private Joystick joy = new Joystick(queue);
	private Keyboard key = new Keyboard(queue);
	private GUI dis = this;
	
	public enum GUIEnum
	{
		
		PENGUIN_UPDATE_PID (1);
		
		public int value;
		private static final Map<Integer,GUIEnum> lookup = new HashMap<Integer,GUIEnum>();
	    static {
	    	for(GUIEnum s : EnumSet.allOf(GUIEnum.class))
	         lookup.put(s.getValue(), s);
	    }
	    public static GUIEnum getGUIEnum(int value){
	    	return lookup.get(value);
	    }
		private GUIEnum(int v){
	    	this.value = v;
	    }
		public int getValue(){
	    	return this.value;
	    }
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frmIllinoisTechRobotics.setVisible(true);
					window.init();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});		
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
		redirectSystemStreams();
	}
	
	public class deviceChecker extends TimerTask{
	    
		@Override
        public void run(){
        	try{
        		List<CommPortIdentifier> com = Serial.getSerialPorts();
    			//only change the layout if the number of ports changed
    			if( com.size() != comboBox_SerialPort.getItemCount()){
    				comboBox_SerialPort.removeAllItems();
    				for(int i=com.size()-1; i>=0; i--){ //put them on in reverse order since high comm port is the more likely to be chosen
    					comboBox_SerialPort.addItem(com.get(i).getName());
    					if(serial.isOpen() && serial.getName().equals(com.get(i).getName())){
    						comboBox_SerialPort.setSelectedIndex(i);
    					}
    				}
    			}
        		
        		if(rdbtnXbee.isSelected()){		
        			if(serial.isOpen() && 
        				!(serial.getName().equals(comboBox_SerialPort.getSelectedIndex())
        				&&
        				serial.getBaudRate() == Integer.parseInt(comboBox_BaudRate.getSelectedItem().toString()))		
        			){
        				serial.closeSerial();
        			}

        			if(serial.isOpen() == false)
        			{
        				if(comboBox_BaudRate.getSelectedItem()!=null && comboBox_SerialPort.getSelectedItem()!=null){
        					serial.openSerial(Integer.parseInt(comboBox_BaudRate.getSelectedItem().toString()),comboBox_SerialPort.getSelectedItem().toString());	
        				}
        			}
        		}
        		else{
        			if(serial.isOpen()){
        				serial.closeSerial();
        			}
        		}
        		
        		if(rdbtnWifi.isSelected()){
        			if(ethernet.isConnected() &&
        					!(ethernet.getIPAddress().equals(txtIPAddress.getText()) 
        					&&
        					ethernet.getPortNumber() != Integer.parseInt(txtPortNumber.getText())
        					&&
        					ethernet.getListeningPort() != Integer.parseInt(txtListeningPort.getText()))
        			){
        				ethernet.stopThread(); 
        				ethernet = new Ethernet(queue);
        			}

        			if(ethernet.isConnected()==false){
        				ethernet.connect(txtIPAddress.getText(), Integer.parseInt(txtPortNumber.getText()), Integer.parseInt(txtListeningPort.getText()));
        			}
        		}
        		else
        		{
        			if(ethernet.isConnected() == true){
        				ethernet.stopThread();
        				ethernet = new Ethernet(queue);
        			}
        		}
        		
        		List<String> con = Joystick.getJoystickNames();
        		//only change the layout if the number of ports changed
        		if(con.size() != comboBox_JoyStick.getItemCount()){
        			comboBox_JoyStick.removeAllItems();
    				for(int i=0; i<con.size(); i++){
    					comboBox_JoyStick.addItem(con.get(i));
    				}
    			}
        		
        		if(chckbxJoystick.isSelected()){ 	
        			if(joy.checkJoystick() == false || (joy.getJoyName() != null && !joy.getJoyName().equals((String)comboBox_JoyStick.getSelectedItem()))){
        					//joystick disconnected stop joy and start searching again 
        					joy.stopThread();
        					joy = new Joystick(queue);
        			}
        			if(joy.getJoy() == null){
        				joy.listen((String)comboBox_JoyStick.getSelectedItem());
        			}
        		}
        		else{
        			if(joy.getJoy() != null){
        				joy.stopThread();
        				joy = new Joystick(queue);
        			}
        		}
        		
        		if(chckbxKeyboard.isSelected() && !key.isListening()){
        			key.listen();
        		}
        		else if(!chckbxKeyboard.isSelected() && key.isListening()){
        			key.stop();
        		}
        	}
        	catch(Exception e){
        		e.printStackTrace();
       		}
        }
    }

	public class StanbyQueueReading extends TimerTask{
	    
		int heartbeatcount = 0;
        
		@Override
		public void run(){
    		while(queue.getSize() > 0)
    		{
    			Event ev = queue.take();
    			switch(ev.getCommand()){
    			case CMD_HEARTBEAT:
    				//check the heartbeat and update connection status
    				heartbeatcount = 0;
    				btnGeneralStatus.setBackground(Color.GREEN);
    				btnGeneralStatus.setText(RobotEnum.getRobot(ev.getIndex()).toString());
    				break;
    			case TIMER:
    				//ethernet.sendEvent(new Event(EventEnum.ROBOT_EVENT_CMD_HEARTBEAT,(short)RobotEnum.COMPUTER.getValue(),(short)0));
    				if (ev.getIndex() == TimerEnum.TIMER_HEARTBEAT.value){
    					heartbeatcount++;
    					if(heartbeatcount > 4){
    						btnGeneralStatus.setBackground(Color.RED);
    	    				btnGeneralStatus.setText("");
    					}
    				}
    			case JOY_AXIS:
    				updateAxisGUI(ev);
    				break;
    			case JOY_BUTTON:
    				updateButtonGUI(ev);
    				break;
    			case JOY_HAT:
    				updateHatGUI(ev);
    				break;
    			case JOY_STATUS:
    				updateJoyStatus(ev);
    				break;
				default:
					break;
    			}
    		}
        }
	}
	
	public void init()
	{
		timer.start();
        trSerialCommChecker = new Timer();
        trSerialCommChecker.schedule(new deviceChecker(), 0, 1000);
        trStanbyQueueReading = new Timer();
        trStanbyQueueReading.schedule(new StanbyQueueReading(), 0, 25);
	}
	
	private boolean running = false;
	
	private Controller robot;
	public JTextField txtPD;
	public JTextField txtPI;
	public JTextField txtPP;
	public JTextField txtRP;
	public JTextField txtYP;
	public JTextField txtRI;
	public JTextField txtYI;
	public JTextField txtRD;
	public JTextField txtYD;
	
	private class btnStartListener implements ActionListener{
	  	
		@Override
		public void actionPerformed(ActionEvent event){
	  		JToggleButton btnTemp = (JToggleButton)event.getSource();
	  		
	  		if(running == true && btnTemp.getText().equals("Connect")){
	  			btnTemp.setSelected(false);
	  			return;
  			}
	  		
	  		Communication comm = null;
	  		
	  		if(rdbtnXbee.isSelected()){
	  			comm = (Communication)serial;
	  		}
	  		else if(rdbtnWifi.isSelected()){
	  			comm = (Communication)ethernet;
	  		}
	  		
	  		//TODO:
	  		//Add the new button check and create the right robot
	  		//Example: Add what is for Simple Robot
	  		if(btnTemp.getText().equals("Connect")){
	  			trStanbyQueueReading.cancel();
  				running = true;
  				btnTemp.setText("Disconnect");
	  			if(btnTemp == tglbtnConnectToPenguin){
	  				robot = new Penguin(queue,comm,dis,timer);	
	  			}
	  			else if(btnTemp == tglbtnConnectToModulus){
	  				robot = new Modulus(queue,comm,dis,timer);
	  			}
	  			else if(btnTemp == tglbtnConnectToSample){
	  				robot = new SampleRobot(queue,comm,dis,timer);
	  			}
	  			else{
	  				return;
	  			}
	  			robot.start();
	  		}
	  		else
	  		{
	  			trStanbyQueueReading = new Timer();
	  			trStanbyQueueReading.schedule(new StanbyQueueReading(), 0, 25);
	  			btnTemp.setText("Connect");
	  			robot.stopThread();
	  			running = false;
	  		}
	   	}
	}
	
	//joy stick page updates
	public void updateJoyStatus(Event ev){
		if(ev.getIndex()>=50 && ev.getIndex()<100){
			ev.setCommand(EventEnum.JOY_AXIS);
			ev.setIndex((short)(ev.getIndex()-50));
			updateAxisGUI(ev);
		}
		else if(ev.getIndex()>=100 && ev.getIndex()<200){
			ev.setCommand(EventEnum.JOY_BUTTON);
			ev.setIndex((short)(ev.getIndex()-100));
			updateButtonGUI(ev);
		}
		else if(ev.getIndex()>=200){
			ev.setCommand(EventEnum.JOY_HAT);
			ev.setIndex((short)(ev.getIndex()-200));
			updateHatGUI(ev);
		}
	}
	
	public void updateButtonGUI(Event ev){
		if(ev.getValue() == 0)
			btnBut[ev.getIndex()].setBackground(Color.BLUE);
		else
			btnBut[ev.getIndex()].setBackground(Color.RED);
	}
	
	public void updateAxisGUI(Event ev){
		switch(ev.getIndex()){
		case 0:
			sldX1.setValue(ev.getValue());
			//sldX1.setText(Integer.toString(ev.getValue()));
			break;
		case 1:
			sldY1.setValue(ev.getValue());
			//sldY1.setText(Integer.toString(ev.getValue()));
			break;
		case 2:
			sldX2.setValue(ev.getValue());
			//sldX2.setText(Integer.toString(ev.getValue()));
			break;
		case 3:
			sldY2.setValue(ev.getValue());
			//sldY2.setText(Integer.toString(ev.getValue()));
			break;
		}
	}
	
	public void updateHatGUI(Event ev){
		for(int i=0; i<9; i++){
			if(ev.getValue() ==i)
				btnD_Pad[i].setBackground(Color.RED);
			else
				btnD_Pad[i].setBackground(Color.BLUE);
		}
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmIllinoisTechRobotics = new JFrame();
		frmIllinoisTechRobotics.setResizable(false);
		frmIllinoisTechRobotics.setTitle("Illinois Tech Robotics Robot Interface");
		frmIllinoisTechRobotics.setIconImage(Toolkit.getDefaultToolkit().getImage(GUI.class.getResource("/resources/logo.png")));
		frmIllinoisTechRobotics.setBounds(100, 100, 600, 400);
		frmIllinoisTechRobotics.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmIllinoisTechRobotics.setFocusable(true);
		
		frmIllinoisTechRobotics.addWindowListener(new WindowAdapter() {
		    
			@Override
		    public void windowClosing(WindowEvent windowEvent) {
		    	if(serial.isOpen()){
    				serial.closeSerial();
    			}
		    	
		    	if(ethernet.isConnected() == true){
    				ethernet.stopThread();
    			}
		    	
		    	if(joy.getJoy() != null){
    				joy.stopThread();
    			}
		    	
		    	if(key.isListening()){
        			key.stop();
        		}
		    	
		    	trSerialCommChecker.cancel();
		        trStanbyQueueReading.cancel();
		    	
		        try{
		        	Thread.sleep(1000);
		        } catch(InterruptedException e) {	
		        }
		        
		    	System.exit(0);
		    }
		});
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);		
		frmIllinoisTechRobotics.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panSettings = new JPanel();
		tabbedPane.addTab("Settings", null, panSettings, null);
		
		JLabel lblTypeOffCommunication = new JLabel("Communication");
		
		rdbtnXbee = new JRadioButton("Xbee");
		rdbtnXbee.setSelected(true);
		
		rdbtnWifi = new JRadioButton("WiFi");
		
		ButtonGroup grpRadioButton = new ButtonGroup();
		grpRadioButton.add(rdbtnXbee);
		grpRadioButton.add(rdbtnWifi);
		
		JLabel lblSerialPort = new JLabel("Serial Port");
		
		comboBox_SerialPort = new JComboBox<String>();
		
		JLabel lblBaudRate = new JLabel("Baud Rate");
		
		comboBox_BaudRate = new JComboBox<Integer>();
		comboBox_BaudRate.addItem(new Integer(9600));
		comboBox_BaudRate.addItem(new Integer(19200));
		comboBox_BaudRate.addItem(new Integer(38400));
		comboBox_BaudRate.addItem(new Integer(57600));
		comboBox_BaudRate.addItem(new Integer(115200));
		comboBox_BaudRate.setSelectedIndex(3);
		
		JLabel lblIpAdress = new JLabel("IP Address");
		
		txtIPAddress = new JTextField();
		txtIPAddress.setText("192.168.20.92");
		txtIPAddress.setColumns(10);
		
		JLabel lblPortNumber = new JLabel("Port Number");
		
		txtPortNumber = new JTextField();
		txtPortNumber.setText("2000");
		txtPortNumber.setColumns(10);
		
		JLabel lblInputDevice = new JLabel("Input Device");
		
		chckbxJoystick = new JCheckBox("JoyStick");
		chckbxJoystick.setSelected(true);
		
		chckbxKeyboard = new JCheckBox("Keyboard");
		
		btnGeneralStatus = new JButton("");
		btnGeneralStatus.setBackground(Color.RED);
		btnGeneralStatus.setEnabled(false);
		
		JLabel lblListeningPort = new JLabel("Listening Port");
		
		txtListeningPort = new JTextField();
		txtListeningPort.setText("2001");
		txtListeningPort.setColumns(10);
		
		JLabel lblJoyStick = new JLabel("Joy Stick");
		
		comboBox_JoyStick = new JComboBox<String>();
		
		GroupLayout gl_panSettings = new GroupLayout(panSettings);
		gl_panSettings.setHorizontalGroup(
			gl_panSettings.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panSettings.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panSettings.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panSettings.createSequentialGroup()
							.addGroup(gl_panSettings.createParallelGroup(Alignment.LEADING)
								.addComponent(lblSerialPort)
								.addComponent(lblBaudRate))
							.addGap(28)
							.addGroup(gl_panSettings.createParallelGroup(Alignment.LEADING)
								.addComponent(comboBox_SerialPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(comboBox_BaudRate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_panSettings.createSequentialGroup()
							.addComponent(lblTypeOffCommunication)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(rdbtnXbee)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(rdbtnWifi)
							.addPreferredGap(ComponentPlacement.RELATED, 292, Short.MAX_VALUE)
							.addComponent(btnGeneralStatus, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panSettings.createSequentialGroup()
							.addComponent(lblInputDevice)
							.addGap(18)
							.addComponent(chckbxJoystick)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxKeyboard))
						.addGroup(gl_panSettings.createSequentialGroup()
							.addGroup(gl_panSettings.createParallelGroup(Alignment.LEADING)
								.addComponent(lblListeningPort, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblPortNumber)
								.addComponent(lblIpAdress))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panSettings.createParallelGroup(Alignment.LEADING)
								.addComponent(txtIPAddress, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
								.addComponent(txtPortNumber, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
								.addComponent(txtListeningPort, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_panSettings.createSequentialGroup()
							.addComponent(lblJoyStick, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(comboBox_JoyStick, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		gl_panSettings.setVerticalGroup(
			gl_panSettings.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panSettings.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panSettings.createParallelGroup(Alignment.LEADING)
						.addComponent(btnGeneralStatus, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panSettings.createSequentialGroup()
							.addGroup(gl_panSettings.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblTypeOffCommunication)
								.addComponent(rdbtnXbee)
								.addComponent(rdbtnWifi))
							.addGap(22)
							.addGroup(gl_panSettings.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblSerialPort)
								.addComponent(comboBox_SerialPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panSettings.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblBaudRate)
								.addComponent(comboBox_BaudRate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(33)
							.addGroup(gl_panSettings.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblIpAdress)
								.addComponent(txtIPAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(gl_panSettings.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblPortNumber)
								.addComponent(txtPortNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panSettings.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblListeningPort)
								.addComponent(txtListeningPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(19)
							.addGroup(gl_panSettings.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblInputDevice)
								.addComponent(chckbxJoystick)
								.addComponent(chckbxKeyboard))))
					.addGap(18)
					.addGroup(gl_panSettings.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblJoyStick)
						.addComponent(comboBox_JoyStick, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(58, Short.MAX_VALUE))
		);
		panSettings.setLayout(gl_panSettings);
		
		//#####################################
		//Penguin
		//#####################################
		JPanel panPenguin = new JPanel();
		tabbedPane.addTab("Penguin", null, panPenguin, null);
		
		btnPenguinConnected = new JButton("");
		btnPenguinConnected.setBounds(516, 14, 63, 23);
		btnPenguinConnected.setEnabled(false);
		btnPenguinConnected.setBackground(Color.RED);
		
		tglbtnConnectToPenguin = new JToggleButton("Connect");
		tglbtnConnectToPenguin.setBounds(374, 14, 132, 23);
		tglbtnConnectToPenguin.addActionListener(new btnStartListener());
		
		sldRoll = new JSlider();
		sldRoll.setBounds(10, 14, 200, 45);
		sldRoll.setEnabled(false);
		sldRoll.setPaintLabels(true);
		sldRoll.setPaintTicks(true);
		sldRoll.setMajorTickSpacing(90);
		sldRoll.setMaximum(90);
		sldRoll.setMinimum(-90);
		sldRoll.setValue(0);
		
		sldPitch = new JSlider();
		sldPitch.setBounds(20, 65, 47, 200);
		sldPitch.setEnabled(false);
		sldPitch.setPaintLabels(true);
		sldPitch.setPaintTicks(true);
		sldPitch.setMajorTickSpacing(90);
		sldPitch.setMaximum(90);
		sldPitch.setMinimum(-90);
		sldPitch.setValue(0);
		sldPitch.setOrientation(SwingConstants.VERTICAL);
		
		JLabel lblP = new JLabel("P");
		lblP.setBounds(432, 212, 45, 14);
		lblP.setHorizontalAlignment(SwingConstants.CENTER);
		
		txtPD = new JTextField();
		txtPD.setBounds(534, 232, 45, 20);
		txtPD.setColumns(10);
		
		txtPI = new JTextField();
		txtPI.setBounds(483, 232, 45, 20);
		txtPI.setColumns(10);
		
		txtPP = new JTextField();
		txtPP.setBounds(432, 232, 45, 20);
		txtPP.setColumns(10);
		
		txtRP = new JTextField();
		txtRP.setBounds(432, 258, 45, 20);
		txtRP.setColumns(10);
		
		txtYP = new JTextField();
		txtYP.setBounds(432, 284, 45, 20);
		txtYP.setColumns(10);
		
		txtRI = new JTextField();
		txtRI.setBounds(483, 258, 45, 20);
		txtRI.setColumns(10);
		
		txtYI = new JTextField();
		txtYI.setBounds(483, 284, 45, 20);
		txtYI.setColumns(10);
		
		txtRD = new JTextField();
		txtRD.setBounds(534, 258, 45, 20);
		txtRD.setColumns(10);
		
		txtYD = new JTextField();
		txtYD.setBounds(534, 284, 45, 20);
		txtYD.setColumns(10);
		panPenguin.setLayout(null);
		panPenguin.add(sldRoll);
		panPenguin.add(tglbtnConnectToPenguin);
		panPenguin.add(btnPenguinConnected);
		panPenguin.add(sldPitch);
		panPenguin.add(txtRP);
		panPenguin.add(txtRI);
		panPenguin.add(txtRD);
		panPenguin.add(txtYP);
		panPenguin.add(txtYI);
		panPenguin.add(txtYD);
		panPenguin.add(lblP);
		panPenguin.add(txtPP);
		panPenguin.add(txtPI);
		panPenguin.add(txtPD);
		
		JLabel lblI = new JLabel("I");
		lblI.setHorizontalAlignment(SwingConstants.CENTER);
		lblI.setBounds(483, 212, 45, 14);
		panPenguin.add(lblI);
		
		JLabel lblD = new JLabel("D");
		lblD.setHorizontalAlignment(SwingConstants.CENTER);
		lblD.setBounds(534, 212, 45, 14);
		panPenguin.add(lblD);
		
		JLabel lblPitchPID = new JLabel("Pitch");
		lblPitchPID.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPitchPID.setBounds(376, 235, 46, 14);
		panPenguin.add(lblPitchPID);
		
		JLabel lblRollPID = new JLabel("Roll");
		lblRollPID.setHorizontalAlignment(SwingConstants.TRAILING);
		lblRollPID.setBounds(376, 261, 46, 14);
		panPenguin.add(lblRollPID);
		
		JLabel lblYawPID = new JLabel("Yaw");
		lblYawPID.setHorizontalAlignment(SwingConstants.TRAILING);
		lblYawPID.setBounds(376, 287, 46, 14);
		panPenguin.add(lblYawPID);
		
		btnUpdatePid = new JButton("Update PID");
		btnUpdatePid.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				queue.add(new Event(EventEnum.GUI,GUIEnum.PENGUIN_UPDATE_PID.value,0));
			}
		});
		btnUpdatePid.setBounds(432, 309, 147, 23);
		panPenguin.add(btnUpdatePid);
		
		sldMot4 = new JSlider();
		sldMot4.setMaximum(2000);
		sldMot4.setMinimum(1000);
		sldMot4.setValue(1000);
		sldMot4.setOrientation(SwingConstants.VERTICAL);
		sldMot4.setMajorTickSpacing(500);
		sldMot4.setPaintLabels(true);
		sldMot4.setPaintTicks(true);
		sldMot4.setEnabled(false);
		sldMot4.setBounds(530, 87, 47, 100);
		panPenguin.add(sldMot4);
		
		sldMot3 = new JSlider();
		sldMot3.setMaximum(2000);
		sldMot3.setMinimum(1000);
		sldMot3.setValue(1000);
		sldMot3.setPaintTicks(true);
		sldMot3.setPaintLabels(true);
		sldMot3.setOrientation(SwingConstants.VERTICAL);
		sldMot3.setMajorTickSpacing(500);
		sldMot3.setEnabled(false);
		sldMot3.setBounds(475, 87, 47, 100);
		panPenguin.add(sldMot3);
		
		sldMot2 = new JSlider();
		sldMot2.setMaximum(2000);
		sldMot2.setMinimum(1000);
		sldMot2.setValue(1000);
		sldMot2.setPaintTicks(true);
		sldMot2.setPaintLabels(true);
		sldMot2.setOrientation(SwingConstants.VERTICAL);
		sldMot2.setMajorTickSpacing(500);
		sldMot2.setEnabled(false);
		sldMot2.setBounds(420, 87, 47, 100);
		panPenguin.add(sldMot2);
		
		sldMot1 = new JSlider();
		sldMot1.setMaximum(2000);
		sldMot1.setMinimum(1000);
		sldMot1.setValue(1000);
		sldMot1.setPaintTicks(true);
		sldMot1.setPaintLabels(true);
		sldMot1.setOrientation(SwingConstants.VERTICAL);
		sldMot1.setMajorTickSpacing(500);
		sldMot1.setEnabled(false);
		sldMot1.setBounds(365, 87, 47, 100);
		panPenguin.add(sldMot1);
		
		JLabel lblMot1_txt = new JLabel("Mot 1");
		lblMot1_txt.setBounds(374, 63, 46, 14);
		panPenguin.add(lblMot1_txt);
		
		JLabel lblMot2_txt = new JLabel("Mot 2");
		lblMot2_txt.setBounds(431, 63, 46, 14);
		panPenguin.add(lblMot2_txt);
		
		JLabel lblMot3_txt = new JLabel("Mot 3");
		lblMot3_txt.setBounds(482, 62, 46, 14);
		panPenguin.add(lblMot3_txt);
		
		JLabel lblMot4_txt = new JLabel("Mot 4");
		lblMot4_txt.setBounds(533, 62, 46, 14);
		panPenguin.add(lblMot4_txt);
		
		lblMot1 = new JLabel("0");
		lblMot1.setBounds(374, 187, 36, 14);
		panPenguin.add(lblMot1);
		
		lblMot2 = new JLabel("0");
		lblMot2.setBounds(431, 187, 36, 14);
		panPenguin.add(lblMot2);
		
		lblMot3 = new JLabel("0");
		lblMot3.setBounds(482, 187, 36, 14);
		panPenguin.add(lblMot3);
		
		lblMot4 = new JLabel("0");
		lblMot4.setBounds(533, 187, 36, 14);
		panPenguin.add(lblMot4);
		
		JLabel lblPitch_txt = new JLabel("Pitch:");
		lblPitch_txt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPitch_txt.setBounds(92, 70, 32, 14);
		panPenguin.add(lblPitch_txt);
		
		JLabel lblRoll_txt = new JLabel("Roll:");
		lblRoll_txt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRoll_txt.setBounds(92, 89, 32, 14);
		panPenguin.add(lblRoll_txt);
		
		JLabel lblYaw_txt = new JLabel("Yaw:");
		lblYaw_txt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblYaw_txt.setBounds(92, 107, 32, 14);
		panPenguin.add(lblYaw_txt);
		
		lblPitch = new JLabel("0");
		lblPitch.setBounds(134, 70, 120, 14);
		panPenguin.add(lblPitch);
		
		lblRoll = new JLabel("0");
		lblRoll.setBounds(134, 89, 120, 14);
		panPenguin.add(lblRoll);
		
		lblYaw = new JLabel("0");
		lblYaw.setBounds(134, 107, 120, 14);
		panPenguin.add(lblYaw);
		
		
		//#####################################
		//Modulus
		//#####################################
		JPanel panModulus = new JPanel();
		tabbedPane.addTab("Modulus", null, panModulus, null);
		panModulus.setLayout(null);
		
		btnModulusConnected = new JButton("");
		btnModulusConnected.setBounds(516, 14, 63, 23);
		btnModulusConnected.setEnabled(false);
		btnModulusConnected.setBackground(Color.RED);
		panModulus.add(btnModulusConnected);
		
		tglbtnConnectToModulus = new JToggleButton("Connect");
		tglbtnConnectToModulus.setBounds(374, 14, 132, 23);
		tglbtnConnectToModulus.addActionListener(new btnStartListener());
		panModulus.add(tglbtnConnectToModulus);
		
		//TODO:
		//Add GUI stuff for robots
		//Example: Minimum for a robots
		//Can add manually or use GUI tools
		//If using GUI tools make sure to make JButton and JToggleButton public
		//Also make sure to add btnStartListener to JToggleButton if using GUI tools
		JPanel panSample = new JPanel();
		tabbedPane.addTab("Sample", null, panSample, null);
		panSample.setLayout(null);
		
		btnSampleConnected = new JButton("");
		btnSampleConnected.setEnabled(false);
		btnSampleConnected.setBackground(Color.RED);
		btnSampleConnected.setBounds(516, 14, 63, 23);
		panSample.add(btnSampleConnected);
		
		tglbtnConnectToSample = new JToggleButton("Connect");
		tglbtnConnectToSample.setBounds(374, 14, 132, 23);
		tglbtnConnectToSample.addActionListener(new btnStartListener());
		panSample.add(tglbtnConnectToSample);
		
		//#####################################
		//Joy Stick
		//#####################################
		JPanel panJoystick = new JPanel();
		tabbedPane.addTab("Joystick", null, panJoystick, null);
		panJoystick.setLayout(null);
		
		sldX1 = new JSlider();
		sldX1.setPaintTicks(true);
		sldX1.setMajorTickSpacing(127);
		sldX1.setValue(127);
		sldX1.setMaximum(255);
		sldX1.setBounds(195, 170, 100, 30);
		panJoystick.add(sldX1);
		
		sldX2 = new JSlider();
		sldX2.setPaintTicks(true);
		sldX2.setMajorTickSpacing(127);
		sldX2.setValue(127);
		sldX2.setMaximum(255);
		sldX2.setBounds(295, 170, 100, 30);
		panJoystick.add(sldX2);
		
		sldY1 = new JSlider();
		sldY1.setPaintTicks(true);
		sldY1.setMajorTickSpacing(127);
		sldY1.setValue(127);
		sldY1.setMaximum(255);
		sldY1.setOrientation(SwingConstants.VERTICAL);
		sldY1.setBounds(260, 200, 30, 100);
		panJoystick.add(sldY1);
		
		sldY2 = new JSlider();
		sldY2.setPaintTicks(true);
		sldY2.setMajorTickSpacing(127);
		sldY2.setValue(127);
		sldY2.setMaximum(255);
		sldY2.setOrientation(SwingConstants.VERTICAL);
		sldY2.setBounds(305, 200, 30, 100);
		panJoystick.add(sldY2);
		
		JButton btn2 = new JButton("2");
		btn2.setForeground(Color.GRAY);
		btn2.setBackground(Color.BLUE);
		btn2.setBounds(466, 180, 50, 26);
		panJoystick.add(btn2);
		btnBut[1] = btn2;
		
		JButton btn1 = new JButton("1");
		btn1.setForeground(Color.GRAY);
		btn1.setBackground(Color.BLUE);
		btn1.setBounds(417, 140, 50, 26);
		panJoystick.add(btn1);
		btnBut[0] = btn1;
		
		JButton btn3 = new JButton("3");
		btn3.setForeground(Color.GRAY);
		btn3.setBackground(Color.BLUE);
		btn3.setBounds(516, 140, 50, 26);
		panJoystick.add(btn3);
		btnBut[2] = btn3;
		
		JButton btn4 = new JButton("4");
		btn4.setForeground(Color.GRAY);
		btn4.setBackground(Color.BLUE);
		btn4.setBounds(466, 100, 50, 26);
		panJoystick.add(btn4);
		btnBut[3] = btn4;
		
		JButton btn8 = new JButton("8");
		btn8.setForeground(Color.GRAY);
		btn8.setBackground(Color.BLUE);
		btn8.setBounds(464, 12, 50, 26);
		panJoystick.add(btn8);
		btnBut[7] = btn8;
		
		JButton btn6 = new JButton("6");
		btn6.setForeground(Color.GRAY);
		btn6.setBackground(Color.BLUE);
		btn6.setBounds(464, 36, 50, 26);
		panJoystick.add(btn6);
		btnBut[5] = btn6;
		
		JButton btn7 = new JButton("7");
		btn7.setForeground(Color.GRAY);
		btn7.setBackground(Color.BLUE);
		btn7.setBounds(75, 12, 50, 26);
		panJoystick.add(btn7);
		btnBut[6] = btn7;
		
		JButton btn5 = new JButton("5");
		btn5.setForeground(Color.GRAY);
		btn5.setBackground(Color.BLUE);
		btn5.setBounds(75, 36, 50, 26);
		panJoystick.add(btn5);
		btnBut[4] = btn5;
		
		JButton btn9 = new JButton("9");
		btn9.setForeground(Color.GRAY);
		btn9.setBackground(Color.BLUE);
		btn9.setBounds(224, 100, 50, 26);
		panJoystick.add(btn9);
		btnBut[8] = btn9;
		
		JButton btn10 = new JButton("10");
		btn10.setForeground(Color.GRAY);
		btn10.setBackground(Color.BLUE);
		btn10.setBounds(326, 100, 50, 26);
		panJoystick.add(btn10);
		btnBut[9] = btn10;
		
		JButton hat3 = new JButton("3");
		hat3.setForeground(Color.GRAY);
		hat3.setBackground(Color.BLUE);
		hat3.setBounds(75, 100, 50, 26);
		panJoystick.add(hat3);
		btnD_Pad[3] = hat3;
		
		JButton hat4 = new JButton("4");
		hat4.setForeground(Color.GRAY);
		hat4.setBackground(Color.BLUE);
		hat4.setBounds(110, 126, 50, 26);
		panJoystick.add(hat4);
		btnD_Pad[4] = hat4;
		
		JButton hat5 = new JButton("5");
		hat5.setForeground(Color.GRAY);
		hat5.setBackground(Color.BLUE);
		hat5.setBounds(135, 152, 50, 26);
		panJoystick.add(hat5);
		btnD_Pad[5] = hat5;
		
		JButton hat2 = new JButton("2");
		hat2.setForeground(Color.GRAY);
		hat2.setBackground(Color.BLUE);
		hat2.setBounds(40, 126, 50, 26);
		panJoystick.add(hat2);
		btnD_Pad[2] = hat2;
		
		JButton hat1 = new JButton("1");
		hat1.setForeground(Color.GRAY);
		hat1.setBackground(Color.BLUE);
		hat1.setBounds(15, 152, 50, 26);
		panJoystick.add(hat1);
		btnD_Pad[1] = hat1;
		
		JButton hat8 = new JButton("8");
		hat8.setForeground(Color.GRAY);
		hat8.setBackground(Color.BLUE);
		hat8.setBounds(40, 178, 50, 26);
		panJoystick.add(hat8);
		btnD_Pad[8] = hat8;
		
		JButton hat7 = new JButton("7");
		hat7.setForeground(Color.GRAY);
		hat7.setBackground(Color.BLUE);
		hat7.setBounds(75, 204, 50, 26);
		panJoystick.add(hat7);
		btnD_Pad[7] = hat7;
		
		JButton hat6 = new JButton("6");
		hat6.setForeground(Color.GRAY);
		hat6.setBackground(Color.BLUE);
		hat6.setBounds(110, 178, 50, 26);
		panJoystick.add(hat6);
		btnD_Pad[6] = hat6;
		
		JButton btn11 = new JButton("11");
		btn11.setForeground(Color.GRAY);
		btn11.setBackground(Color.BLUE);
		btn11.setBounds(224, 306, 50, 26);
		panJoystick.add(btn11);
		btnBut[10] = btn11;
		
		JButton btn12 = new JButton("12");
		btn12.setForeground(Color.GRAY);
		btn12.setBackground(Color.BLUE);
		btn12.setBounds(326, 306, 50, 26);
		panJoystick.add(btn12);
		btnBut[11] = btn12;
		
		JButton hat0 = new JButton("0");
		hat0.setForeground(Color.GRAY);
		hat0.setBackground(Color.BLUE);
		hat0.setBounds(75, 152, 50, 26);
		panJoystick.add(hat0);
		btnD_Pad[0] = hat0;
		
		JLabel lblJoystickConnected = new JLabel("Joystick Connected:\r\n");
		lblJoystickConnected.setVerticalAlignment(SwingConstants.TOP);
		lblJoystickConnected.setBounds(157, 12, 289, 16);
		panJoystick.add(lblJoystickConnected);
		
		JLabel lblJoystickName = new JLabel("");
		lblJoystickName.setVerticalAlignment(SwingConstants.TOP);
		lblJoystickName.setBounds(157, 32, 289, 16);
		panJoystick.add(lblJoystickName);
		
		JLabel lblSt = new JLabel("Axis 1");
		lblSt.setBounds(195, 244, 55, 16);
		panJoystick.add(lblSt);
		
		JLabel lblStick = new JLabel("Axis 2");
		lblStick.setBounds(353, 244, 55, 16);
		panJoystick.add(lblStick);
		
		JPanel panTerminal = new JPanel();
		tabbedPane.addTab("Terminal", null, panTerminal, null);
		
		txtMessage = new JTextField();
		txtMessage.setColumns(10);
		
		JButton btnSend = new JButton("Send");
		
		JScrollPane scrollPane = new JScrollPane();
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		
		scrollPane.setViewportView(textArea);
		GroupLayout gl_panTerminal = new GroupLayout(panTerminal);
		gl_panTerminal.setHorizontalGroup(
			gl_panTerminal.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panTerminal.createSequentialGroup()
					.addGap(10)
					.addGroup(gl_panTerminal.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 567, Short.MAX_VALUE)
						.addGroup(Alignment.TRAILING, gl_panTerminal.createSequentialGroup()
							.addComponent(txtMessage, GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		gl_panTerminal.setVerticalGroup(
			gl_panTerminal.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panTerminal.createSequentialGroup()
					.addGap(11)
					.addGroup(gl_panTerminal.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtMessage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnSend))
					.addGap(14)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
					.addContainerGap())
		);
		panTerminal.setLayout(gl_panTerminal);
	}
	
	/**
	 * changes the System.out and System.err to print to the textbox area 
	 */
	private void redirectSystemStreams() {  
			
		OutputStream out = new OutputStream() {  
			
			@Override  
			public void write(int b) throws IOException {  
				textArea.append(String.valueOf((char) b)); 
		    	textArea.setCaretPosition(textArea.getDocument().getLength());
		    	textArea.repaint();
			}  
		  
		    @Override  
		    public void write(byte[] b, int off, int len) throws IOException {  
		    	textArea.append(new String(b, off, len));
		    	textArea.setCaretPosition(textArea.getDocument().getLength());
		    	textArea.repaint();
		    }  
		  
		    @Override  
		    public void write(byte[] b) throws IOException {  
		    	write(b, 0, b.length); 
		    	textArea.setCaretPosition(textArea.getDocument().getLength());
		    	textArea.repaint();
		    }  
		}; 
		System.setOut(new PrintStream(out, true));  
		System.setErr(new PrintStream(out, true)); 
	}
}
