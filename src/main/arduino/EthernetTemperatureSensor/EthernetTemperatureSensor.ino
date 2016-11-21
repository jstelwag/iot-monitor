#include <OneWire.h>
#include <DallasTemperature.h>
#include <SPI.h>
#include <Ethernet.h>
#include <EEPROM.h>
/**
 * Senses the temperature on all attached sensors and sends it to a server
*/

#define flash_props // Writes property setting into eeprom memory

const byte ONE_WIRE_BUS_PIN = 2;
const byte MAX_SENSORS = 10;
const float MAX_TEMP_CHANGE_THRESHOLD_85 = 0.2;

struct Properties {
  char deviceId[20];
  uint8_t macAddress[6];
  IPAddress masterIP;
  uint16_t masterPort;
};

OneWire oneWire(ONE_WIRE_BUS_PIN);
DallasTemperature sensors(&oneWire);

byte sensorAddresses[MAX_SENSORS][8]; //Max 12 sensors addresses (control loops) of 8 bytes
byte sensorCount = 0;
float currentTemperature[MAX_SENSORS];

EthernetClient client;
long lastPostTime;
const long POSTING_INTERVAL = 30000;

void setup(void) {
  Serial.begin(9600);
  #ifdef flash_props
    IPAddress masterIP(192, 168, 178, 18);
    Properties pIn = {"koetshuis_serre", {0xCC, 0x1B, 0xDE, 0xEF, 0xFE, 0x14}, masterIP, 9999};
    EEPROM.put(0, pIn);
  #endif

  Properties prop;
  EEPROM.get(0, prop);
  dhcp(prop.macAddress);
  sensors.begin();
  setupSensors();
}

void loop(void) {
  if (millis() > lastPostTime + POSTING_INTERVAL || millis() < lastPostTime) {
    temperatures();
    post();
  }
}

void temperatures() {
  sensors.requestTemperatures();
  for (byte b = 0; b < sensorCount; b++) {
    float t = sensors.getTempC(sensorAddresses[b]);
    currentTemperature[b] = filterSensorTemp(t, currentTemperature[b]);
  }
}

void post() {
  Serial.print(F("Posting "));
  client.stop();
  
  Properties prop;
  EEPROM.get(0, prop);
  char comma = ':';

  if (client.connect(prop.masterIP, prop.masterPort)) {
    client.print(prop.deviceId);
    client.print(comma);
    Serial.print(prop.deviceId);
    Serial.print(comma);

    byte checksum = 0;
    for (byte b = 0; b < sensorCount; b++) {
      client.print(currentTemperature[b]);
      client.print(comma);
      Serial.print(currentTemperature[b]);
      Serial.print(comma);
      checksum += (byte)currentTemperature[b];
    }
    client.println(checksum);
    Serial.println(checksum);
  } else {
    Serial.println(F(" failed"));
    client.stop();
    Ethernet.maintain();
  }
  lastPostTime = millis();
}

/**
* Filters typical sensor failure at 85C and -127C
*/
float filterSensorTemp(float rawSensorTemp, float currentTemp) {
  if (rawSensorTemp == 85.0 && (abs(rawSensorTemp - 85) > MAX_TEMP_CHANGE_THRESHOLD_85)) {
    return currentTemp;
  } else if (rawSensorTemp == -127.0) {
    return currentTemp;
  } else {
    return rawSensorTemp;
  }
}

// ######################################## SETUP
void setupSensors() {
  byte addr[8];
  byte loopCount = 0;
  while (oneWire.search(addr)) {
 //   Serial.print(loopCount);
 //   Serial.print(" - ");
    for (byte i = 0; i < 8; i++) {
 //     Serial.write(' ');
 //     Serial.print(addr[i], HEX);
      sensorAddresses[loopCount][i] = addr[i];
    }
    loopCount++;
 //   Serial.println();
  }
 // Serial.print(loopCount);
 // Serial.println(F(" sensors found"));
  sensorCount = loopCount;
}


void dhcp(uint8_t *macAddress) {
  Serial.print(F("Connecting to network... "));
  delay(1000); // give the ethernet module time to boot up
  if (Ethernet.begin(macAddress) == 0) {
    Serial.println(F("failed"));
  } else {
    Serial.println(F("success"));
  }
}
// ######################################## /SETUP
