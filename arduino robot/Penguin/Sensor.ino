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

#define GEDC6 Serial1
#define GEDC6_BAUD 115600
enum{
    LOOKING_FOR_HEADER,
    READING_DATA,
    CALCULATE_CHECKSUM
  };

const static int BUFFER_SIZE = 128;  
const static char MESSAGE_HEADER = '$';
const static char MESSAGE_FOOTER = '\n';
const static char MESSAGE_CHECKSUM = '*';

const static char* INIT_MESSAGE = "$HCXDR,RPT=.01"; //100 HZ

char gedc6_buf[BUFFER_SIZE];
int gedc6_length = 0;
int gedc6_state = LOOKING_FOR_HEADER;

void initGEDC6(){
  sensor.pitch = 0.0;
  sensor.roll = 0.0;
  sensor.yaw = 0.0;
  sensor_temp = 0.0;
  sensor_valid = false;
  GEDC6.begin(GEDC6_BAUD);
  delay(1000);
  GEDC6.println(INIT_MESSAGE);
}

void reInitGEDC6(){
  GEDC6.println(INIT_MESSAGE);
}

void readGEDC6(){
  
  while(GEDC6.available()>0){
    gedc6_buf[gedc6_length++] = GEDC6.read();
    
    switch(gedc6_state){
    case LOOKING_FOR_HEADER:
      if(gedc6_buf[0] == MESSAGE_HEADER){
        gedc6_state = READING_DATA;
      }
      else{
        gedc6_length = 0;
      }
      break;
    case READING_DATA:
      if(gedc6_buf[gedc6_length-1] == MESSAGE_HEADER){
        //restart because we found header
        gedc6_buf[0] = MESSAGE_HEADER;
        gedc6_length = 1;
      }
      else if (gedc6_length >= BUFFER_SIZE){
        //restart since buffer full and no valid data yet
        gedc6_buf[0] = '\0';
        gedc6_length = 0;
      }
      if(gedc6_buf[gedc6_length-1] == MESSAGE_FOOTER){
        int pos = 1;
        int checksum = 0;
        while(gedc6_buf[pos] != MESSAGE_CHECKSUM && pos < gedc6_length){
          checksum = checksum ^ gedc6_buf[pos++];
        }
        
        char* cToken;
        //$HCXDR,A,195.5,D,A,195.5,D,A,-19.1,D,A,+123.3,D,C,+34.1,C,G,017*17
        strtok(gedc6_buf, "$,*"); //HCXDR
        strtok(NULL, "$,*"); //A
        cToken = strtok(NULL, "$,*"); //yaw
        sensor.yaw = atof(cToken); 
        strtok(NULL, "$,*"); //D
        strtok(NULL, "$,*"); //A
        strtok(NULL, "$,*"); //yaw mag compensated 
        strtok(NULL, "$,*"); //D
        strtok(NULL, "$,*"); //A
        cToken = strtok(NULL, "$,*"); //pitch
        sensor.pitch = atof(cToken);
        strtok(NULL, "$,*"); //D
        strtok(NULL, "$,*"); //A
        cToken = strtok(NULL, "$,*"); //roll
        sensor.roll = atof(cToken);
        strtok(NULL, "$,*"); //D
        strtok(NULL, "$,*"); //C
        cToken = strtok(NULL, "$,*"); //temp
        sensor_temp = atof(cToken);
        strtok(NULL, "$,*"); //C
        strtok(NULL, "$,*"); //G
        strtok(NULL, "$,*"); //megnatic error
        
        cToken = strtok(NULL, "$,*\n"); //checksum
        int checksum2 = xtoi(cToken);
        if(checksum2 == checksum){
          sensor_valid = true;
          PID();
          calc_motors_plus();
          output();
        }
        gedc6_state = LOOKING_FOR_HEADER;
      }
      break;
    }
  }
}

unsigned long xtoi(const char* xs){
  
  int len = strlen(xs);
  unsigned long result = 0;

  for(int i = 0; i<len; i++){
    int temp = 0;
    if(xs[i] >= '0' && xs[i] <= '9'){
      temp = xs[i] - '0';
    }
    else if(xs[i] >= 'A' && xs[i] <= 'F'){
      temp = xs[i] - 'A' + 10;
    }
    else if(xs[i] >= 'a' && xs[i] <= 'f'){
      temp = xs[i] - 'a' + 10;
    }
    
    result = result << 4;
    result = result | (unsigned long)temp;  
  }
  return result;
}
