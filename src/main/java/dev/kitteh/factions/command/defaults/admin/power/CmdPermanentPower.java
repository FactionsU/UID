package dev.kitteh.factions.command.defaults.admin.power;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.DoubleParser;

import java.util.function.BiConsumer;

public class CmdPermanentPower implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("permanent")
                            .commandDescription(Cloudy.desc(TL.COMMAND_PERMANENTPOWER_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SET_PERMANENTPOWER)))
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .optional("value", DoubleParser.doubleParser())
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        Faction targetFaction = context.get("faction");

        Integer targetPower = context.getOrDefault("value", null);

        targetFaction.setPermanentPower(targetPower);

        String change = TL.COMMAND_PERMANENTPOWER_REVOKE.toString();
        if (targetFaction.hasPermanentPower()) {
            change = TL.COMMAND_PERMANENTPOWER_GRANT.toString();
        }

        FPlayer fPlayer = context.sender().fPlayerOrNull();

        // Inform sender
        context.sender().msg(TL.COMMAND_PERMANENTPOWER_SUCCESS, change, targetFaction.describeTo(fPlayer));

        // Inform all other players
        for (FPlayer fplayer : targetFaction.getFPlayersWhereOnline(true)) {
            if (fplayer == fPlayer) {
                continue;
            }
            String blame = (fPlayer == null ? TL.GENERIC_SERVERADMIN.toString() : fPlayer.describeTo(fplayer, true));
            fplayer.msg(TL.COMMAND_PERMANENTPOWER_FACTION, blame, change);
        }
    }
}
