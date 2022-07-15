package dev.isxander.mixinconflicthelper;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.FabricUtil;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class MixinConflictHelper implements PreLaunchEntrypoint {
    public static final Logger LOGGER = LoggerFactory.getLogger("Mixin Conflict Helper");

    private static final Pattern MERGED_BY_REGEX = Pattern.compile("merged by ((?:[a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*)");

    @Override
    public void onPreLaunch() {
        Mixins.registerErrorHandlerClass("dev.isxander.mixinconflicthelper.ConflictErrorHandler");
    }

    public static Optional<ModContainer> getModForMixinConfig(IMixinConfig mixinConfig) {
        return FabricLoader.getInstance().getModContainer(FabricUtil.getModId(mixinConfig));
    }

    public static Optional<ModContainer> walkExceptionCauseForMerger(Throwable th) {
        try {
            Throwable throwable = th;
            while (throwable != null) {
                if (throwable instanceof InvalidInjectionException injectionException) {
                    if (injectionException.getContext() instanceof InjectionInfo injectionInfo) {
                        var classInfo = ClassInfo.fromCache(injectionInfo.getMixin().getTargetClassRef());
                        var mixinsField = ClassInfo.class.getDeclaredField("mixins");
                        mixinsField.setAccessible(true);
                        Set<IMixinInfo> mixins = (Set<IMixinInfo>) mixinsField.get(classInfo);

                        var matcher = MERGED_BY_REGEX.matcher(injectionException.getMessage());
                        var found = matcher.find();
                        if (found) {
                            var mergerMixinClass = matcher.group(1);

                            for (var mixin : mixins) {
                                if (mixin.getClassName().equals(mergerMixinClass)) {
                                    return getModForMixinConfig(mixin.getConfig());
                                }
                            }
                        }
                    }
                    break;
                }

                throwable = throwable.getCause();
            }
        } catch (NoSuchFieldException | NullPointerException | SecurityException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}
