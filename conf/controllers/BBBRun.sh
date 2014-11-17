#!/bin/bash

#Need to list the ACM* type Serial Ports since RXTX doesn't auto detect them
java -Dgnu.io.rxtx.SerialPorts=/dev/ttyS0:/dev/ttyS1:/dev/ttyS2:/dev/ttyS3 -Djava.ext.dirs=lib/JInput/x86:lib/RXTX/x86 -jar "controllers.jar"
