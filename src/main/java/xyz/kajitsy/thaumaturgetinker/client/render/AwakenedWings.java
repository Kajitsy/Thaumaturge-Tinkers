package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedBoots;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedChest;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedHelm;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedLegs;
import xyz.kajitsy.thaumaturgetinker.item.KamiArmor;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
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

    public static class WingsModel extends AwakenedWingsModel {
        public WingsModel(ModelPart root) {
            super(root);
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

        // Base cowl (inflation 1.0f clears the player's 2nd skin/hat layer which is at 0.5f) + 3D neck collar mantle
        head.addOrReplaceChild(
            "cowl_base",
            CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(1.0f))
                .texOffs(24, 57).addBox(-4.0f, -0.4f, -3.5f, 8.0f, 2.0f, 4.0f, new CubeDeformation(0.45f)),
            PartPose.ZERO
        );

        // 3D Forward-slanted golden brow / hood visor
        head.addOrReplaceChild(
            "hood_brow",
            CubeListBuilder.create()
                .texOffs(0, 50).addBox(-4.5f, -1.0f, -1.5f, 9.0f, 2.0f, 3.0f, new CubeDeformation(0.35f)),
            PartPose.offsetAndRotation(0.0f, -7.8f, -4.2f, 0.18f, 0.0f, 0.0f)
        );

        // 3D Swept mage hood peak (upper + lower trailing tip)
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
            // 3D Awakened celestial crown & protruding Aqua Sapphire bezel
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

        // 1. Torso base + 3D raised chest plastron, back plate & waist belt with golden clasp
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

        // 2. Animated 3D front & back robe tabards/tails
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

        // 3. Right & Left Sleeves + 3D Pauldrons (placed directly on right_arm/left_arm with 4x12x4 base at 40,16 so first-person mods render cleanly!)
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

        // 4. 3D Volumetric Stepped Wings on Awakened Chestplate (every feather tier is a solid 100%-opaque 3D box, zero overlap with arm/torso UVs!)
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

        // Waist girdle
        body.addOrReplaceChild(
            "legs_waist",
            CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0f, 8.0f, -2.0f, 8.0f, 4.0f, 4.0f, new CubeDeformation(0.45f)),
            PartPose.ZERO
        );

        // Right leg greave + 3D angled hip tasset & 3D knee guard
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

        // Left leg greave + 3D angled hip tasset & 3D knee guard
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

        // Right 3D Boot: base boot + 3D flared ichor cuff + 3D shin guard + 3D protruding toe cap
        CubeListBuilder rightBoot = CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.75f))
            .texOffs(40, 50).addBox(-2.5f, 5.5f, -2.5f, 5.0f, 2.0f, 5.0f, new CubeDeformation(0.15f))
            .texOffs(48, 57).addBox(-2.0f, 9.8f, -3.35f, 4.0f, 2.0f, 2.0f, new CubeDeformation(0.12f))
            .texOffs(30, 44).addBox(-1.5f, 6.8f, -2.9f, 3.0f, 3.0f, 1.0f, new CubeDeformation(0.08f));
        if (awakened) {
            rightBoot.texOffs(32, 36).addBox(-1.0f, 7.3f, -3.3f, 2.0f, 2.0f, 1.0f, new CubeDeformation(0.05f));
        }
        rightLeg.addOrReplaceChild("right_boot", rightBoot, PartPose.ZERO);

        // Left 3D Boot
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
