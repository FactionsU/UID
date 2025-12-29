package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.IntegerParser;

public class CmdAdminTNT implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().admin().tnt();
            Command.Builder<Sender> build = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .permission(builder.commandPermission()
                            .and(Cloudy.predicate(s -> FactionsPlugin.instance().conf().commands().tnt().isEnable()))
                            .and(Cloudy.hasPermission(Permission.TNT_MODIFY)))
                    .required("faction", FactionParser.of(FactionParser.Include.SELF));


            manager.command(
                    build.literal(tl.getSubCmdModify())
                            .required("amount", IntegerParser.integerParser())
                            .handler(ctx -> this.handle(ctx, true))
            );

            manager.command(
                    build.literal(tl.getSubCmdSet())
                            .required("amount", IntegerParser.integerParser(0))
                            .handler(ctx -> this.handle(ctx, false))
            );
        };
    }

    private void handle(CommandContext<Sender> context, boolean modify) {
        Sender sender = context.sender();

        Faction faction = context.get("faction");
        int amount = context.get("amount");

        int oldVal = faction.tntBank();
        int newVal = modify ? oldVal + amount : amount;
        newVal = Math.min(faction.tntBankMax(), newVal);
        newVal = Math.max(0, newVal);

        faction.tntBank(newVal);
        sender.sendRichMessage(FactionsPlugin.instance().tl().commands().admin().tnt().getSuccess(),
                FactionResolver.of(sender.fPlayerOrNull(), faction),
                Placeholder.parsed("oldamount", String.valueOf(oldVal))
                );
    }
}
