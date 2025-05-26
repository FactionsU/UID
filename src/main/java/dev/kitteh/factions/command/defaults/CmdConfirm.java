package dev.kitteh.factions.command.defaults;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;
import org.jspecify.annotations.NullMarked;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NullMarked
public class CmdConfirm implements Cmd {
    private static final Cache<UUID, Conf> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    private record Conf(String code, Consumer<FPlayer> consumer) {
    }

    private static final Random random = new Random();
    private static final DecimalFormat decimalFormat = new DecimalFormat("00000");

    public static String add(FPlayer fPlayer, Consumer<FPlayer> consumer) {
        String code = decimalFormat.format(random.nextInt(100000));
        cache.put(fPlayer.uniqueId(), new Conf(code, consumer));
        return FactionsPlugin.instance().tl().commands().generic().getCommandRoot().getFirstAlias() +
                " " +
                FactionsPlugin.instance().tl().commands().confirm().getFirstAlias() +
                " " +
                code;
    }

    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            var confirm = FactionsPlugin.instance().tl().commands().confirm();
            manager.command(
                    builder.literal(confirm.getFirstAlias(), confirm.getSecondaryAliases())
                            .permission(builder.commandPermission().and(Cloudy.hasFaction().or(Cloudy.predicate(s -> !s.isPlayer()))))
                            .required("confirmation-string", StringParser.stringParser())
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        String string = context.get("confirmation-string");

        Conf conf = cache.getIfPresent(sender.uniqueId());
        if (conf == null) {
            sender.sendMessageLegacy(FactionsPlugin.instance().tl().commands().confirm().getNotFound());
            return;
        }

        if (conf.code.equals(string)) {
            conf.consumer.accept(sender);
        } else {
            sender.sendMessageLegacy(FactionsPlugin.instance().tl().commands().confirm().getInvalid());
        }
    }
}
