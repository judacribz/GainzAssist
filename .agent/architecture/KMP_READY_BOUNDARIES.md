# Kotlin Multiplatform (KMP) Ready Boundaries Guide

- Domain models, usecases, and repository interfaces should avoid Android imports (`android.*`, `androidx.*`) where possible.
- Android-specific code stays in the `app/data` implementation layer for now.
- Do not move code to a separate KMP module yet. The goal is to prepare names, layers, and dependencies so future shared module extraction is easy.
- Koin is chosen because it easily supports KMP via platform modules and `expect`/`actual` patterns. Keep DI definitions modular so they can be ported smoothly.
