from flask import Flask, flash, request, jsonify, render_template, redirect, url_for, session
from werkzeug.security import generate_password_hash, check_password_hash
from models import db, Data, User
import paho.mqtt.publish as publish
import json

def create_app():
    app = Flask(__name__)
    app.secret_key = 'your_secret_key'
    app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///data.db'
    db.init_app(app)

    with app.app_context():
        db.create_all()

    register_routes(app)
    return app

def register_routes(app):
    @app.route('/')
    def dashboard():
        count = request.args.get('count', 15, type=int)
        data_values = Data.query.order_by(Data.timestamp_measurement.desc()).limit(count).all()
        data_values = list(reversed(data_values))
        last_value = Data.query.order_by(Data.timestamp_measurement.desc()).first()

        chart_labels = [d.timestamp_measurement.strftime('%Y-%m-%d %H:%M:%S') for d in data_values]
        chart_temps = [d.temperature for d in data_values]

        return render_template('dashboard.html',
                               displayed_data=data_values,
                               last_value=last_value,
                               count=count,
                               chart_labels=chart_labels,
                               chart_temps=chart_temps)

    @app.route('/register', methods=['GET', 'POST'])
    def register():
        if request.method == 'POST':
            username = request.form['username']
            raw_password = request.form['password']

            if User.query.filter_by(username=username).first():
                flash('Uživatel již existuje.', 'danger')
                return redirect(url_for('register'))

            password = generate_password_hash(raw_password)
            new_user = User(username=username, password=password)
            db.session.add(new_user)
            db.session.commit()
            flash('Registrace úspěšná. Přihlaste se.', 'success')
            return redirect(url_for('login'))

        return render_template('register.html')

    @app.route('/login', methods=['GET', 'POST'])
    def login():
        if request.method == 'POST':
            username = request.form['username']
            password = request.form['password']
            user = User.query.filter_by(username=username).first()

            if user and check_password_hash(user.password, password):
                session['user_id'] = user.id
                session['username'] = user.username
                flash('Úspěšně přihlášeno.', 'success')
                return redirect(url_for('dashboard'))

            flash('Nesprávné uživatelské jméno nebo heslo.', 'danger')
        return render_template('login.html')

    @app.route('/logout')
    def logout():
        session.clear()
        flash('Byl jste úspěšně odhlášen.', 'success')
        return redirect(url_for('login'))

    @app.route('/delete_oldest', methods=['POST'])
    def delete_oldest():
        is_ajax = request.headers.get("X-Requested-With") == "XMLHttpRequest"

        if 'user_id' not in session:
            if is_ajax:
                return jsonify({"success": False, "message": "Nejste přihlášen."}), 401
            flash('Neautorizovaný přístup.', 'danger')
            return redirect(url_for('login'))

        count = request.form.get('count', 15, type=int)
        if count <= 0:
            count = 15

        oldest_data = Data.query.order_by(Data.timestamp_measurement.asc()).first()
        if not oldest_data:
            message = "Žádná data nenalezena."
            if is_ajax:
                return jsonify({"success": False, "message": message}), 404
            flash(message, "warning")
            return redirect(url_for('dashboard', count=count))

        db.session.delete(oldest_data)
        db.session.commit()

        message = "Nejstarší hodnota byla odstraněna."
        displayed_data = Data.query.order_by(Data.timestamp_measurement.desc()).limit(count).all()
        displayed_data = list(reversed(displayed_data))
        last_value = Data.query.order_by(Data.timestamp_measurement.desc()).first()

        if is_ajax:
            return jsonify({
                "success": True,
                "message": message,
                "displayed_data": [
                    {
                        "id": d.id,
                        "temperature": d.temperature,
                        "timestamp_measurement": d.timestamp_measurement.strftime('%Y-%m-%d %H:%M:%S'),
                        "timestamp_send": d.timestamp_send.strftime('%Y-%m-%d %H:%M:%S'),
                        "timestamp_received": d.timestamp_received.strftime('%Y-%m-%d %H:%M:%S'),
                    }
                    for d in displayed_data
                ],
                "last_value": {
                    "temperature": last_value.temperature,
                    "timestamp_measurement": last_value.timestamp_measurement.strftime('%Y-%m-%d %H:%M:%S'),
                } if last_value else None
            })

        flash(message, "success")
        return redirect(url_for('dashboard', count=count))

    @app.route('/send_command', methods=['POST'])
    def send_command():
        if not request.is_json:
            return jsonify({"success": False, "message": "Request must be JSON."}), 400

        try:
            payload = request.get_json()
            allowed_keys = {"led", "measure", "period"}
            if not any(k in payload for k in allowed_keys):
                return jsonify({"success": False, "message": "Missing valid command key."}), 400

            publish.single(
                topic="nsi/cmd",
                payload=json.dumps(payload),
                hostname="localhost",
                port=1884
            )
            return jsonify({"success": True, "message": "Command sent."}), 200

        except Exception as e:
            return jsonify({"success": False, "message": str(e)}), 500

app = create_app()

if __name__ == '__main__':
    from api_route import api
    app.register_blueprint(api, url_prefix='/api')
    app.run(host='0.0.0.0', port=80, debug=True, use_reloader=True)
