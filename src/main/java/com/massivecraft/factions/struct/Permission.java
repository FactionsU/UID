package com.massivecraft.factions.struct;

import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.command.CommandSender;

public enum Permission {
    MANAGE_SAFE_ZONE("managesafezone"),
    MANAGE_WAR_ZONE("managewarzone"),
    OWNERSHIP_BYPASS("ownershipbypass"),
    ADMIN("admin"),
    ADMIN_ANY("admin.any"),
    AHOME("ahome"),
    ANNOUNCE("announce"),
    AUTOCLAIM("autoclaim"),
    AUTO_LEAVE_BYPASS("autoleavebypass"),
    BAN("ban"),
    BYPASS("bypass"),
    CHAT("chat"),
    CHATSPY("chatspy"),
    CLAIM("claim"),
    CLAIMAT("claimat"),
    CLAIM_FILL("claim.fill"),
    CLAIM_LINE("claim.line"),
    CLAIM_RADIUS("claim.radius"),
    COLEADER("coleader"),
    COLEADER_ANY("coleader.any"),
    COORDS("coords"),
    CREATE("create"),
    DEFAULTRANK("defaultrank"),
    DEINVITE("deinvite"),
    DELHOME("delhome"),
    DESCRIPTION("description"),
    DISBAND("disband"),
    DISBAND_ANY("disband.any"),
    DTR("dtr"),
    DTR_ANY("dtr.any"),
    FLY("fly"),
    FLY_AUTO("fly.auto"),
    FLY_WILDERNESS("fly.wilderness"),
    FLY_SAFEZONE("fly.safezone"),
    FLY_WARZONE("fly.warzone"),
    FLY_TRAILS("fly.trails"),
    HELP("help"),
    HOME("home"),
    INVITE("invite"),
    JOIN("join"),
    JOIN_ANY("join.any"),
    JOIN_OTHERS("join.others"),
    KICK("kick"),
    KICK_ANY("kick.any"),
    LEAVE("leave"),
    LINK("link"),
    LIST("list"),
    LISTCLAIMS("listclaims"),
    LISTCLAIMS_OTHER("listclaims.other"),
    LOCK("lock"),
    MAP("map"),
    MAPHEIGHT("mapheight"),
    MOD("mod"),
    MOD_ANY("mod.any"),
    MODIFY_DTR("modifydtr"),
    MODIFY_POWER("modifypower"),
    MONEY_BALANCE("money.balance"),
    MONEY_BALANCE_ANY("money.balance.any"),
    MONEY_DEPOSIT("money.deposit"),
    MONEY_WITHDRAW("money.withdraw"),
    MONEY_WITHDRAW_ANY("money.withdraw.any"),
    MONEY_F2F("money.f2f"),
    MONEY_F2P("money.f2p"),
    MONEY_P2F("money.p2f"),
    MONEY_MODIFY("money.modify"),
    MONITOR_LOGINS("monitorlogins"),
    NO_BOOM("noboom"),
    OPEN("open"),
    OWNER("owner"),
    OWNERLIST("ownerlist"),
    SET_PEACEFUL("setpeaceful"),
    SET_PERMANENT("setpermanent"),
    SET_PERMANENTPOWER("setpermanentpower"),
    SHOW_INVITES("showinvites"),
    PERMISSIONS("permissions"),
    POWERBOOST("powerboost"),
    POWER("power"),
    POWER_ANY("power.any"),
    PROMOTE("promote"),
    RELATION("relation"),
    RELOAD("reload"),
    SAVE("save"),
    SETHOME("sethome"),
    SETHOME_ANY("sethome.any"),
    SHOW("show"),
    SHOW_BYPASS_EXEMPT("show.bypassexempt"),
    STATUS("status"),
    STUCK("stuck"),
    TAG("tag"),
    TITLE("title"),
    TITLE_COLOR("title.color"),
    TNT_INFO("tnt.info"),
    TNT_DEPOSIT("tnt.deposit"),
    TNT_WITHDRAW("tnt.withdraw"),
    TNT_FILL("tnt.fill"),
    TNT_SIPHON("tnt.siphon"),
    TOGGLE_ALLIANCE_CHAT("togglealliancechat"),
    UNCLAIM("unclaim"),
    UNCLAIM_FILL("unclaim.fill"),
    UNCLAIM_ALL("unclaimall"),
    VERSION("version"),
    SCOREBOARD("scoreboard"),
    SEECHUNK("seechunk"),
    SETWARP("setwarp"),
    TOP("top"),
    VAULT("vault"),
    SETMAXVAULTS("setmaxvaults"),
    NEAR("near"),
    WARP("warp"),
    UPDATES("updates"),
    DEBUG("debug"),
    WARMUP_EXEMPT("warmups.exempt");

    public final String node;

    Permission(final String node) {
        this.node = "factions." + node;
    }

    @Override
    public String toString() {
        return this.node;
    }

    public boolean has(CommandSender sender, boolean informSenderIfNot) {
        return FactionsPlugin.getInstance().getPermUtil().has(sender, this.node, informSenderIfNot);
    }

    public boolean has(CommandSender sender) {
        return has(sender, false);
    }
}
