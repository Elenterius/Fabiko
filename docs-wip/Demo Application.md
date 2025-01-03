# Fabiko Demo Application

If you'd like to see what the Fabiko library does, then a YouTube demonstration video is available of the original
Caliko library:
https://www.youtube.com/watch?v=D9-V66m9DVI

If you'd like to experiment with the demo yourself then you can either download a pre-compiled release
from https://github.com/Elenterius/Fabiko/releases or you can use git to clone the Fabiko repository
at https://github.com/Elenterius/Fabiko and run/build/install the application via **gradle**.

### Requirements

- Linux, FreeBSD, macOS or Windows
- OpenGL 3.3 or newer
- Java 17 or newer

### Gradle

Run via Gradle:

```gradle
gradlew run
```

Install via Gradle:

```gradle
gradlew installDist
```

Once installed the application can be found inside `/demo/build/install/Fabiko-Demo/` and started via the startup
scripts inside the `/Fabiko-Demo/bin` folder.

### Controls

In 2D mode, clicking or holding the left mouse button (LMB) updates the target location to be at the location of the
cursor.

In 3D mode holding the LMB and moving the mouse allows you to look around.

The keyboard controls used in the demo are listed below:

| Input      | Action                                            |
|------------|---------------------------------------------------|
| ↑, ↓       | Toggle 2D/3D mode                                 |
| ←, →       | Previous/Next demo                                |
| C          | Toggle drawing constraints                        |
| X          | Toggle drawing axes (3D)                          |
| W, A, S, D | Move camera forward/back/left/right (3D)          |
| P          | Toggle orthographic / perspective projection (3D) |
| F          | Toggle fixed-base mode                            |
| R          | Rotate base locations (3D)                        |
| M          | Toggle drawing models (3D)                        |
| L          | Toggle drawing lines                              |
| SPACE      | Toggle pausing moving target (3D)                 |
| Esc        | Exit demo                                         |
