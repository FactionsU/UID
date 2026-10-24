package dev.kitteh.factions.command.paper;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.util.Mini;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public final class Dialogue {
    public static final ClickCallback.Options CLICK_CALLBACK = ClickCallback.Options.builder().build();

    public static List<DialogBody> body(List<String> body, TagResolver... tagResolvers) {
        return List.of(DialogBody.plainMessage(Mini.parse(body, tagResolvers), 400));
    }

    public static List<DialogBody> body(List<String> body, FPlayer fPlayer, TagResolver... tagResolvers) {
        return List.of(DialogBody.plainMessage(Mini.parse(body, fPlayer, tagResolvers), 400));
    }
}
