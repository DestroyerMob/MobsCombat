# Mobs Combat

Mobs Combat is a compatibility-first combat framework for Minecraft 1.21.1 on NeoForge. It adds server-authoritative posture, guard, timed defense, stealth, and dual-wield systems around vanilla damage and targeting instead of replacing every mob AI or moving combat authority to the client.

## Current Status

Active internal playtesting in Minecraft Beyond. The current slice is intentionally conservative: it works through damage events, attributes, tags, data profiles, and narrowly scoped mixins. Lock-on, dodge rolls, global hostile-AI rewrites, and assumptions about third-party animation systems are out of scope.

## Project Facts

- Mod id: `mobscombat`
- Current version: `0.1.0-1.21.1-neoforge`
- Target: Minecraft 1.21.1, NeoForge 21.1.234, Java 21
- Common config: `config/mobscombat-common.toml`
- Optional integrations: Jade, Punchy, and Apotheosis/Apothic Attributes data exposed through Jade

## Combat Systems

- Living entities receive a posture pool derived from health and a data-driven combat profile. Weapon profiles define posture and guard pressure, damage kind, recovery punishment, knockback, and stagger capability.
- Empty posture opens soft or hard stagger and a recovery window. Boss-like entities default to shorter, safer staggers, and hard stagger can be disabled globally or per entity profile.
- Players can also use posture. When damaged posture is active, a dedicated HUD bar temporarily replaces the experience bar until posture recovers.
- Shields use a separate guard pool with recovery delay, guard break, a configurable perfect-block window, and a counter window.
- Parrying is a timed weapon strike into an incoming direct melee hit. Daggers receive a configurable timing bonus; a successful parry can cancel damage, pressure the attacker, and open a counter.
- Sneaking reduces hostile visibility outside a configurable close-awareness radius and vision cone. Direct hidden melee attacks gain stealth-strike damage and posture pressure, with a stronger dagger bonus.
- Combat feedback for perfect blocks, guard breaks, parries, and stealth strikes is sent to the client as short action-bar messages alongside world sound and particles.

## Dual Wielding

Mobs Combat turns the existing offhand slot into a real second melee attack path for recognized weapons. When both hands hold valid weapons, attacks alternate between hands, use the selected stack's attack attributes, and apply configurable cooldown and damage multipliers. An offhand weapon can also attack when the main hand is empty or not recognized as a weapon.

The attack remains server-authoritative: the client requests a hand and target, the server validates range, cooldown, target, and held stacks, then routes the hit through the normal player attack calculation with the hands restored immediately afterward.

When Punchy is present, Mobs Combat coordinates a three-beat visual sequence: main-hand slash, mirrored offhand slash, then a dual thrust. The thrust can act as a configurable damage/posture finisher. Mounted boat attacks use a guarded fallback so normal attacks remain available without forcing the dual-thrust animation.

## Data-Driven Compatibility

Pack and datapack overrides live under:

```text
data/<namespace>/mobscombat/entity_combat_profiles/*.json
data/<namespace>/mobscombat/weapon_combat_profiles/*.json
```

Broad compatibility uses item and entity tags under:

```text
mobscombat:combat/*
mobscombat:weapons/*
mobscombat:shields/*
```

Built-in weapon families cover great swords, battle axes, daggers, machetes, spears, blunt, chopping, slashing, and piercing weapons. Entity tags classify common archetypes and provide opt-outs for posture, hard stagger, knockback changes, or combat profiling.

## Optional Integrations

- Jade shows posture, guard, armor, toughness, stagger/recovery/counter state, archetype, and profile source for living entities.
- When Apotheosis data is available, Jade also reports world-tier armor and toughness adjustments without creating a hard dependency.
- Punchy integration is reflective and conditional so Mobs Combat continues to load when Punchy is absent or changes incompatibly.

## Configuration

The common config can independently disable the overall overhaul, posture, player or PvP posture, hard stagger, shield guard, timed blocks, parry, stealth, dual wielding, recovery windows, and debug messages. It also exposes the main timing and balance values for posture recovery, stagger, guard, parry, counters, stealth visibility/strikes, and dual-wield cooldown, damage, and finisher multipliers.

Defaults keep the systems enabled except PvP posture, boss hard stagger, and debug messages.

## Diagnostics

Operators can inspect an entity's resolved profile and live posture state with:

```text
/mobscombat posture <target>
```

## Building

```sh
./gradlew build
```

The built jar is written to `build/libs/`.

## Known Limitations

- Balance is tuned for Minecraft Beyond playtesting and is not a public compatibility guarantee.
- True interruption is deliberately limited; many hostile actions are softened through movement and outgoing-damage penalties instead of animation-specific cancellation.
- Punchy and Apotheosis support depend on optional implementation details and are designed to fail closed when those details cannot be resolved.
- Data profiles currently cover the pack's known weapon and entity families; other mods may need tags or JSON overrides.

## License

MIT.
