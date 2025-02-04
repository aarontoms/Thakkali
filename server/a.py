from flask import Flask, request, Response, jsonify
from flask_cors import CORS
from pymongo import MongoClient
import bcrypt, os
from datetime import datetime
from dotenv import load_dotenv


app = Flask(__name__)
CORS(app, supports_credentials=True, resources={r"*": {"origins": "*"}})

load_dotenv()
username = os.getenv('MONGO_USER')
password = os.getenv('MONGO_PASS')
cluster = os.getenv('MONGO_CLUSTER')
uri = f"mongodb+srv://{username}:{password}@{cluster}?retryWrites=true&w=majority&appName=VasteDB"
mongo = MongoClient(uri)
db = mongo["Thakkali"]

@app.route('/', methods=['OPTIONS'])
@app.route('/<path:path>', methods=['OPTIONS'])
@app.route('/*', methods=['OPTIONS'])
def handle_options(path=None):
    response = Response()
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Methods'] = 'GET, POST, PUT, DELETE, OPTIONS'
    response.headers['Access-Control-Allow-Headers'] = 'Content-Type, Authorization, X-Requested-With'
    response.headers['Access-Control-Allow-Credentials'] = 'true'
    response.headers['Access-Control-Max-Age'] = '86400'
    return response

@app.route('/signup', methods=['POST'])
def signup():
    data = request.get_json()
    username = data['username'].lower() 
    email = data['email']
    password = data['password']
    hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    user = db.Auth.find_one({"username": username})
    if user:
        return jsonify({"message": "Username already exists"}), 400
    else:
        user = {
            "username": username,
            "email": email,
            "password": hashed,
            "created_at": datetime.now()
        }
        db.Auth.insert_one(user)
        user = db.Auth.find_one({"username": username})
        return jsonify({"message": "Signup successful", "userid": str(user["_id"]), "username":user["username"]}), 200

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username').lower()
    password = data.get('password')
    user = db.Auth.find_one({"username": username})
    if user:
        if bcrypt.checkpw(password.encode('utf-8'), user['password']):
            userid = str(db.Auth.find_one({"username": username})["_id"])
            return jsonify({"message": "Login successful", "userid": userid, "username": username}), 200
        else:
            return jsonify({"message": "Invalid password"}), 401
    else:
        return jsonify({"message": "User not found"}), 404
 
 
@app.route('/upload', methods=['POST'])
def upload():
    data = request.get_json()
    username = data.get('username').lower()
    imageUri = data.get('uri')
    user = db.URIS.find_one({"username": username})
    if user:
        db.URIS.update_one({"username": username}, {"$push": {"images": imageUri}})
    else:
        db.URIS.insert_one({"username": username, "images": [imageUri]})
    return jsonify({"message": "Image stored successfully"}), 200

@app.route('/test', methods=['GET'])
def test():
    return jsonify({"message": "Hello World"}), 200


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)