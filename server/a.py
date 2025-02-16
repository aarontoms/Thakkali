from flask import Flask, request, Response, jsonify
from flask_cors import CORS
from pymongo import MongoClient
import bcrypt, os, base64
from PIL import Image
from io import BytesIO
from datetime import datetime
from dotenv import load_dotenv
import google.generativeai as genai


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
    client = genai.Client(api_key=os.environ["GOOGLE_API_KEY"],
                      http_options={'api_version': 'v1alpha'})
    response = client.models.generate_content(
        contents = "Why is the sky blue?",
    )
    print(response)
    
    return jsonify({"message": "Hello World"}), 200


@app.route('/descGenerate', methods=['POST'])
def descGenerate():
    data = request.get_json()
    username = data.get('username').lower()
    disease = data.get('disease')
    
    genai.configure(api_key=os.environ["GOOGLE_API_KEY"])
    
    generation_config = {
        "temperature": 0.5,
        "top_p": 0.9,
        "top_k": 64,
        "max_output_tokens": 8192,
        "response_mime_type": "application/json",
        "response_schema": {
        "type": "object",
        "properties": {
            "Disease": {"type": "string"},
            "Description": {"type": "string"},
            "Symptoms": {"type": "array", "items": {"type": "string"}},
            "Causes": {"type": "array", "items": {"type": "string"}},
            "Long Term Steps": {"type": "array", "items": {"type": "string"}},
            "Short Term Steps": {"type": "array", "items": {"type": "string"}}
        },
        "required": ["Disease", "Description", "Symptoms", "Causes", "Long Term Steps", "Short Term Steps"]
    }
    }
    model = genai.GenerativeModel(
        model_name="gemini-2.0-flash",
        generation_config=generation_config,
    )
    response = model.generate_content("Give me a description of " + disease + " for tomato leaf. Keep the response short and to the point. Format the response with necessary punctuations and line breaks. Also include the symptoms and causes of the disease and necessary steps to be taken in the longer and shorter run. The response should contain the following keys and nothing else: disease, description, symptoms, causes, Long Term Steps, Short Term Steps. Prefer to answer in points than in paragraphs. DO NOT USE SPECIAL CHARACTERS AND EMOJIS AT ALL. And remember that the response is to be displayed on a mobile device.")
    text = response.text
    print(text)
    
    return text

def send_image_to_gemini(image):
    generation_config = {
        "temperature": 0.5,
        "top_p": 0.9,
        "top_k": 64,
        "max_output_tokens": 8192,
        "response_mime_type": "text/plain",
    }
    model = genai.GenerativeModel("gemini-2.0-flash", generation_config)
    response = model.generate_content([
        "For the provided image give a brief and consice description in context of agriculture. Do not deviate from the argicultural, plant/animal domain. For out of domain images, just respond with a request to retry the upload", image
    ])
    return ("\n\n" + response.text)

@app.route('/chat', methods=['POST'])
def SeachChat():
    data = request.get_json()
    query = data.get('query', '')
    image_data = data.get('image')
    
    if image_data:
        image_bytes = base64.b64decode(image_data)
        image = Image.open(BytesIO(image_bytes))
        gemini_response = send_image_to_gemini(image)
    else:
        gemini_response = ""

    generation_config = {
        "temperature": 0.5,
        "top_p": 0.9,
        "top_k": 64,
        "max_output_tokens": 2048,
        "response_mime_type": "text/plain",
    }
    model = genai.GenerativeModel("gemini-2.0-flash", generation_config = generation_config)
    chat_response = model.generate_content(
        f"You are an AI ChatBot with agricultural specialties. Answer in context of agriculture only.\nUser query: {query}.\nRespond in a natural, conversational tone without special characters or emojis."
    )
    
    return (chat_response.text + "" + gemini_response)

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)