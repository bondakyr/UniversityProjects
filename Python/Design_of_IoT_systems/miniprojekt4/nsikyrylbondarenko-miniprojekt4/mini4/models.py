from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

db = SQLAlchemy()

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    password = db.Column(db.String(200), nullable=False)

    def __repr__(self):
        return f'<User {self.username}>'

class Data(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    temperature = db.Column(db.Float, nullable=False)
    timestamp_measurement = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    timestamp_send = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    timestamp_received = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    def __repr__(self):
        return f'<Data {self.temperature} °C @ {self.timestamp_measurement}>'
