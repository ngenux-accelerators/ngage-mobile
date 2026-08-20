# GitHub Copilot Instructions

This is a Kotlin Android app using Jetpack Compose, Amazon IVS (real-time stage + chat), AWS Amplify (Cognito auth), Retrofit + OkHttp, and Coroutines. Follow all rules below on every interaction.

---

## 1. Reuse Before You Create

Before writing any new code, check:
- Does a utility, extension, or helper already exist in this repo?
- Can a Kotlin stdlib function, Compose built-in, or Android framework API solve this?
- Can an already-installed dependency handle it?

**Key existing patterns to reuse — do not duplicate them:**
- `launchMain` / `launchDefault` in `core/common/CoroutineExtensions.kt` — use these for all coroutine launches; never create new `CoroutineScope`s ad hoc.
- `NetworkHandler` in `core/handlers/networking/NetworkHandler.kt` — all HTTP calls go through here via the configured Retrofit/OkHttp client.
- `AuthHandler` — all auth state (`user`, `isLoading`, `isError`) and Amplify/Cognito operations live here.
- `StageHandler` / `StageWrapper` / `StageRendererWrapper` — all IVS stage lifecycle, media controls, and participant state.
- `ChatHandler` — IVS chat messaging.
- `NavigationHandler` — all in-app navigation and error display.
- `Const.kt` — add new shared constants here, never inline magic values.
- `ui/components/` — reuse `Buttons`, `TextInput`, `Containers`, `Modifiers`, `LoadingSpinner`, `ErrorBarContent`, `ParticipantView`, etc. before writing new UI primitives.

---

## 2. Dependency Discipline

- Do **not** add a new Gradle dependency unless:
  1. The functionality is non-trivial,
  2. It cannot reasonably be done with an existing dep or stdlib, and
  3. It clearly improves reliability, security, or maintainability.
- If a new dependency is required, propose it explicitly in a comment with justification, referencing `gradle/libs.versions.toml`.
- Already available: Compose BOM, Material3, Retrofit, OkHttp, Kotlinx Serialization, Coil, Timber, AWS Amplify, Amazon IVS Broadcast + Chat SDKs, Navigation Compose, Lifecycle/ViewModel KTX, Window. Prefer these.

---

## 3. Production-Grade Code Quality

- Functions must be **small and single-responsibility**. If a function needs a comment to explain what it does, split it.
- Use **Timber** for all logging (`Timber.d`, `Timber.w`, `Timber.e`). Never use `Log.*` or `println`.
- **Always handle errors explicitly.** Use the `CoroutineExceptionHandler` already wired into `launchMain`/`launchDefault`. Surface user-facing errors via `NavigationHandler.showError(R.string.*)`.
- Use `MutableStateFlow` + `asStateFlow()` for all observable state, following the pattern in `AuthHandler` and `StageHandler`.
- No magic numbers or strings. Define constants in `Const.kt` or a relevant `companion object`.
- Prefer `sealed class` or `enum class` for finite states (see `StageType`, `Destination`).

---

## 4. DRY and Reusability

- Extract repeated UI or logic into an existing component or helper before creating a new one.
- When adding new screens, follow the pattern in `ui/screens/` (stateless composable + state hoisted from handler objects).
- When adding new bottom sheets, follow `ui/bottomsheets/BottomSheet.kt`.
- Compose Modifier chains belong in `ui/components/Modifiers.kt` if used in more than one place.
- Do **not** create a new abstraction used in only one place. Inline it.

---

## 5. Minimalism — No Crap Code

- No "just in case" parameters, types, or wrapper classes.
- No unused imports, variables, or commented-out code.
- No layers of indirection unless they demonstrably reduce duplication or complexity.
- If a simpler, direct solution works, prefer it over a "scalable" design that is not needed now.
- Dead code must be removed, not worked around.

---

## 6. Consistency with Existing Patterns

- **Package structure:** `core/common` (utils/extensions), `core/handlers` (business logic, handlers), `core/handlers/networking` (API), `core/handlers/stage` (IVS stage), `ui/screens`, `ui/components`, `ui/bottomsheets`.
- **Naming:** Handler objects are `object`s (singletons). UI files are named after what they render (`StageScreen`, `Buttons`, `TextInput`). Follow this exactly.
- **Imports:** Use alias-free imports. Match the style of the file you're editing.
- **State exposure:** Private `MutableStateFlow` backing field + public `asStateFlow()`. Never expose mutable state directly.
- When unsure about style, read 2–3 nearby files and match them exactly.

---

## Behavioral Rules

- Before proposing new code, mentally verify no equivalent already exists in `core/common`, `core/handlers`, or `ui/components`.
- If your suggestion reuses an existing pattern, briefly note which file it follows.
- If a request would introduce a new dependency or duplicate a helper, propose the simpler reuse-based alternative first and explain why.
- Never suggest switching architectural patterns (e.g., adding Hilt/DI, moving to ViewModel) — the project uses singleton handler objects intentionally.
