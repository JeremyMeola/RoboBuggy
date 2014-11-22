#include <Servo.h>

Servo brake;
Servo steer;

int brakeWritePin = 2;
int brakeReadPin = 1;

int steerWritePin = 3;
int steerReadPin = 2;

void setup() {
  Serial.begin(9600);
  brake.attach(brakeWritePin);
  steer.attach(steerWritePin);
}

void loop() {
  int brakeVal = map(analogRead(brakeReadPin), 512, 1024, 0, 255);
  brake.write(brakeVal);
  int steerVal = map(analogRead(steerReadPin), 512, 1024, 0, 255);
  steer.write(steerVal);
  Serial.print(brakeVal);
  Serial.print(" ");
  Serial.println(steerVal);
}
