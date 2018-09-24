#include <SoftwareSerial.h>
#include "DHT.h"
#include <HX711_ADC.h>

#define RX  10
#define TX  11

#define DHTPIN 8     // DHT 22 connection pin

#define DHTTYPE DHT22   // DHT 22  (AM2302), AM2321

SoftwareSerial gsm(RX, TX);


DHT dht(DHTPIN, DHTTYPE);
HX711_ADC LoadCell(4, 5);


String textSms;

char number[10] = "0743674322";

float temperature;
float humidity;
float weight;


char sWeight[10];
char sTemperature[10];
char sHumidity[10];


char weightText[45] = "*** B e e S c a l e ***\n\nWeight: ";
char temperatureText[35] = " g\nTemperature: ";
char humidityText[35] = " *C\nHumidity: ";

int w;
int t;
int h;

long stabilisingtime = 2000; // tare preciscion can be improved by adding a few seconds of stabilising time

char response[150];

void setup() {

  Serial.begin(9600); // only for debug the results .
  Serial.println(F("Testing GSM SIM800L"));
  gsm.begin(9600);

  Serial.println(F("Connecting .... "));
  //wait until SIM800L register to network
  while ( sendATcommand("AT+CREG?", "+CREG: 0,1", "+CREG: 0,5", 1000) == 0 );
  Serial.println(F("Setup finished!"));
  delay(2000);
  gsm.println("AT"); //checking
  delay(1000);
  gsm.println("AT+CMGF=1");  //Set to Text mode
  delay(1000);
  gsm.println("AT+CNMI=1, 2, 0, 0, 0"); //Set to notification for new message, New message indication

  dht.begin();

  LoadCell.begin();

  LoadCell.start(stabilisingtime);
  LoadCell.setCalFactor(-413.00); // user set calibration factor (float)
  //LoadCell.tareNoDelay();

}

void loop() {
  //Read weight as grams
  //update() should be called at least as often as HX711 sample rate; >10Hz@10SPS, >80Hz@80SPS
  LoadCell.update();

  if (gsm.available()) {
    //Check if GSM Send any data
    textSms = gsm.readString(); //Read data received from SIM800L GSM Module

    Serial.println(F(textSms));

    if (textSms.length() > 7)  // optional you can avoid SMS empty
    {
      textSms.toUpperCase(); 

      if (textSms.indexOf("TURNON") != -1) {
        Serial.println(F("Received SMS"));

        //Read weight
        weight = LoadCell.getData();

        // Reading temperature or humidity takes about 250 milliseconds!
        // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
        humidity = dht.readHumidity();

        // Read temperature as Celsius (the default)
        temperature = dht.readTemperature();

        w = weight;
        t = temperature;
        h = humidity;

        char sWeight[10] = "";
        char sTemperature[10] = "";
        char sHumidity[10] = "";

        char text[150] = "";

        //put all values into char arrays
        int i = 0;
        while (t > 0) {
          sTemperature[i] = (t % 10) + '0';
          t = t / 10;
          i++;
        }
        i = 0;
        while (h > 0) {
          sHumidity[i] = (h % 10) + '0';
          h = h / 10;
          i++;
        }
        i = 0;
        if (w <= 0)
        {
          sWeight[0] = '0';
        }
        else {
          while (w > 0) {
            sWeight[i] = (w % 10) + '0';
            w = w / 10;
            i++;
          }
        }


        //construct the sms
        for (int j = 0; j < strlen(weightText); j++)
        {
          text[j] = weightText[j];
        }

        for (int j = 0; j < strlen(sWeight); j++)
        {
          text[strlen(weightText) + strlen(sWeight) - j - 1] = sWeight[j];
        }


        for (int j = 0; j < strlen(temperatureText); j++)
        {
          text[strlen(weightText) + strlen(sWeight) + j] = temperatureText[j];
        }

        for (int j = 0; j < strlen(sTemperature); j++)
        {
          text[strlen(weightText) + strlen(sWeight) + strlen(temperatureText) + strlen(sTemperature) - j - 1] = sTemperature[j];
        }

        for (int j = 0; j < strlen(humidityText); j++)
        {
          text[strlen(weightText) + strlen(sWeight) + strlen(temperatureText) + strlen(sTemperature) + j] = humidityText[j];
        }

        for (int j = 0; j < strlen(sHumidity); j++)
        {
          text[strlen(weightText) + strlen(sWeight) + strlen(temperatureText) + strlen(sTemperature) + strlen(humidityText) + strlen(sHumidity) - j - 1] = sHumidity[j];
        }

        text[strlen(text)] = '%';

        //start to send Sms
        gsm.println("AT+CMGS=\"0743674322\"");
        delay(1000);
        gsm.println(text);
        delay(1000);
        gsm.println((char)26); //CTRL-Z
        delay(1000);
        Serial.println(F("Successfully sent SMS"));

        //delete all sms
        gsm.println("AT+CMGDA=\"DEL ALL\"");
        Serial.println(F("Successfully deleted messages\n"));
        delay(1000);

        textSms="";
      }
    }
  }
}

int8_t sendATcommand(char const* ATcommand, char const* expected_answer1, char const* expected_answer2, unsigned int timeout) {

  uint8_t x = 0, answer = 0;
  unsigned long previous;

  memset(response, '\0', 150);    // Initialize the string
  delay(100);
  while ( gsm.available() > 0) gsm.read();   // Clean the input buffer
  gsm.println(ATcommand);    // Send the AT command
  x = 0;
  previous = millis();

  // this loop waits for the answer
  do {
    // if there are data in the UART input buffer, reads it and checks for the asnwer
    if (gsm.available() != 0) {
      response[x] = gsm.read();
      x++;
      if (x >= 150) { //Overflow protection
        Serial.println(response);
        memset(response, 0, 150);    // Initialize the string
        x = 0;
      }
      // check if the desired answer 1  is in the response of the module
      if (strstr(response, expected_answer1) != NULL)
      {
        answer = 1;
      }
      // check if the desired answer 2 is in the response of the module
      else if (strstr(response, expected_answer2) != NULL)
      {
        answer = 2;
      }
    }
  }
  // Waits for the asnwer with time out
  while ((answer == 0) && ((millis() - previous) < timeout));
  Serial.println(response);
  return answer;
}
