import network
import time
import ubinascii
import machine
import random
import json
from umqtt.simple import MQTTClient

# Konfigurace
WIFI_SSID = "iPhonee"
WIFI_PASSWORD = "01020304"
MQTT_BROKER = "172.20.10.13"
MQTT_PORT = 1884
TOPIC_DATA = b"nsi/data"
TOPIC_CMD = b"nsi/cmd"
TOPIC_STATUS = b"nsi/status"
SLEEP_INTERVAL = 10

# Inicializace LED a stavu
led = machine.Pin("LED", machine.Pin.OUT)
measuring = True


def connect_wifi() -> bool:
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    if not wlan.isconnected():
        print("Připojování k Wi-Fi")
        wlan.connect(WIFI_SSID, WIFI_PASSWORD)
        timeout = 10
        while not wlan.isconnected() and timeout > 0:
            time.sleep(1)
            timeout -= 1

    if wlan.isconnected():
        print("Wi-Fi připojeno:", wlan.ifconfig())
        return True
    else:
        print("Nepodařilo se připojit k Wi-Fi")
        return False


def handle_command(topic, msg):
    global measuring, SLEEP_INTERVAL
    try:
        payload = json.loads(msg)
        if "led" in payload:
            if payload["led"] == "on":
                led.on()
            elif payload["led"] == "off":
                led.off()

        if "measure" in payload:
            measuring = (payload["measure"] == "start")

        if "period" in payload:
            SLEEP_INTERVAL = int(payload["period"])

        print("Přijatý příkaz:", payload)
    except Exception as e:
        print("Chyba při zpracování příkazu:", e)


def run():
    client_id = ubinascii.hexlify(machine.unique_id())
    client = MQTTClient(client_id, MQTT_BROKER, port=MQTT_PORT)
    client.set_callback(handle_command)
    client.connect()
    client.subscribe(TOPIC_CMD)
    print("MQTT připojeno. Poslouchám příkazy.")

    while True:
        client.check_msg()

        if measuring:
            temperature = round(random.uniform(20.0, 30.0), 2)
            timestamp = time.time()
            payload = {
                "temperature": temperature,
                "timestamp_measurement": timestamp,
                "timestamp_send": timestamp
            }
            client.publish(TOPIC_DATA, json.dumps(payload))
            print("Odesláno:", payload)

        # Jemněší časová prodleva a průběžné zpracování zpráv
        for _ in range(SLEEP_INTERVAL * 10):
            time.sleep(0.1)
            client.check_msg()


# Start
if __name__ == '__main__':
    time.sleep(5)
    if connect_wifi():
        run()


