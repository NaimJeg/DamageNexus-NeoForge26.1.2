# DamageNexus

![NeoForge](https://img.shields.io/badge/NeoForge-26.1.x-orange.svg)
![Java](https://img.shields.io/badge/Java-25-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

DamageNexus is a damage framework mod for Minecraft, built with NeoForge.

It replaces fragmented damage calculations with a structured, extensible pipeline that allows mods and data packs to define how damage is created, modified, converted, mitigated, and finalized.

## Features

* Multiple damage channels, including physical, fire, cold, lightning, magic, poison, wither, and kinetic damage
* Ordered damage-processing phases for predictable calculations
* Data-driven damage rules
* Item damage entries and affixes
* Damage conversion, extra damage, true damage, multipliers, resistance, and mitigation
* Integration with vanilla melee, projectile, critical-hit, enchantment, armor, and status-effect mechanics
* Public Java API for registering custom rules, conditions, operations, providers, and processors
* Diagnostic logging and configurable compatibility behavior

## Purpose

DamageNexus is primarily an infrastructure mod for mod developers and modpack authors. It provides a common system for implementing custom damage mechanics without requiring every mod to replace or independently reproduce Minecraft's damage logic.
