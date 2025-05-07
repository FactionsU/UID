package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.function.BiConsumer;

public class CmdZone implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            var tl = FactionsPlugin.getInstance().tl().commands().zone();
            Command.Builder<Sender> zoneBuilder = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(TL.COMMAND_ZONE_DESCRIPTION))
                    .permission(builder.commandPermission()
                            .and(Cloudy.predicate(Sender::hasFaction))
                            .and(Cloudy.predicate(s -> s.fPlayerOrNull() instanceof FPlayer fp && fp.getFaction().getUpgradeLevel(Upgrades.ZONES) > 0))
                            .and(Cloudy.hasSelfFactionPerms(PermissibleActions.ZONE))
                    );

            manager.command(
                    zoneBuilder.literal(tl.create().getFirstAlias(), tl.create().getSecondaryAliases())
                            .required("name", StringParser.stringParser())
                            .handler(this::create)
            );

            manager.command(
                    zoneBuilder.literal(tl.set().getFirstAlias(), tl.set().getSecondaryAliases())
                            .required("name", StringParser.stringParser(), SuggestionProvider.blockingStrings(zoneSuggester))
                            .literal(tl.set().name().getFirstAlias(), tl.set().name().getSecondaryAliases())
                            .required("newName", StringParser.stringParser())
                            .handler(this::setName)
            );

            manager.command(
                    zoneBuilder.literal(tl.set().getFirstAlias(), tl.set().getSecondaryAliases())
                            .required("name", StringParser.stringParser(), SuggestionProvider.blockingStrings(zoneSuggester))
                            .literal(tl.set().greeting().getFirstAlias(), tl.set().greeting().getSecondaryAliases())
                            .required("greeting", StringParser.greedyStringParser())
                            .handler(this::setGreeting)
            );

            manager.command(
                    zoneBuilder.literal(tl.delete().getFirstAlias(), tl.delete().getSecondaryAliases())
                            .required("name", StringParser.stringParser(), SuggestionProvider.blockingStrings(zoneSuggester))
                            .handler(this::delete)
            );
        };
    }

    static BlockingSuggestionProvider.@NonNull Strings<Sender> zoneSuggester = (c, i) ->
            c.sender().fPlayerOrNull().getFaction().zones().get().stream()
                    .map(Faction.Zone::name)
                    .filter(s -> s.toLowerCase().startsWith(i.peekString().toLowerCase()))
                    .toList();

    private void create(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.getFaction();

        String name = context.get("name");

        var tl = FactionsPlugin.getInstance().tl().commands().zone().create();

        if (faction.zones().get(name) != null) {
            sender.sendMessage(Mini.parse(tl.getNameAlreadyInUse(), Placeholder.unparsed("name", name)));
            return;
        }

        faction.zones().create(name);
        sender.sendMessage(Mini.parse(tl.getSuccess(), Placeholder.unparsed("name", name)));
    }

    private void setName(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.getFaction();

        String name = context.get("name");

        var tl = FactionsPlugin.getInstance().tl().commands().zone().set().name();

        Faction.Zone zone = faction.zones().get(name);

        if (zone == null) {
            sender.sendMessage(Mini.parse(tl.getZoneNotFound(), Placeholder.unparsed("name", name)));
            return;
        }

        String newName = context.get("newName");

        if (faction.zones().get(newName) != null) {
            sender.sendMessage(Mini.parse(tl.getNameAlreadyInUse(), Placeholder.unparsed("name", name)));
            return;
        }

        zone.name(newName);

        sender.sendMessage(Mini.parse(tl.getSuccess(),
                Placeholder.unparsed("oldname", name),
                Placeholder.unparsed("newname", newName)
        ));
    }

    private void setGreeting(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.getFaction();

        String name = context.get("name");

        var tl = FactionsPlugin.getInstance().tl().commands().zone().set().greeting();

        Faction.Zone zone = faction.zones().get(name);

        if (zone == null) {
            sender.sendMessage(Mini.parse(tl.getZoneNotFound(), Placeholder.unparsed("name", name)));
            return;
        }

        String greeting = context.get("greeting");

        zone.greeting(greeting);

        sender.sendMessage(Mini.parse(tl.getSuccess(),
                Placeholder.unparsed("name", name),
                Placeholder.component("greeting", Mini.parse(greeting, Placeholder.unparsed("tag", faction.getTag())))
        ));
    }

    private void delete(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.getFaction();

        String name = context.get("name");

        var tl = FactionsPlugin.getInstance().tl().commands().zone().delete();

        Faction.Zone zone = faction.zones().get(name);

        if (zone == null) {
            sender.sendMessage(Mini.parse(tl.getZoneNotFound(), Placeholder.unparsed("name", name)));
            return;
        }

        String conf = CmdConfirm.add(sender, s -> this.delete(sender, name));

        sender.sendMessage(Mini.parse(tl.getConfirm(),
                Placeholder.unparsed("name", name),
                Placeholder.unparsed("command", "/f confirm " + conf) // TODO
        ));
    }

    private void delete(FPlayer sender, String name) {
        Faction faction = sender.getFaction();

        if (!faction.hasAccess(sender, PermissibleActions.ZONE, null)) {
            return; // Lost perms while confirming, just silently die, meh
        }

        var tl = FactionsPlugin.getInstance().tl().commands().zone().set().greeting();

        Faction.Zone zone = faction.zones().get(name);

        if (zone == null) {
            sender.sendMessage(Mini.parse(tl.getZoneNotFound(), Placeholder.unparsed("name", name)));
            return;
        }

        faction.zones().delete(name);

        sender.sendMessage(Mini.parse(tl.getSuccess(), Placeholder.unparsed("name", name)));
    }

    /*
    set perms <zone>
     */
}
