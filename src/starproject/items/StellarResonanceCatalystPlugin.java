package starproject.items;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;

public class StellarResonanceCatalystPlugin extends BaseSpecialItemPlugin {

    @Override
    public String getName() {
        return "Stellar Resonance Catalyst";
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded,
                              CargoTransferHandlerAPI transferHandler, Object stackSource) {

        super.createTooltip(tooltip, expanded, transferHandler, stackSource);
        tooltip.addPara("Installation requires a compatible ship.", 5f);
    }

    @Override
    public boolean hasRightClickAction() {
        return true;
    }

    @Override
    public void performRightClickAction() {

        boolean installed = false;

        for (FleetMemberAPI m : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            Global.getLogger(this.getClass()).info(m.getHullId());
            if ((m.getHullId().equals("starproject_stellae")) && (m.getVariant().hasHullMod("empty_stellar_resonance_catalyst_hullmod"))) {
                
                m.getVariant().removePermaMod("empty_stellar_resonance_catalyst_hullmod");
                m.getVariant().addPermaMod("stellar_resonance_catalyst_hullmod");
                m.getVariant().addWeapon("WS 006", "stellar_resonance_lance");
                m.updateStats();

                installed = true;
                break;
            }
        }


        if (installed) {
            Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
                "Catalyst installed successfully!"
            );
        } else {
            Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
                "No compatible ship found in fleet."
            );
        }
    }
}