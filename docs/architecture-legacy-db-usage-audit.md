# Legacy DB Usage Audit

This document tracks the remaining usages of legacy `WorkoutViewModel` and `WorkoutRepo` components.

## WorkoutViewModel
`WorkoutViewModel` is still actively referenced in the following files:
- `app/src/main/java/ca/gainzassist/activities/main/fragments/Resume.kt`
- `app/src/main/java/ca/gainzassist/activities/main/fragments/Settings.kt`
- `app/src/main/java/ca/gainzassist/activities/main/fragments/Workouts.kt`
- `app/src/main/java/ca/gainzassist/activities/start_workout/fragments/WorkoutScreen.kt`
- `app/src/main/java/ca/gainzassist/util/firebase/Authentication.kt`

**Status**: NOT safe to remove yet. `WorkoutScreen` still uses it for database operations, and `MainTab` fragments rely on it.

## WorkoutRepo
`WorkoutRepo` is still actively referenced in the following files:
- `app/src/main/java/ca/gainzassist/models/db/WorkoutViewModel.kt`
- `app/src/main/java/ca/gainzassist/background/FirebaseService.kt`

`WorkoutRepo` still heavily uses `Thread { ... }` for background operations.

**Status**: NOT safe to remove yet. `FirebaseService` and `WorkoutViewModel` depend on it.
