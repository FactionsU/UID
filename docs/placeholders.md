# Placeholders

!!! note "Looking for PlaceholderAPI placeholders? [Click here](placeholderapi.md)"


Fancy variables. Can only be used in /f show

Variable | Explanation
--| ---
{allies-list}  | Lists each faction ally with tooltips
{enemies-list} | Lists each faction enemy with tooltips
{online-list}  | Lists all online members with tooltips
{offline-list} | Lists all offline members with tooltips
 
Player variables. Can be used in tooltips.show, scoreboards, or /f show

Variable | Explanation
--| ---
{group}     | Players group (/f show only)
{name}      | Players name
{lastSeen}  | Last time player was seen (if offline), or just 'Online'
{balance} | Players balance
{player-kills} | # of kills the player has
{player-deaths}| # of deaths the player has
{player-power} | Current player power
{player-maxpower} | Player max power
{total-online-visible}| # of players online from the perspective of the current player
 
Faction variables. Can be used in tooltips.list, scoreboards, or /f show

Variable | Explanation
--| ---
{header}    | Default factions header (ex. /f show)
{faction}   | Factions tag (if none, uses lang.yml for factionless name)
{faction-relation-color} | Factions color relative to the viewer
{joining}   | How to join this faction
{power}     | Factions deaths until raidable value
{power-boost}  | DTR Symbol based on current DTR (max, regen, frozen, raidable)
{maxPower}  | Factions max deaths until raidable value
{chunks}    | # of claims faction has (in chunks)
{warps}     | # of warps faction has
{description} | Factions description
{create-date} | Date faction was created
{leader}    | Faction leader
{land-value}  | Value of all claims
{land-refund} | Calculated refund value
{allies}    | # of allies faction has
{enemies}   | # of enemies faction has
{online}    | # of faction members online
{offline}   | # of faction members offline
{members}   | # of faction members (includes offline)
{faction-balance}      | Faction bank balance
{world}, {x}, {y}, {z} | Faction home variables. You don't need to use them all.
{faction-kills} | # of kills the faction has
{faction-deaths}| # of deaths the faction has
{faction-bancount} | # of bans the faction has
{raidable} | Displays true/false (modifiable in lang.yml)
{dtr} | Current faction DTR
{max-dtr} | Max faction DTR based on players
{max-chunks} | Maximum claims the faction can have (power or DTR)
{peaceful} | Displays a message if peaceful
{permanent} | Displays a message if permanent
{dtr-frozen-status} | True or false (customizable in lang.yml) for DTR frozen state
{dtr-frozen-time} | Time remaining in frozen state (configure in main.conf) or blank if not frozen
{tnt-balance} | TNT bank balance
{tnt-max-balance} | Maximum TNT bank balance
{faction-link} | Faction link from `/f link`

 
Faction Permissions GUI variables. Can only be used in GUI

Variable | Explanation
--| ---
{relation}            | Shows relation name (Can be used in action and relation)
{relation-color}      | Relation color
{action}              | Shows action name (Can only be used in action)
{action-access}       | Shows the action's access with current relation
{action-access-color} | Access color

General variables. Can be used anywhere.

Variable | Explanation
--| ---
{total-online}       | Total # of players on the server
{max-warps}          | Max # of warps a faction can set
{max-allies}         | Max # of allies a faction can have
{max-enemies}        | Max # of enemies a faction can have
{factionless}        | Count of all factionless players online
{factionless-total}  | Count of all factionless players online

Scoreboard-only variable with special behavior.
Configure the expected width and height of the board in the map section of the config.

Variable | Explanation
--| ---
{map}       | Displays a line of the scoreboard map. Add one per line the scoreboard will appear on.