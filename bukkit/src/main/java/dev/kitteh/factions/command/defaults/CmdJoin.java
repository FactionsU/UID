package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerJoinEvent;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdJoin implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().join();
            Command.Builder<Sender> build = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.JOIN).and(Cloudy.isPlayer())));

            manager.command(
                    build.required("faction", FactionParser.of())
                            .handler(this::handle)
            );

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands("f join <faction>", ctx.sender())));
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().join();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction faction = context.get("faction");

        if (!faction.isNormal()) {
            sender.sendRichMessage(tl.getDeniedSpecial());
            return;
        }

        if (faction == sender.faction()) {
            sender.sendRichMessage(tl.getDeniedAlreadyMember(), FactionResolver.of(sender, faction));
            return;
        }

        if (sender.hasFaction()) {
            sender.sendRichMessage(tl.getDeniedAlreadyHaveFaction());
            return;
        }

        int max = faction.memberLimit();
        if (faction.size() >= max) {
            sender.sendRichMessage(tl.getDeniedMaxMembers(), Placeholder.unparsed("limit", String.valueOf(max)), FactionResolver.of(sender, faction));
            return;
        }

        if (!FactionsPlugin.instance().landRaidControl().canJoinFaction(faction, sender)) {
            return;
        }

        if (!(faction.open() || faction.hasInvite(sender))) {
            sender.sendRichMessage(tl.getDeniedRequiresInvite(), FactionResolver.of(sender, faction));
            if (!faction.isBanned(sender)) {
                faction.membersOnline(true).forEach(fp -> fp.sendRichMessage(tl.getDeniedRequiresInviteNotice(), FPlayerResolver.of("player", fp, sender)));
            }
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
        if (!context.sender().canAffordCommand(FactionsPlugin.instance().conf().economy().getCostJoin(), FactionsPlugin.instance().tl().economy().actions().getJoinTo())) {
            return;
        }

        // Check for ban
        if (faction.isBanned(sender)) {
            sender.sendRichMessage(tl.getDeniedBanned(), FactionResolver.of(sender, faction));
            return;
        }

        // trigger the join event (cancellable)
        FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(sender, faction, FPlayerJoinEvent.Reason.COMMAND);
        Bukkit.getServer().getPluginManager().callEvent(joinEvent);
        if (joinEvent.isCancelled()) {
            return;
        }

        // then make 'em pay (if applicable)
        var econ = FactionsPlugin.instance().conf().economy();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        if (!context.sender().payForCommand(econ.getCostJoin(), econTl.getJoinTo(), econTl.getJoinFor())) {
            return;
        }

        sender.sendRichMessage(tl.getSuccess(), FactionResolver.of(sender, faction));

        faction.membersOnline(true).forEach(fp -> fp.sendRichMessage(tl.getSuccessNotice(), FactionResolver.of(fp, faction), FPlayerResolver.of("player", fp, sender)));

        sender.resetFactionData();
        sender.faction(faction);
        faction.deInvite(sender);
        sender.role(faction.defaultRole());
        ((Sender.Player) context.sender()).player().updateCommands();

        if (FactionsPlugin.instance().conf().logging().isFactionJoin()) {
            AbstractFactionsPlugin.instance().log(sender.name() + " joined the faction " + faction.tag());
        }
    }
}
