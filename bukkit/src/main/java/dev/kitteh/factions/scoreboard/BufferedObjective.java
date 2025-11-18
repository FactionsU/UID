package dev.kitteh.factions.scoreboard;

import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.TriFunction;
import net.kyori.adventure.text.Component;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ApiStatus.Internal
public class BufferedObjective {
    public static Consumer<Objective> objectiveConsumer = c -> {
    };
    public static TriFunction<Objective, Component, Integer, Score> scoreFunction = (objective, component, lineNum) -> objective.getScore(Mini.toLegacy(component));
    public static BiConsumer<Objective, Component> titleConsumer = (objective, component) -> objective.setDisplayName(Mini.toLegacy(component));

    private final Scoreboard scoreboard;
    private final String baseName;

    private Objective current;
    private DisplaySlot displaySlot;

    private int objPtr;
    private boolean requiresUpdate = false;

    private Component title;
    private final Map<Integer, Component> contents = new HashMap<>();

    public BufferedObjective(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
        this.baseName = createBaseName();

        String name = getNextObjectiveName();
        current = scoreboard.registerNewObjective(name, Criteria.DUMMY, name);
        objectiveConsumer.accept(current);
    }

    private String createBaseName() {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        while (builder.length() < 14) {
            builder.append(Integer.toHexString(random.nextInt()));
        }
        return builder.substring(0, 14);
    }

    public void setTitle(Component title) {
        if (this.title == null || !this.title.equals(title)) {
            this.title = title;
            requiresUpdate = true;
        }
    }

    public void setDisplaySlot(DisplaySlot slot) {
        this.displaySlot = slot;
        current.setDisplaySlot(slot);
    }

    public void setAllLines(List<Component> lines) {
        if (lines.size() != contents.size()) {
            contents.clear();
        }
        for (int i = 0; i < lines.size(); i++) {
            setLine(lines.size() - i, lines.get(i));
        }
    }

    private void setLine(int lineNumber, Component content) {
        if (contents.get(lineNumber) == null || !contents.get(lineNumber).equals(content)) {
            contents.put(lineNumber, content);
            requiresUpdate = true;
        }
    }

    public void flip() {
        if (!requiresUpdate) {
            return;
        }
        requiresUpdate = false;

        String objectiveName = getNextObjectiveName();
        Objective buffer = scoreboard.registerNewObjective(objectiveName, Criteria.DUMMY, objectiveName);
        objectiveConsumer.accept(buffer); // Extra processing of objective on paper for number format
        titleConsumer.accept(buffer, title); // Set title

        for (Map.Entry<Integer, Component> entry : contents.entrySet()) {
            // Create/set title
            scoreFunction.apply(buffer, entry.getValue(), entry.getKey()).setScore(entry.getKey());
        }

        if (displaySlot != null) {
            buffer.setDisplaySlot(displaySlot);
        }

        // Unregister _ALL_ the old things
        current.unregister();

        current = buffer;
    }

    private String getNextObjectiveName() {
        return baseName + "_" + ((objPtr++) % 2);
    }
}
