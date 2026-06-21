package dev.kitteh.factions.command.defaults.admin.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FactionRenameEvent;
import dev.kitteh.factions.scoreboard.FTeamWrapper;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.MiscUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().set().tag();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.isBypass()))
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .required("tag", StringParser.stringParser())
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().set().tag();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction faction = context.get("faction");
        String tag = context.get("tag");

        if (Factions.factions().get(tag) != null) {
            sender.sendRichMessage(tl.getTaken());
            return;
        }

        List<Component> errors = MiscUtil.validateTag(tag);
        if (!errors.isEmpty()) {
            errors.forEach(sender::sendMessage);
            return;
        }

        // trigger the faction rename event (cancellable)
        FactionRenameEvent renameEvent = new FactionRenameEvent(sender, tag);
        Bukkit.getServer().getPluginManager().callEvent(renameEvent);
        if (renameEvent.isCancelled()) {
            return;
        }

        String oldTag = faction.tag();
        faction.tag(tag);

        // Inform
        for (FPlayer fplayer : FPlayers.fPlayers().online()) {
            if (fplayer.faction() == faction) {
                fplayer.sendRichMessage(tl.getFactionMsg(), FPlayerResolver.of("player", sender), FactionResolver.of(faction));
                continue;
            }

            if (FactionsPlugin.instance().conf().factions().chat().isBroadcastTagChanges()) {
                fplayer.sendRichMessage(tl.getChanged(), Placeholder.unparsed("oldtag", oldTag), FactionResolver.of(faction));
            }
        }

        FTeamWrapper.updatePrefixes(faction);
    }
}
