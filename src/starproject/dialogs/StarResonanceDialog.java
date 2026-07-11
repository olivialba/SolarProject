package starproject.dialogs;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;

public class StarResonanceDialog implements InteractionDialogPlugin {

    private InteractionDialogAPI dialog;
    private OptionPanelAPI options;
    private TextPanelAPI text;


    @Override
    public void init(InteractionDialogAPI dialog) {

        this.dialog = dialog;
        this.options = dialog.getOptionPanel();
        this.text = dialog.getTextPanel();

        showMain();
    }


    private void showMain() {
        text.clear();
        options.clearOptions();

        if (!canUseAtLocation()) {
            text.addParagraph(
                "Captain, the Catalyst cannot synchronize with the surrounding stellar field. " +
                "We'll need to approach a suitable star before resonance can be initiated."
            );
        }
        else {
            text.addParagraph(
                "Captain, the Catalyst has synchronized with the star's hyperfield. " +
                "Current distance is well within operational tolerances. We're ready to initiate stellar resonance on your order."
            );
            options.addOption(
                "Option 1",
                "OPTION_1"
            );

            options.addOption(
                "Option 2",
                "OPTION_2"
            );

            options.addOption(
                "Option 3",
                "OPTION_3"
            );
        }


        options.addOption(
            "Leave",
            "LEAVE"
        );
    }


    private boolean canUseAtLocation() {

        CampaignFleetAPI fleet =
            Global.getSector().getPlayerFleet();

        StarSystemAPI system = fleet.getStarSystem();
        if (system == null) {
            return false; // hyperspace
        }

        PlanetAPI star = system.getStar();
        if (star == null || star.getSpec().isBlackHole()) {
            return false; // no star or blackhole
        }

        float distance = Misc.getDistance(
            fleet.getLocation(),
            star.getLocation()
        );

        Global.getLogger(this.getClass()).info("Distance from star: " + distance);
        return distance < 1600f;
    }


    @Override
    public void optionSelected(String text, Object optionData) {

        if ("LEAVE".equals(optionData)) {
            dialog.dismiss();
            return;
        }

        if ("OPTION_1".equals(optionData)) {
            spawnResonanceFlash();

            dialog.dismiss();
            return;
        }

        if ("OPTION_2".equals(optionData)) {
            // future action
        }

        if ("OPTION_3".equals(optionData)) {
            // future action
        }
    }


    private void spawnResonanceFlash() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) {
            return;
        }

        LocationAPI location = fleet.getContainingLocation();
        if (location == null) {
            return;
        }

        CustomCampaignEntityAPI beam = location.addCustomEntity(
            null,
            null,
            "star_resonance_beam",
            "neutral"
        );
        if (beam == null) {
            return;
        }

        beam.setFixedLocation(fleet.getLocation().x, fleet.getLocation().y);
        beam.setRadius(1f);

        Misc.fadeAndExpire(beam, 12f);
    }


    @Override public void advance(float amount) {}
    @Override public void backFromEngagement(EngagementResultAPI result) {}
    @Override public Object getContext() { return null; }
    @Override public Map<String, MemoryAPI> getMemoryMap() { return null; }
    @Override public void optionMousedOver(String optionText, Object optionData) {}
}