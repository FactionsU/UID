# Commands

Click on any command for more details.

!!! note
    Some commands take additional arguments.  
    Arguments like &lt;this&gt; are *required*.  
    Arguments like [this] are *optional*.

### Territory

??? abstract  "/f claim [radius] [faction]"
    Defaults: radius = 1, faction = yours  
    Claims one or more chunks for the given faction.  
    Can only claim if the land is not claimed by another faction, or if the other faction has more land than power (and is not an ally).
    
    !!! success "Requirements"
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

??? abstract "/f claimat &lt;world&gt; &lt;x&gt; &lt;z&gt;"
    Attempts to claim a chunk in the given world at the given coordinates.  
    The x and z values are *chunk coordinates*, not normal block coordinates.  
    You can see chunk coordinates from `/f map`, for example.
    
    !!! success "Requirements"
        `factions.claimat` node.
        Must be faction admin, or be granted `territory` perms in the faction.  

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

### Faction Management

??? abstract "/f admin &lt;player&gt;"
    Sets the new leader of your faction.
    
    A server administrator can make any targeted player the leader of the faction that player is in.
    
    !!! success "Requirements"
        `factions.admin` node.  
        Must be admin of your faction to use.  
        `factions.admin.any` node to change status of any player.

??? abstract "/f announce &lt;message...&gt;"
    Creates an announcement sent to all faction members.  
    Also saves the message for all current members who are offline, sending to them upon login.
    
    !!! success "Requirements"
        `factions.announce` node.  
        Must be moderator of your faction or higher to use.  

??? abstract "/f ban &lt;target&gt;"
    Bans a player from the faction.  
    
    !!! success "Requirements"
        `factions.ban` node.  
        Must be faction admin, or be granted `ban` perms in the faction.  
