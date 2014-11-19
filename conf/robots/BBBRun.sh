#!/bin/bash

#Need to list the ACM* type Serial Ports since RXTX doesn't auto detect them
java -Dgnu.io.rxtx.SerialPorts=/dev/ttyO0:/dev/ttyO1:/dev/ttyO2:/dev/ttyO3:/dev/ttyO5 -Djava.ext.dirs=lib:lib/JInput/BBB:lib/RXTX/BBB:/lib/Bulldog/Debian_Ubuntu -jar "robots.jar" -r Modulus -sp /dev/ttyO2 -sb 9600 -j "0 Logitech Logitech Dual Action"

