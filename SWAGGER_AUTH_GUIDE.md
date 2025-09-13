# Swagger UI with JWT Authentication Guide

## Overview
All microservices in the Movie Booking System now have enhanced Swagger UI with JWT authentication support. This allows you to test protected endpoints directly from the Swagger interface.

## Services with JWT Authentication

### ✅ **User Service** - `/user-service/swagger-ui.html`
- **Public Endpoints**: `/api/auth/login`, `/api/auth/register`
- **Protected Endpoints**: `/api/users/**` (requires JWT token)
- **Features**: Login endpoint documented, JWT security scheme configured

### ✅ **Booking Service** - `/booking-service/swagger-ui.html`
- **Protected Endpoints**: All endpoints require JWT token
- **Features**: JWT authorization button available

### ✅ **Payment Service** - `/payment-service/swagger-ui.html`
- **Protected Endpoints**: All endpoints require JWT token
- **Features**: JWT authorization button available

### ✅ **Ticket Service** - `/ticket-service/swagger-ui.html`
- **Protected Endpoints**: All endpoints require JWT token
- **Features**: JWT authorization button available

### ✅ **Theatre Service** - `/theatre-service/swagger-ui.html`
- **Protected Endpoints**: All endpoints require JWT token
- **Features**: JWT authorization button available

## How to Use JWT Authentication in Swagger

### Step 1: Get JWT Token
1. Navigate to **User Service** Swagger UI
2. Use the `/api/auth/login` endpoint
3. **Important**: Use a valid email format (the validation was causing issues)
4. Provide credentials:
   ```json
   {
     "email": "user@example.com",
     "password": "password123"
   }
   ```
5. Copy the `token` from the response (without the "Bearer " prefix)

### Step 2: Authorize in Swagger UI
1. Click the **🔒 Authorize** button (top-right in Swagger UI)
2. In the "Bearer Authentication" dialog:
   - Enter your JWT token (without "Bearer " prefix)
   - Click **Authorize**
   - Click **Close**

### Step 3: Access Protected Endpoints
- All protected endpoints will now include the Authorization header automatically
- The 🔒 lock icon next to endpoints indicates authentication required
- You can test any protected endpoint normally

## Example Authentication Flow

```bash
# 1. Get token via User Service
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# Response: {"token":"eyJ...","type":"Bearer","email":"...","name":"..."}

# 2. Use token in other services (automatically handled by Swagger UI)
# Just click Authorize and paste the token!
```

## Gateway Access
All services are also accessible through the API Gateway:
- **Gateway Swagger**: `http://localhost:8080/swagger-ui.html`
- **Direct Service Access**: `http://localhost:PORT/swagger-ui.html`

## Security Notes
- Tokens expire based on JWT configuration
- Re-authenticate when tokens expire
- Public endpoints (auth, search, movies) don't require tokens
- All other endpoints require valid JWT tokens

## Troubleshooting

### ❌ "Invalid email format" Error
- **Issue**: Using "string" instead of valid email in Swagger UI
- **Solution**: Use a proper email format like `user@example.com`

### ❌ "403 Forbidden" on Login
- **Issue**: Security config blocking /error endpoint
- **Solution**: Fixed - `/error` endpoint is now permitted

### ❌ "WeakKeyException: The signing key's size is 400 bits"
- **Issue**: JWT secret key too short for HS512 algorithm
- **Solution**: Fixed - JWT secret updated to 720 bits (90 characters) for secure HS512 signing

### ❌ "Failed to find user" / Login with 403 Error
- **Issue**: Trying to login with email that doesn't exist in database
- **Common Cause**: Registered with one email, trying to login with different email
- **Solution**:
  1. Use the EXACT same email/password you used during registration
  2. OR register a new user first, then login with those same credentials
  3. Check logs to see what email was actually registered

### ❌ "401 Unauthorized" on Protected Endpoints
- **Issue**: Missing or invalid JWT token
- **Solution**:
  1. Login first via `/api/auth/login`
  2. Click 🔒 Authorize button
  3. Paste JWT token (without "Bearer" prefix)
  4. Try the protected endpoint again

### ❌ Swagger UI Not Loading
- **Issue**: Service-specific Swagger endpoints
- **Solution**: Use correct URLs:
  - User Service: `http://localhost:8083/swagger-ui.html`
  - Booking Service: `http://localhost:8084/swagger-ui.html`
  - Payment Service: `http://localhost:8085/swagger-ui.html`
  - etc.

### ✅ Complete Test Workflow

**Option 1: Register New User First**
1. Go to User Service Swagger (`http://localhost:8083/swagger-ui.html`)
2. Use `/api/auth/register` to create a new user:
   ```json
   {
     "name": "Test User",
     "email": "test@example.com",
     "password": "test123",
     "phone": "+1234567890"
   }
   ```
3. Registration response includes JWT token - you can use it immediately!
4. Or use `/api/auth/login` with the SAME credentials you just registered

**Option 2: Login with Existing User**
1. If you already registered a user, use `/api/auth/login` with the EXACT same credentials
2. **Important**: Use the same email/password you used during registration
3. Common mistake: Registering with one email, then trying to login with a different email

**Example Complete Flow:**
```bash
# Step 1: Register
POST /api/auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "+1234567890"
}

# Step 2: Login (use SAME email/password)
POST /api/auth/login
{
  "email": "john@example.com",
  "password": "password123"
}

# Step 3: Copy token and authorize in other services
```

## 🚨 IMPORTANT: Testing Authentication

### ⚠️ Gateway vs Direct Service Access
**The JWT authentication only works when accessing services through the API Gateway!**

- **✅ Protected (via Gateway)**: `http://localhost:8080/api/bookings/` (requires JWT)
- **❌ Unprotected (direct)**: `http://localhost:8084/api/booking/` (bypasses gateway)

### 🧪 How to Test Authentication Properly

1. **Always use Gateway URLs**: All requests must go through `localhost:8080`
2. **Protected Endpoints**: These require JWT tokens when accessed via gateway:
   - `http://localhost:8080/api/v1/cities/`
   - `http://localhost:8080/api/v1/theatres/`
   - `http://localhost:8080/api/bookings/`
   - `http://localhost:8080/api/payments/`
   - `http://localhost:8080/api/tickets/`
   - `http://localhost:8080/api/users/`

3. **Public Endpoints**: These work without tokens:
   - `http://localhost:8080/api/auth/login`
   - `http://localhost:8080/api/auth/register`
   - `http://localhost:8080/api/v1/search/`
   - `http://localhost:8080/api/movies/`

### 🔍 Debugging Authentication
Look for these logs in the Gateway console:
```
JWT Filter applied to: /api/bookings/test
Authorization header: Bearer eyJ...
JWT token validated successfully
```

If you don't see these logs, the filter isn't being applied to your request path.