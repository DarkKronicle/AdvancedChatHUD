package io.github.darkkronicle.advancedchathud.mixin;

import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import io.github.darkkronicle.advancedchathud.ResolutionEventHandler;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "onResolutionChanged", at = @At("HEAD"))
    private void onResChange(CallbackInfo ci) {
        for (ResolutionEventHandler handler : ResolutionEventHandler.ON_RESOLUTION_CHANGE) {
            handler.onResolutionChange();
        }
    }

}
