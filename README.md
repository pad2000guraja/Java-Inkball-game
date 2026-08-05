# Java-Inkball-game
A Java-based implementation of the classic Inkball puzzle game built using the Processing graphics library. Player's guide moving colored balls into their corresponding-colored holes by drawing lines that alter the balls' trajectories while avoiding incorrect matches and managing the level timer.

# Features
1) Interactive gameplay with real-time line drawing.
2) Physics-based ball movement and collision detection.
3) Color-matching scoring system with configurable score modifiers.
4) Wall and boundary collision mechanics.
5) Timed levels with pause and restart functionality.
6) Dynamic ball spawning and respawning.
7) Multi-level gameplay driven by JSON configuration files.
8) Sprite-based rendering using the Processing library.

# Technologies Used
1) Java
2) Processing Library
3) Object-Oriented Programming (OOP)
4) JSON Configuration Files

# Project Structure
The project follows an object-oriented design consisting of four primary classes:

App.java – Manages game initialization, rendering, scoring, level progression, timers, and user input.
Ball.java – Implements ball movement, collision detection, reflection, and hole capture logic.
Wall.java – Represents static obstacles that alter ball trajectories.
Hole.java – Handles color-matching objectives and ball attraction mechanics.

# Gameplay

Players draw lines to redirect moving balls toward holes of the same color. Successfully matching colors increases the score, while incorrect matches result in penalties and the ball being re-queued. The objective is to clear each level before the timer expires.

