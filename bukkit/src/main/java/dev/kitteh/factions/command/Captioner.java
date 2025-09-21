package dev.kitteh.factions.command;

import dev.kitteh.factions.FactionsPlugin;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.caption.DelegatingCaptionProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@ApiStatus.Internal
@NullMarked
public final class Captioner extends DelegatingCaptionProvider<Sender> {
    public static final Caption NO_FACTION_FOUND = Caption.of("argument.parse.failure.faction");
    public static final Caption NO_PLAYER_FOUND = Caption.of("argument.parse.failure.fplayer");

    private static final CaptionProvider<Sender> PROVIDER = CaptionProvider.<Sender>constantProvider()
            .putCaption(NO_FACTION_FOUND, FactionsPlugin.instance().tl().commands().generic().getNoFactionFound())
            .putCaption(NO_PLAYER_FOUND, FactionsPlugin.instance().tl().commands().generic().getNoPlayerFound())
            .build();

    @Override
    public CaptionProvider<Sender> delegate() {
        return PROVIDER;
    }
}
