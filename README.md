# Plant Health Detection App

## Overview
The **Plant Health Detection App** is a comprehensive solution for plant disease identification, medication recommendations, and online store management. It includes offline disease detection using pre-trained models, fetching disease descriptions and remedies from the internet, recommending medications, and linking to nearby stores for purchasing supplies. The app also features a shop dashboard for managing stock listings and prices, as well as a responsive AI assistant for agricultural queries.

## Features

### 1. Offline Plant Disease Detection
- Detects plant diseases from leaf images using offline trained models.

### 2. Disease Descriptions & Remedies
- Fetches detailed descriptions, causes, symptoms, and remedies from the internet after detection.

### 3. Medication Recommendations & Nearby Shops
- Suggests medications and shows nearby shops with available stock and pricing.

### 4. Shop Dashboard
- Shop owners can log in to manage their product listings, stock, and prices.

### 5. Online Disease Detection
- Detects diseases online for plants not covered by the offline model.

### 6. AI Agricultural Assistant
- AI assistant answers agriculture-related queries with text and images.

## Usage Guide

### For Users

#### 1. Disease Detection
- Click/Upload a leaf image.
- App detects disease offline using trained models.
- View detected disease name and basic information.

#### 2. Fetch Disease Descriptions & Remedies
- After detection, view detailed disease description, symptoms, causes, and remedies fetched from the internet.

#### 3. Medication Recommendations & Nearby Shops
- App suggests suitable medications.
- Browse nearby shops showing stock availability and prices.
- Contact or visit shops directly.

#### 4. Online Disease Detection (For Unrecognized Plants)
- If offline detection fails, perform online detection(can also be used to detect pests whether they are harmful or not).
- Upload image, app fetches disease name and remedies from the internet.

#### 5. AI Agricultural Assistant
- Ask any plant or agriculture-related queries.
- Receive text and image-based responses from the AI assistant.

---

### For Shop Owners

#### 1. Shop Login
- Log in with shop credentials through the Shop Login page.

#### 2. Manage Stock
- Add, update, or remove products.
- Set product names, stock quantity, and prices.

#### 3. Track Inventory
- Monitor available stock.
- Adjust inventory based on sales and demand.

## Development

1. Clone the repository:
   ```bash
   git clone https://github.com/aarontoms/Thakkali.git
   ```

2. Navigate to view the app code:
    ```bash
    cd app/src/main/java/com/example/thakkali
    ```
3. Build the mobile app using the Build tool in Android Studio

4. Navigate to the backend code:
    ```bash
    cd server
    ```
5. Install the necessary dependencies:
 - For Android: Use Android Studio to open the project and sync the dependencies.
 - For Backend (Flask and MongoDB): Ensure you have Python 3.x installed, then run:
    ```bash
    pip install -r requirements.txt
    ```
6. Turn on the server using  
    This hosts the server on ```http://localhost:5000```, use it locally or use port forwarding to access it over the internet.  
    ```bash
    python a.py
    ```
7. Train Your Own Model  
  [Learn how to train the model here](https://github.com/aarontoms/Plant-Disease-Detection-Model)
