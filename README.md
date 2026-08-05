# TAAM Artifact Management System

Android application for managing and viewing artifacts from the TAAM collection. Users can browse, search, and save artifacts, leave comments and likes, and manage their profile, while admins get tools to manage artifacts and other admins.

## Features

- **Authentication**: email/password sign up and login and stay signed in feature (Firebase Auth)
- **Artifact browsing**: view, filter, and search artifacts in the collection, with pagination
- **Artifact details**: dedicated detail view with related artifacts (description, date, materials, etc.)
- **Comments & likes**: comment on artifacts and like/save them for later and reply to others comments
- **User profiles**: full login flow, view and interact with artifacts. View liked and saved artifacts
- **Admin tools**: manage artifacts (add/edit/remove) and manage admin users (manager exclusive)
- **Admin moderation**: manage comments on artifacts and delete innappropriate comments
- **Image uploads**: artifact images uploaded to Supabase storage and deleted on artifact deletion

## Getting started

### Test accounts

For testing/grading, the following accounts can be used to sign in:

| Role  | Email                | Password      | What it can do |
|-------|----------------------|----------------|-----------------|
| Admin | `test@email.com`     | `password123`          | Full admin access, manage artifacts |
| Manager (super admin) | `z@email.com`         | `test123`      | Full admin access, manage artifacts and admins |
| User  | `ryan@email.com`      | `password123`  | Browse, search, save, like, and comment on artifacts |

*Note you can make your own accounts too and they will default to user. To promote to admin use Manager account to promote and demote.*

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
Run the `ExampleUnitTest` file (`app/src/test/java/com/example/b07taam2026/ExampleUnitTest.java`).
# unit tests (JUnit, Mockito, Robolectric)
```
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
