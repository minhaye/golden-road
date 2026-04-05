# Golden Road (LibGDX)

A 2D dungeon-style game project for OOP, migrated to a LibGDX multi-module structure.

## Tech Stack

- Language: Java 17
- Engine: LibGDX 1.12.1
- Desktop backend: LWJGL3
- Build tool: Gradle (multi-module)

## Project Structure

- `core/`: game logic and rendering code shared across platforms
- `lwjgl3/`: desktop launcher (LWJGL3 backend)
- `src/`: legacy Java Swing prototype code kept for reference

Current runtime entry points:

- LibGDX game class: `core/src/main/java/goldenroad/GoldenRoadGame.java`
- Desktop launcher: `lwjgl3/src/main/java/goldenroad/lwjgl3/Lwjgl3Launcher.java`

## Build and Run

Use Gradle Wrapper (recommended):

```bash
./gradlew :core:build
./gradlew :lwjgl3:run
```

On Windows PowerShell:

```powershell
.\gradlew.bat :core:build
.\gradlew.bat :lwjgl3:run
```

## Notes

- The legacy Swing code under `src/` is not part of the LibGDX runtime modules.
- Primary gameplay screen for LibGDX is implemented in `PlayScreen`.
