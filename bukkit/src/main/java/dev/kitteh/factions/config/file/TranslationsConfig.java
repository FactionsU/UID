package dev.kitteh.factions.config.file;

import dev.kitteh.factions.config.annotation.Comment;
import dev.kitteh.factions.config.annotation.WipeOnReload;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
public class TranslationsConfig {
    public static class AbsCommand {
        private List<String> aliases;

        protected AbsCommand(String firstAlias, String... aliases) {
            this.aliases = new ArrayList<>();
            this.aliases.add(firstAlias);
            this.aliases.addAll(Arrays.asList(aliases));
        }

        public List<String> getAliases() {
            return List.copyOf(this.aliases);
        }

        public String getFirstAlias() {
            return this.aliases.getFirst();
        }

        public String[] getSecondaryAliases() {
            List<String> secondaries = new ArrayList<>(this.aliases);
            secondaries.removeFirst();
            return secondaries.toArray(new String[0]);
        }
    }

    public static class Commands {
        public static class Generic {
            private AbsCommand commandRoot = new AbsCommand("f");
            private AbsCommand commandAdminRoot = new AbsCommand("fa");

            private String noFactionFound = "No faction found for input '<input>'";
            private String noPlayerFound = "No player found for input '<input>'";

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

        public static class Chat extends AbsCommand {
            protected Chat() {
                super("chat");
            }
        }

        public static class Confirm extends AbsCommand {
            public Confirm() {
                super("confirm");
            }

            private String invalid = "<red>Invalid confirmation code.";
            private String notFound = "<red>No confirmation found.";

            public String getInvalid() {
                return invalid;
            }

            public String getNotFound() {
                return notFound;
            }
        }

        public static class ListCmd extends AbsCommand {
            public ListCmd() {
                super("list");
            }

            public static class ListBans extends AbsCommand {
                public ListBans() {
                    super("bans");
                }
            }

            public static class ListClaims extends AbsCommand {
                public ListClaims() {
                    super("claims");
                }
            }

            public static class ListFactions extends AbsCommand {
                @Comment("You can also use <page_current> and <page_count> in the header.\n" +
                        "Blank entry results in nothing being displayed.")
                private String header = "<fuuid:title><dark_green>Faction List</dark_green> <gold><page_current></gold>/<gold><page_count></gold>";
                @Comment("You can also use <page_current> and <page_count> in the header.\n" +
                        "Blank entry results in nothing being displayed.")
                private String footer = "";
                @Comment("You can use per-faction <faction:thing> placeholders here")
                private String factionlessEntry = "<yellow>Factionless <faction:members_online_count> online";
                @Comment("You can use per-faction <faction:thing> placeholders here")
                private String entry = "<yellow><faction> <faction:members_online_count>/<faction:members_total_count> online, <gold>Land/Power/Max Power: <yellow><faction:claims_count></yellow>/<yellow><faction:power></yellow>/<yellow><faction:power_max>";

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
                    super("factions");
                }
            }

            public static class ListInvites extends AbsCommand {
                public ListInvites() {
                    super("invites");
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

        public static class Set extends AbsCommand {
            public Set() {
                super("set");
            }
        }

        public static class Show extends AbsCommand {
            protected Show() {
                super("show");
            }

            private List<String> normalFormat = new ArrayList<>() {
                {
                    this.add("<fuuid:title><faction:relation_color><faction:name>");
                    this.add("<gold>Description: <yellow><faction:description>");
                    this.add("<gold><faction:if_open>No invitation required</faction:if_open><faction:if_open:else>Invitation required</faction:if_open:else><faction:if_peaceful>.    <fuuid:color:peaceful>Peaceful");
                    this.add("<gold>Land / Power / Max Power: <yellow><faction:claims_count></yellow> / <yellow><faction:power></yellow> / <yellow><faction:power_max>");
                    this.add("<gold>Raidable: <faction:if_raidable><green>Yes</faction:if_raidable><faction:if_raidable:else><red>No");
                    this.add("<gold>Founded: <yellow><faction:creation_date>");
                    this.add("<faction:if_permanent><gold>This faction is permanent, remaining even with no members.");
                    this.add("<fuuid:if_economy><fuuid:if_banks><gold>Balance: <yellow><faction:bank_balance></fuuid:if_banks>     <gold>Land value: <yellow><faction:claims_value>");
                    this.add("<faction:if_allies><gold>Allies (<yellow><faction:allies_count></yellow>/<yellow><faction:allies_max></yellow>): {allies-list}");
                    this.add("<faction:if_online><gold>Online: (<yellow><faction:members_online_count></yellow>/<yellow><faction:members_total_count></yellow>)<faction:if_online>: {online-list}");
                    this.add("<faction:if_offline><gold>Offline: (<yellow><faction:members_offline_count></yellow>/<yellow><faction:members_total_count></yellow>)<faction:if_offline>: {offline-list}");
                }
            };

            private List<String> safezoneFormat = new ArrayList<>() {
                {
                    this.add("<yellow>The <fuuid:color:safezone>safezone</fuuid:color:safezone> faction is a special faction that 'owns' territory where players cannot be hurt");
                }
            };

            private List<String> warzoneFormat = new ArrayList<>() {
                {
                    this.add("<yellow>The <fuuid:color:warzone>warzone</fuuid:color:warzone> faction is a special faction that 'owns' territory where players can be hurt");
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
        }

        public static class Permissions extends AbsCommand {
            public static class SubCmdAdd extends AbsCommand {
                public SubCmdAdd() {
                    super("add");
                }

                private String availableSelectorsIntro = "Available: ";
                private String availableSelectorsSelector = "<click:run_command:\"<command>\"><color:#66ebff><selector></color:#66ebff></click>  ";

                private String selectorNotFound = "<red>No selector available with that name</red>";
                private String selectorCreateFail = "<red>Could not create selector:</red> <error>";
                private String selectorOptionHere = "OPTIONHERE";
                private String selectorOptionsIntro = "Available: ";
                private String selectorOptionsItem = "<click:run_command:\"<command>\"><color:#66ebff><display></color:#66ebff></click>  ";

                private String actionOptionsIntro = "Available: ";
                private String actionOptionsItem = "<hover:show_text:\"<description>\"><action></hover>" +
                        "<click:run_command:\"<commandtrue>\"><color:#66ffb0>+</color:#66ffb0></click>" +
                        "<click:run_command:\"<commandfalse>\"><color:#ff6666>-</color:#ff6666></click>  ";
                private String actionNotFound = "<red>No action available with that name</red>";
                private String actionAllowDenyOptions = "<red>Unrecognized choice. What about </red><allow> <red>or</red> <deny><red>?</red>";

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
                private String footer = "           <click:run_command:\"<commandadd>\"><color:#66ebff>[Add]";
                private String header = "Selectors (ranked by priority) [<click:run_command:\"<commandoverride>\"><color:#66ebff>Overrides</color:#66ebff></click>]:";

                private String item = "<hover:show_text:\"<color:#ff6666>Move up\"><click:run_command:\"<commandmoveup>\">^</click></hover> " +
                        "<hover:show_text:\"<color:#ff6666>Move down\"><click:run_command:\"<commandmovedown>\">V</click></hover> " +
                        "<hover:show_text:\"<color:#ff6666>Remove\"><click:run_command:\"<commandremove>\"><color:#ff6666>X</color:#ff6666></click></hover> " +
                        "#<rownumber> <hover:show_text:\"<color:#ff6666>Click to show actions\"><click:run_command:\"<commandshow>\"><color:#66ebff><name></color:#66ebff></click></hover>: " +
                        "<color:#66ffb0><value>";

                public SubCmdList() {
                    super("list");
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

                private String item = "#<rownumber> <hover:show_text:\"<color:#ff6666>Click to show actions\"><click:run_command:\"<commandshow>\"><color:#66ebff><name></color:#66ebff></click></hover>: " +
                        "<color:#66ffb0><value>";

                public SubCmdListOverride() {
                    super("listoverride");
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
                private String errorOptions = "<red>Unrecognized choice. What about </red><up> <red>or</red> <down><red>?</red>";
                private String errorHighest = "<red>Cannot move highest selector any higher!</red>";
                private String errorLowest = "<red>Cannot move lowest selector any lower!</red>";
                private String errorInvalidPositon = "<red>Cannot move invalid position!</red>";

                public SubCmdMove() {
                    super("move");
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
                    super("remove");
                }
            }

            public static class SubCmdReset extends AbsCommand {
                private String warning = "<#ff6666>Warning:</#ff6666> Are you sure you wish to reset all permissions? <click:run_command:\"<command>\"><#ff6666>[CONFIRM]</#ff6666></click>";
                private String resetComplete = "<color:#66ebff>Permissions reset!";
                @Comment("must be a single word, to confirm resetting")
                private String confirmWord = "confirm";

                public SubCmdReset() {
                    super("reset");
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
                private String header = "#<rownumber> <color:#66ebff><name></color:#66ebff>: <color:#66ffb0><value>";
                private String footer = "   <click:run_command:\"<command>\"><color:#66ebff>[Add]";
                private String item = "<hover:show_text:\"<color:#ff6666>Remove\"><click:run_command:\"<commandremove>\"><color:#ff6666>X</color:#ff6666></click></hover> " +
                        "<hover:show_text:\"<desc>\"><color:#66ebff><shortdesc></color:#66ebff></hover>: <color:#66ffb0><state>";

                private String selectorNotFound = "<red>No selector available with that name</red>";

                public SubCmdShow() {
                    super("show");
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
                private String header = "#<rownumber> <color:#66ebff><name></color:#66ebff>:<color:#66ffb0><value>";
                private String footer = "";
                private String item = "<hover:show_text:\"<desc>\"><color:#66ebff><shortdesc></color:#66ebff></hover>:<color:#66ffb0><state>";

                private String selectorNotFound = "<red>No override selector available with that name</red>";

                public SubCmdShowOverride() {
                    super("showoverride");
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
                super("perms", "perm", "permission", "permissions");
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

        public static class Upgrades extends AbsCommand {
            protected Upgrades() {
                super("upgrades");
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
                    private String statusCurrentLevel = "Current level: <level>";

                    private String upgradeAvailable = "Upgrade available!";
                    private String upgradeAvailableCosts = "Costs <cost>";
                    private String upgradeAtMaxLevel = "Max level!";
                    private String upgradeAvailableLevelNumberIfNotSingleLevel = "Level <level>";
                    private String purchaseButton = "Purchase Upgrade";

                    public String getTitle() {
                        return title;
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
                super("warp");
            }
        }

        public static class Zone extends AbsCommand {
            public static class Claim extends AbsCommand {
                private String zoneNotFound = "<red>Zone named '<name>' not found</red>";

                private String notInTerritory = "<red>Not standing in faction territory";
                private String alreadyZone = "<red>Already standing in <green><zone></green>";
                private String cannotManage = "<red>Cannot manage zone <green><zone></green>!";
                private String success = "<yellow>Successfully set zone <green><newzone></green>!";
                private String attemptingRadius = "<yellow>Setting zone <green><zone></green> for any chunks you can update";
                private String autoSetOn = "<yellow>Automatically setting zone for <green><zone></green> as you enter chunks";
                private String autoSetOff = "<yellow>Disabled automatic zone setting";

                public Claim() {
                    super("claim");
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
                private String nameAlreadyInUse = "<red>Zone name '<name>' is already in use</red>";
                private String success = "<yellow>Created new zone '<name>'";

                public Create() {
                    super("create");
                }

                public String getNameAlreadyInUse() {
                    return nameAlreadyInUse;
                }

                public String getSuccess() {
                    return success;
                }
            }

            public static class Delete extends AbsCommand {
                private String zoneNotFound = "<red>Zone named '<name>' not found</red>";
                private String success = "<yellow>Deleted zone '<name>'";
                private String confirm = "Are you sure you want to delete zone '<name>'? If so, run /<command>";

                public Delete() {
                    super("delete");
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
                private String zoneNotFound = "<red>Zone named '<name>' not found</red>";

                public String getZoneNotFound() {
                    return zoneNotFound;
                }
            }

            public static class Set extends AbsCommand {
                public static class Greeting extends AbsCommand {
                    private String success = "<yellow>Set zone '<name>' greeting to '<greeting>'";
                    private String zoneNotFound = "<red>Zone '<name>' not found</red>";

                    protected Greeting() {
                        super("greeting");
                    }

                    public String getSuccess() {
                        return success;
                    }

                    public String getZoneNotFound() {
                        return zoneNotFound;
                    }
                }

                public static class Name extends AbsCommand {
                    private String nameAlreadyInUse = "<red>Zone name '<name>' is already in use</red>";
                    private String success = "<yellow>Set zone '<oldname>' name to '<newname>'";
                    private String zoneNotFound = "<red>Zone '<name>' not found</red>";

                    protected Name() {
                        super("name");
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
                    super("set");
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
                super("zone");
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

        private Generic generic = new Generic();

        private Chat chat = new Chat();
        private Confirm confirm = new Confirm();
        private ListCmd list = new ListCmd();
        private Set set = new Set();
        private Show show = new Show();
        private Permissions permissions = new Permissions();
        private Upgrades upgrades = new Upgrades();
        private Warp warp =  new Warp();
        private Zone zone = new Zone();

        public Generic generic() {
            return generic;
        }

        public Chat chat() {
            return chat;
        }

        public Confirm confirm() {
            return confirm;
        }

        public ListCmd list() {
            return list;
        }

        public Set set() {
            return set;
        }

        public Show show() {
            return show;
        }

        public Permissions permissions() {
            return permissions;
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
                @Comment("Used if the player is not in the Factions database (pruned?)")
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
        public static class Shield {
            private String activeTrue = "active";
            private String activeFalse = "not active";

            private String statusTrue = "Shield active. Time remaining: <remaining>";
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
            private String titleMain = "<left_color><st><left_repeat></st></left_color><center><right_color><right_repeat></right_color>";
            private String titleCenter = "<gold>.[ </gold><dark_green><content></dark_green><gold> ].</gold>";
            private String leftRepeat = "_";
            private String rightRepeat = "_";
            @WipeOnReload
            private transient TextColor leftColorColor;
            private String leftColor = "gold";
            @WipeOnReload
            private transient TextColor rightColorColor;
            private String rightColor = "gold";

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
                return this.leftColorColor = MainConfig.getColor(this.leftColor, this.leftColorColor, NamedTextColor.GOLD);
            }

            public TextColor getRightColor() {
                return this.rightColorColor = MainConfig.getColor(this.rightColor, this.rightColorColor, NamedTextColor.GOLD);
            }
        }

        public static class LastSeen {
            private String onlineText = "<green>Online";

            private String tooRecentText = "<green>Within the last hour";
            private int tooRecentSeconds = 3599;

            private String recentText = "<yellow><duration> ago";
            private int recentSeconds = 432000;

            private String olderText = "<red><duration> ago";
            private String unknownText = "<red>Unknown";

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
            @Comment("Faction on-hover tooltip information")
            private List<String> faction = new ArrayList<>() {
                {
                    this.add("<faction:if_leader><gold>Leader: <yellow><faction:leader>");
                    this.add("<gold>Land / Power / Max Power: <yellow><faction:claims_count></yellow> / <yellow><faction:power></yellow> / <yellow><faction:power_max>");
                    this.add("<gold>Raidable: <faction:if_raidable><green>Yes</faction:if_raidable><faction:if_raidable:else><red>No");
                    this.add("<gold>Online: <yellow><faction:members_online_count></yellow>/<yellow><faction:members_total_count></yellow>");
                }
            };
            @Comment("Player on-hover tooltip information")
            private List<String> player = new ArrayList<>() {
                {
                    this.add("<gold>Last Seen: <yellow><player:last_seen>");
                    this.add("<gold>Power: <yellow><player:power></yellow> / <yellow><player:power_max>");
                }
            };

            public List<String> getFaction() {
                return Collections.unmodifiableList(faction);
            }

            public List<String> getPlayer() {
                return Collections.unmodifiableList(player);
            }
        }

        private LastSeen lastSeen = new LastSeen();
        private Shield shield = new Shield();
        private Title title = new Title();
        private ToolTips tooltips = new ToolTips();

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
    }

    public static class Scoreboard {
        public static class Constant {

            private String prefixTemplate = "<faction:relation_color>[<faction:name>] </faction:relation_color> ";
            private String suffixTemplate = " <faction:relation_color>[<faction:name>]";

            private List<String> normalContent = new ArrayList<>() {
                {
                    this.add("<gold>Your Faction");
                    this.add("<fuuid:color:relation:member><faction:name>");
                    this.add("<gold>Your Power");
                    this.add("<player:power>");
                }
            };

            @Comment("Can use any placeholders, but does not update once set")
            private String normalTitle = "Faction Status";

            private List<String> factionlessContent = new ArrayList<>() {
                {
                    this.add("<gold>Not in a faction");
                    this.add("");
                    this.add("<gold>Join a faction");
                    this.add("<gold>  or make your own!");
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
            @Comment("Supports placeholders")
            private List<String> content = new ArrayList<>() {
                {
                    this.add("<gold>Power");
                    this.add("<faction:power>");
                    this.add("<gold>Members");
                    this.add("<faction:members_online_count>/<faction:members_total_count>");
                    this.add("<gold>Leader");
                    this.add("<faction:leader>");
                    this.add("<gold>Territory");
                    this.add("<faction:claims_count>");
                }
            };
            private String title = "<faction:relation_color><faction:name>";

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

        private UpgradeDetail dtrClaimLimit = new UpgradeDetail("<green>Claim Limit Increase", "<green>Increases maximum faction territory", "<green>+<increase> claims");

        private UpgradeDetail fallDamage = new UpgradeDetail("<green>Fall Damage Reduction", "<green>Decreases fall damage in your own territory", "<green>-<percent>% reduction");

        private UpgradeDetail flight = new UpgradeDetail("<green>Flight", "<green>Enables flying in faction territory", "");

        private UpgradeDetail growth = new UpgradeDetail("<green>Growth", "<green>Boosts plant growth in faction land", "<green><chance>% chance to grow <boost> extra step");

        private UpgradeDetail maxMembers = new UpgradeDetail("<green>Member Limit Increase", "<green>Increases maximum number of faction members", "<green>+<increase> members");

        private UpgradeDetail powerMax = new UpgradeDetail("<green>Maximum Power Limit Increase", "<green>Increases the maximum limit on faction power", "<green>+<increase> power");

        private UpgradeDetail redstoneAntiFlood = new UpgradeDetail("<green>Redstone Anti-Flood", "<green>Protect circuits from flooding", "");

        private UpgradeDetail shield = new UpgradeDetail("<green>Shield", "<green>Protect territory from explosions", "<green><duration> shield, cooldown <cooldown>");

        private UpgradeDetail warps = new UpgradeDetail("<green>Warps", "<green>Additional locations to which members can teleport", "<green><count> warps");

        private UpgradeDetail zones = new UpgradeDetail("<green>Zones", "<green>Assign your faction claims to zones to label or grant different permissions", "<green>Grants <increase> zones");

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

        private String unlimited = "unlimited";

        public String getUnlimited() {
            return this.unlimited;
        }
    }

    @Comment("This config file will slowly become the location for all text content\n" +
            "All information here uses MiniMessage. https://docs.adventure.kyori.net/minimessage.html")
    private Commands commands = new Commands();
    private Permissions permissions = new Permissions();
    private Placeholders placeholders = new Placeholders();
    private Scoreboard scoreboard = new Scoreboard();
    private Upgrades upgrades = new Upgrades();

    public Commands commands() {
        return this.commands;
    }

    public Permissions permissions() {
        return this.permissions;
    }

    public Placeholders placeholders() {
        return this.placeholders;
    }

    public Scoreboard scoreboard() {
        return this.scoreboard;
    }

    public Upgrades upgrades() {
        return this.upgrades;
    }
}
