# DamageNexus

![NeoForge](https://img.shields.io/badge/NeoForge-26.1.x-orange.svg)
![Java](https://img.shields.io/badge/Java-25-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

DamageNexus is a NeoForge combat framework that replaces parts of Minecraft's damage calculation flow with a phase-based modifier pipeline.

The project is intended for mods or modpacks that need more explicit control over damage types, offensive modifiers, resistance formulas, armor handling, and selected vanilla enchantment behavior.

## Status

DamageNexus is under active development.

The internal pipeline is usable, but the public API is not considered stable yet. Method names, phase rules, and registry structure may still change before a stable release.

## Goals

DamageNexus aims to provide:

- a predictable damage processing order
- separate offensive and defensive calculation stages
- damage channels such as physical, fire, cold, magic, poison, wither, and kinetic
- configurable armor and resistance formulas
- integration points for vanilla enchantment hooks
- a modifier API for other mods to participate in the damage pipeline

## Non-Goals

DamageNexus does not currently try to replace every part of Minecraft combat.

Some systems may still be handled by vanilla or other mods depending on configuration and implementation status, including:

- shield blocking
- absorption
- post-damage effects
- custom damage behavior from other mods
- damage changes made after `LivingIncomingDamageEvent`
