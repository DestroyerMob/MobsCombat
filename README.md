# Mobs Combat

Mobs Combat is a NeoForge 1.21.1 combat framework for Minecraft Beyond.

It adds a conservative, server-authoritative posture and shield-guard layer around vanilla damage events. The first slice deliberately avoids hostile mob animation assumptions, global AI rewrites, dodge rolls, lock-on, and client-side combat authority.

Pack overrides live in data JSON:

- `data/<namespace>/mobscombat/entity_combat_profiles/*.json`
- `data/<namespace>/mobscombat/weapon_combat_profiles/*.json`

Broad compatibility uses item and entity type tags under `mobscombat:combat/*`, `mobscombat:weapons/*`, and `mobscombat:shields/*`.

## Current combat direction

- Block feedback is explicit: perfect blocks, guard breaks, parries, and stealth strikes send short client action-bar indicators, with particles/sounds still used for world feedback.
- Parry is a timed weapon strike into an incoming direct melee hit. A successful parry reduces incoming damage by config, applies posture pressure to the attacker, and opens the counter window. Daggers get a configurable timing bonus.
- Stealth keeps vanilla's range-based hostile search, then reduces sneaking-player visibility and cancels target acquisition when the player is outside a hostile mob's configurable vision cone. Stealth strikes apply to direct melee attacks from hidden sneaking players; daggers get a stronger configured bonus.

## Dual-wielding

Vanilla already stores an offhand item, but it does not provide a true second melee attack loop. Mobs Combat routes valid offhand weapon attacks through vanilla's normal main-hand attack calculation for one server attack, then restores the hands immediately after the hit resolves.

When both hands hold recognized weapons, attacks alternate between main hand and offhand. Dual wielding also applies a configurable attack-speed modifier so the cooldown is about 65% of normal by default. When the main hand is empty or not a recognized weapon but the offhand is valid, the offhand weapon handles the hit instead of punching.
