# 🌿 EcoLearn – Gamified Environmental Learning Platform
### Java Swing + MySQL Desktop Application
**Converted from:** MERN Stack (GamEd project) → Java Swing + JDBC + MySQL

---

## 📁 Project Structure
```
EcoLearnSwing/
├── src/
│   ├── main/MainApp.java          ← Entry point
│   ├── ui/                        ← All Swing frames (View)
│   │   ├── LoginFrame.java
│   │   ├── RegisterFrame.java
│   │   ├── DashboardFrame.java
│   │   ├── QuizFrame.java
│   │   ├── LeaderboardFrame.java
│   │   └── AdminFrame.java
│   ├── controller/                ← Event handlers (Controller)
│   │   ├── AuthController.java
│   │   ├── QuizController.java
│   │   └── UserController.java
│   ├── service/                   ← Business logic (Service)
│   │   ├── AuthService.java
│   │   ├── QuizService.java
│   │   └── UserService.java
│   ├── dao/                       ← Database queries (DAO)
│   │   ├── UserDAO.java
│   │   ├── QuizDAO.java
│   │   └── ScoreDAO.java
│   ├── model/                     ← POJO classes (Model)
│   │   ├── User.java
│   │   ├── Question.java
│   │   └── Score.java
│   ├── db/DBConnection.java       ← JDBC singleton
│   └── utils/
│       ├── Constants.java         ← Colours, fonts, config
│       └── Validator.java         ← Input validation
├── database/schema.sql            ← MySQL schema + seed data
├── lib/                           ← Put mysql-connector here
└── README.md
```

---

## ⚙️ Prerequisites
| Requirement | Version |
|---|---|
| Java JDK | 11 or higher |
| MySQL Server | 8.0+ |
| MySQL Connector/J | 8.x |

---

## 🚀 Setup Guide (Step-by-Step)

### Step 1 – Download MySQL Connector
1. Go to: https://dev.mysql.com/downloads/connector/j/
2. Download **"Platform Independent"** ZIP
3. Extract and copy **`mysql-connector-java-8.x.x.jar`** into the `lib/` folder

### Step 2 – Set Up Database
Open MySQL Workbench or CLI and run:
```sql
SOURCE /path/to/EcoLearnSwing/database/schema.sql;
```
Or paste the contents of `database/schema.sql` directly.

### Step 3 – Configure DB Connection
Open `src/db/DBConnection.java` and update if needed:
```java
private static final String DB_URL  = "jdbc:mysql://localhost:3306/ecolearn_db?...";
private static final String DB_USER = "root";
private static final String DB_PASS = "root";   // ← your MySQL password
```

### Step 4 – Compile
From the `EcoLearnSwing/` root directory:

**Windows:**
```cmd
javac -cp "lib\mysql-connector-java-8.x.x.jar" -d out ^
  src\main\MainApp.java ^
  src\ui\*.java ^
  src\controller\*.java ^
  src\service\*.java ^
  src\dao\*.java ^
  src\model\*.java ^
  src\db\*.java ^
  src\utils\*.java
```

**Linux / macOS:**
```bash
javac -cp "lib/mysql-connector-java-8.x.x.jar" -d out \
  src/main/MainApp.java \
  src/ui/*.java \
  src/controller/*.java \
  src/service/*.java \
  src/dao/*.java \
  src/model/*.java \
  src/db/*.java \
  src/utils/*.java
```

### Step 5 – Run
**Windows:**
```cmd
java -cp "out;lib\mysql-connector-java-8.x.x.jar" main.MainApp
```
**Linux / macOS:**
```bash
java -cp "out:lib/mysql-connector-java-8.x.x.jar" main.MainApp
```

---

## 🔑 Default Login Credentials
| Role | Email | Password |
|---|---|---|
| Admin | admin@ecolearn.com | admin123 |

---

## 🎮 Features
| Feature | Description |
|---|---|
| 🔐 Authentication | Register / Login with SHA-256 hashed passwords |
| 🏠 Dashboard | User stats (points, level, quizzes done), quiz list |
| 📝 Quiz System | MCQ questions loaded from MySQL, radio button UI |
| ⏱ Timer | 10-minute countdown, auto-submit on expiry |
| ⭐ Points & Levels | Earn points per correct answer; level up every 100 pts |
| 🏆 Leaderboard | Global ranking sorted by total points (JTable) |
| ⚙ Admin Panel | Add quizzes + questions; view and delete users |

---

## 🏗️ Architecture (MERN → Java Swing Mapping)

| MERN Layer | Java Layer |
|---|---|
| MongoDB Model | MySQL Table + POJO (`model/`) |
| Express Route | Controller method (`controller/`) |
| Express Controller | Service method (`service/`) |
| Mongoose Query | DAO PreparedStatement (`dao/`) |
| React Component | JFrame / JPanel (`ui/`) |
| JWT Auth | Session-scoped User object |

---

## 🎨 Theme
Dark eco-green theme with:
- Background: `#121212`
- Card: `#1E1E1E`
- Primary: Forest Green `#228B22`
- Accent: Amber `#FFC107`

---

## 🛠 Troubleshooting

**"MySQL JDBC Driver not found"**
→ Ensure `mysql-connector-java.jar` is in `lib/` and on the classpath.

**"Access denied for user 'root'"**
→ Check `DB_USER`/`DB_PASS` in `DBConnection.java`.

**"Unknown database ecolearn_db"**
→ Run `database/schema.sql` first.

**UI looks plain/ugly**
→ Nimbus L&F may not be available. The dark theme still applies via custom painting.
