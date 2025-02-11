import paho.mqtt.client as mqtt
import json
import time
from threading import Thread, Lock
from dataclasses import dataclass
from pynput import keyboard
import socket
import traceback
import cv2
import numpy as np

# Configurazione simulata EV3
#EV3_NAME = "EV3A"
BROKER = "localhost"
PORT = 1883
#UDP_PORT = 11002
# Configuration
UDP_IP = "127.0.0.1"  # Localhost
SHOW = False

MAX_ERROR_NUMBER = 99999

mqtt_broker = BROKER
mqtt_port = PORT

        
# Mappatura tasti-colori
KEY_COLOR_MAP = {
    '1': 'red',
    '3': 'green',
    '4': 'blue',
    '5': 'yellow',
    '6': 'white'
}

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

SPEED_DOWNSCALE_FACTOR = 1000

@dataclass
class Motor:
    name: str
    type: str
    port: str
    speed: int = 0
    angle: int = 0

@dataclass
class Sensor:
    name: str
    port: str
    key: str
    active: bool = False
    value: str = "unknown"

class EV3Simulator:
    def __init__(self, UDP_PORT):
        self.client = mqtt.Client()
        self.motors = {}
        self.sensors = {}
        self.running = True
        self.key_lock = Lock()
        self.prev_color = ""
        self.UDP_PORT = UDP_PORT
        self.EV3_NAME = self.name_from_port(UDP_PORT)
        self.MOTOR_PORT_MAP = {
            "outA": self.UDP_PORT,
            "outB": self.UDP_PORT,
            "outC": self.UDP_PORT,
            "outD": self.UDP_PORT
        }
        
        self.SENSOR_PORT_MAP = {
            "inA": self.UDP_PORT + 1000,
            "inB": self.UDP_PORT + 2000,
            "inC": self.UDP_PORT + 3000,
            "inD": self.UDP_PORT + 4000
        }
        
        self.sensor_socks = {}
        for port in self.SENSOR_PORT_MAP.values():
            sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            sock.bind((UDP_IP, port))
            sock.settimeout(2)
            self.sensor_socks[port] = sock
            
        for sock in self.sensor_socks.values():
            Thread(target=self.sensor_loop, args=(sock,), daemon=True).start()
        
        # Inizializza sensori con tasti associati
        self.init_key_sensors()
        
        # Configura callback MQTT
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message

        # Listener tastiera
        #self.keyboard_listener = keyboard.Listener(
        #    on_press=self.on_key_press
        #)
        
    def name_from_port(self, port):
        color_numbers = {
            11002: "B",
            11003: "G",
            11004: "O",
            11005: "R",
            11006: "Y"
        }
        #f"EV3{str(UDP_PORT)[-1]}"
        return f"EV3{color_numbers.get(port, 'X')}"

    def init_key_sensors(self):
        """Inizializza sensori con mappatura tasti"""
        for key, color in KEY_COLOR_MAP.items():
            sensor_name = f"{self.EV3_NAME}{key}"
            self.sensors[sensor_name] = Sensor(
                name=sensor_name,
                port=f"in{key}",
                key=key,
                value="unknown"
            )

    def on_key_press(self, key):
        """Gestione pressione tasti con toggle"""
        try:
            key_char = key.char
            if key_char in KEY_COLOR_MAP:
                with self.key_lock:
                    sensor_name = f"color_sensor_{key_char}"
                    sensor = self.sensors.get(sensor_name)
                    
                    if sensor:
                        # Toggle stato del sensore
                        sensor.active = not sensor.active
                        sensor.value = KEY_COLOR_MAP[key_char] if sensor.active else "unknown"
                        
                        action = "ATTIVATO" if sensor.active else "DISATTIVATO"
                        print(f"Sensore {sensor.name} {action} - Valore: {sensor.value}")

        except AttributeError:
            pass

    def on_connect(self, client, userdata, flags, rc):
        print(f"Connesso al broker MQTT ({BROKER}:{PORT})")
        client.subscribe([
            ("ev3_config", 2),
            ("scan", 2),
            ("motor/action/#", 2),
            ("sensor/on_demand/question", 2),
            ("stop", 2)
        ])

    def on_message(self, client, userdata, msg):
        try:
            topic = msg.topic
            payload = json.loads(msg.payload.decode())

            if topic == "ev3_config":
                self.handle_config(payload)
            
            elif topic == "scan":
                self.send_scan_response()
            
            elif topic.startswith("motor/action"):
                self.handle_motor_command(topic, payload)
            
            elif topic == "sensor/on_demand/question":
                self.handle_sensor_question(payload)
            
            elif topic == "stop":
                self.handle_stop()

        except Exception as e:
            print(topic + " - " + str(payload))
            print(f"Errore: {str(e)}")
            traceback.print_exc()

    def handle_config(self, payload):
        print(f"Configurazione EV3: {payload}")
        # Simula configurazione motori/sensori
        for i in range(1, 9, 2):
            motor_name = payload.get(str(i))
            motor_type = payload.get(str(i+1))
            if motor_name and motor_type:
                port = f"out{chr(64 + (i//2 + 1))}"
                self.motors[motor_name] = Motor(
                    name=motor_name,
                    type=motor_type,
                    port=port
                )
        
        # Pubblica conferma configurazione
        self.client.publish(
            "ev3_configured",
            json.dumps({"name": self.EV3_NAME}),
            qos=2
        )

    def send_scan_response(self):
        self.client.publish(
            "hello",
            json.dumps({"name": self.EV3_NAME}),
            qos=2
        )

    def handle_motor_command(self, topic, payload):
        ev3 = payload.get("ev3", 0)
        if ev3 != self.EV3_NAME:
            return
        motor_name = payload.get("motor")
        action = topic.split("/")[-1]
        value = payload.get("value", 0)

        print(f"Comando motore {motor_name}: {action} - {value}")

        if action == "forever":
            #self.motors[motor_name].speed = value
            self.send_speed(self.MOTOR_PORT_MAP[motor_name], motor_name, int(value) / SPEED_DOWNSCALE_FACTOR)

        if action == "rel":
            #self.motors[motor_name].speed = value
            self.send_push(self.MOTOR_PORT_MAP[motor_name], motor_name, value)
        
        elif action == "abs":
            #self.motors[motor_name].angle = value.get("angle1", 0)
            time.sleep(2)
            #self.motors[motor_name].angle = value.get("angle2", 0)
        
        elif action == "stop":
            #self.motors[motor_name].speed = 0
            self.send_speed(self.MOTOR_PORT_MAP[motor_name], motor_name, 0)

    def handle_sensor_question(self, payload):
        sensor_name = payload.get("sensor")
        sensor = self.sensors.get(sensor_name)
        
        if sensor:
            self.client.publish(
                "sensor/on_demand/answer",
                json.dumps({
                    "ev3": self.EV3_NAME,
                    "sensor": sensor.name,
                    "value": sensor.value,
                    "ts": time.time()
                }),
                qos=2
            )

    def handle_stop(self):
        print("Ricevuto comando STOP")
        self.running = False
        self.client.disconnect()

    def run(self):
        self.client.connect(BROKER, PORT, 60)
        #self.keyboard_listener.start()
        #Thread(target=self.sensor_loop, daemon=True).start()
        self.client.loop_forever()
        
    # Function to send speed value
    def send_speed(self, port, motor, speed):
        try:
            # Create a UDP socket
            sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            
            # Convert the speed to a string and encode it
            speed = str(speed).replace(".", ",")
            message = {motor : speed}
            message = json.dumps(message).encode('utf-8')
            # Send the message to the Unity application
            sock.sendto(message, (UDP_IP, int(port)))
            print(f"Sent speed: {message} on port {port}")
            
            # Close the socket
            sock.close()
        except Exception as e:
            print(f"Error sending UDP message: {e}")

    def send_push(self, port, motor, value):
        try:
            if int(value) > 0:
                # Create a UDP socket
                sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
                
                # Convert the speed to a string and encode it
                message = {motor : "1"}
                message = json.dumps(message).encode('utf-8')
                # Send the message to the Unity application
                sock.sendto(message, (UDP_IP, int(self.MOTOR_PORT_MAP[motor])))
                print(f"Sent push: {message} on port {self.MOTOR_PORT_MAP[motor]}")
                
                # Close the socket
                sock.close()
        except Exception as e:
            print(f"Error sending UDP message: {e}")
            
    def detect_dominant_color(self, frame):
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

    def sensor_loop(self, sock):
        print(f"Sensor loop for ev3 {self.EV3_NAME} started on port {sock.getsockname()[1]}")
        previous_color = None
        sensor_window_name = f"{sock.getsockname()[1]} - {self.EV3_NAME}"
        #cv2.namedWindow(f"{sensor_window_name}", cv2.WINDOW_NORMAL)
        """Aggiornamento periodico dei sensori"""
        number_of_errors = 0
        while self.running:
            #with self.key_lock:
            try:
                data, addr = sock.recvfrom(65536)
                if data.startswith(b'time'):
                    timestring = data.decode()[5:]
                    print(f"Latency: {time.time() - float(timestring):.3f}s")
                else:
                    frame = cv2.imdecode(np.frombuffer(data, dtype=np.uint8), -1)
                    if frame is not None:
                        # Detect dominant color
                        current_color = self.detect_dominant_color(frame)
                        
                        if SHOW:
                            # Update display with color information
                            cv2.putText(frame, f"Dominant: {current_color}", (10, 30),
                                    cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
                            cv2.imshow(f"{sensor_window_name}", frame)
                        
                        # Publish on color change
                        if current_color != previous_color:
                            print(f"Detected color: {current_color}")
                            #form message like  {"ev3": "EV3", "sensor": "color_sensor_1", "value": "unknown", "ts": 1738626790.2187595}
                            message = json.dumps({
                                "ev3": self.EV3_NAME,
                                "sensor": f"in{str(sock.getsockname()[1] - 1000)[1]}",
                                "value": current_color,
                                "ts": time.time()
                            })
                            self.client.publish("sensor/on_change", message, qos=2)
                            previous_color = current_color

                    if cv2.waitKey(1) & 0xFF == ord('q'):
                        break

            except socket.error as e:
                #print(f"Error receiving frame: {e} on sensor {sock.getsockname()[1]}")
                number_of_errors += 1
                time.sleep(1)
            if number_of_errors > MAX_ERROR_NUMBER:
                print(f"Too many errors on sensor {sock.getsockname()[1]}, stopping sensor loop")
                break


if __name__ == "__main__":
    #simulator = EV3Simulator()
    #print("Simulatore EV3 avviato...")
    #simulator.run()
    #init simulators for ports 11002, 11003, 11004, 11005, each on separate threads
    simulators = []
    for i in range(2, 7):
        simulator = EV3Simulator(11000 + i)
        simulators.append(simulator)
        Thread(target=simulator.run, daemon=True).start()
        print(f"Simulatore EV3 {simulator.EV3_NAME} avviato...")
    print("Simulatori EV3 avviati...")
    while True:
        time.sleep(1)
        