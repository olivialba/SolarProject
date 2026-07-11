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

        float dirX = toX - fromX;
        float dirY = toY - fromY;
        float distance = Misc.getDistance(fromX, fromY, toX, toY);
        if (distance <= 1f) {
            return;
        }

        float invDistance = 1f / distance;
        float beamDistance = distance;

        float px = -dirY * invDistance;
        float py = dirX * invDistance;
        float halfWidth = BEAM_WIDTH * 0.5f;

        float startLeftX = fromX + px * halfWidth;
        float startLeftY = fromY + py * halfWidth;
        float startRightX = fromX - px * halfWidth;
        float startRightY = fromY - py * halfWidth;
        float endLeftX = toX + px * halfWidth;
        float endLeftY = toY + py * halfWidth;
        float endRightX = toX - px * halfWidth;
        float endRightY = toY - py * halfWidth;

        float texWidth = beamSprite.getTextureWidth();
        float texHeight = beamSprite.getTextureHeight();
        float startU = (elapsed * BEAM_SCROLL_SPEED) % texWidth;
        float repeat = Math.max(0.75f, Math.min(3f, beamDistance / 900f));
        float endU = startU + texWidth * repeat;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        beamSprite.bindTexture();
        float red = BEAM_COLOR.getRed() / 255f;
        float green = BEAM_COLOR.getGreen() / 255f;
        float blue = BEAM_COLOR.getBlue() / 255f;
        GL11.glColor4f(red, green, blue, alphaMult * BEAM_ALPHA);

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glTexCoord2f(startU, 0f);
        GL11.glVertex2f(startLeftX, startLeftY);
        GL11.glTexCoord2f(startU, texHeight);
        GL11.glVertex2f(startRightX, startRightY);
        GL11.glTexCoord2f(endU, 0f);
        GL11.glVertex2f(endLeftX, endLeftY);
        GL11.glTexCoord2f(endU, texHeight);
        GL11.glVertex2f(endRightX, endRightY);
        GL11.glEnd();

        GL11.glPopMatrix();
    }
}
