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
import dev.kitteh.factions.util.TextUtil;
import net.kyori.adventure.text.format.TextColor;
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
        Faction faction = sender.faction();

        Faction them = context.get("faction");


        if (!them.isNormal()) {
            sender.msg(TL.COMMAND_RELATIONS_ALLTHENOPE);
            return;
        }

        if (them == faction) {
            sender.msg(TL.COMMAND_RELATIONS_MORENOPE);
            return;
        }

        if (faction.relationWish(them) == targetRelation) {
            sender.msg(TL.COMMAND_RELATIONS_ALREADYINRELATIONSHIP, them.tag());
            return;
        }

        if (hasMaxRelations(them, targetRelation, faction, sender)) {
            // We message them down there with the count.
            return;
        }
        Relation oldRelation = faction.relationTo(them, true);
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
        faction.relationWish(them, targetRelation);
        Relation currentRelation = faction.relationTo(them, true);
        String currentRelationColor = TextUtil.getString(currentRelation.color());
        String targetRelationColor = TextUtil.getString(targetRelation.color());

        // if the relation change was successful
        if (targetRelation.value == currentRelation.value) {
            // trigger the faction relation event
            FactionRelationEvent relationEvent = new FactionRelationEvent(faction, them, oldRelation, currentRelation);
            Bukkit.getServer().getPluginManager().callEvent(relationEvent);

            them.msg(TL.COMMAND_RELATIONS_MUTUAL, currentRelationColor + targetRelation.translation(), currentRelationColor + faction.tag());
            faction.msg(TL.COMMAND_RELATIONS_MUTUAL, currentRelationColor + targetRelation.translation(), currentRelationColor + them.tag());
        } else {
            // inform the other faction of your request
            them.msg(TL.COMMAND_RELATIONS_PROPOSAL_1, currentRelationColor + faction.tag(), targetRelationColor + targetRelation.translation());
            them.msg(TL.COMMAND_RELATIONS_PROPOSAL_2, FactionsPlugin.instance().conf().getCommandBase().getFirst(), targetRelation, faction.tag());
            faction.msg(TL.COMMAND_RELATIONS_PROPOSAL_SENT, currentRelationColor + them.tag(), targetRelationColor + targetRelation);
        }

        if (!targetRelation.isNeutral() && them.peaceful()) {
            them.msg(TL.COMMAND_RELATIONS_PEACEFUL);
            faction.msg(TL.COMMAND_RELATIONS_PEACEFULOTHER);
        }

        if (!targetRelation.isNeutral() && faction.peaceful()) {
            them.msg(TL.COMMAND_RELATIONS_PEACEFULOTHER);
            faction.msg(TL.COMMAND_RELATIONS_PEACEFUL);
        }

        FTeamWrapper.updatePrefixes(faction);
        FTeamWrapper.updatePrefixes(them);
    }

    private boolean hasMaxRelations(Faction them, Relation targetRelation, Faction us, FPlayer sender) {
        if (FactionsPlugin.instance().conf().factions().maxRelations().isEnabled()) {
            int max = targetRelation.getMax();
            if (max != -1) {
                if (us.relationCount(targetRelation) >= max) {
                    sender.msg(TL.COMMAND_RELATIONS_EXCEEDS_ME, max, targetRelation.getPluralTranslation());
                    return true;
                }
                if (them.relationCount(targetRelation) >= max) {
                    sender.msg(TL.COMMAND_RELATIONS_EXCEEDS_THEY, max, targetRelation.getPluralTranslation());
                    return true;
                }
            }
        }
        return false;
    }
}
