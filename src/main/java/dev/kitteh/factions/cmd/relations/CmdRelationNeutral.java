package dev.kitteh.factions.cmd.relations;

import dev.kitteh.factions.permissible.Relation;

public class CmdRelationNeutral extends FRelationCommand {

    public CmdRelationNeutral() {
        super(Relation.NEUTRAL, "neutral");
    }
}
