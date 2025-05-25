package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.*;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.set.CmdSetPerm;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.SpiralTask;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.function.BiConsumer;

public class CmdZone implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            var tl = FactionsPlugin.instance().tl().commands().zone();
            Command.Builder<Sender> zoneBuilder = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(TL.COMMAND_ZONE_DESCRIPTION))
                    .permission(builder.commandPermission()
                            .and(Cloudy.predicate(Sender::hasFaction))
                            .and(Cloudy.predicate(s -> s.fPlayerOrNull() instanceof FPlayer fp && fp.faction().upgradeLevel(Upgrades.ZONES) > 0))
                            .and(Cloudy.hasSelfFactionPerms(PermissibleActions.ZONE))
                    );

            manager.command(
                    zoneBuilder.literal(tl.create().getFirstAlias(), tl.create().getSecondaryAliases())
                            .required("zone", StringParser.stringParser())
                            .handler(this::create)
            );

            manager.command(
                    zoneBuilder.literal(tl.set().getFirstAlias(), tl.set().getSecondaryAliases())
                            .required("zone", StringParser.stringParser(), SuggestionProvider.blockingStrings(zoneSuggester))
                            .literal(tl.set().name().getFirstAlias(), tl.set().name().getSecondaryAliases())
                            .required("newName", StringParser.stringParser())
                            .handler(this::setName)
            );

            manager.command(
                    zoneBuilder.literal(tl.set().getFirstAlias(), tl.set().getSecondaryAliases())
                            .required("zone", StringParser.stringParser(), SuggestionProvider.blockingStrings(zoneSuggester))
                            .literal(tl.set().greeting().getFirstAlias(), tl.set().greeting().getSecondaryAliases())
                            .required("greeting", StringParser.greedyStringParser())
                            .handler(this::setGreeting)
            );

            manager.command(
                    zoneBuilder.literal(tl.delete().getFirstAlias(), tl.delete().getSecondaryAliases())
                            .required("zone", StringParser.stringParser(), SuggestionProvider.blockingStrings(zoneSuggester))
                            .handler(this::delete)
            );

            manager.command(
                    zoneBuilder.literal(tl.claim().getFirstAlias(), tl.claim().getSecondaryAliases())
                            .required("zone", StringParser.stringParser(), SuggestionProvider.blockingStrings(zoneSuggester))
                            .flag(manager.flagBuilder("radius").withComponent(IntegerParser.integerParser(1)))
                            .flag(manager.flagBuilder("auto"))
                            .handler(this::claim)
            );

            new CmdSetPerm((ctx) -> '/' + MiscUtil.commandRoot() + ' ' + tl.getFirstAlias() +
                    ' ' + tl.set().getFirstAlias() + ' ' + ctx.get("zone") + ' ' + FactionsPlugin.instance().tl().commands().permissions().getFirstAlias() + ' ', context -> {
                FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
                Faction faction = sender.faction();
                String name = context.get("zone");
                Faction.Zone zone = faction.zones().get(name);
                if (zone == null) {
                    sender.sendMessage(Mini.parse(tl.perms().getZoneNotFound(), Placeholder.unparsed("name", name)));
                    return null;
                }
                return zone.id() == 0 ? faction.permissions() : zone.permissions();
            }, context -> {
                FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
                Faction faction = sender.faction();
                String name = context.get("zone");
                Faction.Zone zone = faction.zones().get(name);
                if (zone == null) {
                    sender.sendMessage(Mini.parse(tl.perms().getZoneNotFound(), Placeholder.unparsed("name", name)));
                    return false;
                }
                zone.permissions().clear();
                return true;
            }).consumer().accept(manager, zoneBuilder.literal(tl.set().getFirstAlias(), tl.set().getSecondaryAliases()).required("zone", StringParser.stringParser(), SuggestionProvider.blockingStrings(zoneSuggester)));
        };
    }

    static BlockingSuggestionProvider.@NonNull Strings<Sender> zoneSuggester = (c, i) ->
            c.sender().fPlayerOrNull().faction().zones().get().stream()
                    .map(Faction.Zone::name)
                    .filter(s -> s.toLowerCase().startsWith(i.peekString().toLowerCase()))
                    .toList();

    private void create(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        String name = context.get("zone");

        var tl = FactionsPlugin.instance().tl().commands().zone().create();

        if (faction.zones().get(name) != null) {
            sender.sendMessage(Mini.parse(tl.getNameAlreadyInUse(), Placeholder.unparsed("name", name)));
            return;
        }

        faction.zones().create(name);
        sender.sendMessage(Mini.parse(tl.getSuccess(), Placeholder.unparsed("name", name)));
    }

    private void setName(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        String name = context.get("zone");

        var tl = FactionsPlugin.instance().tl().commands().zone().set().name();

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
        Faction faction = sender.faction();

        String name = context.get("zone");

        var tl = FactionsPlugin.instance().tl().commands().zone().set().greeting();

        Faction.Zone zone = faction.zones().get(name);

        if (zone == null) {
            sender.sendMessage(Mini.parse(tl.getZoneNotFound(), Placeholder.unparsed("name", name)));
            return;
        }

        String greeting = context.get("greeting");

        zone.greeting(greeting);

        sender.sendMessage(Mini.parse(tl.getSuccess(),
                Placeholder.unparsed("name", name),
                Placeholder.component("greeting", Mini.parse(greeting, Placeholder.unparsed("tag", faction.tag())))
        ));
    }

    private void claim(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();
        Faction faction = sender.faction();

        String name = context.get("zone");

        var tl = FactionsPlugin.instance().tl().commands().zone().claim();

        Faction.Zone zone = faction.zones().get(name);

        if (zone == null) {
            sender.sendMessage(Mini.parse(tl.getZoneNotFound(), Placeholder.unparsed("name", name)));
            return;
        }

        if (context.flags().hasFlag("auto")) {
            if (sender.autoSetZone() == null) {
                sender.autoSetZone(zone.name());
                sender.sendMessage(Mini.parse(tl.getAutoSetOn(), Placeholder.unparsed("zone", zone.name())));
            } else {
                sender.autoSetZone(null);
                sender.sendMessage(Mini.parse(tl.getAutoSetOff()));
            }

            return;
        }

        FLocation standing = new FLocation(player);

        if (context.flags().get("radius") instanceof Integer radius) {
            var claims = Board.board().allClaims(faction);
            sender.sendMessage(Mini.parse(tl.getAttemptingRadius(), Placeholder.unparsed("zone", zone.name())));

            new SpiralTask(standing, radius) {
                @Override
                public boolean work() {
                    FLocation loc = this.currentFLocation();
                    if (claims.contains(loc)) {
                        try {
                            claim(sender, faction, loc, zone, tl, false);
                        } catch (Exception ignored) {
                        }
                    }
                    return true;
                }
            };
            return;
        }

        claim(sender, faction, standing, zone, tl, true);
    }

    public static boolean claim(FPlayer sender, Faction faction, FLocation location, Faction.Zone zone, boolean msg) {
        return claim(sender, faction, location, zone, FactionsPlugin.instance().tl().commands().zone().claim(), msg);
    }

    private static boolean claim(FPlayer sender, Faction faction, FLocation location, Faction.Zone zone, TranslationsConfig.Commands.Zone.Claim tl, boolean msg) {
        if (location.faction() != faction) {
            if (msg) {
                sender.sendMessage(Mini.parse(tl.getNotInTerritory()));
            }
            return false;
        }

        Faction.Zone currentZone = faction.zones().get(location);

        if (!currentZone.canPlayerManage(sender)) {
            if (msg) {
                sender.sendMessage(Mini.parse(tl.getCannotManage(), Placeholder.unparsed("zone", currentZone.name())));
            }
            return false;
        }

        if (currentZone == zone) {
            if (msg) {
                sender.sendMessage(Mini.parse(tl.getAlreadyZone(), Placeholder.unparsed("zone", zone.name())));
            }
            return false;
        }

        if ((currentZone != faction.zones().main()) && !zone.canPlayerManage(sender)) {
            if (msg) {
                sender.sendMessage(Mini.parse(tl.getCannotManage(), Placeholder.unparsed("zone", zone.name())));
            }
            return false;
        }

        faction.zones().set(zone, location);
        if (msg) {
            sender.sendMessage(Mini.parse(tl.getSuccess(), Placeholder.unparsed("oldzone", currentZone.name()), Placeholder.unparsed("newzone", zone.name())));
        }
        return true;
    }

    private void delete(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        String name = context.get("zone");

        var tl = FactionsPlugin.instance().tl().commands().zone().delete();

        Faction.Zone zone = faction.zones().get(name);

        if (zone == null) {
            sender.sendMessage(Mini.parse(tl.getZoneNotFound(), Placeholder.unparsed("name", name)));
            return;
        }

        String conf = CmdConfirm.add(sender, s -> this.delete(sender, name));

        sender.sendMessage(Mini.parse(tl.getConfirm(),
                Placeholder.unparsed("name", name),
                Placeholder.unparsed("command", conf)
        ));
    }

    private void delete(FPlayer sender, String name) {
        Faction faction = sender.faction();

        if (!faction.hasAccess(sender, PermissibleActions.ZONE, null)) {
            return; // Lost perms while confirming, just silently die, meh
        }

        var tl = FactionsPlugin.instance().tl().commands().zone().set().greeting();

        Faction.Zone zone = faction.zones().get(name);

        if (zone == null) {
            sender.sendMessage(Mini.parse(tl.getZoneNotFound(), Placeholder.unparsed("name", name)));
            return;
        }

        faction.zones().delete(name);

        sender.sendMessage(Mini.parse(tl.getSuccess(), Placeholder.unparsed("name", name)));
    }
}
