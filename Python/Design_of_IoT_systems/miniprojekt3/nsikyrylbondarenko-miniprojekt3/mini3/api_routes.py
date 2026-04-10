# api_routes.py
from flask import Blueprint, request, jsonify, session
from functools import wraps
from models import db, Data
from datetime import datetime

def login_required_api(func):
    @wraps(func)
    def decorated_function(*args, **kwargs):
        if not session.get("logged_in"):
            return jsonify({'error': 'Neautorizovaný přístup.'}), 401
        return func(*args, **kwargs)
    return decorated_function

bp = Blueprint('api', __name__, url_prefix='/api')

@bp.route('/temperature', methods=['POST'])
def add_temperature():
    data = request.get_json()
    if not data:
        return jsonify({'error': 'Chybí data.'}), 400
    if 'temperature' not in data or 'measurement_timestamp' not in data or 'send_timestamp' not in data:
        return jsonify({'error': 'Chybí jedna z povinných položek: temperature, measurement_timestamp, send_timestamp'}), 400

    try:
        temperature = float(data['temperature'])
        measurement_timestamp = datetime.fromisoformat(data['measurement_timestamp'])
        send_timestamp = datetime.fromisoformat(data['send_timestamp'])
    except Exception as e:
        return jsonify({'error': 'Chyba při zpracování dat: ' + str(e)}), 400

    new_data = Data(
        temperature=temperature,
        measurement_timestamp=measurement_timestamp,
        send_timestamp=send_timestamp
    )
    db.session.add(new_data)
    db.session.commit()

    return jsonify({'message': 'Data byla přidána', 'id': new_data.id}), 201
