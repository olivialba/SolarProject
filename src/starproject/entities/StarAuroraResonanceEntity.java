package starproject.entities;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AuroraRenderer;
import com.fs.starfarer.api.impl.campaign.terrain.AuroraRenderer.AuroraRendererDelegate;
import com.fs.starfarer.api.impl.campaign.terrain.RangeBlockerUtil;

public class StarAuroraResonanceEntity extends BaseCustomEntityPlugin implements AuroraRendererDelegate {

    private AuroraRenderer auroraRenderer;
    private SpriteAPI auroraTexture;

    private float elapsed;
    private float growthDuration = 10.0f;
    private Color starColor = new Color(247, 255, 229, 180);

    @Override
    public void init(SectorEntityToken entity, Object pluginParams) {
        super.init(entity, pluginParams);

        if (pluginParams instanceof Color) {
            starColor = (Color) pluginParams;
        }

        // 1. Load the texture (Use a vanilla aurora texture or your own)
        auroraTexture = Global.getSettings().getSprite("terrain", "aurora");
        auroraRenderer = new AuroraRenderer(this);
    }

    @Override
    public void advance(float amount) {
        // 3. Advance the animation phase angle
        if (auroraRenderer != null) {
            auroraRenderer.advance(amount);
        }
        elapsed += amount;
    }

    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        // 4. Draw the aurora on the appropriate layer (TERRAIN layers usually work best for auroras)
        if (layer == CampaignEngineLayers.TERRAIN_7) { 
            auroraRenderer.render(viewport.getAlphaMult());
        }
    }
    
    // -----------------------------------------------------------
    // AuroraRendererDelegate Methods: Tweak these to change the look
    // -----------------------------------------------------------

    @Override
    public float getAuroraInnerRadius() { 
        // Starts just outside the entity
        return entity.getRadius() + 30f; 
    }

    @Override
    public float getAuroraOuterRadius() { 
        // How far the aurora extends
        return entity.getRadius() + 800f;
    }

    @Override
    public Vector2f getAuroraCenterLoc() { 
        // Anchors the aurora to your entity
        return entity.getLocation(); 
    }

    @Override
    public Color getAuroraColorForAngle(float angle) { 
        // You can return different colors based on the angle for rainbow effects!
        return starColor;
    }

    @Override
    public float getAuroraAlphaMultForAngle(float angle) { 
        float progress = Math.min(elapsed / growthDuration, 1.0f);
        // Smoothstep
        float smooth = progress * progress * (3f - 2f * progress);
        return smooth * 0.9f;
    }

    @Override
    public float getAuroraShortenMult(float angle) { 
        float progress = Math.min(elapsed / growthDuration, 1.0f);
        float smooth = progress * progress * (3f - 2f * progress);
        
        // START small: low shortenMult = compressed radial length
        // The sine wave amplitude is reduced, so the aurora stays close to inner radius
        // END at 1.0: full extension to outer radius
        return 0.15f + smooth * 0.85f; // 0.15 → 1.0
    }

    @Override
    public float getAuroraInnerOffsetMult(float angle) { 
        // Returns the offset multiplier for the inner radius at the specified angle.
        return 1f; 
    }

    @Override
    public float getAuroraThicknessMult(float angle) { 
        float progress = Math.min(elapsed / growthDuration, 1.0f);
        float smooth = progress * progress * (3f - 2f * progress);
        
        // Start THIN (0.2), grow to NORMAL (1.0)
        // The aurora will appear tight around the star at first, then fill out
        return 0.2f + smooth * 0.8f; // 0.2 → 1.0
    }

    @Override
    public float getAuroraThicknessFlat(float angle) { 
        float progress = Math.min(elapsed / growthDuration, 1.0f);
        float smooth = progress * progress * (3f - 2f * progress);
        // Start with minimal flat thickness, build up
        return 10f + smooth * 15f; // 10 → 25
    }

    @Override
    public float getAuroraTexPerSegmentMult() { 
        // Returns the multiplier controlling the frequency of the texture tiling per segment.
        return 1f; 
    }

    @Override
    public float getAuroraBandWidthInTexture() { 
        // Usually matches the height of your texture file in pixels
        return 256f; 
    }

    @Override
    public SpriteAPI getAuroraTexture() { 
        return auroraTexture; 
    }

    @Override
    public RangeBlockerUtil getAuroraBlocker() { 
        // Used by vanilla to make planets block the sun's corona. Null is fine here.
        return null; 
    }
}