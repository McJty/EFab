package mcjty.efab.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.thirteen.Context;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetMonitorText implements IMessage {

    public static String CMD_GETMESSAGES = "getMessages";
    public static String CLIENTCMD_GETMESSAGES = "getMessages";
    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetMonitorText() {
    }

    public PacketGetMonitorText(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketGetMonitorText(BlockPos pos) {
        this.pos = pos;
        this.params = TypedMap.EMPTY;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        params = TypedMapTools.readArguments(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        TypedMapTools.writeArguments(buf, params);
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            if(!(te instanceof ICommandHandler)) {
                Logging.log("TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<String> list = commandHandler.executeWithResultList(CMD_GETMESSAGES, params, Type.STRING);
            EFabMessages.INSTANCE.sendTo(new PacketMonitorTextReady(pos, CLIENTCMD_GETMESSAGES, list), ctx.getSender());
        });
        ctx.setPacketHandled(true);
    }
}
