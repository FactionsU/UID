package dev.kitteh.factions.command;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.entity.Player;
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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@NullMarked
public class FactionParser implements ArgumentParser<Sender, Faction>, BlockingSuggestionProvider<Sender> {
    public static ParserDescriptor<Sender, Faction> of(Include... includes) {
        List<Include> includeList = new ArrayList<>();
        Collections.addAll(includeList, includes);
        return ParserDescriptor.of(new FactionParser(includeList), Faction.class);
    }

    private static final int SANE_SUGGESTION_LIMIT = 100;

    public enum Include {
        SAFE,
        SELF,
        WAR,
        WILD,
        PLAYERS
    }

    private final List<Include> includeFactions;

    private FactionParser(List<Include> includeFaction) {
        this.includeFactions = includeFaction;
    }

    @Override
    public ArgumentParseResult<Faction> parse(CommandContext<Sender> commandContext, CommandInput commandInput) {
        String name = commandInput.peekString();

        // First we try an exact match
        Faction faction = Factions.factions().get(name);

        // Now lets try for warzone / safezone. Helpful for custom warzone / safezone names.
        // Do this after we check for an exact match in case they rename the warzone / safezone
        // and a player created faction took one of the names.
        if (faction == null) {
            if (this.includeFactions.contains(Include.WAR) && name.equalsIgnoreCase("warzone")) {
                faction = Factions.factions().warZone();
            } else if (this.includeFactions.contains(Include.SAFE) && name.equalsIgnoreCase("safezone")) {
                faction = Factions.factions().safeZone();
            }
        }

        if (faction == null && this.includeFactions.contains(Include.PLAYERS)) {
            for (FPlayer fplayer : FPlayers.fPlayers().all()) {
                if (fplayer.getName().equalsIgnoreCase(name)) {
                    faction = fplayer.getFaction();
                    break;
                }
            }
        }

        if (faction == null) {
            return ArgumentParseResult.failure(new FactionParseException(name, commandContext));
        }

        commandInput.readString();
        return ArgumentParseResult.success(faction);
    }

    @Override
    public SuggestionProvider<Sender> suggestionProvider() {
        return this; // Saves an instanceof/cast, and reminds me that it does default to this
    }

    @Override
    public Iterable<? extends Suggestion> suggestions(CommandContext<Sender> context, CommandInput input) {
        List<String> output = new ArrayList<>();
        List<String> secondary = Factions.factions().all().stream().map(Faction::getTag).collect(Collectors.toCollection(ArrayList::new));

        Player sendingPlayer = context.sender() instanceof Sender.Player player ? player.player() : null;
        for (Player player : AbstractFactionsPlugin.getInstance().getServer().getOnlinePlayers()) {
            if (sendingPlayer != null && !player.canSee(player)) {
                continue;
            }
            Faction f = FPlayers.fPlayers().get(player).getFaction();
            if (!output.contains(f.getTag())) {
                output.add(f.getTag());
                secondary.remove(f.getTag());
            }
        }

        Collections.sort(output);
        Collections.sort(secondary);

        if (this.includeFactions.contains(Include.PLAYERS)) {
            boolean isPlayer = context.sender().isPlayer();
            int count = output.size();

            for (FPlayer player : FPlayers.fPlayers().online()) {
                if (count > SANE_SUGGESTION_LIMIT) {
                    break;
                }
                if (isPlayer && !((Sender.Player) context.sender()).player().canSee(player.getPlayer())) {
                    continue;
                }
                output.add(player.getName());
                count++;
            }
        }

        Iterator<String> it = secondary.iterator();
        for (int i = output.size(); i < SANE_SUGGESTION_LIMIT && it.hasNext(); i++) {
            output.add(it.next());
        }

        if (!this.includeFactions.contains(Include.SELF) && context.sender() instanceof Sender.Player player && player.faction().isNormal()) {
            output.remove(player.faction().getTag());
        }
        if (!this.includeFactions.contains(Include.SAFE)) {
            output.remove(Factions.factions().safeZone().getTag());
        }
        if (!this.includeFactions.contains(Include.WAR)) {
            output.remove(Factions.factions().warZone().getTag());
        }
        if (!this.includeFactions.contains(Include.WILD)) {
            output.remove(Factions.factions().wilderness().getTag());
        }

        return output.stream().map(Suggestion::suggestion).toList();
    }

    public static final class FactionParseException extends ParserException {
        private final String input;
        private final CommandContext<Sender> context;

        public FactionParseException(final String input, final CommandContext<Sender> context) {
            super(FactionParser.class, context, Captioner.NO_FACTION_FOUND, CaptionVariable.of("input", input));
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