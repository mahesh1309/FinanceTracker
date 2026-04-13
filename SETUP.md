# Finance Tracker - Setup Guide

## Prerequisites
- Java 17+
- Maven 3.6+
- A Google Cloud project with OAuth2 credentials

## Google OAuth2 Setup
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or use existing)
3. Navigate to **APIs & Services > Credentials**
4. Click **Create Credentials > OAuth 2.0 Client ID**
5. Choose **Web application**
6. Add Authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
7. Copy the **Client ID** and **Client Secret**

## Running the App

### Option A: Environment Variables (recommended)
```bash
set GOOGLE_CLIENT_ID=your-client-id
set GOOGLE_CLIENT_SECRET=your-client-secret
mvn spring-boot:run
```

### Option B: Local Properties File
Create `src/main/resources/application-local.properties`:
```properties
spring.security.oauth2.client.registration.google.client-id=your-client-id
spring.security.oauth2.client.registration.google.client-secret=your-client-secret
```
Then run:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Access
- App: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console  
  - JDBC URL: `jdbc:h2:file:./financedb`
  - Username: `sa`, Password: *(blank)*

## Features
- Login with Google account
- **Expenses**: Add/remove personal expenses grouped by month, with amount, description, and optional payment source
- **Splits**: Create groups, add members (by email search), add splits that auto-create expenses for each participant, exit groups
