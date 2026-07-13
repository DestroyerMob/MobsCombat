package org.destroyermob.mobscombat.integration.bettercombat;

import java.util.Locale;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobscombat.combat.CombatDamageKind;
import org.destroyermob.mobscombat.combat.ResolvedWeaponProfile;
import org.destroyermob.mobscombat.combat.WeaponCombatProfile;

/** Direct API calls isolated behind {@link BetterCombatCompat}'s load check. */
final class BetterCombatBridge {
    private static final WeaponCombatProfile GREAT_WEAPON = new WeaponCombatProfile(
            CombatDamageKind.SLASH, 11.0F, 11.0F, 0.35F, 1.0F, 1.35F, 1.25F, 1.05F, true, true
    );
    private static final WeaponCombatProfile DAGGER = new WeaponCombatProfile(
            CombatDamageKind.PIERCE, 4.0F, 4.0F, 0.55F, 1.0F, 1.5F, 1.35F, 0.8F, false, true
    );
    private static final WeaponCombatProfile PIERCING = new WeaponCombatProfile(
            CombatDamageKind.PIERCE, 7.0F, 7.0F, 0.45F, 1.0F, 1.4F, 1.3F, 1.0F, true, true
    );

    private BetterCombatBridge() {
    }

    static ResolvedWeaponProfile resolveWeaponProfile(ItemStack stack) {
        var attributes = WeaponRegistry.getAttributes(stack);
        if (attributes == null || attributes.attacks() == null || attributes.attacks().length == 0) {
            return null;
        }

        String category = attributes.category() == null
                ? "weapon"
                : attributes.category().trim().toLowerCase(Locale.ROOT);
        WeaponCombatProfile profile = switch (category) {
            case "claymore", "glaive", "scythe", "twin_blade" -> GREAT_WEAPON;
            case "dagger", "soul_knife", "claw" -> DAGGER;
            case "spear", "trident", "rapier", "lance" -> PIERCING;
            case "axe", "double_axe", "heavy_axe", "halberd", "pickaxe" -> WeaponCombatProfile.AXE;
            case "mace", "hammer", "anchor", "staff", "battlestaff" -> WeaponCombatProfile.MACE;
            default -> WeaponCombatProfile.SWORD;
        };
        return new ResolvedWeaponProfile(profile, "bettercombat:" + category, true);
    }
}
