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
package common;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.EventQueue;
import java.awt.event.*;

import javax.swing.JFrame;

import java.awt.Toolkit;

import javax.swing.JTabbedPane;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;

import java.awt.Color;

import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.*;

import common.Robot.RobotEnum;
import common.Timer.TimerEnum;
import gnu.io.*;
import net.java.games.input.Controller;
import robots.*;


public class GUI extends Thread{

	private JFrame frmIllinoisTechRobotics;
	private JTextField txtMessage;
	private JButton btnGeneralStatus;
	private JRadioButton rdbtnXbee;
	private JRadioButton rdbtnWifi;
	private JTextField txtIPAddress;
	private JTextField txtPortNumber;
	private JComboBox comboBox_SerialPort;
	private JComboBox comboBox_BaudRate;
	private JTabbedPane tabbedPane;
	private JButton[] btnBut = new JButton[12];
	private JButton[] btnD_Pad = new JButton[9];
	private JSlider sldX1;
	private JSlider sldX2;
	private JSlider sldY1;
	private JSlider sldY2;
	
	private Timer trSerialCommChecker;
	private Timer trStanbyQueueReading;
	private Queue queue = new Queue(1000);
	private common.Timer timer = new common.Timer(queue);
	private Serial serial = new Serial(queue, this);
	private Ethernet ethernet = new Ethernet(queue, this);
	private Joystick joy;
	private GUI dis = this;
	
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
	}
	
	public class deviceChecker extends TimerTask{
	       
        public void run(){
        	try{
        		if(rdbtnXbee.isSelected()){
        			if(ethernet.isConnected() == true){
        				ethernet.stopThread();
        			}
        			
        			if(serial.isOpen() && !serial.getName().equals(comboBox_SerialPort.getSelectedIndex())){
        				serial.closeSerial();
        			}
        			
        			List<CommPortIdentifier> com = Serial.getSerialPorts();
        			//only change the layout if the number of ports changed and setting tab is in view
        			if( com.size() != comboBox_SerialPort.getItemCount()){
        				comboBox_SerialPort.removeAllItems();
        				for(int i=com.size()-1; i>=0; i--){ //put them on in reverse order since high comm port is the more likely to be chosen
        					comboBox_SerialPort.addItem(com.get(i).getName());
        					if(serial.isOpen() && serial.getName().equals(com.get(i).getName())){
        						comboBox_SerialPort.setSelectedIndex(i);
        					}
        				}
        				
        			}
        			
        			if(serial.isOpen() == false)
        			{
        				serial.OpenSerial(Integer.parseInt(comboBox_BaudRate.getSelectedItem().toString()),comboBox_SerialPort.getSelectedItem().toString());	
        			}
        		}
        		else if(rdbtnWifi.isSelected()){
        			if(serial.isOpen()){
        				serial.closeSerial();
        			}
        			if(ethernet.isConnected()==false){
        				if(ethernet.connect(txtIPAddress.getText(), Integer.parseInt(txtPortNumber.getText()))){
        					ethernet.start();
        				}
        				else{
        					ethernet.stopThread();
        				}
        			}
        			else if(ethernet.getIPAddress().equals(txtIPAddress.getText()) == false){
        				ethernet.stopThread();
        				if(ethernet.connect(txtIPAddress.getText(), Integer.parseInt(txtPortNumber.getText()))){
        					ethernet.start();
        				}
        				else{
        					ethernet.stopThread();
        				}
        			}
        			else if(ethernet.getPortNumber() != Integer.parseInt(txtPortNumber.getText())){
        				ethernet.stopThread();
        				if(ethernet.connect(txtIPAddress.getText(), Integer.parseInt(txtPortNumber.getText()))){
        					ethernet.start();
        				}
        				else{
        					ethernet.stopThread();
        				}
        			}
        		}
		
        		//if joy null check for joystick
        		if(joy == null)
        		{
        			Controller con = Joystick.getJoystick();
        			//if joystick found use it
        			if( con != null )
        			{
        				joy = new Joystick(queue, con, dis);	
        				joy.start();
        				
        			}
        		}
        		else //joystick exits check to see if connected
        		{
        			if( joy.checkJoystick() == false )
        			{
        				//joystick disconnected stop joy and start searching again 
        				joy.stopThread();
        				joy = null;
        			}
        		}          			
        	}
        	catch(Exception e){
       		}
        }
    }

	public class StanbyQueueReading extends TimerTask{
	    
		int heartbeatcount = 0;
        public void run(){
    		while(queue.getSize() > 0)
    		{
    			Event ev = queue.take();
    			switch(ev.getCommand()){
    			case ROBOT_EVENT_CMD_HEARTBEAT:
    				//check the heartbeat and update connection status
    				heartbeatcount = 0;
    				btnGeneralStatus.setBackground(Color.GREEN);
    				btnGeneralStatus.setText(Robot.RobotEnum.getRobot(ev.getIndex()).toString());
    				break;
    			case ROBOT_EVENT_TIMER:
    				serial.sendEvent(new Event(EventEnum.ROBOT_EVENT_CMD_HEARTBEAT,(short)RobotEnum.COMPUTER.getValue(),(short)0));
    				if (ev.getIndex() == TimerEnum.TIMER_HEARTBEAT.value){
    					Date d = new Date();
    					//System.out.println(d.getTime());
    					heartbeatcount++;
    					if(heartbeatcount > 4){
    						btnGeneralStatus.setBackground(Color.RED);
    	    				btnGeneralStatus.setText("");
    					}
    				}
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
        trSerialCommChecker.schedule(new deviceChecker(), 0, 5000);
        trStanbyQueueReading = new Timer();
        trStanbyQueueReading.schedule(new StanbyQueueReading(), 0, 25);
	}
	
	private boolean running = false;
	
	private Ghost ghost;
	
	public void changeRobotStatus(int stat){
		if(stat == 0){
			//btnStatus.setBackground(Color.GREEN);
		}
		if(stat == 1){
			//btnStatus.setBackground(Color.RED);
		}
	}
	
	//joystick page updates
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
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmIllinoisTechRobotics.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panSettings = new JPanel();
		tabbedPane.addTab("Settings", null, panSettings, null);
		
		JLabel lblTypeOffCommunication = new JLabel("Communication");
		
		rdbtnXbee = new JRadioButton("Xbee");
		rdbtnXbee.setSelected(true);
		rdbtnXbee.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(rdbtnXbee.isSelected()){
					if(serial.isOpen() == false){
						serial.OpenSerial(Integer.parseInt(comboBox_BaudRate.getSelectedItem().toString()),comboBox_SerialPort.getSelectedItem().toString());
					}
				}
				else{
					if(serial.isOpen()){
						serial.closeSerial();
					}
				}
			}
		});
		
		rdbtnWifi = new JRadioButton("WiFi");
		
		ButtonGroup grpRadioButton = new ButtonGroup();
		grpRadioButton.add(rdbtnXbee);
		grpRadioButton.add(rdbtnWifi);
		
		JLabel lblSerialPort = new JLabel("Serial Port");
		
		comboBox_SerialPort = new JComboBox();
		comboBox_SerialPort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(comboBox_SerialPort.getSelectedItem() != null){
					if(serial.isOpen() == false){
						serial.OpenSerial(Integer.parseInt(comboBox_BaudRate.getSelectedItem().toString()),comboBox_SerialPort.getSelectedItem().toString());
					}
					else if(serial.getPortName().equals(((JComboBox)e.getSource()).getSelectedItem().toString()) == false){
						serial.closeSerial();
						serial.OpenSerial(Integer.parseInt(comboBox_BaudRate.getSelectedItem().toString()),comboBox_SerialPort.getSelectedItem().toString());
					}
				}
			}
		});
		
		JLabel lblBaudRate = new JLabel("Baud Rate");
		
		comboBox_BaudRate = new JComboBox();
		comboBox_BaudRate.addItem(new Integer(9600));
		comboBox_BaudRate.addItem(new Integer(19200));
		comboBox_BaudRate.addItem(new Integer(38400));
		comboBox_BaudRate.addItem(new Integer(57600));
		comboBox_BaudRate.addItem(new Integer(115200));
		comboBox_BaudRate.setSelectedIndex(3);
		comboBox_BaudRate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(comboBox_SerialPort.getSelectedItem() != null){
					if(serial.isOpen() == false){
						serial.OpenSerial(Integer.parseInt(comboBox_BaudRate.getSelectedItem().toString()),comboBox_SerialPort.getSelectedItem().toString());
					}
					else if(serial.getBaudRate() != Integer.parseInt(((JComboBox)e.getSource()).getSelectedItem().toString())){
						serial.closeSerial();
						serial.OpenSerial(Integer.parseInt(comboBox_BaudRate.getSelectedItem().toString()),comboBox_SerialPort.getSelectedItem().toString());
					}
				}
			}
		});
		
		JLabel lblIpAdress = new JLabel("IP Address");
		
		txtIPAddress = new JTextField();
		txtIPAddress.setText("192.168.1.1");
		txtIPAddress.setColumns(10);
		
		JLabel lblPortNumber = new JLabel("Port Number");
		
		txtPortNumber = new JTextField();
		txtPortNumber.setText("31337");
		txtPortNumber.setColumns(10);
		
		JLabel lblInputDevice = new JLabel("Input Device");
		
		JCheckBox chckbxJoystick = new JCheckBox("JoyStick");
		chckbxJoystick.setSelected(true);
		
		JCheckBox chckbxKeyboard = new JCheckBox("Keyboard");
		
		btnGeneralStatus = new JButton("");
		btnGeneralStatus.setBackground(Color.RED);
		btnGeneralStatus.setEnabled(false);
		
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
						.addGroup(gl_panSettings.createParallelGroup(Alignment.LEADING)
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
									.addComponent(lblPortNumber)
									.addComponent(lblIpAdress))
								.addGap(18)
								.addGroup(gl_panSettings.createParallelGroup(Alignment.LEADING)
									.addComponent(txtIPAddress, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
									.addComponent(txtPortNumber, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)))))
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
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panSettings.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblPortNumber)
								.addComponent(txtPortNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addGroup(gl_panSettings.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblInputDevice)
								.addComponent(chckbxJoystick)
								.addComponent(chckbxKeyboard))))
					.addContainerGap(121, Short.MAX_VALUE))
		);
		panSettings.setLayout(gl_panSettings);
		
		JPanel panFenrir = new JPanel();
		tabbedPane.addTab("Fenrir", null, panFenrir, null);
		
		JPanel panGoliath = new JPanel();
		tabbedPane.addTab("Goliath", null, panGoliath, null);
		
		JPanel panPenguin = new JPanel();
		tabbedPane.addTab("Penguin", null, panPenguin, null);
		
		JPanel panRoslund = new JPanel();
		tabbedPane.addTab("Roslund", null, panRoslund, null);
		
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
		
		JTextArea textArea = new JTextArea();
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
}
