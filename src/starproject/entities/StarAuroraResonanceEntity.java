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

    @Override
    public void init(SectorEntityToken entity, Object pluginParams) {
        super.init(entity, pluginParams);
        
        // 1. Load the texture (Use a vanilla aurora texture or your own)
        auroraTexture = Global.getSettings().getSprite("terrain", "aurora");
        
        // 2. Initialize the renderer, passing this class as the data provider
        auroraRenderer = new AuroraRenderer(this);
    }

    @Override
    public void advance(float amount) {
        // 3. Advance the animation phase angle
        if (auroraRenderer != null) {
            auroraRenderer.advance(amount);
        }
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
        return entity.getRadius() + 10f; 
    }

    @Override
    public float getAuroraOuterRadius() { 
        // How far the aurora extends
        return entity.getRadius() + 150f; 
    }

    @Override
    public Vector2f getAuroraCenterLoc() { 
        // Anchors the aurora to your entity
        return entity.getLocation(); 
    }

    @Override
    public Color getAuroraColorForAngle(float angle) { 
        // You can return different colors based on the angle for rainbow effects!
        return new Color(100, 255, 150, 180); 
    }

    @Override
    public float getAuroraAlphaMultForAngle(float angle) { 
        return 1f; 
    }

    @Override
    public float getAuroraShortenMult(float angle) { 
        // Controls the intensity of the "pulsing" sine wave effect
        return 0.8f; 
    }

    @Override
    public float getAuroraInnerOffsetMult(float angle) { 
        return 1f; 
    }

    @Override
    public float getAuroraThicknessMult(float angle) { 
        return 2f; 
    }

    @Override
    public float getAuroraThicknessFlat(float angle) { 
        return 0f; 
    }

    @Override
    public float getAuroraTexPerSegmentMult() { 
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