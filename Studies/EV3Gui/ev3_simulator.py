import paho.mqtt.client as mqtt
import json
import time
from threading import Thread
from dataclasses import dataclass

# Configurazione simulata EV3
EV3_NAME = "ev3_simulator"
BROKER = "localhost"
PORT = 1883

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
    value: str = "unknown"

class EV3Simulator:
    def __init__(self):
        self.client = mqtt.Client()
        self.motors = {}
        self.sensors = {}
        self.running = True

        # Configura callback MQTT
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message

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
            print(f"Errore: {str(e)}")

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
            json.dumps({"name": EV3_NAME}),
            qos=2
        )

    def send_scan_response(self):
        self.client.publish(
            "hello",
            json.dumps({"name": EV3_NAME}),
            qos=2
        )

    def handle_motor_command(self, topic, payload):
        motor_name = payload.get("motor")
        action = topic.split("/")[-1]
        values = payload.get("value", {})

        print(f"Comando motore {motor_name}: {action} - {values}")

        if action == "forever":
            self.motors[motor_name].speed = values.get("speed", 0)
        
        elif action == "abs":
            self.motors[motor_name].angle = values.get("angle1", 0)
            time.sleep(2)
            self.motors[motor_name].angle = values.get("angle2", 0)
        
        elif action == "stop":
            self.motors[motor_name].speed = 0

    def handle_sensor_question(self, payload):
        sensor_name = payload.get("sensor")
        sensor_value = self.sensors.get(sensor_name, Sensor("unknown", "inX")).value
        
        self.client.publish(
            "sensor/on_demand/answer",
            json.dumps({
                "ev3": EV3_NAME,
                "sensor": sensor_name,
                "value": sensor_value,
                "ts": time.time()
            }),
            qos=2
        )

    def handle_stop(self):
        print("Ricevuto comando STOP")
        self.running = False
        self.client.disconnect()

    def sensor_loop(self):
        """Simula l'aggiornamento periodico dei sensori"""
        while self.running:
            for sensor in self.sensors.values():
                self.client.publish(
                    "sensor/on_change",
                    json.dumps({
                        "ev3": EV3_NAME,
                        "sensor": sensor.name,
                        "value": sensor.value,
                        "ts": time.time()
                    }),
                    qos=2
                )
            time.sleep(0.1)

    def run(self):
        self.client.connect(BROKER, PORT, 60)
        Thread(target=self.sensor_loop, daemon=True).start()
        self.client.loop_forever()

if __name__ == "__main__":
    simulator = EV3Simulator()
    print("Simulatore EV3 avviato...")
    simulator.run()