# SCIENTIFIC WITCHERY

A simple 2D metroidvania exploration game developed in **Java** for the **Object-Oriented Programming course**.

The player explores a giant research facility , fights robots, collects items, and unlock skills to progress to the next level.
All in the name of science

---

# Game Overview

SCIENTIFIC WITCHERY is a small metroidvania game where the player traverse through multiple research floors filled with combat robots and challenges.

The game combines:

- Exploration
- Combat
- Item collection
- Finding secrets

At the end of the facility, the player must defeat the **Appraiser**.

---

# Gameplay Mechanics

## Player

The player has the following attributes:

- HP (Health Points)
- MP (Mana Points)
- MP regeneration slowly over time
- Skills
- Inventory

Player abilities:

- Move
- Attack: 3 attack modes: blaster, shotgun, laser
- Use skill: block
- Use item: HP healer

---

## Enemies

There are **Three types of enemies** in the game.

### Ground Driod

- Moves within a fixed area
- Close-range attacks

### Flying Drone

- Moves freely in the air
- Ranged attacks

### Boss Monster

- Large HP
- Multiple skills
- Guards the final floor

Monster behavior:

- Attacks the player automatically when in range
- Uses skills every few seconds

---

## Map System

Each map contains:

- Terrain
- Hidden paths
- Enemies zones
- Traps
- Items
- Elevators

### Items

Items available in the game:

- HP Potion
- MP Potion
- Key
- Treasure Chest

---

# Level Design

The facility has **4 floors**.

Floor 1: Introduction floor: show the player basic movements, mechanics  

Floor 2: Unlock the 1st ability: shotgun shot

Floor 3: Unlock the 2nd ability: double jump

Floor 4: Unlock the 3rd ability: laser shot

Floor 5: Boss fight

---

# Floor Progression

To advance to the next floor, the player must:

1. Reach the exit area
2. Unlock the skills/Collect key required to open some doors



# Technology Stack

Language

Java

Graphics

Java Swing (2D rendering)

Programming Paradigm

Object-Oriented Programming

Concepts used:

- Inheritance
- Polymorphism
- Encapsulation
- Composition

---

# Project Architecture

The project follows a simple **game engine architecture suitable for a small Java game project**.


Game
│
├── engine
│ ├── GameLoop
│ ├── Renderer
│ └── InputHandler
│
├── entity
│ ├── Entity
│ ├── Player
│ └── Monster
│ ├── GroundMonster
│ ├── FlyingMonster
│ └── Boss
│
├── item
│ ├── Item
│ ├── HpPotion
│ ├── MpPotion
│ ├── Key
│ └── Chest
│
├── map
│ ├── GameMap
│ ├── Tile
│ └── Door
│
├── skill
│ ├── Skill
│ └── AttackSkill
│
└── main
└── Game


---

# Core System Design

## Entity System

All objects in the game inherit from the base class:


Entity


Examples:


Player extends Entity

Monster extends Entity

Boss extends Monster


This design allows:

- code reuse
- polymorphism
- easy extension

---

## Game Loop

The game runs using a **main game loop**.


while (gameRunning) {

update();

render();

sleep(16ms);

}


This loop controls:

- game logic
- entity updates
- rendering

---

## Rendering System

The game uses **Java Swing rendering** with:

- JFrame
- JPanel
- paintComponent()

Sprites and objects are drawn inside the render function.
