/*
 * 
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * ///////// This is a senior graduation project developed by Sami Khaled Nofal, ID: 0138086 ////////
 * /////// University of Jordan. Facultay of Engineering. Department of Electrical Engineering //////
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * 
 */



/*
 * 
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * /////////////////////////////////////// Include Libraries ////////////////////////////////////////
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * 
 */
#include "Wire.h"                       // Include I2C communication library
#include "TinyGPS++.h"                  // Include TinyGPS++ library for encoding GPS NMEA data





/*
 * 
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * /////////////////////////////////////// Initializations //////////////////////////////////////////
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * 
 */
// Initialize UARTs
HardwareSerial Serial_0(0);             // Serial 0
HardwareSerial Serial_2(2);             // Serial 2 

TinyGPSPlus GPS;                        // TinyGPS++ GPS data encoding





/*
 * 
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * /////////////////////////////////////// Global Variables /////////////////////////////////////////
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * 
 */
// IMU variables
int calMPU;
int16_t GyX, GyY, GyZ;
volatile int16_t Tmp;
float GyX_Cal, GyY_Cal, GyZ_Cal, AcX_Cal, AcY_Cal, AcZ_Cal;
int16_t AcX, AcY, AcZ, AccTotalVector;
float Angle_Pitch, Angle_Roll;
float Angle_Pitch_Acc, Angle_Roll_Acc;
int Angle_Pitch_Buffer, Angle_Roll_Buffer;
float Angle_Pitch_Output, Angle_Roll_Output;
float Roll_Level_Adjust, Pitch_Level_Adjust;

double Gyro_Pitch, Gyro_Roll, Gyro_Yaw;
float PID_Error_Temp;
float PID_I_Mem_Roll, PID_Roll_Setpoint, Gyro_Roll_Input, PID_Output_Roll, PID_Last_Roll_D_Error;
float PID_I_Mem_Pitch, PID_Pitch_Setpoint, Gyro_Pitch_Input, PID_Output_Pitch, PID_Last_Pitch_D_Error;
float PID_I_Mem_Yaw, PID_Yaw_Setpoint, Gyro_Yaw_Input, PID_Output_Yaw, PID_Last_Yaw_D_Error;
int Received_Throttle = 1500, Received_Roll = 1500, Received_Pitch = 1500, Received_Yaw = 1500;
byte Motors_Flag = 0;
long Loop_Timer;
int Throttle = 0, MaxThrottle = 1700;
boolean ThrottleFlag = false;
int Battery_Voltage;
int ESC_1, ESC_2, ESC_3, ESC_4;

// PID gain variables
float PID_P_Gain_Roll = 0.5;                // Gain setting for the roll P-controller
float PID_I_Gain_Roll = 0;                  // Gain setting for the roll I-controller
float PID_D_Gain_Roll = 15;                 // Gain setting for the roll D-controller
int PID_Max_Roll = 400;                     // Maximum roll output of the PID-controller (+/-)

float PID_P_Gain_Pitch = PID_P_Gain_Roll;  // Gain setting for the pitch P-controller.
float PID_I_Gain_Pitch = PID_I_Gain_Roll;  // Gain setting for the pitch I-controller.
float PID_D_Gain_Pitch = PID_D_Gain_Roll;  // Gain setting for the pitch D-controller.
int PID_Max_Pitch = PID_Max_Roll;          // Maximum pitch output of the PID-controller (+/-)

float PID_P_Gain_Yaw = 3.0;               // Gain setting for the yaw P-controller.
float PID_I_Gain_Yaw = 0.0;              // Gain setting for the yaw I-controller.
float PID_D_Gain_Yaw = 5.0;               // Gain setting for the yaw D-controller.
int PID_Max_Yaw = PID_Max_Roll;           // Maximum yaw output of the PID-controller (+/-)


// Pulse width modulation for ESP32 - Defined as LED PWM                 
#define PWM_CHANNEL_1 1                  // Use second channel of 16 channels (starts from zero) ... 
#define PWM_CHANNEL_2 2                  //  ... You can define other PWM channels here
#define PWM_CHANNEL_3 3
#define PWM_CHANNEL_4 4
#define PWM_TIMER_BIT 16                 // Use 16 bits precission for LEDC timer
#define PWM_FREQUENCY 250                // Use LEDC base frequency


// Multicore and task declararion
#define ESP32_CORE_0 0
#define ESP32_CORE_1 1
TaskHandle_t  Task_1;


// Interrupts settings and declarations - Timer & Extrernal
portMUX_TYPE Mux = portMUX_INITIALIZER_UNLOCKED; // Interrupt synchronization
/* For timer */
hw_timer_t * timer = NULL;
volatile boolean TimerIntterupt = false;
volatile byte BL_Sensors_TX_Count = 0;  // Bluetooth sensors transmission count
volatile byte BL_GPS_TX_Count = 0;      // Bluetooth GPS transmission count
/* For external */
const byte GPS_Interrupt = 26;
volatile boolean GPS_Int_LastCh = false;
volatile unsigned long GPS_Int_Timer = 0;
volatile unsigned long GPS_Int_RX;


// Bluetooth variables
int BL_Data = 0; 
char BL_RX_Pointer;
String BL_RX_String = "";


// Sensors Variables
volatile boolean SensorTX = false;
volatile int SensorData;
#define Sensor_1 25
#define Sensor_2 32
#define Sensor_3 35
#define Sensor_4 34
volatile byte SensorPointer = Sensor_1;


// GPS variables
#define GPS_EN_Pin 33
volatile byte GPS_Data_Pointer = 0, Satellites = 0;
volatile float Latitude = 0, Longitude = 0, Altitude = 0;
volatile boolean GPSFix = false; 


// Other variables
#define Baudrate_0 9600                   // UARTs baudrate
#define Baudrate_2 115200
#define SM_LED 2                          // Surface mount LED
byte Start;                               // Start flag to indicate starting or stopping the motors





/*
 * 
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * /////////////////////////////////////////// Functions ////////////////////////////////////////////
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * 
 */
void PWM_Write(uint8_t channel, uint32_t duty) {
  // Write duty to LEDC -> Convert duty from 1000 to 2000 PWM ...
  // ... to (2 ^ PWM_TIMER_BIT - 1) format using linear interpolation
  ledcWrite(channel, 16384 + ((duty-1000) * 16.384));               
}

// ESC Calibration
void ESC_Cali() {
  PWM_Write(PWM_CHANNEL_1, 2000);
  PWM_Write(PWM_CHANNEL_2, 2000);
  PWM_Write(PWM_CHANNEL_3, 2000);
  PWM_Write(PWM_CHANNEL_4, 2000);
  delay(2000);
  PWM_Write(PWM_CHANNEL_1, 1000);
  PWM_Write(PWM_CHANNEL_2, 1000);
  PWM_Write(PWM_CHANNEL_3, 1000);
  PWM_Write(PWM_CHANNEL_4, 1000);
  delay(2000);
}

// Setup IMU on start up
void setupIMU() {  
    Wire.begin();
    Wire.beginTransmission(0x68);                                       // Start communication with the address found during search.
    Wire.write(0x6B);                                                   // We want to write to the PWR_MGMT_1 register (6B hex)
    Wire.write(0x00);                                                   // Set the register bits as 00000000 to activate the IMU
    Wire.endTransmission(true);                                         // End the transmission with the gyro.

    Wire.begin();
    Wire.beginTransmission(0x68);                                       // Start communication with the address found during search.
    Wire.write(0x1B);                                                   // We want to write to the GYRO_CONFIG register (1B hex)
    Wire.write(0x08);                                                   // Set the register bits as 00001000 (500dps full scale)
    Wire.endTransmission(true);                                         // End the transmission with the gyro

    Wire.begin();
    Wire.beginTransmission(0x68);                                       // Start communication with the address found during search.
    Wire.write(0x1C);                                                   // We want to write to the ACCEL_CONFIG register (1A hex)
    Wire.write(0x10);                                                   // Set the register bits as 00010000 (+/- 8g full scale range)
    Wire.endTransmission(true);                                         // End the transmission with the gyro

    // Perform a random register check to see if the values are written correct
    Wire.begin();
    Wire.beginTransmission(0x68);                                       // Start communication with the address found during search
    Wire.write(0x1B);                                                   // Start reading @ register 0x1B
    Wire.endTransmission();                                             // End the transmission
    Wire.requestFrom(0x68, 1);                                          // Request 1 byte from the gyro
    while(Wire.available() < 1);                                        // Wait until the byte is received
    if(Wire.read() != 0x08){                                            // Check if the value is 0x08
      while(true) {
        // If true then loop forever while blinking the LED to indicate setup error
        digitalWrite(SM_LED, ! digitalRead(SM_LED)); 
        delay(100);
      }
    }

    Wire.begin();
    Wire.beginTransmission(0x68);                                       // Start communication with the address found during search
    Wire.write(0x1A);                                                   // We want to write to the CONFIG register (1A hex)
    Wire.write(0x03);                                                   // Set the register bits as 00000011 (Set Digital Low Pass Filter to ~43Hz)
    Wire.endTransmission(true);                                         // End the transmission with the gyro    
}

// Extract IMU data registers
void readIMU(void) {
  Wire.begin();
  Wire.beginTransmission(0x68);
  Wire.write(0x3B);  // starting with register 0x3B (ACCEL_XOUT_H)
  Wire.endTransmission(false);
  Wire.requestFrom(0x68,14,true);  // request a total of 14 registers
  AcX=Wire.read()<<8|Wire.read();  // 0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)    
  AcY=Wire.read()<<8|Wire.read();  // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
  AcZ=Wire.read()<<8|Wire.read();  // 0x3F (ACCEL_ZOUT_H) & 0x40 (ACCEL_ZOUT_L)
  Tmp=Wire.read()<<8|Wire.read();  // 0x41 (TEMP_OUT_H) & 0x42 (TEMP_OUT_L)
  GyX=Wire.read()<<8|Wire.read();  // 0x43 (GYRO_XOUT_H) & 0x44 (GYRO_XOUT_L)
  GyY=Wire.read()<<8|Wire.read();  // 0x45 (GYRO_YOUT_H) & 0x46 (GYRO_YOUT_L)
  GyZ=Wire.read()<<8|Wire.read();  // 0x47 (GYRO_ZOUT_H) & 0x48 (GYRO_ZOUT_L)
}

// Calibrate the IMU data
void caliIMU(void) {
  // Take multiple samples to determine the average gyro offset
  for(calMPU = 0; calMPU <= 2000 ; calMPU++){       // Take multiple readings
    if(calMPU % 15 == 0) {
      digitalWrite(SM_LED, !digitalRead(SM_LED));    // Change the surface mount LED status to indicate calibration.
    }
    readIMU();                                       // Read the raw acc and gyro data from the MPU-6050
    GyX_Cal += GyX;                                  // Add the gyro x-axis offset to the gyro-x calibrated variable
    GyY_Cal += GyY;                                  // Add the gyro y-axis offset to the gyro-y calibrated variable
    GyZ_Cal += GyZ;                                  // Add the gyro z-axis offset to the gyro-z calibrated variable  
    delay(3);                                        // Delay 3us to simulate the 250Hz program loop
  }
  GyX_Cal /= 2000;                                   // Divide the gyro-x calibrated variable by total readings to get the average offset
  GyY_Cal /= 2000;                                   // Divide the gyro-y calibrated variable by total readings to get the average offset
  GyZ_Cal /= 2000;                                   // Divide the gyro-z calibrated variable by total readings to get the average offset
  
  // Check if IMU calibration is accomplished or not 
  if(GyX_Cal == 0 || GyY_Cal == 0 || GyZ_Cal == 0) {
    while(true) {
      // Loop forever
      digitalWrite(SM_LED, ! digitalRead(SM_LED)); 
      delay(100);
    }
  } 
}

// PID Controller
void PID_Control(void) {
  // Roll calculations
  PID_Error_Temp = Gyro_Roll_Input - PID_Roll_Setpoint;
  PID_I_Mem_Roll += PID_I_Gain_Roll * PID_Error_Temp;
  if(PID_I_Mem_Roll > PID_Max_Roll)PID_I_Mem_Roll = PID_Max_Roll;
  else if(PID_I_Mem_Roll < PID_Max_Roll * -1)PID_I_Mem_Roll = PID_Max_Roll * -1;

  PID_Output_Roll = PID_P_Gain_Roll * PID_Error_Temp + PID_I_Mem_Roll + PID_D_Gain_Roll * (PID_Error_Temp - PID_Last_Roll_D_Error);
  if(PID_Output_Roll > PID_Max_Roll)PID_Output_Roll = PID_Max_Roll;
  else if(PID_Output_Roll < PID_Max_Roll * -1)PID_Output_Roll = PID_Max_Roll * -1;

  PID_Last_Roll_D_Error = PID_Error_Temp;

  // Pitch calculations
  PID_Error_Temp = Gyro_Pitch_Input - PID_Pitch_Setpoint;
  PID_I_Mem_Pitch += PID_I_Gain_Pitch * PID_Error_Temp;
  if(PID_I_Mem_Pitch > PID_Max_Pitch)PID_I_Mem_Pitch = PID_Max_Pitch;
  else if(PID_I_Mem_Pitch < PID_Max_Pitch * -1)PID_I_Mem_Pitch = PID_Max_Pitch * -1;

  PID_Output_Pitch = PID_P_Gain_Pitch * PID_Error_Temp + PID_I_Mem_Pitch + PID_D_Gain_Pitch * (PID_Error_Temp - PID_Last_Pitch_D_Error);
  if(PID_Output_Pitch > PID_Max_Pitch)PID_Output_Pitch = PID_Max_Pitch;
  else if(PID_Output_Pitch < PID_Max_Pitch * -1)PID_Output_Pitch = PID_Max_Pitch * -1;

  PID_Last_Pitch_D_Error = PID_Error_Temp;

  // Yaw calculations
  PID_Error_Temp = Gyro_Yaw_Input - PID_Yaw_Setpoint;
  PID_I_Mem_Yaw += PID_I_Gain_Yaw * PID_Error_Temp;
  if(PID_I_Mem_Yaw > PID_Max_Yaw)PID_I_Mem_Yaw = PID_Max_Yaw;
  else if(PID_I_Mem_Yaw < PID_Max_Yaw * -1)PID_I_Mem_Yaw = PID_Max_Yaw * -1;

  PID_Output_Yaw = PID_P_Gain_Yaw * PID_Error_Temp + PID_I_Mem_Yaw + PID_D_Gain_Yaw * (PID_Error_Temp - PID_Last_Yaw_D_Error);
  if(PID_Output_Yaw > PID_Max_Yaw)PID_Output_Yaw = PID_Max_Yaw;
  else if(PID_Output_Yaw < PID_Max_Yaw * -1)PID_Output_Yaw = PID_Max_Yaw * -1;

  PID_Last_Yaw_D_Error = PID_Error_Temp;
}

// Data transmission
void Data_Transmission(void) {
  BL_Sensors_TX_Count += 1;
  if(BL_Sensors_TX_Count <= 12) {
    if (SensorTX) {
      if(SensorPointer == Sensor_2) Serial_2.print("D" + (String)Tmp + ",");
      else {
        SensorData = analogRead(SensorPointer);
        Serial_2.print("D" + (String)SensorData + ",");
      } 
    }
  }
  else {
    BL_Sensors_TX_Count = 0;
    if(GPSFix) {
      BL_GPS_TX_Count += 1;
      if(BL_GPS_TX_Count <= 4) {
        switch(BL_GPS_TX_Count) {
          case 1: // Send latitude
            Serial_2.print("V");
            Serial_2.print(Latitude, 6);
            Serial_2.print(";");
            break;
          case 2: // Send longitude
            Serial_2.print("H");
            Serial_2.print(Longitude, 6);
            Serial_2.print(";");
            break;
          case 3: // Send altitude
            Serial_2.print("L");
            Serial_2.print(Altitude, 6);
            Serial_2.print(";");
            break;
          case 4: // Send satellites count
            Serial_2.print("I");
            Serial_2.print(Satellites);
            Serial_2.print(",");
            break;
        }
      }
      else BL_GPS_TX_Count = 0;
    }
  }
}


void GPS_Data(void) {
  // Check if GPS serial buffer is full
  if(Serial_0.available()) {
     GPS.encode(Serial_0.read());
     if(GPS.location.isUpdated()) {
        Latitude = GPS.location.lat();
        Longitude = GPS.location.lng();
     }
     if(GPS.altitude.isUpdated()) Altitude = GPS.altitude.meters();
     if(GPS.satellites.isUpdated()) Satellites = GPS.satellites.value();
  }
}

// Bluetooth transmission protocol 
void Bluetooth(void) {
  // Check if bluetooth serial buffer is full
  if(Serial_2.available()) {
    BL_Data = Serial_2.read();
    // Test if incoming data is a character
    if(!isDigit(BL_Data)) {
      switch (BL_Data) {
        case ',': // Integer data type
          if (BL_RX_Pointer == 'G') digitalWrite(GPS_EN_Pin, BL_RX_String.toInt()); // Toggle GPS
          
          else if(BL_RX_Pointer == 'M') Motors_Flag = BL_RX_String.toInt(); // Motors flag
          
          else if(BL_RX_Pointer == 'T') Received_Throttle = BL_RX_String.toInt(); // Throttle
          else if(BL_RX_Pointer == 'R') Received_Roll = BL_RX_String.toInt(); // Roll
          else if(BL_RX_Pointer == 'P') Received_Pitch = BL_RX_String.toInt(); // Pitch
          else if(BL_RX_Pointer == 'Y') Received_Yaw = BL_RX_String.toInt(); // Yaw

          else if(BL_RX_Pointer == 'D') { // Sensors 
            if (BL_RX_String.toInt() == 0) SensorTX = false; // Enable sensor data transmission
            else if (BL_RX_String.toInt() == 1) SensorTX = true; // Disable sensor data transmission
            else if (BL_RX_String.toInt() == 2) SensorPointer = Sensor_1; // Sensor pointer
            else if (BL_RX_String.toInt() == 3) SensorPointer = Sensor_2; // Sensor pointer
            else if (BL_RX_String.toInt() == 4) SensorPointer = Sensor_3; // Sensor pointer
            else if (BL_RX_String.toInt() == 5) SensorPointer = Sensor_4; // Sensor pointer
          }

          else if (BL_RX_Pointer == 'F') {
            if (BL_RX_String.toInt() == 0) ThrottleFlag = false;
            else  ThrottleFlag = true;
          }

          else if (BL_RX_Pointer == 'Q') {  // Transmite PID stored gains
            Serial_2.print("J");
            Serial_2.print(PID_P_Gain_Roll, 6);
            Serial_2.print(";");
            delay(20);
            Serial_2.print("X");
            Serial_2.print(PID_I_Gain_Roll, 6);
            Serial_2.print(";");
            delay(20);
            Serial_2.print("K");
            Serial_2.print(PID_D_Gain_Roll, 6);
            Serial_2.print(";");
          }

          else {
            BL_RX_String="";
            BL_RX_Pointer = 0;
          }

          BL_RX_String="";
          BL_RX_Pointer = 0;
          break;
          
        case ';': // Float data type
          if(BL_RX_Pointer == 'J') { // PID tuning - Kp
            PID_P_Gain_Roll = BL_RX_String.toFloat();
            PID_P_Gain_Pitch = BL_RX_String.toFloat();
          }
          // PID tuning - Ki
          else if(BL_RX_Pointer == 'X') {
            PID_I_Gain_Roll = BL_RX_String.toFloat();
            PID_I_Gain_Pitch = BL_RX_String.toFloat();
          }
          // PID tuning - Kd
          else if(BL_RX_Pointer == 'K') {
            PID_D_Gain_Roll = BL_RX_String.toFloat();
            PID_D_Gain_Pitch = BL_RX_String.toFloat();
          }

          else {
            BL_RX_String="";
            BL_RX_Pointer = 0;
          }
          
          BL_RX_String="";
          BL_RX_Pointer = 0;
          break;

        case '.':
          BL_RX_String += (char)BL_Data;
          break;

        default: // Character data 
          BL_RX_Pointer = BL_Data;
      }
    }
    // Test if incoming data is a number
    if(isDigit(BL_Data))BL_RX_String += (char)BL_Data;
  }
}





/*
 * 
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////// Core 0 Loop ///////////////////////////////////////////
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * 
 */
void coreTask_0( void * parameter ) {
  while(true) {
    Bluetooth();
    GPS_Data();
    if(TimerIntterupt) {
      TimerIntterupt = false;
      Data_Transmission();
    }
  }
}




/*
 * 
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * /////////////////////////////////// Interrupt Service Routine ////////////////////////////////////
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * 
 */
void IRAM_ATTR onTimer() {
  portENTER_CRITICAL_ISR(&Mux);
  TimerIntterupt = true;
  portEXIT_CRITICAL_ISR(&Mux);
}

void IRAM_ATTR ExtInterrupt() {
  portENTER_CRITICAL_ISR(&Mux);
  //******************* GPS Interrupt Channel *******************
  if(!GPS_Int_LastCh && digitalRead(GPS_Interrupt) == HIGH) {
    GPS_Int_LastCh = true;
    GPS_Int_Timer = millis();
  }
  else if(GPS_Int_LastCh && digitalRead(GPS_Interrupt) == LOW) {
    GPS_Int_LastCh = false;
    GPS_Int_RX = millis() - GPS_Int_Timer;
    // Send GPS no fix code if pulse is greater than 0.7s
    if(GPS_Int_RX >= 700) {
      GPSFix = false;
      Serial_2.print("G404,");
    }
    // Send GPS fix code if pulse is less than 0.3s   
    else if (GPS_Int_RX < 700) {
      GPSFix = true;
      Serial_2.print("G101,"); 
    }
  }
  portEXIT_CRITICAL_ISR(&Mux);
}




/*
 * 
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * ///////////////////////////////////////////// Setup //////////////////////////////////////////////
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * 
 */
void setup() {
  // Pins
  pinMode(SM_LED, OUTPUT);                // Output surface mount LED
  pinMode(GPS_EN_Pin, OUTPUT);            // GPS enable pin
  pinMode(Sensor_1, INPUT);               // Sensors pins 
  pinMode(Sensor_2, INPUT);                   
  pinMode(Sensor_3, INPUT);               
  pinMode(Sensor_4, INPUT);                  

  // Disable GPS on startup
  digitalWrite(GPS_EN_Pin, LOW);
  
  // Serial baudrates
  Serial_0.begin(Baudrate_0);
  Serial_2.begin(Baudrate_2);    

  // Setup PWM timer and attach PWM timer to a pin
  ledcSetup(PWM_CHANNEL_1, PWM_FREQUENCY, PWM_TIMER_BIT);
  ledcSetup(PWM_CHANNEL_2, PWM_FREQUENCY, PWM_TIMER_BIT);
  ledcSetup(PWM_CHANNEL_3, PWM_FREQUENCY, PWM_TIMER_BIT);
  ledcSetup(PWM_CHANNEL_4, PWM_FREQUENCY, PWM_TIMER_BIT);
  ledcAttachPin(12, PWM_CHANNEL_1);
  ledcAttachPin(13, PWM_CHANNEL_2);
  ledcAttachPin(14, PWM_CHANNEL_3);
  ledcAttachPin(27, PWM_CHANNEL_4);

  // Signal a pulse lower than 1000us to each ESC to disable beeping --> 998
  ledcWrite(PWM_CHANNEL_1, 16000); 
  ledcWrite(PWM_CHANNEL_2, 16000); 
  ledcWrite(PWM_CHANNEL_3, 16000); 
  ledcWrite(PWM_CHANNEL_4, 16000); 
  
  // IMU setup and calibration
  setupIMU();
  caliIMU();
  
  // Multicore task declaration
  xTaskCreatePinnedToCore(
      coreTask_0,                 // Function to implement the task
      "Task_1",                   // Name of the task
      10000,                      // Stack size in words
      NULL,                       // Task input parameter
      0,                          // Priority of the task (Higher number --> higher priority [Priority of setup and main loop is 1])
      NULL,                       // Task handle
      ESP32_CORE_0                // Core where the task should run
  );

  // Timer interrupt setup
  timer = timerBegin(0, 80, true);                // Timer number (0-3, Parameter # 1), Assume 80MHz frequencey for timers (Depending on board, Parameter # 2), Count Up(True)/Count Down(False) (Parameter # 3)
  timerAttachInterrupt(timer, &onTimer, true);    // Attach on call interrupt function (Parmeter # 2), Edge(True) or Level(False) type interrupt(Parmeter # 3)
  timerAlarmWrite(timer, 25000, true);            // Assume 80Mhz frequency divide by 80 prodcues 1MHz thus 1000000uS (Parameter # 2), Periodical interrupt generation (Parameter # 3)
  timerAlarmEnable(timer);                        // Enable timer

  // External interrupt setup
  pinMode(GPS_Interrupt, INPUT);
  attachInterrupt(digitalPinToInterrupt(GPS_Interrupt), ExtInterrupt, CHANGE);

  // Wait until the receiver is active and the throttle is set to the lower position.
  while(Received_Throttle < 990 || Received_Throttle > 1020){
      digitalWrite(SM_LED, ! digitalRead(SM_LED)); 
      delay(500);
      readIMU();                                    // Read IMU data on hold
  }
  Start = 0;                                        // Set start to 0
  
  // Turn on LED (Setup is completed)
  digitalWrite(SM_LED, HIGH);
  
  // Reset the loop timer
  Loop_Timer = micros();   
}




/*
 * 
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * ////////////////////////////////////////// Core 1 Loop ///////////////////////////////////////////
 * //////////////////////////////////////////////////////////////////////////////////////////////////
 * 
 */
void loop() {
  /* **************************************************************************
   * ************************** Calculate IMU angles **************************
   * **************************************************************************
  */
  // Read IMU data
  readIMU();
  
  // Subtract the offset calibration value from the raw value and store it.
  // Gyro angles pitch and roll depends on the orientation of the IMU.
  Gyro_Roll = GyX - GyX_Cal;
  Gyro_Pitch = GyY - GyY_Cal;
  Gyro_Yaw = GyZ - GyZ_Cal;

  // 65.5 = 1 deg/sec (As per the datasheet of the MPU-6050).
  Gyro_Roll_Input = (Gyro_Roll_Input * 0.7) + ((Gyro_Roll / 65.5) * 0.3);    // Gyro pid input is deg/sec.
  Gyro_Pitch_Input = (Gyro_Pitch_Input * 0.7) + ((Gyro_Pitch / 65.5) * 0.3); // Gyro pid input is deg/sec.
  Gyro_Yaw_Input = (Gyro_Yaw_Input * 0.7) + ((Gyro_Yaw / 65.5) * 0.3);       // Gyro pid input is deg/sec.

  // Gyro angle calculations -> 0.0000611 = 1 / (250Hz / 65.5)
  Angle_Pitch += Gyro_Pitch * 0.0000611;                                      // Calculate the traveled pitch angle and add this to the Angle_Pitch variable
  Angle_Roll += Gyro_Roll * 0.0000611;                                        // Calculate the traveled roll angle and add this to the Angle_Roll variable

  // 0.000001066 = 0.0000611 * (3.142(PI) / 180deg) -> The Arduino sin function is in radians
  Angle_Pitch -= Angle_Roll * sin(Gyro_Yaw * 0.000001066);                   // If the IMU has yawed transfer the roll angle to the pitch angle
  Angle_Roll += Angle_Pitch * sin(Gyro_Yaw * 0.000001066);                   // If the IMU has yawed transfer the pitch angle to the roll angle

  // Accelerometer angle calculations
  AccTotalVector = sqrt((AcX*AcX)+(AcY*AcY)+(AcZ*AcZ));                      // Calculate the total accelerometer vector.
  // 57.296 = 1 / (3.142 / 180) -> The Arduino asin function is in radians
  if(abs(AcY) < AccTotalVector){                                             // Prevent the asin function to produce a NaN
    Angle_Pitch_Acc = asin((float)AcX/AccTotalVector)* -57.296;               // Calculate the pitch angle in degrees.
  }
  if(abs(AcX) < AccTotalVector){                                             // Prevent the asin function to produce a NaN
    Angle_Roll_Acc = asin((float)AcY/AccTotalVector)* +57.296;               // Calculate the roll angle in degrees.
  }

  // Place the MPU-6050 at spirit level and note the values in the following two lines for calibration.
  Angle_Pitch_Acc += 0.8;                                                    // Accelerometer calibration value for pitch.
  Angle_Roll_Acc -= 0.6;                                                     // Accelerometer calibration value for roll.

  // Apply complementary filter when the motors have started.
  if (Start == 2) {
    // Complemenraty filter
    Angle_Pitch = Angle_Pitch * 0.9996 + Angle_Pitch_Acc * 0.0004;           // Correct the drift of the gyro pitch angle with the accelerometer pitch angle.
    Angle_Roll = Angle_Roll * 0.9996 + Angle_Roll_Acc * 0.0004;              // Correct the drift of the gyro roll angle with the accelerometer roll angle.
  }



  /* **************************************************************************
   * ******************** Starting and stopping the motors ********************
   * **************************************************************************
  */
  // For starting the motors: throttle low and yaw left (step 1).
  if(Received_Throttle < 1200 && Received_Yaw < 1200) Start = 1;
  // When yaw stick is back in the center position start the motors (step 2).
  if(Start == 1 && Received_Throttle < 1050 && Received_Yaw > 1450){
    Start = 2;

    Angle_Pitch = Angle_Pitch_Acc;    // Set the gyro pitch angle equal to the accelerometer pitch angle when the quadcopter is started.
    Angle_Roll = Angle_Roll_Acc;      // Set the gyro roll angle equal to the accelerometer roll angle when the quadcopter is started.

    //Reset the PID controllers for a bump-less start.
    PID_I_Mem_Roll = 0;
    PID_Last_Roll_D_Error = 0;
    PID_I_Mem_Pitch = 0;
    PID_Last_Pitch_D_Error = 0;
    PID_I_Mem_Yaw = 0;
    PID_Last_Yaw_D_Error = 0;
  }
  //Stopping the motors: throttle low and motor flag is true.
  if(Start == 2 && Received_Throttle < 1050 && Motors_Flag == 1)Start = 0;


  /* **************************************************************************
   * ***************************** PID Controller *****************************
   * **************************************************************************
   */
  Pitch_Level_Adjust = Angle_Pitch * 15;                                     // Calculate the pitch angle correction
  Roll_Level_Adjust = Angle_Roll * 15;                                       // Calculate the roll angle correction
   
  // The PID set point in degrees per second is determined by the roll receiver input.
  // In the case of dividing by 3, the max roll rate is aprox 164 degrees per second ( (500-8)/3 = 164d/s ).
  PID_Roll_Setpoint = 0;
  // A dead band of 16us is applied for better results.
  if(Received_Roll > 1508)PID_Roll_Setpoint = Received_Roll - 1508;
  else if(Received_Roll < 1492)PID_Roll_Setpoint = Received_Roll - 1492;
  PID_Roll_Setpoint -= Roll_Level_Adjust;                                   // Subtract the angle correction from the standardized receiver roll input value.
  PID_Roll_Setpoint /= 3.0;                                                 // Divide the setpoint for the PID roll controller by 3 to get angles in degrees.

  // The PID set point in degrees per second is determined by the pitch receiver input.
  // In the case of dividing by 3, the max pitch rate is aprox 164 degrees per second ( (500-8)/3 = 164d/s ).
  PID_Pitch_Setpoint = 0;
  // A dead band of 16us is applied for better results.
  if(Received_Pitch > 1508)PID_Pitch_Setpoint = Received_Pitch - 1508;
  else if(Received_Pitch < 1492)PID_Pitch_Setpoint = Received_Pitch - 1492;
  PID_Pitch_Setpoint -= Pitch_Level_Adjust;                                  // Subtract the angle correction from the standardized receiver pitch input value.
  PID_Pitch_Setpoint /= 3.0;                                                 // Divide the setpoint for the PID pitch controller by 3 to get angles in degrees.

  // The PID set point in degrees per second is determined by the yaw receiver input.
  // In the case of dividing by 3, the max yaw rate is aprox 164 degrees per second ( (500-8)/3 = 164d/s ).
  PID_Yaw_Setpoint = 0;
  // A dead band of 16us is applied for better results.
  if(Received_Throttle > 1050){ // Do not yaw when turning off the motors.
    if(Received_Yaw > 1508)PID_Yaw_Setpoint = (Received_Yaw - 1508)/3.0;
    else if(Received_Yaw < 1492)PID_Yaw_Setpoint = (Received_Yaw - 1492)/3.0;
  }
  
  // Apply PID algorithm
  PID_Control();


  /* **************************************************************************
   * ********************* Control the speed of the motors ********************
   * **************************************************************************
   */
  Throttle = Received_Throttle;                                             // Make throttle signal as a base signal.
  if (Start == 2){                                                          // The motors are started.
    if (ThrottleFlag) Throttle = MaxThrottle;                               

    ESC_1 = Throttle - PID_Output_Pitch - PID_Output_Roll + PID_Output_Yaw; // Calculate the pulse for ESC_1 (front-right - CCW) - PWM_CHANNEL_2
    ESC_2 = Throttle + PID_Output_Pitch - PID_Output_Roll - PID_Output_Yaw; // Calculate the pulse for ESC_2 (rear-right - CW) - PWM_CHANNEL_1
    ESC_3 = Throttle + PID_Output_Pitch + PID_Output_Roll + PID_Output_Yaw; // Calculate the pulse for ESC_3 (rear-left - CCW) - PWM_CHANNEL_3
    ESC_4 = Throttle - PID_Output_Pitch + PID_Output_Roll - PID_Output_Yaw; // Calculate the pulse for ESC_4 (front-left - CW) - PWM_CHANNEL_4

    if (ESC_1 < 1100) ESC_1 = 1100;                                         // Keep the motors running.
    if (ESC_2 < 1100) ESC_2 = 1100;                                         // Keep the motors running.
    if (ESC_3 < 1100) ESC_3 = 1100;                                         // Keep the motors running.
    if (ESC_4 < 1100) ESC_4 = 1100;                                         // Keep the motors running.

    if(ESC_1 > 2000)ESC_1 = 2000;                                           // Limit the ESC_1 pulse to 2000us.
    if(ESC_2 > 2000)ESC_2 = 2000;                                           // Limit the ESC_2 pulse to 2000us.
    if(ESC_3 > 2000)ESC_3 = 2000;                                           // Limit the ESC_3 pulse to 2000us.
    if(ESC_4 > 2000)ESC_4 = 2000;                                           // Limit the ESC_4 pulse to 2000us.  
  }

  else {
    ESC_1 = 1000;                                                           // If start is not 2 keep a 1000us pulse for ESC_1.
    ESC_2 = 1000;                                                           // If start is not 2 keep a 1000us pulse for ESC_2.
    ESC_3 = 1000;                                                           // If start is not 2 keep a 1000us pulse for ESC_3.
    ESC_4 = 1000;                                                           // If start is not 2 keep a 1000us pulse for ESC_4.
  }

  // Warns the developer if the code's processing time is greater than 4ms
  if(micros()- Loop_Timer > 4000) {
    digitalWrite(SM_LED, LOW);
  }

  while(micros() - Loop_Timer < 4000);                                      // Wait until 4000us are passed - Refresh rate of ESC.
  Loop_Timer = micros();                                                    // Set the timer for the next loop.

  // Write PWM to ESCs 
  PWM_Write(PWM_CHANNEL_2, ESC_1);
  PWM_Write(PWM_CHANNEL_1, ESC_2);
  PWM_Write(PWM_CHANNEL_3, ESC_3);
  PWM_Write(PWM_CHANNEL_4, ESC_4);
}
