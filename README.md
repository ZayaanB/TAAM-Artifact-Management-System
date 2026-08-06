# TAAM Artifact Management System

Android app for browsing, searching, and managing the TAAM artifact collection. Users can explore artifacts, like and save them, and leave comments; admins can add, edit, and delete artifacts, moderate comments, and manage other admins.

Demo Link:
https://drive.google.com/file/d/1KYRwgma0v-SkNNz24oVE3xuI9o6dz0K6/view?usp=sharing

Work Log:
https://docs.google.com/document/d/1r8RXQWJgK_IKUnG3j0GN0yBo20hQQwgygyzGeS_kiw8/edit?usp=sharing

## Demo

https://github.com/user-attachments/assets/5c57a6ac-ffb6-4fd2-8419-71d78008bd78


## Features

- Email/password authentication with Firebase (stay signed in)
- Browse, search, and filter artifacts with pagination
- Artifact detail view with related artifacts
- Comments with likes and replies
- Like and save artifacts; view them in your profile
- Admin tools: manage artifacts, manage admins, and moderate comments
- Artifact image uploads to Supabase Storage

## Getting started

1. Clone the repo and open it in Android Studio:
   ```bash
   git clone https://github.com/ZayaanB/TAAM-Artifact-Management-System.git
   ```
2. Add your Firebase config: place `app/google-services.json` in the `app/` folder (download it from your Firebase project settings; this file is gitignored).
3. Configure Supabase: set the URL, anon key, and bucket name in `app/src/main/res/values/strings.xml`.
4. Sync Gradle and run the app on an emulator or device (min SDK 24).

### Test accounts

| Role    | Email            | Password      |
|---------|------------------|---------------|
| Admin   | `test@email.com` | `password123` |
| Manager | `z@email.com`    | `test123`     |
| User    | `ryan@email.com` | `password123` |

New accounts default to the user role. Use the Manager account to promote or demote admins.

## Running tests

```bash
./gradlew :app:testDebugUnitTest
```

## Tech stack

- Java 11 · Android (min SDK 24) · Gradle (Kotlin DSL)
- Firebase Authentication & Realtime Database
- Supabase Storage (artifact images)
- Glide, OkHttp, Material Components, AndroidX
- JUnit & Mockito for unit tests

## Project structure

```
app/src/main/java/com/example/b07taam2026/
├── auth/     Login/signup screens and presenters
├── ui/       Activities and fragments
├── adapter/  RecyclerView adapters
├── model/    Data models
├── data/     Managers, image uploads, preferences
└── util/     Filtering helpers
```
