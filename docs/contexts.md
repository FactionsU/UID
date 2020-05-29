# Permission Contexts

Presently, only [LuckPerms](https://luckperms.net/) is supported for this feature. To get more information on how to use
contexts in LuckPerms, click [this link for their documentation](https://luckperms.net/wiki/Context). You need to be
running at least LuckPerms version 5.1.0 for FactionsUUID to utilize this feature.

## Contexts Offered

### Territory relation

`factionsuuid:territory-relation`  
**Possible values**: member, ally, truce, neutral, enemy
**Description**: The player's relation to the land they are in. Only one value at a time. Could be used to, for example

### Faction role at least
 
`factionsuuid:role-at-least`  
**Possible values**: admin, coleader, moderator, normal, recruit  
**Description**: If a player is a recruit, they will only have the recruit value. If moderator, they will have moderator,
normal, and recruit. Could be used to, for example, grant a permission node only to faction admins.

### Faction role at most

`factionsuuid:role-at-most`  
**Possible values**: admin, coleader, moderator, normal, recruit  
**Description**: If a player is an admin, they will only have the admin value. If moderator, they will have moderator,
coleader, and admin. Could be used to, for example, grant a permission node only to normal members and recruits.
