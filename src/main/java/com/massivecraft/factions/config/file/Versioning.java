package com.massivecraft.factions.config.file;

import com.massivecraft.factions.config.annotation.Comment;

public class Versioning {
    public static class Info {
        @Comment("Don't change this value yourself, unless you WANT a broken config!")
        private int version = 2;
    }

    private Versioning.Info aVeryFriendlyFactionsConfig = new Versioning.Info();
}
