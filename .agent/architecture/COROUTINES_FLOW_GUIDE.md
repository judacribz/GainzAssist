# Coroutines and Flow Guide

- Use `suspend` functions for one-shot work (e.g., simple network request or database write).
- Use `Flow` for streams of data that change over time (e.g., room database observing).
- Use `StateFlow` for exposing UI state from ViewModel to Compose.
- Use `SharedFlow` or `Channel` for one-off events (like showing a snackbar or navigating).
- Inject `DispatcherProvider` instead of hardcoding dispatchers.
- Do not hardcode `Dispatchers.IO` in new repository/usecase code. Pass the injected dispatcher instead.
- No RxJava anywhere in the new architecture.
- Avoid callback-based async in new code. Bridge existing callbacks to Coroutines using `suspendCancellableCoroutine` or `callbackFlow` if needed.
