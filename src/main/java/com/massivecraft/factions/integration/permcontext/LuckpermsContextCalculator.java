package com.massivecraft.factions.integration.permcontext;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.entity.Player;

public class LuckpermsContextCalculator implements ContextCalculator<Player> {
    @Override
    public void calculate(Player player, ContextConsumer contextConsumer) {
        for (Context context : ContextManager.getContexts()) {
            for (String value : context.getValues(player)) {
                contextConsumer.accept(context.getNamespacedName(), value);
            }
        }
    }

    @Override
    public ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        for (Context context : ContextManager.getContexts()) {
            for (String value : context.getPossibleValues()) {
                builder.add(context.getNamespacedName(), value);
            }
        }
        return builder.build();
    }
}
