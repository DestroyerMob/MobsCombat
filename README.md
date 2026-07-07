# Mobs Combat

Mobs Combat is a NeoForge 1.21.1 combat framework for Minecraft Beyond.

It adds a conservative, server-authoritative posture and shield-guard layer around vanilla damage events. The first slice deliberately avoids hostile mob animation assumptions, global AI rewrites, dodge rolls, lock-on, and client-side combat authority.

Pack overrides live in data JSON:

- `data/<namespace>/mobscombat/entity_combat_profiles/*.json`
- `data/<namespace>/mobscombat/weapon_combat_profiles/*.json`

Broad compatibility uses item and entity type tags under `mobscombat:combat/*`, `mobscombat:weapons/*`, and `mobscombat:shields/*`.
