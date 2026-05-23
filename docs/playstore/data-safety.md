# Play Console Data Safety Checklist

**TODO(owner):** Verify all information in the Google Play Console before submission. Do not guess exact answers if uncertain.

## 1. Account Info (Email / User ID)
* **Is it collected?** Yes
* **Why it is collected?** App functionality, Account management
* **Is it shared with third parties/SDK providers?** Yes (Firebase Auth, Google Sign-In, Facebook Login)
* **Is it required for core app functionality?** Yes
* **Can the user request deletion?** TODO(owner): Confirm if user can request deletion in-app or via a web link.

## 2. User-generated fitness/workout data
* **Is it collected?** Yes
* **Why it is collected?** App functionality (Tracking workouts/fitness data)
* **Is it shared with third parties/SDK providers?** Yes (Firebase Realtime Database for cloud syncing)
* **Is it required for core app functionality?** Yes
* **Can the user request deletion?** TODO(owner): Confirm deletion process.

## 3. App activity/analytics
* **Is it collected?** Yes (via Firebase Analytics)
* **Why it is collected?** Analytics, App performance
* **Is it shared with third parties/SDK providers?** Yes (Firebase/Google)
* **Is it required for core app functionality?** No (Analytics are typically optional for core functionality)
* **Can the user request deletion?** TODO(owner): Confirm Analytics data retention and deletion policies.

## 4. Device/app diagnostics (Crashes/Analytics)
* **Is it collected?** Yes (via Firebase Analytics / Google SDKs)
* **Why it is collected?** Analytics, App performance, Diagnostics
* **Is it shared with third parties/SDK providers?** Yes (Firebase/Google)
* **Is it required for core app functionality?** No
* **Can the user request deletion?** TODO(owner): Confirm data retention and deletion policies.

## 5. Third-Party SDKs Usage

### Firebase Auth
* **Data collected/shared:** Account Info (Email, User ID)
* **Purpose:** Authentication, Account management
* **Required:** Yes

### Firebase Realtime Database
* **Data collected/shared:** User-generated fitness/workout data
* **Purpose:** Cloud sync, App functionality
* **Required:** Yes

### Firebase Analytics
* **Data collected/shared:** App activity, Device/app diagnostics
* **Purpose:** Analytics
* **Required:** No

### Google Sign-In
* **Data collected/shared:** Account Info (Email, User ID)
* **Purpose:** Authentication
* **Required:** Yes

### Facebook Login
* **Data collected/shared:** Account Info (Email, User ID)
* **Purpose:** Authentication
* **Required:** Yes

### YouTube API
* **Data collected/shared:** App activity (Video views)
* **Purpose:** App functionality (Displaying workout videos)
* **Required:** Yes
* **TODO(owner):** Confirm specific YouTube API terms of service regarding data collection and sharing.
