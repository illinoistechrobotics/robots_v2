#!/bin/bash

#Need to list the ACM* type Serial Ports since RXTX doesn't auto detect them
java -Dgnu.io.rxtx.SerialPorts=/dev/ttyACM0:/dev/ttyACM1:/dev/ttyACM2:/dev/ttyACM3 -Djava.ext.dirs=lib/JInput/x86:lib/RXTX/x86 -jar "controllers.jar"
