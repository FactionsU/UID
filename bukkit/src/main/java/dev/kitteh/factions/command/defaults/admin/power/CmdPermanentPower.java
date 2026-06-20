package dev.kitteh.factions.command.defaults.admin.power;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.DoubleParser;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdPermanentPower implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().admin().power().permanentPower();
            manager.command(
                    builder.literal("permanent")
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SET_PERMANENTPOWER)))
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .optional("value", DoubleParser.doubleParser())
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().admin().power().permanentPower();
        Faction targetFaction = context.get("faction");

        Integer targetPower = context.getOrDefault("value", null);

        targetFaction.permanentPower(targetPower);

        String change = targetFaction.hasPermanentPower() ? tl.getGrant() : tl.getRevoke();

        FPlayer fPlayer = context.sender().fPlayerOrNull();

        context.sender().sendRichMessage(tl.getSuccess(), Placeholder.unparsed("change", change), FactionResolver.of(targetFaction));

        TagResolver blame = Placeholder.unparsed("player", fPlayer == null ? "A server admin" : fPlayer.name());
        for (FPlayer fplayer : targetFaction.membersOnline(true)) {
            if (fplayer == fPlayer) {
                continue;
            }
            fplayer.sendRichMessage(tl.getFactionMsg(), blame, Placeholder.unparsed("change", change));
        }
    }
}
