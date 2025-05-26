package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.function.BiConsumer;

public class CmdSetTitle implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("title")
                        .commandDescription(Cloudy.desc(TL.COMMAND_TITLE_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.TITLE).and(Cloudy.isAtLeastRole(Role.MODERATOR))))
                        .required("player", FPlayerParser.of(FPlayerParser.Include.SAME_FACTION))
                        .optional("title", StringParser.greedyStringParser())
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        FPlayer target = context.get("player");
        Faction faction = sender.faction();

        if (sender.faction() != target.faction() || sender.role().value <= target.role().value) {
            sender.msgLegacy(TL.COMMAND_TITLE_CANNOTPLAYER);
            return;
        }

        String title = context.getOrDefault("title", "").trim();

        title = title.replaceAll(",", "");

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostTitle(), TL.COMMAND_TITLE_TOCHANGE, TL.COMMAND_TITLE_FORCHANGE)) {
            return;
        }

        if (context.sender().hasPermission(Permission.TITLE_COLOR)) {
            title = LegacyComponentSerializer.legacySection().serialize(MiniMessage.builder()
                    .tags(TagResolver.resolver(StandardTags.color(), StandardTags.decorations(), StandardTags.rainbow(), StandardTags.pride()))
                    .build()
                    .deserialize(title));
        }
        target.title(title);

        // Inform
        faction.msgLegacy(TL.COMMAND_TITLE_CHANGED, sender.describeToLegacy(faction), target.describeToLegacy(faction));
    }

}
