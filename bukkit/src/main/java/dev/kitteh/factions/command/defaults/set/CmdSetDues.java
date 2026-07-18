package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.policy.DuesFailurePolicy;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.DoubleParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CmdSetDues implements Cmd {
    private static final List<String> POLICY_NAMES = Arrays.stream(DuesFailurePolicy.values())
            .map(p -> p.name().toLowerCase(Locale.ROOT))
            .toList();

    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = Confs.tl().commands().set().dues();

            Command.Builder<Sender> duesBuilder = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission()
                            .and(Cloudy.hasPermission(Permission.DUES).and(Cloudy.isAtLeastRole(Role.ADMIN)).and(Cloudy.predicate(_ -> Econ.duesEnabled()))));

            manager.command(
                    duesBuilder.literal(tl.defaultDues().getFirstAlias(), tl.defaultDues().getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.defaultDues().getDescription()))
                            .required("amount", DoubleParser.doubleParser(0))
                            .handler(this::handleDefault)
            );

            manager.command(
                    duesBuilder.literal(tl.role().getFirstAlias(), tl.role().getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.role().getDescription()))
                            .required("role", StringParser.stringParser(), SuggestionProvider.suggestingStrings(Role.COLEADER.getRoleNamesAtOrBelow()))
                            .required("amount", DoubleParser.doubleParser(0))
                            .handler(this::handleRole)
            );

            manager.command(
                    duesBuilder.literal(tl.clear().getFirstAlias(), tl.clear().getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.clear().getDescription()))
                            .required("role", StringParser.stringParser(), SuggestionProvider.suggestingStrings(Role.COLEADER.getRoleNamesAtOrBelow()))
                            .handler(this::handleClear)
            );

            manager.command(
                    duesBuilder.literal(tl.policy().getFirstAlias(), tl.policy().getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.policy().getDescription()))
                            .required("policy", StringParser.stringParser(), SuggestionProvider.suggestingStrings(POLICY_NAMES))
                            .handler(this::handlePolicy)
            );

            manager.command(duesBuilder.handler(this::handleStatus));
        };
    }

    private boolean checkEnabled(FPlayer sender) {
        if (!Econ.duesEnabled()) {
            sender.sendRichMessage(Confs.tl().commands().set().dues().getDisabled());
            return false;
        }
        return true;
    }

    private void handleDefault(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        if (!checkEnabled(sender)) {
            return;
        }
        double raw = context.get("amount");
        double amount = Math.max(0, raw);
        sender.faction().dues(amount);
        sender.sendRichMessage(Confs.tl().commands().set().dues().defaultDues().getSet(),
                Placeholder.unparsed("amount", Econ.moneyString(amount)));
    }

    private void handleRole(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().set().dues();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        if (!checkEnabled(sender)) {
            return;
        }
        Role role = resolveRole(sender, context.get("role"));
        if (role == null) {
            return;
        }
        double raw = context.get("amount");
        double amount = Math.max(0, raw);
        sender.faction().dues(role, amount);
        sender.sendRichMessage(tl.role().getSet(),
                Placeholder.unparsed("role", role.translation()),
                Placeholder.unparsed("amount", Econ.moneyString(amount)));
    }

    private void handleClear(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().set().dues();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        if (!checkEnabled(sender)) {
            return;
        }
        Role role = resolveRole(sender, context.get("role"));
        if (role == null) {
            return;
        }
        Faction faction = sender.faction();
        if (!faction.duesOverrides().containsKey(role)) {
            sender.sendRichMessage(tl.clear().getNotSet(), Placeholder.unparsed("role", role.translation()));
            return;
        }
        faction.dues(role, null);
        sender.sendRichMessage(tl.clear().getCleared(),
                Placeholder.unparsed("role", role.translation()),
                Placeholder.unparsed("amount", Econ.moneyString(faction.dues())));
    }

    private void handlePolicy(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().set().dues();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        if (!checkEnabled(sender)) {
            return;
        }
        String input = context.get("policy");
        DuesFailurePolicy policy = DuesFailurePolicy.fromString(input);
        if (policy == null) {
            sender.sendRichMessage(tl.policy().getInvalid(), Placeholder.unparsed("policy", input));
            return;
        }
        sender.faction().duesFailurePolicy(policy);
        sender.sendRichMessage(tl.policy().getSet(), Placeholder.unparsed("policy", policy.translation()));
    }

    private void handleStatus(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().set().dues();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        if (!checkEnabled(sender)) {
            return;
        }
        Faction faction = sender.faction();
        Map<Role, Double> overrides = faction.duesOverrides();

        if (faction.dues() <= 0 && overrides.values().stream().allMatch(v -> v <= 0)) {
            sender.sendRichMessage(tl.getStatusNone());
            return;
        }

        sender.sendRichMessage(tl.getStatusHeader());
        sender.sendRichMessage(tl.getStatusDefault(), Placeholder.unparsed("amount", Econ.moneyString(faction.dues())));
        for (Role role : Role.values()) {
            Double override = overrides.get(role);
            if (override != null) {
                sender.sendRichMessage(tl.getStatusOverride(),
                        Placeholder.unparsed("role", role.translation()),
                        Placeholder.unparsed("amount", Econ.moneyString(override)));
            }
        }
        sender.sendRichMessage(tl.getStatusPolicy(), Placeholder.unparsed("policy", faction.duesFailurePolicy().translation()));
    }

    private Role resolveRole(FPlayer sender, String roleString) {
        var tl = Confs.tl().commands().set().dues();
        Role role = Role.fromString(roleString);
        if (role == null) {
            sender.sendRichMessage(tl.getInvalidRole(), Placeholder.unparsed("role", roleString));
            return null;
        }
        if (role == Role.ADMIN) {
            sender.sendRichMessage(tl.getNotThatRole());
            return null;
        }
        return role;
    }
}
