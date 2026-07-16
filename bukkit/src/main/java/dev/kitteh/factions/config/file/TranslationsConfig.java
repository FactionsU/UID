package dev.kitteh.factions.config.file;

import dev.kitteh.factions.annotation.NoFinalFields;
import dev.kitteh.factions.config.annotation.Comment;
import dev.kitteh.factions.config.annotation.WipeOnReload;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jspecify.annotations.Nullable;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

@NoFinalFields
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
public class TranslationsConfig {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public static class AColorfulMessage {
        @Comment("""
                Custom color names so you can more easily re-style.
                Here are the defaults:
                
                info: Normal info
                focus: Gently highlight items within an info message.
                
                deny: Rejection
                denyfocus: Gently highlight items within a deny message.
                
                positive: Positive
                currently used for a few enabled statuses
                
                negative: Not rejection but still a negative thing
                negativefocus: Gently highlight within a negative message
                Note that these are the same color as deny by default
                
                golden: A gold that fits with the others
                goldenfocus: Gently highlight within a golden message
                
                titling: A color used in the title for <title>
                """)
        private Colors colors = new Colors();
        @Comment("Define your own custom names and colors here, to make them valid for MiniMessage strings in this plugin!\n" +
                "Names must be lowercase and cannot be one of the other names")
        private Map<String, String> customColors = new HashMap<>();

        @WipeOnReload
        private transient Map<String, String> allColors;

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        public static class Colors {
            private String info = "#85d2e7";
            private String focus = "#acedff";

            private String deny = "#ffb4ab";
            private String negative = "#ffb4ab";

            private String denyfocus = "#ffdad6";
            private String negativefocus = "#ffdad6";

            private String positive = "#8ad6b7";

            private String golden = "#e6c36c";
            private String goldenfocus = "#ffdf95";

            private String titling = "#6ce677";

            @WipeOnReload
            private transient TextColor infoColor;
            @WipeOnReload
            private transient TextColor focusColor;
            @WipeOnReload
            private transient TextColor denyColor;
        }

        public TextColor info() {
            return colors.infoColor = MainConfig.getColor(colors.info, colors.infoColor, "#85d2e7");
        }

        public TextColor focus() {
            return colors.focusColor = MainConfig.getColor(colors.focus, colors.focusColor, "#acedff");
        }

        public TextColor deny() {
            return colors.denyColor = MainConfig.getColor(colors.deny, colors.denyColor, "#ffb4ab");
        }

        public Map<String, String> getColors() {
            if (this.allColors == null) {
                Map<String, String> newMap = new HashMap<>();
                this.customColors.forEach((key, value) -> newMap.put(key.toLowerCase(Locale.ROOT), value));
                newMap.put("info", colors.info);
                newMap.put("focus", colors.focus);
                newMap.put("deny", colors.deny);
                newMap.put("negative", colors.negative);
                newMap.put("denyfocus", colors.denyfocus);
                newMap.put("negativefocus", colors.negativefocus);
                newMap.put("positive", colors.positive);
                newMap.put("golden", colors.golden);
                newMap.put("goldenfocus", colors.goldenfocus);
                newMap.put("titling", colors.titling);
                this.allColors = Collections.unmodifiableMap(newMap);
            }
            return this.allColors;
        }
    }

    public static class AbsCommand {
        private List<String> aliases;
        private String description;

        protected AbsCommand(String description, String firstAlias, String... aliases) {
            this.aliases = new ArrayList<>();
            this.aliases.add(firstAlias);
            this.aliases.addAll(Arrays.asList(aliases));
            this.description = description;
        }

        public List<String> getAliases() {
            return List.copyOf(this.aliases);
        }

        public String getFirstAlias() {
            return this.getAlias(this.aliases.getFirst());
        }

        public String[] getSecondaryAliases() {
            List<String> secondaries = new ArrayList<>(this.aliases);
            secondaries.removeFirst();
            for (int i = 0; i < secondaries.size(); i++) {
                String alias = secondaries.get(i);
                String newAlias = this.getAlias(alias);
                if (!newAlias.equals(alias)) {
                    secondaries.set(i, newAlias);
                }
            }
            return secondaries.toArray(new String[0]);
        }

        private String getAlias(String alias) {
            if (alias.contains(" ")) {
                AbstractFactionsPlugin.instance().getLogger().warning("Found invalid alias with a space: \"" + alias + "\". Removing spaces to make an ugly command, so you notice.");
                alias = alias.replace(" ", "");
            }
            return alias;
        }

        public String getDescription() {
            return this.description;
        }
    }

    public static class Commands {
        public static class Generic {
            private AbsCommand commandRoot = new AbsCommand("Root command for all faction commands", "f");
            private AbsCommand commandAdminRoot = new AbsCommand("Root command for all administrative factions commands", "fa");

            @Comment("Supports <input>")
            private String noFactionFound = "<deny>No faction found for input '<input>'.";
            @Comment("Supports <input>")
            private String noPlayerFound = "<deny>No player found for input '<input>'.";
            private String warmupCancelled = "<deny>You have cancelled your pending action!";

            public static class CommandDeny {
                @Comment("Supports <command>")
                private String permanent = "<deny>You can't use the command '<command>' because you are in a permanent faction.";
                @Comment("Supports <command>")
                private String wilderness = "<deny>You can't use the command '<command>' in the wilderness.";
                @Comment("Supports <command>")
                private String ally = "<deny>You can't use the command '<command>' in ally territory.";
                @Comment("Supports <command>")
                private String truce = "<deny>You can't use the command '<command>' in truce territory.";
                @Comment("Supports <command>")
                private String neutral = "<deny>You can't use the command '<command>' in neutral territory.";
                @Comment("Supports <command>")
                private String enemy = "<deny>You can't use the command '<command>' in enemy territory.";
                @Comment("Supports <command>")
                private String warzone = "<deny>You can't use the command '<command>' in war zone.";

                public String getPermanent() {
                    return permanent;
                }

                public String getWilderness() {
                    return wilderness;
                }

                public String getAlly() {
                    return ally;
                }

                public String getTruce() {
                    return truce;
                }

                public String getNeutral() {
                    return neutral;
                }

                public String getEnemy() {
                    return enemy;
                }

                public String getWarzone() {
                    return warzone;
                }
            }

            private CommandDeny commandDeny = new CommandDeny();

            public CommandDeny commandDeny() {
                return commandDeny;
            }

            public String getWarmupCancelled() {
                return warmupCancelled;
            }

            public String getNoFactionFound() {
                return noFactionFound;
            }

            public String getNoPlayerFound() {
                return noPlayerFound;
            }

            public AbsCommand getCommandRoot() {
                return commandRoot;
            }

            public AbsCommand getCommandAdminRoot() {
                return commandAdminRoot;
            }
        }

        public static class Admin {
            public static class DTR extends AbsCommand {
                public DTR() {
                    super("Change faction DTR", "dtr");
                }

                public static class DTRModify extends AbsCommand {
                    public DTRModify() {
                        super("Modify faction DTR", "modify");
                    }

                    @Comment("Supports <faction>, <faction:dtr_rounded>")
                    private String success = "<info>Set DTR for <faction> to <faction:dtr_rounded>.";

                    public String getSuccess() {
                        return success;
                    }
                }

                public static class DTRResetAll extends AbsCommand {
                    public DTRResetAll() {
                        super("Reset all faction DTR", "reset-all");
                    }

                    private String success = "<info>Reset all faction DTR to their max!";

                    public String getSuccess() {
                        return success;
                    }
                }

                public static class DTRSet extends AbsCommand {
                    public DTRSet() {
                        super("Set faction DTR", "set");
                    }

                    @Comment("Supports <faction>, <faction:dtr_rounded>")
                    private String success = "<info>Set DTR for <faction> to <faction:dtr_rounded>.";

                    public String getSuccess() {
                        return success;
                    }
                }

                private DTRModify modify = new DTRModify();
                private DTRResetAll resetAll = new DTRResetAll();
                private DTRSet set = new DTRSet();

                public DTRModify modify() {
                    return this.modify;
                }

                public DTRResetAll resetAll() {
                    return this.resetAll;
                }

                public DTRSet set() {
                    return this.set;
                }
            }

            public static class Force extends AbsCommand {
                public Force() {
                    super("Override behavior", "force");
                }

                public static class Home extends AbsCommand {
                    public Home() {
                        super("Send a player to their f home no matter what.", "home");
                    }

                    @Comment("Supports <player>")
                    private String success = "<info><player> was sent to their faction home.";
                    private String successNotice = "<info>You were sent to your faction home.";
                    @Comment("Supports <player>")
                    private String offline = "<negative><player> is offline.";
                    @Comment("Supports <player>")
                    private String noHome = "<negative><player>'s faction has no home.";

                    public String getSuccess() {
                        return success;
                    }

                    public String getSuccessNotice() {
                        return successNotice;
                    }

                    public String getOffline() {
                        return offline;
                    }

                    public String getNoHome() {
                        return noHome;
                    }
                }

                public static class Join extends AbsCommand {
                    public Join() {
                        super("Join a faction", "join");
                    }

                    private String deniedSpecial = "<deny>Players cannot join this faction.";
                    @Comment("Supports <player>")
                    private String deniedAlreadyHasFaction = "<deny><player> must leave their current faction first.";
                    @Comment("Supports <faction>")
                    private String successNoticePlayer = "<info>You joined <faction>.";
                    @Comment("Supports <player>")
                    private String successNotice = "<info><player> joined your faction.";
                    @Comment("Supports <player>, <faction>")
                    private String success = "<info><player> forced to join <faction>.";

                    public String getDeniedSpecial() {
                        return deniedSpecial;
                    }

                    public String getDeniedAlreadyHasFaction() {
                        return deniedAlreadyHasFaction;
                    }

                    public String getSuccessNoticePlayer() {
                        return successNoticePlayer;
                    }

                    public String getSuccessNotice() {
                        return successNotice;
                    }

                    public String getSuccess() {
                        return success;
                    }
                }

                private Home home = new Home();
                private Join join = new Join();

                public Home home() {
                    return this.home;
                }

                public Join join() {
                    return this.join;
                }
            }

            public static class Get extends AbsCommand {
                public Get() {
                    super("Get faction details", "get");
                }

                private String output = "<faction> <info><key>:</info> <value>";

                public String getOutput() {
                    return output;
                }
            }

            public static class Power extends AbsCommand {
                public Power() {
                    super("Change faction power", "power");
                }

                public static class PowerBoost extends AbsCommand {
                    public PowerBoost() {
                        super("Apply permanent power bonus/penalty", "boost");
                    }

                    @Comment("Supports <target>, <value>")
                    private String boost = "<info><target> now has a power bonus/penalty of <value> to min and max power levels.";
                    private String subCmdSet = "set";
                    private String subCmdModify = "modify";
                    private String subCmdFaction = "faction";
                    private String subCmdPlayer = "player";

                    public String getBoost() {
                        return boost;
                    }

                    public String getSubCmdSet() {
                        return subCmdSet;
                    }

                    public String getSubCmdModify() {
                        return subCmdModify;
                    }

                    public String getSubCmdFaction() {
                        return subCmdFaction;
                    }

                    public String getSubCmdPlayer() {
                        return subCmdPlayer;
                    }
                }

                public static class ModifyPower extends AbsCommand {
                    public ModifyPower() {
                        super("Modify the power of a player", "modify");
                    }

                    @Comment("Supports <change>, <player>, <power>")
                    private String added = "<info>Added <focus><change></focus> power to <player>. New total rounded power: <focus><power></focus>.";

                    public String getAdded() {
                        return added;
                    }
                }

                public static class PermanentPower extends AbsCommand {
                    public PermanentPower() {
                        super("Toggle faction power permanence", "permanent");
                    }

                    private String grant = "added permanent power status to";
                    private String revoke = "removed permanent power status from";
                    @Comment("Supports <change>, <faction>")
                    private String success = "<info>You <change> <faction>.";
                    @Comment("Supports <player>, <change>")
                    private String factionMsg = "<info><player> <change> your faction.";

                    public String getGrant() {
                        return grant;
                    }

                    public String getRevoke() {
                        return revoke;
                    }

                    public String getSuccess() {
                        return success;
                    }

                    public String getFactionMsg() {
                        return factionMsg;
                    }
                }

                public static class SetPower extends AbsCommand {
                    public SetPower() {
                        super("Set the power of a player", "set");
                    }

                    @Comment("Supports <value>, <player>")
                    private String set = "<info>Set <focus><value></focus> power to <player>.";

                    public String getSet() {
                        return set;
                    }
                }

                private PowerBoost powerBoost = new PowerBoost();
                private ModifyPower modifyPower = new ModifyPower();
                private PermanentPower permanentPower = new PermanentPower();
                private SetPower setPower = new SetPower();

                public PowerBoost powerBoost() {
                    return powerBoost;
                }

                public ModifyPower modifyPower() {
                    return modifyPower;
                }

                public PermanentPower permanentPower() {
                    return permanentPower;
                }

                public SetPower setPower() {
                    return setPower;
                }
            }

            public static class SetCmd extends AbsCommand {
                public SetCmd() {
                    super("Set faction information", "set");
                }

                public static class Boom extends AbsCommand {
                    public Boom() {
                        super("Toggle explosions", "explosions");
                    }

                    private String notNormal = "<deny>This faction can only be controlled via configuration.";
                    @Comment("Supports <faction>")
                    private String setDisabled = "<info>Explosions are now forcibly disabled for <faction> territory.";
                    @Comment("Supports <faction>")
                    private String setNotDisabled = "<info>Explosions are no longer forcibly disabled for <faction> territory.";

                    public String getNotNormal() {
                        return notNormal;
                    }

                    public String getSetNotDisabled() {
                        return setNotDisabled;
                    }

                    public String getSetDisabled() {
                        return setDisabled;
                    }
                }

                public static class Peaceful extends AbsCommand {
                    public Peaceful() {
                        super("Set a faction to peaceful", "peaceful");
                    }

                    private String grant = "granted peaceful status to";
                    private String revoke = "removed peaceful status from";
                    @Comment("Supports <player>, <change>")
                    private String yours = "<info><player> has <change> your faction.";
                    @Comment("Supports <player>, <change>, <faction>")
                    private String other = "<info><player> has <change> <faction>.";

                    public String getGrant() {
                        return grant;
                    }

                    public String getRevoke() {
                        return revoke;
                    }

                    public String getYours() {
                        return yours;
                    }

                    public String getOther() {
                        return other;
                    }
                }

                public static class Permanent extends AbsCommand {
                    public Permanent() {
                        super("Toggles a faction's permanence", "permanent");
                    }

                    private String grant = "added permanent status to";
                    private String revoke = "removed permanent status from";
                    @Comment("Supports <player>, <change>")
                    private String yours = "<info><player> has <change> your faction.";
                    @Comment("Supports <player>, <change>, <faction>")
                    private String other = "<info><player> has <change> the faction '<faction>'.";

                    public String getGrant() {
                        return grant;
                    }

                    public String getRevoke() {
                        return revoke;
                    }

                    public String getYours() {
                        return yours;
                    }

                    public String getOther() {
                        return other;
                    }
                }

                public static class AutoSave extends AbsCommand {
                    public AutoSave() {
                        super("Control autosaving", "autosave");
                    }

                    private String enabled = "<info>Factions autosave enabled.";
                    private String disabled = "<info>Factions autosave disabled.";

                    public String getEnabled() {
                        return enabled;
                    }

                    public String getDisabled() {
                        return disabled;
                    }
                }

                public static class Grace extends AbsCommand {
                    public Grace() {
                        super("Set grace status", "grace");
                    }

                    private String inactive = "<info>Grace disabled!";
                    @Comment("Supports <duration>")
                    private String active = "<info>Grace active! No explosions for <duration>.";

                    public String getInactive() {
                        return inactive;
                    }

                    public String getActive() {
                        return active;
                    }
                }

                private Boom boom = new Boom();
                private Peaceful peaceful = new Peaceful();
                private Permanent permanent = new Permanent();
                private AutoSave autoSave = new AutoSave();
                private Grace grace = new Grace();

                public Boom boom() {
                    return this.boom;
                }

                public Peaceful peaceful() {
                    return peaceful;
                }

                public Permanent permanent() {
                    return permanent;
                }

                public AutoSave autoSave() {
                    return autoSave;
                }

                public Grace grace() {
                    return grace;
                }
            }

            public static class TNT extends AbsCommand {
                public TNT() {
                    super("Change faction TNT bank", "tnt");
                }

                private String subCmdModify = "modify";
                private String subCmdSet = "set";
                @Comment("On top of <faction>, supports <oldamount>")
                private String success = "<info><faction> now has <faction:tnt_bank_balance> TNT.";

                public String getSubCmdModify() {
                    return subCmdModify;
                }

                public String getSubCmdSet() {
                    return subCmdSet;
                }

                public String getSuccess() {
                    return success;
                }
            }

            public static class Bypass extends AbsCommand {
                public Bypass() {
                    super("Enable admin bypass mode", "bypass");
                }

                private String enabled = "You have enabled admin bypass mode. You will be able to build or destroy anywhere.";
                private String disabled = "You have disabled admin bypass mode.";

                public String getEnabled() {
                    return enabled;
                }

                public String getDisabled() {
                    return disabled;
                }
            }

            public static class ChatSpy extends AbsCommand {
                public ChatSpy() {
                    super("Enable admin chat spy mode", "chatspy");
                }

                private String enable = "<info>You have enabled chat spying mode.";
                private String disable = "<info>You have disabled chat spying mode.";

                public String getEnable() {
                    return enable;
                }

                public String getDisable() {
                    return disable;
                }
            }

            public static class Money extends AbsCommand {
                public Money() {
                    super("Modify faction bank money.", "money");
                }

                @Comment("Supports <faction>, <amount>")
                private String modified = "<info>Modified <faction> bank by <amount>.";
                @Comment("Supports <faction>, <amount>")
                private String modifyNotify = "<info>Modified <faction> bank by <amount>.";
                @Comment("Supports <faction>, <amount>")
                private String set = "<info>Set <faction> bank to <amount>.";
                @Comment("Supports <faction>, <amount>")
                private String setNotify = "<info>Modified <faction> bank by <amount>.";
                private String fail = "<deny>Failed to modify!";
                private String subCmdModify = "modify";
                private String subCmdSet = "set";

                public String getModified() {
                    return modified;
                }

                public String getModifyNotify() {
                    return modifyNotify;
                }

                public String getSet() {
                    return set;
                }

                public String getSetNotify() {
                    return setNotify;
                }

                public String getFail() {
                    return fail;
                }

                public String getSubCmdModify() {
                    return subCmdModify;
                }

                public String getSubCmdSet() {
                    return subCmdSet;
                }
            }

            public static class Reload extends AbsCommand {
                public Reload() {
                    super("Reload configuration", "reload");
                }

                @Comment("Supports <millis>")
                private String success = "<info>Reloaded all configuration files, took <millis> ms.";

                public String getSuccess() {
                    return success;
                }
            }

            public static class SaveAll extends AbsCommand {
                public SaveAll() {
                    super("Save all data.", "save-all");
                }

                private String success = "<info>Saving data!";

                public String getSuccess() {
                    return success;
                }
            }

            public static class Upgrades extends AbsCommand {
                public Upgrades() {
                    super("Manage faction upgrades", "upgrades");
                }

                public static class General {
                    private String done = "Done";
                    private String back = "Back";
                    private String cancel = "Cancel";
                    private String returnToList = "Return to Upgrade List";
                    @Comment("Label used for the upgrade cost (the <target> placeholder) on provider pages")
                    private String costLabel = "Cost";

                    public String getDone() {
                        return done;
                    }

                    public String getBack() {
                        return back;
                    }

                    public String getCancel() {
                        return cancel;
                    }

                    public String getReturnToList() {
                        return returnToList;
                    }

                    public String getCostLabel() {
                        return costLabel;
                    }
                }

                public static class ListPage {
                    private String title = "Manage Upgrades";
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Select an upgrade to view or modify it.");
                            this.add("Changes are staged and only applied when you review and confirm them.");
                        }
                    };
                    @Comment("Supports <upgrade>")
                    private String buttonEnabled = "<positive>✔</positive> <upgrade>";
                    @Comment("Supports <upgrade>")
                    private String buttonDisabled = "<negative>✘</negative> <upgrade>";
                    @Comment("Supports <upgrade> (modified upgrades are marked)")
                    private String buttonModified = "<golden>✎</golden> <upgrade>";
                    @Comment("Supports <count>")
                    private String reviewButton = "Review Changes (<count>)";
                    private String reviewButtonNone = "No Pending Changes";

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }

                    public String getButtonEnabled() {
                        return buttonEnabled;
                    }

                    public String getButtonDisabled() {
                        return buttonDisabled;
                    }

                    public String getButtonModified() {
                        return buttonModified;
                    }

                    public String getReviewButton() {
                        return reviewButton;
                    }

                    public String getReviewButtonNone() {
                        return reviewButtonNone;
                    }
                }

                public static class EditPage {
                    @Comment("Supports <upgrade>")
                    private String title = "Edit: <upgrade>";
                    @Comment("Supports <upgrade>, <description>, <enabled>, <maxlevel>, <upgrademaxlevel>, <startinglevel>")
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("<description>");
                            this.add("");
                            this.add("Status: <enabled>");
                            this.add("Max level: <maxlevel> (up to: <upgrademaxlevel>)");
                            this.add("Starting level: <startinglevel>");
                        }
                    };
                    private String enabledYes = "<positive>Enabled";
                    private String enabledNo = "<negative>Disabled";
                    private String unlimited = "Unlimited";
                    private String enableButton = "Enable";
                    private String disableButton = "Disable";
                    private String maxLevelButton = "Set Max Level";
                    private String costButton = "Edit Cost";
                    @Comment("Supports <variable>")
                    private String variableButton = "Edit '<variable>'";
                    private String prerequisitesButton = "Edit Prerequisites";

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }

                    public String getPrerequisitesButton() {
                        return prerequisitesButton;
                    }

                    public String getEnabledYes() {
                        return enabledYes;
                    }

                    public String getEnabledNo() {
                        return enabledNo;
                    }

                    public String getUnlimited() {
                        return unlimited;
                    }

                    public String getEnableButton() {
                        return enableButton;
                    }

                    public String getDisableButton() {
                        return disableButton;
                    }

                    public String getMaxLevelButton() {
                        return maxLevelButton;
                    }

                    public String getCostButton() {
                        return costButton;
                    }

                    public String getVariableButton() {
                        return variableButton;
                    }
                }

                public static class MaxLevelPage {
                    @Comment("Supports <upgrade>")
                    private String title = "Max Level: <upgrade>";
                    @Comment("Supports <upgrademaxlevel>")
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Set the highest level factions may unlock.");
                            this.add("Must be between 1 and <upgrademaxlevel>.");
                        }
                    };
                    private String inputLabel = "Max level";
                    private String confirm = "Set max Level";

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }

                    public String getInputLabel() {
                        return inputLabel;
                    }

                    public String getConfirm() {
                        return confirm;
                    }
                }

                public static class ProviderPage {
                    @Comment("Supports <upgrade>, <target> (cost or variable name)")
                    private String title = "<target>: <upgrade>";
                    @Comment("Supports <type> (current provider type)")
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Choose how values are calculated per level.");
                            this.add("Currently: <type>");
                        }
                    };
                    private String typeEquation = "Equation";
                    private String typeLevelMap = "Per-Level Map";
                    private String equationButton = "Use Equation";
                    private String levelMapButton = "Use Per-Level Map";

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }

                    public String getTypeEquation() {
                        return typeEquation;
                    }

                    public String getTypeLevelMap() {
                        return typeLevelMap;
                    }

                    public String getEquationButton() {
                        return equationButton;
                    }

                    public String getLevelMapButton() {
                        return levelMapButton;
                    }
                }

                public static class EquationPage {
                    @Comment("Supports <upgrade>, <target>")
                    private String title = "Equation: <target>";
                    @Comment("Supports <upgrade>, <target>")
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Enter an expression using 'level' as the variable.");
                            this.add("Example: 100000 * level ^ 2");
                        }
                    };
                    private String inputLabel = "Expression";
                    private String confirm = "Save Equation";

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }

                    public String getInputLabel() {
                        return inputLabel;
                    }

                    public String getConfirm() {
                        return confirm;
                    }
                }

                public static class LevelMapPage {
                    @Comment("Supports <upgrade>, <target>")
                    private String title = "Per-Level: <target>";
                    @Comment("Supports <upgrade>, <target>, <maxlevel>")
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Enter a value for each level up to <maxlevel>.");
                        }
                    };
                    @Comment("Supports <level>")
                    private String inputLabel = "Level <level>";
                    private String confirm = "Save Values";

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }

                    public String getInputLabel() {
                        return inputLabel;
                    }

                    public String getConfirm() {
                        return confirm;
                    }
                }

                public static class PrerequisitesPage {
                    @Comment("Supports <upgrade>")
                    private String title = "Prerequisites: <upgrade>";
                    @Comment("Supports <upgrade>")
                    private List<String> bodyHeader = new ArrayList<>() {
                        {
                            this.add("Factions must own these upgrades before purchasing <upgrade>:");
                            this.add("");
                        }
                    };
                    @Comment("Supports <upgrade>, <level>")
                    private String lineEntry = "<gray>- <upgrade> (level <level>)";
                    private List<String> bodyNone = new ArrayList<>() {
                        {
                            this.add("No prerequisites are set.");
                        }
                    };
                    private String addButton = "Add Prerequisite";
                    @Comment("Supports <upgrade>, <level>")
                    private String removeButton = "Remove <upgrade>";

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBodyHeader() {
                        return bodyHeader;
                    }

                    public String getLineEntry() {
                        return lineEntry;
                    }

                    public List<String> getBodyNone() {
                        return bodyNone;
                    }

                    public String getAddButton() {
                        return addButton;
                    }

                    public String getRemoveButton() {
                        return removeButton;
                    }
                }

                public static class AddPrerequisitePage {
                    @Comment("Supports <upgrade>")
                    private String title = "Add Prerequisite: <upgrade>";
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Select an upgrade to require.");
                        }
                    };
                    private List<String> bodyNone = new ArrayList<>() {
                        {
                            this.add("There are no other upgrades available to require.");
                        }
                    };
                    @Comment("Supports <upgrade>")
                    private String entryButton = "<upgrade>";

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }

                    public List<String> getBodyNone() {
                        return bodyNone;
                    }

                    public String getEntryButton() {
                        return entryButton;
                    }
                }

                public static class PrerequisiteLevelPage {
                    @Comment("Supports <upgrade> (the required upgrade), <parent> (the upgrade being edited)")
                    private String title = "Required Level: <upgrade>";
                    @Comment("Supports <upgrade>, <parent>, <upgrademaxlevel>")
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Minimum level of <upgrade> a faction must own.");
                            this.add("Must be between 1 and <upgrademaxlevel>.");
                        }
                    };
                    private String inputLabel = "Required level";
                    private String confirm = "Add Prerequisite";

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }

                    public String getInputLabel() {
                        return inputLabel;
                    }

                    public String getConfirm() {
                        return confirm;
                    }
                }

                public static class ReviewPage {
                    private String title = "Review Changes";
                    private List<String> bodyHeader = new ArrayList<>() {
                        {
                            this.add("The following changes are staged:");
                            this.add("");
                        }
                    };
                    @Comment("Supports <upgrade>, <from>, <to>")
                    private String lineEnabled = "<golden><upgrade></golden>: status <from> → <to>";
                    @Comment("Supports <upgrade>")
                    private String lineSettings = "<golden><upgrade></golden>: settings updated";
                    private List<String> bodyNone = new ArrayList<>() {
                        {
                            this.add("There are no staged changes to apply.");
                        }
                    };
                    private String applyButton = "Apply Changes";
                    private String reEditButton = "Keep Editing";
                    private String cancelButton = "Discard All";

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBodyHeader() {
                        return bodyHeader;
                    }

                    public String getLineEnabled() {
                        return lineEnabled;
                    }

                    public String getLineSettings() {
                        return lineSettings;
                    }

                    public List<String> getBodyNone() {
                        return bodyNone;
                    }

                    public String getApplyButton() {
                        return applyButton;
                    }

                    public String getReEditButton() {
                        return reEditButton;
                    }

                    public String getCancelButton() {
                        return cancelButton;
                    }
                }

                public static class Applied {
                    private String title = "Changes Applied";
                    @Comment("Supports <count>")
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Applied <count> change(s) successfully.");
                        }
                    };

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }
                }

                public static class Error {
                    private String title = "Error";
                    @Comment("Supports <reason>")
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("<reason>");
                        }
                    };
                    private String invalidNumber = "That is not a valid number.";
                    private String invalidEquation = "That equation could not be parsed.";
                    @Comment("Supports <limit>")
                    private String mapLimitExceeded = "Max level is too high for a per-level map (limit <limit>). Use an equation.";

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }

                    public String getInvalidNumber() {
                        return invalidNumber;
                    }

                    public String getInvalidEquation() {
                        return invalidEquation;
                    }

                    public String getMapLimitExceeded() {
                        return mapLimitExceeded;
                    }
                }

                private General general = new General();
                private ListPage listPage = new ListPage();
                private EditPage editPage = new EditPage();
                private MaxLevelPage maxLevelPage = new MaxLevelPage();
                private ProviderPage providerPage = new ProviderPage();
                private EquationPage equationPage = new EquationPage();
                private LevelMapPage levelMapPage = new LevelMapPage();
                private PrerequisitesPage prerequisitesPage = new PrerequisitesPage();
                private AddPrerequisitePage addPrerequisitePage = new AddPrerequisitePage();
                private PrerequisiteLevelPage prerequisiteLevelPage = new PrerequisiteLevelPage();
                private ReviewPage reviewPage = new ReviewPage();
                private Applied applied = new Applied();
                private Error error = new Error();

                public General general() {
                    return general;
                }

                public ListPage listPage() {
                    return listPage;
                }

                public EditPage editPage() {
                    return editPage;
                }

                public MaxLevelPage maxLevelPage() {
                    return maxLevelPage;
                }

                public ProviderPage providerPage() {
                    return providerPage;
                }

                public EquationPage equationPage() {
                    return equationPage;
                }

                public LevelMapPage levelMapPage() {
                    return levelMapPage;
                }

                public PrerequisitesPage prerequisitesPage() {
                    return prerequisitesPage;
                }

                public AddPrerequisitePage addPrerequisitePage() {
                    return addPrerequisitePage;
                }

                public PrerequisiteLevelPage prerequisiteLevelPage() {
                    return prerequisiteLevelPage;
                }

                public ReviewPage reviewPage() {
                    return reviewPage;
                }

                public Applied applied() {
                    return applied;
                }

                public Error error() {
                    return error;
                }
            }

            private DTR dtr = new DTR();
            private Force force = new Force();
            private Get get = new Get();
            private Power power = new Power();
            private SetCmd set = new SetCmd();
            private TNT tnt = new TNT();
            private Bypass bypass = new Bypass();
            private ChatSpy chatSpy = new ChatSpy();
            private Money money = new Money();
            private Reload reload = new Reload();
            private SaveAll saveAll = new SaveAll();
            @Comment("""
                    The Paper-only command to manage upgrades using dialogs.
                    Each section represents a dialog page.""")
            private Upgrades upgrades = new Upgrades();

            public DTR dtr() {
                return dtr;
            }

            public Get get() {
                return get;
            }

            public Force force() {
                return force;
            }

            public Power power() {
                return power;
            }

            public SetCmd set() {
                return set;
            }

            public TNT tnt() {
                return tnt;
            }

            public Bypass bypass() {
                return bypass;
            }

            public ChatSpy chatSpy() {
                return chatSpy;
            }

            public Money money() {
                return money;
            }

            public Reload reload() {
                return reload;
            }

            public SaveAll saveAll() {
                return saveAll;
            }

            public Upgrades upgrades() {
                return upgrades;
            }
        }

        public static class Chat extends AbsCommand {
            protected Chat() {
                super("Change chat mode", "chat");
            }

            private String modePublic = "<info>Public chat mode.";
            private String modeAlliance = "<info>Alliance only chat mode.";
            private String modeTruce = "<info>Truce only chat mode.";
            private String modeFaction = "<info>Faction only chat mode.";
            private String modeColeader = "<info>Coleader only chat mode.";
            private String modeMod = "<info>Mod only chat mode.";
            private String modeNormal = "<info>Normal member only chat mode.";

            private String allianceChatDescription = "Toggles whether or not you will see alliance chat";
            private String truceChatDescription = "Toggles whether or not you will see truce chat";
            private String allianceChatIgnore = "Alliance chat is now ignored";
            private String allianceChatUnignore = "Alliance chat is no longer ignored";
            private String truceChatIgnore = "Truce chat is now ignored";
            private String truceChatUnignore = "Truce chat is no longer ignored";

            public String getModePublic() {
                return modePublic;
            }

            public String getModeAlliance() {
                return modeAlliance;
            }

            public String getModeTruce() {
                return modeTruce;
            }

            public String getModeFaction() {
                return modeFaction;
            }

            public String getModeColeader() {
                return modeColeader;
            }

            public String getModeMod() {
                return modeMod;
            }

            public String getModeNormal() {
                return modeNormal;
            }

            public String getAllianceChatDescription() {
                return allianceChatDescription;
            }

            public String getTruceChatDescription() {
                return truceChatDescription;
            }

            public String getAllianceChatIgnore() {
                return allianceChatIgnore;
            }

            public String getAllianceChatUnignore() {
                return allianceChatUnignore;
            }

            public String getTruceChatIgnore() {
                return truceChatIgnore;
            }

            public String getTruceChatUnignore() {
                return truceChatUnignore;
            }
        }

        public static class Confirm extends AbsCommand {
            public Confirm() {
                super("Confirm an action", "confirm");
            }

            private String invalid = "<deny>Invalid confirmation code.";
            private String notFound = "<deny>No confirmation found.";

            public String getInvalid() {
                return invalid;
            }

            public String getNotFound() {
                return notFound;
            }
        }

        public static class Disband extends AbsCommand {
            public Disband() {
                super("Disband a faction", "disband");
            }

            private String deniedSpecial = "<deny>You cannot disband this faction.";
            private String deniedPermanent = "<deny>You cannot disband this permanent faction.";
            @Comment("Supports <faction>, <command>")
            private String confirm = "<info>Are you sure you want to disband <faction>? If so, run: /<command>";
            @Comment("Supports <player>")
            private String broadcastYours = "<info><player> disbanded your faction.";
            @Comment("Supports <player>, <faction>")
            private String broadcastNotYours = "<info><player> disbanded <faction>.";
            private String broadcastConsoleYours = "<info>Your faction was disbanded.";
            @Comment("Supports <faction>")
            private String broadcastConsoleNotYours = "<info><faction> was disbanded.";
            @Comment("Supports <amount>")
            private String econHoldings = "<info>You have been given the disbanded faction's bank, totaling <amount>.";

            public String getDeniedSpecial() {
                return deniedSpecial;
            }

            public String getDeniedPermanent() {
                return deniedPermanent;
            }

            public String getConfirm() {
                return confirm;
            }

            public String getBroadcastYours() {
                return broadcastYours;
            }

            public String getBroadcastNotYours() {
                return broadcastNotYours;
            }

            public String getBroadcastConsoleYours() {
                return broadcastConsoleYours;
            }

            public String getBroadcastConsoleNotYours() {
                return broadcastConsoleNotYours;
            }

            public String getEconHoldings() {
                return econHoldings;
            }
        }

        public static class Help extends AbsCommand {
            public Help() {
                super("Command help.", "help");
            }
        }

        public static class Join extends AbsCommand {
            public Join() {
                super("Join a faction", "join");
            }

            private String deniedSpecial = "<deny>Players cannot join this faction.";
            private String deniedAlreadyHaveFaction = "<deny>You must leave your current faction first.";
            @Comment("Supports <faction>")
            private String deniedAlreadyMember = "<deny>You are already a member of <faction>.";
            @Comment("Supports <faction>, <limit>")
            private String deniedMaxMembers = "<deny>The faction <faction> is at the limit of <limit> members.";
            @Comment("Supports <faction>")
            private String deniedRequiresInvite = "<deny>An invitation is required to join <faction>.";
            @Comment("Supports <player>")
            private String deniedRequiresInviteNotice = "<info><player> tried to join your faction without an invitation.";
            @Comment("Supports <faction>")
            private String deniedBanned = "<deny>You are banned from <faction>.";
            @Comment("Supports <faction>")
            private String success = "<info>You joined <faction>.";
            @Comment("Supports <player>")
            private String successNotice = "<info><player> joined your faction.";

            public String getDeniedSpecial() {
                return deniedSpecial;
            }

            public String getDeniedAlreadyHaveFaction() {
                return deniedAlreadyHaveFaction;
            }

            public String getDeniedAlreadyMember() {
                return deniedAlreadyMember;
            }

            public String getDeniedMaxMembers() {
                return deniedMaxMembers;
            }

            public String getDeniedRequiresInvite() {
                return deniedRequiresInvite;
            }

            public String getDeniedRequiresInviteNotice() {
                return deniedRequiresInviteNotice;
            }

            public String getDeniedBanned() {
                return deniedBanned;
            }

            public String getSuccess() {
                return success;
            }

            public String getSuccessNotice() {
                return successNotice;
            }

            @Comment("Supports <player>")
            private String negativePower = "<deny><player> cannot join a faction with a negative power level.";

            public String getNegativePower() {
                return negativePower;
            }
        }

        public static class Link extends AbsCommand {
            public Link() {
                super("Change the faction link", "link");
            }

            @Comment("Supports <faction>, <url>")
            private String show = "<info><faction> link: <url>";
            private String invalidUrl = "<deny>Invalid URL!";
            @Comment("Supports <player>")
            private String changed = "<info><player> changed their link to:";
            private String youMustBeModerator = "<deny>You must be <denyfocus>Moderator</denyfocus>.";

            public String getShow() {
                return show;
            }

            public String getInvalidUrl() {
                return invalidUrl;
            }

            public String getChanged() {
                return changed;
            }

            public String getYouMustBeModerator() {
                return youMustBeModerator;
            }
        }

        public static class ListCmd extends AbsCommand {
            public ListCmd() {
                super("List faction information", "list");
            }

            public static class ListBans extends AbsCommand {
                public ListBans() {
                    super("View faction bans", "bans");
                }

                private String noFaction = "<deny>You are not in a faction.";
                @Comment("Supports <count>, <faction>")
                private String header = "<info>There are <negative><count></negative> bans for <faction>.";
                @Comment("Supports <index>, <player>, <banner>, <date>")
                private String entry = "<gray><index>. <negative><player:name> </negative>- <positive><banner:name> </positive>- <info><date>";

                public String getNoFaction() {
                    return noFaction;
                }

                public String getHeader() {
                    return header;
                }

                public String getEntry() {
                    return entry;
                }
            }

            public static class ListClaims extends AbsCommand {
                public ListClaims() {
                    super("View faction claims", "claims");
                }

                @Comment("Supports <world>")
                private String invalidWorld = "<deny>Invalid world name <denyfocus><world></denyfocus>.";
                @Comment("Supports <faction>, <world>")
                private String noClaims = "<deny>No claims by <faction> in world <denyfocus><world><denyfocus>.";
                @Comment("Supports <faction>, <world>")
                private String message = "<info>Claims by <faction> in <world>:";

                public String getInvalidWorld() {
                    return invalidWorld;
                }

                public String getNoClaims() {
                    return noClaims;
                }

                public String getMessage() {
                    return message;
                }
            }

            public static class ListFactions extends AbsCommand {
                @Comment("You can also use <page_current> and <page_count> in the header.\n" +
                        "Blank entry results in nothing being displayed.")
                private String header = "<fuuid:title>Faction List <focus><page_current></focus>/<focus><page_count></focus>";
                @Comment("You can also use <page_current> and <page_count> in the header.\n" +
                        "Blank entry results in nothing being displayed.")
                private String footer = "";
                @Comment("You can use per-faction <faction:thing> placeholders here")
                private String factionlessEntry = "<info>Factionless <faction:members_online_count> online";
                @Comment("You can use per-faction <faction:thing> placeholders here")
                private String entry = "<info><faction> <faction:members_online_count>/<faction:members_total_count> online, Land/Power/Max Power: <focus><faction:claims_count></focus>/<focus><faction:power></focus>/<focus><faction:power_max>";

                public String getHeader() {
                    return header;
                }

                public String getFooter() {
                    return footer;
                }

                public String getFactionlessEntry() {
                    return factionlessEntry;
                }

                public String getEntry() {
                    return entry;
                }

                public ListFactions() {
                    super("List factions", "factions");
                }
            }

            public static class ListInvites extends AbsCommand {
                public ListInvites() {
                    super("List pending faction invites", "invites");
                }

                private String pending = "Players with pending invites: ";
                @Comment("Supports <player>")
                private String clickToRevoke = "Click to revoke invite for <player>";

                public String getPending() {
                    return pending;
                }

                public String getClickToRevoke() {
                    return clickToRevoke;
                }
            }

            private ListBans bans = new ListBans();
            private ListClaims claims = new ListClaims();
            private ListFactions factions = new ListFactions();
            private ListInvites invites = new ListInvites();

            public ListBans bans() {
                return bans;
            }

            public ListClaims claims() {
                return claims;
            }

            public ListFactions factions() {
                return factions;
            }

            public ListInvites invites() {
                return invites;
            }
        }

        public static class MapCmd extends AbsCommand {
            public MapCmd() {
                super("Show the territory map, and set optional auto update", "map");
            }

            private String compassLetterNorth = "N";
            private String compassLetterSouth = "S";
            private String compassLetterEast = "E";
            private String compassLetterWest = "W";
            @Comment("Takes color names or hex (with # in front, e.g. #abcdef)")
            private String compassColorDefault = "golden";
            @WipeOnReload
            private transient TextColor compassColorDefaultColor;
            @Comment("Takes color names or hex (with # in front, e.g. #abcdef)")
            private String compassColorActive = "info";
            @WipeOnReload
            private transient TextColor compassColorActiveColor;

            public String getCompassLetterNorth() {
                return compassLetterNorth;
            }

            public String getCompassLetterSouth() {
                return compassLetterSouth;
            }

            public String getCompassLetterEast() {
                return compassLetterEast;
            }

            public String getCompassLetterWest() {
                return compassLetterWest;
            }

            public TextColor getCompassColorActive() {
                return compassColorActiveColor = MainConfig.getColor(compassColorActive, compassColorActiveColor, TextColor.fromHexString("#85d2e7"));
            }

            public TextColor getCompassColorDefault() {
                return compassColorDefaultColor = MainConfig.getColor(compassColorDefault, compassColorDefaultColor, TextColor.fromHexString("#e6c36c"));
            }

            @Comment("Supports <lines>")
            private String heightSet = "<info>Set /f map lines to <focus><lines></focus>.";
            private String updateEnabled = "<info>Map auto update <positive>enabled</positive>.";
            private String updateDisabled = "<info>Map auto update <negative>disabled</negative>.";

            public String getHeightSet() {
                return heightSet;
            }

            public String getUpdateEnabled() {
                return updateEnabled;
            }

            public String getUpdateDisabled() {
                return updateDisabled;
            }

            @Comment("Supports <x>, <z>, <faction>")
            private String mapOutputTitle = "(<x>, <z>) <faction>";
            @Comment("Characters picked from, in order, for faction labeling. Loops back to beginning if more factions than options.")
            private String mapOutputFactionCharacters = "\\/#$%=&^ABCDEFGHJKLMNOPQRSTUVWXYZ1234567890abcdeghjmnopqrsuvwxyz?";
            @Comment("Single character, with formatting, for wilderness chunks.")
            private String mapOutputWilderness = "<fuuid:color:wilderness>-";
            @Comment("Single character, with formatting, for warzone chunks.")
            private String mapOutputWarzone = "<fuuid:color:warzone>+";
            @Comment("Single character, with formatting, for safezone chunks.")
            private String mapOutputSafezone = "<fuuid:color:safezone>+";
            @Comment("""
                    Single character, with formatting, for any faction chunks deliberately not identified.
                    By default, matches the wilderness setup, but you could choose another format""")
            private String mapOutputOther = "<fuuid:color:wilderness>-";
            @Comment("Single entry for the key. Supports <label>, <faction>")
            private String mapOutputKeyItem = "<label>: <faction>";

            public String getMapOutputTitle() {
                return mapOutputTitle;
            }

            public String getMapOutputFactionCharacters() {
                return mapOutputFactionCharacters;
            }

            public String getMapOutputKeyItem() {
                return mapOutputKeyItem;
            }

            public String getMapOutputOther() {
                return mapOutputOther;
            }

            public String getMapOutputSafezone() {
                return mapOutputSafezone;
            }

            public String getMapOutputWarzone() {
                return mapOutputWarzone;
            }

            public String getMapOutputWilderness() {
                return mapOutputWilderness;
            }
        }

        public static class Near extends AbsCommand {
            public Near() {
                super("Show nearby faction members", "near");
            }

            @Comment("Supports <player>, <distance>")
            private String perPlayer = "<player> <focus>(<distance>)</focus>";
            private String startOfLine = "<info>Near: </info>";
            private String noneNearby = "<info>No faction members nearby.";

            public String getPerPlayer() {
                return perPlayer;
            }

            public String getStartOfLine() {
                return startOfLine;
            }

            public String getNoneNearby() {
                return noneNearby;
            }
        }

        public static class Role extends AbsCommand {
            public Role() {
                super("Modify a faction member's role.", "role");
            }

            private String wrongFaction = "<deny>That player is not part of your faction.";
            private String notAllowed = "<deny>You can't change that player's role.";
            private String alreadyColeader = "The faction already has a coleader. There can only be 1.";
            @Comment("Supports <player>, <role>")
            private String updated = "<info>Member <player> role updated to <role>.";
            private String notAdmin = "<deny>You are not the faction admin.";
            private String targetSelf = "<deny>The target player mustn't be yourself.";
            @Comment("Supports <player>")
            private String notMember = "<deny><player> is not a member in your faction.";
            @Comment("Supports <player>, <target>, <faction>")
            private String promoted = "<info><player> gave <target><info> the leadership of <faction>.";

            public String getWrongFaction() {
                return wrongFaction;
            }

            public String getNotAllowed() {
                return notAllowed;
            }

            public String getAlreadyColeader() {
                return alreadyColeader;
            }

            public String getUpdated() {
                return updated;
            }

            public String getNotAdmin() {
                return notAdmin;
            }

            public String getTargetSelf() {
                return targetSelf;
            }

            public String getNotMember() {
                return notMember;
            }

            public String getPromoted() {
                return promoted;
            }
        }

        public static class SetCmd extends AbsCommand {
            public SetCmd() {
                super("Set faction info", "set");
            }

            public static class SetBoom extends AbsCommand {
                public SetBoom() {
                    super("Toggle explosions (peaceful factions only)", "boom");
                }

                private String peacefulOnly = "<deny>This command is only usable by factions which are specifically designated as peaceful.";
                @Comment("Supports <player>, <state>")
                private String enabled = "<info><player> has <state> explosions in your faction's territory.";

                public String getPeacefulOnly() {
                    return peacefulOnly;
                }

                public String getEnabled() {
                    return enabled;
                }
            }

            public static class SetDefaultRole extends AbsCommand {
                public SetDefaultRole() {
                    super("/f defaultrole <role> - set your Faction's default role.", "defaultrole");
                }

                @Comment("Supports <role>")
                private String invalidRole = "<deny>Couldn't find matching role for <denyfocus><role></denyfocus>.";
                private String notThatRole = "<deny>You cannot set the default to admin.";
                @Comment("Supports <role>")
                private String success = "<info>Set default role of your faction to <role>.";

                public String getInvalidRole() {
                    return invalidRole;
                }

                public String getNotThatRole() {
                    return notThatRole;
                }

                public String getSuccess() {
                    return success;
                }
            }

            public static class SetDescription extends AbsCommand {
                public SetDescription() {
                    super("Change the faction description", "description");
                }

                @Comment("Supports <limit>")
                private String toolong = "<deny>Description too long! Limit is <limit> characters.";
                @Comment("Supports <faction>")
                private String changed = "<info>The faction <faction> changed their description to:";
                @Comment("Supports <faction>")
                private String changes = "<info>You have changed the description for <faction> to:";

                public String getToolong() {
                    return toolong;
                }

                public String getChanged() {
                    return changed;
                }

                public String getChanges() {
                    return changes;
                }
            }

            public static class SetHome extends AbsCommand {
                public SetHome() {
                    super("Set the faction home", "home");
                }

                private String notClaimed = "<deny>Your faction home can only be set inside your own claimed territory.";
                @Comment("Supports <player>")
                private String set = "<info><player> set the home for your faction. You can now use:";
                private String noHome = "<negative>Your faction does not have a home.";
                private String warpsRemain = "<deny>Cannot delete home while the faction has warps!";
                @Comment("Supports <player>")
                private String del = "<info><player> unset the home for your faction.";

                public String getNotClaimed() {
                    return notClaimed;
                }

                public String getSet() {
                    return set;
                }

                public String getNoHome() {
                    return noHome;
                }

                public String getWarpsRemain() {
                    return warpsRemain;
                }

                public String getDel() {
                    return del;
                }
            }

            public static class SetOpen extends AbsCommand {
                public SetOpen() {
                    super("Switch if invitation is required to join", "open");
                }

                private String open = "open";
                private String closed = "closed";
                @Comment("Supports <player>, <state>")
                private String changes = "<info><player> changed the faction to <focus><state></focus>.";
                @Comment("Supports <faction>, <state>")
                private String changed = "<info>The faction <faction> is now <focus><state></focus>.";

                public String getOpen() {
                    return open;
                }

                public String getClosed() {
                    return closed;
                }

                public String getChanges() {
                    return changes;
                }

                public String getChanged() {
                    return changed;
                }
            }

            public static class SetTag extends AbsCommand {
                public SetTag() {
                    super("Change the faction tag", "tag");
                }

                private String taken = "<deny>That tag is already taken.";
                @Comment("Supports <player>, <faction>")
                private String factionMsg = "<info><player> changed your faction tag to <faction>.";
                @Comment("Supports <oldtag>, <faction>")
                private String changed = "<info>The faction <focus><oldtag></focus> changed their name to <faction>.";

                public String getTaken() {
                    return taken;
                }

                public String getFactionMsg() {
                    return factionMsg;
                }

                public String getChanged() {
                    return changed;
                }
            }

            public static class SetTitleCmd extends AbsCommand {
                public SetTitleCmd() {
                    super("Set or remove a player's title", "title");
                }

                private String cannotChange = "<deny>Cannot change this player's title.";
                @Comment("Supports <sender>, <target>")
                private String changed = "<info><sender> changed a title: <target>.";
                @Comment("Supports <limit>")
                private String limit = "<deny>Titles cannot be longer than <limit> characters.";

                public String getCannotChange() {
                    return cannotChange;
                }

                public String getChanged() {
                    return changed;
                }

                public String getLimit() {
                    return limit;
                }
            }

            public static class SetWarp extends AbsCommand {
                public SetWarp() {
                    super("Set a faction warp", "warp");
                }

                private String notClaimed = "<info>You can only set warps in your faction territory.";
                @Comment("Supports <max>")
                private String limit = "<info>Your Faction already has the max amount of warps set (<focus><max></focus>).";
                @Comment("Supports <warp>, <password>")
                private String set = "<info>Set warp <focus><warp></focus> and password <focus>'<password>'</focus> to your location.";
                private String homeRequired = "<deny>Cannot create warps until a home is set!";
                private String delWarpDescription = "Delete a faction warp";
                @Comment("Supports <warp>")
                private String deleted = "<info>Deleted warp <focus><warp></focus>.";
                @Comment("Supports <warp>")
                private String deleteNotFound = "<info>Couldn't find warp <focus><warp></focus>.";

                public String getNotClaimed() {
                    return notClaimed;
                }

                public String getLimit() {
                    return limit;
                }

                public String getSet() {
                    return set;
                }

                public String getHomeRequired() {
                    return homeRequired;
                }

                public String getDelWarpDescription() {
                    return delWarpDescription;
                }

                public String getDeleted() {
                    return deleted;
                }

                public String getDeleteNotFound() {
                    return deleteNotFound;
                }
            }

            public static class SetWarpProperty extends AbsCommand {
                public SetWarpProperty() {
                    super("Set a faction warp property", "warp-property");
                }

                @Comment("Supports <warp>")
                private String noWarp = "<deny>No warp found with name <denyfocus><warp></denyfocus>.";
                @Comment("Supports <warp>")
                private String removePassword = "<info>Password removed for warp <denyfocus><warp></denyfocus>.";
                @Comment("Supports <warp>")
                private String setPassword = "<info>Password set for warp <denyfocus><warp></denyfocus>.";

                public String getNoWarp() {
                    return noWarp;
                }

                public String getRemovePassword() {
                    return removePassword;
                }

                public String getSetPassword() {
                    return setPassword;
                }
            }

            private SetBoom setBoom = new SetBoom();
            private SetDefaultRole setDefaultRole = new SetDefaultRole();
            private SetDescription setDescription = new SetDescription();
            private SetHome setHome = new SetHome();
            private SetOpen setOpen = new SetOpen();
            private SetTag setTag = new SetTag();
            private SetTitleCmd title = new SetTitleCmd();
            private SetWarp setWarp = new SetWarp();
            private SetWarpProperty setWarpProperty = new SetWarpProperty();

            public SetBoom boom() {
                return setBoom;
            }

            public SetDefaultRole defaultRole() {
                return setDefaultRole;
            }

            public SetDescription description() {
                return setDescription;
            }

            public SetHome home() {
                return setHome;
            }

            public SetOpen open() {
                return setOpen;
            }

            public SetTag tag() {
                return setTag;
            }

            public SetTitleCmd title() {
                return title;
            }

            public SetWarp warp() {
                return setWarp;
            }

            public SetWarpProperty warpProperty() {
                return setWarpProperty;
            }
        }

        public static class Show extends AbsCommand {
            protected Show() {
                super("Show faction information", "show");
            }

            @Comment("Supports <faction> and per-faction <faction:thing> placeholders")
            private List<String> normalFormat = new ArrayList<>() {
                {
                    this.add("<fuuid:title><faction>");
                    this.add("<info>Description: <focus><faction:description>");
                    this.add("<info><faction:if_open>No invitation required</faction:if_open><faction:if_open:else>Invitation required</faction:if_open:else><faction:if_peaceful>.    <fuuid:color:peaceful>Peaceful");
                    this.add("<info>Land / Power / Max Power: <focus><faction:claims_count></focus> / <focus><faction:power></focus> / <focus><faction:power_max>");
                    this.add("<info>Raidable: <faction:if_raidable><positive>Yes</faction:if_raidable><faction:if_raidable:else><negative>No");
                    this.add("<info>Founded: <focus><faction:creation_date>");
                    this.add("<faction:if_permanent><focus>This faction is permanent, remaining even with no members.");
                    this.add("<fuuid:if_economy><info><fuuid:if_banks>Balance: <focus><faction:bank_balance></focus></fuuid:if_banks>     Land value: <focus><faction:claims_value>");
                    this.add("<faction:if_allies><info>Allies (<focus><faction:allies_count></focus>/<focus><faction:allies_max></focus>): {allies-list}");
                    this.add("<faction:if_online><info>Online: (<focus><faction:members_online_count></focus>/<focus><faction:members_total_count></focus>)<faction:if_online>: {online-list}");
                    this.add("<faction:if_offline><info>Offline: (<focus><faction:members_offline_count></focus>/<focus><faction:members_total_count></focus>)<faction:if_offline>: {offline-list}");
                }
            };

            private List<String> safezoneFormat = new ArrayList<>() {
                {
                    this.add("<info>The <fuuid:color:safezone>safezone</fuuid:color:safezone> faction is a special faction that 'owns' territory where players cannot be hurt");
                }
            };

            private List<String> warzoneFormat = new ArrayList<>() {
                {
                    this.add("<info>The <fuuid:color:warzone>warzone</fuuid:color:warzone> faction is a special faction that 'owns' territory where players can be hurt");
                }
            };

            public List<String> getNormalFormat() {
                return Collections.unmodifiableList(normalFormat);
            }

            public List<String> getSafezoneFormat() {
                return Collections.unmodifiableList(safezoneFormat);
            }

            public List<String> getWarzoneFormat() {
                return Collections.unmodifiableList(warzoneFormat);
            }

            private String noFactionOther = "<deny>That's not a faction.";
            private String exempt = "<deny>This faction cannot be seen.";

            public String getNoFactionOther() {
                return noFactionOther;
            }

            public String getExempt() {
                return exempt;
            }
        }

        public static class Permissions extends AbsCommand {
            public static class SubCmdAdd extends AbsCommand {
                public SubCmdAdd() {
                    super("Unused description", "add");
                }

                private String availableSelectorsIntro = "Available: ";
                @Comment("Supports <command>, <selector>")
                private String availableSelectorsSelector = "<click:run_command:\"<command>\"><color:#66ebff><selector></color:#66ebff></click>  ";

                private String selectorNotFound = "<negative>No selector available with that name.";
                @Comment("Supports <error>")
                private String selectorCreateFail = "<deny>Could not create selector:</deny> <error>";
                private String selectorOptionHere = "OPTIONHERE";
                private String selectorOptionsIntro = "Available: ";
                @Comment("Supports <command>, <display>")
                private String selectorOptionsItem = "<click:run_command:\"<command>\"><color:#66ebff><display></color:#66ebff></click>  ";

                private String actionOptionsIntro = "Available: ";
                @Comment("Supports <description>, <action>, <commandtrue>, <commandfalse>")
                private String actionOptionsItem = "<hover:show_text:\"<description>\"><action></hover>" +
                        "<click:run_command:\"<commandtrue>\"><color:#66ffb0>+</color:#66ffb0></click>" +
                        "<click:run_command:\"<commandfalse>\"><color:#ff6666>-</color:#ff6666></click>  ";
                private String actionNotFound = "<deny>No action available with that name.";
                @Comment("Supports <allow>, <deny>")
                private String actionAllowDenyOptions = "<deny>Unrecognized choice. What about </deny><allow> <deny>or</deny> <deny><deny>?</deny>";

                private List<String> actionAllowAlias = new ArrayList<>() {
                    {
                        this.add("allow");
                        this.add("true");
                    }
                };

                private List<String> actionDenyAlias = new ArrayList<>() {
                    {
                        this.add("deny");
                        this.add("false");
                        this.add("disallow");
                    }
                };

                public String getAvailableSelectorsIntro() {
                    return availableSelectorsIntro;
                }

                public String getAvailableSelectorsSelector() {
                    return availableSelectorsSelector;
                }

                public String getSelectorCreateFail() {
                    return selectorCreateFail;
                }

                public String getSelectorNotFound() {
                    return selectorNotFound;
                }

                public String getSelectorOptionsIntro() {
                    return selectorOptionsIntro;
                }

                public String getSelectorOptionsItem() {
                    return selectorOptionsItem;
                }

                public String getSelectorOptionHere() {
                    return selectorOptionHere;
                }

                public String getActionOptionsIntro() {
                    return actionOptionsIntro;
                }

                public String getActionOptionsItem() {
                    return actionOptionsItem;
                }

                public String getActionNotFound() {
                    return actionNotFound;
                }

                public String getActionAllowDenyOptions() {
                    return actionAllowDenyOptions;
                }

                public List<String> getActionAllowAlias() {
                    return actionAllowAlias;
                }

                public List<String> getActionDenyAlias() {
                    return actionDenyAlias;
                }
            }

            public static class SubCmdList extends AbsCommand {
                @Comment("Supports <commandadd>")
                private String footer = "           <click:run_command:\"<commandadd>\"><color:#66ebff>[Add]";
                @Comment("Supports <commandoverride>")
                private String header = "Selectors (ranked by priority) [<click:run_command:\"<commandoverride>\"><color:#66ebff>Overrides</color:#66ebff></click>]:";

                @Comment("Supports <commandmoveup>, <commandmovedown>, <commandremove>, <rownumber>, <commandshow>, <name>, <value>")
                private String item = "<hover:show_text:\"<color:#ff6666>Move up\"><click:run_command:\"<commandmoveup>\">^</click></hover> " +
                        "<hover:show_text:\"<color:#ff6666>Move down\"><click:run_command:\"<commandmovedown>\">V</click></hover> " +
                        "<hover:show_text:\"<color:#ff6666>Remove\"><click:run_command:\"<commandremove>\"><color:#ff6666>X</color:#ff6666></click></hover> " +
                        "#<rownumber> <hover:show_text:\"<color:#ff6666>Click to show actions\"><click:run_command:\"<commandshow>\"><color:#66ebff><name></color:#66ebff></click></hover>: " +
                        "<color:#66ffb0><value>";

                public SubCmdList() {
                    super("Unused description", "list");
                }

                public String getFooter() {
                    return footer;
                }

                public String getHeader() {
                    return header;
                }

                public String getItem() {
                    return item;
                }
            }

            public static class SubCmdListOverride extends AbsCommand {
                private String footer = "";
                private String header = "Server Override Selectors (ranked by priority):";

                @Comment("Supports <rownumber>, <commandshow>, <name>, <value>")
                private String item = "#<rownumber> <hover:show_text:\"<color:#ff6666>Click to show actions\"><click:run_command:\"<commandshow>\"><color:#66ebff><name></color:#66ebff></click></hover>: " +
                        "<color:#66ffb0><value>";

                public SubCmdListOverride() {
                    super("Unused description", "listoverride");
                }

                public String getFooter() {
                    return footer;
                }

                public String getHeader() {
                    return header;
                }

                public String getItem() {
                    return item;
                }
            }

            public static class SubCmdMove extends AbsCommand {
                private List<String> aliasUp = new ArrayList<>() {
                    {
                        this.add("up");
                    }
                };
                private List<String> aliasDown = new ArrayList<>() {
                    {
                        this.add("down");
                    }
                };
                @Comment("Supports <up>, <down>")
                private String errorOptions = "<deny>Unrecognized choice. What about </deny><up> <deny>or</deny> <down><deny>?</deny>";
                private String errorHighest = "<deny>Cannot move highest selector any higher!</deny>";
                private String errorLowest = "<deny>Cannot move lowest selector any lower!</deny>";
                private String errorInvalidPositon = "<deny>Cannot move invalid position!</deny>";

                public SubCmdMove() {
                    super("Unused description", "move");
                }

                public List<String> getAliasUp() {
                    return aliasUp;
                }

                public List<String> getAliasDown() {
                    return aliasDown;
                }

                public String getErrorOptions() {
                    return errorOptions;
                }

                public String getErrorHighest() {
                    return errorHighest;
                }

                public String getErrorLowest() {
                    return errorLowest;
                }

                public String getErrorInvalidPosition() {
                    return errorInvalidPositon;
                }
            }

            public static class SubCmdRemove extends AbsCommand {
                public SubCmdRemove() {
                    super("Unused description", "remove");
                }
            }

            public static class SubCmdReset extends AbsCommand {
                @Comment("Supports <command>")
                private String warning = "<negative>Warning:</negative><info> Are you sure you wish to reset all permissions? <click:run_command:\"<command>\"><negative>[CONFIRM]</negative></click>";
                private String resetComplete = "<info>Permissions reset!";
                @Comment("must be a single word, to confirm resetting")
                private String confirmWord = "confirm";

                public SubCmdReset() {
                    super("Unused description", "reset");
                }

                public String getConfirmWord() {
                    return confirmWord;
                }

                public String getWarning() {
                    return warning;
                }

                public String getResetComplete() {
                    return resetComplete;
                }
            }

            public static class SubCmdShow extends AbsCommand {
                @Comment("Supports <rownumber>, <name>, <value>")
                private String header = "#<rownumber> <color:#66ebff><name></color:#66ebff>: <color:#66ffb0><value>";
                @Comment("Supports <command>")
                private String footer = "   <click:run_command:\"<command>\"><color:#66ebff>[Add]";
                @Comment("Supports <commandremove>, <desc>, <shortdesc>, <state>")
                private String item = "<hover:show_text:\"<color:#ff6666>Remove\"><click:run_command:\"<commandremove>\"><color:#ff6666>X</color:#ff6666></click></hover> " +
                        "<hover:show_text:\"<desc>\"><color:#66ebff><shortdesc></color:#66ebff></hover>: <color:#66ffb0><state>";

                private String selectorNotFound = "<deny>No selector available with that name.";

                public SubCmdShow() {
                    super("Unused description", "show");
                }

                public String getHeader() {
                    return header;
                }

                public String getFooter() {
                    return footer;
                }

                public String getItem() {
                    return item;
                }

                public String getSelectorNotFound() {
                    return selectorNotFound;
                }
            }

            public static class SubCmdShowOverride extends AbsCommand {
                @Comment("Supports <rownumber>, <name>, <value>")
                private String header = "#<rownumber> <color:#66ebff><name></color:#66ebff>:<color:#66ffb0><value>";
                private String footer = "";
                @Comment("Supports <desc>, <shortdesc>, <state>")
                private String item = "<hover:show_text:\"<desc>\"><color:#66ebff><shortdesc></color:#66ebff></hover>:<color:#66ffb0><state>";

                private String selectorNotFound = "<deny>No override selector available with that name.";

                public SubCmdShowOverride() {
                    super("Unused description", "showoverride");
                }

                public String getHeader() {
                    return header;
                }

                public String getFooter() {
                    return footer;
                }

                public String getItem() {
                    return item;
                }

                public String getSelectorNotFound() {
                    return selectorNotFound;
                }
            }

            protected Permissions() {
                super("Your faction's permissions", "perms");
            }

            private SubCmdAdd add = new SubCmdAdd();
            private SubCmdList list = new SubCmdList();
            private SubCmdListOverride listOverride = new SubCmdListOverride();
            private SubCmdMove move = new SubCmdMove();
            private SubCmdRemove remove = new SubCmdRemove();
            private SubCmdReset reset = new SubCmdReset();
            private SubCmdShow show = new SubCmdShow();
            private SubCmdShowOverride showOverride = new SubCmdShowOverride();

            public SubCmdAdd add() {
                return add;
            }

            public SubCmdList list() {
                return list;
            }

            public SubCmdListOverride listOverride() {
                return listOverride;
            }

            public SubCmdMove move() {
                return move;
            }

            public SubCmdRemove remove() {
                return remove;
            }

            public SubCmdReset reset() {
                return reset;
            }

            public SubCmdShow show() {
                return show;
            }

            public SubCmdShowOverride showOverride() {
                return showOverride;
            }
        }

        public static class Relation extends AbsCommand {
            protected Relation() {
                super("Set relation wish to another faction", "relation");
            }

            private String commandAlly = "ally";
            private String commandTruce = "truce";
            private String commandEnemy = "enemy";
            private String commandNeutral = "neutral";
            private String denySystemFaction = "<deny>You cannot set a relation with this faction!";
            private String denySelfFaction = "<deny>You cannot set a relation with your own faction!";
            @Comment("Supports <faction>")
            private String denyAlreadySet = "<deny>Your faction already wishes this relation with <faction>.";
            @Comment("Supports <relation>, <faction>")
            private String updated = "<info>Your faction is now <relation> to <faction>.";
            @Comment("Supports <faction>, <relation>, <command>")
            private String proposal = "<info><faction> wishes to be your <relation>. To accept, run or click <focus><click:run_command:\"/<command>\">/<command></click></focus>.";
            @Comment("Supports <faction>, <relation>")
            private String proposalSent = "<info><faction> were informed that you wish to be <relation>.";
            private String peacefulSelf = "<info>This will have little effect while your faction is peaceful.";
            private String peacefulThem = "<info>This will have little effect while their faction is peaceful.";
            @Comment("Supports <limit>, <relation>")
            private String relationLimitSelf = "<deny>Cannot set relation wish because your faction cannot have more than <limit> <relation>.";
            @Comment("Supports <limit>, <relation>")
            private String relationLimitThem = "<deny>Cannot set relation wish because their faction cannot have more than <limit> <relation>.";

            public String getCommandAlly() {
                return commandAlly;
            }

            public String getCommandTruce() {
                return commandTruce;
            }

            public String getCommandEnemy() {
                return commandEnemy;
            }

            public String getCommandNeutral() {
                return commandNeutral;
            }

            public String getDenySystemFaction() {
                return denySystemFaction;
            }

            public String getDenySelfFaction() {
                return denySelfFaction;
            }

            public String getDenyAlreadySet() {
                return denyAlreadySet;
            }

            public String getPeacefulSelf() {
                return peacefulSelf;
            }

            public String getPeacefulThem() {
                return peacefulThem;
            }

            public String getProposal() {
                return proposal;
            }

            public String getProposalSent() {
                return proposalSent;
            }

            public String getRelationLimitSelf() {
                return relationLimitSelf;
            }

            public String getRelationLimitThem() {
                return relationLimitThem;
            }

            public String getUpdated() {
                return updated;
            }
        }

        public static class Shield extends AbsCommand {
            protected Shield() {
                super("View and manage shields", "shield");
            }

            private String commandActivate = "activate";
            private String commandSchedule = "schedule";
            private String commandStatus = "status";
            @Comment("Supports <duration>")
            private String statusActive = "<info>Shield active! No explosions for <duration>.";
            @Comment("Supports <cooldown>")
            private String statusCooldown = "<negative>Shield on cooldown for <cooldown>.";
            @Comment("Supports <duration>, <cooldown>")
            private String statusAvailable = "<info>Shield available! Duration: <duration>. Cooldown: <cooldown>.";
            private String statusNotActive = "<info>Shield is not active.";
            @Comment("Supports <player>, <duration>")
            private String activated = "<info>Shield activated by <player>! No explosions for <duration>.";
            @Comment("Supports <duration>")
            private String activatedScheduled = "<info>Scheduled shield activated! No explosions for <duration>.";

            private String scheduleMenuSetTitle = "<titling>Schedule Shield";
            private List<String> scheduleMenuSetBody = new ArrayList<>() {
                {
                    this.add("Choose the daily start time of your shield!");
                    this.add("Current time according to the server: <currenttime>");
                }
            };
            private String scheduleMenuSetButtonCancel = "Cancel";
            @Comment("Supports <time>")
            private String scheduleMenuSetButtonTime = "<time>";

            private int scheduleMenuSetColumns = 6;
            private int scheduleMenuSetWidth = 50;
            private String scheduleMenuTimeFormat = "h:mm a";
            @WipeOnReload
            private transient @Nullable DateTimeFormatter scheduleMenuTimeFormatter;

            private String scheduleMenuStatusTitle = "<titling>Schedule Shield";
            private List<String> scheduleMenuStatusBodyNotSet = new ArrayList<>() {
                {
                    this.add("No schedule currently set");
                }
            };
            @Comment("Supports <scheduledtime>, <currenttime>")
            private List<String> scheduleMenuStatusBodyCurrentlySet = new ArrayList<>() {
                {
                    this.add("Your shield is scheduled to start, when not on cooldown, at <scheduledtime>");
                    this.add("Current time according to the server: <currenttime>");
                }
            };
            private String scheduleMenuStatusButtonSetSchedule = "Set schedule";
            private String scheduleMenuStatusButtonClearSchedule = "Clear schedule";
            private String scheduleMenuStatusButtonDone = "Done";
            private int scheduleMenuStatusColumns = 1;

            public String getActivated() {
                return activated;
            }

            public String getActivatedScheduled() {
                return activatedScheduled;
            }

            public String getCommandActivate() {
                return commandActivate;
            }

            public String getCommandSchedule() {
                return commandSchedule;
            }

            public String getCommandStatus() {
                return commandStatus;
            }

            public String getStatusActive() {
                return statusActive;
            }

            public String getStatusAvailable() {
                return statusAvailable;
            }

            public String getStatusCooldown() {
                return statusCooldown;
            }

            public String getStatusNotActive() {
                return statusNotActive;
            }

            public String getScheduleMenuSetTitle() {
                return scheduleMenuSetTitle;
            }

            public List<String> getScheduleMenuSetBody() {
                return scheduleMenuSetBody;
            }

            public String getScheduleMenuSetButtonCancel() {
                return scheduleMenuSetButtonCancel;
            }

            public int getScheduleMenuSetColumns() {
                return scheduleMenuSetColumns;
            }

            public int getScheduleMenuSetWidth() {
                return scheduleMenuSetWidth;
            }

            public String getScheduleMenuSetButtonTime() {
                return scheduleMenuSetButtonTime;
            }

            public DateTimeFormatter getScheduleMenuTimeFormat() {
                if (this.scheduleMenuTimeFormatter == null) {
                    try {
                        this.scheduleMenuTimeFormatter = DateTimeFormatter.ofPattern(this.scheduleMenuTimeFormat);
                    } catch (IllegalArgumentException e) {
                        AbstractFactionsPlugin.instance().getLogger().log(Level.WARNING, "Could not parse schedule menu time format. Defaulting to \"h:mm a\"", e);
                        this.scheduleMenuTimeFormatter = DateTimeFormatter.ofPattern("h:mm a");
                    }
                }

                return scheduleMenuTimeFormatter;
            }

            public String getScheduleMenuStatusTitle() {
                return scheduleMenuStatusTitle;
            }

            public List<String> getScheduleMenuStatusBodyNotSet() {
                return scheduleMenuStatusBodyNotSet;
            }

            public List<String> getScheduleMenuStatusBodyCurrentlySet() {
                return scheduleMenuStatusBodyCurrentlySet;
            }

            public String getScheduleMenuStatusButtonSetSchedule() {
                return scheduleMenuStatusButtonSetSchedule;
            }

            public String getScheduleMenuStatusButtonClearSchedule() {
                return scheduleMenuStatusButtonClearSchedule;
            }

            public String getScheduleMenuStatusButtonDone() {
                return scheduleMenuStatusButtonDone;
            }

            public int getScheduleMenuStatusColumns() {
                return scheduleMenuStatusColumns;
            }
        }

        public static class Upgrades extends AbsCommand {
            protected Upgrades() {
                super("Show faction upgrades", "upgrades");
            }

            public static class Paper {
                public static class General {
                    private String done = "Done";
                    private String returnToList = "Return to Upgrade List";
                    private String returnToInfo = "Return to Upgrade Info";

                    public String getDone() {
                        return done;
                    }

                    public String getReturnToInfo() {
                        return returnToInfo;
                    }

                    public String getReturnToList() {
                        return returnToList;
                    }
                }

                public static class MainPage {
                    private String title = "Faction Upgrades";
                    private String clickValueIfEconEnabled = "Click for details or to buy an upgrade.";
                    private String clickValueIfEconDisabled = "Click for details.";

                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Upgrades are listed below.");
                            this.add("<click>");
                        }
                    };

                    public String getTitle() {
                        return title;
                    }

                    public String getClickValueIfEconEnabled() {
                        return clickValueIfEconEnabled;
                    }

                    public String getClickValueIfEconDisabled() {
                        return clickValueIfEconDisabled;
                    }

                    public List<String> getBody() {
                        return body;
                    }
                }

                public static class InfoPage {
                    private String title = "Faction Upgrade";

                    private String statusLocked = "Not unlocked";
                    private String statusUnlocked = "Unlocked";
                    @Comment("Supports <level>")
                    private String statusCurrentLevel = "Current level: <level>";

                    private String upgradeAvailable = "Upgrade available!";
                    @Comment("Supports <cost>")
                    private String upgradeAvailableCosts = "Costs <cost>";
                    private String upgradeAtMaxLevel = "Max level!";
                    @Comment("Supports <level>")
                    private String upgradeAvailableLevelNumberIfNotSingleLevel = "Level <level>";
                    private String purchaseButton = "Purchase Upgrade";

                    private String prerequisitesHeader = "<negative>Locked - requires:";
                    @Comment("Supports <upgrade> and fills that in with either prerequisiteEntryLevelOne or prerequisiteEntryLevelOverOne based on the level")
                    private String prerequisiteEntry = "<negative>- <upgrade>";
                    @Comment("Supports <upgrade>, <level>")
                    private String prerequisiteEntryLevelOne = "<upgrade>";
                    @Comment("Supports <upgrade>, <level>")
                    private String prerequisiteEntryLevelOverOne = "<upgrade> (level <level>)";

                    public String getTitle() {
                        return title;
                    }

                    public String getPrerequisitesHeader() {
                        return prerequisitesHeader;
                    }

                    public String getPrerequisiteEntry() {
                        return prerequisiteEntry;
                    }

                    public String getPrerequisiteEntryLevelOne() {
                        return prerequisiteEntryLevelOne;
                    }

                    public String getPrerequisiteEntryLevelOverOne() {
                        return prerequisiteEntryLevelOverOne;
                    }

                    public String getStatusLocked() {
                        return statusLocked;
                    }

                    public String getStatusUnlocked() {
                        return statusUnlocked;
                    }

                    public String getStatusCurrentLevel() {
                        return statusCurrentLevel;
                    }

                    public String getUpgradeAvailable() {
                        return upgradeAvailable;
                    }

                    public String getUpgradeAvailableCosts() {
                        return upgradeAvailableCosts;
                    }

                    public String getUpgradeAtMaxLevel() {
                        return upgradeAtMaxLevel;
                    }

                    public String getUpgradeAvailableLevelNumberIfNotSingleLevel() {
                        return upgradeAvailableLevelNumberIfNotSingleLevel;
                    }

                    public String getPurchaseButton() {
                        return purchaseButton;
                    }
                }

                public static class PurchasePage {
                    private String title = "Purchase Upgrade";
                    @Comment("Supports <upgrade>, <cost>")
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Purchase next level of <upgrade>?");
                            this.add("");
                            this.add("Cost: <cost>");
                        }
                    };
                    private String confirmButton = "Confirm Purchase";

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }

                    public String getConfirmButton() {
                        return confirmButton;
                    }
                }

                public static class PurchaseComplete {
                    private String title = "Upgrade Purchase Complete";
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Upgrade successfully purchased!");
                        }
                    };

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }
                }

                public static class NoLongerInFaction {
                    private String title = "Access Denied";
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("You are no longer in a faction.");
                        }
                    };

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }
                }

                public static class CannotAfford {
                    private String title = "Cannot Purchase";
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Cannot afford next upgrade level!");
                        }
                    };

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }
                }

                public static class NoLongerSameLevel {
                    private String title = "Error";
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Upgrade changed level while you were in the menu!");
                        }
                    };

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }
                }

                public static class PrerequisitesNotMet {
                    private String title = "Cannot Purchase";
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("You do not meet the prerequisites for this upgrade.");
                        }
                    };

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }
                }

                public static class AlreadyMax {
                    private String title = "Error";
                    private List<String> body = new ArrayList<>() {
                        {
                            this.add("Upgrade already maxed out!");
                        }
                    };

                    public String getTitle() {
                        return title;
                    }

                    public List<String> getBody() {
                        return body;
                    }
                }

                private General general = new General();
                private MainPage mainPage = new MainPage();
                private InfoPage infoPage = new InfoPage();
                private PurchasePage purchasePage = new PurchasePage();
                private PurchaseComplete purchaseComplete = new PurchaseComplete();
                private CannotAfford cannotAfford = new CannotAfford();
                private AlreadyMax alreadyMax = new AlreadyMax();
                private NoLongerInFaction noLongerInFaction = new NoLongerInFaction();
                private NoLongerSameLevel noLongerSameLevel = new NoLongerSameLevel();
                private PrerequisitesNotMet prerequisitesNotMet = new PrerequisitesNotMet();

                public General general() {
                    return general;
                }

                public MainPage mainPage() {
                    return mainPage;
                }

                public InfoPage infoPage() {
                    return infoPage;
                }

                public PurchasePage purchasePage() {
                    return purchasePage;
                }

                public PurchaseComplete purchaseComplete() {
                    return purchaseComplete;
                }

                public AlreadyMax alreadyMax() {
                    return alreadyMax;
                }

                public CannotAfford cannotAfford() {
                    return cannotAfford;
                }

                public NoLongerInFaction noLongerInFaction() {
                    return noLongerInFaction;
                }

                public NoLongerSameLevel noLongerSameLevel() {
                    return noLongerSameLevel;
                }

                public PrerequisitesNotMet prerequisitesNotMet() {
                    return prerequisitesNotMet;
                }
            }

            public static class Spigot {
                private boolean empty = true;
            }

            @Comment("The Paper version of the command, using dialogs.\n" +
                    "Each section represents a dialog that can appear.")
            private Paper paper = new Paper();
            @Comment("The Spigot version of the command will be translated at a later time")
            private Spigot spigot = new Spigot();

            public Paper paper() {
                return paper;
            }

            public Spigot spigot() {
                return spigot;
            }
        }

        public static class Warp extends AbsCommand {
            protected Warp() {
                super("Teleport to a faction warp", "warp");
            }

            @Comment("Supports <faction>")
            private String noPermission = "<deny>You do not have permission to use <faction> warps.";
            private String invalidPassword = "<deny>Invalid password!";
            @Comment("Supports <warp>")
            private String warped = "<info>Warped to <focus><warp></focus>.";
            @Comment("Supports <warp>")
            private String invalidWarp = "<deny>Couldn't find warp <denyfocus><warp></denyfocus>.";
            @Comment("Supports <warp>, <seconds>")
            private String warmup = "<info>You will teleport to <focus><warp></focus> in <focus><seconds></focus> seconds.";
            @Comment("Supports <faction>")
            private String noWarps = "<info><faction> has no warps.";

            @Comment("Supports <warp>")
            private String menuWarpName = "<green><warp>";
            @Comment("Supports <faction>")
            private String menuTitle = "<faction> warps";
            private List<String> menuBody = new ArrayList<>() {
                {
                    this.add("Click the warp name below to teleport!");
                }
            };
            private String menuCancel = "Cancel";

            @Comment("Supports <warp>")
            private String menuPassTitle = "<warp>";
            private List<String> menuPassBody = new ArrayList<>() {
                {
                    this.add("Enter the password!");
                }
            };
            private String menuPassInputLabel = "Password";
            private String menuPassConfirm = "Confirm";

            public String getNoPermission() {
                return noPermission;
            }

            public String getInvalidPassword() {
                return invalidPassword;
            }

            public String getInvalidWarp() {
                return invalidWarp;
            }

            public String getWarped() {
                return warped;
            }

            public String getWarmup() {
                return warmup;
            }

            public String getNoWarps() {
                return noWarps;
            }

            public String getMenuWarpName() {
                return menuWarpName;
            }

            public String getMenuTitle() {
                return menuTitle;
            }

            public List<String> getMenuBody() {
                return menuBody;
            }

            public String getMenuCancel() {
                return menuCancel;
            }

            public String getMenuPassTitle() {
                return menuPassTitle;
            }

            public List<String> getMenuPassBody() {
                return menuPassBody;
            }

            public String getMenuPassInputLabel() {
                return menuPassInputLabel;
            }

            public String getMenuPassConfirm() {
                return menuPassConfirm;
            }
        }

        public static class Zone extends AbsCommand {
            public static class Claim extends AbsCommand {
                @Comment("Supports <name>")
                private String zoneNotFound = "<deny>Zone named '<name>' not found.";

                private String notInTerritory = "<deny>Not standing in faction territory.";
                @Comment("Supports <zone>")
                private String alreadyZone = "<deny>Already standing in <denyfocus><zone></denyfocus>.";
                @Comment("Supports <zone>")
                private String cannotManage = "<deny>Cannot manage zone <denyfocus><zone></denyfocus>!";
                @Comment("Supports <newzone>")
                private String success = "<info>Successfully set zone <focus><newzone></focus>!";
                @Comment("Supports <zone>")
                private String attemptingRadius = "<info>Setting zone <focus><zone></focus> for any chunks you can update.";
                @Comment("Supports <zone>")
                private String autoSetOn = "<info>Automatically setting zone for <focus><zone></focus> as you enter chunks.";
                private String autoSetOff = "<info>Disabled automatic zone setting.";

                public Claim() {
                    super("Unused description", "claim");
                }

                public String getAlreadyZone() {
                    return alreadyZone;
                }

                public String getAttemptingRadius() {
                    return attemptingRadius;
                }

                public String getCannotManage() {
                    return cannotManage;
                }

                public String getNotInTerritory() {
                    return notInTerritory;
                }

                public String getSuccess() {
                    return success;
                }

                public String getAutoSetOn() {
                    return autoSetOn;
                }

                public String getAutoSetOff() {
                    return autoSetOff;
                }

                public String getZoneNotFound() {
                    return zoneNotFound;
                }
            }

            public static class Create extends AbsCommand {
                @Comment("Supports <name>")
                private String nameAlreadyInUse = "<deny>Zone name '<name>' is already in use.";
                @Comment("Supports <name>")
                private String success = "<info>Created new zone '<name>'.";

                public Create() {
                    super("Unused description", "create");
                }

                public String getNameAlreadyInUse() {
                    return nameAlreadyInUse;
                }

                public String getSuccess() {
                    return success;
                }
            }

            public static class Delete extends AbsCommand {
                @Comment("Supports <name>")
                private String zoneNotFound = "<deny>Zone named '<name>' not found.";
                @Comment("Supports <name>")
                private String success = "<info>Deleted zone '<name>'.";
                @Comment("Supports <name>, <command>")
                private String confirm = "Are you sure you want to delete zone '<name>'? If so, run /<command>.";

                public Delete() {
                    super("Unused description", "delete");
                }

                public String getConfirm() {
                    return confirm;
                }

                public String getSuccess() {
                    return success;
                }

                public String getZoneNotFound() {
                    return zoneNotFound;
                }
            }

            public static class Perms {
                @Comment("Supports <name>")
                private String zoneNotFound = "<deny>Zone named '<name>' not found.";

                public String getZoneNotFound() {
                    return zoneNotFound;
                }
            }

            public static class Set extends AbsCommand {
                public static class Greeting extends AbsCommand {
                    @Comment("Supports <name>, <greeting>")
                    private String success = "<info>Set zone '<name>' greeting to '<greeting>'.";
                    @Comment("Supports <name>")
                    private String zoneNotFound = "<deny>Zone '<name>' not found.";

                    protected Greeting() {
                        super("Unused description", "greeting");
                    }

                    public String getSuccess() {
                        return success;
                    }

                    public String getZoneNotFound() {
                        return zoneNotFound;
                    }
                }

                public static class Name extends AbsCommand {
                    @Comment("Supports <name>")
                    private String nameAlreadyInUse = "<deny>Zone name '<name>' is already in use.";
                    @Comment("Supports <oldname>, <newname>")
                    private String success = "<info>Set zone '<oldname>' name to '<newname>'.";
                    @Comment("Supports <name>")
                    private String zoneNotFound = "<deny>Zone '<name>' not found.";

                    protected Name() {
                        super("Unused description", "name");
                    }

                    public String getNameAlreadyInUse() {
                        return nameAlreadyInUse;
                    }

                    public String getSuccess() {
                        return success;
                    }

                    public String getZoneNotFound() {
                        return zoneNotFound;
                    }
                }

                private Greeting greeting = new Greeting();
                private Name name = new Name();

                public Set() {
                    super("Unused description", "set");
                }

                public Greeting greeting() {
                    return greeting;
                }

                public Name name() {
                    return name;
                }
            }

            private Claim claim = new Claim();
            private Create create = new Create();
            private Delete delete = new Delete();
            @Comment("Reuses most things from the /f perms settings")
            private Perms perms = new Perms();
            private Set set = new Set();

            protected Zone() {
                super("Manage zones", "zone");
            }

            public Claim claim() {
                return claim;
            }

            public Create create() {
                return create;
            }

            public Delete delete() {
                return delete;
            }

            public Perms perms() {
                return perms;
            }

            public Set set() {
                return set;
            }
        }

        public static class Announce extends AbsCommand {
            public Announce() {
                super("Announce a message to all faction members", "announce");
            }

            @Comment("Supports <faction>, <player>, <message>")
            private String format = "<info><faction> [<player>]</info> <message>";

            public String getFormat() {
                return format;
            }
        }

        public static class Ban extends AbsCommand {
            public Ban() {
                super("Ban players from joining your Faction", "ban");
            }

            private String self = "<deny>You may not ban yourself.";
            @Comment("Supports <player>")
            private String insufficientRank = "<deny>Your rank is too low to ban <player>.";
            @Comment("Supports <player>")
            private String alreadyBanned = "<deny><player> is already banned.";
            @Comment("Supports <faction>")
            private String target = "<negative>You were banned from <faction>.";
            @Comment("Supports <player>, <target>")
            private String banned = "<info><player> <negative>banned <target>.";

            public String getSelf() {
                return self;
            }

            public String getInsufficientRank() {
                return insufficientRank;
            }

            public String getAlreadyBanned() {
                return alreadyBanned;
            }

            public String getTarget() {
                return target;
            }

            public String getBanned() {
                return banned;
            }
        }

        public static class Claim extends AbsCommand {
            public Claim() {
                super("Claim land from where you are standing", "claim");
            }

            @Comment("Supports <faction>")
            private String autoClaimEnabled = "<info>Now auto-claiming land for <faction>.";
            private String autoClaimDisabled = "<info>Auto-claiming of land disabled.";
            @Comment("Supports <faction>")
            private String autoClaimOtherFaction = "<deny>You can't claim land for <faction>.";
            private String claimDenied = "<deny>You do not have permission to claim in a radius.";
            @Comment("Supports <max>")
            private String fillAboveMax = "<deny>The maximum limit for claim fill is <max>.";
            private String fillAlreadyClaimed = "<deny>Cannot claim fill using already claimed land!";
            @Comment("Supports <max>")
            private String fillTooFar = "<deny>This fill would exceed the maximum distance of <max>.";
            private String fillPastLimit = "<deny>This claim would exceed the limit!";
            @Comment("Supports <faction>, <count>")
            private String fillNotEnoughLand = "<deny><faction> does not have enough land left to make <count> claims.";
            @Comment("Supports <count>")
            private String fillTooMuchFail = "<deny>Aborting claim fill after <count> failures.";
            @Comment("Supports <faction>")
            private String cantClaim = "<deny>You can't claim land for <faction>.";
            @Comment("Supports <faction>")
            private String alreadyOwn = "<info><faction> already own this land.";

            public String getAutoClaimEnabled() {
                return autoClaimEnabled;
            }

            public String getAutoClaimDisabled() {
                return autoClaimDisabled;
            }

            public String getAutoClaimOtherFaction() {
                return autoClaimOtherFaction;
            }

            public String getClaimDenied() {
                return claimDenied;
            }

            public String getFillAboveMax() {
                return fillAboveMax;
            }

            public String getFillAlreadyClaimed() {
                return fillAlreadyClaimed;
            }

            public String getFillTooFar() {
                return fillTooFar;
            }

            public String getFillPastLimit() {
                return fillPastLimit;
            }

            public String getFillNotEnoughLand() {
                return fillNotEnoughLand;
            }

            public String getFillTooMuchFail() {
                return fillTooMuchFail;
            }

            public String getCantClaim() {
                return cantClaim;
            }

            public String getAlreadyOwn() {
                return alreadyOwn;
            }
        }

        public static class Clear extends AbsCommand {
            public Clear() {
                super("Clear faction data", "clear");
            }

            private String descriptionBans = "Unban all from faction";
            private String descriptionClaims = "Unclaim all territory";
            private String descriptionInvites = "Revoke all invites";
            private String descriptionWarps = "Delete all warps";
            private String subCmdBans = "bans";
            private String subCmdClaims = "claims";
            private String subCmdInvites = "invites";
            private String subCmdWarps = "warps";
            @Comment("Supports <command>")
            private String bansClearConfirm = "<info>Are you sure you want to clear all bans? If so, run /<command>";
            private String bansClearSuccess = "<info>All bans removed.";
            @Comment("Supports <command>")
            private String warpsClearConfirm = "<info>Are you sure you want to clear all warps? If so, run /<command>";
            private String warpsClearSuccess = "<info>Deleted all warps.";
            @Comment("Supports <command>")
            private String invitesClearConfirm = "<info>Are you sure you want to clear all invites? If so, run /<command>";
            private String invitesClearSuccess = "<info>Deleted all invites.";

            public String getDescriptionBans() {
                return descriptionBans;
            }

            public String getDescriptionClaims() {
                return descriptionClaims;
            }

            public String getDescriptionInvites() {
                return descriptionInvites;
            }

            public String getDescriptionWarps() {
                return descriptionWarps;
            }

            public String getBansClearConfirm() {
                return bansClearConfirm;
            }

            public String getBansClearSuccess() {
                return bansClearSuccess;
            }

            public String getWarpsClearConfirm() {
                return warpsClearConfirm;
            }

            public String getWarpsClearSuccess() {
                return warpsClearSuccess;
            }

            public String getInvitesClearConfirm() {
                return invitesClearConfirm;
            }

            public String getInvitesClearSuccess() {
                return invitesClearSuccess;
            }

            public String getSubCmdBans() {
                return subCmdBans;
            }

            public String getSubCmdClaims() {
                return subCmdClaims;
            }

            public String getSubCmdInvites() {
                return subCmdInvites;
            }

            public String getSubCmdWarps() {
                return subCmdWarps;
            }
        }

        public static class Coords extends AbsCommand {
            public Coords() {
                super("Broadcast your current position to your faction", "coords");
            }

            @Comment("Supports <player>, <x>, <y>, <z>, <world>")
            private String message = "<info><player>'s location: <focus><x></focus>, <focus><y></focus>, <focus><z></focus> in <focus><world></focus>.";

            public String getMessage() {
                return message;
            }
        }

        public static class Create extends AbsCommand {
            public Create() {
                super("Create a new faction", "create");
            }

            private String mustLeave = "<deny>You must leave your current faction first.";
            private String inUse = "<deny>That tag is already in use.";
            @Comment("Supports <player>, <faction>")
            private String created = "<info><player> created a new faction <faction>.";

            public String getMustLeave() {
                return mustLeave;
            }

            public String getInUse() {
                return inUse;
            }

            public String getCreated() {
                return created;
            }
        }

        public static class DTR extends AbsCommand {
            public DTR() {
                super("Show faction DTR info", "dtr");
            }

            @Comment("Supports <faction>, <faction:dtr_rounded>, <faction:dtr_max_rounded>")
            private String dtr = "<info><faction> - DTR / Max DTR: <focus><faction:dtr_rounded></focus> / <focus><faction:dtr_max_rounded>";

            public String getDtr() {
                return dtr;
            }
        }

        public static class Fly extends AbsCommand {
            public Fly() {
                super("Enter or leave Faction flight mode", "fly");
            }

            @Comment("Supports <state>")
            private String auto = "<info>Faction auto flight <focus><state></focus>.";
            @Comment("Supports <state>")
            private String trailsChange = "<info>Faction flight trail <focus><state></focus>.";
            private String trailsParticleInvalid = "<deny>Invalid particle effect.";
            @Comment("Supports <particle>")
            private String trailsParticlePerms = "<deny>Insufficient permission to use <denyfocus><particle></denyfocus>.";
            @Comment("Supports <particle>")
            private String trailsParticleChange = "<info>Faction flight trail effect set to <focus><particle></focus>.";
            @Comment("Supports <faction>")
            private String noAccess = "<deny>Cannot fly in territory of <faction>.";
            private String enemyNearby = "<deny>Cannot enable fly, enemy nearby.";
            @Comment("Supports <seconds>")
            private String warmup = "<info>Flight will enable in <focus><seconds></focus> seconds.";
            private String damage = "<info>Faction flight <focus>disabled</focus> due to entering combat.";
            private String enemyDisable = "<deny>Enemy nearby, disabling fly.";
            @Comment("Supports <state>")
            private String change = "<info>Faction flight <focus><state></focus>.";

            public String getAuto() {
                return auto;
            }

            public String getTrailsChange() {
                return trailsChange;
            }

            public String getTrailsParticleInvalid() {
                return trailsParticleInvalid;
            }

            public String getTrailsParticlePerms() {
                return trailsParticlePerms;
            }

            public String getTrailsParticleChange() {
                return trailsParticleChange;
            }

            public String getNoAccess() {
                return noAccess;
            }

            public String getEnemyNearby() {
                return enemyNearby;
            }

            public String getWarmup() {
                return warmup;
            }

            public String getDamage() {
                return damage;
            }

            public String getEnemyDisable() {
                return enemyDisable;
            }

            public String getChange() {
                return change;
            }
        }

        public static class Grace extends AbsCommand {
            public Grace() {
                super("View the current grace status", "grace");
            }

            private String notSet = "<info>Grace is not active.";
            @Comment("Supports <duration>")
            private String active = "<info>Grace active! No explosions for <duration>.";

            public String getNotSet() {
                return notSet;
            }

            public String getActive() {
                return active;
            }
        }

        public static class Home extends AbsCommand {
            public Home() {
                super("Teleport to the faction home", "home");
            }

            @Comment("Supports <faction>")
            private String denied = "<deny>Sorry, you cannot teleport to the home of <faction>.";
            private String noHome = "<deny>Your faction does not have a home.";
            private String inEnemy = "<deny>You cannot teleport to your faction home while in the territory of an enemy faction.";
            private String wrongWorld = "<deny>You cannot teleport to your faction home while in a different world.";
            @Comment("Supports <range>")
            private String enemyNear = "<deny>You cannot teleport to your faction home while an enemy is within <range> blocks of you.";
            @Comment("Supports <seconds>")
            private String warmup = "<info>You will teleport home in <focus><seconds></focus> seconds.";

            public String getDenied() {
                return denied;
            }

            public String getNoHome() {
                return noHome;
            }

            public String getInEnemy() {
                return inEnemy;
            }

            public String getWrongWorld() {
                return wrongWorld;
            }

            public String getEnemyNear() {
                return enemyNear;
            }

            public String getWarmup() {
                return warmup;
            }
        }

        public static class Invite extends AbsCommand {
            public Invite() {
                super("Invite a player to your faction", "invite");
            }

            @Comment("Supports <player>, <faction>")
            private String alreadyMember = "<info><player> is already a member of <faction>.";
            @Comment("Supports <player>, <faction>")
            private String deinviteRevoked = "<info><player> revoked your invitation to <focus><faction></focus>.";
            @Comment("Supports <player>, <target>")
            private String deinviteRevokes = "<info><player> revoked <target>'s<info> invitation.";
            @Comment("Supports <player>")
            private String banned = "<deny><player> is banned from your Faction. Not sending an invite.";
            @Comment("Supports <player>, <target>")
            private String invited = "<info><player> invited <target> to your faction.";
            private String invitedYou = " has invited you to join ";
            private String clickToJoin = "Click to join!";

            public String getAlreadyMember() {
                return alreadyMember;
            }

            public String getDeinviteRevoked() {
                return deinviteRevoked;
            }

            public String getDeinviteRevokes() {
                return deinviteRevokes;
            }

            public String getBanned() {
                return banned;
            }

            public String getInvited() {
                return invited;
            }

            public String getInvitedYou() {
                return invitedYou;
            }

            public String getClickToJoin() {
                return clickToJoin;
            }
        }

        public static class Kick extends AbsCommand {
            public Kick() {
                super("Kick a player from the faction", "kick");
            }

            private String candidates = "<info>Players you can kick: ";
            @Comment("Supports <player>")
            private String clickToKick = "Click to kick <player>";
            private String self = "<deny>You cannot kick yourself.";
            private String none = "<deny>That player is not in a faction.";
            @Comment("Supports <player>, <faction>")
            private String notMember = "<deny><player> is not a member of <faction>.";
            private String insufficientRank = "<deny>Your rank is too low to kick this player.";
            @Comment("Supports <player>, <target>")
            private String factionMsg = "<info><player> kicked <target> from the faction! :O";
            @Comment("Supports <player>, <faction>")
            private String kicked = "<info><player> kicked you from <faction>! :O";
            private String enemyTerritory = "<deny>You cannot kick a player in enemy territory.";
            private String negativePower = "<deny>You cannot kick that member until their power is positive.";

            public String getCandidates() {
                return candidates;
            }

            public String getClickToKick() {
                return clickToKick;
            }

            public String getSelf() {
                return self;
            }

            public String getNone() {
                return none;
            }

            public String getNotMember() {
                return notMember;
            }

            public String getInsufficientRank() {
                return insufficientRank;
            }

            public String getFactionMsg() {
                return factionMsg;
            }

            public String getKicked() {
                return kicked;
            }

            public String getEnemyTerritory() {
                return enemyTerritory;
            }

            public String getNegativePower() {
                return negativePower;
            }
        }

        public static class Leave extends AbsCommand {
            public Leave() {
                super("Leave your faction", "leave");
            }

            private String negativePower = "<deny>You cannot leave until your power is positive.";
            private String passAdmin = "<deny>You must give the admin role to someone else first.";
            @Comment("Supports <player> (who left), <faction>")
            private String leftNotice = "<info><player> left <faction>.";
            @Comment("Supports <faction>")
            private String disbanded = "<info><faction> was disbanded.";

            public String getNegativePower() {
                return negativePower;
            }

            public String getPassAdmin() {
                return passAdmin;
            }

            public String getLeftNotice() {
                return leftNotice;
            }

            public String getDisbanded() {
                return disbanded;
            }
        }

        public static class MoneyCmd extends AbsCommand {
            public MoneyCmd() {
                super("Manage faction bank money", "money");
            }

            private String noFaction = "<deny>You are not member of any faction.";


            public String getNoFaction() {
                return noFaction;
            }

            public static class MoneyBalanceCmd extends AbsCommand {
                public MoneyBalanceCmd() {
                    super("View your faction's bank balance", "balance");
                }
            }

            public static class MoneyDepositCmd extends AbsCommand {
                public MoneyDepositCmd() {
                    super("Deposit money into the faction bank", "deposit");
                }
            }

            public static class MoneyDepositSend extends AbsCommand {
                public MoneyDepositSend() {
                    super("Transfer money between factions", "send");
                }

                private String subCmdTo = "to";
                private String subCmdPlayer = "player";
                private String subCmdFaction = "faction";

                public String getSubCmdTo() {
                    return subCmdTo;
                }

                public String getSubCmdPlayer() {
                    return subCmdPlayer;
                }

                public String getSubCmdFaction() {
                    return subCmdFaction;
                }
            }

            public static class MoneyWithdrawCmd extends AbsCommand {
                public MoneyWithdrawCmd() {
                    super("Withdraw money from the faction bank", "withdraw");
                }

                private String noPermission = "<deny>You don't have permission to withdraw.";

                public String getNoPermission() {
                    return noPermission;
                }
            }

            private MoneyBalanceCmd moneyBalanceCmd = new MoneyBalanceCmd();
            private MoneyDepositCmd moneyDepositCmd = new MoneyDepositCmd();
            private MoneyDepositSend moneyDepositSend = new MoneyDepositSend();
            private MoneyWithdrawCmd moneyWithdrawCmd = new MoneyWithdrawCmd();

            public MoneyBalanceCmd balance() {
                return moneyBalanceCmd;
            }

            public MoneyDepositCmd deposit() {
                return moneyDepositCmd;
            }

            public MoneyDepositSend send() {
                return moneyDepositSend;
            }

            public MoneyWithdrawCmd withdraw() {
                return moneyWithdrawCmd;
            }
        }

        public static class Power extends AbsCommand {
            public Power() {
                super("Show player power info", "power");
            }

            @Comment("Supports <player>, <power>, <power_max>, <bonus_penalty>")
            private String power = "<info><player> - Power / maxpower: <focus><power></focus> / <focus><power_max><bonus_penalty></focus>";
            private String bonus = " (bonus: ";
            private String penalty = " (penalty: ";

            public String getPower() {
                return power;
            }

            public String getBonus() {
                return bonus;
            }

            public String getPenalty() {
                return penalty;
            }
        }

        public static class Status extends AbsCommand {
            public Status() {
                super("Show the status of a player", "status");
            }

            @Comment("Supports <player>, <power>, <last_seen>")
            private String format = "<player> Power: <power> Last Seen: <last_seen>";
            private String online = "<positive>Online";
            private String agoSuffix = " ago.";

            public String getFormat() {
                return format;
            }

            public String getOnline() {
                return online;
            }

            public String getAgoSuffix() {
                return agoSuffix;
            }
        }

        public static class Stuck extends AbsCommand {
            public Stuck() {
                super("Safely teleports you out of enemy faction", "stuck");
            }

            private String alreadyExists = "<deny>You are already teleporting, you must wait!";
            @Comment("Supports <range>")
            private String outside = "<deny>Teleport cancelled because you left <denyfocus><range></denyfocus> block radius.";
            @Comment("Supports <x>, <y>, <z>")
            private String teleport = "<info>Teleported safely to <focus><x></focus>, <focus><y></focus>, <focus><z></focus>.";
            private String failed = "<deny>Failed to find a safe place to get you out.";
            @Comment("Supports <seconds>")
            private String warmup = "<info>You will find a safe place to become unstuck in <focus><seconds></focus> seconds.";

            public String getAlreadyExists() {
                return alreadyExists;
            }

            public String getOutside() {
                return outside;
            }

            public String getTeleport() {
                return teleport;
            }

            public String getFailed() {
                return failed;
            }

            public String getWarmup() {
                return warmup;
            }
        }

        public static class TNTCmd extends AbsCommand {
            public TNTCmd() {
                super("View your faction's TNT bank", "tnt");
            }

            @Comment("Supports <count>")
            private String message = "<info>Your faction has <count> TNT.";
            private String territoryOnly = "<deny>Command can only be run from your faction's territory!";
            private String depositDescription = "Add to your faction's TNT bank";
            private String depositFailFull = "<deny>Faction bank already at maximum!";
            private String depositFailPositive = "<deny>Must deposit at least one!";
            @Comment("Supports <count>")
            private String depositSuccess = "<info>Your faction now has <count> TNT.";
            private String fillDescription = "Fill TNT into nearby dispensers";
            @Comment("Supports <count>, <dispensers>, <remaining>")
            private String fillMessage = "<info>Filled <count> TNT into <dispensers> dispensers. <remaining> left in the faction bank.";
            @Comment("Supports <value>, <max>")
            private String fillFailMaxRadius = "<deny><value> is bigger than the maximum radius of <max>.";
            @Comment("Supports <count>")
            private String fillFailNotEnough = "<deny>The faction bank does not have <count> TNT!";
            private String fillFailPositive = "<deny>Positive values only!";
            private String siphonDescription = "Take TNT from nearby dispensers";
            @Comment("Supports <count>, <total>")
            private String siphonMessage = "<info>Acquired <count> TNT, for a total of <total> in the faction bank.";
            private String siphonFailPositive = "<deny>Positive values only!";
            private String siphonFailFull = "<deny>Faction bank already at maximum!";
            @Comment("Supports <value>, <max>")
            private String siphonFailMaxRadius = "<deny><value> is bigger than the maximum radius of <max>.";
            private String withdrawDescription = "Withdraw TNT from the faction bank";
            @Comment("Supports <count>, <remaining>")
            private String withdrawMessage = "<info>Withdrew <count> TNT. <remaining> left in the faction bank.";
            @Comment("Supports <count>")
            private String withdrawFailNotEnough = "<deny>The faction bank does not have <count> TNT!";
            private String withdrawFailPositive = "<deny>Positive values only!";

            private String subCmdDeposit = "deposit";
            private String subCmdFill = "fill";
            private String subCmdSiphon = "siphon";
            private String subCmdWithdraw = "withdraw";

            public String getMessage() {
                return message;
            }

            public String getTerritoryOnly() {
                return territoryOnly;
            }

            public String getDepositDescription() {
                return depositDescription;
            }

            public String getDepositFailFull() {
                return depositFailFull;
            }

            public String getDepositFailPositive() {
                return depositFailPositive;
            }

            public String getDepositSuccess() {
                return depositSuccess;
            }

            public String getFillDescription() {
                return fillDescription;
            }

            public String getFillMessage() {
                return fillMessage;
            }

            public String getFillFailMaxRadius() {
                return fillFailMaxRadius;
            }

            public String getFillFailNotEnough() {
                return fillFailNotEnough;
            }

            public String getFillFailPositive() {
                return fillFailPositive;
            }

            public String getSiphonDescription() {
                return siphonDescription;
            }

            public String getSiphonMessage() {
                return siphonMessage;
            }

            public String getSiphonFailPositive() {
                return siphonFailPositive;
            }

            public String getSiphonFailFull() {
                return siphonFailFull;
            }

            public String getSiphonFailMaxRadius() {
                return siphonFailMaxRadius;
            }

            public String getWithdrawDescription() {
                return withdrawDescription;
            }

            public String getWithdrawMessage() {
                return withdrawMessage;
            }

            public String getWithdrawFailNotEnough() {
                return withdrawFailNotEnough;
            }

            public String getWithdrawFailPositive() {
                return withdrawFailPositive;
            }

            public String getSubCmdDeposit() {
                return subCmdDeposit;
            }

            public String getSubCmdFill() {
                return subCmdFill;
            }

            public String getSubCmdSiphon() {
                return subCmdSiphon;
            }

            public String getSubCmdWithdraw() {
                return subCmdWithdraw;
            }
        }

        public static class ToggleLogins extends AbsCommand {
            public ToggleLogins() {
                super("Toggle login / logout notifications for Faction members", "logins");
            }

            @Comment("Supports <state>")
            private String toggle = "<info>Set login / logout notifications for Faction members to: <focus><state></focus>.";

            public String getToggle() {
                return toggle;
            }
        }

        public static class ToggleScoreboard extends AbsCommand {
            public ToggleScoreboard() {
                super("Scoreboardy things", "scoreboard");
            }

            private String disabled = "You can't toggle scoreboards while they are disabled.";
            @Comment("Supports <value>")
            private String toggleSb = "You now have scoreboards set to <value>.";

            public String getDisabled() {
                return disabled;
            }

            public String getToggleSb() {
                return toggleSb;
            }
        }

        public static class ToggleSeeChunk extends AbsCommand {
            public ToggleSeeChunk() {
                super("Show chunk boundaries", "seechunk");
            }

            @Comment("Supports <state>")
            private String toggle = "<info>Seechunk <focus><state></focus>.";

            public String getToggle() {
                return toggle;
            }
        }

        public static class Top extends AbsCommand {
            public Top() {
                super("Sort Factions to see the top of some criteria.", "top");
            }

            @Comment("Supports <criteria>, <page_current>, <page_count>")
            private String top = "Top Factions by <criteria>. Page <page_current>/<page_count>";
            @Comment("Supports <rank>, <faction>, <value>")
            private String line = "<rank>. <golden><faction>: <value>";
            @Comment("Supports <criteria>")
            private String invalid = "Could not sort by <criteria>. Try balance, online, members, power or land.";

            public String getTop() {
                return top;
            }

            public String getLine() {
                return line;
            }

            public String getInvalid() {
                return invalid;
            }
        }

        public static class Unclaim extends AbsCommand {
            public Unclaim() {
                super("Unclaim the land where you are standing", "unclaim");
            }

            @Comment("Supports <faction>")
            private String autoUnclaimEnabled = "<info>Now auto-unclaiming land for <faction>.";
            private String autoUnclaimDisabled = "<info>Auto-unclaiming of land disabled.";
            @Comment("Supports <faction>")
            private String autoUnclaimOtherFaction = "<deny>You can't unclaim land for <faction>.";
            @Comment("Supports <faction>")
            private String cantUnclaim = "<deny>You can't unclaim land for <faction>.";
            @Comment("Supports <max>")
            private String fillAboveMax = "<deny>The maximum limit for unclaim fill is <max>.";
            private String fillNotClaimed = "<deny>Cannot unclaim fill using non-claimed land!";
            @Comment("Supports <max>")
            private String fillTooFar = "<deny>This unclaim would exceed the maximum distance of <max>.";
            private String fillPastLimit = "<deny>This unclaim would exceed the limit!";
            @Comment("Supports <count>")
            private String fillTooMuchFail = "<deny>Aborting unclaim fill after <count> failures.";
            @Comment("Supports <player>, <count>, <location>")
            private String fillUnclaimed = "<info><player> unclaimed <count> claims of your faction's land around <location>.";
            @Comment("Supports <count>")
            private String fillBypassComplete = "<info>Unclaimed <count> claims.";
            @Comment("Supports <faction>, <command>")
            private String unclaimAllConfirm = "<info>Are you sure you want to unclaim ALL <faction> territory? If so, run /<command>";
            @Comment("Supports <player>")
            private String unclaimAllUnclaimed = "<info><player> unclaimed ALL of your faction's land.";

            public String getAutoUnclaimEnabled() {
                return autoUnclaimEnabled;
            }

            public String getAutoUnclaimDisabled() {
                return autoUnclaimDisabled;
            }

            public String getAutoUnclaimOtherFaction() {
                return autoUnclaimOtherFaction;
            }

            public String getCantUnclaim() {
                return cantUnclaim;
            }

            public String getFillAboveMax() {
                return fillAboveMax;
            }

            public String getFillNotClaimed() {
                return fillNotClaimed;
            }

            public String getFillTooFar() {
                return fillTooFar;
            }

            public String getFillPastLimit() {
                return fillPastLimit;
            }

            public String getFillTooMuchFail() {
                return fillTooMuchFail;
            }

            public String getFillUnclaimed() {
                return fillUnclaimed;
            }

            public String getFillBypassComplete() {
                return fillBypassComplete;
            }

            public String getUnclaimAllConfirm() {
                return unclaimAllConfirm;
            }

            public String getUnclaimAllUnclaimed() {
                return unclaimAllUnclaimed;
            }
        }

        public static class Unban extends AbsCommand {
            public Unban() {
                super("Unban someone from your Faction", "unban");
            }

            @Comment("Supports <player>")
            private String notBanned = "<deny><player> <negative>isn't banned. Not doing anything.";
            @Comment("Supports <player>, <target>")
            private String unbanned = "<info><player> <negative>unbanned <target>.";
            @Comment("Supports <faction>")
            private String target = "<positive>You were unbanned from <faction>.";

            public String getNotBanned() {
                return notBanned;
            }

            public String getUnbanned() {
                return unbanned;
            }

            public String getTarget() {
                return target;
            }
        }

        public static class Vault extends AbsCommand {
            public Vault() {
                super("/f vault <number> to open one of your Faction's vaults.", "vault");
            }

            @Comment("Supports <vault>, <max>")
            private String tooHigh = "<deny>You tried to open vault <vault> but your Faction only has <max> vaults.";

            public String getTooHigh() {
                return tooHigh;
            }
        }

        public static class Version extends AbsCommand {
            public Version() {
                super("Show plugin and translation version information", "version");
            }
        }

        private Generic generic = new Generic();

        private Admin admin = new Admin();

        private Announce announce = new Announce();
        private Ban ban = new Ban();
        private Chat chat = new Chat();
        private Claim claim = new Claim();
        private Clear clear = new Clear();
        private Confirm confirm = new Confirm();
        private Coords coords = new Coords();
        private Create create = new Create();
        private Disband disband = new Disband();
        private DTR dtr = new DTR();
        private Fly fly = new Fly();
        private Grace grace = new Grace();
        private Help help = new Help();
        private Home home = new Home();
        private Invite invite = new Invite();
        private Join join = new Join();
        private Kick kick = new Kick();
        private Leave leave = new Leave();
        private Link link = new Link();
        private ListCmd list = new ListCmd();
        private MapCmd map = new MapCmd();
        private MoneyCmd money = new MoneyCmd();
        private Near near = new Near();
        private Role role = new Role();
        private Power power = new Power();
        private SetCmd set = new SetCmd();
        private Show show = new Show();
        private Permissions permissions = new Permissions();
        private Relation relation = new Relation();
        private Shield shield = new Shield();
        private Status status = new Status();
        private Upgrades upgrades = new Upgrades();
        private Stuck stuck = new Stuck();
        private TNTCmd tnt = new TNTCmd();
        private ToggleLogins toggleLogins = new ToggleLogins();
        private ToggleScoreboard toggleScoreboard = new ToggleScoreboard();
        private ToggleSeeChunk toggleSeeChunk = new ToggleSeeChunk();
        private Top top = new Top();
        private Unclaim unclaim = new Unclaim();
        private Unban unban = new Unban();
        private Vault vault = new Vault();
        private Version version = new Version();
        private Warp warp = new Warp();
        private Zone zone = new Zone();

        public Generic generic() {
            return generic;
        }

        public Admin admin() {
            return admin;
        }

        public Chat chat() {
            return chat;
        }

        public Confirm confirm() {
            return confirm;
        }

        public Disband disband() {
            return disband;
        }

        public Join join() {
            return join;
        }

        public Help help() {
            return help;
        }

        public Link link() {
            return link;
        }

        public ListCmd list() {
            return list;
        }

        public MapCmd map() {
            return map;
        }

        public Near near() {
            return near;
        }

        public Role role() {
            return role;
        }

        public SetCmd set() {
            return set;
        }

        public Show show() {
            return show;
        }

        public Permissions permissions() {
            return permissions;
        }

        public Relation relation() {
            return relation;
        }

        public Shield shield() {
            return shield;
        }

        public Upgrades upgrades() {
            return upgrades;
        }

        public Warp warp() {
            return warp;
        }

        public Zone zone() {
            return zone;
        }

        public Announce announce() {
            return announce;
        }

        public Ban ban() {
            return ban;
        }

        public Claim claim() {
            return claim;
        }

        public Clear clear() {
            return clear;
        }

        public Coords coords() {
            return coords;
        }

        public Create create() {
            return create;
        }

        public DTR dtr() {
            return dtr;
        }

        public Fly fly() {
            return fly;
        }

        public Grace grace() {
            return grace;
        }

        public Home home() {
            return home;
        }

        public Invite invite() {
            return invite;
        }

        public Kick kick() {
            return kick;
        }

        public Leave leave() {
            return leave;
        }

        public MoneyCmd money() {
            return money;
        }

        public Power power() {
            return power;
        }

        public Status status() {
            return status;
        }

        public Stuck stuck() {
            return stuck;
        }

        public TNTCmd tnt() {
            return tnt;
        }

        public ToggleLogins toggleLogins() {
            return toggleLogins;
        }

        public ToggleScoreboard toggleScoreboard() {
            return toggleScoreboard;
        }

        public ToggleSeeChunk toggleSeeChunk() {
            return toggleSeeChunk;
        }

        public Top top() {
            return top;
        }

        public Unclaim unclaim() {
            return unclaim;
        }

        public Unban unban() {
            return unban;
        }

        public Vault vault() {
            return vault;
        }

        public Version version() {
            return version;
        }
    }

    public static class General {
        public static class EnterTitles {
            @Comment("Supports <faction>")
            private String title = "<faction>";
            @Comment("Supports <faction>, <faction:description>")
            private String subtitle = "<gray><faction:description>";
            @Comment("Supports <oldfaction>, <newfaction>")
            private String chat = "<info>Leaving <oldfaction>. Entering <newfaction>.";

            public String getTitle() {
                return title;
            }

            public String getSubtitle() {
                return subtitle;
            }

            public String getChat() {
                return chat;
            }
        }

        private EnterTitles enterTitles = new EnterTitles();

        public EnterTitles enterTitles() {
            return enterTitles;
        }

        public static class Relations {
            private String ally = "ally";
            private String allies = "allies";
            private String member = "member";
            private String members = "members";
            private String truce = "truce";
            private String truces = "truces";
            private String enemy = "enemy";
            private String enemies = "enemies";
            private String neutral = "neutral";
            private String neutrals = "neutrals";

            public String getAlly() {
                return ally;
            }

            public String getAllies() {
                return allies;
            }

            public String getMember() {
                return member;
            }

            public String getMembers() {
                return members;
            }

            public String getTruce() {
                return truce;
            }

            public String getTruces() {
                return truces;
            }

            public String getEnemy() {
                return enemy;
            }

            public String getEnemies() {
                return enemies;
            }

            public String getNeutral() {
                return neutral;
            }

            public String getNeutrals() {
                return neutrals;
            }
        }

        private Relations relations = new Relations();

        public Relations relations() {
            return relations;
        }

        public static class Roles {
            private String admin = "admin";
            private String coleader = "coleader";
            private String moderator = "moderator";
            private String normal = "member";
            private String recruit = "recruit";

            public String getAdmin() {
                return admin;
            }

            public String getColeader() {
                return coleader;
            }

            public String getModerator() {
                return moderator;
            }

            public String getNormal() {
                return normal;
            }

            public String getRecruit() {
                return recruit;
            }
        }

        private Roles roles = new Roles();

        public Roles roles() {
            return roles;
        }

        public static class FactionTag {
            private String blacklisted = "<info>That faction tag is blacklisted.";
            @Comment("Supports <min>")
            private String tooShort = "<info>The faction tag can't be shorter than <focus><min></focus> chars.";
            @Comment("Supports <max>")
            private String tooLong = "<info>The faction tag can't be longer than <focus><max></focus> chars.";
            @Comment("Supports <chars>")
            private String alphanumeric = "<info>Faction tag must be alphanumeric. \"<focus><chars></focus>\" is not allowed.";

            public String getBlacklisted() {
                return blacklisted;
            }

            public String getTooShort() {
                return tooShort;
            }

            public String getTooLong() {
                return tooLong;
            }

            public String getAlphanumeric() {
                return alphanumeric;
            }
        }

        private FactionTag factionTag = new FactionTag();

        public FactionTag factionTag() {
            return factionTag;
        }

        public static class Duration {
            private String day = "%d day";
            private String days = "%d days";
            private String hour = "%d hour";
            private String hours = "%d hours";
            private String minute = "%d minute";
            private String minutes = "%d minutes";
            private String second = "%d second";
            private String seconds = "%d seconds";
            private String and = "and";

            public String getDay() {
                return day;
            }

            public String getDays() {
                return days;
            }

            public String getHour() {
                return hour;
            }

            public String getHours() {
                return hours;
            }

            public String getMinute() {
                return minute;
            }

            public String getMinutes() {
                return minutes;
            }

            public String getSecond() {
                return second;
            }

            public String getSeconds() {
                return seconds;
            }

            public String getAnd() {
                return and;
            }
        }

        private Duration duration = new Duration();

        public Duration duration() {
            return duration;
        }
    }

    public static class Economy {
        public static class Actions {
            private String warpFor = "for warping";
            private String warpTo = "to warp";
            private String joinFor = "for joining a faction";
            private String joinTo = "to join a faction";
            private String relationFor = "for changing a relation wish";
            private String relationTo = "to change a relation wish";
            private String titleFor = "for changing a player's title";
            private String titleTo = "to change a player's title";
            private String createTo = "to create a new faction";
            private String createFor = "for creating a new faction";
            private String dtrTo = "to show faction DTR info";
            private String dtrFor = "for showing faction DTR info";
            private String homeFor = "for teleporting to your faction home";
            private String homeTo = "to teleport to your faction home";
            private String inviteTo = "to invite someone";
            private String inviteFor = "for inviting someone";
            private String kickTo = "to kick someone from the faction";
            private String kickFor = "for kicking someone from the faction";
            private String listFor = "for listing the factions";
            private String listTo = "to list the factions";
            private String mapFor = "for showing the map";
            private String mapTo = "to show the map";
            private String openFor = "for opening or closing the faction";
            private String openTo = "to open or close the faction";
            private String powerFor = "for showing player power info";
            private String powerTo = "to show player power info";
            private String showFor = "for showing faction information";
            private String showTo = "to show faction information";
            private String stuckFor = "for initiating a safe teleport out";
            private String stuckTo = "to safely teleport out";
            private String tagFor = "for changing the faction tag";
            private String tagTo = "to change the faction tag";
            private String setHomeFor = "for setting the faction home";
            private String setHomeTo = "to set the faction home";
            private String delHomeFor = "for unsetting the faction home";
            private String delHomeTo = "to unset the faction home";
            private String boomFor = "for toggling explosions";
            private String boomTo = "to toggle explosions";
            private String setWarpFor = "for setting warp";
            private String setWarpTo = "to set warp";
            private String delWarpFor = "for deleting warp";
            private String delWarpTo = "to delete warp";
            private String descriptionFor = "for changing faction description";
            private String descriptionTo = "to change faction description";
            private String upgradeFor = "for buying an upgrade";
            private String upgradeTo = "to buy an upgrade";
            private String unclaimFor = "for unclaiming this land";
            private String unclaimTo = "to unclaim this land";
            private String unclaimAllFor = "for unclaiming all faction land";
            private String unclaimAllTo = "to unclaim all faction land";

            public String getJoinTo() {
                return joinTo;
            }

            public String getJoinFor() {
                return joinFor;
            }

            public String getWarpFor() {
                return warpFor;
            }

            public String getWarpTo() {
                return warpTo;
            }

            public String getRelationFor() {
                return relationFor;
            }

            public String getRelationTo() {
                return relationTo;
            }

            public String getTitleFor() {
                return titleFor;
            }

            public String getTitleTo() {
                return titleTo;
            }

            public String getCreateTo() {
                return createTo;
            }

            public String getCreateFor() {
                return createFor;
            }

            public String getDtrTo() {
                return dtrTo;
            }

            public String getDtrFor() {
                return dtrFor;
            }

            public String getHomeFor() {
                return homeFor;
            }

            public String getHomeTo() {
                return homeTo;
            }

            public String getInviteTo() {
                return inviteTo;
            }

            public String getInviteFor() {
                return inviteFor;
            }

            public String getKickTo() {
                return kickTo;
            }

            public String getKickFor() {
                return kickFor;
            }

            public String getListFor() {
                return listFor;
            }

            public String getListTo() {
                return listTo;
            }

            public String getMapFor() {
                return mapFor;
            }

            public String getMapTo() {
                return mapTo;
            }

            public String getOpenFor() {
                return openFor;
            }

            public String getOpenTo() {
                return openTo;
            }

            public String getPowerFor() {
                return powerFor;
            }

            public String getPowerTo() {
                return powerTo;
            }

            public String getShowFor() {
                return showFor;
            }

            public String getShowTo() {
                return showTo;
            }

            public String getStuckFor() {
                return stuckFor;
            }

            public String getStuckTo() {
                return stuckTo;
            }

            public String getTagFor() {
                return tagFor;
            }

            public String getTagTo() {
                return tagTo;
            }

            public String getSetHomeFor() {
                return setHomeFor;
            }

            public String getSetHomeTo() {
                return setHomeTo;
            }

            public String getDelHomeFor() {
                return delHomeFor;
            }

            public String getDelHomeTo() {
                return delHomeTo;
            }

            public String getBoomFor() {
                return boomFor;
            }

            public String getBoomTo() {
                return boomTo;
            }

            public String getSetWarpFor() {
                return setWarpFor;
            }

            public String getSetWarpTo() {
                return setWarpTo;
            }

            public String getDelWarpFor() {
                return delWarpFor;
            }

            public String getDelWarpTo() {
                return delWarpTo;
            }

            public String getDescriptionFor() {
                return descriptionFor;
            }

            public String getDescriptionTo() {
                return descriptionTo;
            }

            public String getUpgradeFor() {
                return upgradeFor;
            }

            public String getUpgradeTo() {
                return upgradeTo;
            }

            public String getUnclaimFor() {
                return unclaimFor;
            }

            public String getUnclaimTo() {
                return unclaimTo;
            }

            public String getUnclaimAllFor() {
                return unclaimAllFor;
            }

            public String getUnclaimAllTo() {
                return unclaimAllTo;
            }

            private String leaveTo = "to leave your faction";
            private String leaveFor = "for leaving your faction";
            private String claimTo = "to claim this land";
            private String claimFor = "for claiming this land";
            private String overclaimTo = "to overclaim this land";
            private String overclaimFor = "for over claiming this land";

            public String getLeaveTo() {
                return leaveTo;
            }

            public String getLeaveFor() {
                return leaveFor;
            }

            public String getClaimTo() {
                return claimTo;
            }

            public String getClaimFor() {
                return claimFor;
            }

            public String getOverclaimTo() {
                return overclaimTo;
            }

            public String getOverclaimFor() {
                return overclaimFor;
            }
        }

        public static class Modification {
            @Comment("Example: You gained $30 for unclaiming land\n" +
                    "Supports <you>, <amount>, <for>")
            private String gainSuccess = "<info><you> gained <focus><amount></focus> <for>.";
            @Comment("Supports <you>, <amount>, <for>")
            private String gainFailure = "<info><you> would have gained <focus><amount></focus> <for>, but the deposit failed.";
            @Comment("Supports <you>, <amount>, <for>")
            private String lossSuccess = "<info><you> lost <focus><amount></focus> <for>.";
            @Comment("Supports <you>, <amount>, <to>")
            private String lossFailure = "<info><you> cannot afford <focus><amount></focus> <to>.";

            private String you = "You";
            private String yourFaction = "Your faction";

            public String getGainSuccess() {
                return gainSuccess;
            }

            public String getGainFailure() {
                return gainFailure;
            }

            public String getLossSuccess() {
                return lossSuccess;
            }

            public String getLossFailure() {
                return lossFailure;
            }

            public String getYou() {
                return you;
            }

            public String getYourFaction() {
                return yourFaction;
            }
        }

        public static class Transfer {
            private String format = "###,###.###";
            @Comment("Supports <entity> (faction/player name), <amount> (money string)")
            private String balance = "<info><entity>'s balance is <focus><amount></focus>.";
            @Comment("Supports <you>, <target>")
            private String noPerm = "<deny><you> lacks permission to control <target>'s money.";
            private String disabled = "Faction econ is disabled.";
            @Comment("Supports <from>, <amount>, <to>")
            private String cantAffordTransfer = "<deny><from> can't afford to transfer <denyfocus><amount></denyfocus> to <to>.";
            @Comment("Supports <amount>")
            private String overBalCap = "<deny>The amount <denyfocus><amount></denyfocus> is over Essentials' balance cap.";
            @Comment("Supports <amount>, <to>, <from>")
            private String transferUnable = "<deny>Unable to transfer <denyfocus><amount></denyfocus> to <denyfocus><to></denyfocus> from <from>.";
            @Comment("Supports <from>, <amount>, <to>")
            private String transferGave = "<info><from> gave <focus><amount></focus> to <to>.";
            @Comment("Supports <to>, <amount>, <from>")
            private String transferTook = "<info><to> took <focus><amount></focus> from <from>.";
            @Comment("Supports <invoker>, <amount>, <from>, <to>")
            private String transferTransfer = "<info><invoker> transferred <focus><amount></focus> from <from> to <to>.";
            @Comment("Supports <thing> (action noun like 'balance', 'value', 'refund')")
            private String off = "no <thing>";
            @Comment("Supports <entity> (name), <amount> (money), <action> (to do this)")
            private String cantAffordAmount = "<deny><entity> can't afford <denyfocus><amount></denyfocus> <action>.";

            public String getFormat() {
                return format;
            }

            public String getBalance() {
                return balance;
            }

            public String getNoPerm() {
                return noPerm;
            }

            public String getDisabled() {
                return disabled;
            }

            public String getCantAffordTransfer() {
                return cantAffordTransfer;
            }

            public String getOverBalCap() {
                return overBalCap;
            }

            public String getTransferUnable() {
                return transferUnable;
            }

            public String getTransferGave() {
                return transferGave;
            }

            public String getTransferTook() {
                return transferTook;
            }

            public String getTransferTransfer() {
                return transferTransfer;
            }

            public String getOff() {
                return off;
            }

            public String getCantAffordAmount() {
                return cantAffordAmount;
            }
        }

        private Actions actions = new Actions();
        private Modification modification = new Modification();
        private Transfer transfer = new Transfer();

        public Actions actions() {
            return actions;
        }

        public Modification modification() {
            return modification;
        }

        public Transfer transfer() {
            return transfer;
        }
    }

    public static class Permissions {
        public static class Selectors {
            public static class BasicSelector {
                private String displayName;

                protected BasicSelector(String displayName) {
                    this.displayName = displayName;
                }

                public String getDisplayName() {
                    return displayName;
                }
            }

            public static class All extends BasicSelector {
                private String displayValue = "Yes";

                public All() {
                    super("Match all");
                }

                public String getDisplayValue() {
                    return displayValue;
                }
            }

            public static class Faction extends BasicSelector {
                @Comment("Supports <lastknown>")
                private String disbandedValue = "Disbanded! (<lastknown>)";

                private String instructions = "Provide a faction tag, like faction:CoolKids";

                protected Faction() {
                    super("Faction");
                }

                public String getDisbandedValue() {
                    return disbandedValue;
                }

                public String getInstructions() {
                    return instructions;
                }
            }

            public static class Player extends BasicSelector {
                @Comment("Used if the player is not in the Factions database (pruned?)\n" +
                        "Supports <uuid>")
                private String uuidValue = "<uuid>";

                private String instructions = "Provide a player name or UUID, like player:Trent";

                protected Player() {
                    super("Player");
                }

                public String getInstructions() {
                    return instructions;
                }

                public String getUuidValue() {
                    return uuidValue;
                }
            }

            public static class RelationAtLeast extends BasicSelector {
                protected RelationAtLeast() {
                    super("Relation at least");
                }
            }

            public static class RelationAtMost extends BasicSelector {
                protected RelationAtMost() {
                    super("Relation at most");
                }
            }

            public static class RelationSingle extends BasicSelector {
                protected RelationSingle() {
                    super("Relation");
                }
            }

            public static class RoleAtLeast extends BasicSelector {
                protected RoleAtLeast() {
                    super("Role at least");
                }
            }

            public static class RoleAtMost extends BasicSelector {
                protected RoleAtMost() {
                    super("Role at Most");
                }
            }

            public static class RoleSingle extends BasicSelector {
                protected RoleSingle() {
                    super("Role");
                }
            }

            public static class Unknown extends BasicSelector {
                protected Unknown() {
                    super("Unknown");
                }
            }

            private All all = new All();
            private Faction faction = new Faction();
            private Player player = new Player();
            private RelationAtLeast relationAtLeast = new RelationAtLeast();
            private RelationAtMost relationAtMost = new RelationAtMost();
            private RelationSingle relationSingle = new RelationSingle();
            private RoleAtLeast roleAtLeast = new RoleAtLeast();
            private RoleAtMost roleAtMost = new RoleAtMost();
            private RoleSingle roleSingle = new RoleSingle();
            private Unknown unknown = new Unknown();

            public All all() {
                return all;
            }

            public Faction faction() {
                return faction;
            }

            public Player player() {
                return player;
            }

            public RelationAtLeast relationAtLeast() {
                return relationAtLeast;
            }

            public RelationAtMost relationAtMost() {
                return relationAtMost;
            }

            public RelationSingle relationSingle() {
                return relationSingle;
            }

            public RoleAtLeast roleAtLeast() {
                return roleAtLeast;
            }

            public RoleAtMost roleAtMost() {
                return roleAtMost;
            }

            public RoleSingle roleSingle() {
                return roleSingle;
            }

            public Unknown unknown() {
                return unknown;
            }
        }

        private Selectors selectors = new Selectors();

        public Selectors selectors() {
            return selectors;
        }
    }

    public static class Placeholders {
        public static class DatesAndTimes {
            private String banTiming = "MM/d/yy h:ma";
            @WipeOnReload
            private transient DateTimeFormatter banTimingFormatter;

            private String factionCreationDate = "MM/d/yy h:ma";
            @WipeOnReload
            private transient DateTimeFormatter factionCreationDateFormatter;

            public String getFactionCreationDate() {
                return factionCreationDate;
            }

            public String formatFactionCreationDate(TemporalAccessor temporal) {
                if (this.factionCreationDateFormatter == null) {
                    try {
                        this.factionCreationDateFormatter = DateTimeFormatter.ofPattern(this.factionCreationDate).withZone(ZoneId.systemDefault());
                    } catch (IllegalArgumentException _) {
                        this.factionCreationDateFormatter = DateTimeFormatter.ofPattern("MM/d/yy h:ma").withZone(ZoneId.systemDefault());
                    }
                }
                return this.factionCreationDateFormatter.format(temporal);
            }

            public String getBanTiming() {
                return banTiming;
            }

            public String formatBanTiming(TemporalAccessor temporal) {
                if (this.banTimingFormatter == null) {
                    try {
                        this.banTimingFormatter = DateTimeFormatter.ofPattern(this.banTiming).withZone(ZoneId.systemDefault());
                    } catch (IllegalArgumentException _) {
                        this.banTimingFormatter = DateTimeFormatter.ofPattern("MM/d/yy h:ma").withZone(ZoneId.systemDefault());
                    }
                }
                return this.banTimingFormatter.format(temporal);
            }
        }

        public static class Shield {
            private String activeTrue = "active";
            private String activeFalse = "not active";

            @Comment("Supports <remaining>")
            private String statusTrue = "Shield active. Time remaining: <remaining>.";
            private String statusFalse = "Shield not active.";

            public String getActiveTrue() {
                return activeTrue;
            }

            public String getActiveFalse() {
                return activeFalse;
            }

            public String getStatusTrue() {
                return statusTrue;
            }

            public String getStatusFalse() {
                return statusFalse;
            }
        }

        public static class Title {
            @Comment("Supports <left_color>, <left_repeat>, <center>, <right_color>, <right_repeat>")
            private String titleMain = "<left_color><st><left_repeat></st></left_color><center><right_color><st><right_repeat></st></right_color>";
            @Comment("Supports <content>")
            private String titleCenter = "<golden>[ </golden><titling><content></titling><golden> ]</golden>";
            private String leftRepeat = " ";
            private String rightRepeat = " ";
            @WipeOnReload
            private transient TextColor leftColorColor;
            private String leftColor = "golden";
            @WipeOnReload
            private transient TextColor rightColorColor;
            private String rightColor = "golden";

            public String getTitleMain() {
                return this.titleMain;
            }

            public String getTitleCenter() {
                return this.titleCenter;
            }

            public String getLeftRepeat() {
                return leftRepeat;
            }

            public String getRightRepeat() {
                return rightRepeat;
            }

            public TextColor getLeftColor() {
                return this.leftColorColor = MainConfig.getColor(this.leftColor, this.leftColorColor, "#e6c36c");
            }

            public TextColor getRightColor() {
                return this.rightColorColor = MainConfig.getColor(this.rightColor, this.rightColorColor, "#e6c36c");
            }
        }

        public static class LastSeen {
            private String onlineText = "<positive>Online";

            private String tooRecentText = "<positive>Within the last hour";
            private int tooRecentSeconds = 3599;

            @Comment("Supports <duration>")
            private String recentText = "<info><duration> ago";
            private int recentSeconds = 432000;

            @Comment("Supports <duration>")
            private String olderText = "<negative><duration> ago";
            private String unknownText = "<deny>Unknown";

            @Comment("""
                    Round down to a number of seconds. Ideally, put in the following:
                    1     - Don't round
                    60    - Don't give seconds, just minutes/hours/days
                    3600  - Don't give minutes, just hours/days
                    86400 - Don't give hours, just days""")
            private int intervalSeconds = 3600;

            public String getOnlineText() {
                return onlineText;
            }

            public String getTooRecentText() {
                return tooRecentText;
            }

            public int getTooRecentSeconds() {
                return tooRecentSeconds;
            }

            public String getRecentText() {
                return recentText;
            }

            public int getRecentSeconds() {
                return recentSeconds;
            }

            public String getOlderText() {
                return olderText;
            }

            public String getUnknownText() {
                return unknownText;
            }

            public int getIntervalSeconds() {
                return intervalSeconds;
            }
        }

        public static class ToolTips {
            @Comment("Faction on-hover tooltip information\n" +
                    "Supports per-faction <faction:thing> placeholders")
            private List<String> faction = new ArrayList<>() {
                {
                    this.add("<faction:if_leader><info>Leader: <faction:leader>");
                    this.add("<info>Land / Power / Max Power: <focus><faction:claims_count></focus> / <focus><faction:power></focus> / <focus><faction:power_max>");
                    this.add("<info>Raidable: <faction:if_raidable><positive>Yes</faction:if_raidable><faction:if_raidable:else><negative>No");
                    this.add("<info>Online: <focus><faction:members_online_count></focus>/<focus><faction:members_total_count></focus>");
                }
            };
            @Comment("Player on-hover tooltip information\n" +
                    "Supports per-player <player:thing> placeholders")
            private List<String> player = new ArrayList<>() {
                {
                    this.add("<info>Last Seen: <focus><player:last_seen>");
                    this.add("<info>Power: <focus><player:power></focus> / <focus><player:power_max>");
                }
            };

            public List<String> getFaction() {
                return Collections.unmodifiableList(faction);
            }

            public List<String> getPlayer() {
                return Collections.unmodifiableList(player);
            }
        }

        public static class Misc {
            private String noFactionPrefix = "<info>[<focus>no-faction</focus>]";
            private String factionless = "factionless";
            private String infinity = "∞";
            private String roleName = "None";
            private String raidableTrue = "true";
            private String raidableFalse = "false";
            private String dtrFrozenTrue = "Frozen";
            private String dtrFrozenFalse = "Not frozen";
            private String dtrFrozenTimeNotFrozen = "";
            private String joinOpen = "no invitation is needed";
            private String joinInvite = "invitation is required";
            private String peaceful = "This faction is Peaceful";
            private String powerBonus = " (bonus: ";
            private String powerPenalty = " (penalty: ";
            private String online = "Online";
            private String lastSeenSuffix = " ago.";

            public String getNoFactionPrefix() {
                return noFactionPrefix;
            }

            public String getFactionless() {
                return factionless;
            }

            public String getInfinity() {
                return infinity;
            }

            public String getRoleName() {
                return roleName;
            }

            public String getRaidableTrue() {
                return raidableTrue;
            }

            public String getRaidableFalse() {
                return raidableFalse;
            }

            public String getDtrFrozenTrue() {
                return dtrFrozenTrue;
            }

            public String getDtrFrozenFalse() {
                return dtrFrozenFalse;
            }

            public String getDtrFrozenTimeNotFrozen() {
                return dtrFrozenTimeNotFrozen;
            }

            public String getJoinOpen() {
                return joinOpen;
            }

            public String getJoinInvite() {
                return joinInvite;
            }

            public String getPeaceful() {
                return peaceful;
            }

            public String getPowerBonus() {
                return powerBonus;
            }

            public String getPowerPenalty() {
                return powerPenalty;
            }

            public String getOnline() {
                return online;
            }

            public String getLastSeenSuffix() {
                return lastSeenSuffix;
            }
        }

        private DatesAndTimes datesAndTimes = new DatesAndTimes();
        private LastSeen lastSeen = new LastSeen();
        private Shield shield = new Shield();
        private Title title = new Title();
        private ToolTips tooltips = new ToolTips();
        private Misc misc = new Misc();

        private boolean playerTitleColorContinuesIntoName = false;

        public boolean isPlayerTitleColorContinuesIntoName() {
            return playerTitleColorContinuesIntoName;
        }

        public DatesAndTimes datesAndTimes() {
            return datesAndTimes;
        }

        public LastSeen lastSeen() {
            return lastSeen;
        }

        public Shield shield() {
            return shield;
        }

        public Title title() {
            return title;
        }

        public ToolTips tooltips() {
            return tooltips;
        }

        public Misc misc() {
            return misc;
        }
    }

    public static class Protection {
        public static class Permissions {
            @Comment("Okay, so this first one isn't really a 'permission' but it's also something you're denied from doing.\n" +
                    "e.g. \"You cannot attack in a safe zone.\"")
            private String attack = "attack";
            private String ban = "Banning players from the faction";
            private String banShort = "ban";
            private String beacon = "Receiving beacon effects in faction territory";
            private String beaconShort = "receive beacon effects";
            private String build = "Building blocks";
            private String buildShort = "build";
            private String button = "Using buttons";
            private String buttonShort = "use buttons";
            private String container = "Opening any block that can store items";
            private String containerShort = "open containers";
            private String destroy = "Breaking blocks";
            private String destroyShort = "destroy";
            private String disband = "Disbanding the entire faction";
            private String disbandShort = "disband";
            private String door = "Opening doors";
            private String doorShort = "open doors";
            private String economy = "Spending faction money";
            private String economyShort = "spend faction money";
            private String fly = "Flying in faction territory";
            private String flyShort = "fly";
            private String frostWalk = "Walking on water with the frostwalk enchantment";
            private String frostWalkShort = "frostwalk";
            private String home = "Visiting the faction home";
            private String homeShort = "visit home";
            private String invite = "Inviting others to join the faction";
            private String inviteShort = "invite";
            private String item = "Using items";
            private String itemShort = "use items";
            private String kick = "Kicking members from the faction";
            private String kickShort = "kick";
            private String lever = "Using levers";
            private String leverShort = "use levers";
            private String listClaims = "View listed faction claims";
            private String listClaimsShort = "list claims";
            private String painBuild = "If allow, can build but hurts to do so";
            private String painBuildShort = "painbuild";
            private String plate = "Using pressure plates";
            private String plateShort = "use pressure plates";
            private String promote = "Promoting members of the faction";
            private String promoteShort = "promote";
            private String setHome = "Setting the faction home";
            private String setHomeShort = "set home";
            private String setWarp = "Setting and unsetting faction warps";
            private String setWarpShort = "set warps";
            private String shield = "Activating a faction shield";
            private String shieldShort = "activate shields";
            private String territory = "Claiming or unclaiming faction territory";
            private String territoryShort = "manage faction territory";
            private String tntDeposit = "Deposit TNT into faction bank";
            private String tntDepositShort = "deposit TNT";
            private String tntWithdraw = "Withdraw TNT from faction bank";
            private String tntWithdrawShort = "withdraw TNT";
            private String upgrade = "Purchasing faction upgrades";
            private String upgradeShort = "buy upgrades";
            private String warp = "Using faction warps";
            private String warpShort = "use warps";
            private String zone = "Managing faction zones";
            private String zoneShort = "manage zones";

            public String getAttack() {
                return attack;
            }

            public String getBan() {
                return ban;
            }

            public String getBanShort() {
                return banShort;
            }

            public String getBeacon() {
                return beacon;
            }

            public String getBeaconShort() {
                return beaconShort;
            }

            public String getBuild() {
                return build;
            }

            public String getBuildShort() {
                return buildShort;
            }

            public String getButton() {
                return button;
            }

            public String getButtonShort() {
                return buttonShort;
            }

            public String getContainer() {
                return container;
            }

            public String getContainerShort() {
                return containerShort;
            }

            public String getDestroy() {
                return destroy;
            }

            public String getDestroyShort() {
                return destroyShort;
            }

            public String getDisband() {
                return disband;
            }

            public String getDisbandShort() {
                return disbandShort;
            }

            public String getDoor() {
                return door;
            }

            public String getDoorShort() {
                return doorShort;
            }

            public String getEconomy() {
                return economy;
            }

            public String getEconomyShort() {
                return economyShort;
            }

            public String getFly() {
                return fly;
            }

            public String getFlyShort() {
                return flyShort;
            }

            public String getFrostWalk() {
                return frostWalk;
            }

            public String getFrostWalkShort() {
                return frostWalkShort;
            }

            public String getHome() {
                return home;
            }

            public String getHomeShort() {
                return homeShort;
            }

            public String getInvite() {
                return invite;
            }

            public String getInviteShort() {
                return inviteShort;
            }

            public String getItem() {
                return item;
            }

            public String getItemShort() {
                return itemShort;
            }

            public String getKick() {
                return kick;
            }

            public String getKickShort() {
                return kickShort;
            }

            public String getLever() {
                return lever;
            }

            public String getLeverShort() {
                return leverShort;
            }

            public String getListClaims() {
                return listClaims;
            }

            public String getListClaimsShort() {
                return listClaimsShort;
            }

            public String getPainBuild() {
                return painBuild;
            }

            public String getPainBuildShort() {
                return painBuildShort;
            }

            public String getPlate() {
                return plate;
            }

            public String getPlateShort() {
                return plateShort;
            }

            public String getPromote() {
                return promote;
            }

            public String getPromoteShort() {
                return promoteShort;
            }

            public String getSetHome() {
                return setHome;
            }

            public String getSetHomeShort() {
                return setHomeShort;
            }

            public String getSetWarp() {
                return setWarp;
            }

            public String getSetWarpShort() {
                return setWarpShort;
            }

            public String getShield() {
                return shield;
            }

            public String getShieldShort() {
                return shieldShort;
            }

            public String getTerritory() {
                return territory;
            }

            public String getTerritoryShort() {
                return territoryShort;
            }

            public String getTntDeposit() {
                return tntDeposit;
            }

            public String getTntDepositShort() {
                return tntDepositShort;
            }

            public String getTntWithdraw() {
                return tntWithdraw;
            }

            public String getTntWithdrawShort() {
                return tntWithdrawShort;
            }

            public String getUpgrade() {
                return upgrade;
            }

            public String getUpgradeShort() {
                return upgradeShort;
            }

            public String getWarp() {
                return warp;
            }

            public String getWarpShort() {
                return warpShort;
            }

            public String getZone() {
                return zone;
            }

            public String getZoneShort() {
                return zoneShort;
            }
        }

        public static class Denied {
            @Comment("Supports <action>")
            private String actionWilderness = "<deny>You cannot <action> in the wilderness.";
            @Comment("Supports <action>")
            private String actionSafezone = "<deny>You cannot <action> in a safe zone.";
            @Comment("Supports <action>")
            private String actionWarzone = "<deny>You cannot <action> in a war zone.";
            @Comment("Supports <action>, <faction>")
            private String actionTerritory = "<deny>You cannot <action> in the territory of <faction>.";
            @Comment("Supports <action>, <faction>")
            private String actionTerritoryPain = "<negative>It is painful to <action> in the territory of <faction>.";
            @Comment("Supports <faction>, <action>")
            private String actionGeneric = "<deny><faction> does not permit you to <action>.";

            @Comment("Supports <seconds>")
            private String pvpLogin = "<info>You cannot hurt other players for <seconds> seconds after logging in.";
            private String pvpRequireFaction = "<info>You cannot hurt other players until you join a faction.";
            private String pvpFactionless = "<info>You cannot hurt players who are not currently in a faction.";
            private String pvpPeaceful = "<info><fuuid:color:peaceful>Peaceful</fuuid:color:peaceful> players cannot participate in combat.";
            private String pvpNeutral = "<info>You cannot hurt <fuuid:color:relation:neutral>neutral</fuuid:color:relation:neutral> factions. Declare them as an <fuuid:color:relation:enemy>enemy</fuuid:color:relation:enemy>.";
            @Comment("Supports <target>")
            private String pvpCantHurt = "<info>You cannot hurt <target>.";
            @Comment("Supports <target>")
            private String pvpNeutralFail = "<info>You cannot hurt <target> in their own territory unless you declare them as an <fuuid:color:relation:enemy>enemy</fuuid:color:relation:enemy>.";
            @Comment("Supports <attacker>")
            private String pvpTried = "<info><attacker> tried to hurt you.";
            private String pvpPeacefulTerritory = "<info>You may not harm other players in <fuuid:color:peaceful>peaceful</fuuid:color:peaceful> territory.";
            private String pvpSafezone = "<info>You may not harm other players in a <fuuid:color:safezone>safe zone</fuuid:color:peaceful>.";

            @Comment("Supports <thing>")
            private String useWilderness = "<deny>You cannot use <denyfocus><thing></denyfocus> in the wilderness.";
            @Comment("Supports <thing>")
            private String useSafezone = "<deny>You cannot use <denyfocus><thing></denyfocus> in a safe zone.";
            @Comment("Supports <thing>")
            private String useWarzone = "<deny>You cannot use <denyfocus><thing></denyfocus> in a war zone.";
            @Comment("Supports <thing>, <faction>")
            private String useTerritory = "<deny>You cannot use <denyfocus><thing></denyfocus> in the territory of <faction>.";
            @Comment("When not possible to display a more specific description, message like \"You cannot use this\" instead occurs.")
            private String useThis = "this";

            private String interactionSpamHurtOuch = "Ouch, that is starting to hurt. You should give it a rest.";

            public String getActionWilderness() {
                return actionWilderness;
            }

            public String getActionSafezone() {
                return actionSafezone;
            }

            public String getActionWarzone() {
                return actionWarzone;
            }

            public String getActionTerritory() {
                return actionTerritory;
            }

            public String getActionTerritoryPain() {
                return actionTerritoryPain;
            }

            public String getActionGeneric() {
                return actionGeneric;
            }

            public String getPvpLogin() {
                return pvpLogin;
            }

            public String getPvpRequireFaction() {
                return pvpRequireFaction;
            }

            public String getPvpFactionless() {
                return pvpFactionless;
            }

            public String getPvpPeaceful() {
                return pvpPeaceful;
            }

            public String getPvpNeutral() {
                return pvpNeutral;
            }

            public String getPvpCantHurt() {
                return pvpCantHurt;
            }

            public String getPvpNeutralFail() {
                return pvpNeutralFail;
            }

            public String getPvpTried() {
                return pvpTried;
            }

            public String getPvpPeacefulTerritory() {
                return pvpPeacefulTerritory;
            }

            public String getPvpSafezone() {
                return pvpSafezone;
            }

            public String getUseWilderness() {
                return useWilderness;
            }

            public String getUseSafezone() {
                return useSafezone;
            }

            public String getUseWarzone() {
                return useWarzone;
            }

            public String getUseTerritory() {
                return useTerritory;
            }

            public String getUseThis() {
                return useThis;
            }

            public String getInteractionSpamHurtOuch() {
                return interactionSpamHurtOuch;
            }
        }

        private Denied denied = new Denied();
        private Permissions permissions = new Permissions();

        public Permissions permissions() {
            return this.permissions;
        }

        public Denied denied() {
            return this.denied;
        }
    }

    public static class Scoreboard {
        public static class Constant {

            @Comment("Supports <faction:relation_color>, <faction:name>")
            private String prefixTemplate = "<faction:relation_color>[<faction:name>] </faction:relation_color> ";
            @Comment("Supports <faction:relation_color>, <faction:name>")
            private String suffixTemplate = " <faction:relation_color>[<faction:name>]";

            private List<String> normalContent = new ArrayList<>() {
                {
                    this.add("<golden>Your Faction");
                    this.add("<fuuid:color:relation:member><faction:name>");
                    this.add("<info>Your Power");
                    this.add("<focus><player:power>");
                }
            };

            @Comment("Can use any placeholders, but does not update once set")
            private String normalTitle = "Faction Status";

            private List<String> factionlessContent = new ArrayList<>() {
                {
                    this.add("<golden>Not in a faction");
                    this.add("");
                    this.add("<info>Join a faction");
                    this.add("<info>  or make your own!");
                }
            };

            private String factionlessTitle = "Status";

            public String getNormalTitle() {
                return normalTitle;
            }

            public String getPrefixTemplate() {
                return prefixTemplate;
            }

            public String getSuffixTemplate() {
                return suffixTemplate;
            }

            public List<String> getNormalContent() {
                return normalContent != null ? Collections.unmodifiableList(normalContent) : Collections.emptyList();
            }

            public List<String> getFactionlessContent() {
                return factionlessContent != null ? Collections.unmodifiableList(factionlessContent) : Collections.emptyList();
            }

            public String getFactionlessTitle() {
                return factionlessTitle;
            }
        }

        public static class Info {
            @Comment("Supports <player>, <faction>")
            private List<String> content = new ArrayList<>() {
                {
                    this.add("<info>Power");
                    this.add("<faction:power>");
                    this.add("<info>Members");
                    this.add("<faction:members_online_count>/<faction:members_total_count>");
                    this.add("<info>Leader");
                    this.add("<faction:leader>");
                    this.add("<info>Territory");
                    this.add("<faction:claims_count>");
                }
            };
            @Comment("Supports <faction>")
            private String title = "<faction>";

            public List<String> getContent() {
                return content != null ? Collections.unmodifiableList(content) : Collections.emptyList();
            }

            public String getTitle() {
                return title;
            }
        }

        @Comment("Constant scoreboard stays around all the time, displaying status info.\n" +
                "Also, if prefixes are enabled while it is enabled, will show prefixes on nametags and tab")
        private Scoreboard.Constant constant = new Scoreboard.Constant();
        @Comment("Info scoreboard is displayed when a player walks into a new Faction's territory.\n" +
                "Scoreboard disappears after <expiration> seconds.")
        private Scoreboard.Info info = new Scoreboard.Info();

        public Scoreboard.Constant constant() {
            return constant;
        }

        public Scoreboard.Info info() {
            return info;
        }
    }

    public static class Upgrades {
        public static class UpgradeDetail {
            private UpgradeDetail(String name, String description, String detail) {
                this.name = name;
                this.description = description;
                this.detail = detail;
            }

            private String name;
            private String description;
            private String detail;

            public String getName() {
                return name;
            }

            public String getDescription() {
                return description;
            }

            public String getDetail() {
                return detail;
            }
        }

        private UpgradeDetail armorDurability = new UpgradeDetail("<titling>Armor Durability Protection", "<titling>Armor and shields lose durability slower while in your territory", "<titling>-<percent>% durability loss for members and allies");

        private UpgradeDetail beaconEffectControl = new UpgradeDetail("<titling>Beacon Effect Control", "<titling>Control who receives beacon effects in your territory", "");

        private UpgradeDetail cropYield = new UpgradeDetail("<titling>Crop Yield Boost", "<titling>Harvesting grown crops in your territory has a chance to drop extra", "<titling><chance>% chance to multiply harvest by <increase>");

        private UpgradeDetail dtrClaimLimit = new UpgradeDetail("<titling>Claim Limit Increase", "<titling>Increases maximum faction territory", "<titling>+<increase> claims");

        private UpgradeDetail dtrLossReduction = new UpgradeDetail("<titling>DTR Loss Reduction", "<titling>Lose less DTR when members die", "<titling>-<percent>% DTR lost on death");

        private UpgradeDetail dtrRegen = new UpgradeDetail("<titling>DTR Regeneration Boost", "<titling>Regain DTR more quickly", "<titling>+<percent>% DTR regeneration");

        private UpgradeDetail fallDamage = new UpgradeDetail("<titling>Fall Damage Reduction", "<titling>Decreases fall damage in your own territory", "<titling>-<percent>% reduction");

        private UpgradeDetail flight = new UpgradeDetail("<titling>Flight", "<titling>Enables flying in faction territory", "");

        private UpgradeDetail growth = new UpgradeDetail("<titling>Growth", "<titling>Boosts plant growth in faction land", "<titling><chance>% chance to grow <boost> extra step");

        private UpgradeDetail maxMembers = new UpgradeDetail("<titling>Member Limit Increase", "<titling>Increases maximum number of faction members", "<titling>+<increase> members");

        private UpgradeDetail mobDrop = new UpgradeDetail("<titling>Mob Drop Boost", "<titling>Mobs killed in your territory have a chance to drop extra loot", "<titling><chance>% chance to multiply drops by <increase>");

        private UpgradeDetail mobExp = new UpgradeDetail("<titling>Mob Experience Boost", "<titling>Gain extra experience from mobs killed in your territory", "<titling>+<percent>% experience");

        private UpgradeDetail noHunger = new UpgradeDetail("<titling>No Hunger Loss", "<titling>Members do not lose hunger while in their own territory", "");

        private UpgradeDetail powerLossReduction = new UpgradeDetail("<titling>Power Loss Reduction", "<titling>Lose less power when members die", "<titling>-<percent>% power lost on death");

        private UpgradeDetail powerMax = new UpgradeDetail("<titling>Maximum Power Limit Increase", "<titling>Increases the maximum limit on faction power", "<titling>+<increase> power");

        private UpgradeDetail powerRegen = new UpgradeDetail("<titling>Power Regeneration Boost", "<titling>Members regain power more quickly", "<titling>+<percent>% power regeneration");

        private UpgradeDetail redstoneAntiFlood = new UpgradeDetail("<titling>Redstone Anti-Flood", "<titling>Protect circuits from flooding", "");

        private UpgradeDetail shield = new UpgradeDetail("<titling>Shield", "<titling>Protect territory from explosions", "<titling><duration> shield, cooldown <cooldown>");

        private UpgradeDetail spawnerRate = new UpgradeDetail("<titling>Spawner Spawn Rate", "<titling>Spawners in your territory spawn more often", "<titling>-<percent>% spawner delay");

        private UpgradeDetail territoryDamageBoost = new UpgradeDetail("<titling>Territory Damage Boost", "<titling>Deal extra damage in your territory", "<titling>+<percent>% damage from members and allies");

        private UpgradeDetail territoryDamageResistance = new UpgradeDetail("<titling>Territory Damage Resistance", "<titling>Take less damage from players in your territory", "<titling>-<percent>% damage taken by members and allies");

        private UpgradeDetail tntBankFill = new UpgradeDetail("<titling>TNT Bank Passive Fill", "<titling>Adds free TNT into your faction's bank over time", "<titling>+<increase> TNT per minute");

        private UpgradeDetail tntBankCapacity = new UpgradeDetail("<titling>TNT Bank", "<titling>Increases the maximum TNT your faction's TNT bank can hold", "<green><count> TNT capacity");

        private UpgradeDetail vaults = new UpgradeDetail("<green>Vaults", "<green>Increases the number of faction vaults", "<green>+<increase> vaults");

        private UpgradeDetail warps = new UpgradeDetail("<green>Warps", "<green>Additional locations to which members can teleport", "<green><count> warps");

        private UpgradeDetail zones = new UpgradeDetail("<green>Zones", "<green>Assign your faction claims to zones to label or grant different permissions", "<green>Grants <increase> zones");

        public UpgradeDetail armorDurability() {
            return this.armorDurability;
        }

        public UpgradeDetail beaconEffectControl() {
            return this.beaconEffectControl;
        }

        public UpgradeDetail cropYield() {
            return this.cropYield;
        }

        public UpgradeDetail dtrClaimLimit() {
            return this.dtrClaimLimit;
        }

        public UpgradeDetail fallDamage() {
            return this.fallDamage;
        }

        public UpgradeDetail flight() {
            return this.flight;
        }

        public UpgradeDetail growth() {
            return this.growth;
        }

        public UpgradeDetail maxMembers() {
            return this.maxMembers;
        }

        public UpgradeDetail powerMax() {
            return this.powerMax;
        }

        public UpgradeDetail redstoneAntiFlood() {
            return this.redstoneAntiFlood;
        }

        public UpgradeDetail shield() {
            return this.shield;
        }

        public UpgradeDetail warps() {
            return this.warps;
        }

        public UpgradeDetail zones() {
            return this.zones;
        }

        public UpgradeDetail spawnerRate() {
            return this.spawnerRate;
        }

        public UpgradeDetail territoryDamageBoost() {
            return this.territoryDamageBoost;
        }

        public UpgradeDetail territoryDamageResistance() {
            return this.territoryDamageResistance;
        }

        public UpgradeDetail powerLossReduction() {
            return this.powerLossReduction;
        }

        public UpgradeDetail powerRegen() {
            return this.powerRegen;
        }

        public UpgradeDetail dtrLossReduction() {
            return this.dtrLossReduction;
        }

        public UpgradeDetail dtrRegen() {
            return this.dtrRegen;
        }

        public UpgradeDetail mobDrop() {
            return this.mobDrop;
        }

        public UpgradeDetail mobExp() {
            return this.mobExp;
        }

        public UpgradeDetail noHunger() {
            return this.noHunger;
        }

        public UpgradeDetail tntBankCapacity() {
            return this.tntBankCapacity;
        }

        public UpgradeDetail tntBankFill() {
            return this.tntBankFill;
        }

        public UpgradeDetail vaults() {
            return this.vaults;
        }

        private String unlimited = "unlimited";

        public String getUnlimited() {
            return this.unlimited;
        }
    }

    public static class FactionEvents {
        public static class SpecialFactions {
            private String wildernessTag = "Wilderness";
            private String wildernessDescription = "";
            private String safeZoneTag = "Safezone";
            private String safeZoneDescription = "Free from pvp and monsters.";
            private String warZoneTag = "Warzone";
            private String warZoneDescription = "Not the safest place to be.";

            public String getWildernessTag() {
                return wildernessTag;
            }

            public String getWildernessDescription() {
                return wildernessDescription;
            }

            public String getSafeZoneTag() {
                return safeZoneTag;
            }

            public String getSafeZoneDescription() {
                return safeZoneDescription;
            }

            public String getWarZoneTag() {
                return warZoneTag;
            }

            public String getWarZoneDescription() {
                return warZoneDescription;
            }
        }

        @Comment("Supports <player>")
        private String login = "<info><player> logged in.";
        @Comment("Supports <player>")
        private String logout = "<info><player> logged out.";
        private String announcementTop = "<info>--Unread Faction Announcements--";
        private String announcementBottom = "<info>--Unread Faction Announcements--";
        private String homeUnset = "<negative>Your faction home has been un-set since it is no longer in your territory.";
        @Comment("Supports <old_leader>, <new_leader> as just strings")
        private String newLeader = "<info>Faction admin <focus><old_leader></focus> has been removed. <focus><new_leader></focus> has been promoted as the new faction admin.";
        private String defaultDescription = "Default faction description :(";
        @Comment("Supports <relation>")
        private String teleportedOnJoin = "<info>You were teleported out of <relation> territory.";
        private String portalNotAllowed = "<deny>Destination portal can't be created there.";
        @Comment("Supports <faction> (the faction that became raidable)")
        private String raidableNow = "<negative><faction> is now raidable!";
        @Comment("Supports <faction> (the faction that is no longer raidable)")
        private String raidableNoLonger = "<negative><faction> is no longer raidable!";
        private SpecialFactions specialFactions = new SpecialFactions();

        public String getLogin() {
            return login;
        }

        public String getLogout() {
            return logout;
        }

        public String getAnnouncementTop() {
            return announcementTop;
        }

        public String getAnnouncementBottom() {
            return announcementBottom;
        }

        public String getHomeUnset() {
            return homeUnset;
        }

        public String getNewLeader() {
            return newLeader;
        }

        public String getDefaultDescription() {
            return defaultDescription;
        }

        public String getTeleportedOnJoin() {
            return teleportedOnJoin;
        }

        public String getPortalNotAllowed() {
            return portalNotAllowed;
        }

        public String getRaidableNow() {
            return raidableNow;
        }

        public String getRaidableNoLonger() {
            return raidableNoLonger;
        }

        public SpecialFactions specialFactions() {
            return specialFactions;
        }
    }

    public static class Claiming {
        public static class Claim {
            private String protectedLand = "<deny>This land is protected.";
            private String disabled = "<deny>This world has land claiming disabled.";
            @Comment("Supports <faction>")
            private String cantClaim = "<deny>You can't claim land for <faction>.";
            @Comment("Supports <faction>")
            private String cantUnclaim = "<deny>You can't unclaim land for <faction>.";
            @Comment("Supports <faction>")
            private String alreadyOwn = "<deny><faction> already own this land.";
            @Comment("Supports <count>")
            private String members = "<deny>Factions must have at least <denyfocus><count></denyfocus> members to claim land.";
            private String safeZone = "<deny>You can not claim a safe zone.";
            private String warZone = "<deny>You can not claim a war zone.";
            private String power = "<deny>You can't claim more land! You need more power!";
            private String dtrLand = "<deny>You can't claim more land!";
            private String limit = "<deny>Limit reached. You can't claim more land!";
            private String ally = "<deny>You can't claim the land of your allies.";
            private String contiguous = "<deny>You can only claim additional land which is connected to your first claim or controlled by another faction!";
            private String factionContiguous = "<deny>You can only claim additional land which is connected to your first claim!";
            @Comment("Supports <count> (the maximum number of chunks allowed in a connected claim)")
            private String contiguousTotalChunks = "<deny>You can't claim more than <denyfocus><count></denyfocus> chunks in one connected area!";
            @Comment("Supports <count> (the maximum X or Z distance, in chunks, allowed across a connected claim)")
            private String contiguousDistance = "<deny>Your connected claim can't span more than <denyfocus><count></denyfocus> chunks!";
            @Comment("Supports <faction> (the faction that owns the land)")
            private String peaceful = "<deny><faction> owns this land. Your faction is peaceful, so you cannot claim land from other factions.";
            @Comment("Supports <faction> (the faction that owns the land)")
            private String peacefulTarget = "<deny><faction> owns this land, and is a peaceful faction. You cannot claim land from them.";
            @Comment("Supports <faction> (the faction that owns the land)")
            private String thisIsSparta = "<deny><faction> owns this land and is strong enough to keep it.";
            private String border = "<deny>You must start claiming land at the border of the territory.";
            private String overclaimDisabled = "<info>Over claiming is disabled on this server.";
            @Comment("Supports <count>")
            private String tooCloseToOtherFaction = "<info>Your claim is too close to another Faction. Buffer required is <count>.";
            private String outsideWorldBorder = "<info>Your claim is outside the border.";
            @Comment("Supports <count>")
            private String outsideBorderBuffer = "<info>Your claim is outside the border. <count> chunks away from world edge required.";
            @Comment("Supports <player> (claimant), <faction> (claimed for), <fromfaction> (claimed from)")
            private String claimed = "<info><player> claimed land for <focus><faction></focus> from <focus><fromfaction></focus>.";
            private String youAreHere = "You are here";

            public String getProtectedLand() {
                return protectedLand;
            }

            public String getDisabled() {
                return disabled;
            }

            public String getCantClaim() {
                return cantClaim;
            }

            public String getCantUnclaim() {
                return cantUnclaim;
            }

            public String getAlreadyOwn() {
                return alreadyOwn;
            }

            public String getMembers() {
                return members;
            }

            public String getSafeZone() {
                return safeZone;
            }

            public String getWarZone() {
                return warZone;
            }

            public String getPower() {
                return power;
            }

            public String getDtrLand() {
                return dtrLand;
            }

            public String getLimit() {
                return limit;
            }

            public String getAlly() {
                return ally;
            }

            public String getContiguous() {
                return contiguous;
            }

            public String getFactionContiguous() {
                return factionContiguous;
            }

            public String getContiguousTotalChunks() {
                return contiguousTotalChunks;
            }

            public String getContiguousDistance() {
                return contiguousDistance;
            }

            public String getPeaceful() {
                return peaceful;
            }

            public String getPeacefulTarget() {
                return peacefulTarget;
            }

            public String getThisIsSparta() {
                return thisIsSparta;
            }

            public String getBorder() {
                return border;
            }

            public String getOverclaimDisabled() {
                return overclaimDisabled;
            }

            public String getTooCloseToOtherFaction() {
                return tooCloseToOtherFaction;
            }

            public String getOutsideWorldBorder() {
                return outsideWorldBorder;
            }

            public String getOutsideBorderBuffer() {
                return outsideBorderBuffer;
            }

            public String getClaimed() {
                return claimed;
            }

            public String getYouAreHere() {
                return youAreHere;
            }
        }

        public static class Unclaim {
            private String wrongFactionOther = "<deny>Attempted to unclaim land for incorrect faction.";
            private String safeZoneSuccess = "<info>Safe zone was unclaimed.";
            private String safeZoneNoPerm = "<deny>This is a safe zone. You lack permissions to unclaim.";
            private String warZoneSuccess = "<info>War zone was unclaimed.";
            private String warZoneNoPerm = "<deny>This is a war zone. You lack permissions to unclaim.";
            @Comment("Supports <player> (who unclaimed)")
            private String unclaimed = "<info><player> unclaimed some of your land.";
            private String unclaims = "<info>You unclaimed this land.";
            private String notAMember = "<deny>You are not a member of any faction.";
            private String wrongFaction = "<deny>You don't own this land.";
            @Comment("Supports <player> (who unclaimed)")
            private String factionUnclaimed = "<info><player> unclaimed some land.";

            public String getWrongFactionOther() {
                return wrongFactionOther;
            }

            public String getSafeZoneSuccess() {
                return safeZoneSuccess;
            }

            public String getSafeZoneNoPerm() {
                return safeZoneNoPerm;
            }

            public String getWarZoneSuccess() {
                return warZoneSuccess;
            }

            public String getWarZoneNoPerm() {
                return warZoneNoPerm;
            }

            public String getUnclaimed() {
                return unclaimed;
            }

            public String getUnclaims() {
                return unclaims;
            }

            public String getNotAMember() {
                return notAMember;
            }

            public String getWrongFaction() {
                return wrongFaction;
            }

            public String getFactionUnclaimed() {
                return factionUnclaimed;
            }
        }

        private Claim claim = new Claim();
        private Unclaim unclaim = new Unclaim();

        public Claim claim() {
            return claim;
        }

        public Unclaim unclaim() {
            return unclaim;
        }
    }

    public static class LandRaid {
        public static class Power {
            private String noPowerLossRegion = "<info>You didn't lose any power due to the region you were in.";
            private String noPowerLossWarzone = "<info>You didn't lose any power since you were in a war zone.";
            @Comment("Supports <power>, <maxpower>")
            private String powerLossWarzone = "<negative>The world you are in has power loss normally disabled, but you still lost power since you were in a war zone.\n<info>Your power is now <negativefocus><power> / <maxpower></negativefocus>.";
            private String noPowerLossWilderness = "<info>You didn't lose any power since you were in the wilderness.";
            private String noPowerLossWorld = "<info>You didn't lose any power due to the world you died in.";
            private String noPowerLossPeaceful = "<info>You didn't lose any power since you are in a peaceful faction.";
            @Comment("Supports <power>, <maxpower>")
            private String powerNow = "<info>Your power is now <focus><power> / <maxpower></focus>.";
            @Comment("Supports <amount>, <player>, <power>, <maxpower>")
            private String vampirismGain = "<info>Stole <focus><amount></focus> power from <player>. Your power is now <focus><power> / <maxpower></focus>.";

            public String getNoPowerLossRegion() {
                return noPowerLossRegion;
            }

            public String getNoPowerLossWarzone() {
                return noPowerLossWarzone;
            }

            public String getPowerLossWarzone() {
                return powerLossWarzone;
            }

            public String getNoPowerLossWilderness() {
                return noPowerLossWilderness;
            }

            public String getNoPowerLossWorld() {
                return noPowerLossWorld;
            }

            public String getNoPowerLossPeaceful() {
                return noPowerLossPeaceful;
            }

            public String getPowerNow() {
                return powerNow;
            }

            public String getVampirismGain() {
                return vampirismGain;
            }
        }

        public static class DTR {
            private String cannotFrozen = "<deny>Action denied due to frozen DTR.";
            private String kickPenalty = "<negative>Penalty DTR lost due to kicking with frozen DTR.";
            @Comment("Supports <amount>, <player>, <dtr>")
            private String vampirismGain = "<info>Stole <focus><amount></focus> DTR from <player>. Your DTR is now <focus><dtr></focus>.";

            public String getCannotFrozen() {
                return cannotFrozen;
            }

            public String getKickPenalty() {
                return kickPenalty;
            }

            public String getVampirismGain() {
                return vampirismGain;
            }
        }

        private Power power = new Power();
        private DTR dtr = new DTR();

        public Power power() {
            return power;
        }

        public DTR dtr() {
            return dtr;
        }
    }

    @Comment("This config file will slowly become the location for all text content\n" +
            "All information here uses MiniMessage. https://docs.papermc.io/adventure/minimessage/")
    private AColorfulMessage aColorfulMessage = new AColorfulMessage();
    private Commands commands = new Commands();
    private General general = new General();
    private Economy economy = new Economy();
    private Permissions permissions = new Permissions();
    private Placeholders placeholders = new Placeholders();
    private Protection protection = new Protection();
    private Scoreboard scoreboard = new Scoreboard();
    private Upgrades upgrades = new Upgrades();
    private FactionEvents factionEvents = new FactionEvents();
    private Claiming claiming = new Claiming();
    private LandRaid landRaid = new LandRaid();

    public AColorfulMessage colors() {
        return aColorfulMessage;
    }

    public Commands commands() {
        return this.commands;
    }

    public Economy economy() {
        return economy;
    }

    public General general() {
        return general;
    }

    public Permissions permissions() {
        return this.permissions;
    }

    public Placeholders placeholders() {
        return this.placeholders;
    }

    public Protection protection() {
        return this.protection;
    }

    public Scoreboard scoreboard() {
        return this.scoreboard;
    }

    public Upgrades upgrades() {
        return this.upgrades;
    }

    public FactionEvents factionEvents() {
        return factionEvents;
    }

    public Claiming claiming() {
        return claiming;
    }

    public LandRaid landRaid() {
        return landRaid;
    }
}
