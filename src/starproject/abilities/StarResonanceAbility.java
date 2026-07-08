package starproject.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import starproject.dialogs.StarResonanceDialog;

public class StarResonanceAbility extends BaseDurationAbility {

    @Override
    protected void activateImpl() {

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        if (!hasResonanceShip(fleet)) {
            Global.getSector().getCampaignUI().addMessage(
                "Requires the Stellae with the Stellar Resonance Catalyst installed."
            );
            return;
        }

        if (!isNearStar(fleet)) {
            Global.getSector().getCampaignUI().addMessage(
                "You must be near a star to initiate resonance."
            );
            return;
        }

        Global.getSector().getCampaignUI().showInteractionDialog(
            new StarResonanceDialog(),
            fleet
        );
    }


    private boolean hasResonanceShip(CampaignFleetAPI fleet) {

        for (var member : fleet.getFleetData().getMembersListCopy()) {
            if (member.getHullId().equals("starproject_stellae") && member.getVariant().hasHullMod("stellar_resonance_catalyst_hullmod")) {
                return true;
            }
        }
        return false;
    }


    private boolean isNearStar(CampaignFleetAPI fleet) {

        StarSystemAPI system = fleet.getStarSystem();

        if (system == null) {
            return false; // hyperspace
        }

        PlanetAPI star = system.getStar();

        if (star == null || star.getSpec().isBlackHole()) {
            return false;
        }

        float distance = Misc.getDistance(
            fleet.getLocation(),
            star.getLocation()
        );

        return distance < 1000f;
    }


    @Override
    protected void applyEffect(float amount, float level) {
        // Runs while ability is active
    }


    @Override
    protected void deactivateImpl() {
        // Cleanup after ability ends
    }


    @Override
    public boolean isUsable() {
        return true;
    }
}