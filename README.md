# README

[![Java CI with build script](https://github.com/mcgivrer/DemoApp01/actions/workflows/main.yml/badge.svg)](https://github.com/mcgivrer/DemoApp01/actions/workflows/main.yml) [![CodeQL](https://github.com/mcgivrer/DemoApp01/actions/workflows/codeql.yml/badge.svg)](https://github.com/mcgivrer/DemoApp01/actions/workflows/codeql.yml)

## Overview

DemoApp01 is a modular Java demo application focused on game/physics concepts, featuring scene management, entities, behaviors, and graphical rendering. It highlights an extensible and configurable architecture.

## What's New

- Entity-Component-Behavior architecture (simplified ECS)
- Dynamic scene management (e.g., `DemoScene`)
- Rendering system with plugins (layers, debug, world, objects...)
- Simple physics engine (gravity, materials)
- Advanced keyboard input management (`InputHandler`)
- External configuration system (properties files)
- Internationalization (i18n/messages)
- Debug mode and multiple log levels

## Project Structure

- `src/main/java/core/`: application core (App, entities, behaviors, physics engine, renderer, utils)
- `src/main/java/demo/scenes/`: demo scenes
- `src/main/resources/`: resources, configuration, i18n
- `src/test/`: test resources and configuration

## Configuration

The file `src/main/resources/config.properties` allows you to configure:

- debug, mode, timeout, window size, available scenes, gravity, etc.

Example:

```properties
debug=2
mode=DEVELOPMENT
timeout=200
winsize=800x600
scenes=demo:demo.scenes.DemoScene
defaultscene=demo
physics.gravity=9.81
```

## Main Entities

- **App**: entry point, lifecycle, services, configuration
- **Scene**: scene and entity manager
- **Entity/GameObject/World/Camera**: base entities, world objects, camera
- **Behavior**: attachable behaviors (gravity, player input, velocity, etc.)
- **Renderer**: graphics engine, layers and plugins management
- **InputHandler**: keyboard input management, debug shortcuts

## Building the Project

### Prerequisites

- Java 17+
- sdkman (for Java version management)

### Build the JAR

```bash
sdk env use
chmod +x ./build
./build
```

### Run tests

```bash
./build test
```

### Run the project

```bash
./build run debug=2
```

> **Note**
> You can pass as many arguments as needed after `run`:
>
> - `debug`=[1 to 5]
> - `mode`=[DEVELOPMENT,TESTING,PRODUCTION]
> - `timeout`=[milliseconds]

#### Program help

```bash
./build run help
```

**Help output:**

```
Usage: java -jar app.jar [key=value]...
Available options:
 debug=<level>       Set debug level (0=none, 1=some, 2=verbose)
 mode=<mode>         Set application mode (DEVELOPMENT, TESTING, PRODUCTION)
 winsize=<WxH>       Set window size (e.g., 800x600)
 help                Show this help message
```

#### Run directly from JAR

```bash
java -jar target/build/DemoApp01-0.0.1.jar debug=2
```

## Usage

- Keyboard controls the player entity and allows debug level adjustment (Ctrl+D).
- `ESC` exits the application.
- Debug mode displays extra information on screen.

## Internationalization

Messages are externalized in `src/main/resources/i18n/messages.properties`.

## Contribution

All contributions are welcome!

---
Frédéric Delorme <frederic.delorme@gmail.com>
