package dev.kitteh.factions.integration;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Participator;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.RelationUtil;
import dev.kitteh.factions.util.TL;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Econ {
    private static Economy econ = null;
    private static final Pattern FACTION_PATTERN = Pattern.compile("^faction-(\\d+)$");

    static {

    }

    public static void setup() {
        if (isSetup()) {
            return;
        }

        String localeString = FactionsPlugin.instance().conf().economy().getLocale();
        DecimalFormat f;
        try {
            String[] split = localeString.split("_");
            f = new DecimalFormat(TL.ECON_FORMAT.toString(), DecimalFormatSymbols.getInstance(Locale.of(split[0], split[1])));
        } catch (Exception e) {
            AbstractFactionsPlugin.instance().getLogger().warning("Fell over on invalid default econ format '" + TL.ECON_FORMAT + "' with locale '" + localeString + "'");
            f = new DecimalFormat("###,###.###");
        }
        format = f;

        String integrationFail = "Economy integration is " + (FactionsPlugin.instance().conf().economy().isEnabled() ? "enabled, but" : "disabled, and") + " the plugin \"Vault\" ";

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

        if (!FactionsPlugin.instance().conf().economy().isEnabled()) {
            AbstractFactionsPlugin.instance().getLogger().info("NOTE: Economy is disabled. You can enable it in config/main.conf");
        }
    }

    public static boolean shouldBeUsed() {
        return FactionsPlugin.instance().conf().economy().isEnabled() && econ != null && econ.isEnabled();
    }

    public static boolean isSetup() {
        return econ != null;
    }

    public static Economy getEcon() {
        return econ;
    }

    private static String getWorld(OfflinePlayer op) {
        return (op instanceof Player) ? ((Player) op).getWorld().getName() : FactionsPlugin.instance().conf().economy().getDefaultWorld();
    }

    public static void modifyUniverseMoney(double delta) {
        if (!shouldBeUsed()) {
            return;
        }

        String universeAccount = FactionsPlugin.instance().conf().economy().getUniverseAccount();

        if (universeAccount == null || universeAccount.isEmpty()) {
            return;
        }
        if (!hasAccount(getUniverseOfflinePlayer())) {
            return;
        }

        modifyBalance(getOfflinePlayerForName(FactionsPlugin.instance().conf().economy().getUniverseAccount()), delta);
    }

    private static OfflinePlayer getUniverseOfflinePlayer() {
        String universeAccount = FactionsPlugin.instance().conf().economy().getUniverseAccount();
        String universeUUID = FactionsPlugin.instance().conf().economy().getUniverseAccountUUID();

        if (universeUUID == null) {
            return getOfflinePlayerForName(universeAccount);
        }
        return AbstractFactionsPlugin.instance().getOfflinePlayer(universeAccount, UUID.fromString(universeUUID));
    }

    public static void sendBalanceInfo(FPlayer to, Participator about) {
        if (!shouldBeUsed()) {
            AbstractFactionsPlugin.instance().log(Level.WARNING, "Vault does not appear to be hooked into an economy plugin.");
            return;
        }
        to.msg(TL.ECON_BALANCE, about.describeToLegacy(to, true), Econ.moneyString(getBalance(about)));
    }

    public static void sendBalanceInfo(CommandSender to, Faction about) {
        if (!shouldBeUsed()) {
            AbstractFactionsPlugin.instance().log(Level.WARNING, "Vault does not appear to be hooked into an economy plugin.");
            return;
        }
        to.sendMessage(ChatColor.stripColor(String.format(TL.ECON_BALANCE.toString(), about.tag(), Econ.moneyString(getBalance(about)))));
    }

    public static boolean canIControlYou(Participator i, Participator you) {
        Faction fI = RelationUtil.getFaction(i);
        Faction fYou = RelationUtil.getFaction(you);

        // This is a system invoker. Accept it.
        if (fI == null) {
            return true;
        }

        // Bypassing players can do any kind of transaction
        if (i instanceof FPlayer fPlayer && fPlayer.adminBypass()) {
            return true;
        }

        // Players with the any-withdraw can do.
        if (i instanceof FPlayer fPlayer && Permission.MONEY_WITHDRAW_ANY.has(fPlayer.asPlayer())) {
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

        // Factions can be controlled by members that are moderators... or any member if any member can withdraw.
        if (you instanceof Faction && fI == fYou && (FactionsPlugin.instance().conf().economy().isBankMembersCanWithdraw() || ((FPlayer) i).role().value >= Role.MODERATOR.value)) {
            return true;
        }

        // Otherwise you may not! ;,,;
        i.msg(TL.ECON_NOPERM, i.describeToLegacy(i, true), you.describeToLegacy(i));
        return false;
    }

    public static boolean transferMoney(Participator invoker, Participator from, Participator to, double amount) {
        return transferMoney(invoker, from, to, amount, true);
    }

    public static boolean transferMoney(Participator invoker, Participator from, Participator to, double amount, boolean notify) {
        if (!shouldBeUsed()) {
            invoker.msg(TL.ECON_DISABLED);
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
            if (invoker != null && notify) {
                invoker.msg(TL.ECON_CANTAFFORD_TRANSFER, from.describeToLegacy(invoker, true), moneyString(amount), to.describeToLegacy(invoker));
            }

            return false;
        }

        // Check if the new balance is over Essential's money cap.
        if (FactionsPlugin.instance().integrationManager().isEnabled(IntegrationManager.Integration.ESS) && Essentials.isOverBalCap(getBalance(toAcc) + amount)) {
            invoker.msg(TL.ECON_OVER_BAL_CAP, amount);
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
            invoker.msg(TL.ECON_TRANSFER_UNABLE, moneyString(amount), to.describeToLegacy(invoker), from.describeToLegacy(invoker, true));
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

        if (invoker == null) {
            for (FPlayer recipient : recipients) {
                recipient.msg(TL.ECON_TRANSFER_NOINVOKER, moneyString(amount), from.describeToLegacy(recipient), to.describeToLegacy(recipient));
            }
        } else if (invoker == from) {
            for (FPlayer recipient : recipients) {
                recipient.msg(TL.ECON_TRANSFER_GAVE, from.describeToLegacy(recipient, true), moneyString(amount), to.describeToLegacy(recipient));
            }
        } else if (invoker == to) {
            for (FPlayer recipient : recipients) {
                recipient.msg(TL.ECON_TRANSFER_TOOK, to.describeToLegacy(recipient, true), moneyString(amount), from.describeToLegacy(recipient));
            }
        } else {
            for (FPlayer recipient : recipients) {
                recipient.msg(TL.ECON_TRANSFER_TRANSFER, invoker.describeToLegacy(recipient, true), moneyString(amount), from.describeToLegacy(recipient), to.describeToLegacy(recipient));
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
            if (toDoThis != null && !toDoThis.isEmpty()) {
                ep.msg(TL.ECON_CANTAFFORD_AMOUNT, ep.describeToLegacy(ep, true), moneyString(delta), toDoThis);
            }
            return false;
        }
        return true;
    }

    public static boolean modifyMoney(Participator ep, double delta, String toDoThis, String forDoingThis) {
        if (!shouldBeUsed()) {
            return false;
        }

        if (delta == 0) {
            return true;
        }

        OfflinePlayer acc = checkStatus(ep.asOfflinePlayer());

        String You = ep.describeToLegacy(ep, true);

        if (delta > 0) {
            // The player should gain money
            // The account might not have enough space
            if (deposit(acc, delta)) {
                modifyUniverseMoney(-delta);
                if (forDoingThis != null && !forDoingThis.isEmpty()) {
                    ep.msg(TL.ECON_GAIN_SUCCESS, You, moneyString(delta), forDoingThis);
                }
                return true;
            } else {
                // transfer to account failed
                if (forDoingThis != null && !forDoingThis.isEmpty()) {
                    ep.msg(TL.ECON_GAIN_FAILURE, You, moneyString(delta), forDoingThis);
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
                    ep.msg(TL.ECON_LOST_SUCCESS, You, moneyString(-delta), forDoingThis);
                }
                return true;
            } else {
                // There was not enough money to pay
                if (toDoThis != null && !toDoThis.isEmpty()) {
                    ep.msg(TL.ECON_LOST_FAILURE, You, moneyString(-delta), toDoThis);
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
        return FactionsPlugin.instance().conf().economy().getCostClaimWilderness() + (FactionsPlugin.instance().conf().economy().getCostClaimWilderness() * FactionsPlugin.instance().conf().economy().getClaimAdditionalMultiplier() * ownedLand) - (takingFromAnotherFaction ? FactionsPlugin.instance().conf().economy().getCostClaimFromFactionBonus() : 0);
    }

    // calculate refund amount for unclaiming land
    public static double calculateClaimRefund(int ownedLand) {
        return calculateClaimCost(ownedLand - 1, false) * FactionsPlugin.instance().conf().economy().getClaimRefundMultiplier();
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
        return calculateTotalLandValue(ownedLand) * FactionsPlugin.instance().conf().economy().getClaimRefundMultiplier();
    }


    // -------------------------------------------- //
    // Standard account management methods
    // -------------------------------------------- //

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

    public static boolean hasAccount(Participator ep) {
        return hasAccount(ep.asOfflinePlayer());
    }

    private static boolean hasAccount(OfflinePlayer op) {
        return econ.hasAccount(op, getWorld(op));
    }

    public static double getBalance(Participator ep) {
        return getBalance(ep.asOfflinePlayer());
    }

    private static double getBalance(OfflinePlayer op) {
        return econ.getBalance(checkStatus(op), getWorld(op));
    }

    public static boolean has(Participator ep, double amount) {
        return has(ep.asOfflinePlayer(), amount);
    }

    private static boolean has(OfflinePlayer op, double amount) {
        return econ.has(checkStatus(op), getWorld(op), amount);
    }

    private static DecimalFormat format;

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
            return econ.withdrawPlayer(op, getWorld(op), current - amount).transactionSuccess();
        } else {
            return econ.depositPlayer(op, getWorld(op), amount - current).transactionSuccess();
        }
    }

    public static boolean modifyBalance(Participator ep, double amount) {
        return modifyBalance(ep.asOfflinePlayer(), amount);
    }

    private static boolean modifyBalance(OfflinePlayer op, double amount) {
        if (amount < 0) {
            return econ.withdrawPlayer(checkStatus(op), getWorld(op), -amount).transactionSuccess();
        } else {
            return econ.depositPlayer(checkStatus(op), getWorld(op), amount).transactionSuccess();
        }
    }

    public static boolean deposit(Participator ep, double amount) {
        return deposit(ep.asOfflinePlayer(), amount);
    }

    private static boolean deposit(OfflinePlayer op, double amount) {
        return econ.depositPlayer(checkStatus(op), getWorld(op), amount).transactionSuccess();
    }

    public static boolean withdraw(Participator ep, double amount) {
        return withdraw(ep.asOfflinePlayer(), amount);
    }

    private static boolean withdraw(OfflinePlayer op, double amount) {
        return econ.withdrawPlayer(checkStatus(op), getWorld(op), amount).transactionSuccess();
    }

    public static void createAccount(Participator ep) {
        createAccount(ep.asOfflinePlayer());
    }

    private static void createAccount(OfflinePlayer op) {
        if (!econ.createPlayerAccount(op, getWorld(op))) {
            AbstractFactionsPlugin.instance().getLogger().warning("FAILED TO CREATE ECONOMY ACCOUNT " + op.getName() + '/' + op.getUniqueId());
        }
    }

    public static OfflinePlayer checkStatus(OfflinePlayer op) {
        if (op.getName() == null || !op.getName().startsWith("faction-")) {
            return op;
        }
        // We need to override the default money given to players.
        String world = getWorld(op);
        if (!hasAccount(op)) {
            createAccount(op);
            setBalance(op, 0);
        }
        return op;
    }
}
