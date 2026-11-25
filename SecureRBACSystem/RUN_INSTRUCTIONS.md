# Secure Role-Based Access Management System - Setup and Run Guide

This guide provides step-by-step instructions on how to set up, build, deploy, and run the SecureRBACSystem from scratch on a macOS environment.

## Prerequisites

Ensure you have the following installed on your system:
1. **Java Development Kit (JDK) 17+**
2. **Apache Maven** (for building the project)
3. **Apache Tomcat 10.1.x** (for deploying the `.war` file)
4. **MySQL Server** (database backend)

---

## Step 1: Database Setup

1. **Start the MySQL service:**
   ```bash
   brew services start mysql
   ```

2. **Log into MySQL and create the database:**
   ```bash
   mysql -u root -p
   ```
   ```sql
   CREATE DATABASE rbac_system;
   ```

3. **Run the Database Schema and Migrations:**
   You will need to run the SQL scripts in the `sql/` directory to set up tables and default data. Run them in this order:
   ```bash
   mysql -u root -p rbac_system < sql/schema.sql
   mysql -u root -p rbac_system < sql/migration_v2.sql
   mysql -u root -p rbac_system < sql/migration_v3.sql
   ```

4. **Configure Database Credentials:**
   Open `src/main/resources/db.properties` and ensure the `db.user` and `db.password` match your local MySQL credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/rbac_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
   db.user=root
   db.password=your_mysql_password
   ```

---

## Step 2: Build the Project

Use Maven to compile the Java code and package it into a `.war` (Web Application Archive) file.

1. **Navigate to the project root directory:**
   ```bash
   cd "/Users/shrey/Desktop/Shrey/Minor Project Sem 5/RBAC MVP/SecureRBACSystem"
   ```

2. **Clean and Build the project:**
   ```bash
   mvn clean package
   ```
   *This command will create a `target/SecureRBACSystem.war` file.*

---

## Step 3: Deploy to Tomcat

Copy the generated `.war` file to your Tomcat `webapps` directory. Based on your Homebrew installation, the path is typically located in the Cellar.

1. **Copy the WAR file:**
   ```bash
   cp target/SecureRBACSystem.war /opt/homebrew/Cellar/tomcat@10/10.1.60/libexec/webapps/
   ```
   *(Note: If you update Tomcat via Homebrew in the future, the version number `10.1.60` in the path might change).*

---

## Step 4: Run Tomcat

1. **Stop any existing Tomcat instances (Important):**
   If you encounter a `java.net.BindException: Address already in use` (like port `8080` or `8005`), it means Tomcat or another service is already running. Stop it using:
   ```bash
   catalina stop
   ```
   *If that doesn't work, you can forcibly kill the process:*
   ```bash
   lsof -ti:8080 | xargs kill -9
   ```

2. **Start the Tomcat server:**
   ```bash
   catalina run
   ```
   *(Using `run` will display the server logs in your terminal. You can also use `catalina start` to run it in the background).*

---

## Step 5: Access the Application

Once Tomcat indicates `Server startup in [X] milliseconds` in the logs, the application is deployed and ready.

1. **Open your web browser and navigate to:**
   ```
   http://localhost:8080/SecureRBACSystem/
   ```

You should now see the application's login or home page!
