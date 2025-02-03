import paho.mqtt.client as mqtt

# Define the MQTT broker details
broker = "broker.hivemq.com"  # Replace with your broker address
port = 1883
topic = "#"  # Use the '#' wildcard to listen to all topics

# Define the callback for when a message is received
def on_message(client, userdata, message):
    print(f"Received message: {message.payload.decode()} on topic {message.topic}")

# Create an MQTT client instance
client = mqtt.Client()

# Assign the on_message callback
client.on_message = on_message

# Connect to the MQTT broker
client.connect(broker, port, 60)

# Subscribe to the wildcard topic '#'
client.subscribe(topic)

# Start the loop to process network traffic and dispatch callbacks
print(f"Listening to all topics on {broker}...")
client.loop_forever()