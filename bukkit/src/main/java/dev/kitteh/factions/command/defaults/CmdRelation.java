package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.*;
import dev.kitteh.factions.event.FactionRelationEvent;
import dev.kitteh.factions.event.FactionRelationWishEvent;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.scoreboard.FTeamWrapper;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;

import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdRelation implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().relation();
            Command.Builder<Sender> build = builder
                    .literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Description.of(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.isAtLeastRole(Role.MODERATOR)));

            Command.Builder<Sender> relationBuilder = build.required("faction", FactionParser.of(FactionParser.Include.PLAYERS));

            manager.command(relationBuilder.literal(tl.getCommandAlly()).handler(ctx -> this.handleRelation(ctx, Relation.ALLY)));
            manager.command(relationBuilder.literal(tl.getCommandTruce()).handler(ctx -> this.handleRelation(ctx, Relation.TRUCE)));
            manager.command(relationBuilder.literal(tl.getCommandNeutral()).handler(ctx -> this.handleRelation(ctx, Relation.NEUTRAL)));
            manager.command(relationBuilder.literal(tl.getCommandEnemy()).handler(ctx -> this.handleRelation(ctx, Relation.ENEMY)));

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx ->
                    help.queryCommands(Cmd.rootCommand() + " " + tl.getFirstAlias() + " <faction>", ctx.sender()))
            );
        };
    }

    private void handleRelation(CommandContext<Sender> context, Relation targetRelation) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        Faction them = context.get("faction");

        var tl = FactionsPlugin.instance().tl().commands().relation();

        if (!them.isNormal()) {
            sender.sendRichMessage(tl.getDenySystemFaction());
            return;
        }

        if (them == faction) {
            sender.sendRichMessage(tl.getDenySelfFaction());
            return;
        }

        if (faction.relationWish(them) == targetRelation) {
            sender.sendRichMessage(tl.getDenyAlreadySet(), FactionResolver.of(them));
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
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        if (!context.sender().payForCommand(targetRelation.getRelationCost(), econTl.getRelationTo(), econTl.getRelationFor())) {
            return;
        }

        // try to set the new relation
        faction.relationWish(them, targetRelation);
        Relation currentRelation = faction.relationTo(them, true);

        // if the relation change was successful
        if (targetRelation.value == currentRelation.value) {
            // trigger the faction relation event
            FactionRelationEvent relationEvent = new FactionRelationEvent(faction, them, oldRelation, currentRelation);
            Bukkit.getServer().getPluginManager().callEvent(relationEvent);

            them.sendRichMessage(tl.getUpdated(), FactionResolver.of(faction), Placeholder.component("relation", Component.text(currentRelation.translation(), currentRelation.color())));
            faction.sendRichMessage(tl.getUpdated(), FactionResolver.of(them), Placeholder.component("relation", Component.text(currentRelation.translation(), currentRelation.color())));
        } else {
            // inform the other faction of your request
            String command = Cmd.rootCommand() + " " + tl.getFirstAlias() + " " + faction.tag() + " "
                    + switch (targetRelation) {
                case ALLY -> tl.getCommandAlly();
                case TRUCE -> tl.getCommandTruce();
                case ENEMY -> tl.getCommandEnemy();
                default -> tl.getCommandNeutral();
            };
            them.sendRichMessage(tl.getProposal(),
                    FactionResolver.of(faction),
                    Placeholder.component("relation", Component.text(targetRelation.translation(), targetRelation.color())),
                    Placeholder.parsed("command", command)
            );
            faction.sendRichMessage(tl.getProposalSent(), FactionResolver.of(faction), Placeholder.component("relation", Component.text(targetRelation.translation(), targetRelation.color())));
        }

        if (!targetRelation.isNeutral() && them.isPeaceful()) {
            them.sendRichMessage(tl.getPeacefulSelf());
            faction.sendRichMessage(tl.getPeacefulThem());
        }

        if (!targetRelation.isNeutral() && faction.isPeaceful()) {
            them.sendRichMessage(tl.getPeacefulThem());
            faction.sendRichMessage(tl.getPeacefulSelf());
        }

        FTeamWrapper.updatePrefixes(faction);
        FTeamWrapper.updatePrefixes(them);
    }

    private boolean hasMaxRelations(Faction them, Relation targetRelation, Faction us, FPlayer sender) {
        if (FactionsPlugin.instance().conf().factions().maxRelations().isEnabled()) {
            int max = targetRelation.getMax();
            if (max != -1) {
                var tl = FactionsPlugin.instance().tl().commands().relation();
                String relation = max == 1 ? targetRelation.translation() : targetRelation.getPluralTranslation();
                if (us.relationCount(targetRelation) >= max) {
                    sender.sendRichMessage(tl.getRelationLimitSelf(), FactionResolver.of(us), Placeholder.unparsed("limit", String.valueOf(max)), Placeholder.component("relation", Component.text(relation, targetRelation.color())));
                    return true;
                }
                if (them.relationCount(targetRelation) >= max) {
                    sender.sendRichMessage(tl.getRelationLimitSelf(), FactionResolver.of(them), Placeholder.unparsed("limit", String.valueOf(max)), Placeholder.component("relation", Component.text(relation, targetRelation.color())));
                    return true;
                }
            }
        }
        return false;
    }
}
