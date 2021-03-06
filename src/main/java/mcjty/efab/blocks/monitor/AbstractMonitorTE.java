package mcjty.efab.blocks.monitor;

import mcjty.efab.blocks.GenericEFabTile;
import mcjty.efab.blocks.ISpeedBooster;
import mcjty.efab.config.ConfigSetup;
import mcjty.efab.network.PacketGetMonitorText;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMonitorTE extends GenericEFabTile implements ITickable, ISpeedBooster, IMonitor {

    private float speed = 1.0f;
    private int speedBoost = 0;

    private long lastHudTime = 0;
    private List<String> clientHudLog = new ArrayList<>();
    protected List<String> messages = null;

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
        markDirtyClient();
    }

    @Override
    public int getSpeedBoost() {
        return speedBoost;
    }

    @Override
    public void setSpeedBoost(int speedBoost) {
        this.speedBoost = speedBoost;
        markDirtyClient();
    }

    @Override
    public BlockPos getMonitorPos() {
        return pos;
    }

    @Override
    public void update() {
        if (speed > 1.0f) {
            speed -= ConfigSetup.steamWheelSpinDown.get();
            if (speed < 1.0f) {
                speed = 1.0f;
            }
            markDirtyQuick();
        }
        if (speedBoost > 0) {
            speedBoost--;
            speed += ConfigSetup.steamWheelSpeedUp.get();
            if (speed > ConfigSetup.maxSteamWheelSpeed.get()) {
                speed = (float) ConfigSetup.maxSteamWheelSpeed.get();
            }
            markDirtyQuick();
        }
    }

    @Override
    public List<String> getClientLog() {
        return clientHudLog;
    }

    @Override
    public long getLastUpdateTime() {
        return lastHudTime;
    }

    @Override
    public void setLastUpdateTime(long t) {
        lastHudTime = t;
    }

    protected List<String> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
            getDefaultMessages(messages);
        }
        return messages;
    }

    protected abstract void getDefaultMessages(List<String> messages);

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        speed = tagCompound.getFloat("speed");
        speedBoost = tagCompound.getInteger("boost");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setFloat("speed", speed);
        tagCompound.setInteger("boost", speedBoost);
        return super.writeToNBT(tagCompound);
    }

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, TypedMap args, Type<T> type) {
        List<T> rc = super.executeWithResultList(command, args, type);
        if (!rc.isEmpty()) {
            return rc;
        }
        if (PacketGetMonitorText.CMD_GETMESSAGES.equals(command)) {
            return type.convert(getMessages());
        }
        return rc;
    }

    @Override
    public <T> boolean receiveListFromServer(String command, List<T> list, Type<T> type) {
        boolean rc = super.receiveListFromServer(command, list, type);
        if (rc) {
            return true;
        }
        if (PacketGetMonitorText.CLIENTCMD_GETMESSAGES.equals(command)) {
            clientHudLog = Type.STRING.convert(list);
            return true;
        }
        return false;
    }

}
