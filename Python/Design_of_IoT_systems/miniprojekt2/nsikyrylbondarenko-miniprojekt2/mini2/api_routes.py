from flask import Blueprint, request, jsonify, session
from functools import wraps
from models import db, Data

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
    if not data or 'temperature' not in data:
        return jsonify({'error': 'Chybí hodnota temperature'}), 400
    temperature = data['temperature']
    new_entry = Data(temperature=temperature)
    db.session.add(new_entry)
    db.session.commit()
    return jsonify({'message': 'Teplota byla přidána', 'id': new_entry.id}), 201

@bp.route('/last_temperature', methods=['GET'])
def last_temperature():
    last_entry = Data.query.order_by(Data.id.desc()).first()
    if last_entry:
        return jsonify({
            'id': last_entry.id,
            'temperature': last_entry.temperature,
            'timestamp': last_entry.timestamp.strftime("%Y-%m-%d %H:%M:%S")
        }), 200
    return jsonify({'error': 'Žádná data nenalezena'}), 404

@bp.route('/temperature/<int:id>', methods=['GET'])
def get_temperature(id):
    entry = Data.query.get(id)
    if entry:
        return jsonify({
            'id': entry.id,
            'temperature': entry.temperature,
            'timestamp': entry.timestamp.strftime("%Y-%m-%d %H:%M:%S")
        }), 200
    return jsonify({'error': 'Data nenalezena'}), 404

@bp.route('/delete_oldest', methods=['DELETE'])
@login_required_api
def delete_oldest():
    oldest = Data.query.order_by(Data.id.asc()).first()
    if oldest:
        db.session.delete(oldest)
        db.session.commit()
        return jsonify({'message': 'Nejstarší záznam byl smazán'}), 200
    return jsonify({'error': 'Žádná data k smazání'}), 404

@bp.route('/temperature/<int:id>', methods=['DELETE'])
@login_required_api
def delete_temperature(id):
    entry = Data.query.get(id)
    if entry:
        db.session.delete(entry)
        db.session.commit()
        return jsonify({'message': 'Záznam byl smazán'}), 200
    return jsonify({'error': 'Data nenalezena'}), 404

@bp.route('/all_temperatures', methods=['GET'])
def all_temperatures():
    sort = request.args.get('sort', 'asc')
    if sort == 'asc':
        entries = Data.query.order_by(Data.id.asc()).all()
    else:
        entries = Data.query.order_by(Data.id.desc()).all()
    data = [{
        'id': entry.id,
        'temperature': entry.temperature,
        'timestamp': entry.timestamp.strftime("%Y-%m-%d %H:%M:%S")
    } for entry in entries]
    return jsonify(data), 200
