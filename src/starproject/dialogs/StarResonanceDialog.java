package starproject.dialogs;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
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

        if (!canUseAtLocation()) {
            text.addParagraph(
                "The Stellar Resonance Catalyst cannot establish a resonance. " +
                "You must be near a suitable star."
            );
        }
        else {
            text.addParagraph(
                "The Stellar Resonance Catalyst detects a compatible stellar source."
            );
        }


        options.clearOptions();

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

        return distance < 1000f;
    }


    @Override
    public void optionSelected(String text, Object optionData) {

        if ("LEAVE".equals(optionData)) {
            dialog.dismiss();
            return;
        }

        if ("OPTION_1".equals(optionData)) {
            // future action
        }

        if ("OPTION_2".equals(optionData)) {
            // future action
        }

        if ("OPTION_3".equals(optionData)) {
            // future action
        }
    }


    @Override public void advance(float amount) {}
    @Override public void backFromEngagement(EngagementResultAPI result) {}
    @Override public Object getContext() { return null; }
    @Override public Map<String, MemoryAPI> getMemoryMap() { return null; }
    @Override public void optionMousedOver(String optionText, Object optionData) {}
}