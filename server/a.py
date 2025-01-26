from flask import Flask, request, Response
from flask_cors import CORS
from pymongo import MongoClient
import bcrypt, os
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

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')
    
    

@app.route('/signup', methods=['POST'])
def signup():
    return 'Signup'




if __name__ == '__main__':
    app.run(debug=True)