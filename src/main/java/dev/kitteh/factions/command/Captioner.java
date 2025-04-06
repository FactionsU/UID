package dev.kitteh.factions.command;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.caption.DelegatingCaptionProvider;

public final class Captioner<C> extends DelegatingCaptionProvider<C> {
    public static final Caption NO_FACTION_FOUND = Caption.of("argument.parse.failure.faction");
    public static final Caption NO_PLAYER_FOUND = Caption.of("argument.parse.failure.fplayer");

    private static final CaptionProvider<?> PROVIDER = CaptionProvider.constantProvider()
            .putCaption(NO_FACTION_FOUND, "No faction found for input '<input>'") // TODO TL.GENERIC_NOFACTIONFOUND
            .putCaption(NO_PLAYER_FOUND, "No player found for input '<input>'") // TODO TL.GENERIC_NOPLAYERFOUND
            .build();

    @Override
    public @NonNull CaptionProvider<C> delegate() {
        return (CaptionProvider<C>) PROVIDER;
    }
}
