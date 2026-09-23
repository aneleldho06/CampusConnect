# CampusChat 💬

A simple **Java-based real-time chat application** built using **Java Swing** and **Socket Programming**.

## Features

* 🔐 User Login
* 💬 Real-time messaging
* 👥 Multiple users
* 🟢 Online user list
* 🔒 Private messaging
* 🖥️ Java Swing GUI
* 🌐 Client–Server architecture

## Tech Stack

* **Java**
* **Java Swing** — GUI
* **Java Sockets** — Networking
* **OOP Concepts**
* **Multithreading**

## Project Structure

```text
src/com/campuschat/
│
├── client/
│   ├── ChatClient.java
│   ├── ChatClientGUI.java
│   └── LoginScreen.java
│
├── model/
│   └── User.java
│
├── server/
│   ├── ChatServer.java
│   └── ClientHandler.java
│
└── Main.java
```

## How to Run

### 1. Compile

From the project root:

```bash
javac -d out $(find src -name "*.java")
```

### 2. Start the application

```bash
java -cp out com.campuschat.Main
```

The server will start on:

```text
Port: 5000
```

Two login windows will open for testing.

### Demo Accounts

```text
Username: user1
Password: pass123

Username: user2
Password: pass123
```

Login with different accounts in the two windows to test chatting.

## Architecture

```text
        Client 1
           │
           │
        Client 2
           │
           ▼
      Chat Server
           │
     ┌─────┴─────┐
     │           │
ClientHandler  ClientHandler
     │           │
   User 1      User 2
```

## OOP Concepts Used

* Classes & Objects
* Encapsulation
* Constructors
* Inheritance
* Interfaces
* Polymorphism
* Exception Handling
* Multithreading

## Project

**CampusChat** is an academic OOP project demonstrating how a Java client-server chat application can be built using **Swing, sockets, and object-oriented programming**.
