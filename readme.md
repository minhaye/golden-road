README.md (mẫu hoàn chỉnh)
# Dungeon Learning Adventure

A simple 2D dungeon exploration game developed in **Java** for the **Object-Oriented Programming course**.

The player explores dungeon floors, fights monsters, collects items, and solves questions to unlock doors and progress to the next level.

---

# Game Overview

Dungeon Learning Adventure is a small dungeon-style game where the player must survive through multiple floors filled with monsters and challenges.

The game combines:

- Exploration
- Combat
- Item collection
- Puzzle challenges

At the end of the dungeon, the player must defeat the **Boss Monster**.

---

# Gameplay Mechanics

## Player

The player has the following attributes:

- HP (Health Points)
- MP (Mana Points)
- HP regeneration over time
- MP regeneration over time
- Skills
- Inventory

Player abilities:

- Move
- Attack
- Use skill
- Use item

---

## Monsters

There are **three types of monsters** in the game.

### Ground Monster

- Moves within a fixed area
- Close-range attacks

### Flying Monster

- Moves freely in the air
- Ranged attacks

### Boss Monster

- Large HP
- Multiple skills
- Guards the final floor

Monster behavior:

- Uses skills every few seconds
- Attacks the player automatically when in range

---

## Map System

Each map contains:

- Terrain
- Underground tunnels
- Monster zones
- Items
- Doors

### Items

Items available in the game:

- HP Potion
- MP Potion
- Key
- Treasure Chest

---

# Level Design

The dungeon has **3 floors**.

Floor 1  
- Scene 1  
- Scene 2  
- Scene 3  

Floor 2  
- Scene 1  
- Scene 2  
- Scene 3  

Floor 3  
- Boss fight

---

# Floor Progression

To advance to the next floor, the player must:

1. Reach the exit area
2. Defeat the gate guardian monster
3. Answer the door challenge question correctly

Example questions:

- 1 + 1 = ?
- What programming language is this game written in?
- Who is the most handsome developer? (humor question)

---

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

---
# Future Improvements

Possible extensions:

- Better monster AI
- More dungeon floors
- Additional skills
- Multiplayer mode
- Procedural dungeon generation

---

# Author

OOP Course Project