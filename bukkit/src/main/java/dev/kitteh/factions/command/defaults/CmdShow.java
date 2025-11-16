package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.ComponentDispatcher;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TriConsumer;
import dev.kitteh.factions.util.TriFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class CmdShow implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {

        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().show();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(TL.COMMAND_SHOW_COMMANDDESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SHOW)))
                            .optional("faction", FactionParser.of(FactionParser.Include.SELF, FactionParser.Include.PLAYERS))
                            .handler(this::handle)
            );
        };
    }

    final List<String> defaults = new ArrayList<>();

    public CmdShow() {
        // add defaults to /f show in case config doesn't have it
        defaults.add("<fuuid:title><faction:relation_color><faction:name>");
        defaults.add("<gold>Description: <yellow><faction:description>");
        defaults.add("<gold><faction:if_open>No invitation required</faction:if_open><faction:if_open:else>Invitation required</faction:if_open:else><faction:if_peaceful>.    <fuuid:color:peaceful>Peaceful");
        defaults.add("<gold>Land / Power / Max Power: <yellow><faction:claims_count></yellow> / <yellow><faction:power></yellow> / <yellow><faction:power_max>");
        defaults.add("<gold>Raidable: <faction:if_raidable><green>Yes</faction:if_raidable><faction:if_raidable:else><red>No");
        defaults.add("<gold>Founded: <yellow><faction:creation_date>");
        defaults.add("<faction:if_permanent><gold>This faction is permanent, remaining even with no members.");
        defaults.add("<fuuid:if_economy><fuuid:if_banks><gold>Balance: <yellow><faction:bank_balance></fuuid:if_banks>     <gold>Land value: <yellow><faction:claims_value>");
        defaults.add("<faction:if_allies><gold>Allies (<yellow><faction:allies_count></yellow>/<yellow><faction:allies_max></yellow>): {allies-list}");
        defaults.add("<faction:if_online><gold>Online: (<yellow><faction:members_online_count></yellow>/<yellow><faction:members_total_count></yellow>)<faction:if_online>: {online-list}");
        defaults.add("<faction:if_offline><gold>Offline: (<yellow><faction:members_offline_count></yellow>/<yellow><faction:members_total_count></yellow>)<faction:if_offline>: {offline-list}");
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer fPlayer = context.sender().fPlayerOrNull();

        Faction faction = context.getOrDefault("faction", fPlayer == null ? Factions.factions().wilderness() : fPlayer.faction());
        if (faction.isWilderness()) {
            context.sender().msgLegacy(TL.COMMAND_SHOW_NOFACTION_OTHER);
            return;
        }

        if (!context.sender().hasPermission(Permission.SHOW_BYPASS_EXEMPT)
                && FactionsPlugin.instance().conf().commands().show().getExempt().contains(faction.tag())) {
            context.sender().msgLegacy(TL.COMMAND_SHOW_EXEMPT);
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostShow(), TL.COMMAND_SHOW_TOSHOW, TL.COMMAND_SHOW_FORSHOW)) {
            return;
        }

        var tl = FactionsPlugin.instance().tl().commands().show();
        List<String> show = tl.getNormalFormat();
        if (show == null || show.isEmpty()) {
            show = defaults;
        }

        if (faction.isSafeZone()) {
            show = tl.getSafezoneFormat();
        }
        if (faction.isWarZone()) {
            show = tl.getWarzoneFormat();
        }

        for (String raw : show) {
            String msg = raw;

            MultiLiner tag = MultiLiner.getMatch(raw);
            if (tag != null) {
                msg = msg.replace(tag.tag(), "");
            }
            Component component = Mini.parse(msg, FactionResolver.of(fPlayer, faction));
            if (component == Component.empty()) {
                continue; // Due to minimal f show.
            }

            if (tag == null) {
                ComponentDispatcher.send(context.sender().sender(), component);
            } else {
                for (Component comp : tag.getComponents(component, faction, fPlayer)) {
                    ComponentDispatcher.send(context.sender().sender(), comp);
                }
            }
        }
    }

    public enum MultiLiner {
        ALLIES_LIST("allies-list", (prefix, faction, observer) -> processRelation(prefix, faction, observer, Relation.ALLY)),
        ENEMIES_LIST("enemies-list", (prefix, faction, observer) -> processRelation(prefix, faction, observer, Relation.ENEMY)),
        TRUCES_LIST("truces-list", (prefix, faction, observer) -> processRelation(prefix, faction, observer, Relation.TRUCE)),
        ONLINE_LIST("online-list", (prefix, faction, observer) -> processPlayers(prefix, faction, observer, true)),
        OFFLINE_LIST("offline-list", (prefix, faction, observer) -> processPlayers(prefix, faction, observer, false));

        private static final int ARBITRARY_LIMIT = 20000;

        private final String tag;
        private final TriFunction<Component, Faction, @Nullable FPlayer, List<Component>> function;

        private static List<Component> processRelation(Component prefix, Faction faction, @Nullable FPlayer fPlayer, Relation relation) {
            List<Component> fancyMessages = new ArrayList<>();
            TextComponent.Builder message = Component.text().append(prefix);
            boolean first = true;
            for (Faction otherFaction : Factions.factions().all()) {
                if (otherFaction == faction) {
                    continue;
                }
                if (otherFaction.relationTo(faction) == relation) {
                    if (!first) {
                        message.append(Component.text(", "));
                    }
                    message.append(Mini.parse("<faction:tooltip><faction>", FactionResolver.of(fPlayer, otherFaction)));
                    first = false;
                    Component current = message.build();
                    if (GsonComponentSerializer.gson().serialize(current).length() > ARBITRARY_LIMIT) {
                        fancyMessages.add(current);
                        message = Component.text();
                    }
                }
            }
            fancyMessages.add(message.build());
            return fancyMessages;
        }

        private static List<Component> processPlayers(Component prefix, Faction faction, @Nullable FPlayer fPlayer, boolean online) {
            List<Component> fancyMessages = new ArrayList<>();
            TextComponent.Builder message = Component.text().append(prefix);
            boolean first = true;
            for (FPlayer p : MiscUtil.rankOrder(faction.membersOnline(online, fPlayer))) {
                if (!first) {
                    message.append(Component.text(", "));
                }
                message.append(Mini.parse("<player:tooltip><player:relation_color><player:name_and_title>", FPlayerResolver.of("player", fPlayer, p)));
                first = false;
                Component current = message.build();
                if (GsonComponentSerializer.gson().serialize(current).length() > ARBITRARY_LIMIT) {
                    fancyMessages.add(current);
                    message = Component.text();
                }
            }
            fancyMessages.add(message.build());
            return fancyMessages;
        }

        public static CmdShow.@Nullable MultiLiner getMatch(String text) {
            for (MultiLiner tag : MultiLiner.values()) {
                if (text.contains(tag.tag)) {
                    return tag;
                }
            }
            return null;
        }

        MultiLiner(String tag, TriFunction<Component, Faction, @Nullable FPlayer, List<Component>> function) {
            this.tag = '{' + tag + '}';
            this.function = function;
        }

        public String tag() {
            return this.tag;
        }

        public List<Component> getComponents(Component prefix, Faction faction, @Nullable FPlayer observer) {
            return this.function.apply(prefix, faction, observer);
        }
    }
}