import network
import time
import ujson
from umqtt.simple import MQTTClient
import ntptime

# Konfigurace
SSID = "iPhone 12 Pro"
PASSWORD = "88888888"

MQTT_BROKER = "test.mosquitto.org"
MQTT_PORT = 1883
MQTT_TOPIC = "pico/temperature"

def connect_wifi():
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    if not wlan.isconnected():
        print("Připojuji se k WiFi...")
        wlan.connect(SSID, PASSWORD)
        while not wlan.isconnected():
            time.sleep(1)
    print("WiFi připojeno, IP:", wlan.ifconfig()[0])
    return wlan

def connect_mqtt(client_id="pico_client"):
    client = MQTTClient(client_id, MQTT_BROKER, port=MQTT_PORT)
    client.connect()
    print("Připojeno k MQTT brokeru na", MQTT_BROKER, ":", MQTT_PORT)
    return client

# Simulace měření teploty – můžete nahradit reálným kódem pro čtení senzoru
def read_temperature():
    import random
    return round(20 + random.random() * 10, 2)

# Vrací aktuální UTC čas ve formátu ISO
def get_timestamp():
    tm = time.gmtime()  # UTC čas (ne localtime!)
    return "{:04d}-{:02d}-{:02d}T{:02d}:{:02d}:{:02d}".format(tm[0], tm[1], tm[2], tm[3], tm[4], tm[5])

def main():
    connect_wifi()
    try:
        ntptime.settime()
        print("Čas synchronizován pomocí NTP.")
    except:
        print("Nepodařilo se synchronizovat čas (NTP).")
    
    client = connect_mqtt()

    while True:
        temperature = read_temperature()
        measurement_timestamp = get_timestamp()  # Čas měření
        send_timestamp = get_timestamp()         # Čas odeslání

        payload = {
            "temperature": temperature,
            "measurement_timestamp": measurement_timestamp,
            "send_timestamp": send_timestamp
        }

        json_payload = ujson.dumps(payload)
        client.publish(MQTT_TOPIC, json_payload, qos=1)
        print("Odesláno:", json_payload)

        time.sleep(10)

if __name__ == "__main__":
    main()

