package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.tnt.CmdTNTDeposit;
import dev.kitteh.factions.command.defaults.tnt.CmdTNTFill;
import dev.kitteh.factions.command.defaults.tnt.CmdTNTSiphon;
import dev.kitteh.factions.command.defaults.tnt.CmdTNTWithdraw;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdTNT implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().tnt();
            Command.Builder<Sender> tntBuilder = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.hasFaction().and(Cloudy.predicate(s -> FactionsPlugin.instance().conf().commands().tnt().isEnable()))));

            manager.command(tntBuilder.permission(tntBuilder.commandPermission().and(Cloudy.hasPermission(Permission.TNT_INFO))).handler(this::handle));
            manager.command(tntBuilder.literal("info").permission(tntBuilder.commandPermission().and(Cloudy.hasPermission(Permission.TNT_INFO))).handler(this::handle));

            new CmdTNTDeposit().consumer().accept(manager, tntBuilder, help);
            new CmdTNTFill().consumer().accept(manager, tntBuilder, help);
            new CmdTNTSiphon().consumer().accept(manager, tntBuilder, help);
            new CmdTNTWithdraw().consumer().accept(manager, tntBuilder, help);
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().tnt();
        context.sender().sendRichMessage(tl.getMessage(), Placeholder.unparsed("count", String.valueOf(((Sender.Player) context.sender()).faction().tntBank())));
    }
}
