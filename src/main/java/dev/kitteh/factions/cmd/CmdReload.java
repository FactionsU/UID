package dev.kitteh.factions.cmd;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;

public class CmdReload extends FCommand {

    public CmdReload() {
        super();
        this.aliases.add("reload");

        this.requirements = new CommandRequirements.Builder(Permission.RELOAD).noDisableOnLock().build();
    }

    @Override
    public void perform(CommandContext context) {
        long timeInitStart = System.currentTimeMillis();
        FactionsPlugin.getInstance().getConfigManager().loadConfigs();
        FactionsPlugin.getInstance().reloadConfig();
        FactionsPlugin.getInstance().loadLang();
        long timeReload = (System.currentTimeMillis() - timeInitStart);

        context.msg(TL.COMMAND_RELOAD_TIME, timeReload);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_RELOAD_DESCRIPTION;
    }
}
