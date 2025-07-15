package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FactionRenameEvent;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.scoreboard.FTeamWrapper;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.List;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdSetTag implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            Command.Builder<Sender> build = builder.literal("tag")
                    .commandDescription(Cloudy.desc(TL.COMMAND_TAG_DESCRIPTION))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.TAG).and(Cloudy.isAtLeastRole(Role.ADMIN))))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.DESCRIPTION).and(Cloudy.isAtLeastRole(Role.MODERATOR))));

            manager.command(
                    build.required("new tag", StringParser.stringParser())
                            .handler(this::handle)
            );

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands("f set tag <new tag>", ctx.sender())));
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();
        String tag = context.get("new tag");

        if (Factions.factions().get(tag) != null) {
            sender.msgLegacy(TL.COMMAND_TAG_TAKEN);
            return;
        }

        List<String> errors = MiscUtil.validateTag(tag);
        if (!errors.isEmpty()) {
            sender.sendMessageLegacy(errors);
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
        if (!context.sender().canAffordCommand(FactionsPlugin.instance().conf().economy().getCostTag(), TL.COMMAND_TAG_TOCHANGE)) {
            return;
        }

        // trigger the faction rename event (cancellable)
        FactionRenameEvent renameEvent = new FactionRenameEvent(sender, tag);
        Bukkit.getServer().getPluginManager().callEvent(renameEvent);
        if (renameEvent.isCancelled()) {
            return;
        }

        // then make 'em pay (if applicable)
        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostTag(), TL.COMMAND_TAG_TOCHANGE, TL.COMMAND_TAG_FORCHANGE)) {
            return;
        }

        String oldTag = faction.tag();
        faction.tag(tag);

        // Inform
        for (FPlayer fplayer : FPlayers.fPlayers().online()) {
            if (fplayer.faction() == faction) {
                fplayer.msgLegacy(TL.COMMAND_TAG_FACTION, sender.describeToLegacy(faction, true), faction.tagLegacy(faction));
                continue;
            }

            // Broadcast the tag change (if applicable)
            if (FactionsPlugin.instance().conf().factions().chat().isBroadcastTagChanges()) {
                Faction fac = fplayer.faction();
                fplayer.msgLegacy(TL.COMMAND_TAG_CHANGED, sender.colorLegacyStringTo(fac) + oldTag, faction.tagLegacy(fac));
            }
        }

        FTeamWrapper.updatePrefixes(faction);
    }
}
