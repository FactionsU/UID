# Developers

!!! warning "This page contains information only useful to plugin developers"

### Maven details:  
Repository: `https://ci.ender.zone/plugin/repository/everything/`  
groupId: `com.massivecraft`  
artifactId: `Factions`  
version: `1.6.9.5-U0.6.35`

## FLocation
FLocation is a Chunk wrapper. If you ever want to deal with the map, claimed land, or something similar, you'll need to
convert a Location or Chunk into an FLocation, both of which are super easy :)

**Getting from a Bukkit Location.**
```java
FLocation flocation = new FLocation(location);
```

**Getting from a Chunk**
```java
// Broken apart just to avoid making an overly wide example
String worldName = chunk.getWorld().getName();
int x = chunk.getX();
int z = chunk.getZ();

FLocation flocation = new FLocation(worldName, x, z);
```

## FPlayers
There is always **1** FPlayer object for each player that's been on the server, including online ones. It's very easy to
get the associated FPlayer if you already have the Bukkit Player or their UUID.

**By Bukkit Player**
```java
FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
```

**By UUID**
```java
FPlayer fplayer = FPlayers.getInstance().getById(uuid.toString());
```

**Get Role**
```java
Role fplayerRole = fplayer.getRole();
``` 

## Factions
There are multiple ways you can get a Faction.

**Most common is by name (aka tag)**
```java
Faction faction = Factions.getInstance().getByTag("name");
```

If you have a FLocation, you can get the Faction that owns it (including Wilderness, Warzone, and Safezone)
```java
Faction faction = Board.getInstance().getFactionAt(fLocation);
```
