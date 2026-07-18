package dev.kitteh.factions.command.paper;

import dev.kitteh.factions.policy.DuesFailurePolicy;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class CmdSetDues implements Cmd {
    private static final List<Role> OVERRIDE_ROLES = List.of(Role.COLEADER, Role.MODERATOR, Role.NORMAL, Role.RECRUIT);

    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().set().dues();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission()
                                    .and(Cloudy.hasPermission(Permission.DUES).and(Cloudy.isAtLeastRole(Role.ADMIN)).and(Cloudy.predicate(_ -> Econ.duesEnabled()))))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        if (!Econ.duesEnabled()) {
            context.sender().sendRichMessage(Confs.tl().commands().set().dues().getDisabled());
            return;
        }
        ((Sender.Player) context.sender()).player().showDialog(this.menu(((Sender.Player) context.sender()).fPlayer()));
    }

    private Dialog menu(FPlayer sender) {
        var tl = Confs.tl().commands().set().dues().paper();
        Faction faction = sender.faction();
        Map<Role, Double> overrides = faction.duesOverrides();

        List<DialogInput> inputs = new ArrayList<>();
        inputs.add(DialogInput.text("default", Mini.parse(tl.getDefaultLabel(), sender))
                .width(300)
                .initial(numberString(faction.dues()))
                .build());
        for (Role role : OVERRIDE_ROLES) {
            Double override = overrides.get(role);
            inputs.add(DialogInput.text(role.name(), Mini.parse(tl.getRoleLabel(), sender, Placeholder.unparsed("role", role.translation())))
                    .width(300)
                    .initial(override == null ? "" : numberString(override))
                    .build());
        }

        DuesFailurePolicy currentPolicy = faction.duesFailurePolicy();
        List<SingleOptionDialogInput.OptionEntry> policyOptions = new ArrayList<>();
        for (DuesFailurePolicy policy : DuesFailurePolicy.values()) {
            policyOptions.add(SingleOptionDialogInput.OptionEntry.create(
                    policy.name(), Mini.parse(policy.translation(), sender), policy == currentPolicy));
        }
        inputs.add(DialogInput.singleOption("policy", Mini.parse(tl.getPolicyLabel(), sender), policyOptions)
                .width(300)
                .build());

        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.getTitle(), sender))
                        .body(Dialogue.body(tl.getBody()))
                        .inputs(inputs)
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Mini.parse(tl.getConfirm(), sender))
                                .action(DialogAction.customClick((response, audience) -> {
                                    Parsed defaultValue = parse(response.getText("default"));
                                    if (!defaultValue.valid()) {
                                        audience.sendMessage(Mini.parse(tl.getInvalidNumber(), sender, Placeholder.unparsed("label", tl.getDefaultLabel())));
                                        return;
                                    }
                                    Map<Role, Parsed> parsed = new LinkedHashMap<>();
                                    for (Role role : OVERRIDE_ROLES) {
                                        Parsed value = parse(response.getText(role.name()));
                                        if (!value.valid()) {
                                            audience.sendMessage(Mini.parse(tl.getInvalidNumber(), sender,
                                                    Placeholder.unparsed("label", role.translation())));
                                            return;
                                        }
                                        parsed.put(role, value);
                                    }

                                    faction.dues(defaultValue.present() ? defaultValue.value() : 0d);
                                    for (Role role : OVERRIDE_ROLES) {
                                        Parsed value = parsed.get(role);
                                        faction.dues(role, value.present() ? value.value() : null);
                                    }
                                    String policyId = response.getText("policy");
                                    DuesFailurePolicy policy = policyId == null ? null : DuesFailurePolicy.fromString(policyId);
                                    if (policy != null) {
                                        faction.duesFailurePolicy(policy);
                                    }
                                    audience.sendMessage(Mini.parse(tl.getSaved(), sender));
                                }, Dialogue.CLICK_CALLBACK))
                                .build(),
                        ActionButton.builder(Mini.parse(tl.getCancel(), sender)).build()
                )));
    }

    private record Parsed(boolean valid, boolean present, double value) {
        private static final Parsed BLANK = new Parsed(true, false, 0d);
        private static final Parsed INVALID = new Parsed(false, false, 0d);
    }

    private Parsed parse(String text) {
        if (text == null || text.isBlank()) {
            return Parsed.BLANK;
        }
        try {
            double value = Double.parseDouble(text.trim());
            if (value < 0 || Double.isNaN(value) || Double.isInfinite(value)) {
                return Parsed.INVALID;
            }
            return new Parsed(true, true, value);
        } catch (NumberFormatException e) {
            return Parsed.INVALID;
        }
    }

    private static String numberString(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }
}
