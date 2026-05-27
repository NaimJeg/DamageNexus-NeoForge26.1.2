# DamageNexus

![NeoForge](https://img.shields.io/badge/NeoForge-26.1.x-orange.svg)
![Java](https://img.shields.io/badge/Java-25-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

**DamageNexus** is a work-in-progress Minecraft NeoForge mod that rebuilds the **damage resolution layer** of combat through a staged, traceable, data-driven damage pipeline.

The project is intentionally scoped to **damage calculation only**. It does not attempt to rewrite the entire Minecraft combat engine, movement system, attack-speed system, mining system, jumping system, AI, knockback physics, or animation flow. Those systems may be read as context, but DamageNexus only owns how a damage event is classified, modified, mitigated, overridden, traced, and finally applied.

> Project principle: **Event first. Mixin only when necessary. Adapter normalizes vanilla. Pipeline owns calculation. Tooltip owns explanation. Trace owns debugging.**

---

## Current Status

DamageNexus currently has a minimum viable damage pipeline with:

- incoming damage interception;
- phase-based damage processors;
- data-driven item damage rules stored on `ItemStack` components;
- offensive and defensive rule roles;
- damage channels such as physical, fire, cold, lightning, magic, poison, wither, and kinetic;
- vanilla bridge processors for selected vanilla contributions;
- debug combat logging;
- post-damage observation for applied damage, health delta, absorption delta, invulnerability, and overkill-cap mismatch.

The project is still in active architecture stabilization. Public-facing gameplay balance, final affix naming, datapack schema stability, and complete vanilla parity are not final.

---

## Design Scope

DamageNexus is not a full combat-engine replacement.

### In Scope

- Damage source classification
- Damage channel conversion and scaling
- Base damage modification
- Critical-hit reconstruction
- Attack cooldown and global damage scaling when relevant
- Armor, protection, resistance, and custom mitigation
- Vanilla enchantment damage contribution normalization
- Vanilla mob-effect damage contribution normalization
- Data-driven weapon, armor, and entity damage rules
- Final damage override rules
- Runtime debug trace
- Tooltip explanation for damage rules and affixes

### Out of Scope, Except as Context

- Movement speed
- Attack speed itself
- Mining speed
- Jump strength
- AI behavior
- Entity pathfinding
- Animation timing
- General item-use mechanics
- Full vanilla combat rewrite

These systems may still be used as **conditions**. For example, a damage rule may check whether the attacker is sprinting, whether attack cooldown is high enough, or whether the target has a specific mob effect. DamageNexus does not own those systems directly.

---

## Core Concept

Every combat-related damage contribution should be normalized into a common runtime form:

```text
source + role + phase + priority + conditions + operations + trace label
```

Examples:

```text
minecraft:sharpness
  role: offensive
  phase: base_modification
  operation: add base physical damage
```

```text
minecraft:resistance
  role: defensive
  phase: mitigation_setup
  operation: reduce incoming damage
```

```text
damagenexus:burning_edge
  role: offensive
  phase: conditional_multi
  condition: target is on fire
  operation: add physical post multiplier
```

This keeps vanilla enchantments, vanilla mob effects, custom affixes, datapack rules, and future mod integrations under one execution model.

---

## Damage Pipeline

DamageNexus uses explicit phases. Processors are registered into phases and ordered by priority.

| Phase | Purpose | Typical Vanilla / DN Content |
|---|---|---|
| `SOURCE_CLASSIFICATION` / initialization | Build the event context | `DamageSource` tags, attacker, direct attacker, weapon, projectile, source item, initial damage channel |
| `BASE_MODIFICATION` | Rebuild or add base damage | `ATTACK_DAMAGE`, Strength, Weakness, Sharpness / Smite / Bane delta, mace fall bonus, spear speed bonus, projectile base damage |
| `TYPE_SCALING` | Convert or scale damage channels | physical-to-fire conversion, gain X% as lightning, projectile element mapping, Power/projectile element if needed |
| `CRITICAL_HIT` | Apply critical-hit logic | vanilla melee crit, projectile crit, critical damage affixes |
| `CONDITIONAL_MULTI` | Apply conditional damage multipliers | burning-target bonuses, undead-target bonuses, low-health bonuses, biome/entity/state checks |
| `GLOBAL_ADJUSTMENT` | Apply broad final offensive scaling | attack cooldown scaling, difficulty scaling, special pre-event scaling, projectile/global multipliers |
| `MITIGATION_SETUP` | Rebuild incoming mitigation | armor effectiveness, armor formula, Protection, Resistance, custom resistance rating, temporary resistance |
| `FINAL_OVERRIDE` | Override or clamp final event damage | command/test overrides, final caps, forced final values |
| `POST_RESOURCE` | Observe applied resources after damage | absorption, health delta, hurt time, invulnerability, overkill cap, mismatch diagnostics |

### Important Phase Semantics

Phase placement changes behavior.

For example, a rule that adds `+5 fire damage` in `BASE_MODIFICATION` is affected by later critical, global, and mitigation logic. The same `+5 fire damage` applied after `MITIGATION_SETUP` as real damage would bypass normal charge penalty, global scaling, and mitigation.

Similarly, global multiplier placement defines how much vanilla attack cooldown and difficulty scaling affect custom damage rules.

---

## Runtime Processor Model

The pipeline currently uses phase processors such as:

```text
BASE_MODIFICATION
  VanillaOffensiveEnchantmentProcessor
  VanillaOffensiveMobEffectProcessor
  VanillaSpearBonusProcessor
  RuleExecutionProcessor

TYPE_SCALING
  RuleExecutionProcessor

CRITICAL_HIT
  VanillaCriticalBridgeProcessor
  CriticalHitProcessor
  RuleExecutionProcessor

GLOBAL_ADJUSTMENT
  VanillaDifficultyScalingProcessor
  VanillaSpecialAttackScalingProcessor
  VanillaPlayerAttackScalingProcessor
  VanillaProjectileScalingProcessor
  RuleExecutionProcessor

MITIGATION_SETUP
  VanillaArmorEffectivenessProcessor
  VanillaDamageProtectionProcessor
  VanillaResistanceEffectProcessor
  RuleExecutionProcessor
  ArmorMitigationProcessor
  ResistanceMitigationProcessor

FINAL_OVERRIDE
  RuleExecutionProcessor
```

Processors should be small, phase-specific units. Hot-path processors should avoid dynamic allocation when possible. Runtime vanilla bridges should not instantiate new rule definitions on every hit unless a stable cached/template form is used.

---

## Vanilla Bridge Strategy

DamageNexus should not scatter damage logic across many unrelated mixins. Vanilla integration should use a bridge/adaptor model:

```text
Vanilla / NeoForge damage event
        ↓
Probe / suppress / normalize hook
        ↓
Vanilla adapter processor
        ↓
DamageNexus pipeline mutation
        ↓
Trace + final event amount
```

### Bridge Modes

A vanilla bridge may operate in one of three conceptual modes:

| Mode | Meaning |
|---|---|
| `observe_only` | Detect vanilla contribution and expose it to trace, without replacing vanilla logic |
| `replace_vanilla` | Suppress or subtract vanilla contribution, then reapply it through DamageNexus |
| `disabled` | Ignore that vanilla mechanic |

This avoids double-counting vanilla effects while allowing gradual migration.

### Mixin Policy

Mixin should be used only for:

1. **Probe** — expose intermediate vanilla data not visible from events;
2. **Suppress** — prevent vanilla from applying a contribution that DamageNexus will rebuild;
3. **Redirect** — route a vanilla calculation into DamageNexus when event-level control is insufficient.

Mixin should not become the primary gameplay logic layer.

---

## Vanilla Mechanics Mapping

Current bridge direction follows this mapping:

| Vanilla Mechanic | DN Phase | Notes |
|---|---:|---|
| `ATTACK_DAMAGE` attribute | `BASE_MODIFICATION` | Base melee weapon/entity damage |
| Strength | `BASE_MODIFICATION` | Affects vanilla melee damage only |
| Weakness | `BASE_MODIFICATION` | Affects vanilla melee damage only; current implementation may defer full handling |
| Sharpness | `BASE_MODIFICATION` | Flat offensive enchantment contribution |
| Smite / Bane of Arthropods | `BASE_MODIFICATION` or conditional base contribution | Conditional target-type enchantment contribution |
| Mace fall bonus | `BASE_MODIFICATION` | Special vanilla bonus source |
| Spear speed bonus | `BASE_MODIFICATION` | Special vanilla/projectile-like bonus source |
| Projectile base damage | `BASE_MODIFICATION` | Needs projectile owner/source reconstruction |
| Power | `TYPE_SCALING` or projectile-specific scaling | Should be handled after projectile provenance is stable |
| Vanilla melee crit | `CRITICAL_HIT` | Rebuilt via critical processors/bridges |
| Projectile crit | `CRITICAL_HIT` | Separate from melee crit |
| Attack cooldown | `GLOBAL_ADJUSTMENT` | Placement controls how much custom damage is scaled |
| Difficulty scaling | `GLOBAL_ADJUSTMENT` | Vanilla global incoming/outgoing adjustment |
| Armor effectiveness | `MITIGATION_SETUP` | Before formula mitigation |
| Armor formula | `MITIGATION_SETUP` | Rebuilt into DN mitigation model |
| Protection | `MITIGATION_SETUP` | Vanilla enchantment damage protection |
| Resistance | `MITIGATION_SETUP` | Vanilla mob-effect reduction |
| Absorption / health / invulnerability | `POST_RESOURCE` | Observed after final damage application |

Important vanilla parity note:

- Strength and Weakness affect vanilla melee damage.
- Vanilla projectile arrow damage is determined when the projectile entity is created.
- The shooter’s Strength effect does not add damage when an arrow hits.

---

## Data-Driven Damage Rules

Damage rules are stored on item components, for example:

```snbt
minecraft:iron_sword[
  damagenexus:item_damage_rules=[
    {
      id:"damagenexus:test_physical_scaling",
      role:"offensive",
      phase:"type_scaling",
      priority:500,
      display:{
        name:"Physical Scaling",
        description:"+25% physical damage"
      },
      conditions:[
        {type:"damagenexus:always"}
      ],
      operations:[
        {
          type:"damagenexus:add_channel_pre_multiplier",
          channel:"damagenexus:physical",
          value:0.25
        }
      ],
      stacking:"stack",
      trace_label:"Physical Scaling"
    }
  ]
]
```

Another example:

```snbt
minecraft:golden_sword[
  damagenexus:item_damage_rules=[
    {
      id:"damagenexus:test_burning_edge",
      role:"offensive",
      phase:"conditional_multi",
      priority:500,
      display:{
        name:"Burning Edge",
        description:"+50% physical damage against burning targets"
      },
      conditions:[
        {type:"damagenexus:target_on_fire"}
      ],
      operations:[
        {
          type:"damagenexus:add_channel_post_multiplier",
          channel:"damagenexus:physical",
          value:0.50
        }
      ],
      stacking:"stack",
      trace_label:"Burning Edge"
    }
  ]
]
```

### Rule Fields

| Field | Meaning |
|---|---|
| `id` | Stable rule identifier |
| `role` | `offensive` or `defensive` |
| `phase` | Pipeline phase where the rule is evaluated |
| `priority` | Order inside phase |
| `display` | Static tooltip/display metadata |
| `conditions` | Runtime predicates required for execution |
| `operations` | Damage mutations applied by this rule |
| `stacking` | Stacking policy |
| `stacking_group` | Optional group for highest/unique stacking behavior |
| `trace_label` | Runtime trace name |

---

## Roles

Damage rules should not be classified only by item slot.

Instead, use:

```text
source location + effect role
```

Examples:

```text
Mainhand sword → offensive rule
Chestplate → defensive rule
Helmet → offensive bow-damage rule
Boots → conditional airborne offensive rule
Entity effect → offensive or defensive rule depending on effect
Projectile → offensive rule sourced from projectile context
```

`role` defines how the rule participates in the event. Slot defines where the rule was collected from.

---

## Damage Channels

DamageNexus supports registry-backed damage channels. Current development logs have shown channels such as:

```text
damagenexus:physical
damagenexus:fire
damagenexus:cold
damagenexus:lightning
damagenexus:magic
damagenexus:poison
damagenexus:wither
damagenexus:kinetic
```

Rules may add damage to a channel, scale a channel, convert one channel into another, or add a percentage of one channel as another channel.

Example channel operations:

```text
add_base_damage
add_channel_pre_multiplier
add_channel_post_multiplier
add_global_pre_multiplier
add_temporary_resistance
convert_channel
add_channel_as_channel
```

Exact operation names should follow the registered implementation names in code/datapack schemas.

---

## Stacking Rules

Damage rules support stacking behavior.

Known policies include:

```text
stack
highest_value
```

Example concept:

```snbt
{
  id:"damagenexus:test_stack_low",
  stacking:"highest_value",
  stacking_group:"damagenexus:test_physical_stack",
  operations:[{type:"damagenexus:add_channel_pre_multiplier", value:0.10}]
},
{
  id:"damagenexus:test_stack_high",
  stacking:"highest_value",
  stacking_group:"damagenexus:test_physical_stack",
  operations:[{type:"damagenexus:add_channel_pre_multiplier", value:0.30}]
}
```

In this case, only the strongest rule in the stacking group should apply.

---

## Debug Trace

DamageNexus should make every damage event explainable.

A trace should include:

- attacker;
- victim;
- direct source;
- original event damage;
- initial reconstructed base damage;
- source channel;
- vanilla bridge plan;
- phase execution order;
- processor run/skip status;
- collected rules;
- executed rules;
- skipped rules with reason;
- offensive per-channel totals;
- mitigation details;
- final event amount;
- post-damage health/absorption delta;
- mismatch classification.

Example trace concepts:

```text
[DN#1] BEGIN attacker=Dev victim=Zombie source=minecraft:player_attack channel=damagenexus:physical event_original=6.000 initial_base=6.000
[DN#1] PHASE BASE_MODIFICATION
[DN#1] [BASE_MODIFICATION] PROCESSOR_RUN processor=RuleExecutionProcessor priority=500
[DN#1] [BASE_MODIFICATION] RULE_COLLECT rule=damagenexus:tooltip_positive_fire_base provider=WEAPON_AFFIX role=OFFENSIVE slot=MAINHAND
[DN#1] [BASE_MODIFICATION] RULE_EXECUTE rule=damagenexus:tooltip_positive_fire_base trace_name=Tooltip +4 Fire
[DN#1] OFFENSE
[DN#1]   channel=damagenexus:physical base=5.000 offensive=8.500
[DN#1]   channel=damagenexus:fire base=4.000 offensive=5.440
[DN#1]   offensive_total=13.940
[DN#1] PHASE MITIGATION_SETUP
[DN#1] FINAL_OVERRIDE
[DN#1] APPLY event_original=6.000 initial_base=6.000 offensive_total=13.940 final_event_amount=13.940
[DN#1] POST victim=Zombie final_event_amount=13.940 health_before=13.442 health_after=0.000 observed_total_delta=13.442
```

Post-damage mismatch such as overkill cap is diagnostic information, not necessarily a pipeline error.

---

## Tooltip Direction

Tooltip text should distinguish numeric direction, rule category, and expand/collapse state.

Recommended conventions:

```text
+5 physical damage
+20% fire damage
-15% incoming physical damage
```

For categories:

```text
[Attack] +5 physical damage
[Defense] -15% incoming physical damage
[Condition] Against burning targets: +25% physical damage
[Vanilla] Sharpness V: +3 physical damage
```

Avoid using `[-] +N damage` for ordinary rule entries. `[-]` reads as negative, removed, or collapsed/expanded state and conflicts with positive values.

Use `[+]` and `[-]` only for group expand/collapse state:

```text
[+] Conditional Rules (2)
[-] Conditional Rules (2)
  Against burning targets: +25% physical damage
  Against undead targets: +5 physical damage
```

---

## Testing Utilities

Development logs indicate test generation for several categories:

```text
defense targets
enchantment bridge targets
mob effect targets
post classification targets
projectile bridge targets
mace bridge targets
spear bridge targets
mob difficulty attacker targets
```

Test kits include:

```text
base kit
enchantment kit
crit kit
channel kit
all test items
```

These tests should remain focused on pipeline correctness, trace readability, and vanilla-bridge parity before large-scale content/balance work begins.

---

## Development Priorities

### 1. Stabilize the Core Pipeline

- Keep phase order stable.
- Keep processor responsibilities narrow.
- Ensure `RuleExecutionProcessor` handles data-driven rules consistently.
- Ensure offensive, defensive, mitigation, override, and post-resource stages are traceable.

### 2. Standardize Rule Schema

- Finalize `role`, `phase`, `priority`, `conditions`, `operations`, `stacking`, `stacking_group`, and `trace_label` behavior.
- Keep tooltip display metadata separate from runtime logic.
- Avoid one-off operation names unless they represent a reusable operation class.

### 3. Normalize Vanilla Contributions

Prioritize stable vanilla adapters:

1. Sharpness
2. Smite / Bane of Arthropods
3. Strength / Weakness, melee only
4. Resistance
5. Protection
6. Armor effectiveness and armor formula
7. Projectile base damage
8. Power and projectile-specific scaling
9. Mace, spear, sweeping, thorns, and other special cases

### 4. Reduce Hot-Path Allocation

Avoid creating new rule definitions, display objects, conditions, and operations every hit for dynamic vanilla contributions. Prefer processors, cached templates, immutable rule forms, or direct pipeline mutations.

### 5. Improve Bridge Safety

- Prevent double counting when vanilla is replaced.
- Keep observe-only mode available for testing.
- Add cleanup for pending bridge state such as critical-hit targets.
- Prefer stable injection points over brittle redirects.

### 6. Finish Tooltip / Trace Separation

Static tooltip answers:

```text
What does this item/rule claim to do?
```

Runtime trace answers:

```text
What actually happened in this damage event?
```

These should share identifiers and display labels, but should not be the same system.

---

## Recommended Package Structure

A clean long-term package split could look like:

```text
io.github.rniamjeg.damagenexus
  api
    DamagePhase
    DamageRole
    DamageContext
    DamageChannel
    DamageRule
    DamageCondition
    DamageOperation

  core
    pipeline
      DamageNexusPipeline
      DamagePhaseProcessor
      RuleExecutionProcessor
    trace
      CombatTrace
      ICombatLogger
    math
      DamageMath
      MitigationMath

  rule
    component
      ItemDamageRulesComponent
    condition
    operation
    stacking
    tooltip

  vanilla
    VanillaDamageBridge
    VanillaOffensiveEnchantmentProcessor
    VanillaOffensiveMobEffectProcessor
    VanillaCriticalBridgeProcessor
    VanillaArmorEffectivenessProcessor
    VanillaDamageProtectionProcessor
    VanillaResistanceEffectProcessor
    VanillaProjectileScalingProcessor
    VanillaDifficultyScalingProcessor

  event
    IncomingDamageHandler
    PostDamageHandler

  mixin
    probe
    suppress
    redirect

  test
    DamageNexusTestCommands
    DamageNexusTestKits
```

Exact class names should follow the current source tree. The important rule is that `mixin` should not contain core damage math, and `vanilla` should normalize data into the pipeline rather than becoming a second pipeline.

---

## Non-Goals

DamageNexus should avoid these traps:

- Do not rebuild the entire combat engine.
- Do not scatter final damage math across mixins.
- Do not make every vanilla mechanic a special case outside the pipeline.
- Do not bind offensive/defensive behavior purely to equipment slot.
- Do not couple tooltip generation to runtime execution.
- Do not generate large numbers of dynamic rule objects on every attack.
- Do not apply vanilla contribution twice during bridge migration.

---

## Current One-Sentence Definition

**DamageNexus is a staged, data-driven damage resolution framework for Minecraft NeoForge that normalizes custom affixes, vanilla enchantments, vanilla mob effects, channels, mitigation, and final overrides into a single traceable damage pipeline.**

