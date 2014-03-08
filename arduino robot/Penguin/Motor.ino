/*
Copyright 2014 (c) Illinois Tech Robotics <robotics.iit@gmail.com>

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

#include <Servo.h>

#define MOTOR0 23
#define MOTOR1 22
#define MOTOR2 21
#define MOTOR3 20

#define MIN_PULSEWIDTH 1000
#define MAX_PULSEWIDTH 2000

Servo motor0;
Servo motor1;
Servo motor2;
Servo motor3;


void initMotors(){
  throttle_input = 0;
  output_motors = false;
  
  motor0.attach(MOTOR0);
  motor1.attach(MOTOR1);
  motor2.attach(MOTOR2);
  motor3.attach(MOTOR3);
  
  motor0.writeMicroseconds(MIN_PULSEWIDTH);
  motor1.writeMicroseconds(MIN_PULSEWIDTH);
  motor2.writeMicroseconds(MIN_PULSEWIDTH);
  motor3.writeMicroseconds(MIN_PULSEWIDTH);
}

void calc_motors_plus(){
  motor_value[0] = constrain(throttle_input - pid_output.pitch - pid_output.yaw + MIN_PULSEWIDTH, MIN_PULSEWIDTH, MAX_PULSEWIDTH);
  motor_value[1] = constrain(throttle_input - pid_output.roll + pid_output.yaw + MIN_PULSEWIDTH, MIN_PULSEWIDTH, MAX_PULSEWIDTH);
  motor_value[2] = constrain(throttle_input + pid_output.pitch - pid_output.yaw + MIN_PULSEWIDTH, MIN_PULSEWIDTH, MAX_PULSEWIDTH);
  motor_value[3] = constrain(throttle_input + pid_output.roll + pid_output.yaw + MIN_PULSEWIDTH, MIN_PULSEWIDTH, MAX_PULSEWIDTH);
}

void output(){
  //
  if(output_motors == true && robot.failsafe == false){
    motor0.writeMicroseconds(motor_value[0]);
    motor1.writeMicroseconds(motor_value[1]);
    motor2.writeMicroseconds(motor_value[2]);
    motor3.writeMicroseconds(motor_value[3]);
  }
  else{
    motor0.writeMicroseconds(MIN_PULSEWIDTH);
    motor1.writeMicroseconds(MIN_PULSEWIDTH);
    motor2.writeMicroseconds(MIN_PULSEWIDTH);
    motor3.writeMicroseconds(MIN_PULSEWIDTH);
  }
}
