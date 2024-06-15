package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FactionsPlugin;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class BrigadierManager {
    public final Commodore commodore;
    public final LiteralArgumentBuilder<Object> brigadier;
    public final Set<String> aliases = new HashSet<>();

    public BrigadierManager() {
        Map<String, Map<String, Object>> commands = FactionsPlugin.getInstance().getDescription().getCommands();
        String main = commands.keySet().stream().findFirst().get();
        Map<String, Object> cmd = commands.get(main);
        if (cmd.containsKey("aliases")) {
            Object ali = cmd.get("aliases");
            if (ali instanceof Collection) {
                aliases.addAll((Collection<String>) ali);
            }
        }

        brigadier = LiteralArgumentBuilder.literal(main);
        commodore = CommodoreProvider.getCommodore(FactionsPlugin.getInstance());
    }

    public void build() {
        commodore.register(brigadier.build());

        // Add factions children to f alias
        for (String alias : aliases) {
            LiteralArgumentBuilder<Object> fLiteral = LiteralArgumentBuilder.literal(alias);
            for (CommandNode<Object> node : brigadier.getArguments()) {
                fLiteral.then(node);
            }
            commodore.register(fLiteral.build());
        }
    }

    public void addSubCommand(FCommand subCommand) {
        this.addSubCommand(subCommand, null);
    }

    public void addSubCommand(FCommand subCommand, ArgumentBuilder<Object, ?> starting) {
        if (starting == null) {
            starting = brigadier;
        }
        // Register brigadier to all command aliases
        for (String alias : subCommand.aliases) {
            LiteralArgumentBuilder<Object> literal = LiteralArgumentBuilder.literal(alias);

            if (subCommand.requirements.getBrigadier() != null) {
                // If the requirements explicitly provide a BrigadierProvider then use it
                Class<? extends BrigadierProvider> brigadierProvider = subCommand.requirements.getBrigadier();

                try {
                    Constructor<? extends BrigadierProvider> constructor = brigadierProvider.getDeclaredConstructor();
                    starting.then(constructor.newInstance().get(literal));
                } catch (Exception e) {
                    FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to reflectively access brigadier", e);
                }
            } else {
                // Generate our own based on args - quite ugly

                RequiredArgumentBuilder<Object, ?> previous = null;
                if (subCommand.subCommands.isEmpty()) {
                    // We create an orderly stack of all args, required and optional, format them differently
                    List<RequiredArgumentBuilder<Object, ?>> stack = new ArrayList<>();
                    for (String required : subCommand.requiredArgs) {
                        // Simply add the arg name as required
                        stack.add(RequiredArgumentBuilder.argument(required, StringArgumentType.word()));
                    }

                    for (Map.Entry<String, String> optionalEntry : subCommand.optionalArgs.entrySet()) {
                        RequiredArgumentBuilder<Object, ?> optional;

                        // Optional without default
                        if (optionalEntry.getKey().equalsIgnoreCase(optionalEntry.getValue())) {
                            optional = RequiredArgumentBuilder.argument(":" + optionalEntry.getKey(), StringArgumentType.word());
                            // Optional with default, explain
                        } else {
                            optional = RequiredArgumentBuilder.argument(optionalEntry.getKey() + "|" + optionalEntry.getValue(), StringArgumentType.word());
                        }

                        stack.add(optional);
                    }

                    // Reverse the stack and apply .then()

                    for (int i = stack.size() - 1; i >= 0; i--) {
                        if (previous == null) {
                            previous = stack.get(i);
                        } else {
                            previous = stack.get(i).then(previous);
                        }
                    }
                } else {
                    for (FCommand cmd : subCommand.subCommands) {
                        this.addSubCommand(cmd, literal);
                    }
                }

                if (previous == null) {
                    starting.then(literal);
                } else {
                    starting.then(literal.then(previous));
                }
            }
        }
    }

}
