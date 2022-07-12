#include <ESP32Servo.h>
#include <WebServer.h>

Servo servo1, servo2;
int servo1_pin = 18;
int servo2_pin = 23;
int min_us = 400, max_us = 2400; // Min/Max pulse width (microseconds) for MG90S servo

int angle1 = 0;
int angle2 = 0;

WebServer server(80);

void handle_root() {
    server.send(200, "text/plain", "The telescope is ready");
}

void handle_get() {
    if (!server.hasArg("motor1") or !server.hasArg("motor2"))
        server.send(400, "text/plain", "Bad Request");
    
    angle1 = server.arg("motor1").toInt();
    angle2 = server.arg("motor2").toInt();
    
    Serial.print("New angles: ");
    Serial.print(angle1);
    Serial.print(" ");
    Serial.println(angle2);

    server.send(200, "text/plain", "Data Received");
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


    WiFi.softAP("ESP32");
    server.on("/", handle_root);
    server.on("/get", HTTP_GET, handle_get);
    server.begin();
}

void loop() {
    server.handleClient();
    servo1.write(angle1);
    delay(300);
    servo2.write(angle2);
}
