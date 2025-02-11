import paho.mqtt.client as mqtt

# Define MQTT broker details
BROKER = "localhost"
PORT = 1883
TOPIC = "#"

# Callback when a message is received
def on_message(client, userdata, message):
    print(f"Topic: {message.topic} | Message: {message.payload.decode()}")

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

# Start MQTT loop
client.loop_forever()
