#!/bin/bash

java -Dgnu.io.rxtx.SerialPorts=/dev/ttyACM0:/dev/ttyACM1:/dev/ttyACM2:/dev/ttyACM3 -Djava.ext.dirs=lib/JInput:lib/RXTX -jar "controllers.jar"