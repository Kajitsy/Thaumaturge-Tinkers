package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedBoots;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedChest;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedHelm;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedLegs;
import xyz.kajitsy.thaumaturgetinker.item.KamiArmor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = "thaumaturgetinker", value = Dist.CLIENT)
public final class AwakenedWings {
    private static final int TEX_WIDTH = 64;
    private static final int TEX_HEIGHT = 64;

    public static final class WingsModel extends HumanoidModel<HumanoidRenderState> {
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

        public WingsModel(ModelPart root) {
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

    public static WingsModel model;
    private static WingsModel ichorHead;
    private static WingsModel ichorChest;
    private static WingsModel ichorLegs;
    private static WingsModel ichorBoots;
    private static WingsModel gemHead;
    private static WingsModel gemLegs;
    private static WingsModel gemBoots;

    public static WingsModel model() {
        if (model == null) {
            model = new WingsModel(createChestLayer(true).bakeRoot());
        }
        return model;
    }

    public static WingsModel modelFor(ItemStack stack, EquipmentClientInfo.LayerType layerType, boolean awakened) {
        if (layerType == EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS) {
            if (awakened) {
                if (gemLegs == null) gemLegs = new WingsModel(createLegsLayer(true).bakeRoot());
                return gemLegs;
            } else {
                if (ichorLegs == null) ichorLegs = new WingsModel(createLegsLayer(false).bakeRoot());
                return ichorLegs;
            }
        }
        Equippable eq = stack.get(DataComponents.EQUIPPABLE);
        EquipmentSlot slot = eq != null ? eq.slot() : EquipmentSlot.CHEST;
        return switch (slot) {
            case HEAD -> {
                if (awakened) {
                    if (gemHead == null) gemHead = new WingsModel(createHeadLayer(true).bakeRoot());
                    yield gemHead;
                } else {
                    if (ichorHead == null) ichorHead = new WingsModel(createHeadLayer(false).bakeRoot());
                    yield ichorHead;
                }
            }
            case FEET -> {
                if (awakened) {
                    if (gemBoots == null) gemBoots = new WingsModel(createBootsLayer(true).bakeRoot());
                    yield gemBoots;
                } else {
                    if (ichorBoots == null) ichorBoots = new WingsModel(createBootsLayer(false).bakeRoot());
                    yield ichorBoots;
                }
            }
            case LEGS -> {
                if (awakened) {
                    if (gemLegs == null) gemLegs = new WingsModel(createLegsLayer(true).bakeRoot());
                    yield gemLegs;
                } else {
                    if (ichorLegs == null) ichorLegs = new WingsModel(createLegsLayer(false).bakeRoot());
                    yield ichorLegs;
                }
            }
            default -> {
                if (awakened) {
                    yield model();
                } else {
                    if (ichorChest == null) ichorChest = new WingsModel(createChestLayer(false).bakeRoot());
                    yield ichorChest;
                }
            }
        };
    }

    private static MeshDefinition emptyHumanoidMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0f, 2.0f, 0.0f));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0f, 2.0f, 0.0f));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9f, 12.0f, 0.0f));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9f, 12.0f, 0.0f));
        return mesh;
    }

    public static LayerDefinition createHeadLayer(boolean awakened) {
        MeshDefinition mesh = emptyHumanoidMesh();
        PartDefinition head = mesh.getRoot().getChild("head");

        // Base cowl (inflation 1.0f clears the player's 2nd skin/hat layer which is at 0.5f)
        head.addOrReplaceChild(
            "cowl_base",
            CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(1.0f))
                .texOffs(24, 57).addBox(-4.0f, -0.4f, -3.5f, 8.0f, 2.0f, 4.0f, new CubeDeformation(0.45f)),
            PartPose.ZERO
        );

        head.addOrReplaceChild(
            "hood_brow",
            CubeListBuilder.create()
                .texOffs(0, 50).addBox(-4.5f, -1.0f, -1.5f, 9.0f, 2.0f, 3.0f, new CubeDeformation(0.35f)),
            PartPose.offsetAndRotation(0.0f, -7.8f, -4.2f, 0.18f, 0.0f, 0.0f)
        );

        PartDefinition peakUpper = head.addOrReplaceChild(
            "hood_peak_upper",
            CubeListBuilder.create()
                .texOffs(0, 55).addBox(-3.5f, -2.5f, 0.0f, 7.0f, 5.0f, 4.0f, new CubeDeformation(0.2f)),
            PartPose.offsetAndRotation(0.0f, -5.8f, 4.2f, -0.32f, 0.0f, 0.0f)
        );
        peakUpper.addOrReplaceChild(
            "hood_peak_lower",
            CubeListBuilder.create()
                .texOffs(24, 50).addBox(-2.0f, -1.5f, 0.0f, 4.0f, 3.0f, 4.0f, new CubeDeformation(0.05f)),
            PartPose.offsetAndRotation(0.0f, 0.2f, 3.5f, -0.42f, 0.0f, 0.0f)
        );

        if (awakened) {
            head.addOrReplaceChild(
                "awakened_crown",
                CubeListBuilder.create()
                    .texOffs(48, 61).addBox(-3.0f, -9.5f, -5.5f, 6.0f, 2.0f, 1.0f, new CubeDeformation(0.15f))
                    .texOffs(56, 28).addBox(-1.5f, -9.0f, -6.05f, 3.0f, 3.0f, 1.0f, new CubeDeformation(0.05f)),
                PartPose.ZERO
            );
        }

        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    public static LayerDefinition createChestLayer(boolean awakened) {
        MeshDefinition mesh = emptyHumanoidMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");

        body.addOrReplaceChild(
            "robe_torso",
            CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.75f))
                .texOffs(0, 39).addBox(-3.5f, 1.0f, -3.1f, 7.0f, 4.0f, 1.0f, new CubeDeformation(0.05f))
                .texOffs(16, 39).addBox(-3.5f, 1.0f, 2.1f, 7.0f, 4.0f, 1.0f, new CubeDeformation(0.05f))
                .texOffs(0, 32).addBox(-4.5f, 9.2f, -2.5f, 9.0f, 2.0f, 5.0f, new CubeDeformation(0.15f))
                .texOffs(28, 32).addBox(-1.5f, 8.9f, -3.25f, 3.0f, 3.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.ZERO
        );

        PartDefinition frontFlap = body.addOrReplaceChild(
            "front_flap",
            CubeListBuilder.create()
                .texOffs(0, 44).addBox(-3.5f, 0.0f, -0.5f, 7.0f, 5.0f, 1.0f, new CubeDeformation(0.05f)),
            PartPose.offsetAndRotation(0.0f, 10.8f, -2.4f, -0.05f, 0.0f, 0.0f)
        );
        frontFlap.addOrReplaceChild(
            "front_hem",
            CubeListBuilder.create()
                .texOffs(16, 44).addBox(-3.0f, 0.0f, -0.5f, 6.0f, 3.0f, 1.0f, new CubeDeformation(0.02f)),
            PartPose.offset(0.0f, 4.8f, 0.0f)
        );

        PartDefinition backFlap = body.addOrReplaceChild(
            "back_flap",
            CubeListBuilder.create()
                .texOffs(0, 44).addBox(-3.5f, 0.0f, -0.5f, 7.0f, 5.0f, 1.0f, new CubeDeformation(0.08f)),
            PartPose.offsetAndRotation(0.0f, 10.8f, 2.4f, 0.08f, 0.0f, 0.0f)
        );
        backFlap.addOrReplaceChild(
            "back_hem",
            CubeListBuilder.create()
                .texOffs(16, 44).addBox(-3.0f, 0.0f, -0.5f, 6.0f, 3.0f, 1.0f, new CubeDeformation(0.04f)),
            PartPose.offset(0.0f, 4.8f, 0.0f)
        );

        // Placed directly on right_arm/left_arm with 4x12x4 base at 40,16 so first-person mods render cleanly
        CubeListBuilder rightSleeve = CubeListBuilder.create()
            .texOffs(40, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.55f))
            .texOffs(40, 32).addBox(-3.5f, -2.5f, -2.5f, 5.0f, 4.0f, 5.0f, new CubeDeformation(0.2f));
        if (awakened) {
            rightSleeve.texOffs(32, 32).addBox(-3.85f, -0.8f, -1.0f, 1.0f, 2.0f, 2.0f, new CubeDeformation(0.05f));
        }
        root.addOrReplaceChild("right_arm", rightSleeve, PartPose.offset(-5.0f, 2.0f, 0.0f));

        CubeListBuilder leftSleeve = CubeListBuilder.create()
            .texOffs(40, 16).mirror().addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.55f)).mirror(false)
            .texOffs(40, 32).mirror().addBox(-1.5f, -2.5f, -2.5f, 5.0f, 4.0f, 5.0f, new CubeDeformation(0.2f)).mirror(false);
        if (awakened) {
            leftSleeve.texOffs(32, 32).mirror().addBox(2.85f, -0.8f, -1.0f, 1.0f, 2.0f, 2.0f, new CubeDeformation(0.05f)).mirror(false);
        }
        root.addOrReplaceChild("left_arm", leftSleeve, PartPose.offset(5.0f, 2.0f, 0.0f));

        // Zero overlap with arm/torso UVs
        if (awakened) {
            PartDefinition wing1 = body.addOrReplaceChild(
                "wing1",
                CubeListBuilder.create()
                    .texOffs(40, 0).addBox(-0.45f, 0.0f, 0.0f, 1.0f, 2.0f, 7.0f, new CubeDeformation(0.02f))
                    .texOffs(50, 41).addBox(-0.35f, 2.0f, 1.0f, 1.0f, 2.0f, 6.0f, new CubeDeformation(0.0f))
                    .texOffs(38, 48).addBox(-0.25f, 4.0f, 3.0f, 1.0f, 2.0f, 4.0f, new CubeDeformation(-0.02f))
                    .texOffs(16, -12).addBox(0.0f, 0.0f, 0.0f, 0.0f, 7.0f, 12.0f),
                PartPose.offsetAndRotation(-2.0f, 1.0f, 2.0f, 0.0f, -0.6108652f, 0.0f)
            );
            wing1.addOrReplaceChild(
                "wing1_outer",
                CubeListBuilder.create()
                    .texOffs(52, 9).addBox(-0.4f, 0.0f, 0.0f, 1.0f, 2.0f, 5.0f, new CubeDeformation(0.0f))
                    .texOffs(38, 41).addBox(-0.3f, 2.0f, 0.0f, 1.0f, 2.0f, 5.0f, new CubeDeformation(-0.02f))
                    .texOffs(48, 49).addBox(-0.2f, 4.0f, 0.0f, 1.0f, 2.0f, 4.0f, new CubeDeformation(-0.04f))
                    .texOffs(56, 20).addBox(-0.15f, 6.0f, 1.0f, 1.0f, 1.0f, 3.0f, new CubeDeformation(-0.05f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 6.8f, 0.0f, -0.12f, 0.0f)
            );

            PartDefinition wing2 = body.addOrReplaceChild(
                "wing2",
                CubeListBuilder.create()
                    .texOffs(40, 0).mirror().addBox(-0.55f, 0.0f, 0.0f, 1.0f, 2.0f, 7.0f, new CubeDeformation(0.02f)).mirror(false)
                    .texOffs(50, 41).mirror().addBox(-0.65f, 2.0f, 1.0f, 1.0f, 2.0f, 6.0f, new CubeDeformation(0.0f)).mirror(false)
                    .texOffs(38, 48).mirror().addBox(-0.75f, 4.0f, 3.0f, 1.0f, 2.0f, 4.0f, new CubeDeformation(-0.02f)).mirror(false)
                    .texOffs(16, -12).addBox(0.1f, 0.0f, 0.0f, 0.0f, 7.0f, 12.0f),
                PartPose.offsetAndRotation(2.0f, 1.0f, 2.0f, 0.0f, 0.4468043f, 0.0f)
            );
            wing2.addOrReplaceChild(
                "wing2_outer",
                CubeListBuilder.create()
                    .texOffs(52, 9).mirror().addBox(-0.6f, 0.0f, 0.0f, 1.0f, 2.0f, 5.0f, new CubeDeformation(0.0f)).mirror(false)
                    .texOffs(38, 41).mirror().addBox(-0.7f, 2.0f, 0.0f, 1.0f, 2.0f, 5.0f, new CubeDeformation(-0.02f)).mirror(false)
                    .texOffs(48, 49).mirror().addBox(-0.8f, 4.0f, 0.0f, 1.0f, 2.0f, 4.0f, new CubeDeformation(-0.04f)).mirror(false)
                    .texOffs(56, 20).mirror().addBox(-0.85f, 6.0f, 1.0f, 1.0f, 1.0f, 3.0f, new CubeDeformation(-0.05f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 6.8f, 0.0f, 0.12f, 0.0f)
            );
        } else {
            body.addOrReplaceChild("wing1", CubeListBuilder.create(), PartPose.ZERO);
            body.addOrReplaceChild("wing2", CubeListBuilder.create(), PartPose.ZERO);
        }

        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    public static LayerDefinition createLegsLayer(boolean awakened) {
        MeshDefinition mesh = emptyHumanoidMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        PartDefinition rightLeg = root.getChild("right_leg");
        PartDefinition leftLeg = root.getChild("left_leg");

        body.addOrReplaceChild(
            "legs_waist",
            CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0f, 8.0f, -2.0f, 8.0f, 4.0f, 4.0f, new CubeDeformation(0.45f)),
            PartPose.ZERO
        );

        CubeListBuilder rightGreave = CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.38f))
            .texOffs(24, 32).addBox(-1.5f, 3.2f, -2.65f, 3.0f, 3.0f, 1.0f, new CubeDeformation(0.05f));
        rightLeg.addOrReplaceChild("right_greave", rightGreave, PartPose.ZERO);

        CubeListBuilder rightTasset = CubeListBuilder.create()
            .texOffs(0, 32).addBox(-2.6f, -0.5f, -2.5f, 5.0f, 7.0f, 5.0f, new CubeDeformation(-0.05f));
        if (awakened) {
            rightTasset.texOffs(36, 32).addBox(-3.0f, 1.2f, -1.0f, 1.0f, 2.0f, 2.0f, new CubeDeformation(0.05f));
        }
        rightLeg.addOrReplaceChild(
            "right_tasset",
            rightTasset,
            PartPose.offsetAndRotation(-0.25f, 0.0f, 0.0f, 0.0f, 0.0f, 0.10f)
        );

        CubeListBuilder leftGreave = CubeListBuilder.create()
            .texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.38f)).mirror(false)
            .texOffs(24, 32).mirror().addBox(-1.5f, 3.2f, -2.65f, 3.0f, 3.0f, 1.0f, new CubeDeformation(0.05f)).mirror(false);
        leftLeg.addOrReplaceChild("left_greave", leftGreave, PartPose.ZERO);

        CubeListBuilder leftTasset = CubeListBuilder.create()
            .texOffs(0, 32).mirror().addBox(-2.4f, -0.5f, -2.5f, 5.0f, 7.0f, 5.0f, new CubeDeformation(-0.05f)).mirror(false);
        if (awakened) {
            leftTasset.texOffs(36, 32).mirror().addBox(2.0f, 1.2f, -1.0f, 1.0f, 2.0f, 2.0f, new CubeDeformation(0.05f)).mirror(false);
        }
        leftLeg.addOrReplaceChild(
            "left_tasset",
            leftTasset,
            PartPose.offsetAndRotation(0.25f, 0.0f, 0.0f, 0.0f, 0.0f, -0.10f)
        );

        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    public static LayerDefinition createBootsLayer(boolean awakened) {
        MeshDefinition mesh = emptyHumanoidMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition rightLeg = root.getChild("right_leg");
        PartDefinition leftLeg = root.getChild("left_leg");

        CubeListBuilder rightBoot = CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.75f))
            .texOffs(40, 50).addBox(-2.5f, 5.5f, -2.5f, 5.0f, 2.0f, 5.0f, new CubeDeformation(0.15f))
            .texOffs(48, 57).addBox(-2.0f, 9.8f, -3.35f, 4.0f, 2.0f, 2.0f, new CubeDeformation(0.12f))
            .texOffs(30, 44).addBox(-1.5f, 6.8f, -2.9f, 3.0f, 3.0f, 1.0f, new CubeDeformation(0.08f));
        if (awakened) {
            rightBoot.texOffs(32, 36).addBox(-1.0f, 7.3f, -3.3f, 2.0f, 2.0f, 1.0f, new CubeDeformation(0.05f));
        }
        rightLeg.addOrReplaceChild("right_boot", rightBoot, PartPose.ZERO);

        CubeListBuilder leftBoot = CubeListBuilder.create()
            .texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.75f)).mirror(false)
            .texOffs(40, 50).mirror().addBox(-2.5f, 5.5f, -2.5f, 5.0f, 2.0f, 5.0f, new CubeDeformation(0.15f)).mirror(false)
            .texOffs(48, 57).mirror().addBox(-2.0f, 9.8f, -3.35f, 4.0f, 2.0f, 2.0f, new CubeDeformation(0.12f)).mirror(false)
            .texOffs(30, 44).mirror().addBox(-1.5f, 6.8f, -2.9f, 3.0f, 3.0f, 1.0f, new CubeDeformation(0.08f)).mirror(false);
        if (awakened) {
            leftBoot.texOffs(32, 36).mirror().addBox(-1.0f, 7.3f, -3.3f, 2.0f, 2.0f, 1.0f, new CubeDeformation(0.05f)).mirror(false);
        }
        leftLeg.addOrReplaceChild("left_boot", leftBoot, PartPose.ZERO);

        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    @SubscribeEvent
    public static void register(RegisterClientExtensionsEvent event) {
        if (!KamiStartupConfig.enabled()) return;

        IClientItemExtensions normalExt = new IClientItemExtensions() {
            @Override
            @SuppressWarnings("rawtypes")
            public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
                return modelFor(stack, layerType, false);
            }
        };

        IClientItemExtensions awakenedExt = new IClientItemExtensions() {
            @Override
            @SuppressWarnings("rawtypes")
            public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
                return modelFor(stack, layerType, true);
            }
        };

        event.registerItem(
            normalExt,
            KamiArmor.ITEMS.stream().map(holder -> (Item) holder.get()).toArray(Item[]::new)
        );

        event.registerItem(
            awakenedExt,
            AwakenedHelm.ITEM.get(),
            AwakenedChest.ITEM.get(),
            AwakenedLegs.ITEM.get(),
            AwakenedBoots.ITEM.get()
        );
    }
}
