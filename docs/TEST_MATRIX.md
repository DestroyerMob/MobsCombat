# Combat Playtest Matrix

Run this matrix in a fresh local world after changing combat profiles, balance defaults, input handling, or network code. Use `/mobscombat inspect <target>` before and after each case to confirm the server resolved the expected entity and weapon profiles.

## Baseline Profiles

| Scenario | Expected result |
| --- | --- |
| MTF sword | Resolves as `json:#mobscombat:weapons/slashing`; the Parry key can arm it. |
| MTF axe or mattock | Resolves as `json:#mobscombat:weapons/chopping`; its higher guard pressure is visible in shield testing. |
| MoreWeapons knife | Resolves as the dagger profile and receives the configured parry readiness bonus. |
| MoreWeapons great sword, battle axe, machete, and spear | Each resolves to its explicit family profile rather than `fallback:generic_item`. |
| Vanilla sword, axe, mace, trident | Resolves to slash, chop, blunt, and pierce respectively. |

## Defense

| Scenario | Steps | Expected result |
| --- | --- | --- |
| Parry readiness | Hold a recognised weapon, press the Parry key, then run `/mobscombat inspect @s`. | `parry_ready` starts near the configured window and `parry_cooldown` prevents immediate re-arming. |
| Untargeted parry | Arm parry while looking away from a zombie, then let the zombie land a direct melee hit. | The hit is cancelled or reduced by `parry_damage_multiplier`, the zombie takes posture damage, and a counter window opens. No prior strike against that zombie is required. |
| Dagger parry | Repeat with a recognised dagger. | Readiness lasts the base window plus the dagger bonus. |
| Expired parry | Wait until `parry_ready` reaches zero, then take a direct melee hit. | Damage is not parried. |
| Shield block | Raise a shield immediately before a melee hit, then repeat after holding it up. | The first case is a perfect block; the second consumes normal guard. |
| Guard break | Allow guard to deplete. | Blocking fails during guard-break cooldown and guard recovers only after its delay. |

## Core Combat

| Scenario | Expected result |
| --- | --- |
| Posture break | Repeated full hits reduce a hostile's posture, then apply the configured stagger/recovery behavior. |
| Counter | Attack during the counter window from a successful parry or perfect block. | The attack receives the weapon profile's counter posture multiplier. |
| Optional parry integration state | After a successful parry, inspect the server combat state from an optional integration. | `hasParryCounterWindow()` remains true for the configured counter window and `successfulParryCount()` increments exactly once. A later perfect block marks the active counter as non-parry. |
| Stealth | Strike an unaware hostile while sneaking, then repeat inside close awareness range. | Only the unseen strike gets stealth damage and posture bonuses. |
| Dual wield | Attack with two recognised weapons, then repeat with a utility item in off-hand. | Valid weapon pairs alternate hands; utility off-hand preserves normal attacks and use actions. |
| Boat | Attack from a boat with and without dual weapons. | Normal boat attacks remain available; no stuck attack state or forced finisher occurs. |

## Multiplayer And Compatibility

| Scenario | Expected result |
| --- | --- |
| Dedicated server | Join a server with the same mod version and parry repeatedly under normal latency. | The server, not the client, decides every parry; no double hits or client-only feedback occurs. |
| Reload | Run `/reload`, then repeat the profile checks. | Profile sources remain correct after data reload. |
| Optional mods | Test with and without Punchy, Jade, and Apotheosis. | Combat remains functional; optional feedback or visuals fail closed rather than breaking attacks. |
