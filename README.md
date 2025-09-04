# 🚀 Microservices Project with Spring Boot, Docker & JWT Security

This project demonstrates a microservices architecture using **Spring Boot**, **Spring Security (JWT)**, **Eureka Service Registry**, **API Gateway**, and **Role-Based Authorization**. It also uses **Postgres** and **pgAdmin** via **Docker Compose**.

---

# 🚀 Microservices with Spring Boot, Security & Docker

## 📦 Services from Docker

- **Postgres** → `localhost:5433`  
- **pgAdmin** → `localhost:5050`  
  - **Login**: `admin@admin.com`  
  - **Password**: `admin`  

---

## ⚙️ Run Services

Run each service from IntelliJ IDEA or from the terminal:

- `service-registry` → Eureka Server  
- `auth-service` → Authentication & JWT provider  
- `api-gateway` → Entry point for clients  
- `role-based-service` → Example secured microservice  

💡 Each service will register itself automatically to Eureka.

---

## 🔄 Flow of Requests

1. Client sends a request to **API Gateway**.  
2. Gateway checks if the endpoint is secured using a `RouteValidator`.  
3. If secured → runs `AuthenticationFilter`:  
   - Extracts JWT from `Authorization` header.  
   - Calls **Auth Service** (`/validate-and-get-info`) via **WebClient**.  
   - On success → attaches roles to the request as a header `X-Auth-Roles`.  
4. Request is routed to **Role-Based Service**.  
5. `RoleHeaderAuthenticationFilter` reads roles from the header and sets them in **Spring Security Context**.  
6. Role-based annotations (`@PreAuthorize`) decide access.  

---

## 📌 Example APIs

### 🔑 Auth Service
- `POST /auth/register` → Register a new user  
- `POST /auth/login` → Authenticate a user & get JWT  
- `GET /auth/validate-and-get-info` → Validate token & return user's authorities  

### 🎯 Role-Based Service
- `GET /role-based/admin` → Accessible only by `ADMIN` role  
- `GET /role-based/user` → Accessible only by `USER` role  

---

## 📂 Project Structure

```
├── auth-service
│   └── Handles user authentication & JWT generation
│
├── api-gateway
│   └── Routes requests & applies AuthenticationFilter
│
├── role-based-service
│   └── Example microservice with role-based endpoints
│
├── service-registry
│   └── Eureka Server for service discovery
│
└── docker-compose.yml
    └── Runs Postgres + pgAdmin
```

---

## ✅ Tech Stack

- **Spring Boot 3**  
- **Spring Security** with JWT  
- **Spring Cloud** (Eureka, Gateway)  
- **Postgres** & **pgAdmin** (Docker)  
- **Lombok**  
- **Maven**  

---

## 🚦 How to Run

1. **Run databases**:
   ```bash
   docker-compose up -d
   ```
2. Start `service-registry`.  
3. Start `auth-service`.  
4. Start `api-gateway`.  
5. Start `role-based-service`.  

👉 Access APIs via: `http://localhost:8222` (API Gateway entry point).  

---

## 🔐 Security Flow (Quick Recap)

1. **Client** sends a request to **API Gateway** (with JWT).  
2. **Gateway** validates the token via **Auth Service**.  
3. **Roles** are attached and the request is forwarded to the services.  
4. The services use `@PreAuthorize` for **role-based access control**.  
