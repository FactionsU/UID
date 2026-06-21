package dev.kitteh.factions.permissible;

import com.google.gson.annotations.SerializedName;
import dev.kitteh.factions.FactionsPlugin;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public enum Role implements Permissible {
    @SerializedName(value = "ADMIN", alternate = {"LEADER"}) // Import
    ADMIN(4),
    COLEADER(3),
    MODERATOR(2),
    NORMAL(1),
    RECRUIT(0);

    @Deprecated(forRemoval = true, since = "4.6.0")
    public final int value;
    @Deprecated(forRemoval = true, since = "4.6.0")
    public final String nicename = "";
    private @Nullable Set<String> roleNamesAtOrBelow;
    private @Nullable Set<String> roleNamesAtOrAbove;

    Role(final int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public boolean isAtLeast(Role role) {
        return this.value >= role.value;
    }

    public boolean isAtMost(Role role) {
        return this.value <= role.value;
    }

    public static @Nullable Role getRelative(Role role, int relative) {
        return Role.getByValue(role.value + relative);
    }

    public static @Nullable Role getByValue(int value) {
        return switch (value) {
            case 0 -> RECRUIT;
            case 1 -> NORMAL;
            case 2 -> MODERATOR;
            case 3 -> COLEADER;
            case 4 -> ADMIN;
            default -> null;
        };
    }

    public static @Nullable Role fromString(String check) {
        return switch (check.toLowerCase(Locale.ROOT)) {
            case "admin" -> ADMIN;
            case "coleader", "coowner" -> COLEADER;
            case "mod", "moderator" -> MODERATOR;
            case "normal", "member" -> NORMAL;
            case "recruit", "rec" -> RECRUIT;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return this.translation();
    }

    @Override
    public String translation() {
        return switch (this) {
            case ADMIN -> FactionsPlugin.instance().tl().general().roles().getAdmin();
            case COLEADER -> FactionsPlugin.instance().tl().general().roles().getColeader();
            case MODERATOR -> FactionsPlugin.instance().tl().general().roles().getModerator();
            case NORMAL -> FactionsPlugin.instance().tl().general().roles().getNormal();
            case RECRUIT -> FactionsPlugin.instance().tl().general().roles().getRecruit();
        };
    }

    public String getPrefix() {
        return switch (this) {
            case ADMIN -> FactionsPlugin.instance().conf().factions().prefixes().getAdmin();
            case COLEADER -> FactionsPlugin.instance().conf().factions().prefixes().getColeader();
            case MODERATOR -> FactionsPlugin.instance().conf().factions().prefixes().getMod();
            case NORMAL -> FactionsPlugin.instance().conf().factions().prefixes().getNormal();
            case RECRUIT -> FactionsPlugin.instance().conf().factions().prefixes().getRecruit();
        };
    }

    @Override
    public TextColor color() {
        return Relation.MEMBER.color();
    }

    /**
     * Gets this role name and roles above it in priority. These names are
     * not localized and will always match the enum values.
     *
     * @return an immutable set of role names
     */
    public Set<String> getRoleNamesAtOrAbove() {
        if (this.roleNamesAtOrAbove == null) {
            Set<String> set = new HashSet<>();
            for (Role role : values()) {
                if (this.isAtMost(role)) {
                    set.add(role.name().toLowerCase());
                }
            }
            this.roleNamesAtOrAbove = Collections.unmodifiableSet(set);
        }
        return this.roleNamesAtOrAbove;
    }

    /**
     * Gets this role name and roles below it in priority. These names are
     * not localized and will always match the enum values.
     *
     * @return an immutable set of role names
     */
    public Set<String> getRoleNamesAtOrBelow() {
        if (this.roleNamesAtOrBelow == null) {
            Set<String> set = new HashSet<>();
            for (Role role : values()) {
                if (this.isAtLeast(role)) {
                    set.add(role.name().toLowerCase());
                }
            }
            this.roleNamesAtOrBelow = Collections.unmodifiableSet(set);
        }
        return this.roleNamesAtOrBelow;
    }
}
