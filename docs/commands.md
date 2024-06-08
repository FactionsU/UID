# Commands

!!! warning "This page is presently under construction, and is incomplete"

Click on any command for more details.

!!! note
    Some commands take additional arguments.  
    Arguments like &lt;this&gt; are *required*.  
    Arguments like [this] are *optional*.

### Faction Member Commands

??? abstract  "/f join &lt;faction&gt;"
    Join a defined faction.

    !!! success "Requirements"
        `factions.join`

??? abstract  "/f leave"
    Leave your faction.

    !!! success "Requirements"
        `factions.leave`

??? abstract  "/f chat &lt;mode&gt;"
    Toggles chat modes or specify which channel you want to be in (public, alliance, faction, truce)

    !!! success "Requirements"
        `factions.chat` node.

??? abstract  "/f togglealliancechat"
    Toggle ignoring alliance chat.

    !!! success "Requirements"
        `factions.togglealliancechat` node.

??? abstract  "/f home"
    Teleports you to your faction's home.

    !!! success "Requirements"
        `factions.home`

??? abstract "/f warp [name] [password]"
    Teleports you to a warp, password optional. Opens GUI if no warp defined.

    !!! success "Requirements"
        `factions.warp` node.

??? abstract "/f warpother &lt;faction&gt; [name] [password]"
    Teleports you to a faction's warp, password optional. Opens GUI if no warp defined.

    !!! success "Requirements"
        `factions.warp` node.  
        Permission to use that faction's warps.

??? abstract  "/f fly"
    Fly in your faction's territory. Disabled in combat.

    !!! success "Requirements"
        `factions.fly` node.  

??? abstract "/f vault [number]"
    Opens your faction's vault. If no vault is defined, it will list available vaults.

    !!! success "Requirements"
        `factions.vault` node.  

### Faction Creation and Management

??? abstract "/f create &lt;name&gt;"
    Create a faction with the given name.

    !!! success "Requirements"
        `factions.create` node.  

??? abstract "/f disband"
    Disband your faction.

    !!! success "Requirements"
        `factions.disband` node.  

??? abstract "/f tag &lt;tag&gt;"
    Change your faction's tag.

    !!! success "Requirements"
        `factions.tag` node.  
        Must be faction moderator or higher

??? abstract "/f desc &lt;description...&gt;"
    Set your faction's new description.  

    !!! success "Requirements"
        `factions.description` node.  
        Must be faction moderator or higher

??? abstract "/f sethome"
    Set your faction's home.

    !!! success "Requirements"
        `factions.sethome` node.

??? abstract "/f delhome &lt;name&gt;"
    Delete your faction's home

    !!! success "Requirements"
        `factions.delhome` node.

??? abstract "/f setwarp &lt;name&gt; [password]"
    Set a warp with an optional password to your location.

    !!! success "Requirements"
        `factions.setwarp` node.

??? abstract "/f delwarp &lt;name&gt;"
    Delete a warp

    !!! success "Requirements"
        `factions.setwarp` node.

??? abstract "/f announce &lt;message...&gt;"
    Creates an announcement sent to all faction members.  
    Also saves the message for all current members who are offline, sending to them upon login.

    !!! success "Requirements"
        `factions.announce` node.  
        Must be moderator of your faction or higher to use.  

??? abstract "/f perms"
    Manage permissions for your Faction.

    !!! success "Requirements"
        `factions.permissions` node.
        Must be faction admin.

### Member management

??? abstract "/f defaultrole &lt;role&gt;""
    Sets your faction's default role for new members.

    !!! success "Requirements"
        `factions.defaultrank` node.
        Must be faction leader.

??? abstract "/f open"
    Toggle allowing anyone being able to join the faction.

    !!! success "Requirements"
        `factions.open` node.  
        Must be faction moderator or higher.

??? abstract "/f invite &lt;target&gt;"
    Invite a player to your faction.

    !!! success "Requirements"
        `factions.invite` node.
        Must be faction admin, or be granted `invite` perms in the faction.

??? abstract "/f deinvite &lt;target&gt;"
    Revoke an invite from a player. If no player is defined, it will list all players with pending invites. 

    !!! success "Requirements"
        `factions.deinvite` node.
        Must be faction admin, or be granted `invite` perms in the faction.

??? abstract "/f deinvite &lt;target&gt;"
    Lists all players with pending invites. Click the names to revoke their invite.

    !!! success "Requirements"
        `factions.showinvites` node.
        Must be faction admin, or be granted `invite` perms in the faction.

??? abstract "/f title &lt;player&gt; [title]"
    Set a player's custom title. Will charge them if enabled.

    !!! success "Requirements"
        `factions.title` node.
        Must be faction moderator or higher

??? abstract "/f promote &lt;name&gt;"
    Promote a player by one rank.

    !!! success "Requirements"
        `factions.promote` node.  

??? abstract "/f demote &lt;name&gt;"
    Demote a player by one rank.

    !!! success "Requirements"
        `factions.promote` node.  

??? abstract "/f mod [name]"
    Promote a player in your faction to mod.

    !!! success "Requirements"
        `factions.mod` node.  
        Must be faction coleader or higher

??? abstract "/f coleader [name]"
    Promote a player in your faction to coleader.

    !!! success "Requirements"
        `factions.coleader` node.  
        Must be faction leader

??? abstract "/f admin &lt;player&gt;"
    Sets the new leader of your faction.

    A server administrator can make any targeted player the leader of the faction that player is in.
    
    !!! success "Requirements"
        `factions.admin` node.  
        Must be admin of your faction to use.  
        `factions.admin.any` node to change status of any player.

??? abstract "/f kick &lt;target&gt;"
    Kicks a player from the faction.

    !!! success "Requirements"
        `factions.kick` node.
        Must be faction admin, or be granted `kick` perms in the faction.

??? abstract "/f ban &lt;target&gt;"
    Bans a player from the faction.

    !!! success "Requirements"
        `factions.ban` node.  
        Must be faction admin, or be granted `ban` perms in the faction.  

??? abstract "/f unban &lt;target&gt;"
    Unbans a player from the faction.

    !!! success "Requirements"
        `factions.ban` node.  
        Must be faction admin, or be granted `ban` perms in the faction.  

??? abstract "/f banlist"
    Lists players currently banned from the faction.

    !!! success "Requirements"
        `factions.ban` node.  



### Territory

??? abstract  "/f claim [radius] [faction]"
    Defaults: radius = 1, faction = yours  
    Claims one or more chunks for the given faction.  
    Can only claim if the land is not claimed by another faction, or if the other faction has more land than power (and is not an ally).
    
    !!! success "Requirements"
        Land cannot be owned by another faction (unless using power, the owning faction has more land than power, and the owning faction is not an ally)  
        `factions.claim` node.  
        `factions.claim.radius` node to claim a radius greater than 1.  
        Must be faction admin, or be granted `territory` perms in the faction.  
        To claim for safezone, must have `factions.managesafezone`.  
        To claim for warzone, must have `factions.managewarzone`.  

??? abstract "/f unclaim [radius] [faction]"
    Defaults: radius = 1, faction = yours  
    Returns one or more chunks of faction territory to the wilderness.  
    
    !!! success "Requirements"
        `factions.unclaim` node.  
        Must be faction admin, or be granted `territory` perms in the faction.  
        To unclaim for safezone, must have `factions.managesafezone`.  
        To unclaim for warzone, must have `factions.managewarzone`.  

??? abstract "/f autoclaim [faction]"
    Defaults: faction = yours  
    Turns autoclaiming on or off. If on, any chunk you enter that you *can* claim, *will* be claimed up until
    you reach your faction's limit.
    
    !!! success "Requirements"
        `factions.autoclaim` node.  
        Must be faction admin, or be granted `territory` perms in the faction.  
        To claim for safezone, must have `factions.managesafezone`.  
        To claim for warzone, must have `factions.managewarzone`.

??? abstract "/f autounclaim [faction]"
    Defaults: faction = yours  
    Turns autounclaiming on or off. If on, any chunk you enter that you *can* unclaim, *will* be unclaimed.

    !!! success "Requirements"
        `factions.autoclaim` node.  
        Must be faction admin, or be granted `territory` perms in the faction.  
        To claim for safezone, must have `factions.managesafezone`.  
        To claim for warzone, must have `factions.managewarzone`.

??? abstract "/f claimat &lt;world&gt; &lt;x&gt; &lt;z&gt;"
    Attempts to claim a chunk in the given world at the given coordinates.  
    The x and z values are *chunk coordinates*, not normal block coordinates.  
    You can see chunk coordinates from `/f map`, for example.
    
    !!! success "Requirements"
        `factions.claimat` node.  
        Must be faction admin, or be granted `territory` perms in the faction.  

??? abstract "/f claimfill [amount] [faction]"
    Defaults: amount = config limit, faction = yours
    Attempts to claim a number of blocks filling in an established shape of claims.  
    
    !!! success "Requirements"
        `factions.claim.fill` node.  
        Must be faction admin, or be granted `territory` perms in the faction.  
        To claim for safezone, must have `factions.managesafezone`.  
        To claim for warzone, must have `factions.managewarzone`.

??? abstract "/f unclaimfill [amount] [faction]"
    Defaults: amount = config limit, faction = yours  
    Attempts to unclaim all claims connected to the current claim.

    !!! success "Requirements"
        `factions.unclaim.fill` node.  
        Must be faction admin, or be granted `territory` perms in the faction.  
        To claim for safezone, must have `factions.managesafezone`.  
        To claim for warzone, must have `factions.managewarzone`.

??? abstract "/f claimline [amount] [direction] [faction]"
    Defaults: amount = 1, direction = facing, faction = yours  
    Attempts to claim a number of blocks in a line based on the direction given (or facing direction, if not specified).  
    Acceptable directions: north, south, east, west.
    
    !!! success "Requirements"
        `factions.claim.line` node.  
        Must be faction admin, or be granted `territory` perms in the faction.  
        To claim for safezone, must have `factions.managesafezone`.  
        To claim for warzone, must have `factions.managewarzone`.

??? abstract "/f unclaimall"
    The nuclear option. Removes all claims in your faction, returning them to the wilderness.
    
    !!! success "Requirements"
        `factions.unclaimall` node.  
        Must be faction admin, or be granted `territory` perms in the faction.  

??? abstract "/f safeunclaimall [world]"
    Defaults: world = all  
    Removes all safezone claims in a given world, or in all worlds.
    
    !!! success "Requirements"
        `factions.managesafezone` node.

??? abstract "/f warunclaimall [world]"
    Defaults: world = all  
    Removes all warzone claims in a given world, or in all worlds.
    
    !!! success "Requirements"
        `factions.managewarzone` node.

??? abstract "/f listclaims [world] [faction]"
    Defaults: world = current, faction = current  
    Lists all coordinates of faction claims, merging attached claims into one coordinate with number of claims in parentheses.
    
    !!! success "Requirements"
        `factions.listclaims` node.  
        Must be faction admin, or be granted `listclaims` perms in the faction.  
        To view other factions, must have`factions.listclaims.other`.

### Money

??? abstract  "/f money"
    Shows help for money commands.

    !!! success "Requirements"
        None.

??? abstract  "/f money balance"
    Check a faction's balance. Default is your own faction.

    !!! success "Requirements"
        `factions.money.balance` node.

??? abstract  "/f money deposit &lt;amount&gt; [faction]"
    Deposit money into your faction. Admins can specify other factions and can add money to the specified faction.

    !!! success "Requirements"
        `factions.money.deposit` node.

??? abstract  "/f money ff &lt;amount&gt; &lt;factionfrom&gt; &lt;factionto&gt;"
    Transfer money from one faction to another.

    !!! success "Requirements"
        `factions.money.f2f` node.

??? abstract  "/f money fp &lt;amount&gt; &lt;factionfrom&gt; &lt;playerto&gt;"
    Transfer money from one faction to a player.

    !!! success "Requirements"
        `factions.money.f2f` node.

??? abstract  "/f money pf &lt;amount&gt; &lt;playerfrom&gt; &lt;factionto&gt;"
    Transfer money from one player to a faction.

    !!! success "Requirements"
        `factions.money.f2f` node.

??? abstract  "/f money withdraw &lt;amount&gt; [faction]"
    Withdraw money from your faction. Admins can specify any faction and take away money from the faction.

    !!! success "Requirements"
        `factions.money.withdraw` node.

??? abstract  "/f money modify &lt;amount&gt; &lt;faction&gt;"
    Modify a faction's bank account.

    !!! success "Requirements"
        `factions.money.modify` node.

### TNT Bank

??? abstract "/f tnt"
    Shows faction TNT bank.

    !!! success "Requirements"
        `factions.tnt.info` node.

??? abstract "/f tnt deposit &lt;amount&gt;"
    Deposit into the faction TNT bank

    !!! success "Requirements"
        `factions.tnt.deposit` node.

??? abstract "/f tnt fill &lt;radius&gt; &lt;amount&gt;"
    Fill nearby dispensers with TNT from faction TNT bank

    !!! success "Requirements"
        `factions.tnt.fill` node.

??? abstract "/f tnt info"
    Shows faction TNT bank.

    !!! success "Requirements"
        `factions.tnt.info` node.

??? abstract "/f tnt siphon &lt;radius&gt; [amount]"
    Siphon TNT from nearby dispensers into the faction TNT bank

    !!! success "Requirements"
        `factions.tnt.siphon` node.

??? abstract "/f tnt withdraw &lt;amount&gt;"
    Withdraw from the faction TNT bank

    !!! success "Requirements"
        `factions.tnt.withdraw` node.

### Remaining commands

Commands yet to be documented in the new format

| Command                                          | Permission                 | Meaning                                                                                                                                    |
|--------------------------------------------------|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `/f ahome <target>`                              | factions.ahome             | Teleport a player to their faction's home.                                                                                                 |
| `/f autohelp`                                    | none                       | Show help for all commands.                                                                                                                |
| `/f boom [on/off]`                               | factions.noboom            | Toggle peaceful explosions in your faction's territory on or off.                                                                          |
| `/f bypass`                                      | factions.bypass            | Set yourself to bypass faction permission checks.                                                                                          |
| `/f chatspy`                                     | factions.chatspy           | Enable spying on all private chat channels.                                                                                                |
| `/f coords`                                      | factions.coords            | Send faction members your current position                                                                                                 |
| `/f help <page>`                                 | factions.help              | List help pages for things.                                                                                                                |
| `/f list`                                        | factions.list              | List top Factions by players.                                                                                                              |
| `/f lock`                                        | factions.lock              | Lock datafiles from being overwritten. Will make anything on the server not get saved.                                                     |
| `/f logins`                                      | factions.monitorlogins     | Toggle monitoring of logins for your faction.                                                                                              |
| `/f map [on/off]`                                | factions.map               | View the faction map of the area around you.                                                                                               |
| `/f mapheight [value]`                           | factions.mapheight         | Set how many lines your /f map will show.                                                                                                  |
| `/f modifypower <name> <power>`                  | factions.modifypower       | Modify a player's power. The <power> variable adds power to the player's current power.                                                    |
| `/f owner [name]`                                | factions.owner             | Set claim ownership for this chunk. Admins can specify a target player.                                                                    |
| `/f ownerlist`                                   | factions.ownerlist         | Get the current owner of the chunk you're in if it's in your faction.                                                                      |
| `/f peaceful <faction>`                          | factions.setpeaceful       | Set a faction to being peaceful.                                                                                                           |
| `/f permanent <faction>`                         | factions.setpermanent      | Set a faction to permanent status. This will make the faction stay if there are zero members.                                              |
| `/f permanentpower <faction> [power]`            | factions.setpermanentpower | Set permanent power to a faction.                                                                                                          |
| `/f power <player>`                              | factions.power             | Check power of a player. Default is yourself.                                                                                              |
| `/f powerboost <player/faction> <name> <number>` | factions.powerboost        | Set powerboost of a player or faction. <player/faction> can be 'f' or 'p' to let the plugin know if you're specifying a player or faction. |
| `/f rel <relation> <faction>`                    | factions.relation          | Request to change your faction's relationship with a target faction. Relations can be ally, truce, neutral, enemy.                         |
| `/f reload`                                      | factions.reload            | Reload configurations (lang.yml, config.yml, conf.json). This does not reload factions saved data from disk.                               |
| `/f safeunclaimall [world]`                      | factions.managesafezone    | Safely unclaim all territories in your world. Can specify another world.                                                                   |
| `/f saveall`                                     | factions.save              | Force save all factions data to disk.                                                                                                      |
| `/f sb`                                          | factions.scoreboard        | Toggle the factions scoreboard on or off.                                                                                                  |
| `/f sc`                                          | factions.seechunk          | See outlines around the border of the chunk you're standing in. No one else can see the outlines.                                          |
| `/f setmaxvaults <faction> <number>`             | factions.setmaxvaults      | Set the max vaults a faction can have.                                                                                                     |
| `/f show [faction]`                              | factions.show              | Show info about a Faction. Default is yours.                                                                                               |
| `/f status`                                      | factions.status            | Show status of all players in your faction.                                                                                                |
| `/f stuck`                                       | factions.stuck             | Attempts to teleport you to the nearest wilderness chunk.                                                                                  |
| `/f top <criteria> [page]`                       | factions.top               | List top factions by criteria (members, start, power, land, online, money).                                                                |
| `/f version`                                     | factions.version           | Show the version string for FactionsUUID.                                                                                                  |
| `/f warunclaimall`                               | factions.managewarzone     | Unclaim all warzone claims.                                                                                                                |
| `/f near`                                        | factions.near              | Show nearby faction members                                                                                                                |
