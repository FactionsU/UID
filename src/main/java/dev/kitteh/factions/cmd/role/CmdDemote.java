package dev.kitteh.factions.cmd.role;

public class CmdDemote extends FPromoteCommand {

    public CmdDemote() {
        aliases.add("demote");
        this.relative = -1;
    }
}
