package dev.kitteh.factions.tagresolver;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class FactionResolver extends ObservedResolver {
    public static FactionResolver of(@Nullable FPlayer observer, Faction faction) {
        return new FactionResolver("faction", observer, faction);
    }

    public static FactionResolver of(@Nullable Player observer, Faction faction) {
        return new FactionResolver("faction", observer, faction);
    }

    private final Faction faction;

    public FactionResolver(String name, @Nullable FPlayer observer, Faction faction) {
        super(name, observer);
        this.faction = faction;
    }

    public FactionResolver(String name, @Nullable Player observer, Faction faction) {
        super(name, observer);
        this.faction = faction;
    }

    @Override
    protected Tag solve(ArgumentQueue arguments, Context ctx) {
        String main = arguments.hasNext() ? arguments.pop().lowerValue() : "";

        return switch (main) {
            case "", "name_decorated" -> tagLegacy(faction.describeToLegacy(observer));

            case "name" -> tag(faction.tag());

            case "description" -> tag(faction.description());

            case "link" -> tag(Component.text().content(faction.link()).clickEvent(ClickEvent.openUrl(faction.link())));

            case "create-date" -> tag(TL.sdf.format(faction.founded().toEpochMilli()));

            case "members_total_count" -> tag(faction.members().size());
            case "members_online_count" -> tag(faction.membersOnline(true, observer).size());
            case "members_offline_count" -> tag(faction.membersOnline(false, observer).size());

            case "id" -> tag(faction.id());

            case "claims_count" -> tag(faction.claimCount());
            case "claims_max" -> tag(FactionsPlugin.instance().landRaidControl().landLimit(faction));

            case "warps_count" -> tag(faction.warps().size());
            case "warps_max" -> tag(FactionsPlugin.instance().conf().commands().warp().getMaxWarps());

            case "power_exact" -> tag(faction.powerExact());
            case "power_rounded" -> tag(faction.power());
            case "power_max_exact" -> tag(faction.powerMaxExact());
            case "power_max_rounded" -> tag(faction.powerMax());
            case "power_boost" -> tag(faction.powerBoost());

            case "dtr_exact" -> {
                if (FactionsPlugin.instance().landRaidControl() instanceof DTRControl) {
                    yield tag(faction.dtr());
                } else {
                    yield tag((faction.power() - faction.claimCount()) / FactionsPlugin.instance().conf().factions().landRaidControl().power().getLossPerDeath());
                }
            }
            case "dtr_rounded" -> {
                if (FactionsPlugin.instance().landRaidControl() instanceof DTRControl) {
                    yield tag(DTRControl.round(faction.dtr()));
                } else {
                    yield tag(DTRControl.round((faction.power() - faction.claimCount()) / FactionsPlugin.instance().conf().factions().landRaidControl().power().getLossPerDeath()));
                }
            }
            case "dtr_max_exact" -> {
                if (FactionsPlugin.instance().landRaidControl() instanceof DTRControl dtrControl) {
                    yield tag(dtrControl.getMaxDTR(faction));
                } else {
                    yield tag(faction.powerMax() / FactionsPlugin.instance().conf().factions().landRaidControl().power().getLossPerDeath());
                }
            }
            case "dtr_max_rounded" -> {
                if (FactionsPlugin.instance().landRaidControl() instanceof DTRControl dtrControl) {
                    yield tag(DTRControl.round(dtrControl.getMaxDTR(faction)));
                } else {
                    yield tag(DTRControl.round(faction.powerMax() / FactionsPlugin.instance().conf().factions().landRaidControl().power().getLossPerDeath()));
                }
            }
            case "dtr_frozen_status" -> tag(TL.DTR_FROZEN_STATUS_MESSAGE.format(faction.dtrFrozen() ? TL.DTR_FROZEN_STATUS_TRUE.toString() : TL.DTR_FROZEN_STATUS_FALSE.toString()));
            case "dtr_frozen_time" -> tag(TL.DTR_FROZEN_TIME_MESSAGE.format(faction.dtrFrozen() ?
                    DurationFormatUtils.formatDuration(faction.dtrFrozenUntil() - System.currentTimeMillis(), FactionsPlugin.instance().conf().factions().landRaidControl().dtr().getFreezeTimeFormat()) :
                    TL.DTR_FROZEN_TIME_NOTFROZEN.toString()));

            case "raidable" -> tagLegacy(FactionsPlugin.instance().landRaidControl().isRaidable(faction) ? TL.RAIDABLE_TRUE : TL.RAIDABLE_FALSE);

            case "leader" -> faction.admin() instanceof FPlayer fp ? FPlayerResolver.of("leader", observer, fp).solve(arguments, ctx) : tag("");

            case "bank_balance" -> {
                if (Econ.shouldBeUsed() && FactionsPlugin.instance().conf().economy().isBankEnabled()) {
                    yield tag(Econ.moneyString(Econ.getBalance(faction)));
                }
                yield tag(0);
            }

            case "tnt_bank_balance" -> tag(FactionsPlugin.instance().conf().commands().tnt().isEnable() ? faction.tntBank() : 0);
            case "tnt_bank_max" -> tag(FactionsPlugin.instance().conf().commands().tnt().isEnable() ? faction.tntBankMax() : 0);

            case "allies_count" -> tag(faction.relationCount(Relation.ALLY));
            case "allies_max" -> tagLegacy(getRelation(Relation.ALLY));

            case "enemies_count" -> tag(faction.relationCount(Relation.ENEMY));
            case "enemies_max" -> tagLegacy(getRelation(Relation.ENEMY));

            case "truces_count" -> tag(faction.relationCount(Relation.TRUCE));
            case "truces_max" -> tagLegacy(getRelation(Relation.TRUCE));

            case "relation_name" -> tagLegacy(faction.relationTo(observer).translation());
            case "relation_color" -> tag(faction.relationTo(observer).color());

            default -> tag(Component.empty());
        };
    }

    private String getRelation(Relation relation) {
        if (FactionsPlugin.instance().conf().factions().maxRelations().isEnabled()) {
            return String.valueOf(relation.getMax());
        }
        return TL.GENERIC_INFINITY.toString();
    }
}
