package io.github.darkkronicle.advancedchathud.mixin;

import io.github.darkkronicle.advancedchathud.gui.WindowManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChatHud.class, priority = 1050)
@Environment(EnvType.CLIENT)
public class MixinChatHud {

    @Inject(at = @At("HEAD"), method = "getText", cancellable = true)
    private void getTextAt(
        double X,
        double Y,
        CallbackInfoReturnable<Style> ci
    ) {
        ci.setReturnValue(WindowManager.getInstance().getText(X, Y));
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void render(MatrixStack stack, int delta, CallbackInfo ci) {
        ci.cancel();
    }
}
