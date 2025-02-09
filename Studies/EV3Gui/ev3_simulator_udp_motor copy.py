import paho.mqtt.client as mqtt
import json
import time
from threading import Thread, Lock
from dataclasses import dataclass
from pynput import keyboard
import socket
import traceback

# Configurazione simulata EV3
#EV3_NAME = "EV3A"
BROKER = "localhost"
PORT = 1883
#UDP_PORT = 11002
# Configuration
UDP_IP = "127.0.0.1"  # Localhost


        
# Mappatura tasti-colori
KEY_COLOR_MAP = {
    '1': 'red',
    '3': 'green',
    '4': 'blue',
    '5': 'yellow',
    '6': 'white'
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
        self.EV3_NAME = f"EV3{str(UDP_PORT)[-1]}"
        self.MOTOR_PORT_MAP = {
            "outA": self.UDP_PORT,
            "outB": self.UDP_PORT,
            "outC": self.UDP_PORT,
            "outD": self.UDP_PORT
        }

        # Inizializza sensori con tasti associati
        self.init_key_sensors()
        
        # Configura callback MQTT
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message

        # Listener tastiera
        #self.keyboard_listener = keyboard.Listener(
        #    on_press=self.on_key_press
        #)

    def init_key_sensors(self):
        """Inizializza sensori con mappatura tasti"""
        for key, color in KEY_COLOR_MAP.items():
            sensor_name = f"color_sensor_{key}"
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
            self.send_speed(self.MOTOR_PORT_MAP[motor_name], motor_name, value / SPEED_DOWNSCALE_FACTOR)

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

    def sensor_loop(self):
        """Aggiornamento periodico dei sensori"""
        while self.running:
            with self.key_lock:
                current_color = "unknown"
                #check if one sensor value is != "unknown"
                for sensor in self.sensors.values():
                    if sensor.active:
                        if sensor.value != "unknown":
                            current_color = sensor.value
                for sensor in self.sensors.values():
                    #if value is not unknown, publish the value
                    #if sensor.active:
                        if self.prev_color != current_color:
                            
                            message = json.dumps({
                                    "ev3": self.EV3_NAME,
                                    "sensor": sensor.name,
                                    "value": current_color,
                                    "ts": time.time()
                                })
                            print(f"Invio messaggio: {message}")
                            self.client.publish(
                                "sensor/on_change",
                                message,
                                qos=2
                            )
                            self.prev_color = current_color
            time.sleep(0.1)

    def run(self):
        self.client.connect(BROKER, PORT, 60)
        #self.keyboard_listener.start()
        Thread(target=self.sensor_loop, daemon=True).start()
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

if __name__ == "__main__":
    #simulator = EV3Simulator()
    #print("Simulatore EV3 avviato...")
    #simulator.run()
    #init simulators for ports 11002, 11003, 11004, 11005, each on separate threads
    simulators = []
    for i in range(2, 6):
        simulator = EV3Simulator(11000 + i)
        simulators.append(simulator)
        Thread(target=simulator.run, daemon=True).start()
        print(f"Simulatore EV3 {simulator.EV3_NAME} avviato...")
    print("Simulatori EV3 avviati...")
    while True:
        time.sleep(1)
        