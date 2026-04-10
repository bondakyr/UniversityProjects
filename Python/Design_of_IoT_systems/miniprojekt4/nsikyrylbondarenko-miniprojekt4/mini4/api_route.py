from flask import Blueprint, jsonify, request
from models import Data, db
from datetime import datetime

api = Blueprint('api', __name__)

@api.route('/data', methods=['GET'])
def get_all_data():
    data = Data.query.order_by(Data.timestamp_measurement.desc()).all()
    return jsonify([
        {
            'id': d.id,
            'temperature': d.temperature,
            'timestamp_measurement': d.timestamp_measurement.isoformat(),
            'timestamp_send': d.timestamp_send.isoformat(),
            'timestamp_received': d.timestamp_received.isoformat()
        }
        for d in data
    ])

@api.route('/data/<int:data_id>', methods=['GET'])
def get_data_by_id(data_id):
    data = Data.query.get_or_404(data_id)
    return jsonify({
        'id': data.id,
        'temperature': data.temperature,
        'timestamp_measurement': data.timestamp_measurement.isoformat(),
        'timestamp_send': data.timestamp_send.isoformat(),
        'timestamp_received': data.timestamp_received.isoformat()
    })

@api.route('/data', methods=['POST'])
def create_data():
    payload = request.get_json()
    try:
        new_data = Data(
            temperature=payload['temperature'],
            timestamp_measurement=datetime.fromtimestamp(payload['timestamp_measurement']),
            timestamp_send=datetime.fromtimestamp(payload['timestamp_send']),
            timestamp_received = datetime.fromtimestamp(payload['timestamp_received'])
        )
        db.session.add(new_data)
        db.session.commit()
        return jsonify({'success': True, 'id': new_data.id}), 201
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 400

@api.route('/data/<int:data_id>', methods=['DELETE'])
def delete_data(data_id):
    data = Data.query.get_or_404(data_id)
    db.session.delete(data)
    db.session.commit()
    return jsonify({'success': True, 'message': f'Data with ID {data_id} deleted.'})
