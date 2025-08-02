package dev.kitteh.factions;

import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.permissible.Selectable;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.WarmUpUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Logged in players always have exactly one FPlayer instance. Logged out players may or may not have a stored FPlayer
 * instance. They will always have one if they are part of a faction.
 * <p>
 * The FPlayer is linked to a minecraft player using the player name.
 * <p>
 * The same instance is always returned for the same player. This means you can use the == operator.
 */
@NullMarked
public interface FPlayer extends Participator, Selectable {
    UUID uniqueId();

    String name();

    Faction faction();

    boolean hasFaction();

    void faction(Faction faction);

    boolean autoLeaveExempt();

    void autoLeaveExempt(boolean autoLeave);

    long lastFrostwalkerMessageTime();

    void updateLastFrostwalkerMessageTime();

    void monitorJoins(boolean monitor);

    boolean monitorJoins();

    Role role();

    void role(Role role);

    boolean takeFallDamage();

    void takeFallDamage(boolean fallDamage);

    @Nullable
    Faction autoClaim();

    void autoClaim(@Nullable Faction faction);

    @Nullable
    String autoSetZone();

    void autoSetZone(@Nullable String zone);

    @Nullable
    Faction autoUnclaim();

    void autoUnclaim(@Nullable Faction faction);

    boolean adminBypass();

    void adminBypass(boolean val);

    boolean isVanished();

    ChatTarget chatTarget();

    void chatTarget(ChatTarget chatTarget);

    void ignoreAllianceChat(boolean ignore);

    boolean ignoreAllianceChat();

    void ignoreTruceChat(boolean ignore);

    boolean ignoreTruceChat();

    void spyingChat(boolean chatSpying);

    boolean spyingChat();

    boolean showScoreboard();

    void showScoreboard(boolean show);

    void resetFactionData();

    void resetFactionData(boolean updateCommands);

    long lastLogin();

    boolean mapAutoUpdating();

    void mapAutoUpdating(boolean mapAutoUpdating);

    boolean loginPVPDisabled();

    FLocation lastStoodAt();

    void lastStoodAt(FLocation flocation);

    default String titleLegacy() {
        return Mini.toLegacy(this.title());
    }

    Component title();

    void title(Component title);

    Component nameWithTitle();

    default String nameWithTitleLegacy() {
        return Mini.toLegacy(this.nameWithTitle());
    }

    Component nameWithTag();

    default String nameWithTagLegacy() {
        return Mini.toLegacy(this.nameWithTag());
    }

    int kills();

    int deaths();

    double power();

    void power(double power);

    void alterPower(double delta);

    double powerMax();

    double powerMin();

    int powerRounded();

    int powerMaxRounded();

    int powerMinRounded();

    double powerBoost();

    void powerBoost(double powerBoost);

    void updatePower();

    void losePowerFromBeingOffline();

    void onDeath();

    boolean isInOwnTerritory();

    boolean isInOthersTerritory();

    boolean isInAllyTerritory();

    boolean isInNeutralTerritory();

    boolean isInEnemyTerritory();

    void sendFactionHereMessage(Faction from);

    void leave(boolean makePay);

    boolean canClaimForFaction(Faction forFaction);

    boolean canClaimForFactionAtLocation(Faction forFaction, FLocation location, boolean notifyFailure);

    boolean attemptClaim(Faction forFaction, Location location, boolean notifyFailure);

    boolean attemptClaim(Faction forFaction, FLocation location, boolean notifyFailure);

    boolean attemptUnclaim(Faction forFaction, FLocation flocation, boolean notifyFailure);

    void attemptAutoSetZone(FLocation flocation);

    @Nullable
    Player asPlayer();

    boolean isOnline();

    void sendMessageLegacy(String message);

    void sendMessageLegacy(List<String> messages);

    int mapHeight();

    void mapHeight(int height);

    void eraseData();

    void flightCheck();

    boolean flying();

    void flying(boolean fly);

    void flying(boolean fly, boolean damage);

    boolean autoFlying();

    void autoFlying(boolean autoFly);

    default boolean canFlyAtLocation() {
        return this.canFlyAtLocation(this.lastStoodAt());
    }

    boolean canFlyAtLocation(FLocation location);

    boolean seeChunk();

    void seeChunk(boolean seeingChunk);

    boolean flyTrail();

    void flyTrail(boolean state);

    @Nullable
    String flyTrailEffect();

    void flyTrailEffect(String effect);

    default boolean warmingUp() {
        return this.warmup() != null;
    }

    WarmUpUtil.@Nullable Warmup warmup();

    void addWarmup(WarmUpUtil.Warmup warmup, int taskId);

    void cancelWarmup();
}
