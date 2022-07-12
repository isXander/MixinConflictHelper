package dev.isxander.mixinconflicthelper;

import dev.isxander.mixinconflicthelper.exception.MixinConflictException;
import dev.isxander.mixinconflicthelper.gui.SwingPopups;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;

public class ConflictErrorHandler implements IMixinErrorHandler {
    @Override
    public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin, ErrorAction action) {
        return action;
    }

    @Override
    public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin, ErrorAction action) {
        var mod1 = MixinConflictHelper.getModForMixinConfig(mixin.getConfig());
        var mod2 = MixinConflictHelper.walkExceptionCauseForMerger(th);

        if (mod1.isPresent() && mod2.isPresent()) {
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                try {
                    SwingPopups.showConflict(mod1.get(), mod2.get(), th);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else
                throw new MixinConflictException(String.format("%s tried to inject into code already modified by %s", mod1.get().getMetadata().getName(), mod2.get().getMetadata().getName()), th);
        }

        return action;
    }
}
