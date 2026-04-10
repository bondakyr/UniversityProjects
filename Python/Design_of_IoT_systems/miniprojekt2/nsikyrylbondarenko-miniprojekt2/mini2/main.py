from flask import Flask, render_template, request, redirect, url_for, flash, session
from models import db, User, Data
from werkzeug.security import generate_password_hash, check_password_hash
from api_routes import bp as api_blueprint
from datetime import datetime

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///database.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.secret_key = 'tajny_klic'

db.init_app(app)
app.register_blueprint(api_blueprint, url_prefix='/api')

with app.app_context():
    db.create_all()

@app.route('/')
def index():
    if 'user_id' in session:
        return redirect(url_for('dashboard'))
    return redirect(url_for('login'))

@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']

        if User.query.filter_by(username=username).first():
            flash('Uživatel již existuje.')
            return redirect(url_for('register'))

        hashed_password = generate_password_hash(password)
        new_user = User(username=username, password_hash=hashed_password)
        db.session.add(new_user)
        db.session.commit()

        flash('Registrace byla úspěšná. Přihlaste se.')
        return redirect(url_for('login'))

    return render_template('register.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        user = User.query.filter_by(username=username).first()

        if user and check_password_hash(user.password_hash, password):
            session['user_id'] = user.id
            return redirect(url_for('dashboard'))
        else:
            flash('Neplatné přihlašovací údaje.')
            return redirect(url_for('login'))

    return render_template('login.html')

@app.route('/logout')
def logout():
    session.pop('user_id', None)
    return redirect(url_for('login'))

@app.route('/dashboard', methods=['GET', 'POST'])
def dashboard():
    if 'user_id' not in session:
        return redirect(url_for('login'))

    count = request.args.get('count', 15, type=int)
    sort = request.args.get('sort', 'asc')

    data_query = Data.query.order_by(Data.timestamp.asc() if sort == 'asc' else Data.timestamp.desc())
    data = data_query.all()

    if request.method == 'POST' and data:
        oldest = data_query.first()
        db.session.delete(oldest)
        db.session.commit()
        return redirect(url_for('dashboard', count=count, sort=sort))

    return render_template('dashboard.html',
        table_data=data[:count],
        last_value=data[-1] if data else None,
        data=data,
        count=count,
        sort=sort
    )

if __name__ == '__main__':
    app.run(debug=True)
