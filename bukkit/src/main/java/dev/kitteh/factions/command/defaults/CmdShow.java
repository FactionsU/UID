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
import dev.kitteh.factions.util.TextUtil;
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
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import java.util.stream.Collectors;

public class CmdShow implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        var tl = FactionsPlugin.instance().tl().commands().show();
        return (manager, builder, help) -> manager.command(
                builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                        .commandDescription(Cloudy.desc(TL.COMMAND_SHOW_COMMANDDESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SHOW)))
                        .optional("faction", FactionParser.of(FactionParser.Include.SELF, FactionParser.Include.PLAYERS))
                        .handler(this::handle)
        );
    }

    final List<String> defaults = new ArrayList<>();

    public CmdShow() {
        // add defaults to /f show in case config doesn't have it
        defaults.add("{header}");
        defaults.add("&6Description: &e{description}");
        defaults.add("&6Joining: &e{joining}    {peaceful}");
        defaults.add("&6Land / Power / Maxpower: &e {chunks} / {power} / {maxPower}");
        defaults.add("&6Raidable: {raidable}");
        defaults.add("&6Founded: &e{create-date}");
        defaults.add("&6This faction is permanent, remaining even with no members.");
        defaults.add("&6Land value: &e{land-value} {land-refund}");
        defaults.add("&6Balance: &e{faction-balance}");
        defaults.add("&6Bans: &e{faction-bancount}");
        defaults.add("&6Allies(&e{allies}&6/&e{max-allies}&6): {allies-list}");
        defaults.add("&6Online: (&e{online}&6/&e{members}&6): {online-list}");
        defaults.add("&6Offline: (&e{offline}&6/&e{members}&6): {offline-list}");
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

        List<String> show = FactionsPlugin.instance().conf().commands().show().getFormat();
        if (show == null || show.isEmpty()) {
            show = defaults;
        }

        if (!faction.isNormal()) {
            String tag = faction.tagLegacy(fPlayer);
            // send header and that's all
            String header = show.getFirst();
            if (FactionTag.HEADER.foundInString(header)) {
                context.sender().sender().sendMessage(TextUtil.titleizeLegacy(tag));
            } else {
                String message = header.replace(FactionTag.FACTION.getTag(), tag);
                message = Tag.parsePlain(faction, fPlayer, message);
                context.sender().sender().sendMessage(TextUtil.parse(message));
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
            new GroupGetter(messageList, fPlayer, faction).runTaskAsynchronously(AbstractFactionsPlugin.instance());
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
                ComponentDispatcher.send(recipient, LegacyComponentSerializer.legacySection().deserialize(TextUtil.parse(parsed)));
            }
        }
    }

    private void onOffLineMessage(StringBuilder builder, CommandSender recipient, Faction faction, boolean online) {
        boolean first = true;
        for (FPlayer p : MiscUtil.rankOrder(faction.membersOnline(online))) {
            String name = p.nameWithTitleLegacy();
            builder.append(first ? name : ", " + name);
            first = false;
        }
        recipient.sendMessage(TextUtil.parse(builder.toString()));
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
        recipient.sendMessage(TextUtil.parse(builder.toString()));
    }

    private boolean groupPresent() {
        for (String line : FactionsPlugin.instance().conf().commands().toolTips().getPlayer()) {
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
            this.players = faction.members().stream().map(fp -> AbstractFactionsPlugin.instance().getOfflinePlayer(fp.name(), fp.uniqueId())).collect(Collectors.toSet());
        }

        @Override
        public void run() {
            Map<UUID, String> map = new HashMap<>();
            for (OfflinePlayer player : this.players) {
                map.put(player.getUniqueId(), AbstractFactionsPlugin.instance().getPrimaryGroup(player));
            }
            new SenderRunner(this.messageList, this.sender, this.faction, map).runTask(AbstractFactionsPlugin.instance());
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