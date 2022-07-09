package dev.isxander.mixinconflicthelper;

import dev.isxander.mixinconflicthelper.gui.SwingPopups;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

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
            SwingPopups.setupAwt(() -> SwingPopups.conflict(mod1.get(), mod2.get(), th));
        }

        return action;
    }
}
