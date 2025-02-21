package dev.kitteh.factions.util;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ComponentDispatcher {
    private static boolean isPaper;
    private static MethodHandle sendMessage;
    private static MethodHandle deserialize;
    private static Object gsonSerializer;

    static {
        try {
            Class<?> audienceClass = Class.forName("net..kyori.adventure.Audience".replace("..", "."));
            Class<?> componentClass = Class.forName("net..kyori.adventure.text.Component".replace("..", "."));

            MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
            MethodType sendMessageType = MethodType.methodType(void.class, componentClass);

            sendMessage = publicLookup.findVirtual(audienceClass, "sendMessage", sendMessageType);

            Class<?> gsonSerializerClass = Class.forName("net..kyori.adventure.text.serializer.gson.GsonComponentSerializer".replace("..", "."));

            gsonSerializer = publicLookup.findStatic(gsonSerializerClass, "gson", MethodType.methodType(gsonSerializerClass)).invokeExact();

            deserialize = publicLookup.findVirtual(gsonSerializerClass, "deserializeFromTree", MethodType.methodType(componentClass, JsonElement.class));

            isPaper = true;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ignored) {
        } catch (Throwable e) {
            throw new RuntimeException("WHAT", e);
        }
    }

    public static void send(CommandSender commandSender, ComponentLike component) {
        if (isPaper) {
            try {
                Object comp = deserialize.invokeExact(gsonSerializer, GsonComponentSerializer.gson().serializeToTree(component.asComponent()));
                sendMessage.invoke(commandSender, comp);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            commandSender.spigot().sendMessage(ComponentSerializer.deserialize(GsonComponentSerializer.gson().serializeToTree(component.asComponent())));
        }
    }
}
