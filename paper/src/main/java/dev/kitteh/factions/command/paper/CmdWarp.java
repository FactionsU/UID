package dev.kitteh.factions.command.paper;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.LazyLocation;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class CmdWarp implements Cmd {
    private static final ClickCallback.Options OPT = ClickCallback.Options.builder().build();

    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().warp();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.WARP).and(Cloudy.hasSelfFactionPerms(PermissibleActions.WARP).or(Cloudy.isBypass()))))
                            .optional("warp", StringParser.stringParser())
                            .flag(manager.flagBuilder("password").withComponent(StringParser.stringParser()))
                            .flag(manager.flagBuilder("faction").withComponent(FactionParser.of()))
                            .handler(ctx -> dev.kitteh.factions.command.defaults.CmdWarp.handle(ctx, this::menu))
            );
        };
    }

    private void menu(Sender sender, Faction faction) {
        Player player = ((Sender.Player) sender).player();

        player.showDialog(menuWarp(sender, faction));
    }

    private Dialog menuWarp(Sender sender, Faction faction) {
        FactionResolver factionResolver = FactionResolver.of(sender.fPlayerOrNull(), faction);
        var tl = FactionsPlugin.instance().tl().commands().warp();
        List<ActionButton> warps = faction.warps().keySet().stream()
                .map(warp -> {
                    ActionButton.Builder builder = ActionButton.builder(Mini.parse(tl.getMenuWarpName(), Placeholder.unparsed("warp", warp)));
                    if (faction.hasWarpPassword(warp)) {
                        return builder.action(DialogAction.customClick((r, audience) ->
                                audience.showDialog(this.password(sender, warp, faction, factionResolver)), OPT)).build();
                    } else {
                        return builder.action(DialogAction.customClick(
                                (r, audience) -> warp(faction, warp, sender), OPT)
                        ).build();
                    }
                })
                .toList();

        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.getMenuTitle(), factionResolver)).body(this.body(tl.getMenuBody(), factionResolver)).build())
                .type(DialogType.multiAction(
                        warps,
                        ActionButton.builder(Mini.parse(tl.getMenuCancel(), factionResolver)).build(),
                        2
                )));
    }

    private Dialog password(Sender sender, String warpName, Faction faction, FactionResolver factionResolver) {
        var tl = FactionsPlugin.instance().tl().commands().warp();
        TagResolver warp = Placeholder.unparsed("warp", warpName);
        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.getMenuPassTitle(), warp, factionResolver))
                        .body(this.body(tl.getMenuPassBody(), warp, factionResolver))
                        .inputs(List.of(
                                DialogInput.text("password", Mini.parse(tl.getMenuPassInputLabel()))
                                        .width(300)
                                        .build()
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Mini.parse(tl.getMenuPassConfirm(), factionResolver, warp))
                                .action(DialogAction.customClick(
                                        (r, audience) ->
                                        {
                                            String passAttempt = r.getText("password");
                                            if (passAttempt != null && faction.isWarpPassword(warpName, passAttempt)) {
                                                warp(faction, warpName, sender);
                                            } else if (faction.isWarp(warpName)) {
                                                audience.showDialog(password(sender, warpName, faction, factionResolver));
                                            } else {
                                                audience.showDialog(menuWarp(sender, faction));
                                            }
                                        }
                                        , OPT)).build(),
                        ActionButton.builder(Mini.parse(tl.getMenuCancel(), factionResolver)).build()
                )));
    }

    private void warp(Faction faction, String warp, Sender sender) {
        var tl = FactionsPlugin.instance().tl().commands().warp();
        LazyLocation destination = faction.warp(warp);
        if (destination == null) {
            sender.sendRichMessage(tl.getInvalidWarp(), Placeholder.unparsed("warp", warp));
        } else {
            dev.kitteh.factions.command.defaults.CmdWarp.teleport(sender.fPlayerOrNull(), faction, warp, sender, destination);
        }
    }

    private List<DialogBody> body(List<String> body, TagResolver... tagResolvers) {
        return List.of(DialogBody.plainMessage(Mini.parse(body, tagResolvers), 400));
    }
}
