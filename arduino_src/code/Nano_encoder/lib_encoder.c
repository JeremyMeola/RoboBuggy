/**
 * @file lib_encoder.c
 * @author Joseph Paetz (rpaetz)
 */
#include <Arduino.h>
#include "lib_encoder.h"

//-------local Variables-----------------
//required to be volatile for interrupts
volatile int encCount = 0;

//-------local function prototypes-----------
void increment_encoder();

//------------global functions---------------
//sets up the encoder with the given Pin
void encoder_init(){
	pinMonde(ENC_PIN, INPUT);
	attachInterrupt(INTERRUPT_NUM, increment_encoder, CHANGE); 
}

void get_enc_count(){
	return encCount;
}

//-----------local Functions------------------
//this will count every time the encoder goes from 
//high to low or low to high
void increment_encoder(){
	encCount++;
}