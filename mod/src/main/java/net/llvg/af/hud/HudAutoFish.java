package net.llvg.af.hud;

import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.Text;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import net.llvg.af.AutoFish;
import org.jetbrains.annotations.Nullable;

import static net.llvg.af.utils.Utility.*;
import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings ({ "FieldMayBeFinal", "FieldCanBeLocal" })
public final class HudAutoFish
  extends BasicHud
{
    @Exclude
    public static final HudAutoFish instance = new HudAutoFish();
    
    private HudAutoFish() {
        super(false);
    }
    
    @Checkbox (name = "Show When Enabled")
    private boolean showWhenEnabled = false;
    
    @Text (name = "Text When Enabled")
    private String textWhenEnabled = "";
    
    @Checkbox (name = "Show While Disabled")
    private boolean showWhenDisabled = false;
    
    @Text (name = "Text When Disabled")
    private String textWhenDisabled = "";
    
    @Checkbox (
      name = "Draw Shadow",
      size = 2
    )
    private boolean drawShadow = true;
    
    @Exclude
    @Nullable
    private String _text = null;
    
    private void lookupText(boolean example) {
        if (example) {
            _text = "Auto Fish Text";
        } else {
            if (AutoFish.isActive()) {
                _text = showWhenEnabled ? textWhenEnabled : null;
            } else {
                _text = showWhenDisabled ? textWhenDisabled : null;
            }
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
        glPushMatrix();
        glScalef(scale, scale, 1);
        mc().fontRendererObj.drawString(
          text,
          x / scale,
          y / scale,
          -1,
          drawShadow
        );
        glPopMatrix();
    }
    
    @Override
    protected float getWidth(
      float scale,
      boolean example
    ) {
        String text;
        return (text = _text) == null ? 0 : mc().fontRendererObj.getStringWidth(text);
    }
    
    @Override
    protected float getHeight(
      float scale,
      boolean example
    ) {
        return mc().fontRendererObj.FONT_HEIGHT * scale;
    }
}
