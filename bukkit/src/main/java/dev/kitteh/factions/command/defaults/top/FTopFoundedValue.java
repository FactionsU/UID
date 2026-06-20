package dev.kitteh.factions.command.defaults.top;

import java.text.SimpleDateFormat;

public class FTopFoundedValue extends FTopGTNumberValue<FTopFoundedValue, Long> {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/d/yy h:ma");

    public FTopFoundedValue(Long value) {
        super(value);
    }

    @Override
    public String getDisplayString() {
        return sdf.format(value);
    }
}
