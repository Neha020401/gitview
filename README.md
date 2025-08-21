
---

# 🔭 GitView

GitView is a collaborative web platform that lets developers **preview and compare Git branches live** without fetching code locally. It automates the process of building and serving branch-specific previews, making it easier for teams to test, review, and validate changes before merging.

---

## ✨ Features

* 🔌 Live previews of different Git branches.
* 🖥️ Backend powered by **Spring Boot** with **JGit** for repository management.
* 🎨 Frontend interface for browsing available previews and comparing changes.
* 🔄 Real-time updates whenever a new branch is pushed.

---

## 🛠️ Tech Stack

**Backend:**

* ☕ Java 21
* 🌱 Spring Boot (REST APIs, service layer)
* 📂 JGit (Git repository interaction)

**Frontend:**

* ⚛️ React + Tailwind CSS (UI components)

**Other Tools:**

* 📦 Maven (build tool)

---

## ⚙️ How It Works

1. 👩‍💻 A new branch is created or updated in the Git repository.
2. 🛠️ The backend uses **JGit** to detect and fetch branch changes.
3. 🗂️ The **Spring Boot backend** manages active previews (branch name, port, URL).
4. 🌐 The **React frontend** fetches preview metadata from the backend and displays it in a dashboard.
5. 🚀 Team members can click the preview link to open a **live environment** of that branch.

---

## 🏗️ Project Setup

### 1️⃣ Clone Repository

```bash
git clone https://github.com/your-username/gitview.git
cd gitview
```

---

### 2️⃣ Backend Setup (Spring Boot)

```bash
cd gitLive
mvn clean install
mvn spring-boot:run
```

📍 Runs at: `http://localhost:8080`

---

### 3️⃣ Frontend Setup (React)

```bash
cd gitview-frontend
npm install
npm run dev
```

📍 Runs at: `http://localhost:5173`

---

## 📡 API Documentation

The backend provides REST APIs to manage and retrieve branch previews.

**Base URL:**

```
http://localhost:8080/api/previews
```

### Endpoints:

* 📥 **Get All Previews**
  `GET /api/previews`

* ➕ **Add a New Preview**
  `POST /api/previews`

  ```json
  {
    "branchName": "feature/dashboard",
    "port": 3003
  }
  ```

* 🔍 **Get Preview by Branch**
  `GET /api/previews/{branchName}`

* ❌ **Delete a Preview**
  `DELETE /api/previews/{branchName}`

---

## 📸 Example Workflow

1. 👩‍💻 Push or update `feature/login-ui` branch.
2. 📡 Backend registers a preview → `http://localhost:3001`.
3. 🌐 Frontend dashboard shows new card:

   * Branch: `feature/login-ui`
   * Preview Link: [Open](http://localhost:3001)

---

## 🚧 Future Improvements

* 🔐 Authentication for preview access.
* 📦 Support for multiple repositories.
* 🗄️ Database storage of preview metadata.
* 🤝 Integration with GitHub/GitLab API for PR/MR previews.

---

## 📜 License

📝 Licensed under the MIT License.

---
