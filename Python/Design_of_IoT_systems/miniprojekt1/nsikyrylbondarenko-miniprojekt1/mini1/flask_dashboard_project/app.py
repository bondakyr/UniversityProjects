from flask import Flask, render_template, redirect, url_for, request, session, flash
from datetime import datetime
import re

app = Flask(__name__)
app.secret_key = 'tajny_klic'

DATA = [
    {"id": i, "temperature": temp[0], "timestamp": temp[1]} for i, temp in enumerate([
        (21, "2025-01-01 01:00:00"), (33, "2025-01-01 01:05:52"), (38, "2025-01-01 01:10:23"),
        (21, "2025-01-01 01:15:17"), (24, "2025-01-01 01:20:08"), (35, "2025-01-01 01:25:42"),
        (40, "2025-01-01 01:30:00"), (37, "2025-01-01 01:35:19"), (37, "2025-01-01 01:40:25"),
        (20, "2025-01-01 01:45:53"), (35, "2025-01-01 01:50:15"), (34, "2025-01-01 01:55:19"),
        (28, "2025-01-01 02:00:46"), (23, "2025-01-01 02:05:55"), (25, "2025-01-01 02:10:20"),
        (23, "2025-01-01 02:15:43"), (23, "2025-01-01 02:20:52"), (31, "2025-01-01 02:25:47"),
        (29, "2025-01-01 02:30:35"), (31, "2025-01-01 02:35:52"), (35, "2025-01-01 02:40:28")
    ])
]

USERS = {}

@app.route('/')
def home():
    return redirect(url_for('dashboard'))

@app.route('/dashboard', methods=['GET'])
def dashboard():
    global DATA
    error = None
    try:
        count = int(request.args.get("count", 20))
        if count <= 0:
            raise ValueError
    except ValueError:
        error = "Zadejte kladné číslo"
        count = 15

    sorted_data = sorted(DATA, key=lambda x: x['timestamp'])
    latest = sorted_data[-1] if sorted_data else None
    last_values = sorted_data[-count:] if sorted_data else []

    return render_template("dashboard.html", latest=latest, data=last_values, error=error, count=count)

@app.route('/delete_oldest', methods=['POST'])
def delete_oldest():
    global DATA
    if DATA:
        oldest = min(DATA, key=lambda x: x['timestamp'])
        DATA.remove(oldest)

    # Zachovat count parametr při přesměrování
    count = request.form.get("count", "15")
    return redirect(url_for('dashboard', count=count))

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        if USERS.get(username) == password:
            session['user'] = username
            return redirect(url_for('dashboard'))
        else:
            flash('Špatné jméno nebo heslo.')
    return render_template('login.html')

@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        if not re.match(r"^[a-zA-Z0-9_]+$", username):
            flash('Neplatné uživatelské jméno.')
        elif username in USERS:
            flash('Uživatel již existuje.')
        else:
            USERS[username] = password
            flash('Registrace úspěšná.')
            return redirect(url_for('login'))
    return render_template('register.html')

if __name__ == '__main__':
    print("Spouštím Flask aplikaci...")
    app.run(debug=True)
