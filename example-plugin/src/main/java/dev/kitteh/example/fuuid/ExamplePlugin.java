package dev.kitteh.example.fuuid;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.ThirdPartyCommands;
import dev.kitteh.factions.event.FPlayerJoinEvent;
import dev.kitteh.factions.integration.ExternalChecks;
import dev.kitteh.factions.permissible.PermissibleAction;
import dev.kitteh.factions.permissible.PermissibleActionRegistry;
import dev.kitteh.factions.upgrade.LeveledValueProvider;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeRegistry;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.UpgradeVariable;
import moss.factions.shade.org.incendo.cloud.context.CommandContext;
import moss.factions.shade.org.incendo.cloud.description.Description;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public final class ExamplePlugin extends JavaPlugin implements Listener {

    // An upgrade variable, an int from 1 to 10
    private final UpgradeVariable meowVar = UpgradeVariable.ofInteger("meows", BigDecimal.ONE, BigDecimal.TEN);

    // Our custom upgrade
    private final Upgrade meowUpgrade = new Upgrade.Reactive( // Upgrade.Simple doesn't have the last line
            "meow", // Simple name, for referencing in commands and storage
            Component.text().content("Meow").color(NamedTextColor.GREEN).build(), // Name for display
            Component.text().content("Meowpgrade").color(NamedTextColor.GREEN).build(), // Description for display
            // Details to display. Gets the user-configured settings (thus, not using the defaults defined below) and gets the variable for the level
            // Will display something like "Meows 3 times"
            // Obviously this and the other components could be set up in your plugin to have some sort of configurability!
            (settings, level) -> Component.text().content("Meows " + settings.valueAt(meowVar, level).intValue() + " times").color(NamedTextColor.GREEN).build(),
            10, // Max it out at ten. Especially relevant with our variable being (weird choice) limited to ten
            Set.of(this.meowVar), // The only variable tracked here is this meowVar
            Upgrade.Reactor.UPDATE_COMMANDS // Updates user command access if going to or from level 0, because this upgrade is tying into a command!
    );

    private final PermissibleAction meowAction = new PermissibleAction.WithPrerequisite(
            "MEOW", // simple name for commands and display
            "Ability to meow", // description
            "Meowability", // shorter description
            this.meowUpgrade // prerequisite upgrade for a faction to be able to even grant this
    );

    @Override
    public void onLoad() {
        // Some things have to happen during onLoad

        // Define the default settings
        UpgradeSettings defaultSettings = new UpgradeSettings(
                this.meowUpgrade, // The upgrade it's for
                Map.of(
                        this.meowVar, LeveledValueProvider.Equation.of("level") // Lazy equation - just each level goes up by one! Why define all the levels?
                ),
                10, // Max level configured (cannot go higher than 10 as defined above)
                0, // Starting level
                LeveledValueProvider.Equation.of("level * level") // Lazy notation for level squared. Cheap meows.
        );

        // Register the custom variable!
        UpgradeRegistry.registerVariable(this.meowVar); // Register the variable first!

        // Register the custom upgrade!
        UpgradeRegistry.registerUpgrade(
                this.meowUpgrade,
                defaultSettings,
                false // Default disabled? NO
        );

        // Register the custom permissible action!
        PermissibleActionRegistry.register(this.meowAction);

        // Register the custom command!
        ThirdPartyCommands.register(
                this, // registering plugin
                "meow", // command name, really just used for logging issues
                (manager, builder) ->
                        manager.command( // manager#command registers the command as defined below
                                builder.literal("meow", "purr") // Actual command name and aliases
                                        .commandDescription(Description.of("Meow meow meow")) // Command description for help command
                                        .permission(
                                                // We're going to break this up to explain it all
                                                builder.commandPermission() // Highly recommend inheriting the permission from above
                                                        .and(Cloudy.isPlayer()) // Tests if they're a player. We don't need console trying this out!
                                        )
                                        .handler(this::handle) // Direct to the handler method below. You could, of course, just write the code here.
                        )
        );
    }

    private void handle(CommandContext<Sender> context) {
        // Casting because we know they're a player because of the above Cloudy.isPlayer() check
        FPlayer fPlayer = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        Faction faction = fPlayer.lastStoodAt().faction(); // Faction they're standing in

        if (!faction.isNormal()) { // Wilderness, safezone, warzone
            // Using the fPlayer instance so it works on Spigot, too.
            fPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<rainbow>NO MEOW IN THIS TERRITORY :("));
            // If on Paper this would work: player.sendRichMessage("<rainbow>NO MEOW IN THIS TERRITORY :(");
            return;
        }

        if (faction.hasAccess(fPlayer, this.meowAction, fPlayer.lastStoodAt())) { // If they have the permission
            // Normally you only get permission in your own faction,
            //  but faction leaders can also grant perms to relations (e.g. allies)
            //  or even specific factions or individual players

            int upgradeLevel = faction.upgradeLevel(this.meowUpgrade);
            int meows = Universe.universe() // Universal settings
                    .upgradeSettings(this.meowUpgrade) // Get the current configured settings for this upgrade
                    .valueAt(this.meowVar, upgradeLevel) // Get the meow variable value for this level, as configured
                    .intValue(); // As an int!

            // Yeah, all this does is make the player meow a number of times equal to the upgrade level (unless configured differently by the server admin after setup)
            new BukkitRunnable() {
                private int meowsLeft = meows;

                @Override
                public void run() {
                    player.getWorld().playSound(player, Sound.ENTITY_CAT_PURREOW, 1, 1);
                    this.meowsLeft--;
                    if (this.meowsLeft <= 0) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(this, 10, 10);

            fPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<rainbow>MEOW MEOW MEOW MEOW MEOW MEOW!")); // See above note about using fPlayer
        } else {
            fPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<rainbow>NO MEOW POSSIBLE RIGHT NOW :(")); // See above note about using fPlayer
        }
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);

        // Register a custom AFK detector, where sleeping players are considered AFK for... some reason?
        // I dunno, needed some way to demo it.
        ExternalChecks.registerAfk(this, player -> player.getPose() == Pose.SLEEPING);
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(FPlayerJoinEvent event) {
        if (event.getFaction().upgradeLevel(this.meowUpgrade) > 0) {
            event.getFPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<rainbow>MEOW MEOW!"));
        }
    }
}
