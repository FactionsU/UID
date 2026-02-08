package dev.kitteh.factions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.function.BiConsumer;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class ComponentDispatcher {
    private static BiConsumer<CommandSender, ComponentLike> componentSender = (commandSender, component) ->
            commandSender.spigot().sendMessage(ComponentSerializer.deserialize(GsonComponentSerializer.gson().serializeToTree(component.asComponent())));
    private static BiConsumer<Player, ComponentLike> actionBarSender = (player, component) ->
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, ComponentSerializer.deserialize(GsonComponentSerializer.gson().serializeToTree(component.asComponent())));
    private static HexConsumer<Player, ComponentLike, ComponentLike, Integer, Integer, Integer> titleSender = (player, title, subtitle, fadeIn, stay, fadeOut) ->
            player.sendTitle(Mini.toLegacy(title), Mini.toLegacy(subtitle), fadeIn, stay, fadeOut);

    public static void setSenders(BiConsumer<CommandSender, ComponentLike> componentSender, BiConsumer<Player, ComponentLike> actionBarSender,
                                  HexConsumer<Player, ComponentLike, ComponentLike, Integer, Integer, Integer> titleSender) {
        ComponentDispatcher.componentSender = componentSender;
        ComponentDispatcher.actionBarSender = actionBarSender;
        ComponentDispatcher.titleSender = titleSender;
    }

    public static void send(CommandSender commandSender, ComponentLike component) {
        Component comp = component.asComponent();
        if (comp.equals(Component.empty())) {
            return;
        }
        componentSender.accept(commandSender, comp);
    }

    public static void sendActionBar(Player player, ComponentLike component) {
        actionBarSender.accept(player, component);
    }

    public static void sendTitle(Player player, ComponentLike title, ComponentLike subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        titleSender.accept(player, title, subtitle, fadeInTicks, stayTicks, fadeOutTicks);
    }
}
