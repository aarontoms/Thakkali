from flask import Flask, request, Response, jsonify
from flask_cors import CORS
from pymongo import MongoClient
import bcrypt, os, base64, tempfile, json, random, requests
from PIL import Image
from io import BytesIO
from datetime import datetime
from dotenv import load_dotenv
import google.generativeai as genai
from duckduckgo_search import DDGS


app = Flask(__name__)
CORS(app, supports_credentials=True, resources={r"*": {"origins": "*"}})

load_dotenv()
username = os.getenv("MONGO_USER")
password = os.getenv("MONGO_PASS")
cluster = os.getenv("MONGO_CLUSTER")
uri = f"mongodb+srv://{username}:{password}@{cluster}?retryWrites=true&w=majority&appName=VasteDB"
mongo = MongoClient(uri)
db = mongo["Thakkali"]


@app.route("/", methods=["OPTIONS"])
@app.route("/<path:path>", methods=["OPTIONS"])
@app.route("/*", methods=["OPTIONS"])
def handle_options(path=None):
    response = Response()
    response.headers["Access-Control-Allow-Origin"] = "*"
    response.headers["Access-Control-Allow-Methods"] = "GET, POST, PUT, DELETE, OPTIONS"
    response.headers["Access-Control-Allow-Headers"] = (
        "Content-Type, Authorization, X-Requested-With"
    )
    response.headers["Access-Control-Allow-Credentials"] = "true"
    response.headers["Access-Control-Max-Age"] = "86400"
    return response


@app.route("/signup", methods=["POST"])
def signup():
    data = request.get_json()
    username = data["username"].lower()
    email = data["email"]
    password = data["password"]
    hashed = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())
    user = db.Auth.find_one({"username": username})
    if user:
        return jsonify({"message": "Username already exists"}), 400
    else:
        user = {
            "username": username,
            "email": email,
            "password": hashed,
            "created_at": datetime.now(),
        }
        db.Auth.insert_one(user)
        user = db.Auth.find_one({"username": username})
        return (
            jsonify(
                {
                    "message": "Signup successful",
                    "userid": str(user["_id"]),
                    "username": user["username"],
                }
            ),
            200,
        )


@app.route("/login", methods=["POST"])
def login():
    data = request.get_json()
    username = data.get("username").lower()
    password = data.get("password")
    user = db.Auth.find_one({"username": username})
    if user:
        if bcrypt.checkpw(password.encode("utf-8"), user["password"]):
            userid = str(db.Auth.find_one({"username": username})["_id"])
            return (
                jsonify(
                    {
                        "message": "Login successful",
                        "userid": userid,
                        "username": username,
                    }
                ),
                200,
            )
        else:
            return jsonify({"message": "Invalid password"}), 401
    else:
        return jsonify({"message": "User not found"}), 404


@app.route("/upload", methods=["POST"])
def upload():
    data = request.get_json()
    username = data.get("username").lower()
    imageUri = data.get("uri")
    user = db.URIS.find_one({"username": username})
    if user:
        db.URIS.update_one({"username": username}, {"$push": {"images": imageUri}})
    else:
        db.URIS.insert_one({"username": username, "images": [imageUri]})
    return jsonify({"message": "Image stored successfully"}), 200


@app.route("/test", methods=["GET"])
def test():
    # genai.configure(api_key=os.environ["GOOGLE_API_KEY"])
    # generate_config = {
    #     "temperature": 0.5,
    #     "top_p": 0.9,
    #     "top_k": 64,
    #     "max_output_tokens": 8192,
    #     "response_mime_type": "text/plain"
    # }
    # model = genai.GenerativeModel(
    #     "gemini-2.0-flash",
    #     generation_config=generate_config)
    # response = model.generate_content("Say something about the sky")

    # print(response)

    # return response.text

    links = fetchImages("tomato leaf")
    return jsonify(links)


def fetchImages(query):
    num = 10
    results = DDGS().images(keywords=query, max_results=num)

    links = [link["image"] for link in results]
    link = random.choice(links)
    return link

@app.route("/descGenerate", methods=["POST"])
def descGenerate():
    data = request.get_json()
    username = data.get("username").lower()
    disease = data.get("disease")
    species = data.get("species")

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
                "Short Term Steps": {"type": "array", "items": {"type": "string"}},
                "Medications": {"type": "array", "items": {"type": "string"}},
            },
            "required": [
                "Disease",
                "Description",
                "Symptoms",
                "Causes",
                "Long Term Steps",
                "Short Term Steps",
                "Medications",
            ],
        },
    }
    model = genai.GenerativeModel(
        model_name="gemini-2.0-flash",
        generation_config=generation_config,
    )
    response = model.generate_content(
        f"Give me a description of {disease} for {species} leaf. Keep the response short and to the point. Prefer to answer in points than in paragraphs. DO NOT USE SPECIAL CHARACTERS AND EMOJIS AT ALL. And remember that the response is to be displayed on a mobile device. Keep the response in simple words and easy to understand"
    )

    text = response.text.replace("*", "")
    text_json = json.loads(text)
    text_json["images"] = fetchImages(f"{disease} {species} leaf")
    text = json.dumps(text_json)
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
    response = model.generate_content(
        [
            "For the provided image give a brief and consice description in context of agriculture. Do not deviate from the argicultural, plant/animal domain. For out of domain images, just respond with a request to retry the upload",
            image,
        ]
    )
    return "\n\n" + response.text


@app.route("/chat", methods=["POST"])
def SeachChat():
    data = request.get_json()
    query = data.get("query", "")

    # if 'file' not in request.files:
    #     return jsonify({"error": "No file part"}), 400

    # file = request.files['file']

    # if file.filename == '':
    #     return jsonify({"error": "No selected file"}), 400

    # temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".jpg")
    # file.save(temp_file.name)

    # with open(temp_file.name, "rb") as img_file:
    #     gemini_response = genai.GenerativeModel("gemini-2.0-flash").generate_content(
    #         contents=["For the provided image give a brief and consice description in context of agriculture. Do not deviate from the argicultural, plant/animal domain. For out of domain images, just respond with a request to retry the upload", img_file]
    #     )

    # os.remove(temp_file.name)

    generation_config = {
        "temperature": 0.5,
        "top_p": 0.9,
        "top_k": 64,
        "max_output_tokens": 512,
        "response_mime_type": "application/json",
        "response_schema": {
            "type": "object",
            "properties": {
                "valid": {"type": "string"},
                "response": {"type": "string"},
            },
            "required": ["valid", "response"],
        },
    }
    model = genai.GenerativeModel(
        "gemini-2.0-flash", generation_config=generation_config
    )
    chat_response = model.generate_content(
        f"You are an AI ChatBot with agricultural specialties. Answer in context of agriculture only.\nUser query: {query}.\nRespond in a natural, conversational tone no special characters or emojis. Skip the greeting and directly answer query with no deviation. If the query is out of domain, respond with a request to retry the query. Don't think of yourself as a generative model, think of yourself as a human expert in agriculture. Also try to answer in shorter paragraphs than a single long paragraph. Keep it concise and to the point. Set validity to 'valid' if the response is valid, else set it to 'invalid'"
    )
    text = chat_response.text
    text_json = json.loads(text)
    if text_json["valid"] == "invalid":
        text_json["images"] = []
        text = json.dumps(text_json)
        return text

    links = fetchImages(query)
    text_json["images"] = links
    text = json.dumps(text_json)
    return text


@app.route("/scan", methods=["POST"])
def scan():
    data = request.get_json()
    image_url = data.get("image_url")

    genai.configure(api_key=os.environ["GOOGLE_API_KEY"])
    try:
        response = requests.get(image_url, stream=True)
        if response.status_code != 200:
            return "Failed to fetch"

        with tempfile.NamedTemporaryFile(delete=False, suffix=".jpg") as temp_file:
            for chunk in response.iter_content(1024):
                temp_file.write(chunk)
            temp_file_path = temp_file.name

        with Image.open(temp_file_path) as image:
            gemini_response = genai.GenerativeModel(
                "gemini-2.0-flash"
            ).generate_content(
                contents=[
                    "For the provided image, give a brief and concise description in the context of agriculture. Do not deviate from the agricultural, plant/animal domain. For out-of-domain images, just respond with a request to retry the upload.",
                    image,
                ]
            )

        os.remove(temp_file_path)

        return gemini_response.text

    except Exception as e:
        print(e)
        return str(e)


@app.route("/addShop", methods=["POST"])
def addShop():
    data = request.get_json()
    username = data.get("username")
    lat = data.get("lat")
    lon = data.get("lon")
    db.Shops.insert_one({"username": username, "lat": lat, "lon": lon})
    return jsonify({"message": "Shop added successfully"}), 200

@app.route("/updateShop", methods=["POST"])
def updateShop():
    data = request.get_json()
    name = data.get("username")
    inventory = data.get("inventory")
    db.Shops.update_one({"username": name}, {"$set": {"inventory": inventory}})
    return jsonify({"message": "Shop updated successfully"}), 200

@app.route("/getShops", methods=["POST"])
def getShops():
    data = request.get_json()
    lat = data.get("lat")
    lon = data.get("lon")

    lat_range = 0.45
    lon_range = 0.45
    shops = list(
        db.Shops.find(
            {
                "lat": {"$gte": lat - lat_range, "$lte": lat + lat_range},
                "lon": {"$gte": lon - lon_range, "$lte": lon + lon_range},
            }
        )
    )
    for shop in shops:
        del shop["_id"]
    print(shops)

    return jsonify(shops)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
