package starproject.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import starproject.dialogs.StarResonanceDialog;

public class StarResonanceAbility extends BaseDurationAbility {

    @Override
    protected void activateImpl() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        if (!hasResonanceShip(fleet)) {
            Global.getSector().getCampaignUI().addMessage(
                "You don't have a ship that can establish a resonance with a star."
            );
            return;
        }

        Global.getSector().getCampaignUI().showInteractionDialog(
            new StarResonanceDialog(),
            fleet
        );
    }

    @Override
    protected void applyEffect(float amount, float level) {
    }

    @Override
    protected void deactivateImpl() {
    }

    @Override
    protected void cleanupImpl() {
    }

    private boolean hasResonanceShip(CampaignFleetAPI fleet) {
        for (var member : fleet.getFleetData().getMembersListCopy()) {
            if (member.getHullId().equals("starproject_stellae") &&
                member.getVariant().hasHullMod("stellar_resonance_catalyst_hullmod")) {
                return true;
            }
        }
        return false;
    }
}