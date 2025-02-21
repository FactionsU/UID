package dev.kitteh.factions.command.defaults.top;

public class FTopGTIntValue extends FTopGTNumberValue<FTopGTIntValue, Integer> {
    public FTopGTIntValue(Integer value) {
        super(value);
    }

    @Override
    public String getDisplayString() {
        return String.valueOf(this.value);
    }
}
