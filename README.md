# Reflact Engine

The core RPG library built on Minestom. This module handles the "heavy lifting" of the game logic, separating it from the specific server implementation details.

## Features

*   **RPG Mechanics:** Attributes, Spells, Item Management.
*   **Player Data:** handling of `ReflactPlayer` data.
*   **Networking:** Server-side packet handling.

## Build

This module is a dependency for the `server`.

```bash
./gradlew publishToMavenLocal
```
