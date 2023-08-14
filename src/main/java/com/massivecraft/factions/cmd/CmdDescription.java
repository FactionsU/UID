package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.TextUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public class CmdDescription extends FCommand {

    public CmdDescription() {
        super();
        this.aliases.add("desc");
        this.aliases.add("description");

        this.requiredArgs.add("desc");

        this.requirements = new CommandRequirements.Builder(Permission.DESCRIPTION)
                .memberOnly()
                .withRole(Role.MODERATOR)
                .noErrorOnManyArgs()
                .brigadier(GreedyMessageBrigadier.class)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostDesc(), TL.COMMAND_DESCRIPTION_TOCHANGE, TL.COMMAND_DESCRIPTION_FORCHANGE)) {
            return;
        }

        // since "&" color tags seem to work even through plain old FPlayer.sendMessage() for some reason, we need to break those up
        // And replace all the % because it messes with string formatting and this is easy way around that.
        String desc = TextUtil.implode(context.args, " ").replaceAll("%", "").replaceAll("(&([a-f0-9klmnor]))", "& $2");
        int limit = this.plugin.conf().commands().description().getMaxLength();
        if (limit > 0 && desc.length() > limit) {
            context.msg(TL.COMMAND_DESCRIPTION_TOOLONG, String.valueOf(limit));
            return;
        }
        context.faction.setDescription(desc);

        if (!FactionsPlugin.getInstance().conf().factions().chat().isBroadcastDescriptionChanges()) {
            context.fPlayer.msg(TL.COMMAND_DESCRIPTION_CHANGED, context.faction.describeTo(context.fPlayer));
            context.fPlayer.sendMessage(context.faction.getDescription());
            return;
        }

        // Broadcast the description to everyone
        for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
            fplayer.msg(TL.COMMAND_DESCRIPTION_CHANGES, context.faction.describeTo(fplayer));
            fplayer.sendMessage(context.faction.getDescription());  // players can inject "&" or "`" or "<i>" or whatever in their description; &k is particularly interesting looking
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_DESCRIPTION_DESCRIPTION;
    }

    protected static class GreedyMessageBrigadier implements BrigadierProvider {
        @Override
        public ArgumentBuilder<Object, ?> get(ArgumentBuilder<Object, ?> parent) {
            return parent.then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString()));
        }
    }
}
