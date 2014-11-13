package org.illinoistechrobotics.test;

import gnu.io.CommPortIdentifier;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.illinoistechrobotics.common.Event;
import org.illinoistechrobotics.common.Queue;
import org.illinoistechrobotics.common.Serial;


public class SerialTest {

	private JFrame frame;
	JComboBox<String> comboBox_SerialPort;
	private Queue queue = new Queue(1000);
	private Serial serial = new Serial(queue);
	private Timer trSerialCommChecker;
	private Timer trStanbyQueueReading;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SerialTest window = new SerialTest();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SerialTest() {
		initialize();
	}

	
	public class deviceChecker extends TimerTask{
	       
        public void run(){
        	try{
        			/*
        			comboBox_SerialPort.removeAllItems();
        			if(serial.isOpen()){
        				comboBox_SerialPort.addItem(serial.getPortName());
        			}
        			if(!serial.isOpen() && com[0]!=null){
        				serial.OpenSerial(9600, com[0].getName());
        			}
        			for(int i=0; i<com.length; i++){
        				comboBox_SerialPort.addItem(com[i].getName());
        			}
        			*/
        			List<CommPortIdentifier> com = Serial.getSerialPorts();
        			if(serial.isOpen() && System.getProperty("os.name").contains("nux")){
        				com.add(serial.getCommPortIdentifier());
        				Collections.sort(com, new Comparator<CommPortIdentifier>() {
        				    public int compare(CommPortIdentifier a, CommPortIdentifier b) {
        				        return a.getName().compareTo(b.getName());
        				    }
        				});
        			}
        			//only change the layout if the number of ports changed and setting tab is in view
        			if( com.size() != comboBox_SerialPort.getItemCount()){
        				String stemp = (String)comboBox_SerialPort.getSelectedItem();
        				comboBox_SerialPort.removeAllItems();
        				for(int i=com.size()-1; i>=0; i--){ //put them on in reverse order since high comm port is the more likely to be chosen
        					comboBox_SerialPort.addItem(com.get(i).getName());
        					if(stemp != null && stemp.equals(com.get(i).getName())){
        					}
        				}
        				//comboBox_SerialPort.setSelectedIndex(ntemp); //select the previous selected comm port if exists
        				if(serial.isOpen()){
        					if(serial.getPortName().equals(comboBox_SerialPort.getSelectedItem().toString()) == false){
        						serial.closeSerial();
        					}
        				}
        			}	
        				
        	}
        	catch(Exception e){
        		System.out.println(e.toString());
       		}
        }
    }
	
	public class StanbyQueueReading extends TimerTask{
	       
        public void run(){
        	//remove queue items and dispose of them needed to display joystick values
    		while(queue.getSize() > 0)
    		{
    			Event ev = queue.poll();
    			switch(ev.getCommand()){
    			case CMD_HEARTBEAT:
    				//check the heartbeat and update connection status
    				break;
				default:
					break;
    			}
    		}
        }
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		comboBox_SerialPort = new JComboBox<String>();
		comboBox_SerialPort.setBounds(107, 31, 172, 20);
		comboBox_SerialPort.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				if(comboBox_SerialPort.getSelectedItem() != null){
					if(serial.isOpen() == false){
						serial.openSerial(9600,comboBox_SerialPort.getSelectedItem().toString());
					}
					else if(serial.getPortName().equals(((JComboBox<String>)e.getSource()).getSelectedItem().toString()) == false){
						serial.closeSerial();
						serial.openSerial(9600,comboBox_SerialPort.getSelectedItem().toString());
					}
				}
			}
		});
		frame.getContentPane().add(comboBox_SerialPort);
		
		JLabel lblSerialPort = new JLabel("Serial Port");
		lblSerialPort.setBounds(21, 34, 76, 14);
		frame.getContentPane().add(lblSerialPort);
		
		trSerialCommChecker = new Timer();
        trSerialCommChecker.schedule(new deviceChecker(), 0, 1000);
        trStanbyQueueReading = new Timer();
        trStanbyQueueReading.schedule(new StanbyQueueReading(), 0, 25);
	}
}
