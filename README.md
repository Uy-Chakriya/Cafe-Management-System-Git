# ☕ Cafe Shop Management System

A **Java-based desktop application** for managing a cafe, built with **JavaFX** and **Maven**.  
The system provides features for **managing inventory, handling customer orders, and tracking sales**.

---

## 🚀 Features

### 🔑 User Authentication
- Secure **login system** for employees.
- **Registration** feature for new staff members.
- **Password recovery** option using security questions.

### 📦 Inventory Management
Employees can:
- **Add** new products with details such as ID, name, type, stock, price, status, and image.
- **Update** product information when stock or details change.
- **Delete** products that are discontinued.
- **View** all products in a structured table with filters/search options.
- Manage product **status** (e.g., Available, Out of Stock, Discontinued).
- Attach and display **images** for each product to improve menu visualization.

### 🍴 Menu and Ordering System
- Displays a **menu with product images**.
- Customers (via employees) can **add items to an order**.
- Automatically calculates **total price** and **change**.

### 🧾 Sales and Receipt Tracking
- Orders are recorded in the **Receipt table**.
- Employees can **view past receipts** in a table view.
- Helps track **daily sales and employee activity**.

---

## 🛠️ Technologies Used
- **Programming Language:** Java 17  
- **Framework:** JavaFX 17.0.6  
- **Build Tool:** Apache Maven 3.11.0  
- **Database:** SQLite  
- **Database Driver:** `org.xerial:sqlite-jdbc`  

---

## 🗂️ Database Schema

### **Employee**  
Stores user credentials and security questions.  
| Column     | Type     | Description                  |
|------------|----------|------------------------------|
| id         | INTEGER  | Unique employee ID           |
| username   | TEXT     | Login username               |
| password   | TEXT     | Encrypted password           |
| question   | TEXT     | Security question            |
| answer     | TEXT     | Security answer              |
| date       | TEXT     | Date of account creation     |

---

### **Product**  
Contains information about the cafe's products.  
| Column       | Type     | Description                          |
|--------------|----------|--------------------------------------|
| id           | INTEGER  | Unique product ID                    |
| product_id   | TEXT     | Custom product code                  |
| product_name | TEXT     | Name of the product                  |
| type         | TEXT     | Category (e.g., Coffee, Snack)       |
| stock        | INTEGER  | Available stock quantity             |
| price        | REAL     | Unit price                           |
| status       | TEXT     | Availability status (Available/Out)  |
| image        | TEXT     | File path or URL of product image    |
| date         | TEXT     | Date product was added/updated       |

---

### **Customer**  
Tracks customer orders before generating a receipt.  
| Column       | Type       | Description                  |
|--------------|------------|------------------------------|
| id           | INTEGER    | Auto-incremented ID          |
| customer_id  | TEXT       | Customer identifier          |
| product_name | TEXT       | Ordered product name         |
| quantity     | TEXT       | Quantity ordered             |
| price        | REAL       | Price per item               |
| date         | TEXT       | Order date                   |
| em_username  | TEXT       | Employee handling the order  |

---

### **Receipt**  
Stores final receipts after payment is processed.  
| Column       | Type       | Description                  |
|--------------|------------|------------------------------|
| id           | INTEGER    | Auto-incremented ID          |
| customer_id  | TEXT       | Customer identifier          |
| total        | REAL       | Total order amount           |
| date         | TEXT       | Receipt date                 |
| em_username  | TEXT       | Employee handling the order  |

---

## ▶️ How to Run

### ✅ Prerequisites
- Install **Java JDK 17** or higher.
- Install **Maven 3.11.0** or higher.

### 📦 Dependencies
This project uses **Maven** for dependency management. Ensure your environment is properly configured.

### 🔧 Running the Application
- Main class: `com.example.cafeshopmanagement.App`  
- Run from your IDE (IntelliJ IDEA recommended) **or** use Maven command:

```bash
mvn clean javafx:run
