# GravitLauncher Example

This is a simple Java Swing launcher for Minecraft modpacks inspired by GravitLauncher.

## Building

Use Gradle to build a self-contained JAR with all dependencies:

```bash
gradle shadowJar
```

The resulting runnable JAR will be at `build/libs/gravit-launcher-all.jar`.

## Running

Execute the launcher using the packaged JAR:

```bash
java -jar build/libs/gravit-launcher-all.jar
```

A GUI will open allowing you to log in, prepare a modpack instance and launch the game.
