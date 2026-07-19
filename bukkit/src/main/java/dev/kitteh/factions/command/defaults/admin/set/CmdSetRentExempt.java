package dev.kitteh.factions.command.defaults.admin.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdSetRentExempt implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().admin().set().rentExempt();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SET_RENT_EXEMPT)))
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().admin().set().rentExempt();
        Faction faction = context.get("faction");

        String change;
        if (faction.rentExempt()) {
            change = tl.getRevoke();
            faction.rentExempt(false);
        } else {
            change = tl.getGrant();
            faction.rentExempt(true);
        }

        FPlayer fPlayer = context.sender().fPlayerOrNull();
        String blame = fPlayer == null ? "A server admin" : fPlayer.name();

        AbstractFactionsPlugin.instance().log(blame + " " + change + " the faction \"" + faction.tag() + "\".");

        TagResolver blames = Placeholder.unparsed("player", blame);

        for (FPlayer fplayer : FPlayers.fPlayers().online()) {
            if (fplayer.faction() == faction) {
                fplayer.sendRichMessage(tl.getYours(), blames, Placeholder.parsed("change", change), FactionResolver.of(faction));
            } else {
                fplayer.sendRichMessage(tl.getOther(), blames, Placeholder.parsed("change", change), FactionResolver.of(faction));
            }
        }
    }
}
