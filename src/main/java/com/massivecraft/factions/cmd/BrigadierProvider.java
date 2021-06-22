package com.massivecraft.factions.cmd;

import com.mojang.brigadier.builder.ArgumentBuilder;

@FunctionalInterface
public interface BrigadierProvider {
    ArgumentBuilder<Object, ?> get(ArgumentBuilder<Object, ?> parent);
}
