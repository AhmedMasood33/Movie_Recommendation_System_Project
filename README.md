# Movie Recommendation System - Software Testing Project

## 📖 Overview
This repository contains a Java-based Movie Recommendation System. The application reads user and movie data from two input files, validates the information based on specified rules, and generates a recommendations file listing movie suggestions for each user. A core focus of this project is robust error handling, as the system must stop processing immediately and output only the first encountered error if one is detected.

## ✨ Core Features
* **File Parsing:** Reads and processes data from `movies.txt` and `users.txt`.
* **Strict Data Validation:**
  * **Movies:** Every word in the title must start with a capital letter. IDs must consist of all capital letters from the title followed by three unique digits. Categories must be from an allowed list (e.g., horror, action, drama).
  * **Users:** Usernames may contain only alphabetic characters and spaces, and must not begin with a space. User IDs must be exactly 9 characters, start with numbers, end with at most one alphabetic character, and be unique.
* **Recommendation Engine:** For each user, if they like category X, the system recommends all movies that belong to category X.
* **Error Handling:** The system halts immediately upon detecting any input error and outputs only the first encountered error using exact formatting templates.

## 🛠️ Proposed Tech Stack & Tools
* **Programming Language:** Java
* **Test Automation:** JUnit
* **Test Management:** TestRail
* **Bug Tracking:** Jira
* **Documentation:** PDF

## 📂 Input & Output Formats

### Input Files

**`movies.txt`**
```text
movie_title "," movie_id
category1 "," category2
```

**`user.txt`**
```text
username "," user_id
liked_category1 "," liked_category2...
```

### Output Files

**`output.txt`**
```text
Line 1: "For User:" username "," user_id
Line 2: "{category1}:" movie_id "-" movie_title "," movie_id "-" movie_title, ...
Line 3: "{category2}:" movie_id "-" movie_title "," movie_id "-" movie_title, ...
```
