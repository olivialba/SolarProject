package starproject.entities;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.util.Misc;

public class ResonanceBeamEntityPlugin extends BaseCustomEntityPlugin {

    private float elapsed;

    @Override
    public void init(SectorEntityToken entity, Object pluginParams) {
        super.init(entity, pluginParams);
        ((CustomCampaignEntityAPI) entity).setRadius(1f);
    }

    @Override
    public void advance(float amount) {
        elapsed += amount;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null && fleet.getContainingLocation() == entity.getContainingLocation()) {
            entity.setLocation(fleet.getLocation().x, fleet.getLocation().y);
        }
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

        return Misc.getDistance(entity.getLocation(), star.getLocation()) + 1000f;
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

        Vector2f from = entity.getLocation();
        Vector2f to = star.getLocation();

        float viewportAlpha = viewport.getAlphaMult();
        float sensorAlpha = entity.getSensorFaderBrightness() * entity.getSensorContactFaderBrightness();
        float pulse = 0.85f + 0.15f * (float) Math.sin(elapsed * 10f);
        float alphaMult = viewportAlpha * sensorAlpha * pulse;
        if (alphaMult <= 0f) {
            return;
        }

        float distance = Misc.getDistance(from, to);
        if (distance <= 1f) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        renderBeamPass(from, to, 26f, 14f, new Color(80, 210, 255), alphaMult * 0.22f);
        renderBeamPass(from, to, 14f, 7f, new Color(150, 235, 255), alphaMult * 0.35f);
        renderBeamPass(from, to, 6f, 3f, new Color(245, 250, 255), alphaMult * 0.55f);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void renderBeamPass(Vector2f from, Vector2f to, float startHalfWidth, float endHalfWidth,
                                Color color, float alphaMult) {
        Vector2f direction = Vector2f.sub(to, from, null);
        float length = direction.length();
        if (length <= 1f) {
            return;
        }

        direction.scale(1f / length);
        Vector2f perpendicular = new Vector2f(-direction.y, direction.x);

        Vector2f startLeft = new Vector2f(from.x + perpendicular.x * startHalfWidth, from.y + perpendicular.y * startHalfWidth);
        Vector2f startRight = new Vector2f(from.x - perpendicular.x * startHalfWidth, from.y - perpendicular.y * startHalfWidth);
        Vector2f endLeft = new Vector2f(to.x + perpendicular.x * endHalfWidth, to.y + perpendicular.y * endHalfWidth);
        Vector2f endRight = new Vector2f(to.x - perpendicular.x * endHalfWidth, to.y - perpendicular.y * endHalfWidth);

        int alpha = Math.min(255, Math.max(0, Math.round(255f * alphaMult)));
        Color passColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        Misc.setColor(passColor);

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glVertex2f(startLeft.x, startLeft.y);
        GL11.glVertex2f(startRight.x, startRight.y);
        GL11.glVertex2f(endLeft.x, endLeft.y);
        GL11.glVertex2f(endRight.x, endRight.y);
        GL11.glEnd();
    }
}
