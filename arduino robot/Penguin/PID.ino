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

#define MIN_I_PITCH -50
#define MAX_I_PITCH 50
#define MIN_I_ROLL -50
#define MAX_I_ROLL 50
#define MIN_I_YAW -30
#define MAX_I_YAW 30

#define MIN_PITCH -250
#define MAX_PITCH 250
#define MIN_ROLL -250
#define MAX_ROLL 250
#define MIN_YAW -250
#define MAX_YAW 250

anglePosition err_prev;
anglePosition err_P;
anglePosition err_I;
anglePosition err_D;

unsigned long intergrationTimePID = 0;

void initPID(){
  
  pid_pitch.P = 4.0;
  pid_pitch.I = 0.15;
  pid_pitch.D = 1.2;
  
  pid_roll.P = 4.0;
  pid_roll.I = 0.15;
  pid_roll.D = 1.2;

  pid_yaw.P = 3.0;
  pid_yaw.I = 0.1;
  pid_yaw.D = 1.2; 
 
  angle_input.pitch = 0.0;
  angle_input.roll = 0.0;
  angle_input.yaw = 0.0;
 
  angle_off.pitch = 0.0;
  angle_off.roll = 0.0;
  angle_off.yaw = 0.0;
  
  err_prev.pitch = 0.0;
  err_prev.roll = 0.0;
  err_prev.yaw = 0.0;
  
  err_I.pitch = 0.0;
  err_I.roll = 0.0;
  err_I.yaw = 0.0;
}

void PID(){
  float dt = ((float)(micros() - intergrationTimePID))/1000000.0;
  intergrationTimePID = micros();
  
  //PITCH
  err_P.pitch = angle_input.pitch - sensor.pitch + angle_off.pitch;
  err_I.pitch = err_I.pitch + err_P.pitch;
  err_I.pitch = constrain(err_I.pitch,MIN_I_PITCH,MAX_I_PITCH);
  err_D.pitch = err_P.pitch - err_prev.pitch;
  pid_output.pitch = (err_P.pitch *pid_pitch.P) + (err_I.pitch * pid_pitch.I) + (err_D.pitch * pid_pitch.D);
  pid_output.pitch = constrain(pid_output.pitch,MIN_PITCH,MAX_PITCH);
  err_prev.pitch = err_P.pitch;
  
  //ROLL
  err_P.roll = angle_input.roll - sensor.roll + angle_off.roll;
  err_I.roll = err_I.roll + err_P.roll;
  err_I.roll = constrain(err_I.roll,MIN_I_ROLL,MAX_I_ROLL);
  err_D.roll = err_P.roll - err_prev.roll;
  pid_output.roll = (err_P.roll * pid_roll.P) + (err_I.roll * pid_roll.I) + (err_D.roll * pid_roll.D);
  pid_output.roll = constrain(pid_output.roll,MIN_ROLL,MAX_ROLL);
  err_prev.roll = err_P.roll;
  
  //YAW
  err_P.yaw = angle_input.yaw - sensor.yaw + angle_off.yaw;
  if (err_P.yaw > 180.0)    // Normalize to -180,180
    err_P.yaw -= 360.0;
  else if(err_P.yaw < -180.0)
    err_P.yaw += 360.0;
    
  err_I.yaw = err_I.yaw + err_P.yaw;
  err_I.yaw = constrain(err_I.yaw,MIN_I_YAW,MAX_I_YAW);
  err_D.yaw = err_P.yaw - err_prev.yaw;
  pid_output.yaw = (err_P.yaw * pid_yaw.P) + (err_I.yaw * pid_yaw.I) + (err_D.yaw * pid_yaw.D);
  pid_output.yaw = constrain(pid_output.yaw,MIN_YAW,MAX_YAW);
  err_prev.yaw = err_P.yaw;
}

