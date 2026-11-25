# Walkthrough — Secure RBAC System Build

## Summary

Built the complete **Secure Role-Based Access Management System** from scratch — 20 source files across 10 phases, all compiling cleanly into a deployable WAR.

## What Was Built

### Project Structure (matches Architecture Doc §10 exactly)

```
SecureRBACSystem/
├── pom.xml
├── sql/
│   └── schema.sql
└── src/main/
    ├── java/com/rbac/
    │   ├── controller/
    │   │   ├── LoginServlet.java
    │   │   ├── RegisterServlet.java
    │   │   ├── LogoutServlet.java
    │   │   └── AdminManagementServlet.java
    │   ├── filter/
    │   │   └── AuthenticationFilter.java
    │   ├── dao/
    │   │   ├── UserDAO.java
    │   │   └── RoleDAO.java
    │   ├── model/
    │   │   ├── User.java
    │   │   └── Role.java
    │   └── util/
    │       ├── PasswordHasher.java
    │       └── DBConnectionUtil.java
    ├── resources/
    │   └── db.properties
    └── webapp/
        ├── WEB-INF/web.xml
        ├── login.jsp
        ├── register.jsp
        ├── user_home.jsp
        ├── admin_dashboard.jsp
        ├── access_denied.jsp
        ├── error.jsp
        └── css/style.css
```

---

### Architecture Traceability (Architecture Doc §13)

| PRD Requirement | Component Built |
|---|---|
| FR-1 to FR-5 (Authentication) | [LoginServlet](file:///Users/shrey/Desktop/Shrey/Minor%20Project%20Sem%205/RBAC%20MVP/SecureRBACSystem/src/main/java/com/rbac/controller/LoginServlet.java), [RegisterServlet](file:///Users/shrey/Desktop/Shrey/Minor%20Project%20Sem%205/RBAC%20MVP/SecureRBACSystem/src/main/java/com/rbac/controller/RegisterServlet.java), [PasswordHasher](file:///Users/shrey/Desktop/Shrey/Minor%20Project%20Sem%205/RBAC%20MVP/SecureRBACSystem/src/main/java/com/rbac/util/PasswordHasher.java), [UserDAO](file:///Users/shrey/Desktop/Shrey/Minor%20Project%20Sem%205/RBAC%20MVP/SecureRBACSystem/src/main/java/com/rbac/dao/UserDAO.java) |
| FR-6 to FR-9 (RBAC) | [AuthenticationFilter](file:///Users/shrey/Desktop/Shrey/Minor%20Project%20Sem%205/RBAC%20MVP/SecureRBACSystem/src/main/java/com/rbac/filter/AuthenticationFilter.java), [RoleDAO](file:///Users/shrey/Desktop/Shrey/Minor%20Project%20Sem%205/RBAC%20MVP/SecureRBACSystem/src/main/java/com/rbac/dao/RoleDAO.java) |
| FR-10 to FR-13 (Session Mgmt) | [LoginServlet](file:///Users/shrey/Desktop/Shrey/Minor%20Project%20Sem%205/RBAC%20MVP/SecureRBACSystem/src/main/java/com/rbac/controller/LoginServlet.java), [LogoutServlet](file:///Users/shrey/Desktop/Shrey/Minor%20Project%20Sem%205/RBAC%20MVP/SecureRBACSystem/src/main/java/com/rbac/controller/LogoutServlet.java) |
| FR-14 to FR-17 (Admin Dashboard) | [AdminManagementServlet](file:///Users/shrey/Desktop/Shrey/Minor%20Project%20Sem%205/RBAC%20MVP/SecureRBACSystem/src/main/java/com/rbac/controller/AdminManagementServlet.java), [admin_dashboard.jsp](file:///Users/shrey/Desktop/Shrey/Minor%20Project%20Sem%205/RBAC%20MVP/SecureRBACSystem/src/main/webapp/admin_dashboard.jsp) |
| FR-18 to FR-20 (Data & Security) | All DAOs use PreparedStatement, [schema.sql](file:///Users/shrey/Desktop/Shrey/Minor%20Project%20Sem%205/RBAC%20MVP/SecureRBACSystem/sql/schema.sql) with normalized tables |

---

### AI Rules Compliance

| Rule | Status |
|---|---|
| §A1 — Only reference tables in schema | ✅ Only `users`, `roles`, `user_roles` |
| §A3 — All queries use PreparedStatement | ✅ Zero string-concatenated SQL |
| §B1 — Role names match DB exactly | ✅ `"Admin"`, `"Manager"`, `"User"` |
| §B2 — Role checking only in filter | ✅ Only in `AuthenticationFilter` |
| §C2 — No plain-text passwords ever | ✅ BCrypt hashing before any DB write |
| §8 Red Lines | ✅ All five non-negotiable rules satisfied |

### Design Patterns Used (Architecture Doc §11)

| Pattern | Implementation |
|---|---|
| MVC | Servlets → Controllers, DAOs → Model, JSPs → View |
| DAO | `UserDAO`, `RoleDAO` — sole SQL containers |
| Front Controller (partial) | `AuthenticationFilter` — single security choke point |
| Singleton | `DBConnectionUtil` — class-level static initialization |
| Strategy | `PasswordHasher` — BCrypt can be swapped without touching servlets |

---

## Build Verification

✅ **Maven build succeeded** — `mvn clean package` exit code 0
✅ **WAR produced** — `SecureRBACSystem.war` (7.3 MB)
✅ **WAR contents verified** — all 11 `.class` files, 6 `.jsp` files, `web.xml`, `db.properties`, and all library JARs present

---

## Environment Details

| Component | Version |
|---|---|
| JDK | OpenJDK 27 (Homebrew) |
| Maven | 3.9.16 |
| Tomcat | 10.1.60 (Jakarta Servlet 6.0) |
| MySQL | 9.6.0 |
| Servlet API | `jakarta.servlet.*` (not `javax.servlet.*`) |
| Password Hashing | jBCrypt 0.4 |
| JDBC Driver | MySQL Connector/J 8.4.0 |

---

## Steps to Deploy

### 1. Set your MySQL password

Edit [db.properties](file:///Users/shrey/Desktop/Shrey/Minor%20Project%20Sem%205/RBAC%20MVP/SecureRBACSystem/src/main/resources/db.properties) and replace `YOUR_PASSWORD_HERE` with your actual MySQL root password.

### 2. Create the database

```bash
mysql -u root -p < SecureRBACSystem/sql/schema.sql
```

### 3. Rebuild the WAR (after changing db.properties)

```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk/27/libexec/openjdk.jdk/Contents/Home
cd SecureRBACSystem && mvn clean package -q
```

### 4. Deploy to Tomcat

```bash
cp target/SecureRBACSystem.war /opt/homebrew/Cellar/tomcat@10/10.1.60/libexec/webapps/
catalina run
```

### 5. Access the application

Open: **http://localhost:8080/SecureRBACSystem/login**

Default admin login:
- **Username:** `admin`
- **Password:** `admin123`
