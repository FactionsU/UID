package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FactionRelationEvent;
import dev.kitteh.factions.event.FactionRelationWishEvent;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.scoreboard.FTeamWrapper;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;

import java.util.function.BiConsumer;

public class CmdRelation implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> relationBuilder = builder
                    .literal("relation")
                    .commandDescription(Description.of(TL.COMMAND_RELATIONS_DESCRIPTION.toString()))
                    .permission(builder.commandPermission().and(Cloudy.isAtLeastRole(Role.MODERATOR)))
                    .required("faction", FactionParser.of(FactionParser.Include.PLAYERS));

            manager.command(relationBuilder.literal("ally").handler(ctx -> this.handleRelation(ctx, Relation.ALLY)));
            manager.command(relationBuilder.literal("true").handler(ctx -> this.handleRelation(ctx, Relation.TRUCE)));
            manager.command(relationBuilder.literal("neutral").handler(ctx -> this.handleRelation(ctx, Relation.NEUTRAL)));
            manager.command(relationBuilder.literal("enemy").handler(ctx -> this.handleRelation(ctx, Relation.ENEMY)));
        };
    }

    private void handleRelation(CommandContext<Sender> context, Relation targetRelation) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.getFaction();

        Faction them = context.get("faction");


        if (!them.isNormal()) {
            sender.msg(TL.COMMAND_RELATIONS_ALLTHENOPE);
            return;
        }

        if (them == faction) {
            sender.msg(TL.COMMAND_RELATIONS_MORENOPE);
            return;
        }

        if (faction.getRelationWish(them) == targetRelation) {
            sender.msg(TL.COMMAND_RELATIONS_ALREADYINRELATIONSHIP, them.getTag());
            return;
        }

        if (hasMaxRelations(them, targetRelation, faction, sender)) {
            // We message them down there with the count.
            return;
        }
        Relation oldRelation = faction.getRelationTo(them, true);
        FactionRelationWishEvent wishEvent = new FactionRelationWishEvent(sender, faction, them, oldRelation, targetRelation);
        Bukkit.getPluginManager().callEvent(wishEvent);
        if (wishEvent.isCancelled()) {
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(targetRelation.getRelationCost(), TL.COMMAND_RELATIONS_TOMARRY, TL.COMMAND_RELATIONS_FORMARRY)) {
            return;
        }

        // try to set the new relation
        faction.setRelationWish(them, targetRelation);
        Relation currentRelation = faction.getRelationTo(them, true);
        ChatColor currentRelationColor = currentRelation.chatColor();

        // if the relation change was successful
        if (targetRelation.value == currentRelation.value) {
            // trigger the faction relation event
            FactionRelationEvent relationEvent = new FactionRelationEvent(faction, them, oldRelation, currentRelation);
            Bukkit.getServer().getPluginManager().callEvent(relationEvent);

            them.msg(TL.COMMAND_RELATIONS_MUTUAL, currentRelationColor + targetRelation.translation(), currentRelationColor + faction.getTag());
            faction.msg(TL.COMMAND_RELATIONS_MUTUAL, currentRelationColor + targetRelation.translation(), currentRelationColor + them.getTag());
        } else {
            // inform the other faction of your request
            them.msg(TL.COMMAND_RELATIONS_PROPOSAL_1, currentRelationColor + faction.getTag(), targetRelation.chatColor() + targetRelation.translation());
            them.msg(TL.COMMAND_RELATIONS_PROPOSAL_2, FactionsPlugin.getInstance().conf().getCommandBase().getFirst(), targetRelation, faction.getTag());
            faction.msg(TL.COMMAND_RELATIONS_PROPOSAL_SENT, currentRelationColor + them.getTag(), "" + targetRelation.chatColor() + targetRelation);
        }

        if (!targetRelation.isNeutral() && them.isPeaceful()) {
            them.msg(TL.COMMAND_RELATIONS_PEACEFUL);
            faction.msg(TL.COMMAND_RELATIONS_PEACEFULOTHER);
        }

        if (!targetRelation.isNeutral() && faction.isPeaceful()) {
            them.msg(TL.COMMAND_RELATIONS_PEACEFULOTHER);
            faction.msg(TL.COMMAND_RELATIONS_PEACEFUL);
        }

        FTeamWrapper.updatePrefixes(faction);
        FTeamWrapper.updatePrefixes(them);
    }

    private boolean hasMaxRelations(Faction them, Relation targetRelation, Faction us, FPlayer sender) {
        if (FactionsPlugin.getInstance().conf().factions().maxRelations().isEnabled()) {
            int max = targetRelation.getMax();
            if (max != -1) {
                if (us.getRelationCount(targetRelation) >= max) {
                    sender.msg(TL.COMMAND_RELATIONS_EXCEEDS_ME, max, targetRelation.getPluralTranslation());
                    return true;
                }
                if (them.getRelationCount(targetRelation) >= max) {
                    sender.msg(TL.COMMAND_RELATIONS_EXCEEDS_THEY, max, targetRelation.getPluralTranslation());
                    return true;
                }
            }
        }
        return false;
    }
}
