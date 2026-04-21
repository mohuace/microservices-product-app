# Microservices Product Application

A distributed microservices-based e-commerce application built with Spring Boot, featuring multiple services working together through an API Gateway and Service Registry.

## Table of Contents
- [System Architecture](#system-architecture)
- [Prerequisites](#prerequisites)
- [Database Setup (macOS with Docker)](#database-setup-macos-with-docker)
- [Running the Application Locally](#running-the-application-locally)
- [Application Flow](#application-flow)
- [CORS Configuration Note](#cors-configuration-note)
- [Microservices Overview](#microservices-overview)

---

## System Architecture

```
┌─────────────────────┐
│   React Frontend    │
│  (localhost:5173)   │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   API Gateway       │
│  (localhost:8086)   │ ◄── CORS handled here only
└──────────┬──────────┘
           │
    ┌──────┼──────┬────────┐
    ▼      ▼      ▼        ▼
┌────┐ ┌────┐ ┌────┐  ┌────┐
│Prod│ │Ord │ │Pay │  │Inv │
│uct │ │er  │ │ment│  │ent │
│Svc │ │Svc │ │Svc │  │Svc │
└────┘ └────┘ └────┘  └────┘
    │      │      │        │
    └──────┼──────┼────────┘
           ▼
    ┌─────────────────┐
    │  Service      │
    │  Registry     │
    │  (Eureka)     │
    └─────────────────┘
    
    All talk to MSSQL Database
```

---

## Prerequisites

### Required Software
- **Java Development Kit (JDK)**: 11 or higher
- **Maven**: 3.6 or higher (or use included mvnw)
- **Node.js**: 16 or higher (for frontend)
- **npm**: 7 or higher
- **Docker**: For running MSSQL Server

### Port Requirements
- API Gateway: `8086`
- Service Registry: `8761`
- Product Service: `8081`
- Order Service: `8082`
- Payment Service: `8083`
- Inventory Service: `8084`
- Frontend: `5173`
- MSSQL Server: `1433`

---

## Database Setup (macOS with Docker)

### Step 1: Install Docker
Download and install Docker Desktop for macOS from [docker.com](https://www.docker.com/products/docker-desktop)

### Step 2: Run MSSQL Server Container

Execute the following command in your terminal:

```bash
docker run \
--platform linux/amd64 \
-e "ACCEPT_EULA=Y" \
-e "MSSQL_SA_PASSWORD=YourSecurePassword@123" \
-p 1433:1433 \
--name sql \
-d mcr.microsoft.com/mssql/server:2022-latest
```

**Replace `YourSecurePassword@123` with your own secure password.**

### Step 3: Verify Container is Running

```bash
docker ps | grep sql
```

### Step 4: Create Database

Connect to the SQL Server and run the DDL script:

```bash
# Navigate to db_scripts folder
cd db_scripts

# Execute the DDL script (update connection details as needed)
sqlcmd -S localhost,1433 -U sa -P YourSecurePassword@123 -i DDL.sql
```

Or use any SQL client like DBeaver or Azure Data Studio to connect:
- **Host**: localhost
- **Port**: 1433
- **Username**: sa
- **Password**: YourSecurePassword@123

Then execute the SQL script from `db_scripts/DDL.sql`

---

## Running the Application Locally

### Step 1: Configure Database Connection

Update database credentials in each service's `application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=YourDatabaseName
spring.datasource.username=sa
spring.datasource.password=YourSecurePassword@123
```

Files to update:
- `apps/product-service/src/main/resources/application.properties`
- `apps/inventory-service/src/main/resources/application.properties`
- `apps/order-service/src/main/resources/application.properties`
- `apps/payment-service/src/main/resources/application.properties`

### Step 2: Start Service Registry

```bash
cd apps/service-registry
./mvnw spring-boot:run
# or on macOS/Linux
mvn spring-boot:run
```

Wait for it to start (typically 30-60 seconds). Access at: `http://localhost:8761`

### Step 3: Start Microservices

Open separate terminal tabs/windows for each service:

**Product Service:**
```bash
cd apps/product-service
./mvnw spring-boot:run
```

**Inventory Service:**
```bash
cd apps/inventory-service
./mvnw spring-boot:run
```

**Order Service:**
```bash
cd apps/order-service
./mvnw spring-boot:run
```

**Payment Service:**
```bash
cd apps/payment-service
./mvnw spring-boot:run
```

### Step 4: Start API Gateway

```bash
cd apps/api-gateway
./mvnw spring-boot:run
```

### Step 5: Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Access the application at: `http://localhost:5173`

---

## Application Flow

### How the System Works

```
1. USER INTERACTION (Frontend)
   └─► User opens React application in browser (localhost:5173)
       └─► User interacts with UI (browse products, place order, etc.)

2. REQUEST TO API GATEWAY
   └─► React frontend sends HTTP request to API Gateway (localhost:8086)
       └─► Example: GET /products
       └─► Example: POST /orders

3. API GATEWAY PROCESSING
   └─► API Gateway receives request
       ├─► Applies CORS headers (✓ CORS is handled here)
       ├─► Routes request to appropriate microservice
       └─► Uses Service Registry (Eureka) to locate service instances

4. MICROSERVICE PROCESSING
   ├─► Product Service: Handles product catalog and details
   ├─► Inventory Service: Manages product stock levels
   ├─► Order Service: Manages order creation and tracking
   │   └─► Calls Product Service to fetch product details
   │   └─► Calls Inventory Service to check/reserve stock
   │   └─► Calls Payment Service to process payment
   └─► Payment Service: Processes payments

5. DATABASE INTERACTION
   └─► Each microservice queries MSSQL Server for its own data
       └─► Product Service ◄─► Product Table
       └─► Inventory Service ◄─► Inventory Table
       └─► Order Service ◄─► Orders Table
       └─► Payment Service ◄─► Payments Table

6. RESPONSE FLOW
   └─► Microservice returns response to API Gateway
       └─► API Gateway returns response to Frontend
           └─► Frontend displays result to user

```

### Service Communication

- **Frontend ◄─► API Gateway**: Direct HTTP communication
- **API Gateway ◄─► Microservices**: Uses Service Registry to discover endpoints
- **Microservice ◄─► Microservice**: Direct REST calls (Order Service → Product/Inventory/Payment Services)
- **Microservices ◄─► MSSQL Database**: Direct database connections

---

## CORS Configuration Note

### ⚠️ IMPORTANT: CORS Should Only Be Handled at API Gateway Level

**Current Configuration:**
- CORS is properly configured in the **API Gateway only** (`application.properties`)
- Microservices do NOT have @CrossOrigin annotations

**Why This Matters:**

If CORS is enabled at **BOTH** the API Gateway AND individual microservices:
1. Multiple CORS headers are sent in responses
2. Browser receives conflicting CORS directives
3. Browser security model rejects the response
4. Application fails with CORS errors

**Correct Approach (Current Implementation):**
- ✅ **API Gateway handles CORS**: All browser requests go through the gateway
- ✅ **Microservices don't use @CrossOrigin**: They only handle service-to-service calls
- ✅ **CORS is browser-only**: Microservices communicate via server-to-server (no browser involved)

**Current CORS Configuration in API Gateway:**
```properties
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedOrigins=http://localhost:5173
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedMethods=GET,POST,PUT,DELETE,OPTIONS
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedHeaders=*
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowCredentials=true
```

**Do NOT add @CrossOrigin annotations to microservice controllers** - they're not needed and will cause conflicts.

---

## Microservices Overview

### 1. Service Registry (Eureka)
- **Port**: 8761
- **Purpose**: Maintains registry of all microservices
- **Endpoint**: `http://localhost:8761`
- All microservices register themselves on startup

### 2. API Gateway
- **Port**: 8086
- **Purpose**: Single entry point for all client requests
- **Responsibilities**:
  - Route requests to appropriate microservices
  - Handle CORS headers
  - Load balance across service instances
  - Service discovery via Eureka

### 3. Product Service
- **Port**: 8081
- **Purpose**: Manage product catalog
- **Endpoints**:
  - `GET /api/products` - Fetch all products
  - `GET /api/products/{id}` - Fetch product details
  - `POST /api/products` - Create product
  - `PUT /api/products/{id}` - Update product

### 4. Inventory Service
- **Port**: 8084
- **Purpose**: Manage product inventory/stock
- **Endpoints**:
  - `GET /api/inventory/{productId}` - Check stock
  - `POST /api/inventory/update` - Update stock
  - `POST /api/inventory/batch-update` - Batch stock update

### 5. Order Service
- **Port**: 8082
- **Purpose**: Manage customer orders
- **Endpoints**:
  - `GET /api/orders` - Fetch all orders
  - `POST /api/orders` - Create new order
  - `GET /api/orders/{id}` - Fetch order details
- **Calls Other Services**:
  - Product Service (fetch product details)
  - Inventory Service (check/reserve stock)
  - Payment Service (process payment)

### 6. Payment Service
- **Port**: 8083
- **Purpose**: Process payments
- **Endpoints**:
  - `POST /api/payments` - Process payment
  - `GET /api/payments/{id}` - Get payment status

---

## Troubleshooting

### Issue: Microservices won't start
- Ensure Service Registry is running on port 8761
- Check port availability: `lsof -i :8086,8081,8082,8083,8084,8761`
- Update `application.properties` with correct MSSQL credentials

### Issue: Database connection errors
- Verify MSSQL container is running: `docker ps | grep sql`
- Check database credentials in `application.properties`
- Ensure DDL script has been executed

### Issue: Frontend can't communicate with backend
- Verify API Gateway is running on port 8086
- Check browser console for CORS errors
- Ensure frontend is configured to use `http://localhost:8086`

### Issue: Services not registering with Eureka
- Check Service Registry is accessible at `http://localhost:8761`
- Verify `eureka.client.service-url.defaultZone` in each service's `application.properties`

---

## Development Notes

- All microservices use **Spring Boot** and **Spring Cloud**
- Service discovery: **Eureka**
- API Gateway: **Spring Cloud Gateway**
- Database: **Microsoft SQL Server**
- Frontend: **React with TypeScript + Vite**

---

## License

This project is provided as-is for educational purposes.
