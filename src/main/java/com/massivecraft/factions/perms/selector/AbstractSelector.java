package com.massivecraft.factions.perms.selector;

import com.massivecraft.factions.perms.PermSelector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default, abstract implementations of basics that all built-in
 * PermSelectors utilize.
 */
public abstract class AbstractSelector implements PermSelector {
    public static class BasicDescriptor implements Descriptor {
        private final Function<String, PermSelector> function;
        private final String name;
        private boolean acceptsEmpty;
        private Supplier<String> instructions;
        private final Supplier<String> displayName;

        public BasicDescriptor(String name, Supplier<String> displayName, Function<String, PermSelector> function) {
            this.name = name;
            this.function = function;
            this.displayName = displayName;
        }

        @Override
        public PermSelector create(String input) {
            return this.function.apply(input);
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Component getDisplayName() {
            return MiniMessage.miniMessage().deserialize(this.displayName.get());
        }

        public BasicDescriptor acceptEmpty() {
            this.acceptsEmpty = true;
            return this;
        }

        @Override
        public boolean acceptsEmpty() {
            return this.acceptsEmpty;
        }

        public BasicDescriptor withInstructions(Supplier<String> instructions) {
            this.instructions = instructions;
            return this;
        }

        @Override
        public String getInstructions() {
            return this.instructions == null ? null : this.instructions.get();
        }
    }

    private final Descriptor descriptor;

    protected AbstractSelector(Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    public Descriptor descriptor() {
        return this.descriptor;
    }

    @Override
    public String toString() {
        return this.serialize();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.serialize());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PermSelector && ((PermSelector) obj).serialize().equals(this.serialize());
    }
}
