package dev.kitteh.factions.command.paper;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Mini;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class CmdShield extends dev.kitteh.factions.command.defaults.CmdShield {
    @Override
    protected Command.Builder<Sender> registerSchedule(Command.Builder<Sender> shield) {
        return shield.handler(this::handleSchedule);
    }

    private void handleSchedule(CommandContext<Sender> context) {
        FPlayer fPlayer = context.sender().fPlayerOrNull();
        Player player = fPlayer.asPlayer();

        this.statusDialog(fPlayer, player);
    }

    private void statusDialog(Audience audience) {
        Player player = (Player) audience;
        FPlayer fPlayer = FPlayers.fPlayers().get(player);

        this.statusDialog(fPlayer, player);
    }

    private void statusDialog(FPlayer fPlayer, Player player) {
        Faction faction = fPlayer.faction();

        var tl = FactionsPlugin.instance().tl().commands().shield();

        FactionResolver factionResolver = FactionResolver.of(faction);
        List<DialogBody> dialogBody;

        LocalTime scheduledTime = faction.shieldDailyScheduleTime();
        if (scheduledTime == null) {
            dialogBody = Dialogue.body(tl.getScheduleMenuStatusBodyNotSet(),
                    factionResolver,
                    Placeholder.unparsed("currenttime", LocalTime.now().format(tl.getScheduleMenuTimeFormat()))
            );
        } else {
            dialogBody = Dialogue.body(tl.getScheduleMenuStatusBodyNotSet(),
                    FactionResolver.of(faction),
                    Placeholder.unparsed("currenttime", LocalTime.now().format(tl.getScheduleMenuTimeFormat())),
                    Placeholder.unparsed("scheduledtime", scheduledTime.format(tl.getScheduleMenuTimeFormat()))
            );
        }

        player.showDialog(Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.getScheduleMenuStatusTitle(), fPlayer))
                        .body(dialogBody)
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Mini.parse(tl.getScheduleMenuStatusButtonSetSchedule(), fPlayer, factionResolver))
                                .action(DialogAction.customClick(
                                        (r, audience) -> audience.showDialog(this.schedulingMenu(audience))
                                        , Dialogue.CLICK_CALLBACK)).build(),
                        ActionButton.builder(Mini.parse(tl.getScheduleMenuStatusButtonDone(), fPlayer, factionResolver)).build()
                ))));
    }

    private Dialog schedulingMenu(Audience audience) {
        Player player = (Player) audience;
        FPlayer fPlayer = FPlayers.fPlayers().get(player);
        Faction faction = fPlayer.faction();

        var tl = FactionsPlugin.instance().tl().commands().shield();

        FactionResolver factionResolver = FactionResolver.of(faction);

        List<DialogBody> dialogBody = Dialogue.body(tl.getScheduleMenuStatusBodyNotSet(),
                FactionResolver.of(faction),
                Placeholder.unparsed("currenttime", LocalTime.now().format(tl.getScheduleMenuTimeFormat()))
        );

        int width = tl.getScheduleMenuSetWidth();
        List<ActionButton> timeButtons = new ArrayList<>();

        LocalTime time = LocalTime.MIDNIGHT;

        for (int x = 0; x < 48; x++) {
            final LocalTime chosenTime = time;
            timeButtons.add(ActionButton.builder(Mini.parse(tl.getScheduleMenuSetButtonTime(), fPlayer,
                            Placeholder.unparsed("time", chosenTime.format(tl.getScheduleMenuTimeFormat()))))
                    .width(width)
                    .action(DialogAction.customClick(
                            (r, a) -> {
                                faction.shieldDailyScheduleTime(chosenTime);
                                this.statusDialog(a);
                            }
                            , Dialogue.CLICK_CALLBACK))
                    .build());
            time = time.plusMinutes(30);
        }

        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.getScheduleMenuStatusTitle(), fPlayer))
                        .body(dialogBody)
                        .build())
                .type(DialogType.multiAction(
                        timeButtons,
                        ActionButton.builder(Mini.parse(tl.getScheduleMenuSetButtonCancel(), fPlayer, factionResolver)).build(),
                        tl.getScheduleMenuSetColumns()
                )));
    }
}
