# Mobs Combat

Mobs Combat is a compatibility-first combat framework for Minecraft 1.21.1 on NeoForge. It adds server-authoritative posture, guard, timed defense, stealth, and dual-wield systems around vanilla damage and targeting instead of replacing every mob AI or moving combat authority to the client.

## Current Status

Active internal playtesting in Minecraft Beyond. The current slice is intentionally conservative: it works through damage events, attributes, tags, data profiles, and narrowly scoped mixins. Lock-on, dodge rolls, global hostile-AI rewrites, and assumptions about third-party animation systems are out of scope.

## Project Facts

- Mod id: `mobscombat`
- Current version: `0.1.0-1.21.1-neoforge`
- Target: Minecraft 1.21.1, NeoForge 21.1.234, Java 21
- Common config: `config/mobscombat-common.toml`
- Optional integrations: Better Combat, Jade, Punchy, and Apotheosis/Apothic Attributes data exposed through Jade

## Combat Systems

- Living entities receive a posture pool derived from health and a data-driven combat profile. Weapon profiles define posture and guard pressure, damage kind, recovery punishment, knockback, and stagger capability.
- Empty posture opens soft or hard stagger and a recovery window. Boss-like entities default to shorter, safer staggers, and hard stagger can be disabled globally or per entity profile.
- Players can also use posture. When damaged posture is active, a dedicated HUD bar temporarily replaces the experience bar until posture recovers.
- Shields and genuinely block-capable weapons use a separate guard pool with recovery delay and guard break. Every successful block opens a one-hit counter window; the configurable perfect-block window only improves guard efficiency and adds posture pressure.
- Parry is the block-and-counter sequence rather than a separate input. The first successful melee follow-up consumes the counter and uses the weapon profile's counter posture multiplier. Items in `mobscombat:weapons/parry_weapons`, including MoreWeapons katanas, use the opening frames of their use stance to deflect a frontal melee hit without a shield; a success releases the stance and opens the same counter window.
- Sneaking reduces hostile visibility outside a configurable close-awareness radius and vision cone. Direct hidden melee attacks gain stealth-strike damage and posture pressure, with a stronger dagger bonus.
- Combat feedback for perfect blocks, guard breaks, parries, and stealth strikes is sent to the client as short action-bar messages alongside world sound and particles.

## Dual Wielding

Mobs Combat turns the existing offhand slot into a real second melee attack path for recognized weapons. When both hands hold valid weapons, attacks alternate between hands, use the selected stack's attack attributes, and apply configurable cooldown and damage multipliers. An offhand weapon can also attack when the main hand is empty or not recognized as a weapon.

Items in `mobscombat:weapons/two_handed` never enter the dual-wield path when held in either hand. The built-in tag includes great swords, battle axes, and spears, and datapacks can extend it for additional weapon families.

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

- Better Combat can own normal attack input, combos, range, multi-target delivery, animation, and dual-wield hand swapping. Mobs Combat keeps posture/stagger, guard/counter-parry, recovery vulnerability, stealth strikes, headshots, profiles, and HUD state server-authoritative. Better Combat weapon categories provide a final profile fallback for weapons not already covered by Mobs Combat JSON or tags. Without Better Combat, Mobs Combat's existing vanilla attack and dual-wield paths remain enabled.
- Jade shows posture, guard, armor, toughness, stagger/recovery/counter state, archetype, and profile source for living entities.
- When Apotheosis data is available, Jade also reports world-tier armor and toughness adjustments without creating a hard dependency.
- Punchy integration is reflective and conditional so Mobs Combat continues to load when Punchy is absent or changes incompatibly.

## Configuration

The common config can independently disable the overall overhaul, posture, player or PvP posture, hard stagger, shield guard, timed blocks, block counters, stealth, dual wielding, recovery windows, and debug messages. It also exposes the main timing and balance values for posture recovery, stagger, guard, counters, stealth visibility/strikes, and dual-wield cooldown, damage, and finisher multipliers.

Defaults keep the systems enabled except PvP posture, boss hard stagger, and debug messages.

## Diagnostics

Operators can inspect an entity's resolved profile and live posture state with:

```text
/mobscombat posture <target>
/mobscombat inspect <target>
```

`inspect` also reports the resolved main- and off-hand weapon sources plus guard, recovery, and counter state. See `docs/TEST_MATRIX.md` for the pack playtest pass.

## Building

```sh
./gradlew build
```

The built jar is written to `build/libs/`.

## Known Limitations

- Balance is tuned for Minecraft Beyond playtesting and is not a public compatibility guarantee.
- True interruption is deliberately limited; many hostile actions are softened through movement and outgoing-damage penalties instead of animation-specific cancellation.
- Better Combat support targets its public weapon registry and vanilla server attack path. Punchy and Apotheosis support depend on optional implementation details and are designed to fail closed when those details cannot be resolved.
- Data profiles currently cover the pack's known weapon and entity families; other mods may need tags or JSON overrides.

## License

MIT.
