package dev.isxander.mixinconflicthelper;

import net.fabricmc.loader.api.ModContainer;
import org.spongepowered.asm.mixin.FabricUtil;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.reflect.InvocationTargetException;

public class ConflictErrorHandler implements IMixinErrorHandler {
    @Override
    public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin, ErrorAction action) {
        return action;
    }

    @Override
    public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin, ErrorAction action) {
        var mod = MixinConflictHelper.getModForMixinConfig(mixin.getConfig());
        System.out.println(mod.getMetadata().getId() + " caused the conflict");
        System.out.println("issues page here: " + mod.getMetadata().getContact().get("issues").orElse("no issues repo"));

        ModContainer mergerMod;
        try {
            mergerMod = MixinConflictHelper.walkExceptionCauseForMerger(th).orElseThrow();
        } catch (InvocationTargetException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        System.out.println(mergerMod.getMetadata().getId() + " caused the conflict");
        System.out.println("issues page here: " + mergerMod.getMetadata().getContact().get("issues").orElse("no issues repo"));

        return action;
    }
}
