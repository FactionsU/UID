package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.StringParser;

public class CmdSetTitle implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().set().title();
            Command.Builder<Sender> build = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.TITLE).and(Cloudy.isAtLeastRole(Role.MODERATOR))));

            manager.command(
                    build.required("player", FPlayerParser.of(FPlayerParser.Include.SAME_FACTION))
                            .optional("title", StringParser.greedyStringParser())
                            .handler(this::handle)
            );

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx ->
                    help.queryCommands("f " + FactionsPlugin.instance().tl().commands().set().getFirstAlias() + " " + tl.getFirstAlias() + " <player> [title]", ctx.sender())));
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        FPlayer target = context.get("player");
        Faction faction = sender.faction();

        var tl = FactionsPlugin.instance().tl().commands().set().title();

        if (sender.faction() != target.faction() || (sender.role() != Role.ADMIN && sender.role().value <= target.role().value)) {
            sender.sendRichMessage(tl.getCannotChange());
            return;
        }

        String title = context.getOrDefault("title", "").trim();

        Component titleComponent;
        if (context.sender().hasPermission(Permission.TITLE_COLOR)) {
            titleComponent = Mini.parseLimited(title);
        } else {
            titleComponent = Component.text(title);
        }

        int limit = FactionsPlugin.instance().conf().factions().other().getTitleLengthMax();
        if (PlainTextComponentSerializer.plainText().serialize(titleComponent).length() > limit) {
            sender.sendRichMessage(tl.getLimit(), Placeholder.unparsed("limit", String.valueOf(limit)));
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        var econ = FactionsPlugin.instance().conf().economy();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        if (!context.sender().payForCommand(econ.getCostTitle(), econTl.getTitleTo(), econTl.getTitleFor())) {
            return;
        }

        target.title(titleComponent);

        // Inform
        faction.sendRichMessage(tl.getChanged(), FPlayerResolver.of("sender", (FPlayer) null, sender), FPlayerResolver.of("target", (FPlayer) null, target));
    }

}
