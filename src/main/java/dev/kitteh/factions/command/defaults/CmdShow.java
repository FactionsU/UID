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
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tag.FactionTag;
import dev.kitteh.factions.tag.FancyTag;
import dev.kitteh.factions.tag.Tag;
import dev.kitteh.factions.util.ComponentDispatcher;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class CmdShow implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("show")
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
        defaults.add("{header}");
        defaults.add("<a>Description: <i>{description}");
        defaults.add("<a>Joining: <i>{joining}    {peaceful}");
        defaults.add("<a>Land / Power / Maxpower: <i> {chunks} / {power} / {maxPower}");
        defaults.add("<a>Raidable: {raidable}");
        defaults.add("<a>Founded: <i>{create-date}");
        defaults.add("<a>This faction is permanent, remaining even with no members.");
        defaults.add("<a>Land value: <i>{land-value} {land-refund}");
        defaults.add("<a>Balance: <i>{faction-balance}");
        defaults.add("<a>Bans: <i>{faction-bancount}");
        defaults.add("<a>Allies(<i>{allies}<a>/<i>{max-allies}<a>): {allies-list}");
        defaults.add("<a>Online: (<i>{online}<a>/<i>{members}<a>): {online-list}");
        defaults.add("<a>Offline: (<i>{offline}<a>/<i>{members}<a>): {offline-list}");
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer fPlayer = context.sender().fPlayerOrNull();

        Faction faction = context.getOrDefault("faction", fPlayer == null ? Factions.factions().wilderness() : fPlayer.faction());
        if (faction.isWilderness()) {
            context.sender().msg(TL.COMMAND_SHOW_NOFACTION_OTHER);
            return;
        }

        if (!context.sender().hasPermission(Permission.SHOW_BYPASS_EXEMPT)
                && FactionsPlugin.getInstance().conf().commands().show().getExempt().contains(faction.tag())) {
            context.sender().msg(TL.COMMAND_SHOW_EXEMPT);
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.getInstance().conf().economy().getCostShow(), TL.COMMAND_SHOW_TOSHOW, TL.COMMAND_SHOW_FORSHOW)) {
            return;
        }

        List<String> show = FactionsPlugin.getInstance().conf().commands().show().getFormat();
        if (show == null || show.isEmpty()) {
            show = defaults;
        }

        if (!faction.isNormal()) {
            String tag = faction.tagString(fPlayer);
            // send header and that's all
            String header = show.getFirst();
            if (FactionTag.HEADER.foundInString(header)) {
                context.sender().sender().sendMessage(AbstractFactionsPlugin.getInstance().txt().titleize(tag));
            } else {
                String message = header.replace(FactionTag.FACTION.getTag(), tag);
                message = Tag.parsePlain(faction, fPlayer, message);
                context.sender().sender().sendMessage(AbstractFactionsPlugin.getInstance().txt().parse(message));
            }
            return; // we only show header for non-normal factions
        }

        List<String> messageList = new ArrayList<>();
        for (String raw : show) {
            String parsed = Tag.parsePlain(faction, fPlayer, raw); // use relations
            if (parsed == null) {
                continue; // Due to minimal f show.
            }

            if (fPlayer != null) {
                parsed = Tag.parsePlaceholders(fPlayer.asPlayer(), parsed);
            }

            if (!parsed.contains("{notFrozen}") && !parsed.contains("{notPermanent}")) {
                if (parsed.contains("{ig}")) {
                    // replaces all variables with no home TL
                    parsed = parsed.substring(0, parsed.indexOf("{ig}")) + TL.COMMAND_SHOW_NOHOME;
                }
                parsed = parsed.replace("%", ""); // Just in case it got in there before we disallowed it.
                messageList.add(parsed);
            }
        }
        if (fPlayer != null && this.groupPresent()) {
            new GroupGetter(messageList, fPlayer, faction).runTaskAsynchronously(AbstractFactionsPlugin.getInstance());
        } else {
            this.sendMessages(messageList, context.sender().sender(), faction, fPlayer);
        }
    }

    private void sendMessages(List<String> messageList, CommandSender recipient, Faction faction, FPlayer player) {
        this.sendMessages(messageList, recipient, faction, player, null);
    }

    private void sendMessages(List<String> messageList, CommandSender recipient, Faction faction, FPlayer player, Map<UUID, String> groupMap) {
        FancyTag tag;
        for (String parsed : messageList) {
            if ((tag = FancyTag.getMatch(parsed)) != null) {
                if (player != null) {
                    List<Component> fancy = FancyTag.parse(parsed, faction, player, groupMap);
                    if (fancy != null) {
                        for (Component component : fancy) {
                            ComponentDispatcher.send(recipient, component);
                        }
                    }
                } else {
                    StringBuilder builder = new StringBuilder();
                    builder.append(parsed.replace(tag.getTag(), ""));
                    switch (tag) {
                        case ONLINE_LIST:
                            this.onOffLineMessage(builder, recipient, faction, true);
                            break;
                        case OFFLINE_LIST:
                            this.onOffLineMessage(builder, recipient, faction, false);
                            break;
                        case ALLIES_LIST:
                            this.relationMessage(builder, recipient, faction, Relation.ALLY);
                            break;
                        case ENEMIES_LIST:
                            this.relationMessage(builder, recipient, faction, Relation.ENEMY);
                            break;
                        case TRUCES_LIST:
                            this.relationMessage(builder, recipient, faction, Relation.TRUCE);
                            break;
                        default:
                            // NO
                    }
                }
            } else {
                ComponentDispatcher.send(recipient, LegacyComponentSerializer.legacySection().deserialize(AbstractFactionsPlugin.getInstance().txt().parse(parsed)));
            }
        }
    }

    private void onOffLineMessage(StringBuilder builder, CommandSender recipient, Faction faction, boolean online) {
        boolean first = true;
        for (FPlayer p : MiscUtil.rankOrder(faction.membersOnline(online))) {
            String name = p.nameWithTitle();
            builder.append(first ? name : ", " + name);
            first = false;
        }
        recipient.sendMessage(AbstractFactionsPlugin.getInstance().txt().parse(builder.toString()));
    }

    private void relationMessage(StringBuilder builder, CommandSender recipient, Faction faction, Relation relation) {
        boolean first = true;
        for (Faction otherFaction : Factions.factions().all()) {
            if (otherFaction != faction && otherFaction.relationTo(faction) == relation) {
                String s = otherFaction.tag();
                builder.append(first ? s : ", " + s);
                first = false;
            }
        }
        recipient.sendMessage(AbstractFactionsPlugin.getInstance().txt().parse(builder.toString()));
    }

    private boolean groupPresent() {
        for (String line : FactionsPlugin.getInstance().conf().commands().toolTips().getPlayer()) {
            if (line.contains("{group}")) {
                return true;
            }
        }
        return false;
    }

    private class GroupGetter extends BukkitRunnable {
        private final List<String> messageList;
        private final FPlayer sender;
        private final Faction faction;
        private final Set<OfflinePlayer> players;

        private GroupGetter(List<String> messageList, FPlayer sender, Faction faction) {
            this.messageList = messageList;
            this.sender = sender;
            this.faction = faction;
            this.players = faction.members().stream().map(fp -> AbstractFactionsPlugin.getInstance().getOfflinePlayer(fp.name(), fp.uniqueId())).collect(Collectors.toSet());
        }

        @Override
        public void run() {
            Map<UUID, String> map = new HashMap<>();
            for (OfflinePlayer player : this.players) {
                map.put(player.getUniqueId(), AbstractFactionsPlugin.getInstance().getPrimaryGroup(player));
            }
            new SenderRunner(this.messageList, this.sender, this.faction, map).runTask(AbstractFactionsPlugin.getInstance());
        }
    }

    private class SenderRunner extends BukkitRunnable {
        private final List<String> messageList;
        private final FPlayer sender;
        private final Faction faction;
        private final Map<UUID, String> map;

        private SenderRunner(List<String> messageList, FPlayer sender, Faction faction, Map<UUID, String> map) {
            this.messageList = messageList;
            this.sender = sender;
            this.faction = faction;
            this.map = map;
        }

        @Override
        public void run() {
            Player player = Bukkit.getPlayerExact(sender.name());
            if (player != null) {
                CmdShow.this.sendMessages(messageList, player, faction, sender, map);
            }
        }
    }
}