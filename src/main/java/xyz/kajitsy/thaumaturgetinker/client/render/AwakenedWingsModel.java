package xyz.kajitsy.thaumaturgetinker.client.render;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.core.Rotations;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class AwakenedWingsModel extends HumanoidModel<HumanoidRenderState> {
    private static final class WingAnimState {
        float flightBlend = 0.0f;
        float leftY = -0.6108652f;
        float rightY = 0.4468043f;
        float leftZ = 0.0f;
        float rightZ = 0.0f;
        float outerLeftY = -0.14f;
        float outerRightY = 0.14f;
        float lastAge = -1.0f;
    }

    private static final Map<Integer, WingAnimState> WING_STATES = new ConcurrentHashMap<>();

    public AwakenedWingsModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);

        if (state instanceof ZombieRenderState zombie) {
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, zombie.isAggressive, zombie);
        }
        if (state instanceof ArmorStandRenderState stand) {
            applyStandPose(stand);
        }

        boolean flying = state.isFallFlying;
        int entityKey = 0;
        if (state instanceof AvatarRenderState avatar) {
            entityKey = avatar.id;
            if (!flying) {
                var mc = Minecraft.getInstance();
                Player player = null;
                if (mc.level != null) {
                    var entity = mc.level.getEntity(avatar.id);
                    if (entity instanceof Player p) {
                        player = p;
                    }
                }
                if (player == null && mc.player != null && (avatar.id == mc.player.getId() || avatar.id == 0)) {
                    player = mc.player;
                }
                if (player != null) {
                    if (player.getAbilities().flying) {
                        flying = true;
                    } else if (player instanceof RemotePlayer) {
                        if (!player.onGround() && !player.isInWater() && !player.isPassenger() && (player.getDeltaMovement().y > -0.05 || player.fallDistance == 0)) {
                            flying = true;
                        }
                    }
                }
            }
        }

        // 3D Robe flaps & tails animation (present on chestplate models)
        if (body.hasChild("front_flap") && body.hasChild("back_flap")) {
            var frontFlap = body.getChild("front_flap");
            var backFlap = body.getChild("back_flap");
            if (state instanceof ArmorStandRenderState) {
                frontFlap.xRot = -0.05f;
                backFlap.xRot = 0.08f;
            } else {
                float fwd = Math.min(0.0f, Math.min(rightLeg.xRot, leftLeg.xRot));
                float bwd = Math.max(0.0f, Math.max(rightLeg.xRot, leftLeg.xRot));
                float crouch = state.isCrouching ? 0.32f : 0.0f;
                float flutter = flying
                    ? (float) (Math.sin(state.ageInTicks * 0.35f) * 0.09f + 0.22f)
                    : (float) (Math.sin(state.ageInTicks * 0.08f) * 0.025f);
                frontFlap.xRot = Math.min(-0.05f, fwd * 0.78f - crouch - flutter * 0.35f);
                backFlap.xRot = Math.max(0.08f, bwd * 0.78f + crouch * 0.85f + flutter);
                if (frontFlap.hasChild("front_hem")) {
                    frontFlap.getChild("front_hem").xRot = Math.min(0.0f, fwd * 0.35f - flutter * 0.25f);
                }
                if (backFlap.hasChild("back_hem")) {
                    backFlap.getChild("back_hem").xRot = Math.max(0.06f, bwd * 0.35f + flutter * 0.5f);
                }
            }
        }

        // 3D Volumetric Wings animation with smooth ease-in and ease-out transitions
        if (body.hasChild("wing1") && body.hasChild("wing2")) {
            var left = body.getChild("wing1");
            var right = body.getChild("wing2");

            WingAnimState anim = WING_STATES.computeIfAbsent(entityKey, k -> new WingAnimState());
            float dt = anim.lastAge < 0.0f ? 0.0f : Mth.clamp(state.ageInTicks - anim.lastAge, 0.0f, 3.0f);
            anim.lastAge = state.ageInTicks;

            if (dt == 0.0f && state.ageInTicks == 0.0f) {
                anim.flightBlend = flying ? 1.0f : 0.0f;
            } else {
                float step = (dt > 0.0f ? dt : 0.25f) * 0.16f;
                anim.flightBlend = Mth.clamp(anim.flightBlend + (flying ? step : -step), 0.0f, 1.0f);
            }

            // Smoothstep curve for organic ease-in / ease-out between flying and resting
            float s = anim.flightBlend * anim.flightBlend * (3.0f - 2.0f * anim.flightBlend);

            float crouchOpen = state.isCrouching ? 0.16f : 0.0f;
            float idle = (float) (Math.sin(state.ageInTicks * 0.06f) * 0.03f);
            float restLeftY = -0.6108652f - idle - crouchOpen;
            float restRightY = 0.4468043f + idle + crouchOpen;
            float restOuterLeftY = -0.14f - idle * 0.5f;
            float restOuterRightY = 0.14f + idle * 0.5f;

            float flyLeftY = (float) ((Math.sin(state.ageInTicks) + 1.0) * (Math.PI / 180.0) * 15.0 - 0.6108651757240295);
            float flyRightY = -flyLeftY;
            float phase = state.ageInTicks * 0.42f;
            float flyLeftZ = (float) (Math.cos(phase) * 0.07f - 0.04f);
            float flyRightZ = -flyLeftZ;
            float tipWave = (float) (Math.sin(state.ageInTicks - 0.9f) * 0.22f);
            float flyOuterLeftY = tipWave - 0.10f;
            float flyOuterRightY = -tipWave + 0.10f;

            float targetLeftY = Mth.lerp(s, restLeftY, flyLeftY);
            float targetRightY = Mth.lerp(s, restRightY, flyRightY);
            float targetLeftZ = Mth.lerp(s, 0.0f, flyLeftZ);
            float targetRightZ = Mth.lerp(s, 0.0f, flyRightZ);
            float targetOuterLeftY = Mth.lerp(s, restOuterLeftY, flyOuterLeftY);
            float targetOuterRightY = Mth.lerp(s, restOuterRightY, flyOuterRightY);

            float damp = dt > 0.0f ? Mth.clamp(dt * 0.42f, 0.12f, 1.0f) : 1.0f;
            anim.leftY = Mth.lerp(damp, anim.leftY, targetLeftY);
            anim.rightY = Mth.lerp(damp, anim.rightY, targetRightY);
            anim.leftZ = Mth.lerp(damp, anim.leftZ, targetLeftZ);
            anim.rightZ = Mth.lerp(damp, anim.rightZ, targetRightZ);
            anim.outerLeftY = Mth.lerp(damp, anim.outerLeftY, targetOuterLeftY);
            anim.outerRightY = Mth.lerp(damp, anim.outerRightY, targetOuterRightY);

            left.yRot = anim.leftY;
            right.yRot = anim.rightY;
            left.zRot = anim.leftZ;
            right.zRot = anim.rightZ;

            if (left.hasChild("wing1_outer") && right.hasChild("wing2_outer")) {
                left.getChild("wing1_outer").yRot = anim.outerLeftY;
                right.getChild("wing2_outer").yRot = anim.outerRightY;
            }
        }
    }

    private void applyStandPose(ArmorStandRenderState stand) {
        setRotation(this.head, stand.headPose);
        setRotation(this.body, stand.bodyPose);
        setRotation(this.leftArm, stand.leftArmPose);
        setRotation(this.rightArm, stand.rightArmPose);
        setRotation(this.leftLeg, stand.leftLegPose);
        setRotation(this.rightLeg, stand.rightLegPose);
    }

    private static void setRotation(ModelPart part, Rotations rot) {
        final float degToRad = (float) (Math.PI / 180.0);
        part.xRot = degToRad * rot.x();
        part.yRot = degToRad * rot.y();
        part.zRot = degToRad * rot.z();
    }
}
