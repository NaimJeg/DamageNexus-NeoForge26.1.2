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

### Damage Phases

DamageNexus currently uses these major phases:

```text
BASE_MODIFICATION
TYPE_SCALING
CRITICAL_HIT
CONDITIONAL_MULTI
GLOBAL_ADJUSTMENT
MITIGATION_SETUP
FINAL_OVERRIDE
```

Phase choice is important.

For example:

- `BASE_MODIFICATION` is for adding or rebuilding base damage.
- `TYPE_SCALING` is for channel conversion or channel scaling.
- `CRITICAL_HIT` is for critical-hit related logic.
- `CONDITIONAL_MULTI` is for target- or state-dependent multipliers.
- `GLOBAL_ADJUSTMENT` is for broad final offensive scaling.
- `MITIGATION_SETUP` is for armor, resistance, protection, and defensive reductions.
- `FINAL_OVERRIDE` is for explicit final damage overrides or caps.

### Application Buckets

DamageNexus distinguishes damage by application bucket. This is separate from damage channel.

Application buckets describe which later systems affect a damage contribution, such as melee cooldown, melee crit, projectile crit, pre-multipliers, post-multipliers, and mitigation.

Examples include:

```text
VANILLA_MELEE_BASE
VANILLA_MELEE_ENCHANTMENT
VANILLA_WEAPON_SPECIAL
VANILLA_PROJECTILE_BASE
VANILLA_PROJECTILE_ENCHANTMENT
VANILLA_PROJECTILE_CRIT_BONUS
DN_RULE_BASE
DN_TRUE_DAMAGE
```

This separation is important because `+5 physical damage` is not always the same thing. It may or may not be affected by cooldown, crit, post multipliers, armor, or resistance depending on the application bucket.

### Damage Channels

Damage channels represent what kind of damage is being processed.

Known built-in channel identifiers include:

```text
damagenexus:untyped
damagenexus:physical
damagenexus:fire
damagenexus:cold
damagenexus:lightning
damagenexus:magic
damagenexus:wither
damagenexus:kinetic
damagenexus:poison
```

Channels should be used for damage identity. Application buckets should be used for lifecycle behavior.

### Registering Extensions

DamageNexus provides registration hooks for:

```java
DamageNexusApi.registerCondition(...)
DamageNexusApi.registerOperation(...)
DamageNexusApi.registerRuleProvider(...)
DamageNexusApi.registerGlobalRule(...)
DamageNexusApi.registerPreMultiplierBucket(...)
DamageNexusApi.registerPhaseProcessor(...)
```

Use a `DamageRuleProvider` if your mod wants to supply rules from items, entities, attachments, mob effects, or other runtime sources.

Use a custom `DamagePhaseProcessor` only when your feature is not naturally expressible as ordinary conditions and operations.

---

## Vanilla Bridge

DamageNexus includes bridge logic for selected vanilla mechanics.

The bridge layer observes and normalizes vanilla contributions such as:

- initial base damage;
- offensive mob-effect deltas such as Strength / Weakness;
- offensive enchantment deltas such as Sharpness-like bonuses;
- pre-event scaling such as player attack scaling, difficulty scaling, projectile scaling, spear or special weapon bonuses;
- critical-hit handling;
- defensive mitigation such as armor, protection, and resistance.

The incoming damage handler consumes a vanilla snapshot, builds a damage source profile, computes mob-effect breakdowns, creates a bridge plan, then constructs a `DamageNexusContext` and executes the pipeline.

This bridge exists to prevent scattered special cases. Vanilla behavior should be normalized into DamageNexus semantics before it affects final damage.

### Mixin Compatibility

DamageNexus may use Mixins where events do not expose enough intermediate vanilla data.

Mixin usage should be limited to:

1. probing vanilla intermediate values;
2. suppressing vanilla contributions that DamageNexus will rebuild;
3. redirecting narrow vanilla calculations when event-level control is insufficient.

Mixin code should not become the main damage-math layer.

### Compatibility Risk

Mods that modify the same vanilla damage calculations may require explicit compatibility handling.

High-risk areas include:

- enchantment damage replacement;
- Strength / Weakness replacement;
- Resistance replacement;
- Protection replacement;
- projectile damage replacement;
- critical-hit modification;
- armor formula replacement;
- damage events that set final damage directly.

---

## Debugging

When debug mode is enabled, DamageNexus logs detailed information about damage transactions.

Useful debug categories include:

- event original damage;
- initial reconstructed base damage;
- vanilla bridge plan;
- active phase processors;
- rule collection;
- rule execution;
- skipped conditions;
- channel results;
- application bucket results;
- mitigation results;
- final event damage;
- post-damage health / absorption observations.

When reporting issues, include the full transaction log for the problematic hit.

---

## Development Goals

Short-term goals:

- keep the phase pipeline stable;
- keep rule schema consistent;
- avoid double-counting vanilla mechanics;
- keep tooltip display separate from runtime execution;
- improve vanilla bridge switches and compatibility diagnostics;
- reduce hot-path allocation;
- provide reliable exhibition/demo scenarios.

Long-term goals:

- robust datapack and mod API support;
- stable vanilla bridge behavior;
- clearer tooltip explanations;
- stronger compatibility controls;
- richer damage channels and rule providers;
- better documentation for pack and mod authors.

---

## Non-Goals

DamageNexus does not aim to:

- rewrite the entire Minecraft combat engine;
- own movement, jump, mining, pathfinding, or animation systems;
- make every combat-adjacent mechanic part of the damage pipeline;
- guarantee compatibility with every combat overhaul mod without adaptation;
- treat current alpha/exhibition balance as final.

---

## Current Stability Warning

This README describes the current design direction and visible source structure, not a finalized public API guarantee.

Expect breaking changes before a stable release.
