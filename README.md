# 🎓 College ERP System

A Java-based College ERP (Enterprise Resource Planning) system designed to manage students, courses, enrollments, and user authentication efficiently.

---

## 🚀 Features

* 🔐 Secure Login System with Password Hashing (SHA-256)
* 👨‍🎓 Student Management
* 📚 Course Management
* 🧑‍🏫 Instructor Module
* 📝 Enrollment System
* 📊 Grade Management
* ⛔ Login Blocking after Multiple Failed Attempts
* 🔄 Password Change Functionality

---

## 🛠️ Tech Stack

* **Language:** Java
* **UI:** Java Swing
* **Database:** CSV Files
* **Security:** SHA-256 Password Hashing

---

## 📁 Project Structure

```
college-erp-system/
│── src/                # Java source code
│── csv's               # CSV files (users, courses, etc.)
│── docs/               # Report and UML diagrams
│── demo/               # Demo video
│── README.md
│── .gitignore
```

---

## ⚙️ How to Run

1. Open the project in IntelliJ IDEA or Eclipse
2. Make sure JDK (Java 17 or above) is installed
3. Navigate to:

   ```
   src/edu/univ/erp/Main.java
   ```
4. Run the file

---

## 🔐 Authentication Details

* Passwords are securely stored using hashing
* Login system validates credentials using hashed comparison
* System blocks login after multiple failed attempts

---

## 🎥 Demo

👉 https://drive.google.com/drive/folders/1j1MLZPjiqqACD7JxwpPno3YAZIOGSHBR?usp=sharing

---

## 📄 Documentation

* 📘 Project Report: Available in `docs/`
* 📊 UML Diagram: Included in `docs/`

---

## 🧪 Project Status

✔ Fully functional and tested
✔ Working authentication system
✔ Modular and structured codebase

---

## 💡 Future Improvements

* 🌐 Convert to Web Application
* 🗄️ Replace CSV with Database (MySQL / PostgreSQL)
* 🔐 Add JWT / Session-based Authentication
* 📱 Build Mobile Version

---

## 👨‍💻 Author

**Rahul IIITD**

---

## ⭐ Acknowledgement

This project was developed as part of academic learning and demonstrates core concepts of software design, authentication, and data management.
