package dev.kitteh.factions.cmd.top;

import dev.kitteh.factions.integration.Econ;

public class FTopBalanceValue extends FTopGTNumberValue<FTopBalanceValue, Double> {
    public FTopBalanceValue(Double value) {
        super(value);
    }

    @Override
    public String getDisplayString() {
        return Econ.moneyString(value);
    }
}
