package dev.kitteh.factions.integration;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.Participator;
import dev.kitteh.factions.command.defaults.top.TopMoneyCache;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.ComponentDispatcher;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@NullMarked
public class Econ {
    private static @Nullable Economy econ = null;
    private static final Pattern FACTION_PATTERN = Pattern.compile("^faction-(\\d+)$");

    public static void setup() {
        if (isSetup()) {
            return;
        }

        String localeString = Confs.main().economy().getLocale();
        DecimalFormat f;
        try {
            String[] split = localeString.split("_");
            f = new DecimalFormat(Confs.tl().economy().transfer().getFormat(), DecimalFormatSymbols.getInstance(Locale.of(split[0], split[1])));
            format = f;
        } catch (Exception e) {
            AbstractFactionsPlugin.instance().getLogger().warning("Fell over on invalid default econ format " + Confs.tl().economy().transfer().getFormat() + " with locale '" + localeString + "'");
        }

        String integrationFail = "Economy integration is " + (Confs.main().economy().isEnabled() ? "enabled, but" : "disabled, and") + " the plugin \"Vault\" ";

        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            AbstractFactionsPlugin.instance().getLogger().info(integrationFail + "is not installed.");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            AbstractFactionsPlugin.instance().getLogger().info(integrationFail + "is not hooked into an economy plugin.");
            return;
        }
        econ = rsp.getProvider();

        AbstractFactionsPlugin.instance().getLogger().info("Found economy plugin through Vault: " + econ.getName());

        if (!Confs.main().economy().isEnabled()) {
            AbstractFactionsPlugin.instance().getLogger().info("NOTE: Economy is disabled. You can enable it in config/main.conf");
        } else {
            TopMoneyCache.start(AbstractFactionsPlugin.instance());
        }
    }

    public static boolean shouldBeUsed() {
        return Confs.main().economy().isEnabled() && econ != null && econ.isEnabled();
    }

    @ApiStatus.AvailableSince("4.3.0")
    public static boolean shouldBeUsedWithBanks() {
        return shouldBeUsed() && Confs.main().economy().isBankEnabled();
    }

    @ApiStatus.AvailableSince("4.7.0")
    public static boolean duesEnabled() {
        return shouldBeUsedWithBanks() && Confs.main().economy().isDuesEnabled();
    }

    @ApiStatus.AvailableSince("4.7.0")
    public static boolean rentEnabled() {
        return shouldBeUsedWithBanks() && Confs.main().economy().isRentEnabled();
    }

    @ApiStatus.AvailableSince("4.7.0")
    public static double calculateRent(Faction faction) {
        if (!rentEnabled()) {
            return 0d;
        }
        return calculateRent(faction.claimCount());
    }

    @ApiStatus.AvailableSince("4.7.0")
    public static double calculateRent(int claims) {
        String equation = Confs.main().economy().getRentEquation();
        try {
            double value = new Expression(equation).with("claims", claims).evaluate().getNumberValue().doubleValue();
            return Math.max(0, value);
        } catch (RuntimeException | EvaluationException | ParseException e) {
            AbstractFactionsPlugin.instance().getLogger().warning("Invalid faction rent equation \"" + equation + "\": " + e.getMessage());
            return 0d;
        }
    }

    public static boolean isSetup() {
        return econ != null;
    }

    public static @Nullable Economy getEcon() {
        return econ;
    }

    private static String getWorld(OfflinePlayer op) {
        return (op instanceof Player) ? ((Player) op).getWorld().getName() : Confs.main().economy().getDefaultWorld();
    }

    public static void modifyUniverseMoney(double delta) {
        if (!shouldBeUsed()) {
            return;
        }

        String universeAccount = Confs.main().economy().getUniverseAccount();

        if (universeAccount.isEmpty()) {
            return;
        }
        OfflinePlayer universe = getUniverseOfflinePlayer();
        if (needsAccount(universe)) {
            return;
        }

        modifyBalance(universe, delta);
    }

    private static OfflinePlayer getUniverseOfflinePlayer() {
        String universeAccount = Confs.main().economy().getUniverseAccount();
        String universeUUID = Confs.main().economy().getUniverseAccountUUID();

        if (universeUUID == null) {
            return getOfflinePlayerForName(universeAccount);
        }
        return AbstractFactionsPlugin.instance().getOfflinePlayer(universeAccount, UUID.fromString(universeUUID));
    }

    public static void modifyRentGatheringAccountMoney(double delta) {
        if (!shouldBeUsed()) {
            return;
        }

        String landlordAccount = Confs.main().economy().getUniverseAccount();

        if (landlordAccount.isEmpty()) {
            return;
        }
        OfflinePlayer landlord = getRentGatheringOfflinePlayer();
        if (needsAccount(landlord)) {
            return;
        }

        modifyBalance(landlord, delta);
    }

    private static OfflinePlayer getRentGatheringOfflinePlayer() {
        String rentAccount = Confs.main().economy().getRentGatheringAccount();
        String rentUUID = Confs.main().economy().getRentGatheringAccountUUID();

        if (rentUUID == null) {
            return getOfflinePlayerForName(rentAccount);
        }
        return AbstractFactionsPlugin.instance().getOfflinePlayer(rentAccount, UUID.fromString(rentUUID));
    }

    public static void sendBalanceInfo(FPlayer to, Participator about) {
        if (!shouldBeUsed()) {
            AbstractFactionsPlugin.instance().log(Level.WARNING, "Vault does not appear to be hooked into an economy plugin.");
            return;
        }
        to.sendMessage(Mini.parse(Confs.tl().economy().transfer().getBalance(), to,
                Placeholder.component("entity", about.describeTo(to)),
                Placeholder.unparsed("amount", Econ.moneyString(getBalance(about)))));
    }

    public static void sendBalanceInfo(CommandSender to, Faction about) {
        if (!shouldBeUsed()) {
            AbstractFactionsPlugin.instance().log(Level.WARNING, "Vault does not appear to be hooked into an economy plugin.");
            return;
        }
        ComponentDispatcher.send(to, Mini.parse(Confs.tl().economy().transfer().getBalance(),
                Placeholder.unparsed("entity", about.tag()),
                Placeholder.unparsed("amount", Econ.moneyString(getBalance(about)))));
    }

    public static boolean canIControlYou(Participator i, Participator you) {
        Faction fI = i.faction();
        Faction fYou = you.faction();

        // Bypassing players can do any kind of transaction
        if (i instanceof FPlayer fPlayer && fPlayer.adminBypass()) {
            return true;
        }

        // Players with the any-withdraw can do.
        if (i instanceof FPlayer fPlayer && fPlayer.asPlayer() instanceof Player plr && Permission.MONEY_WITHDRAW_ANY.has(plr)) {
            return true;
        }

        // You can deposit to anywhere you feel like. It's your loss if you can't withdraw it again.
        if (i == you) {
            return true;
        }

        // A faction can always transfer away the money of its members and its own money...
        // This will however probably never happen as a faction does not have free will.
        // Ohh by the way... Yes it could. For daily rent to the faction.
        if (i == fI && fI == fYou) {
            return true;
        }

        // Factions can be controlled by members with the permissible action.
        if (you instanceof Faction faction && fI == fYou && i instanceof FPlayer fpI && faction.hasAccess(fpI, PermissibleActions.ECONOMY, fpI.lastStoodAt())) {
            return true;
        }

        // Otherwise you may not! ;,,;
        i.sendRichMessage(Confs.tl().economy().transfer().getNoPerm(),
                Placeholder.component("you", i.describeTo(i)),
                Placeholder.component("target", you.describeTo(i)));
        return false;
    }

    public static boolean transferMoney(Participator invoker, Participator from, Participator to, double amount) {
        return transferMoney(invoker, from, to, amount, true);
    }

    public static boolean transferMoney(Participator invoker, Participator from, Participator to, double amount, boolean notify) {
        if (!shouldBeUsed()) {
            invoker.sendRichMessage(Confs.tl().economy().transfer().getDisabled());
            return false;
        }

        // The amount must be positive.
        // If the amount is negative we must flip and multiply amount with -1.
        if (amount < 0) {
            amount *= -1;
            Participator temp = from;
            from = to;
            to = temp;
        }

        // Check the rights
        if (!canIControlYou(invoker, from)) {
            return false;
        }

        OfflinePlayer fromAcc = checkStatus(from.asOfflinePlayer());
        OfflinePlayer toAcc = checkStatus(to.asOfflinePlayer());

        // Is there enough money for the transaction to happen?
        if (!has(fromAcc, amount)) {
            // There was not enough money to pay
            if (notify) {
                invoker.sendRichMessage(Confs.tl().economy().transfer().getCantAffordTransfer(),
                        Placeholder.component("from", from.describeTo(invoker)),
                        Placeholder.unparsed("amount", moneyString(amount)),
                        Placeholder.component("to", to.describeTo(invoker)));
            }

            return false;
        }

        // Check if the new balance is over Essential's money cap.
        if (AbstractFactionsPlugin.instance().integrationManager().isEnabled(IntegrationManager.Integrations.ESS) && Essentials.isOverBalCap(getBalance(toAcc) + amount)) {
            invoker.sendRichMessage(Confs.tl().economy().transfer().getOverBalCap(),
                    Placeholder.unparsed("amount", moneyString(amount)));
            return false;
        }

        // Transfer money

        if (withdraw(fromAcc, amount)) {
            if (deposit(toAcc, amount)) {
                if (notify) {
                    sendTransferInfo(invoker, from, to, amount);
                }
                return true;
            } else {
                // transaction failed, refund account
                deposit(fromAcc, amount);
            }
        }

        // if we get here something with the transaction failed
        if (notify) {
            invoker.sendRichMessage(Confs.tl().economy().transfer().getTransferUnable(),
                    Placeholder.unparsed("amount", moneyString(amount)),
                    Placeholder.component("to", to.describeTo(invoker)),
                    Placeholder.component("from", from.describeTo(invoker)));
        }

        return false;
    }

    private static void addFPlayers(Set<FPlayer> fPlayers, Participator participator) {
        if (participator instanceof FPlayer fPlayer) {
            fPlayers.add(fPlayer);
        } else if (participator instanceof Faction faction) {
            fPlayers.addAll(faction.members());
        }
    }

    public static void sendTransferInfo(Participator invoker, Participator from, Participator to, double amount) {
        Set<FPlayer> recipients = new HashSet<>();
        addFPlayers(recipients, invoker);
        addFPlayers(recipients, from);
        addFPlayers(recipients, to);

        var transfer = Confs.tl().economy().transfer();
        if (invoker == from) {
            for (FPlayer recipient : recipients) {
                recipient.sendMessage(Mini.parse(transfer.getTransferGave(), recipient,
                        Placeholder.component("from", from.describeTo(recipient)),
                        Placeholder.unparsed("amount", moneyString(amount)),
                        Placeholder.component("to", to.describeTo(recipient))));
            }
        } else if (invoker == to) {
            for (FPlayer recipient : recipients) {
                recipient.sendMessage(Mini.parse(transfer.getTransferTook(), recipient,
                        Placeholder.component("to", to.describeTo(recipient)),
                        Placeholder.unparsed("amount", moneyString(amount)),
                        Placeholder.component("from", from.describeTo(recipient))));
            }
        } else {
            for (FPlayer recipient : recipients) {
                recipient.sendMessage(Mini.parse(transfer.getTransferTransfer(), recipient,
                        Placeholder.component("invoker", invoker.describeTo(recipient)),
                        Placeholder.unparsed("amount", moneyString(amount)),
                        Placeholder.component("from", from.describeTo(recipient)),
                        Placeholder.component("to", to.describeTo(recipient))));
            }
        }
    }

    public static boolean hasAtLeast(Participator ep, double delta, String toDoThis) {
        if (!shouldBeUsed()) {
            return true;
        }

        boolean affordable = false;
        double currentBalance = getBalance(ep);

        if (currentBalance >= delta) {
            affordable = true;
        }

        if (!affordable) {
            if (!toDoThis.isEmpty()) {
                ep.sendRichMessage(Confs.tl().economy().transfer().getCantAffordAmount(),
                        Placeholder.component("entity", ep.describeTo(ep)),
                        Placeholder.unparsed("amount", moneyString(delta)),
                        Placeholder.unparsed("action", toDoThis));
            }
            return false;
        }
        return true;
    }

    public static boolean modifyMoney(Participator participator, double delta) {
        return modifyMoney(participator, delta, null, null);
    }

    public static boolean modifyMoney(Participator participator, double delta, @Nullable String toDoThis, @Nullable String forDoingThis) {
        if (!shouldBeUsed()) {
            return false;
        }

        if (delta == 0) {
            return true;
        }

        OfflinePlayer acc = checkStatus(participator.asOfflinePlayer());

        var tl = Confs.tl().economy().modification();

        String you = participator instanceof FPlayer ? tl.getYou() : tl.getYourFaction();

        if (delta > 0) {
            // The player should gain money
            // The account might not have enough space
            if (deposit(acc, delta)) {
                modifyUniverseMoney(-delta);
                if (forDoingThis != null && !forDoingThis.isEmpty()) {
                    participator.sendRichMessage(tl.getGainSuccess(),
                            Placeholder.parsed("you", you),
                            Placeholder.parsed("amount", moneyString(delta)),
                            Placeholder.parsed("for", forDoingThis)
                    );
                }
                return true;
            } else {
                // transfer to account failed
                if (forDoingThis != null && !forDoingThis.isEmpty()) {
                    participator.sendRichMessage(tl.getGainFailure(),
                            Placeholder.parsed("you", you),
                            Placeholder.parsed("amount", moneyString(delta)),
                            Placeholder.parsed("for", forDoingThis)
                    );
                }
                return false;
            }
        } else {
            // The player should lose money
            // The player might not have enough.

            if (has(acc, -delta) && withdraw(acc, -delta)) {
                // There is enough money to pay
                modifyUniverseMoney(-delta);
                if (forDoingThis != null && !forDoingThis.isEmpty()) {
                    participator.sendRichMessage(tl.getLossSuccess(),
                            Placeholder.parsed("you", you),
                            Placeholder.parsed("amount", moneyString(-delta)),
                            Placeholder.parsed("for", forDoingThis)
                    );
                }
                return true;
            } else {
                // There was not enough money to pay
                if (toDoThis != null && !toDoThis.isEmpty()) {
                    participator.sendRichMessage(tl.getLossFailure(),
                            Placeholder.parsed("you", you),
                            Placeholder.parsed("amount", moneyString(-delta)),
                            Placeholder.parsed("to", toDoThis)
                    );
                }
                return false;
            }
        }
    }

    public static String moneyString(double amount) {
        return format.format(amount);
    }

    // calculate the cost for claiming land
    public static double calculateClaimCost(int ownedLand, boolean takingFromAnotherFaction) {
        if (!shouldBeUsed()) {
            return 0d;
        }

        // basic claim cost, plus land inflation cost, minus the potential bonus given for claiming from another faction
        return Confs.main().economy().getCostClaimWilderness() + (Confs.main().economy().getCostClaimWilderness() * Confs.main().economy().getClaimAdditionalMultiplier() * ownedLand) - (takingFromAnotherFaction ? Confs.main().economy().getCostClaimFromFactionBonus() : 0);
    }

    // calculate refund amount for unclaiming land
    public static double calculateClaimRefund(int ownedLand) {
        return calculateClaimCost(ownedLand - 1, false) * Confs.main().economy().getClaimRefundMultiplier();
    }

    // calculate value of all owned land
    public static double calculateTotalLandValue(int ownedLand) {
        double amount = 0;
        for (int x = 0; x < ownedLand; x++) {
            amount += calculateClaimCost(x, false);
        }
        return amount;
    }

    // calculate refund amount for all owned land
    public static double calculateTotalLandRefund(int ownedLand) {
        return calculateTotalLandValue(ownedLand) * Confs.main().economy().getClaimRefundMultiplier();
    }


    @SuppressWarnings({"DataFlowIssue", "deprecation"})
    private static OfflinePlayer getOfflinePlayerForName(String name) {
        try {
            Matcher matcher = FACTION_PATTERN.matcher(name);
            if (matcher.find()) {
                return Factions.factions().get(Integer.parseInt(matcher.group(1))).asOfflinePlayer();
            }
            return Bukkit.getOfflinePlayer(UUID.fromString(name));
        } catch (Exception ex) {
            return Bukkit.getOfflinePlayer(name);
        }
    }

    private static boolean needsAccount(OfflinePlayer op) {
        return !Objects.requireNonNull(econ).hasAccount(op, getWorld(op));
    }

    public static double getBalance(Participator ep) {
        return getBalance(ep.asOfflinePlayer());
    }

    private static double getBalance(OfflinePlayer op) {
        return Objects.requireNonNull(econ).getBalance(checkStatus(op), getWorld(op));
    }

    public static boolean has(Participator ep, double amount) {
        return has(ep.asOfflinePlayer(), amount);
    }

    private static boolean has(OfflinePlayer op, double amount) {
        return Objects.requireNonNull(econ).has(checkStatus(op), getWorld(op), amount);
    }

    private static DecimalFormat format = new DecimalFormat("###,###.###");

    public static String getFriendlyBalance(FPlayer player) {
        OfflinePlayer p;
        if ((p = player.asPlayer()) == null) {
            return "0";
        }
        return format.format(getBalance(p));
    }

    public static boolean setBalance(Participator ep, double amount) {
        return setBalance(ep.asOfflinePlayer(), amount);
    }

    private static boolean setBalance(OfflinePlayer op, double amount) {
        double current = getBalance(op); // Already checks status
        if (current > amount) {
            return Objects.requireNonNull(econ).withdrawPlayer(op, getWorld(op), current - amount).transactionSuccess();
        } else {
            return Objects.requireNonNull(econ).depositPlayer(op, getWorld(op), amount - current).transactionSuccess();
        }
    }

    public static boolean modifyBalance(Participator ep, double amount) {
        return modifyBalance(ep.asOfflinePlayer(), amount);
    }

    private static boolean modifyBalance(OfflinePlayer op, double amount) {
        if (amount < 0) {
            return Objects.requireNonNull(econ).withdrawPlayer(checkStatus(op), getWorld(op), -amount).transactionSuccess();
        } else {
            return Objects.requireNonNull(econ).depositPlayer(checkStatus(op), getWorld(op), amount).transactionSuccess();
        }
    }

    public static boolean deposit(Participator ep, double amount) {
        return deposit(ep.asOfflinePlayer(), amount);
    }

    private static boolean deposit(OfflinePlayer op, double amount) {
        return Objects.requireNonNull(econ).depositPlayer(checkStatus(op), getWorld(op), amount).transactionSuccess();
    }

    public static boolean withdraw(Participator ep, double amount) {
        return withdraw(ep.asOfflinePlayer(), amount);
    }

    private static boolean withdraw(OfflinePlayer op, double amount) {
        return Objects.requireNonNull(econ).withdrawPlayer(checkStatus(op), getWorld(op), amount).transactionSuccess();
    }

    private static void createAccount(OfflinePlayer op) {
        if (!Objects.requireNonNull(econ).createPlayerAccount(op, getWorld(op))) {
            AbstractFactionsPlugin.instance().getLogger().warning("FAILED TO CREATE ECONOMY ACCOUNT " + op.getName() + '/' + op.getUniqueId());
        }
    }

    public static OfflinePlayer checkStatus(OfflinePlayer op) {
        if (op.getName() == null || !op.getName().startsWith("faction-")) {
            return op;
        }
        // We need to override the default money given to players.
        if (needsAccount(op)) {
            createAccount(op);
            setBalance(op, 0);
        }
        return op;
    }
}
