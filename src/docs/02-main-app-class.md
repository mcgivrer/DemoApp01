# Architecture globale de l'application

## Vue d'ensemble

La classe `App` constitue le point d'entrée et le coordinateur central de l'application. Elle orchestre l'ensemble des composants principaux et gère le cycle de vie complet de l'application, depuis l'initialisation jusqu'à la libération des ressources.

## Architecture générale

L'application suit une architecture modulaire organisée autour d'un moteur de jeu classique avec une boucle principale (game loop). Les composants clés sont gérés par la classe `App` qui assure leur coordination.

### Diagramme d'architecture

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam classBackgroundColor #E8F4FD
skinparam classBorderColor #2196F3

class App {
    + {static} ResourceBundle messages
    + {static} int debug
    + {static} AppMode mode
    - PhysicsEngine physicsEngine
    - Renderer renderer
    - InputHandler inputHandler
    + void run(String[] args)
    + void initialize(String[] args)
}

class Service {
    + App app
    + boolean active
    + void initialize(Properties config)
    + void update(Scene scene, float deltaTime, Map stats)
}

class PhysicsEngine extends Service {
    + void update(Scene scene, float deltaTime, Map stats)
}

class Renderer extends Service {
    + InputHandler inputHandler
    + void update(Scene scene, float deltaTime, Map stats)
}

class InputHandler {
    + boolean[] keys
    + void keyPressed(KeyEvent key)
    + void keyReleased(KeyEvent key)
}

class Scene {
    + List<Entity<?>> entities
    + void load(App app)
    + void update(float deltaTime)
}

' Relationships
App "1" *-- "1" PhysicsEngine : creates >
App "1" *-- "1" Renderer : creates >
App "1" *-- "1" InputHandler : creates >
App "1" --> "1" Scene : manages >

Renderer "1" --> "1" InputHandler : uses >

PhysicsEngine -up-|> Service
Renderer -up-|> Service

Scene "1" *-- "*" Entity : contains >

note right of App::physicsEngine
  App instantiates and manages
  all service components
end note

note right of Service
  Base class for all
  modular services
end note

@enduml
```

### Diagramme de packages

```plantuml
@startuml
package "core" {
    package "behavior" {
        [Behavior]
        [CameraBehavior]
        [GravityBehavior]
        [PlayerInputBehavior]
        [VelocityBehavior]
        [WorldContainedBehavior]
    }
    package "entity" {
        [Entity]
        [Camera]
        [GameObject]
        [World]
    }
    package "graphics" {
        [Renderer]
        [RenderPlugin]
        [Layer]
        [GameObjectRenderPlugin]
        [WorldRenderPlugin]
        [DebugRenderPlugin]
    }
    package "physics" {
        [PhysicsEngine]
        [Material]
    }
    package "scene" {
        [Scene]
    }
    package "utils" {
        [InputHandler]
        [Service]
    }
    [App]
}

package "demo" {
    package "scenes" {
        [DemoScene]
    }
}

[App] --> [Service]
[App] --> [Scene]
[App] --> [PhysicsEngine]
[App] --> [Renderer]
[App] --> [InputHandler]
[App] --> [Entity]
[App] --> [Behavior]
@enduml
```

### Diagramme de classes principal

```plantuml
@startuml
class App {
    + {static} ResourceBundle messages
    + {static} int debug
    + {static} AppMode mode
    + {static} boolean exit
    - Properties config
    - PhysicsEngine physicsEngine
    - Renderer renderer
    + InputHandler inputHandler

    + App()
    + void run(String[] args)
    - void initialize(String[] args)
    + void run()
    - void loop()
    - void update(float elapsed, Map<String, Object> stats)
    - void draw(float elapsed, Map<String, Object> stats)
    - void parseConfiguration()
    - void parseCliArgs(String[] args)
    - void parseArg(String arg)
    - void parseConfig(String key, String value)
    + {static} void main(String[] args)
    + {static} void log(Class<?> cls, LogLevel level, String message, Object... args)
    - void dispose()
    + {static} void requestExit()
    + InputHandler getInputHandler()
}

enum LogLevel {
    DEBUG
    INFO
    WARN
    ERROR
    FATAL
}

enum AppMode {
    DEVELOPMENT
    TESTING
    PRODUCTION
}

class PhysicsEngine extends Service {
    + PhysicsEngine(App app)
    + void update(Scene scene, float deltaTime, Map<String, Object> stats)
    + void initialize(Properties config)
    + void dispose()
}

class Renderer extends Service {
    + Renderer(App app, InputHandler inputHandler)
    + void update(Scene scene, float deltaTime, Map<String, Object> stats)
    + void initialize(Properties config)
    + void registerPlugin(RenderPlugin<? extends Entity<?>> renderPlugin)
    - void drawEntity(Graphics2D g, Camera camera, Entity<?> entity)
    + JFrame getWindow()
    + void dispose()
}

class InputHandler extends KeyAdapter {
    - boolean[] keys
    + void keyPressed(KeyEvent key)
    + void keyReleased(KeyEvent key)
    + boolean isKeyPressed(int keyCode)
}

class Service {
    - {static} List<Service> services
    + App app
    + boolean active
    + Service(App app)
    + void register()
    + void initialize(Properties config)
    + void start()
    + void stop()
    + void update(Scene scene, float deltaTime, Map<String, Object> stats)
    + void dispose()
    + {static} void initializeAll(Properties config)
    + {static} void startAll()
    + {static} void stopAll()
    + {static} void disposeAll()
    + {static} List<Service> getServices()
    + {static} <T extends Service> T get(Class<T> serviceClass)
}

class Scene extends Entity<Scene> {
    + {static} List<Scene> scenes
    + {static} Scene currentScene
    + List<Entity<?>> entities
    + Scene(String name)
    + {static} void add(Scene scene)
    + void load(App app)
    + void init(App app)
    + void create(App app)
    + void dispose(App app)
    + Scene addEntity(Entity<?> entity)
    + Scene removeEntity(Entity<?> entity)
    + List<Entity<?>> getEntities()
    + {static} Scene activate(App app, String name)
    + {static} Scene getActiveScene()
    + {static} void initialize(Properties config)
    + Camera getCameras()
    + void setActiveCamera(Camera camera)
    + void setActiveCamera(String cameraName)
}

App *-- PhysicsEngine
App *-- Renderer
App *-- InputHandler
App ..> LogLevel
App ..> AppMode
App --> Service : manages
App --> Scene : manages
PhysicsEngine --> Scene
Renderer --> Scene
Service --> Scene
@enduml
```

## Composants principaux

### 1. La classe App

La classe `App` est le cœur de l'application. Elle assume plusieurs responsabilités :

- **Initialisation** : Charge la configuration, analyse les arguments de ligne de commande et initialise tous les sous-systèmes
- **Orchestration** : Coordonne les services, les scènes et les composants moteur
- **Boucle principale** : Exécute la game loop qui gère les phases de mise à jour et de rendu
- **Configuration** : Gère les propriétés de configuration et l'internationalisation (i18n)
- **Logging** : Fournit un système de journalisation centralisé avec différents niveaux

### 2. Moteur physique (PhysicsEngine)

Le `PhysicsEngine` est responsable de la simulation physique de l'application. Il met à jour les états physiques des entités dans une scène, appliquant les comportements aux scènes et aux entités.

**Méthodes principales :**

- `update(Scene scene, float deltaTime, Map<String, Object> stats)` : Met à jour la physique de la scène et de ses entités.
- `initialize(Properties config)` : Initialise le moteur physique avec la configuration.
- `dispose()` : Libère les ressources du moteur physique.

### 3. Moteur de rendu (Renderer)

Le `Renderer` gère l'affichage graphique de l'application. Il rend les scènes en utilisant des plugins de rendu et gère une fenêtre d'affichage.

**Méthodes principales :**

- `update(Scene scene, float deltaTime, Map<String, Object> stats)` : Rend la scène sur l'écran.
- `initialize(Properties config)` : Initialise le renderer, crée la fenêtre et enregistre les plugins.
- `registerPlugin(RenderPlugin<? extends Entity<?>> renderPlugin)` : Enregistre un plugin de rendu.
- `dispose()` : Libère les ressources du renderer et ferme la fenêtre.

### 4. Gestionnaire d'entrées (InputHandler)

L'`InputHandler` capture et traite les entrées utilisateur (clavier). Il maintient l'état des touches et gère les événements spéciaux.

**Méthodes principales :**

- `keyPressed(KeyEvent key)` : Gère l'appui sur une touche.
- `keyReleased(KeyEvent key)` : Gère le relâchement d'une touche et traite les clés spéciales.
- `isKeyPressed(int keyCode)` : Vérifie si une touche est actuellement pressée.

### 5. Services et Scènes

- **Service** : Classe de base pour les composants modulaires avec cycle de vie (initialize, start, stop, update, dispose). Gère une liste statique de services.
- **Scene** : Représente un état de l'application, gère une liste d'entités. Étend Entity<Scene>.

**Méthodes principales de Scene :**

- `load(App app)`, `init(App app)`, `create(App app)`, `dispose(App app)` : Cycle de vie de la scène.
- `addEntity(Entity<?> entity)`, `removeEntity(Entity<?> entity)`, `getEntities()` : Gestion des entités.
- `activate(App app, String name)` : Active une scène par nom.
- `getActiveScene()` : Retourne la scène active.
- `initialize(Properties config)` : Initialise les scènes depuis la configuration.

## Diagramme de séquence : Démarrage de l'application

```plantuml
@startuml
actor User
participant "App" as App
participant "Config" as Config
participant "PhysicsEngine" as PE
participant "Renderer" as R
participant "InputHandler" as IH
participant "Service" as S
participant "Scene" as Sc

User -> App: main(args)
activate App

App -> App: new App()
App -> Config: parseConfiguration()
activate Config
Config --> App: config loaded
deactivate Config

App -> App: parseCliArgs(args)
App -> App: initialize(args)

App -> IH: new InputHandler()
activate IH
App -> PE: new PhysicsEngine(this)
activate PE
App -> R: new Renderer(this, inputHandler)
activate R

App -> S: initializeAll(config)
activate S
S --> App: services ready
deactivate S

App -> Sc: activate(this, defaultSceneName)
activate Sc
Sc --> App: scene ready

App -> App: loop()
loop Game Loop
    App -> App: update(elapsed, stats)
    App -> PE: update(currentScene, elapsed, stats)
    App -> Sc: behaviors update (via PhysicsEngine)

    App -> App: draw(elapsed, stats)
    App -> R: update(currentScene, elapsed, stats)
end

User -> App: requestExit()
App -> App: dispose()
App -> S: disposeAll()
deactivate S
App -> PE: dispose()
deactivate PE
App -> R: dispose()
deactivate R
App -> IH: dispose()
deactivate IH
App -> Sc: dispose()
deactivate Sc

deactivate App
@enduml
```

## Cycle de vie de l'application

### Phase 1 : Initialisation

1. Chargement du fichier de configuration (`parseConfiguration()`)
2. Analyse des arguments de ligne de commande (`parseCliArgs()`)
3. Création des composants principaux (InputHandler, PhysicsEngine, Renderer)
4. Initialisation des services (`Service.initializeAll()`)
5. Activation de la scène initiale (`Scene.activate()`)

### Phase 2 : Boucle principale

La méthode `loop()` exécute continuellement :

1. **Update** (`update()`):
   - Mise à jour du moteur physique (`physicsEngine.update()`)
   - Application des comportements aux entités via le PhysicsEngine

2. **Draw** (`draw()`):
   - Rendu de la scène via le Renderer (`renderer.update()`)

### Phase 3 : Terminaison

La méthode `dispose()` libère les ressources dans l'ordre inverse de création :

- Libération des services (`Service.disposeAll()`)
- Libération du moteur physique
- Libération du renderer
- Libération de l'input handler

## Diagramme d'états : Modes de l'application

```plantuml
@startuml
[*] --> DEVELOPMENT : default

DEVELOPMENT --> TESTING : mode=testing
DEVELOPMENT --> PRODUCTION : mode=production

TESTING --> DEVELOPMENT : mode=development
TESTING --> PRODUCTION : mode=production

PRODUCTION --> DEVELOPMENT : mode=development
PRODUCTION --> TESTING : mode=testing

DEVELOPMENT : Debug actif
DEVELOPMENT : Logs verbeux
DEVELOPMENT : Outils de développement

TESTING : Tests automatisés
TESTING : Validation
TESTING : Logs modérés

PRODUCTION : Performance optimale
PRODUCTION : Logs minimaux
PRODUCTION : Fonctionnalités complètes

DEVELOPMENT --> [*] : exit
TESTING --> [*] : exit
PRODUCTION --> [*] : exit
@enduml
```

## Configuration et internationalisation

L'application utilise deux mécanismes de configuration :

1. **Fichier de propriétés** : Configuration technique (résolution, FPS, etc.)
2. **ResourceBundle** : Messages internationalisés (i18n) via `ResourceBundle.getBundle("i18n/messages")`

## Système de logging

Le système de logging (`log()`) offre 5 niveaux de verbosité :

- **DEBUG** : Informations détaillées pour le débogage
- **INFO** : Informations générales sur l'exécution
- **WARN** : Avertissements sur des situations potentiellement problématiques
- **ERROR** : Erreurs nécessitant attention
- **FATAL** : Erreurs critiques entraînant l'arrêt

## Points d'extension

L'architecture permet l'extension via :

1. **Services personnalisés** : Implémentation de l'interface `Service`
2. **Scènes personnalisées** : Implémentation de l'interface `Scene`
3. **Configuration** : Ajout de nouvelles propriétés dans le fichier de configuration
4. **Mode de fonctionnement** : Adaptation du comportement selon `AppMode`
