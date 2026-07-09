package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import java.util.function.Function;

public class CmdGet implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().admin().get();

            Command.Builder<Sender> getBuilder = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .required("faction", FactionParser.of(FactionParser.Include.SELF));


            manager.command(getBuilder.literal("id")
                    .handler(ctx -> this.handleInt(ctx, "id", Faction::id)));
        };
    }

    private void handle(CommandContext<Sender> context, String key, Function<Faction, String> function) {
        var tl = FactionsPlugin.instance().tl().commands().admin().get();
        Faction faction = context.get("faction");
        context.sender().sendRichMessage(tl.getOutput(),
                FactionResolver.of(faction),
                Placeholder.unparsed("key", key),
                Placeholder.unparsed("value", function.apply(faction))
        );
    }

    private void handleInt(CommandContext<Sender> context, String key, Function<Faction, Integer> function) {
        this.handle(context, key, function.andThen(String::valueOf));
    }
}
