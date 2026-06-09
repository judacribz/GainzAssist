# Koin Dependency Injection Guide

- Use Koin DSL modules for now (`module { ... }`).
- Do not use annotations yet (no `@Single`, etc.).
- Keep modules split logically by core/data/domain/presentation (`CoreModule.kt`, `DataModule.kt`, etc.).
- Bind interfaces to implementations where possible to decouple abstraction from details.
- Avoid injecting Android `Context` directly into the `domain` or `usecases` layers to keep them platform-agnostic.
- If platform-specific APIs are needed (like SharedPreferences or Android resources), keep them in the `data` layer (the implementation), and abstract them behind an interface in the `domain` layer.
