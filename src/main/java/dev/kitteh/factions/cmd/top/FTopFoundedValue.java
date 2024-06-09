package dev.kitteh.factions.cmd.top;

import dev.kitteh.factions.util.TL;

public class FTopFoundedValue extends FTopGTNumberValue<FTopFoundedValue, Long> {
    public FTopFoundedValue(Long value) {
        super(value);
    }

    @Override
    public String getDisplayString() {
        return TL.sdf.format(value);
    }
}
