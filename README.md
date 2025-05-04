# 📌 Mini-assessment - AuthenticationService

This is a Spring Boot-based authentication service that allows users to log in using either manual credentials or OAuth 2.0 authentication (e.g., Google login).

---

## 🔧 Tech Stack

- Java  
- Spring Boot  
- MongoDB Atlas  
- OAuth 2.0

---

## 🚀 Features

- Manual login with username and password  
- OAuth 2.0 authentication (Google)  
- MongoDB Atlas integration for persistent user storage  
- Secure and scalable architecture

---

## 🛠️ Installation & Setup

### 1. Clone the repository

```bash
git clone https://github.com/bhaskarkatara/Mini-assessment1-Service1.git
cd Mini-assessment1-Service1
### 2. Add your credentials

Go to `src/main/resources/application.properties` and add your MongoDB and OAuth2 credentials:

```properties
spring.data.mongodb.uri=your_mongo_uri
spring.security.oauth2.client.registration.google.client-id=your_client_id
spring.security.oauth2.client.registration.google.client-secret=your_client_secret

