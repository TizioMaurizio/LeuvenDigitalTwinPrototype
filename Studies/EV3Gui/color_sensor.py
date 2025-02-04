import cv2
import socket
import struct
import numpy as np
import time
import paho.mqtt.client as mqtt
import json

# MQTT setup
mqtt_broker = "localhost"
mqtt_port = 1883
client = mqtt.Client()
client.connect(mqtt_broker, mqtt_port, 60)
client.loop_start()

# UDP setup
UDP_IP = '127.0.0.1'
UDP_PORT = 25668
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind((UDP_IP, UDP_PORT))
sock.settimeout(2)

COLOR_KEY_MAP = {
    'red': '1',
    'green': '3',
    'blue': '4',
    'yellow': '5',
    'white': '6'
}


# OpenCV window setup
cv2.namedWindow("Received Stream", cv2.WINDOW_NORMAL)

# Color detection parameters
color_ranges = {
    'unknown': [(np.array([0, 0, 0]), np.array([180, 255, 50]))],  # Very dark colors
    'red': [
        (np.array([0, 120, 150]), np.array([10, 255, 255])),      # Bright reds
        (np.array([160, 120, 150]), np.array([180, 255, 255]))    # Deep reds
    ],
    'green': [(np.array([40, 100, 100]), np.array([80, 255, 255]))],
    'blue': [(np.array([100, 100, 100]), np.array([140, 255, 255]))],
    'yellow': [(np.array([20, 100, 100]), np.array([40, 255, 255]))],
    'white': [(np.array([0, 0, 200]), np.array([180, 30, 255]))]
}

previous_color = None

def detect_dominant_color(frame):
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    max_area = 0
    dominant_color = 'unknown'

    color_areas = {}
    
    for color_name, ranges in color_ranges.items():
        mask = np.zeros(hsv.shape[:2], dtype=np.uint8)
        for lower, upper in ranges:
            mask = cv2.bitwise_or(mask, cv2.inRange(hsv, lower, upper))
        area = cv2.countNonZero(mask)
        color_areas[color_name] = area

    dominant_color = max(color_areas, key=color_areas.get)
    return dominant_color

while True:
    try:
        data, addr = sock.recvfrom(65536)
        if data.startswith(b'time'):
            timestring = data.decode()[5:]
            print(f"Latency: {time.time() - float(timestring):.3f}s")
        else:
            frame = cv2.imdecode(np.frombuffer(data, dtype=np.uint8), -1)
            if frame is not None:
                # Detect dominant color
                current_color = detect_dominant_color(frame)
                
                # Update display with color information
                cv2.putText(frame, f"Dominant: {current_color}", (10, 30),
                           cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
                cv2.imshow("Received Stream", frame)
                
                # Publish on color change
                if current_color != previous_color:
                    print(f"Detected color: {current_color}")
                    #form message like  {"ev3": "EV3", "sensor": "color_sensor_1", "value": "unknown", "ts": 1738626790.2187595}
                    message = json.dumps({
                        "ev3": "EV3",
                        "sensor": "color_sensor_1",
                        "value": current_color,
                        "ts": time.time()
                    })
                    client.publish("sensor/on_change", message, qos=2)
                    previous_color = current_color

            if cv2.waitKey(1) & 0xFF == ord('q'):
                break

    except socket.error as e:
        print(f"Error receiving frame: {e}")
        time.sleep(1)

# Cleanup
sock.close()
cv2.destroyAllWindows()
client.disconnect()
client.loop_stop()