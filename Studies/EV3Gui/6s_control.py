import paho.mqtt.client as mqtt
import json
import time
from enum import Enum
import keyboard

# Define MQTT broker details
BROKER = "localhost"
PORT = 1883
TOPIC = "#"

#enum RED, WHITE, BLUE
class Color(Enum):
    RED = "RED"
    WHITE = "WHITE"
    BLUE = "BLUE"

def push_action(motor, ev3, value):
    message = json.dumps({
        "motor": motor,
        "ev3": ev3,
        "value": value
    })
    print(f"Sending message: {message}")
    client.publish("motor/action/rel", message, qos=2)

def speed_action(motor, ev3, value):
    message = json.dumps({
        "motor": motor,
        "ev3": ev3,
        "value": value
    })
    print(f"Sending message: {message}")
    client.publish("motor/action/forever", message, qos=2)

def produce_pallet(color):
    print(f"Producing {color} pallet")
    if color == Color.BLUE:
        push_action("outC", "EV3Y", "1")
    elif color == Color.WHITE:
        push_action("outB", "EV3Y", "1")
    elif color == Color.RED:
        push_action("outA", "EV3Y", "1")

def check_push_color(data, color_ev3, sensor, value, push_ev3, push_motor):
    #if sensor == "in3" and color_ev3 == "EV3G" and value == "red":
    #    push_action(push_motor, push_ev3, "1")
    #if sensor == "in2" and color_ev3 == "EV3G" and value == "white":
    #    push_action(push_motor, push_ev3, "1")
    #if sensor == "in1" and color_ev3 == "EV3G" and value == "blue":
    #    push_action(push_motor, push_ev3, "1")
    if data.get("ev3") == color_ev3 and data.get("sensor") == sensor and data.get("value") == value:
        push_action(push_motor, push_ev3, "1")
        return True
        
def check_stop_color(data, color_ev3, sensor, value, push_ev3, push_motor):
    if data.get("ev3") == color_ev3 and data.get("sensor") == sensor and (data.get("value") == value or value == "any"):
        speed_action(push_motor, push_ev3, "0")
        return True
    


# Callback when a message is received
def on_message(client, userdata, message):
    print(f"Topic: {message.topic} | Message: {message.payload.decode()}")
    #parse json
    data = json.loads(message.payload.decode())
    
    # push colors into machines
    check_push_color(data, "EV3G", "in3", "red", "EV3O", "outA")
    check_push_color(data, "EV3G", "in2", "white", "EV3O", "outB")
    check_push_color(data, "EV3G", "in1", "blue", "EV3O", "outC")
    
    # reload colors into buffer
    check_push_color(data, "EV3R", "in1", "white", "EV3R", "outA")
    check_push_color(data, "EV3R", "in2", "red", "EV3R", "outB")
    check_push_color(data, "EV3R", "in3", "blue", "EV3R", "outC")
    
    # stop machine conveyors to work piece
    check_stop_color(data, "EV3Y", "in1", "any", "EV3G", "outB")
    check_stop_color(data, "EV3Y", "in2", "any", "EV3G", "outC")
    check_stop_color(data, "EV3Y", "in3", "any", "EV3G", "outD")
        
# Callback when connected to the broker
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("Connected to MQTT Broker!")
        client.subscribe(TOPIC)  # Subscribe to all topics
    else:
        print(f"Failed to connect, return code {rc}")

# Initialize MQTT client
client = mqtt.Client()

# Assign callbacks
client.on_connect = on_connect
client.on_message = on_message

# Connect to broker
client.connect(BROKER, PORT, 60)

# Start MQTT loop on a separate thread
#client.loop_forever()
client.loop_start()

while True:
    time.sleep(0.1)
    #if keyboard pressed, produce pallet
    if keyboard.is_pressed('1'):
        produce_pallet(Color.RED)
    if keyboard.is_pressed('2'):
        produce_pallet(Color.WHITE)
    if keyboard.is_pressed('3'):
        produce_pallet(Color.BLUE)
        
    if keyboard.is_pressed('q'):
        speed_action("outA", "EV3B", "0")
    if keyboard.is_pressed('w'):
        speed_action("outA", "EV3B", "1000")