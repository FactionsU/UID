# /f perms

!!! note "Looking for permission nodes? [Click here](permissionnodes.md)"

You can control who inside, or outside, your faction has access to various functionality.

## Defaults

Set default states in the default_permissions files. There is an additional offline permissions file, if you
choose to enable separate perms for offline factions.

### Locking

Setting a permission to `locked` means that no faction owners can control that permission and it is locked in that state
for all factions. If you lock a permission *after* some factions have already set that permission to a different state,
the locked default state that you set still takes priority. If you later unlock that permission, the factions choice of
state will return.

## Permissions

Permission | Description
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
plate | Can interact with plates
promote | Can promote members up to their own role within the faction
sethome | Can set the faction home
setwarp | Can set a faction warp
territory | Can claim/unclaim faction territory
tntdeposit | Can deposit into faction TNT bank (including siphon)
tntwithdraw | Can withdraw from faction TNT bank (including fill)
warp | Can use faction warps
