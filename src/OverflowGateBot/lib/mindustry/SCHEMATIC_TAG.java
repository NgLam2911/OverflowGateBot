package OverflowGateBot.lib.mindustry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SCHEMATIC_TAG {
    SERPULO,
    EREKIR,
    // Resource
    COPPER,
    LEAD,
    COAL,
    SCRAP,
    GRAPHITE,
    METAGLASS,
    SILICON,
    SPORE_POD,
    TITANIUM,
    PLASTANIUM,
    THORIUM,
    PHASE_FABRIC,
    SURGE_ALLOY,
    BERYLIUM,
    TUNGSTEN,
    OXIDE,
    CARDIBE,
    // Liquid
    WATER,
    SLAG,
    OIL,
    CRYOFLUID,
    NEOPLASM,
    ARKYCITE,
    OZONE,
    HYDROGEN,
    NITROGEN,
    CYANOGEN,
    // Type
    UNIT,
    LOGIC,
    RESOURCE,
    DEFENSE,
    J4F,
    POWER,
    KICKSTART,
    SPAM,
    // Placement
    ON_CORE,
    REMOTE,
    ON_ORE,
    MASS_DRIVER,
    // Unit
    TIER1,
    TIER2,
    TIER3,
    TIER4,
    TIER5,
    // Mode
    CAMPAIGN,
    PVP,
    ATTACK,
    SANDBOX,
    HEX;

    public static List<String> getTags() {
        List<String> a = new ArrayList<String>();
        List<SCHEMATIC_TAG> temp = Arrays.asList(SCHEMATIC_TAG.values());
        temp.forEach(t -> a.add(t.name()));
        return a;
    }
}
