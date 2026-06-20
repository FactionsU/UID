package dev.kitteh.factions.command.defaults.top;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FTopFoundedValue extends FTopGTNumberValue<FTopFoundedValue, Long> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/d/yy h:ma").withZone(ZoneId.systemDefault());

    public FTopFoundedValue(Long value) {
        super(value);
    }

    @Override
    public String getDisplayString() {
        return FORMATTER.format(Instant.ofEpochMilli(value));
    }
}
