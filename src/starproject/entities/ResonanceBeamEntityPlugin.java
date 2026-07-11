package starproject.entities;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.util.Misc;

public class ResonanceBeamEntityPlugin extends BaseCustomEntityPlugin {

    private static final float BEAM_WIDTH = 78f;
    private static final float BEAM_ALPHA = 0.92f;
    private static final float BEAM_PULSE_SPEED = 6f;
    private static final float BEAM_SCROLL_SPEED = 0.12f;

    private static final Color BEAM_COLOR = new Color(255, 230, 175);

    private SpriteAPI beamSprite;
    private float elapsed;

    @Override
    public void init(SectorEntityToken entity, Object pluginParams) {
        super.init(entity, pluginParams);
        ((CustomCampaignEntityAPI) entity).setRadius(1f);

        beamSprite = Global.getSettings().getSprite("graphics/fx/beamfringe.png");
        beamSprite.setCenter(beamSprite.getWidth() * 0.5f, beamSprite.getHeight() * 0.5f);
    }

    @Override
    public void advance(float amount) {
        elapsed += amount;
    }

    @Override
    public float getRenderRange() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) {
            return 1000f;
        }

        StarSystemAPI system = fleet.getStarSystem();
        if (system == null) {
            return 1000f;
        }

        PlanetAPI star = system.getStar();
        if (star == null) {
            return 1000f;
        }

        return Misc.getDistance(entity.getLocation(), fleet.getLocation()) + 1000f;
    }

    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        if (layer != CampaignEngineLayers.STATIONS) {
            return;
        }

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) {
            return;
        }

        StarSystemAPI system = fleet.getStarSystem();
        if (system == null) {
            return;
        }

        PlanetAPI star = system.getStar();
        if (star == null || star.getSpec().isBlackHole()) {
            return;
        }

        float viewportAlpha = viewport.getAlphaMult();
        float sensorAlpha = entity.getSensorFaderBrightness() * entity.getSensorContactFaderBrightness();
        float pulse = 0.88f + 0.12f * (float) Math.sin(elapsed * BEAM_PULSE_SPEED);
        float alphaMult = viewportAlpha * sensorAlpha * pulse;
        if (alphaMult <= 0f) {
            return;
        }

        float fromX = entity.getLocation().x;
        float fromY = entity.getLocation().y;
        float toX = fleet.getLocation().x;
        float toY = fleet.getLocation().y;

        float distance = Misc.getDistance(fromX, fromY, toX, toY);
        if (distance <= 1f) {
            return;
        }

        float angle = Misc.getAngleInDegrees(entity.getLocation(), fleet.getLocation());
        float midX = (fromX + toX) * 0.5f;
        float midY = (fromY + toY) * 0.5f;

        float scroll = elapsed * BEAM_SCROLL_SPEED;
        float texWidth = beamSprite.getTextureWidth();
        float texHeight = beamSprite.getTextureHeight();
        float texSpan = Math.max(0.25f, Math.min(1f, distance / 3000f));
        float texX = (scroll % 1f) * texWidth;

        beamSprite.setAdditiveBlend();
        beamSprite.setAngle(angle);
        beamSprite.setColor(BEAM_COLOR);
        beamSprite.setAlphaMult(alphaMult * BEAM_ALPHA);
        beamSprite.setSize(distance, BEAM_WIDTH);
        beamSprite.setTexX(texX);
        beamSprite.setTexY(0f);
        beamSprite.setTexWidth(texWidth * texSpan);
        beamSprite.setTexHeight(texHeight);
        beamSprite.renderAtCenter(midX, midY);
    }
}
