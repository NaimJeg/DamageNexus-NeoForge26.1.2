# DamageNexus

![NeoForge](https://img.shields.io/badge/NeoForge-26.1.x-orange.svg)
![Java](https://img.shields.io/badge/Java-25-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)


**DamageNexus is a NeoForge damage-system framework that rebuilds Minecraft damage resolution through staged processing, damage channels, data-driven rules, and vanilla-mechanic bridges.**

> Current status: active development / exhibition-oriented build. APIs, data formats, balance values, and vanilla bridge behavior may still change.

---

## What DamageNexus Does

DamageNexus focuses on **damage resolution**, not the entire combat engine.

It intercepts incoming damage, classifies the damage source, rebuilds or normalizes selected vanilla damage contributions, applies custom damage rules, performs mitigation, and produces debug traces for each handled damage transaction.

DamageNexus is intended to answer one question:

> Given this attacker, target, damage source, vanilla state, equipment, rules, and mitigation, what final damage should be applied?

It does **not** try to rewrite movement, mining speed, jump physics, AI, animations, general item use, or the full Minecraft combat engine.

---

## For Players

DamageNexus changes how damage is calculated.

Depending on the current build and configuration, this may affect:

- weapon damage;
- custom item damage rules;
- damage types such as physical, fire, cold, lightning, magic, poison, wither, and kinetic;
- armor mitigation;
- resistance-style mitigation;
- critical hit handling;
- selected vanilla enchantment or mob-effect damage contributions.

You may see items or effects that describe damage using DamageNexus terms such as:

```text
+5 physical damage
+25% fire damage
Against burning targets: +50% physical damage
-15% incoming physical damage
```

### Player Notice

DamageNexus is still in development.

Because it intercepts and rebuilds parts of the damage calculation flow, it may not behave exactly like vanilla Minecraft or other combat mods. If damage looks too high, too low, duplicated, missing, or inconsistent, report the issue with:

- the mod list;
- the exact weapon, armor, enchantments, and effects involved;
- the target entity;
- the damage source, if known;
- `latest.log` or crash report;
- reproduction steps.

---

## For Modpack Authors

DamageNexus is best treated as a **damage-overhaul framework**, not a small balance tweak.

It can be used to build packs with:

- clearer damage-type identity;
- custom weapon and armor rules;
- predictable phase-based damage scaling;
- alternative armor and resistance formulas;
- more transparent damage debugging;
- controlled integration with selected vanilla mechanics.

### Configuration

Current common configuration includes:

```text
developer_settings.debugMode
combat_formulas.asymptoticKValue
combat_formulas.resistanceKValue
combat_formulas.ratingPerProtScore
```

Use `debugMode` only when diagnosing damage behavior. It produces detailed combat logs and should not be enabled in normal gameplay packs.

### Compatibility Notes

DamageNexus may conflict with mods that also change:

- damage calculation;
- armor or toughness formulas;
- Protection-style mitigation;
- Resistance / Strength / Weakness behavior;
- enchantment damage effects;
- projectile damage;
- critical hits;
- post-damage behavior.

For safer packs, avoid stacking multiple large combat-overhaul mods unless explicit compatibility has been tested.

### Pack Integration Guidance

For exhibition or alpha packs, prefer a narrow test scope:

- a small set of showcase weapons;
- a small set of showcase armor;
- a few fixed test enemies;
- limited vanilla bridge coverage;
- debug traces available for pack developers;
- clear compatibility notice in the modpack description.

Do not assume final balance. DamageNexus is still an architecture-first project.

---

## For Mod Authors

DamageNexus exposes a damage-rule and processor-oriented API.

The current extension model is based on:

- custom damage rule conditions;
- custom damage rule operations;
- custom damage rule providers;
- global damage rules;
- pre-multiplier buckets;
- custom phase processors.

Conceptually, a damage rule is:

```text
id + role + phase + priority + display + conditions + operations + stacking + trace_label
```

A rule does not need to know whether it came from a weapon, armor piece, enchantment, mob effect, projectile, or another mod. The provider decides where the rule comes from; the pipeline decides when it runs.

### Damage Rule Shape

A typical rule contains:

```json
{
  "id": "examplemod:burning_bonus",
  "role": "offensive",
  "phase": "conditional_multi",
  "priority": 500,
  "display": {
    "name": "Burning Bonus",
    "description": "+50% physical damage against burning targets"
  },
  "conditions": [
    {
      "type": "damagenexus:target_on_fire"
    }
  ],
  "operations": [
    {
      "type": "damagenexus:add_channel_post_multiplier",
      "channel": "damagenexus:physical",
      "value": 0.5
    }
  ],
  "stacking": "stack",
  "trace_label": "Burning Bonus"
}
```

Exact operation and condition names depend on the registered codecs in the target build.

### Damage Phases# DamageNexus

DamageNexus is a NeoForge damage framework that restructures Minecraft damage handling into a phase-based, channel-aware, and rule-driven pipeline.

It is designed for mod developers, datapack authors, and modpack creators who need more control over damage types, scaling, mitigation, critical hits, and custom combat rules.

## Features

- Phase-based damage pipeline
- Damage channels such as physical, fire, lightning, magic, poison, and more
- Bucketized damage application model
- Rule-based damage modifiers
- Datapack-configurable damage rules
- Java API extension points
- Vanilla damage bridge for compatibility
- Armor, resistance, and mitigation control
- Debug logging and transaction tracing

## Documentation

Full documentation is available in the GitHub Wiki:

[DamageNexus Wiki](../../wiki)

Recommended starting pages:

- [Core Concepts](../../wiki/Core-Concepts)
- [Damage Pipeline](../../wiki/Damage-Pipeline)
- [Damage Channels](../../wiki/Damage-Channels)
- [Rule System](../../wiki/Rule-System)
- [Datapack Rules](../../wiki/Datapack-Rules)
- [Java API Guide](../../wiki/Java-API-Guide)

## Project Status

DamageNexus is currently under active development.

APIs, rule formats, and internal behavior may change before a stable release.

## For Modpack Authors

Use the Wiki to understand:

- how damage channels work
- how datapack rules are written
- how resistance and mitigation are calculated
- how to debug unexpected damage results

Start with:

[Datapack Rules](../../wiki/Datapack-Rules)

## For Mod Developers

DamageNexus exposes integration points for:

- custom rule conditions
- custom rule operations
- rule providers
- global rules
- phase processors
- pre-multiplier buckets

Start with:

[Java API Guide](../../wiki/Java-API-Guide)

## Design Summary

DamageNexus treats damage as a transaction:

```text
incoming damage event
  -> channel detection
  -> base damage construction
  -> scaling
  -> critical hit handling
  -> global adjustment
  -> mitigation setup
  -> final override
  -> event damage application
  -> post-damage validation