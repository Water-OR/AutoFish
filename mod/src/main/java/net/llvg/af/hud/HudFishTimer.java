package net.llvg.af.hud;

import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import net.llvg.af.AutoFish;
import net.llvg.af.utils.AutoClosableNE;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.item.EntityArmorStand;
import org.jetbrains.annotations.Nullable;

import static net.llvg.af.utils.RenderUtility.*;
import static net.llvg.af.utils.Utility.*;
import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings ({ "FieldMayBeFinal", "FieldCanBeLocal" })
public final class HudFishTimer
  extends BasicHud
{
    @Exclude
    public static final HudFishTimer DEFAULT = new HudFishTimer();
    
    private HudFishTimer() { }
    
    @Checkbox (
      name = "Draw Shadow",
      size = 2
    )
    private boolean drawShadow = true;
    
    @Number (
      name = "Box Width",
      size = 2,
      min = 0,
      max = Float.MAX_VALUE
    )
    private float boxWidth = 20f;
    
    @Exclude
    @Nullable
    private transient String _text = null;
    
    private void lookupText(boolean example) {
        if (example) {
            _text = "§e§l0.1";
        } else {
            EntityArmorStand timer;
            _text = (timer = AutoFish.getTimer()) == null ? null : timer.getCustomNameTag();
        }
    }
    
    @Override
    public void drawAll(
      UMatrixStack matrices,
      boolean example
    ) {
        lookupText(example);
        if (_text != null) super.drawAll(matrices, example);
    }
    
    @Override
    protected void draw(
      UMatrixStack matrices,
      float x,
      float y,
      float scale,
      boolean example
    ) {
        String text;
        if ((text = _text) == null) return;
        try (AutoClosableNE ignored = glWrapBlock()) {
            glScalef(scale, scale, 1);
            FontRenderer fr = mc().fontRendererObj;
            fr.drawString(text, x / scale + (boxWidth - fr.getStringWidth(text)) / 2, y / scale, -1, drawShadow);
        }
    }
    
    @Override
    protected float getWidth(
      float scale,
      boolean example
    ) {
        return boxWidth * scale;
    }
    
    @Override
    protected float getHeight(
      float scale,
      boolean example
    ) {
        return mc().fontRendererObj.FONT_HEIGHT * scale;
    }
}
