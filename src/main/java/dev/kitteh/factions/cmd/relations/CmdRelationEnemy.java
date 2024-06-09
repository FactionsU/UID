package dev.kitteh.factions.cmd.relations;

import dev.kitteh.factions.permissible.Relation;

public class CmdRelationEnemy extends FRelationCommand {

    public CmdRelationEnemy() {
        super(Relation.ENEMY, "enemy");
    }
}
