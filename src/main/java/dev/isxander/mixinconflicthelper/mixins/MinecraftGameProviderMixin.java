package dev.isxander.mixinconflicthelper.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.mixinconflicthelper.MixinConflictHelper;
import net.fabricmc.loader.impl.FormattedException;
import net.fabricmc.loader.impl.game.minecraft.MinecraftGameProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Mixin(value = MinecraftGameProvider.class, remap = false, priority = 1)
public class MinecraftGameProviderMixin {
    @ModifyExpressionValue(method = "launch", at = @At(value = "NEW", target = "net/fabricmc/loader/impl/FormattedException", ordinal = 1))
    private FormattedException onCrash(FormattedException exception) throws IOException {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        MixinConflictHelper.processStacktrace(pw.toString());

        pw.close();
        sw.close();

        return exception;
    }
}
