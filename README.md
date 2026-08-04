# TAAM Artifact Management System

Android application for managing and viewing artifacts from the TAAM collection. Users can browse, search, and save artifacts, leave comments and likes, and manage their profile, while admins get tools to manage artifacts and other admins.

## Features

- **Authentication** — email/password sign up and login (Firebase Auth)
- **Artifact browsing** — view, filter, and search artifacts in the collection, with pagination
- **Artifact details** — dedicated detail view with related artifacts
- **Comments & likes** — comment on artifacts and like/save them for later
- **User profiles** — view and manage account info
- **Admin tools** — manage artifacts (add/edit/remove) and manage admin users, gated by role
- **Image uploads** — artifact images uploaded to Supabase storage

## Tech stack

- **Language:** Java 11
- **Platform:** Android (min SDK 24, target/compile SDK 34)
- **Build:** Gradle (Kotlin DSL), AGP 9.2.1
- **Backend:**
  - [Firebase Realtime Database](https://firebase.google.com/docs/database) — artifacts, users, roles, comments, likes
  - [Firebase Authentication](https://firebase.google.com/docs/auth) — user sign up/login
  - [Supabase Storage](https://supabase.com/docs/guides/storage) — artifact image hosting
- **Libraries:** AndroidX (AppCompat, Material, ConstraintLayout, RecyclerView, Activity KTX), [Glide](https://github.com/bumptech/glide) (image loading), [OkHttp](https://square.github.io/okhttp/) (Supabase REST calls)
- **Testing:** JUnit, Mockito, Robolectric, AndroidX Test, Espresso

## Project structure

```
app/src/main/java/com/example/b07taam2026/
├── LoginPage.java / SignUpPage.java          # Auth screens
├── AuthManager.java / SignUpManager.java     # Firebase Auth wrappers
├── HomeActivity.java                         # Main artifact browsing screen
├── Artifact.java / ArtifactAdapter.java       # Artifact model + list rendering
├── ArtifactManager.java / ArtifactFilter.java # Artifact data + filtering/search
├── ArtifactDetailFragment.java                # Artifact detail view
├── RelatedArtifactAdapter.java                # Related artifacts list
├── Comment.java / CommentAdapter.java / CommentManager.java  # Comments on saved artifacts
├── LikeManager.java / SaveManager.java        # Likes and saved artifacts
├── ProfileActivity.java                       # User profile screen
├── RoleManager.java / AdminManager.java / AdminFilter.java   # Role/admin logic
├── ManageArtifactsActivity.java / ManageArtifactAdapter.java # Admin: manage artifacts
├── ManageAdminsActivity.java / ManageAdminsAdapter.java      # Admin: manage admins
├── SupabaseImageUploader.java                 # Uploads images to Supabase storage
├── PaginationPrefs.java                       # Pagination settings/state
└── User.java / UserDebugActivity.java / UserDebugAdapter.java # User model + debug screen
```

## Getting started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (recent stable version)
- JDK 11+
- A Firebase project with **Realtime Database** and **Authentication (Email/Password)** enabled
- A Supabase project with a **Storage** bucket for artifact images

### Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/ZayaanB/TAAM-Artifact-Management-System.git
   ```
2. Open the project in Android Studio.
3. Add your Firebase config file at `app/google-services.json` (download it from your Firebase project settings — this file is gitignored and required for the `google-services` Gradle plugin to work).
4. Configure Supabase credentials (URL, anon key, bucket name) used by `SupabaseImageUploader`.
5. Sync Gradle and run the app on an emulator or device (min SDK 24 / Android 7.0+).

### Running tests

```bash
./gradlew test                    # unit tests (JUnit, Mockito, Robolectric)
```
