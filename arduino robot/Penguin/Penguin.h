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

#ifndef PENGUIN_H
#define PENGUIN_H

typedef struct{
  float pitch;
  float roll;
  float yaw;
}anglePosition;

typedef struct{
  float P;
  float I;
  float D;
}pid_constants;

anglePosition sensor;
//anglePosition sensor; 

float sensor_temp;
boolean sensor_valid;

pid_constants pid_pitch;                     
pid_constants pid_roll;  
pid_constants pid_yaw;

anglePosition angle_input;
anglePosition angle_off;
anglePosition pid_output;


int throttle_input;
boolean output_motors;

int motor_value[4];

enum {
  PITCH  = 0,
  ROLL   = 1,
  YAW    = 2,
  MOT0   = 3,
  MOT1   = 4,
  MOT2   = 5,
  MOT3   = 6
};

enum {
  PITCH_P = 0,
  PITCH_I = 1,
  PITCH_D = 2,
  ROLL_P = 3,
  ROLL_I = 4,
  ROLL_D = 5,
  YAW_P = 6,
  YAW_I = 7,
  YAW_D = 8,
  PID_UPDATE_SUCCESS = 0x10
};

enum {
  INPUT_THRUST = 0,
  INPUT_PITCH = 1,
  INPUT_ROLL = 2,
  INPUT_YAW = 3,
  OFF_PITCH = 4,
  OFF_ROLL = 5,
  OFF_YAW = 6,
  OUTPUT_MOTORS = 7
};

#define GEDC6 Serial1
#define GEDC6_BAUD 115600
enum{
    LOOKING_FOR_HEADER,
    READING_DATA,
    CALCULATE_CHECKSUM
  };

#endif
