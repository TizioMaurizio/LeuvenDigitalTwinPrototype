import paho.mqtt.client as mqtt
import json
import time
from enum import Enum
import keyboard
import pandas as pd
import openpyxl

#create or clear excel file "system_log.xlsx"
pd.DataFrame(columns=["ts", "ev3", "sensor", "value"]).to_excel("system_log.xlsx", index=False)

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
    if data.get("ev3") == color_ev3 and data.get("sensor") == sensor and (data.get("value") == value or (value == "any" and data.get("value") != "unknown" and data.get("value") != "yellow")):
        speed_action(push_motor, push_ev3, "0")
        return True
    
machines = {
    "outB": [0,1],
    "outC": [0,1],
    "outD": [0,1]
}
    
WORK_TIME = 3
    
def work_pallets():
    global machines
    #for each machine check if more than one second passed, if yes set value to time.time() and send speed once
    for key, value in machines.items():
        if time.time() - value[0] > WORK_TIME and value[1] == 0:
            speed_action(key, "EV3G", "1000")
            value[1] = 1
            machines[key][0] = time.time()
            print(f"Machine on converyone {key} finished working")
    
log_df = []    
def log(message):
    global log_df
    if message.get("value") != "unknown" and message.get("value") != "yellow" and message.get("sensor"):
        #convert message "ts": 1739314348.9529247 to datetime
        message["ts"] = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(message.get("ts")))
        log_df.append(message)

# Callback when a message is received
def on_message(client, userdata, message):
    global machines
    
    print(f"Topic: {message.topic} | Message: {message.payload.decode()}")
    log(json.loads(message.payload.decode()))
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
    if check_stop_color(data, "EV3Y", "in1", "any", "EV3G", "outB"):
        print(f"Machine on conveyor outB is working")
        machines["outB"][0] = time.time()
        machines["outB"][1] = 0
    if check_stop_color(data, "EV3Y", "in2", "any", "EV3G", "outC"):
        print(f"Machine on conveyor outC is working")  
        machines["outC"][0] = time.time()
        machines["outC"][1] = 0
    if check_stop_color(data, "EV3Y", "in3", "any", "EV3G", "outD"):
        print(f"Machine on conveyor outD is working")
        machines["outD"][0] = time.time()
        machines["outD"][1] = 0
        
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

log_interval = 50
current_log = 0

while True:
    time.sleep(0.1)
    #if keyboard pressed, produce pallet
    if keyboard.is_pressed('1'):
        produce_pallet(Color.RED)
    if keyboard.is_pressed('2'):
        produce_pallet(Color.WHITE)
    if keyboard.is_pressed('3'):
        produce_pallet(Color.BLUE)
        
    if keyboard.is_pressed('p'):
        speed_action("outA", "EV3B", "0")
    if keyboard.is_pressed('o'):
        speed_action("outA", "EV3B", "1000")
        
    current_log += 1
    if current_log >= log_interval:
        try:
            if len(log_df) == 0:
                pass
            else:
                print(f"Logging {len(log_df)} messages to excel file")
                excel_df = pd.read_excel("system_log.xlsx")
                log_df_pandas = pd.DataFrame(log_df)
                log_df_pandas = pd.concat([excel_df, log_df_pandas])
                log_df_pandas.to_excel("system_log.xlsx", index=False)
                log_df = []
            current_log = 0
        except Exception as e:
            print(f"Error logging to excel file: {e}")
        
    work_pallets()