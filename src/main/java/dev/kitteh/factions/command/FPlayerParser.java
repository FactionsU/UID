package dev.kitteh.factions.command;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.permissible.Role;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NullMarked
public class FPlayerParser implements ArgumentParser<Sender, FPlayer>, BlockingSuggestionProvider<Sender> {
    public static ParserDescriptor<Sender, FPlayer> of(Include primary, Include... includes) {
        List<Include> includeList = new ArrayList<>();
        includeList.add(primary);
        Collections.addAll(includeList, includes);
        return ParserDescriptor.of(new FPlayerParser(includeList), FPlayer.class);
    }

    private static final int SANE_SUGGESTION_LIMIT = 100;

    public enum Include {
        SELF,
        ROLE_AT_OR_BELOW,
        ROLE_BELOW,
        SAME_FACTION,
        OTHER_FACTION,
        NO_FACTION,
        ONLINE,
        ALL
    }

    private final List<Include> includePlayers;

    private FPlayerParser(List<Include> includePlayers) {
        this.includePlayers = includePlayers;
    }

    @Override
    public ArgumentParseResult<FPlayer> parse(CommandContext<Sender> commandContext, CommandInput commandInput) {
        String name = commandInput.peekString();

        for (FPlayer fplayer : FPlayers.fPlayers().all()) {
            if (fplayer.getName().equalsIgnoreCase(name)) {
                commandInput.readString();
                return ArgumentParseResult.success(fplayer);
            }
        }

        return ArgumentParseResult.failure(new FPlayerParseException(name, commandContext));
    }

    @Override
    public SuggestionProvider<Sender> suggestionProvider() {
        return this; // Saves an instanceof/cast, and reminds me that it does default to this
    }

    @Override
    public Iterable<? extends Suggestion> suggestions(CommandContext<Sender> context, CommandInput input) {
        List<Suggestion> finalList = new ArrayList<>();

        List<String> temp1 = new ArrayList<>();
        List<String> temp2 = new ArrayList<>();
        int count = 0;

        boolean isPlayer = context.sender().isPlayer();

        singingItForeverJustBecause:
        // this is the loop that doesn't end
        for (Include include : this.includePlayers) {
            temp1.clear();
            temp2.clear();
            switch (include) {
                case SELF:
                    if (context.sender() instanceof Sender.Player player) {
                        finalList.add(Suggestion.suggestion(player.player().getName()));
                    }
                    break;
                case ROLE_AT_OR_BELOW:
                    if (context.sender() instanceof Sender.Player player && player.hasFaction()) {
                        Role role = player.fPlayer().getRole();
                        for (FPlayer member : player.faction().getFPlayers()) {
                            if (member.getRole().isAtMost(role)) {
                                (member.isOnline() ? temp1 : temp2).add(member.getName());
                            }
                        }
                    }
                    break;
                case ROLE_BELOW:
                    if (context.sender() instanceof Sender.Player player && player.hasFaction()) {
                        Role role = Role.getRelative(player.fPlayer().getRole(), -1);
                        if (role == null) {
                            break;
                        }
                        for (FPlayer member : player.faction().getFPlayers()) {
                            if (member.getRole().isAtMost(role)) {
                                (member.isOnline() ? temp1 : temp2).add(member.getName());
                            }
                        }
                    }
                    break;
                case SAME_FACTION:
                    if (context.sender() instanceof Sender.Player player && player.hasFaction()) {
                        for (FPlayer member : player.faction().getFPlayers()) {
                            (member.isOnline() ? temp1 : temp2).add(member.getName());
                        }
                    }
                    break;
                case OTHER_FACTION:
                    Faction faction = context.sender() instanceof Sender.Player player && player.faction().isNormal() ? player.faction() : null;
                    for (FPlayer player : FPlayers.fPlayers().online()) {
                        if (count > SANE_SUGGESTION_LIMIT) {
                            continue singingItForeverJustBecause;
                        }
                        if (isPlayer && !((Sender.Player) context.sender()).player().canSee(player.getPlayer())) {
                            continue;
                        }
                        if (player.getFaction() != faction) {
                            temp1.add(player.getName());
                            count++;
                        }
                    }
                    for (FPlayer player : FPlayers.fPlayers().all()) {
                        if (count > SANE_SUGGESTION_LIMIT) {
                            continue singingItForeverJustBecause;
                        }
                        if (player.getFaction() != faction && !temp1.contains(player.getName())) {
                            temp2.add(player.getName());
                            count++;
                        }
                    }
                    break;
                case NO_FACTION:
                    for (FPlayer player : FPlayers.fPlayers().online()) {
                        if (count > SANE_SUGGESTION_LIMIT) {
                            continue singingItForeverJustBecause;
                        }
                        if (isPlayer && !((Sender.Player) context.sender()).player().canSee(player.getPlayer())) {
                            continue;
                        }
                        if (player.getFaction().isWilderness()) {
                            temp1.add(player.getName());
                            count++;
                        }
                    }
                    for (FPlayer player : FPlayers.fPlayers().all()) {
                        if (count > SANE_SUGGESTION_LIMIT) {
                            continue singingItForeverJustBecause;
                        }
                        if (player.getFaction().isWilderness() && !temp1.contains(player.getName())) {
                            temp2.add(player.getName());
                            count++;
                        }
                    }
                    break;
                case ONLINE:
                    for (FPlayer player : FPlayers.fPlayers().online()) {
                        if (count > SANE_SUGGESTION_LIMIT) {
                            continue singingItForeverJustBecause;
                        }
                        if (isPlayer && !((Sender.Player) context.sender()).player().canSee(player.getPlayer())) {
                            continue;
                        }
                        temp1.add(player.getName());
                        count++;
                    }
                case ALL:
                    for (FPlayer player : FPlayers.fPlayers().online()) {
                        if (count > SANE_SUGGESTION_LIMIT) {
                            continue singingItForeverJustBecause;
                        }
                        if (isPlayer && !((Sender.Player) context.sender()).player().canSee(player.getPlayer())) {
                            continue;
                        }
                        temp1.add(player.getName());
                        count++;
                    }
                    for (FPlayer player : FPlayers.fPlayers().all()) {
                        if (count > SANE_SUGGESTION_LIMIT) {
                            continue singingItForeverJustBecause;
                        }
                        if (!temp1.contains(player.getName())) {
                            temp2.add(player.getName());
                            count++;
                        }
                    }
                    break;
            }
            Collections.sort(temp1);
            Collections.sort(temp2);
            temp1.stream().distinct().forEach(s -> finalList.add(Suggestion.suggestion(s)));
            temp2.stream().distinct().forEach(s -> finalList.add(Suggestion.suggestion(s)));
        }
        return finalList;
    }

    public static final class FPlayerParseException extends ParserException {
        private final String input;
        private final CommandContext<Sender> context;

        public FPlayerParseException(final String input, final CommandContext<Sender> context) {
            super(FPlayerParser.class, context, Captioner.NO_PLAYER_FOUND, CaptionVariable.of("input", input));
            this.context = context;
            this.input = input;
        }

        public CommandContext<Sender> getContext() {
            return this.context;
        }

        public String getInput() {
            return this.input;
        }
    }
}