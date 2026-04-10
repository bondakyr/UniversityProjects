from flask import Flask, render_template, request, redirect, url_for, flash, session
from werkzeug.security import generate_password_hash, check_password_hash
from models import db, User, Data
from api_routes import bp as api_blueprint
from paho.mqtt.client import Client
from datetime import datetime
from threading import Thread
import json
import os

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///database.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.secret_key = 'tajny_klic'

db.init_app(app)
with app.app_context():
    db.create_all()

app.register_blueprint(api_blueprint)

# MQTT zpracování zpráv
def mqtt_callback(client, userdata, message):
    try:
        content = json.loads(message.payload.decode())

        temperature = content.get("temperature")
        measured = content.get("measurement_timestamp")
        sent = content.get("send_timestamp")
        received = datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S")

        new_record = Data(
            temperature=temperature,
            measurement_timestamp=measured,
            send_timestamp=sent,
            server_timestamp=received
        )

        with app.app_context():
            db.session.add(new_record)
            db.session.commit()

        print(f"✅ Uloženo: {temperature} °C, měřeno: {measured}, přijato: {received}")

    except Exception as e:
        print("❌ Chyba při zpracování JSON zprávy:", e)



# Spuštění MQTT klienta

import sys

def run_mqtt():
    client = Client()
    client.on_message = mqtt_callback
    client.connect("test.mosquitto.org")
    client.subscribe("pico/temperature", qos=1)
    try:
        print("📡 MQTT klient spuštěn")
        client.loop_forever()
    except (OSError, KeyboardInterrupt) as e:
        print("🛑 MQTT ukončeno:", e)
        sys.exit()


@app.route('/')
def index():
    return redirect(url_for('dashboard'))

@app.route('/dashboard', methods=['GET', 'POST'])
def dashboard():
    if request.method == 'POST':
        record = Data.query.order_by(Data.id.asc()).first()
        if record:
            db.session.delete(record)
            db.session.commit()
            flash("Nejstarší záznam odstraněn.")
        return redirect(url_for('dashboard'))

    try:
        count = int(request.args.get('count', 15))
        if count <= 0:
            raise ValueError
    except ValueError:
        count = 15
        flash("Neplatný počet, použit výchozí.")

    latest = Data.query.order_by(Data.id.desc()).limit(count).all()
    table_data = list(reversed(latest))
    all_data = Data.query.order_by(Data.id.asc()).all()

    return render_template('dashboard.html', table_data=table_data, count=count, data=all_data)

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        user_input = request.form['username']
        user_pass = request.form['password']
        user = User.query.filter_by(username=user_input).first()

        if user and check_password_hash(user.password_hash, user_pass):
            session['user_id'] = user.id
            session['logged_in'] = True
            return redirect(url_for('dashboard'))
        flash("Neplatné přihlašovací údaje.")

    return render_template('login.html')

@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        name = request.form['username']
        pw = request.form['password']
        if User.query.filter_by(username=name).first():
            flash("Uživatel již existuje.")
            return redirect(url_for('register'))

        hashed_pw = generate_password_hash(pw)
        new_user = User(username=name, password_hash=hashed_pw)
        db.session.add(new_user)
        db.session.commit()
        flash("Registrace úspěšná, přihlaste se.")
        return redirect(url_for('login'))

    return render_template('register.html')

@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('login'))


if __name__ == '__main__':
    if os.environ.get("WERKZEUG_RUN_MAIN") == "true":
        try:
            Thread(target=run_mqtt, daemon=True).start()
        except Exception as e:
            print("⚠️ Chyba při spouštění MQTT vlákna:", e)

    app.run(debug=True)

