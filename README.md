# Greengrocer Management System

A comprehensive JavaFX application for managing a greengrocer business, featuring distinct interfaces for Customers, Carriers, and Store Owners.

## 📋 Table of Contents
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Usage](#-usage)
- [Project Structure](#-project-structure)
- [License](#-license)

## ✨ Features

The application is divided into three main user modules:

### 👤 Customer
- **Product Browsing**: View vegetables and fruits with details (price, stock, images).
- **Shopping Cart**: Add items, adjust quantities, and view real-time totals.
- **Order Placement**: Select delivery preferences, use coupons, and place orders.
- **Order Tracking**: Track order status (Pending, Selected, Delivered) with visual timelines.
- **Chatbot Support**: AI-powered assistant for common queries.

### 🚚 Carrier
- **Job Board**: View available orders ready for delivery.
- **My Tasks**: Manage accepted deliveries and mark them as delivered.
- **Communication**: Chat with customers/owners regarding specific orders.
- **Route Planning**: View delivery addresses (simulated map integration).

### 🏪 Store Owner
- **Dashboard**: Overview of sales, active orders, and inventory status.
- **Inventory Management**: Add, update, or remove products; manage stock levels.
- **Staff Management**: Manage carrier accounts and performance.
- **Analytics**: Generate reports and view sales charts.
- **Promotions**: Create and manage coupons and loyalty programs.

## 🛠 Technology Stack

- **Language**: Java 17
- **UI Framework**: JavaFX 21
- **Styling**: MaterialFX (Material Design components), Ikonli (Icons), CSS
- **Database**: MySQL 8.2.0
- **Build Tool**: Maven
- **PDF Generation**: iText 7.2.5 (for invoices)
- **Security**: Argon2id for password hashing

## ⚙️ Prerequisites

Ensure you have the following installed on your system:
- **Java Development Kit (JDK) 17** or higher
- **Maven** 3.8+
- **MySQL Server** 8.0+

## 🚀 Installation & Setup

1.  **Clone the Repository**
    ```bash
    git clone <repository-url>
    cd MergedProject
    ```

2.  **Database Configuration**
    The application connects to a MySQL database named `greengrocer_db`. The default configuration expects:
    - **Host**: localhost:3306
    - **User**: `myuser`
    - **Password**: `1234`

    You can automatically set up the database and user by running the provided SQL script:
    ```bash
    mysql -u root -p < db_init.sql
    ```
    *Note: The script creates the database, tables, and the default user with necessary privileges.*

3.  **Build the Application**
    ```bash
    mvn clean install
    ```

4.  **Run the Application**
    ```bash
    mvn javafx:run
    ```

## 🎮 Usage

You can log in with the following demo credentials (defined in `db_init.sql`):

| Role | Username | Password |
|------|----------|----------|
| **Customer** | `cust` | `1234` |
| **Carrier** | `carr` | `1234` |
| **Owner** | `own` | `1234` |

*Additional sample users (`customer1`, `carrier1`, etc.) are also available.*

## 📂 Project Structure

```
src/main/java/com/greengrocer/
├── controllers/    # JavaFX Controllers handling UI logic
├── dao/            # Data Access Objects for database interaction
├── models/         # POJOs representing database entities
├── utils/          # Helper classes (hashing, session, PDF gen)
└── Main.java       # Application entry point
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
