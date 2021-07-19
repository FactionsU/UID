# Permission Contexts

Presently, only [LuckPerms](https://luckperms.net/) is supported for this feature. To get more information on how to use
contexts in LuckPerms, click [this link for their documentation](https://luckperms.net/wiki/Context). You need to be
running at least LuckPerms version 5.1.0 for FactionsUUID to utilize this feature.

## Contexts Offered

### Faction ID

`factionsuuid:faction-id`  
**Possible values**: Any number matching an existing faction's ID  
**Description**: The ID of the faction the player is in. If not in a faction, they get the 
wilderness ID which is 0. Presently you need to check `data/factions.json` for the faction ID. 
The tag is not used because factions could change tag and that would be painful to juggle.

### Is peaceful?

`factionsuuid:is-peaceful`  
**Possible values**: true, false  
**Description**: True if the player is in a peaceful faction.

### Is permanent?

`factionsuuid:is-permanent`  
**Possible values**: true, false  
**Description**: True if the player is in a permanent faction.

### Territory relation

`factionsuuid:territory-relation`  
**Possible values**: member, ally, truce, neutral, enemy  
**Description**: The player's relation to the land they are in. Only one value at a time. Could be used to, for example, 
prevent players in enemy territory from using particular features of another plugin that are permission-based (not 
just limited to commands).

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
