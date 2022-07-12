#include <ESP32Servo.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

const char* ssid = "amirh";
const char* password = "#define x kh";

const char* mqtt_server = "45.149.77.235";
const int mqtt_port = 1883;
const char* mqtt_username = "97522292";
const char* mqtt_password = "kYLB2MaR";
const char* mqtt_client_name = "ESP32Client";
const char* mqtt_out_topic = "97522292/connected";
const char* mqtt_in_topic = "97522292/angles";

WiFiClient espClient;
PubSubClient client(espClient);

Servo servo1, servo2;
int servo1_pin = 18;
int servo2_pin = 23;
int min_us = 400, max_us = 2400; // Min/Max pulse width (microseconds) for MG90S servo

int angle1 = 0;
int angle2 = 0;


void callback(char* topic, byte* payload, unsigned int length) {
    Serial.print("Message arrived [");
    Serial.print(topic);
    Serial.print("] ");

    char json[length];
    for (int i = 0; i < length; ++i) {
        json[i] = (char)payload[i];
        Serial.print(json[i]);
    }
    Serial.println();

    StaticJsonDocument<200> doc;
    DeserializationError error = deserializeJson(doc, json);
    
    if (error) {
        Serial.print(F("deserializeJson() failed: "));
        Serial.println(error.f_str());
        return;
    }

    if (doc.containsKey("motor1") && doc.containsKey("motor2")) {
        angle1 = (int)doc["motor1"];
        angle2 = (int)doc["motor2"];
        
        Serial.print("New angles: ");
        Serial.print(angle1);
        Serial.print(" ");
        Serial.println(angle2);
    } else {
        Serial.println("The json does not contain \'motor1\' or \'motor2\'");
        return;
    }
}

void reconnect() {
    while (!client.connected()) {
        Serial.println("Attempting MQTT connection...");
        if (client.connect(mqtt_client_name, mqtt_username, mqtt_password)) {
            Serial.println("connected");
            client.publish(mqtt_out_topic, "esp32 is up.");

            client.subscribe(mqtt_in_topic);
        } else {
            Serial.print("failed, rc=");
            Serial.print(client.state());
            Serial.println(" try again in 5 seconds");
            // Wait 5 seconds before retrying
            delay(5000);
        }
    }
}

void setup() {
    Serial.begin(115200);
    
    // Allow allocation of all timers
    ESP32PWM::allocateTimer(0);
    ESP32PWM::allocateTimer(1);
    ESP32PWM::allocateTimer(2);
    ESP32PWM::allocateTimer(3);

    servo1.setPeriodHertz(50);    // standard 50 hz servo
    servo2.setPeriodHertz(50);

    servo1.attach(servo1_pin, min_us, max_us);
    servo2.attach(servo2_pin, min_us, max_us);


    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.println("Connecting to WiFi..");
    }
    Serial.println("Connected to the WiFi network");

    client.setServer(mqtt_server, mqtt_port);
    client.setCallback(callback);
}
 
void loop() {
    if (!client.connected()) {
        reconnect();
    }
    client.loop();
    
    servo1.write(angle1);
    delay(300);
    servo2.write(angle2);
}
