# Summary of Documentation

## Overview
This documentation covers the architecture and key components of the game engine. Below is a summary of each section:

## 1. Preface
- Introduction to the project documentation
- Next steps: Preface and Main application architecture

## 2. Main Application Architecture
- **App Class**: Central coordinator, manages initialization, orchestration, main loop, configuration, and logging
- **PhysicsEngine**: Handles physics simulation, updates scene and entity states
- **Renderer**: Manages graphical display, uses plugins for rendering different entity types
- **InputHandler**: Captures and processes keyboard inputs
- **Services and Scenes**: Modular components with lifecycle management (initialize, start, stop, update, dispose)

### Key Diagrams
- Package and class diagrams showing component relationships
- Sequence diagram for application startup
- State diagram for application modes (DEVELOPMENT, TESTING, PRODUCTION)

## 3. Entity Class
- **Entity<T>**: Base class for all game objects with position, velocity, behaviors, and materials
- **GameObject**: Extends Entity with sprite and color properties
- **Generic Design**: Allows method chaining and specialized entity types

### Key Features
- Position, size, velocity, and rotation management
- Behavior attachment for modular logic
- Material properties for physical interactions

## 4. Renderer Service
- **Architecture**: Uses Swing with double buffering for optimal performance
- **Plugin System**: Modular rendering via `RenderPlugin` interface
- **Layer Management**: BACKGROUND, MIDGROUND, FOREGROUND, UI, OVERLAY
- **Camera Handling**: Applies camera translation to MIDGROUND and FOREGROUND layers

### Key Methods
- `initialize()`: Configures display window and registers default plugins
- `update()`: Renders the scene with camera translation and debug overlay
- `registerPlugin()`: Adds custom rendering plugins

## 5. Physics Engine
- **PhysicsEngine**: Service for physics simulation, applies behaviors to entities
- **World**: Defines global physics environment (gravity, colors)
- **Material**: Record defining friction and restitution properties (DEFAULT, ICE, RUBBER, etc.)

### Physics Simulation
- Simplified Newtonian laws: position and velocity updates
- Material properties influence collisions and movement
- Supports different environments (planets, space, etc.)

## 6. Camera System
- **Scene**: Manages entities and cameras, handles lifecycle and activation
- **Camera**: Follows target entities with smooth tweening
- **Layer System**: Organizes entities by depth (BACKGROUND, MIDGROUND, FOREGROUND, UI, OVERLAY)

### Key Features
- Camera targeting and smooth following
- Layer-based rendering with z-index sorting
- Selective camera translation per layer

## 7. Collision Detection
- **CollisionManager**: Service for collision detection (broad and narrow phases)
- **CollisionEvent**: Immutable record describing collision details
- **CollisionBehavior**: Interface for collision response behaviors

### Collision Process
1. **Broad Phase**: AABB intersection test for quick elimination
2. **Narrow Phase**: SAT (Separating Axis Theorem) for precise overlap calculation
3. **Response**: Positional correction and impulse application based on mass and material properties

### Key Illustrations

#### AABB Intersection Test
![AABB Intersection](illustrations/07-aabb-intersection.svg)

#### Collision Normal and Penetration
![Collision Normal](illustrations/07-collision-normal.svg)

#### Positional Correction
![Positional Correction](illustrations/07-positional-correction.svg)

#### Collision Simulation
![Collision Simulation](illustrations/07-collision-simulation.svg)

### Key Components
- **DefaultCollisionResponseBehavior**: Implements impulse-based response with friction and restitution
- **PhysicsType**: Defines entity behavior (NONE, STATIC, DYNAMIC, KINETIC)
- **Material Properties**: Friction and restitution coefficients for realistic interactions

## Summary Table

| Component | Description | Key Features |
|-----------|-------------|--------------|
| **App** | Central coordinator | Initialization, main loop, configuration |
| **PhysicsEngine** | Physics simulation | Behavior application, material properties |
| **Renderer** | Graphical display | Plugin system, layer management, camera handling |
| **Entity/GameObject** | Base game objects | Position, velocity, behaviors, materials |
| **CollisionManager** | Collision detection | Broad/narrow phase, impulse response |
| **Scene/Camera** | Scene management | Entity organization, camera targeting, layer system |

## Architecture Highlights
- **Modular Design**: Services and plugins for extensibility
- **Lifecycle Management**: Consistent initialization and disposal
- **Separation of Concerns**: Clear division between detection and response
- **Flexibility**: Multiple behaviors per entity, customizable materials and physics types
