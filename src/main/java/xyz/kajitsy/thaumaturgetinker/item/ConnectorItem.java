package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.entity.TransvectorLink;


import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

public final class ConnectorItem extends Item {
    public ConnectorItem(){super(ThaumicTinkererModern.itemProps("connector", new Properties().stacksTo(1)));}
    @Override public InteractionResult useOn(UseOnContext context){
        var level=context.getLevel();var player=context.getPlayer();var pos=context.getClickedPos();
        if(player==null||!player.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand())||!level.mayInteract(player,pos))return InteractionResult.FAIL;
        if(level.isClientSide())return InteractionResult.SUCCESS;
        var stack=context.getItemInHand();var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();String message;
        if(level.getBlockEntity(pos) instanceof TransvectorLink){
            if(data.contains("Interface")&&pos.asLong()==data.getLong("Interface")){
                data.remove("Interface");data.remove("Dimension");message="ttmisc.connector.clear";
            }else{
                data.putLong("Interface",pos.asLong());data.putString("Dimension",level.dimension().location().toString());message="ttmisc.connector.set";
            }
        }else if(!data.contains("Interface")){
            message="ttmisc.connector.notinterf";
        }else if(!level.dimension().location().toString().equals(data.getString("Dimension"))){message="ttmisc.connector.dimension";}
        else {
            var source=BlockPos.of(data.getLong("Interface"));
            if(!level.isLoaded(source)){message="ttmisc.connector.unloaded";}
            else if(!(level.getBlockEntity(source) instanceof TransvectorLink tile)){data.remove("Interface");data.remove("Dimension");message="ttmisc.connector.notpresent";}
            else if(!tile.inRange(pos)){message="ttmisc.connector.toofar";}
            else if(!level.mayInteract(player,source)){return InteractionResult.FAIL;}
            else if(tile.link(pos)){data.remove("Interface");data.remove("Dimension");message="ttmisc.connector.complete";}
            else message="ttmisc.connector.interffail";
        }
        if(message.equals("ttmisc.connector.set")||message.equals("ttmisc.connector.complete"))level.playSound(null,pos,net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,net.minecraft.sounds.SoundSource.BLOCKS,.8f,1);
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));player.sendSystemMessage(Component.translatable(message));
        return InteractionResult.CONSUME;
    }
}
