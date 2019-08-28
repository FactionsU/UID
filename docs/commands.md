# Commands

Click on any command for more details.

!!! note
    Some commands take additional arguments.  
    Arguments like &lt;this&gt; are *required*.  
    Arguments like [this] are *optional*.

## Territory

??? abstract  "/f claim [radius] [faction]"
    Defaults: radius = 1, faction = yours  
    Claims one or more chunks for the given faction.  
    Can only claim if the land is not claimed by another faction, or if the other faction has more land than power (and is not an ally).
    
    !!! info "Requirements"
        `factions.claim` node.  
        Must be faction admin, or be granted `territory` perms in the faction.  
        To claim for safezone, must have `factions.managesafezone`.  
        To claim for warzone, must have `factions.managewarzone`.  
        