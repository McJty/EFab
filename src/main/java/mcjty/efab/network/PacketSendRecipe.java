package mcjty.efab.network;

import io.netty.buffer.ByteBuf;
import mcjty.efab.blocks.crafter.CrafterTE;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.tools.ItemStackList;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSendRecipe implements IMessage {
    private ItemStackList stacks;
    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf buf) {
        int l = buf.readInt();
        stacks = ItemStackList.create(l);
        for (int i = 0 ; i < l ; i++) {
            if (buf.readBoolean()) {
                stacks.set(i, NetworkTools.readItemStack(buf));
            } else {
                stacks.set(i, ItemStackTools.getEmptyStack());
            }
        }
        if (buf.readBoolean()) {
            pos = NetworkTools.readPos(buf);
        } else {
            pos = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(stacks.size());
        for (ItemStack stack : stacks) {
            if (ItemStackTools.isValid(stack)) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stack);
            } else {
                buf.writeBoolean(false);
            }
        }
        if (pos != null) {
            buf.writeBoolean(true);
            NetworkTools.writePos(buf, pos);
        } else {
            buf.writeBoolean(false);
        }
    }

    public PacketSendRecipe() {
    }

    public PacketSendRecipe(ItemStackList stacks, BlockPos pos) {
        this.stacks = stacks;
        this.pos = pos;
    }

    public static class Handler implements IMessageHandler<PacketSendRecipe, IMessage> {
        @Override
        public IMessage onMessage(PacketSendRecipe message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSendRecipe message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            World world = player.getEntityWorld();
            TileEntity te = world.getTileEntity(message.pos);
            if (te instanceof CrafterTE) {
                CrafterTE acceptor = (CrafterTE) te;
                acceptor.setGridContents(message.stacks);
            }
        }

    }
}