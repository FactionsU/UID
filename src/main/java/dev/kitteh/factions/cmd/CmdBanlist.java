package dev.kitteh.factions.cmd;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.util.BanInfo;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;

import java.util.ArrayList;
import java.util.List;

public class CmdBanlist extends FCommand {

    public CmdBanlist() {
        super();
        this.aliases.add("banlist");
        this.aliases.add("bans");
        this.aliases.add("banl");

        this.optionalArgs.put("faction", "faction");

        this.requirements = new CommandRequirements.Builder(Permission.BAN)
                .playerOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        Faction target = context.faction;
        if (!context.args.isEmpty()) {
            target = context.argAsFaction(0);
        }

        if (target == null) {
            context.msg(TL.COMMAND_BANLIST_INVALID.format(context.argAsString(0)));
            return;
        }

        if (!target.isNormal()) {
            context.msg(TL.COMMAND_BANLIST_NOFACTION);
            return;
        }

        List<String> lines = new ArrayList<>();
        lines.add(TL.COMMAND_BANLIST_HEADER.format(target.getBannedPlayers().size(), target.getTag(context.faction)));
        int i = 1;

        for (BanInfo info : target.getBannedPlayers()) {
            FPlayer banned = FPlayers.getInstance().getById(info.banned());
            FPlayer banner = FPlayers.getInstance().getById(info.banner());
            String timestamp = TL.sdf.format(info.time());

            lines.add(TL.COMMAND_BANLIST_ENTRY.format(i, banned.getName(), banner.getName(), timestamp));
            i++;
        }

        for (String s : lines) {
            context.fPlayer.sendMessage(s);
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_BANLIST_DESCRIPTION;
    }
}
