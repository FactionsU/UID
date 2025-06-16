package dev.kitteh.factions.util;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.function.BiConsumer;

@NullMarked
public class ComponentDispatcher {
    private static BiConsumer<CommandSender, ComponentLike> componentSender = (commandSender, component) ->
            commandSender.spigot().sendMessage(ComponentSerializer.deserialize(GsonComponentSerializer.gson().serializeToTree(component.asComponent())));
    private static BiConsumer<Player, ComponentLike> actionBarSender = (player, component) ->
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, ComponentSerializer.deserialize(GsonComponentSerializer.gson().serializeToTree(component.asComponent())));

    public static void setSenders(BiConsumer<CommandSender, ComponentLike> componentSender,  BiConsumer<Player, ComponentLike> actionBarSender) {
        ComponentDispatcher.componentSender = componentSender;
        ComponentDispatcher.actionBarSender = actionBarSender;
    }

    public static void send(CommandSender commandSender, ComponentLike component) {
        componentSender.accept(commandSender, component);
    }

    public static void sendActionBar(Player player, ComponentLike component) {
        actionBarSender.accept(player, component);
    }
}
