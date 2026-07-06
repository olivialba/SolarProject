package starproject;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;

public class StarProjectPlugin extends BaseModPlugin {

    @Override
    public void onNewGameAfterEconomyLoad() {
        MarketAPI market = Global.getSector().getEconomy().getMarket("jangala");
        market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo().addSpecial(
            new SpecialItemData("stellar_resonance_catalyst", null),
            5f
        );
    }
}