package net.llvg.af;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Info;
import cc.polyfrost.oneconfig.config.annotations.KeyBind;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import net.llvg.af.hud.HudAutoFish;
import net.llvg.af.hud.HudFishTimer;
import net.minecraft.client.entity.EntityPlayerSP;

import static net.llvg.af.utils.Utility.*;

@SuppressWarnings ({ "FieldMayBeFinal", "FieldCanBeLocal" })
public final class AutoFishConfiguration
  extends Config
{
    @Exclude
    private static final AutoFishConfiguration instance = new AutoFishConfiguration();
    
    private AutoFishConfiguration() {
        super(
          new Mod("Auto Fish", ModType.THIRD_PARTY),
          "config.wor.auto-fish.json",
          false,
          true
        );
        
        initialize();
        
        registerKeyBind(toggleKey, AutoFish::toggle);
        registerKeyBind(rotateTestKey, AutoFishConfiguration::rotate);
    }
    
    @Override
    public void save() {
        super.save();
        if (!enabled) AutoFish.onConfigurationDisable();
    }
    
    static void init() { }
    
    @KeyBind (
      name = "Toggle Key",
      size = 2
    )
    private OneKeyBind toggleKey = new OneKeyBind();
    
    @Checkbox (
      name = "Anti Afk (WIP)",
      size = 2
    )
    private boolean antiAfk = false;
    
    public static boolean isAntiAfk() {
        return instance.antiAfk;
    }
    
    @Number (
      name = "Anti Afk Rotation Yaw",
      min = -180,
      max = 180
    )
    private float antiAfkRotationYaw = .2f;
    
    public static float getAntiAfkRotationYaw() {
        return instance.antiAfkRotationYaw;
    }
    
    @Number (
      name = "Anti Afk Rotation Pitch",
      min = -90,
      max = 90
    )
    private float antiAfkRotationPitch = .1f;
    
    public static float getAntiAfkRotationPitch() {
        return instance.antiAfkRotationPitch;
    }
    
    @Number (
      name = "Anti Afk Rotation time (ms)",
      min = 4,
      max = 5000
    )
    private int antiAfkRotationTime = 200;
    
    public static int getAntiAfkRotationTime() {
        return instance.antiAfkRotationTime;
    }
    
    @Exclude
    private static final String SUBCATEGORY_CATCH = "Catch";
    
    @Checkbox (
      name = "Auto Catch",
      subcategory = SUBCATEGORY_CATCH
    )
    private boolean autoCatch = false;
    
    public static boolean isAutoCatch() {
        return instance.autoCatch;
    }
    
    @Number (
      name = "Auto Catch Delay (ms)",
      min = 0,
      max = Integer.MAX_VALUE,
      subcategory = SUBCATEGORY_CATCH
    )
    private int autoCatchDelay = 0;
    
    public static int getAutoCatchDelay() {
        return instance.autoCatchDelay;
    }
    
    
    @Checkbox (
      name = "Use player swim sound check",
      size = 2,
      subcategory = SUBCATEGORY_CATCH
    )
    private boolean useSwimSoundCheck = false;
    
    public static boolean isUseSwimSoundCheck() {
        return instance.useSwimSoundCheck;
    }
    
    @Slider (
      name = "Expand Swim Sound Check Range",
      min = 0,
      max = 1,
      subcategory = SUBCATEGORY_CATCH
    )
    private float expandSwimSoundCheckRange = 0.01f;
    
    public static float getExpandSwimSoundCheckRange() {
        return instance.expandSwimSoundCheckRange;
    }
    
    @Checkbox (
      name = "Use note sound check (Require skyblock 'Fishing Sound' setting enabled)",
      size = 2,
      subcategory = SUBCATEGORY_CATCH
    )
    private boolean useNoteSoundCheck = false;
    
    public static boolean isUseNoteSoundCheck() {
        return instance.useNoteSoundCheck;
    }
    
    @Checkbox (
      name = "Use fish timer check (Require skyblock 'Fishing Timer' setting enabled)",
      size = 2,
      subcategory = SUBCATEGORY_CATCH
    )
    private boolean useFishTimerCheck = false;
    
    public static boolean isUseFishTimerCheck() {
        return instance.useFishTimerCheck;
    }
    
    @Exclude
    static final String SUBCATEGORY_THROW = "Throw";
    
    @Checkbox (
      name = "Auto Throw",
      subcategory = SUBCATEGORY_THROW
    )
    private boolean autoThrow = false;
    
    public static boolean isAutoThrow() {
        return instance.autoThrow;
    }
    
    @Number (
      name = "Auto Throw Delay (ms)",
      min = 0,
      max = Integer.MAX_VALUE,
      subcategory = SUBCATEGORY_THROW
    )
    private int autoThrowDelay = 0;
    
    public static int getAutoThrowDelay() {
        return instance.autoThrowDelay;
    }
    
    @Exclude
    private static final String CATEGORY_HUD = "HUD";
    
    @HUD (
      name = "Auto Fish",
      category = CATEGORY_HUD
    )
    @SuppressWarnings ("unused")
    private HudAutoFish hudAutoFish = HudAutoFish.instance;
    
    @HUD (
      name = "Fish Timer",
      category = CATEGORY_HUD
    )
    @SuppressWarnings ("unused")
    private HudFishTimer hudFishTimer = HudFishTimer.instance;
    
    @Exclude
    private static final String CATEGORY_DEBUG = "Debug";
    
    @Info (
      text = "These features are only for debug, please don't change unless you know what you are doing!",
      type = InfoType.WARNING,
      size = 2,
      category = CATEGORY_DEBUG
    )
    @SuppressWarnings ("unused")
    private Void info1 = null;
    
    @Checkbox (
      name = "Verbose",
      size = 2,
      category = CATEGORY_DEBUG
    )
    private boolean verbose = false;
    
    public static boolean isVerbose() {
        return instance.verbose;
    }
    
    @Button (
      name = "Rotate test",
      text = "Click",
      category = CATEGORY_DEBUG
    )
    @SuppressWarnings ("unused")
    private static void rotate() {
        EntityPlayerSP player;
        if ((player = mc().thePlayer) != null) AutoFishAntiAfk.trigger(player);
    }
    
    @KeyBind (
      name = "Rotate test key",
      category = CATEGORY_DEBUG
    )
    private OneKeyBind rotateTestKey = new OneKeyBind();
}
