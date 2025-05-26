package dev.kitteh.factions.gui;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WarmUpUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.InactivityConversationCanceller;
import org.bukkit.conversations.ManuallyAbandonedConversationCanceller;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarpGUI extends GUI<Integer> {
    private static final SimpleItem warpItem;
    private static final SimpleItem passwordModifier;

    static {
        warpItem = SimpleItem.builder()
                .setName("&8[&7{warp}&8]")
                .setMaterial(Material.LIME_STAINED_GLASS)
                .build();
        passwordModifier = SimpleItem.builder()
                .setMaterial(Material.BLACK_STAINED_GLASS)
                .setLore(Collections.singletonList("&8Password Protected"))
                .build();
    }

    private final List<String> warps;
    private final String name;
    private final int page;
    private final Faction faction;

    public WarpGUI(FPlayer user, Faction faction) {
        this(user, -1, faction);
    }

    @SuppressWarnings("this-escape")
    private WarpGUI(FPlayer user, int page, Faction faction) {
        super(user, getRows(faction));
        this.faction = faction;
        warps = new ArrayList<>(faction.warps().keySet());
        if (page == -1 && warps.size() > (5 * 9)) {
            page = 0;
        }
        this.page = page;
        name = page == -1 ? TL.GUI_WARPS_ONE_PAGE.format(faction.tag()) : TL.GUI_WARPS_PAGE.format(faction.tag(), page + 1);
        build();
    }

    @Override
    public String getName() {
        return name;
    }

    private static int getRows(Faction faction) {
        int warpCount = faction.warps().size();
        if (warpCount == 0) {
            return 1;
        }
        if (warpCount > (5 * 9)) {
            return 6;
        }
        return (int) Math.ceil(((double) warpCount) / 9D);
    }

    @Override
    protected String parse(String toParse, Integer index) {
        if (index < 0) {
            return toParse;
        }
        if (warps.size() > index) {
            toParse = toParse.replace("{warp}", warps.get(index));
        }
        return toParse;
    }

    @Override
    protected void onClick(Integer index) {
        if (!faction.hasAccess(this.user, PermissibleActions.WARP, this.user.lastStoodAt())) {
            user.msgLegacy(TL.COMMAND_FWARP_NOACCESS, faction.tagLegacy(user));
            this.user.asPlayer().closeInventory();
            return;
        }
        if (index == -1) {
            int targetPage = page + 1;
            new WarpGUI(this.user, targetPage, faction).open();
            return;
        }
        if (index == -2) {
            int targetPage = page - 1;
            new WarpGUI(this.user, targetPage, faction).open();
            return;
        }
        // Check if there are enough faction warps for this index
        if (warps.size() > index) {
            String warp = warps.get(index);
            if (!faction.hasWarpPassword(warp)) {
                if (transact()) {
                    doWarmup(warp);
                }
            } else {
                HashMap<Object, Object> sessionData = new HashMap<>();
                sessionData.put("warp", warp);
                PasswordPrompt passwordPrompt = new PasswordPrompt();
                ConversationFactory inputFactory = new ConversationFactory(AbstractFactionsPlugin.instance())
                        .withModality(false)
                        .withLocalEcho(false)
                        .withInitialSessionData(sessionData)
                        .withFirstPrompt(passwordPrompt)
                        .addConversationAbandonedListener(passwordPrompt)
                        .withTimeout(5);// TODO get config.getInt("password-timeout", 5)

                user.asPlayer().closeInventory();
                inputFactory.buildConversation(user.asPlayer()).begin();
            }
        }
    }

    @Override
    protected Map<Integer, Integer> createSlotMap() {
        Map<Integer, Integer> map = new HashMap<>();
        final int num;
        final int startingIndex;
        if (page == -1) {
            num = warps.size();
            startingIndex = 0;
        } else {
            num = Math.min(warps.size() - ((5 * 9) * page), 5 * 9);
            startingIndex = (5 * 9) * page;
            if (page > 0) {
                map.put(45, -2);
            }
            if (page < (getMaxPages() - 1)) {
                map.put(53, -1);
            }
        }
        int modifier = 0;
        for (int warpIndex = startingIndex, count = 0; count < num; warpIndex++, count++) {
            if (count % 9 == 0) {
                int remaining = num - count;
                if (remaining < 9) {
                    modifier = (9 - remaining) / 2;
                }
            }
            map.put(count + modifier, warpIndex);
        }
        return map;
    }

    @Override
    protected SimpleItem getItem(Integer index) {
        if (index == -1) {
            return SimpleItem.builder().setName(TL.GUI_BUTTON_NEXT.toString()).setMaterial(Material.ARROW).build();
        }
        if (index == -2) {
            return SimpleItem.builder().setName(TL.GUI_BUTTON_PREV.toString()).setMaterial(Material.ARROW).build();
        }
        SimpleItem item = new SimpleItem(warpItem);
        if (faction.hasWarpPassword(warps.get(index))) {
            item.merge(passwordModifier);
        }
        return item;
    }

    private int getMaxPages() {
        if (warps.size() <= (5 * 9)) {
            return 0;
        }
        return (int) Math.ceil(((double) warps.size()) / (5d * 9d));
    }

    @Override
    protected Map<Integer, SimpleItem> createDummyItems() {
        return Collections.emptyMap();
    }

    private class PasswordPrompt extends StringPrompt implements ConversationAbandonedListener {

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext context) {
            return TL.COMMAND_FWARP_PASSWORD_REQUIRED.toString();
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            String warp = (String) context.getSessionData("warp");
            if (faction.isWarpPassword(warp, input)) {
                // Valid Password, make em pay
                if (transact()) {
                    doWarmup(warp);
                }
            } else {
                // Invalid Password
                user.msgLegacy(TL.COMMAND_FWARP_INVALID_PASSWORD);
            }
            return END_OF_CONVERSATION;
        }

        @Override
        public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
            if (abandonedEvent.getCanceller() instanceof ManuallyAbandonedConversationCanceller ||
                    abandonedEvent.getCanceller() instanceof InactivityConversationCanceller) {
                user.msgLegacy(TL.COMMAND_FWARP_PASSWORD_CANCEL);
                open();
            }
        }
    }

    private void doWarmup(final String warp) {
        WarmUpUtil.process(user, WarmUpUtil.Warmup.WARP, TL.WARMUPS_NOTIFY_TELEPORT, warp, () -> {
            Player player = Bukkit.getPlayer(user.asPlayer().getUniqueId());
            if (player != null) {
                if (!faction.hasAccess(this.user, PermissibleActions.WARP, this.user.lastStoodAt())) {
                    user.msgLegacy(TL.COMMAND_FWARP_NOACCESS, faction.tagLegacy(user));
                    return;
                }
                AbstractFactionsPlugin.instance().teleport(player, faction.warp(warp).asLocation()).thenAccept(success -> {
                    if (success) {
                        user.msgLegacy(TL.COMMAND_FWARP_WARPED, warp);
                    }
                });
            }
        }, FactionsPlugin.instance().conf().commands().warp().getDelay());
    }

    private boolean transact() {
        if (!user.adminBypass()) {
            return true;
        }

        double cost = FactionsPlugin.instance().conf().economy().getCostWarp();

        if (!Econ.shouldBeUsed() || this.user == null || cost == 0.0 || user.adminBypass()) {
            return true;
        }

        if (FactionsPlugin.instance().conf().economy().isBankEnabled() && FactionsPlugin.instance().conf().economy().isBankFactionPaysCosts() && user.hasFaction() && user.faction().hasAccess(user, PermissibleActions.ECONOMY, this.user.lastStoodAt())) {
            return Econ.modifyMoney(user.faction(), -cost, TL.COMMAND_FWARP_TOWARP.toString(), TL.COMMAND_FWARP_FORWARPING.toString());
        } else {
            return Econ.modifyMoney(user, -cost, TL.COMMAND_FWARP_TOWARP.toString(), TL.COMMAND_FWARP_FORWARPING.toString());
        }
    }

}
