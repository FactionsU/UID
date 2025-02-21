package dev.kitteh.factions.chat;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ChatTarget {
    Public PUBLIC = new Public();

    record Relation(dev.kitteh.factions.permissible.Relation relation) implements ChatTarget {
        public static final Relation ALLY = new Relation(dev.kitteh.factions.permissible.Relation.ALLY);
        public static final Relation TRUCE = new Relation(dev.kitteh.factions.permissible.Relation.TRUCE);
    }

    record Role(dev.kitteh.factions.permissible.Role role) implements ChatTarget {
         public static final Role COLEADER = new Role(dev.kitteh.factions.permissible.Role.COLEADER);
         public static final Role MODERATOR = new Role(dev.kitteh.factions.permissible.Role.MODERATOR);
         public static final Role NORMAL = new Role(dev.kitteh.factions.permissible.Role.NORMAL);
         public static final Role ALL = new Role(dev.kitteh.factions.permissible.Role.RECRUIT);
    }

    record Public() implements ChatTarget {
    }
}
