package net.llvg.af;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Info;
import cc.polyfrost.oneconfig.config.annotations.KeyBind;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import net.llvg.af.hud.HudAutoFish;
import net.llvg.af.hud.HudFishTimer;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings ({ "FieldMayBeFinal", "FieldCanBeLocal" })
final class AutoFishConfiguration
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
        
        StackTraceElement trace = new Throwable().getStackTrace()[1];
        if (
          !AutoFishConfiguration.class.getName().equals(trace.getClassName()) ||
          !"<clinit>".equals(trace.getMethodName())
        ) throw new UnsupportedOperationException();
        
        initialize();
        
        registerKeyBind(toggleKey, AutoFish::toggle);
        registerKeyBind(rotateTestKey, AutoFishConfiguration::rotate);
    }
    
    @Override
    public void load() {
        super.load();
    }
    
    @Override
    public void save() {
        super.save();
        AutoFish.logger.info("Saving configs");
        if (!enabled) AutoFish.toggle();
    }
    
    static void init() { /* For <clinit> invoke */ }
    
    @SuppressWarnings ("BooleanMethodIsAlwaysInverted")
    public static boolean isEnabled() {
        return instance.enabled;
    }
    
    @KeyBind (
      name = "Toggle Key",
      size = 2
    )
    private OneKeyBind toggleKey = new OneKeyBind();
    
    @Exclude
    private static final String SUBCATEGORY_ANTI_AFK = "Anti Afk";
    
    @Checkbox (
      name = "Anti Afk",
      size = 2,
      subcategory = SUBCATEGORY_ANTI_AFK
    )
    private boolean antiAfk = false;
    
    public static boolean isAntiAfk() {
        return instance.antiAfk;
    }
    
    @Number (
      name = "Anti Afk Rotation Yaw",
      min = -180,
      max = 180,
      subcategory = SUBCATEGORY_ANTI_AFK
    )
    private float antiAfkRotationYaw = .5f;
    
    public static float getAntiAfkRotationYaw() {
        return instance.antiAfkRotationYaw;
    }
    
    @Number (
      name = "Anti Afk Rotation Pitch",
      min = -90,
      max = 90,
      subcategory = SUBCATEGORY_ANTI_AFK
    )
    private float antiAfkRotationPitch = .5f;
    
    public static float getAntiAfkRotationPitch() {
        return instance.antiAfkRotationPitch;
    }
    
    @Number (
      name = "Anti Afk Rotation time (ms)",
      min = 4,
      max = 5000,
      subcategory = SUBCATEGORY_ANTI_AFK
    )
    private int antiAfkRotationTime = 50;
    
    public static int getAntiAfkRotationTime() {
        return instance.antiAfkRotationTime;
    }
    
    @Checkbox (
      name = "Reset Facing When Not Fishing",
      subcategory = SUBCATEGORY_ANTI_AFK
    )
    private boolean resetFacingWhenNotFishing = false;
    
    public static boolean isResetFacingWhenNotFishing() {
        return instance.resetFacingWhenNotFishing;
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
      name = "Use Player Swim Sound Check",
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
      name = "Use Note Sound Check (Require SkyBlock 'Fishing Sound' Setting Enabled)",
      size = 2,
      subcategory = SUBCATEGORY_CATCH
    )
    private boolean useNoteSoundCheck = false;
    
    public static boolean isUseNoteSoundCheck() {
        return instance.useNoteSoundCheck;
    }
    
    @Checkbox (
      name = "Use Fish Timer Check (Require SkyBlock 'Fishing Timer' Setting Enabled)",
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
    private int autoThrowDelay = 100;
    
    public static int getAutoThrowDelay() {
        return instance.autoThrowDelay;
    }
    
    @Number (
      name = "Auto Throw First Delay (ms)",
      min = 0,
      max = Integer.MAX_VALUE,
      subcategory = SUBCATEGORY_THROW
    )
    private int autoThrowFirstDelay = 500;
    
    public static int getAutoThrowFirstDelay() {
        return instance.autoThrowFirstDelay;
    }
    
    @Checkbox (
      name = "Do Not Wait Hook Dead",
      subcategory = SUBCATEGORY_THROW
    )
    private boolean doNotWaitHookDead = false;
    
    public static boolean isDoNotWaitHookDead() {
        return instance.doNotWaitHookDead;
    }
    
    @Exclude
    private static final String SUBCATEGORY_CH_LAVA_ESP = "Crystal Hollows Lava ESP";
    
    @Checkbox (
      name = "Scan CH Lava",
      subcategory = SUBCATEGORY_CH_LAVA_ESP
    )
    private boolean scanCHLava = false;
    
    public static boolean isScanCHLava() {
        return instance.scanCHLava;
    }
    
    @Button (
      name = "Submit CH Lava Scan",
      text = "Submit",
      subcategory = SUBCATEGORY_CH_LAVA_ESP
    )
    @Exclude
    @SuppressWarnings ("unused")
    private transient Runnable submitCHLavaScan = () -> {
        if (isEnabled() && isScanCHLava() && AutoFish.isInCH()) AutoFish.getLoadedChunks().forEach(AutoFishCHLavaESP::submitChunkScan);
    };
    
    @Checkbox (
      name = "Render Outline",
      subcategory = SUBCATEGORY_CH_LAVA_ESP
    )
    private boolean renderOutline = false;
    
    public static boolean isRenderOutline() {
        return instance.renderOutline;
    }
    
    @Color (
      name = "Outline Color",
      subcategory = SUBCATEGORY_CH_LAVA_ESP
    )
    @NotNull
    private OneColor outlineColor = new OneColor(0xFFFFC800);
    
    @NotNull
    public static OneColor getOutlineColor() {
        return instance.outlineColor;
    }
    
    @Number (
      name = "Outline Width",
      min = 0,
      max = Float.MAX_VALUE,
      subcategory = SUBCATEGORY_CH_LAVA_ESP
    )
    private float outlineWidth = 2f;
    
    public static float getOutlineWidth() {
        return instance.outlineWidth;
    }
    
    @Checkbox (
      name = "Disable Outline Connection",
      subcategory = SUBCATEGORY_CH_LAVA_ESP
    )
    private boolean disableOutlineConnection = false;
    
    public static boolean isDisableOutlineConnection() {
        return instance.disableOutlineConnection;
    }
    
    @Checkbox (
      name = "Render Face",
      subcategory = SUBCATEGORY_CH_LAVA_ESP
    )
    private boolean renderFace = false;
    
    public static boolean isRenderFace() {
        return instance.renderFace;
    }
    
    @Color (
      name = "Face Color",
      subcategory = SUBCATEGORY_CH_LAVA_ESP
    )
    private OneColor faceColor = new OneColor(0x3F00FFFF);
    
    public static OneColor getFaceColor() {
        return instance.faceColor;
    }
    
    @Exclude
    private static final String CATEGORY_HUD = "HUD";
    
    @HUD (
      name = "Auto Fish",
      category = CATEGORY_HUD
    )
    @SuppressWarnings ("unused")
    private HudAutoFish hudAutoFish = HudAutoFish.DEFAULT;
    
    @HUD (
      name = "Fish Timer",
      category = CATEGORY_HUD
    )
    @SuppressWarnings ("unused")
    private HudFishTimer hudFishTimer = HudFishTimer.DEFAULT;
    
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
        AutoFishAntiAfk.trigger();
    }
    
    @KeyBind (
      name = "Rotate test key",
      category = CATEGORY_DEBUG
    )
    private OneKeyBind rotateTestKey = new OneKeyBind();
}
