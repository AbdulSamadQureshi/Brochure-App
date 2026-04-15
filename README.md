# Solution

## Overview

This project is a Rick & Morty character browser built on a clean, scalable Android architecture. It was developed as a coding challenge and covers the full requirements list: browsing a paginated list, viewing character details, favouriting items, searching, sharing, and graceful error/empty-state handling.

---

## Architecture

The app follows **Clean Architecture** split across Gradle modules:

```
:app          — Compose UI, ViewModels, navigation
:domain       — Use-cases, repository interfaces, domain models
:data         — Repository implementations, Room database
:network      — Retrofit client, API service, response models
:core         — Base classes (MviViewModel, BaseUseCase), shared utilities, DataStore
```

### Presentation pattern — MVI

Every screen is driven by a `MviViewModel<State, Intent, Effect>`:

| Type | Role |
|------|------|
| **State** | Immutable snapshot rendered by Compose |
| **Intent** | User actions (load, search, toggle favourite, etc.) |
| **Effect** | One-shot events (snack-bar errors, navigation) |

`setState {}` atomically updates state; `setEffect {}` emits a single event that the UI collects once.

---

## Feature walkthrough

### Character list

- Paginated via the Rick & Morty REST API (page-based).
- Infinite scroll: `LoadNextPage` intent fires when the last visible item is reached.
- A concurrent-request guard (`loadJob?.cancel()`) prevents stale responses from overtaking a newer page load.

### Search

Search is implemented as **local client-side filtering** on the already-fetched page(s):

```kotlin
val filteredCharacters: List<CharacterUi>
    get() = if (searchQuery.isBlank()) characters
            else characters.filter {
                it.name?.contains(searchQuery, ignoreCase = true) == true
            }
```

**Trade-off**: Local filtering is instant (no debounce, no extra network call) and works offline. The downside is that it only searches the pages already loaded — characters on later pages won't appear until paged in. For a production app with a backend that supports full-text search, the intent handler would instead trigger an API call with a debounced query.

### Empty states

Two distinct empty states are shown:

- **No data at all** (`characters` is empty after a successful load) — prompts the user to check back later.
- **No search results** (`filteredCharacters` is empty but `characters` is not) — prompts the user to try a different term.

### Error state with retry

When the API call fails, the error message is shown with a **Retry** button that re-dispatches `LoadCharacters` to attempt the request again.

### Character detail

- Hero image uses `ContentScale.Fit` to display the full image without cropping or upscaling artefacts. The Rick & Morty API returns 300×300 px images; `Crop` would stretch them in the 320 dp hero box and cause visible blur.
- Metadata rows: species, gender, origin, last known location.
- Status chip (Alive / Dead / Unknown) with colour-coded dot.
- Slide-up animation on content card.

### Favourites

Favourites are persisted locally in **Room** (no API involvement). The `imageUrl` is used as the stable key. A single `ToggleFavourite` use-case both inserts and deletes. The `observeFavourites` coroutine in `CharactersViewModel` keeps the list in sync without a full reload.

### Share

Tapping the share icon on the detail screen opens the native Android share sheet with the character name, species, status, and image URL pre-filled as plain text.

---

## Data / field mapping

The Rick & Morty API fields map naturally to a marketplace-style listing:

| API field | UI label |
|-----------|----------|
| `name` | Title / heading |
| `species` + `status` | Description tags |
| `location.name` | Last known location |
| `image` | Cover / hero image |

There is no price field in the API, so none is shown.

---

## Testing

| Layer | Tool | What's tested |
|-------|------|---------------|
| ViewModel | JUnit 4 + Turbine + Coroutines test | State transitions, intent handling |
| Use-cases | JUnit 4 + Mockito | Happy path, error path |
| Network | JUnit 4 + MockWebServer | Auth headers, logging level |
| UI screenshots | Roborazzi + Robolectric | Theme colour regression |

---

## Build instructions

```bash
# Clone
git clone <repo-url>
cd BonialCodingChallenge

# Run all unit tests
./gradlew test

# Run screenshot tests
./gradlew verifyRoborazziDebug

# Install debug APK
./gradlew installDebug
```

Minimum SDK: **24** · Target/Compile SDK: **36** · Kotlin: **2.2** · Compose BOM: **2025.11**

---

## What I would add next

- **Remote search**: debounced API query via a dedicated search endpoint, replacing the local filter for full-catalogue coverage.
- **Offline support**: cache the first page in Room so the list is visible without a network connection.
- **Pagination with Paging 3**: replace the manual page-counter with `Pager` + `LazyPagingItems` for cleaner load-state handling.
- **Analytics / logging**: structured event tracking on list interactions and detail views.
