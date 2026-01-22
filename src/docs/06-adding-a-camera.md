# Adding a Camera

## Vue d'ensemble

Ce chapitre explique comment les scènes, caméras et layers travaillent ensemble pour créer un système d'affichage hiérarchisé avec gestion de la profondeur. Les scènes contiennent les entités, les caméras gèrent la vue, et les layers organisent l'ordre de rendu.

## Fonctionnement des scènes

### Classe Scene

La classe `Scene` représente un état ou un écran de l'application. Elle hérite de `Entity<Scene>`, ce qui lui permet d'avoir des propriétés et comportements comme toute autre entité.

#### Gestion des entités

- **Collection d'entités** : `List<Entity<?>> entities`
- **Ajout/Suppression** : `addEntity()`, `removeEntity()`
- **Accès** : `getEntities()`

#### Cycle de vie

- **load(App app)** : Chargement des ressources
- **init(App app)** : Initialisation
- **create(App app)** : Création des objets
- **dispose(App app)** : Libération des ressources

#### Gestion statique

- **Liste globale** : `scenes` contient toutes les scènes chargées
- **Scène active** : `currentScene` pointe vers la scène courante
- **Activation** : `activate(App app, String name)` change la scène active
- **Initialisation** : `initialize(Properties config)` charge les scènes depuis la config

#### Gestion des caméras

- **Caméra active** : `getCameras()` retourne la première caméra active
- **Définition** : `setActiveCamera(Camera camera)` ou `setActiveCamera(String cameraName)`

## Utilisation des caméras

### Classe Camera

La `Camera` hérite de `Entity<Camera>` et gère la vue du joueur sur la scène.

#### Attributs principaux

- **target** (Entity<?>) : Entité suivie par la caméra
- **tweenFactor** (float) : Facteur de lissage du mouvement (0.1 par défaut)
- **active** (boolean) : Indique si la caméra est active

#### Constructeurs

```java
public Camera(String name)
public Camera(String name, float offsetX, float offsetY) // Ajoute CameraBehavior
```

#### Méthodes

- **setTarget(Entity<?> target)** : Définit l'entité à suivre
- **setTweenFactor(float tweenFactor)** : Ajuste la fluidité du suivi
- **setActive(boolean active)** : Active/désactive la caméra

#### Fonctionnement

La caméra suit généralement un joueur ou un objet principal. Le `CameraBehavior` applique un mouvement fluide vers la cible avec un facteur de tweening pour éviter les saccades.

## Gestion des layers et profondeur

### Classe Layer

Les `Layer` organisent les entités en plans d'affichage avec une notion de profondeur.

#### Types de layers

```java
enum LayerType {
    BACKGROUND,    // Arrière-plan (décors lointains)
    MIDGROUND,     // Plan intermédiaire (éléments principaux)
    FOREGROUND,    // Premier plan (éléments proches)
    UI,           // Interface utilisateur
    OVERLAY       // Superpositions (menus, effets)
}
```

#### Attributs

- **layerType** (LayerType) : Type du layer
- **zIndex** (int) : Profondeur dans le layer (plus élevé = devant)

#### Gestion des entités

- **add(Entity<?> entity)** : Ajoute une entité au layer et définit sa référence

### Système de profondeur

#### Tri des entités

Le renderer trie les entités par `zIndex` croissant :
```java
.sorted((e1, e2) -> Integer.compare(e1.getLayer().getZIndex(), e2.getLayer().getZIndex()))
```

#### Translation caméra

Seuls les layers `MIDGROUND` et `FOREGROUND` subissent la translation caméra :
- **Avant rendu** : `g.translate(-camera.getX(), -camera.getY())`
- **Après rendu** : `g.translate(camera.getX(), camera.getY())`

Les layers `BACKGROUND`, `UI`, et `OVERLAY` restent fixes par rapport à l'écran.

## Architecture d'affichage

### Hiérarchie

```
Scene
├── Camera (active)
├── Layer (BACKGROUND, zIndex: 0-99)
│   ├── Entity (décors lointains)
├── Layer (MIDGROUND, zIndex: 100-199)
│   ├── Entity (éléments principaux, suivent la caméra)
├── Layer (FOREGROUND, zIndex: 200-299)
│   ├── Entity (éléments proches, suivent la caméra)
├── Layer (UI, zIndex: 300-399)
│   ├── Entity (boutons, HUD)
└── Layer (OVERLAY, zIndex: 400+)
    ├── Entity (menus, effets spéciaux)
```

### Exemple d'utilisation

```java
// Création d'une scène
Scene gameScene = new Scene("game");

// Création des layers
Layer background = new Layer("background", LayerType.BACKGROUND, 0);
Layer midground = new Layer("midground", LayerType.MIDGROUND, 100);
Layer foreground = new Layer("foreground", LayerType.FOREGROUND, 200);
Layer ui = new Layer("ui", LayerType.UI, 300);

// Création d'entités
GameObject player = new GameObject("player").setLayer(midground);
GameObject enemy = new GameObject("enemy").setLayer(midground);
GameObject tree = new GameObject("tree").setLayer(foreground);
GameObject cloud = new GameObject("cloud").setLayer(background);

// Création de la caméra
Camera camera = new Camera("mainCamera", 0, -100)
    .setTarget(player)
    .setTweenFactor(0.05f);

// Ajout à la scène
gameScene
    .addEntity(background)
    .addEntity(midground.add(player).add(enemy))
    .addEntity(foreground.add(tree))
    .addEntity(ui)
    .addEntity(camera);

// Activation
Scene.activate(app, "game");
gameScene.setActiveCamera(camera);
```

### Avantages du système

1. **Performance** : Tri efficace par zIndex
2. **Flexibilité** : Caméra sélective par layer
3. **Organisation** : Séparation claire des éléments visuels
4. **Extensibilité** : Ajout facile de nouveaux layers/types

Ce système permet de créer des mondes 2D complexes avec parallax scrolling et interface utilisateur sans conflit de profondeur.

