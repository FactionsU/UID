package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.permissible.PermSelector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default, abstract implementations of basics that all built-in
 * PermSelectors utilize.
 */
@NullMarked
public abstract class AbstractSelector implements PermSelector {
    public static class BasicDescriptor implements Descriptor {
        private final Function<String, PermSelector> function;
        private final String name;
        private boolean acceptsEmpty;
        private @Nullable Supplier<String> instructions;
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
        public String name() {
            return this.name;
        }

        @Override
        public Component displayName() {
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
        public @Nullable String instructions() {
            return this.instructions == null ? null : this.instructions.get();
        }
    }

    private final Descriptor descriptor;

    protected AbstractSelector(Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
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
