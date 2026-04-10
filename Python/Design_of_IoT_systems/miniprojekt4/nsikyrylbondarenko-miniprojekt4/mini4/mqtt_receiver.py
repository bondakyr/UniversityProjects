import paho.mqtt.client as mqtt
import json
from datetime import datetime
from models import db, Data
from flask import Flask

# Inicializace Flask aplikace pro kontext databáze
app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///data.db'
db.init_app(app)

TOPIC = "nsi/data"
BROKER = "localhost"
PORT = 1884


def on_connect(client, userdata, flags, rc):
    print(f"Připojeno s kódem výsledku {rc}")
    client.subscribe(TOPIC)


def on_message(client, userdata, msg):
    try:
        payload = json.loads(msg.payload.decode())
        print(f"Zpráva přijata: {payload}")

        new_data = Data(
            temperature=payload['temperature'],
            timestamp_measurement=datetime.fromtimestamp(payload['timestamp_measurement']),
            timestamp_send = datetime.fromtimestamp(payload['timestamp_send']),
            timestamp_received=datetime.utcnow()
        )

        with app.app_context():
            db.session.add(new_data)
            db.session.commit()
            print("Data uložena do DB.")

    except Exception as e:
        print(f"Chyba při zpracování zprávy: {e}")


def main():
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message

    client.connect(BROKER, PORT, 60)
    client.loop_forever()


if __name__ == '__main__':
    main()
