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

    private static final float BASE_WIDTH = 90f;
    private static final float BEAM_ALPHA = 0.9f;

    private static final float BEAM_PULSE_SPEED = 6f;
    private static final float BEAM_SCROLL_SPEED = 0.12f;

    // Warm, stellar resonance colors
    private static final Color FRINGE_COLOR = new Color(255, 120, 40);
    private static final Color CORE_COLOR = new Color(255, 240, 200);

    private SpriteAPI fringeSprite;
    private SpriteAPI coreSprite;
    private float elapsed;

    @Override
    public void init(SectorEntityToken entity, Object pluginParams) {
        super.init(entity, pluginParams);
        ((CustomCampaignEntityAPI) entity).setRadius(1f);

        fringeSprite = Global.getSettings().getSprite("graphics/fx/beamfringe.png");
        coreSprite = Global.getSettings().getSprite("graphics/fx/beamcore.png");
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
        if (layer != CampaignEngineLayers.STATIONS) return;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null || fleet.getStarSystem() == null) return;

        PlanetAPI star = fleet.getStarSystem().getStar();
        if (star == null || star.getSpec().isBlackHole()) return;

        float viewportAlpha = viewport.getAlphaMult();
        float sensorAlpha = entity.getSensorFaderBrightness() * entity.getSensorContactFaderBrightness();
        
        // A gentle, slow pulse for the overall brightness to make it feel "alive"
        float pulse = 0.85f + 0.15f * (float) Math.sin(elapsed * 4f);
        float baseAlpha = viewportAlpha * sensorAlpha * pulse * BEAM_ALPHA;

        if (baseAlpha <= 0f) return;

        float fromX = entity.getLocation().x;
        float fromY = entity.getLocation().y;
        float toX = fleet.getLocation().x;
        float toY = fleet.getLocation().y;

        float distance = Misc.getDistance(fromX, fromY, toX, toY);
        if (distance <= 1f) return;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        // Additive blending: makes overlapping colors brighter, perfect for energy
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); 

        // 1. Outer Fringe (Wide, transparent, pulsing slightly in width, scrolling slowly backwards)
        float outerWidth = BASE_WIDTH + 15f * (float)Math.sin(elapsed * 3f);
        renderBeamLayer(fromX, fromY, toX, toY, distance, outerWidth, FRINGE_COLOR, baseAlpha * 0.35f, fringeSprite, -0.05f);

        // 2. Inner Fringe (Medium width, more opaque, scrolling forwards)
        float innerWidth = BASE_WIDTH * 0.6f;
        renderBeamLayer(fromX, fromY, toX, toY, distance, innerWidth, FRINGE_COLOR, baseAlpha * 0.65f, fringeSprite, 0.15f);

        // 3. Hot Core (Narrow, very bright, scrolling fast)
        float coreWidth = BASE_WIDTH * 0.27f;
        renderBeamLayer(fromX, fromY, toX, toY, distance, coreWidth, CORE_COLOR, baseAlpha, coreSprite, 0.35f);

        GL11.glPopMatrix();
    }

    private void renderBeamLayer(float fromX, float fromY,
                                float toX, float toY,
                                float distance,
                                float width,
                                Color color,
                                float alpha,
                                SpriteAPI sprite,
                                float scrollSpeed) {

        float dirX = toX - fromX;
        float dirY = toY - fromY;

        float invDistance = 1f / distance;

        // Unit direction vector
        float nx = dirX * invDistance;
        float ny = dirY * invDistance;

        // Perpendicular vector
        float px = -ny;
        float py = nx;

        float halfWidth = width * 0.5f;

        //-------------------------------------------------
        // How long the cone should be
        //-------------------------------------------------

        float taperLength = Math.min(90f, distance * 0.25f);

        //-------------------------------------------------
        // Rectangle ends here
        //-------------------------------------------------

        float taperStartX = toX - nx * taperLength;
        float taperStartY = toY - ny * taperLength;

        //-------------------------------------------------
        // Rectangle vertices
        //-------------------------------------------------

        float startLeftX = fromX + px * halfWidth;
        float startLeftY = fromY + py * halfWidth;

        float startRightX = fromX - px * halfWidth;
        float startRightY = fromY - py * halfWidth;

        float taperLeftX = taperStartX + px * halfWidth;
        float taperLeftY = taperStartY + py * halfWidth;

        float taperRightX = taperStartX - px * halfWidth;
        float taperRightY = taperStartY - py * halfWidth;

        //-------------------------------------------------
        // Texture coordinates
        //-------------------------------------------------

        float startU = elapsed * scrollSpeed;

        float repeat = Math.max(1f, distance / 400f);

        float taperFraction = (distance - taperLength) / distance;

        float taperU = startU + repeat * taperFraction;
        float endU = startU + repeat;

        sprite.bindTexture();

        GL11.glColor4f(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                alpha);

        //-------------------------------------------------
        // MAIN RECTANGLE
        //-------------------------------------------------

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

        GL11.glTexCoord2f(startU, 0f);
        GL11.glVertex2f(startLeftX, startLeftY);

        GL11.glTexCoord2f(startU, 1f);
        GL11.glVertex2f(startRightX, startRightY);

        GL11.glTexCoord2f(taperU, 0f);
        GL11.glVertex2f(taperLeftX, taperLeftY);

        GL11.glTexCoord2f(taperU, 1f);
        GL11.glVertex2f(taperRightX, taperRightY);

        GL11.glEnd();

        //-------------------------------------------------
        // CONE
        //-------------------------------------------------

        GL11.glBegin(GL11.GL_TRIANGLES);

        GL11.glTexCoord2f(taperU, 0f);
        GL11.glVertex2f(taperLeftX, taperLeftY);

        GL11.glTexCoord2f(taperU, 1f);
        GL11.glVertex2f(taperRightX, taperRightY);

        // Sample the center of the texture at the tip
        GL11.glTexCoord2f(endU, 0.5f);
        GL11.glVertex2f(toX, toY);

        GL11.glEnd();
    }
}
