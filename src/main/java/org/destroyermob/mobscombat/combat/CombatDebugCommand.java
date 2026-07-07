package org.destroyermob.mobscombat.combat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.destroyermob.mobscombat.MobsCombat;

public final class CombatDebugCommand {
    private CombatDebugCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(MobsCombat.MOD_ID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("posture")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(CombatDebugCommand::showPosture))));
    }

    private static int showPosture(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(context, "target");
        if (!(target instanceof LivingEntity living)) {
            context.getSource().sendFailure(Component.literal("Target is not a living entity."));
            return 0;
        }

        ResolvedEntityProfile resolved = CombatProfileResolver.resolve(living);
        CombatState state = CombatStateManager.get(living);
        float current = state == null ? PostureSystem.maxPosture(living, resolved.profile()) : state.currentPosture();
        float max = state == null ? PostureSystem.maxPosture(living, resolved.profile()) : state.maxPosture();
        int stagger = state == null ? 0 : state.staggerTicks();
        int recovery = state == null ? 0 : state.recoveryTicks();
        context.getSource().sendSuccess(() -> Component.literal(
                "Combat " + net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(living.getType())
                        + " posture=" + format(current) + "/" + format(max)
                        + " stagger=" + stagger
                        + " recovery=" + recovery
                        + " archetype=" + resolved.profile().archetype()
                        + " source=" + resolved.source()
        ), false);
        return Command.SINGLE_SUCCESS;
    }

    private static String format(float value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
