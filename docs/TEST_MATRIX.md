# Combat Playtest Matrix

Run this matrix in a fresh local world after changing combat profiles, balance defaults, input handling, or network code. Use `/mobscombat inspect <target>` before and after each case to confirm the server resolved the expected entity and weapon profiles.

## Baseline Profiles

| Scenario | Expected result |
| --- | --- |
| MTF sword | Resolves as `json:#mobscombat:weapons/slashing`; it receives the configured counter posture multiplier after a successful block. |
| MTF axe or mattock | Resolves as `json:#mobscombat:weapons/chopping`; its higher guard pressure is visible in shield testing. |
| MoreWeapons knife | Resolves as the dagger profile and receives its higher counter posture multiplier. |
| MoreWeapons great sword, battle axe, machete, and spear | Each resolves to its explicit family profile rather than `fallback:generic_item`. |
| Vanilla sword, axe, mace, trident | Resolves to slash, chop, blunt, and pierce respectively. |

## Defense

| Scenario | Steps | Expected result |
| --- | --- | --- |
| Shield block counter | Block a melee hit with a raised shield, then run `/mobscombat inspect @s`. | The block consumes guard and opens the configured counter window regardless of how long the shield was raised. |
| Block-capable weapon counter | Successfully block with a weapon that provides real blocking behavior. | The block opens the same counter window as a shield; a block animation without actual blocking does not qualify. |
| Perfect block | Raise a shield immediately before a hit, then repeat after holding it up. | Both blocks open counters; only the first receives perfect-block guard efficiency and posture pressure. |
| Guard break | Allow guard to deplete. | Blocking fails during guard-break cooldown and guard recovers only after its delay. |

## Core Combat

| Scenario | Expected result |
| --- | --- |
| Posture break | Repeated full hits reduce a hostile's posture, then apply the configured stagger/recovery behavior. |
| Counter | Land a melee attack after a successful block. | The attack receives the weapon profile's counter posture multiplier and consumes the counter, so later attacks do not retain it. |
| Optional parry integration state | After a successful block, inspect the server combat state from an optional integration. | `hasParryCounterWindow()` remains true until the configured window expires or the next melee hit consumes it; `successfulParryCount()` increments once per successful block. |
| Katana stance parry | With a MoreWeapons katana equipped and no shield, enter its use stance immediately before a frontal melee hit, then counterattack. Repeat with a late stance and an attack from behind. | The opening stance window negates the frontal hit, damages attacker posture, releases the stance, and opens one counter; late and rear attacks land normally. Moonlit Reversal sees the successful parry sequence. |
| Stealth | Strike an unaware hostile while sneaking, then repeat inside close awareness range. | Only the unseen strike gets stealth damage and posture bonuses. |
| Dual wield | Attack with two recognised weapons, then repeat with a utility item in off-hand. | Valid weapon pairs alternate hands; utility off-hand preserves normal attacks and use actions. |
| Boat | Attack from a boat with and without dual weapons. | Normal boat attacks remain available; no stuck attack state or forced finisher occurs. |

## Multiplayer And Compatibility

| Scenario | Expected result |
| --- | --- |
| Dedicated server | Join a server with the same mod version and block/counter repeatedly under normal latency. | The server decides every block counter and consumes it on the first landed melee follow-up. |
| Reload | Run `/reload`, then repeat the profile checks. | Profile sources remain correct after data reload. |
| Optional mods | Test with and without Punchy, Jade, and Apotheosis. | Combat remains functional; optional feedback or visuals fail closed rather than breaking attacks. |
| Better Combat absent | Attack, dual wield, counter, stealth strike, and posture-break using only Mobs Combat. | The standalone client attack and hand-routing paths remain active. |
| Better Combat normal combo | Complete a full Better Combat combo against one target. | Every landed hit applies the resolved Mobs Combat posture profile; counter and stealth bonuses are consumed normally. |
| Better Combat multi-target swing | Hit several targets with one Better Combat arc. | Each valid target receives one health hit and one posture hit; later targets are not downgraded merely because they were later in the same swing. |
| Better Combat dual wield | Alternate two Better Combat-compatible one-handed weapons. | Better Combat owns hand selection, damage, cooldown and animation; Mobs Combat does not add a second speed modifier or duplicate hit packet. |
| Better Combat attributed fallback | Hold a Better Combat-attributed weapon with no Mobs Combat item tag or JSON profile and run `/mobscombat inspect @s`. | The weapon resolves from `bettercombat:<category>` rather than `fallback:generic_item`. |
