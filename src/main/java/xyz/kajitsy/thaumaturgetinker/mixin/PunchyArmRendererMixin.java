package xyz.kajitsy.thaumaturgetinker.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.mojang.blaze3d.vertex.PoseStack;

@Pseudo
@Mixin(targets = "punchy.client.render.PunchyArmRenderer", remap = false)
public class PunchyArmRendererMixin {
    @SuppressWarnings("rawtypes")
    @Redirect(
        method = {"renderArmArmor", "original$renderArmArmor"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ArmorModelSet;get(Lnet/minecraft/world/entity/EquipmentSlot;)Ljava/lang/Object;"
        ),
        require = 0
    )
    private static Object thaumaturgetinker$useCustomHumanoidArmorModel(
        ArmorModelSet<?> modelSet,
        EquipmentSlot slot,
        AvatarRenderer<AbstractClientPlayer> renderer,
        AvatarRenderState state,
        PlayerModel playerModel,
        AbstractClientPlayer player,
        HumanoidArm arm,
        PoseStack poseStack,
        SubmitNodeCollector collector,
        int packedLight
    ) {
        Object defaultModel = modelSet.get(slot);
        if (state != null && !state.chestEquipment.isEmpty() && defaultModel instanceof Model<?> baseModel) {
            Model customModel = IClientItemExtensions.of(state.chestEquipment)
                .getHumanoidArmorModel(state.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID, baseModel);
            if (customModel instanceof HumanoidModel<?>) {
                return customModel;
            }
        }
        return defaultModel;
    }
}
