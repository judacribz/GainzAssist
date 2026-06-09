# Legacy DB Usage Audit

This document tracked the remaining usages of legacy `WorkoutViewModel` and `WorkoutRepo` components.

## Final Status
- **WorkoutViewModel**: Deleted.
- **WorkoutRepo**: Deleted.
- Legacy `AndroidViewModel` / `LiveData` / `Thread` DB wrapper was completely removed.

## Current Architecture
- **Path**: Compose/UI -> feature ViewModels -> use cases -> `WorkoutRepository` -> `RoomWorkoutRepository` -> Room DAOs.
- `FirebaseService` now securely uses `WorkoutRepository` and avoids Firebase write-back loops with `syncToFirebase = false`.

The architecture foundation cleanup is fully complete!
