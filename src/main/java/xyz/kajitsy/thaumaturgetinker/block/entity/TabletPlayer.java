package xyz.kajitsy.thaumaturgetinker.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import java.util.UUID;

public final class TabletPlayer extends FakePlayer {
    public TabletPlayer(ServerLevel level) {
        super(level, new GameProfile(UUID.fromString("a8f026a0-135b-11e4-9191-0800200c9a66"), "[ThaumcraftTablet]"));
        setGameMode(GameType.SURVIVAL);
    }

    public void prepare(AnimationTabletEntity tile) {
        getInventory().clearContent();
        getAbilities().instabuild = false;
        setHealth(20);
        stopRiding();
        ejectPassengers();
        setDeltaMovement(Vec3.ZERO);
        snapTo(tile.getBlockPos().getX() + .5, tile.getBlockPos().getY() + 1.6, tile.getBlockPos().getZ() + .5, tile.facing().toYRot(), -15f);
        setItemInHand(InteractionHand.MAIN_HAND, tile.inventory.getItem(0));
        attackStrengthTicker = 100;
    }

    @Override
    public void sendSystemMessage(Component message) {}

    @Override
    public void sendOverlayMessage(Component message) {}
}
