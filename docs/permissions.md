# /f perms

!!! note "Looking for permission nodes? [Click here](permissionnodes.md)"

You can control who, inside or outside your faction, has access to various actions within your territory.

## How it all works: Selectors and Actions

Determination of who can perform what actions is sorted out by *selectors*, which then have defined for them a series of 
allowed or denied *actions*. For example, a selector may describe all members of the faction with at least the 
moderator role, and that selector is allowed the `setwarp` action, so they may set warps within the faction. Or, perhaps 
a selector describing allies of the faction is allowed to open doors within the faction, as this faction wants their 
allies to enter their territory. 

To determine if a player can perform an action, the selectors for a given faction are checked *in order*. If a player 
matches a selector, the matched selector is then checked for the action. If the action is found then that state, 
allow or deny, is used. Only the first selector-matched action state (allow/deny) is used. For anyone familiar with 
Discord permissions, this system behaves similarly. If no selectors match, or the action is not present in any matching 
selectors, the action is denied.

Server admins can control *override* permissions, defining selectors and actions that will apply to *all factions* and 
will be processed *before* any faction-set selectors are checked. As an example usage, a server admin could *force* all 
factions to allow allies to use buttons through an ally matching override selector with `button` set to allow. In the 
config, admins can additionally set actions to *hidden* and this will prevent the action from showing up anywhere. It 
is not advisable to hide an action after the server has been running without also setting an `all` override selector for 
it to avoid any previously set options from being unchangeable by faction admins.

All of this functionality can be controlled by a faction leader through the `/f perms` command, just by running that 
base command and clicking the text within to add/remove/reorder selectors.

## Actions

Action | Description
--- | --- 
ban | Can ban others from the faction
build | Can build in faction territory (while not raidable)
button | Use buttons in faction territory (while not raidable)
container | Use containers in faction territory (while not raidable)
destroy | Can destroy in faction territory (while not raidable)
disband | Can disband the faction (careful!)
door | Use doors in faction territory (while not raidable)
economy | Can access faction economy
fly | Can fly in faction territory
frost | Can frost walk in faction territory (while not raidable)
home | Can visit the faction home
invite | Able to invite others to the faction
item | Use items in faction territory (while not raidable)
kick | Can kick faction members
lever | Use levers in faction territory (while not raidable)
owner | Can created owned areas with /f owner
pain | Allows building/destroying in faction territory but causes pain (while not raidable)
plate | Can interact with pressure plates
promote | Can promote members up to their own role within the faction
sethome | Can set the faction home
setwarp | Can set a faction warp
territory | Can claim/unclaim faction territory
tntdeposit | Can deposit into faction TNT bank (including siphon)
tntwithdraw | Can withdraw from faction TNT bank (including fill)
warp | Can use faction warps

## Selectors

!!! note "Any selector that can match a faction (all, faction, relation) can be used for non-player actions like pistons"

Selector | Description
--- | ---
all | Matches everything, always. 
faction | Matches a specific faction. Can input the faction name, but will be stored using non-changing internal ID.
player | Matches a specific player. Can input the player name, but will be stored using non-changing Mojang UUID.
relation-single | Matches a specific relation. 
relation-atleast | Matches a relation of at least the given type. Order: Enemy, Neutral, Truce, Ally.
relation-atmost | Matches a relation of at most the given type. Order: Enemy, Neutral, Truce, Ally.
role-single | Matches a specific relation
role-atleast | Matches a role of at least the given type. Order: Recruit, Normal, Moderator, Coleader, Admin.
role-atmost | Matches a role of at most the given type. Order: Recruit, Normal, Moderator, Coleader, Admin.
unknown | This is how any unknown selectors loaded from storage are treated, like if a plugin providing extra selectors didn't load. Unknown selectors never match anything.
