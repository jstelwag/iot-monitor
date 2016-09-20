#include <SPI.h>
#include <Ethernet.h>

boolean received = false;
EthernetClient client;
const long POSTING_INTERVAL = 10000;
long lastPostTime = -POSTING_INTERVAL;
IPAddress masterController(192, 168, 178, 18);

void setup(void) {
  Serial.begin(9600);
  uint8_t macAddress[] = {0xAA, 0xBB, 0xDE, 0xAF, 0xFE, 0x11};

  dhcp(macAddress);

  Serial.println(F("ready"));
}

void loop(void) {
  if (millis() > lastPostTime + POSTING_INTERVAL || millis() < lastPostTime) {
    get_controller();
    //get_google();
    lastPostTime = millis();
  }
  while (client.available()) {
    char c = client.read();
    Serial.write(c);
  }
}

void get_google() {
  client.stop();
  char server[] = "www.google.com";
  if (client.connect(server, 80)) {
    Serial.println("connected");
    client.println("GET /search?q=arduino HTTP/1.0");
    client.println();
  } else {
    Serial.println("connection failed");
  }
}

void get_controller() {
  Serial.print(F("post"));
  client.stop();

  if (client.connect(masterController, 9999)) {
    client.println("POST /echo/test HTTP/1.1");
    client.println("HOST: iot");
    client.println("Content-Length: 6"); // Can not be too little, add a few spaces at the end and take an conservative guess
    client.println(); //This line is mandatory (for a Jetty server at least)
    client.print("body  ");
    Serial.println(F("ed"));
  } else {
    Serial.println(F(" failed"));
    client.stop();
    Ethernet.maintain();
  }
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

