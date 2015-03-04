#!/bin/bash

#Need to list the ACM* type Serial Ports since RXTX doesn't auto detect them
/home/robot/jdk1.7.0_60/bin/java -Dgnu.io.rxtx.SerialPorts=/dev/ttyO0:/dev/ttyO1:/dev/ttyO2:/dev/ttyO3:/dev/ttyO5 -Djava.ext.dirs=lib:lib/JInput/BBB:lib/RXTX/BBB:lib/BullDog/Debian_Ubuntu -jar "robots.jar" -r Modulus -ip 192.168.1.105 -esp 5500 -ecp 5600 -j "0 Logitech Logitech Dual Action"