package mcjty.efab.blocks.monitor;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class AutoCraftingMonitorRenderer extends TileEntitySpecialRenderer<AutoCraftingMonitorTE> {

    @Override
    public void render(AutoCraftingMonitorTE te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        if (te.getWorld().isAirBlock(te.getPos())) {
            return;
        }
        MonitorRenderHelper.renderHud(te, x, y, z, 0, false);
    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(AutoCraftingMonitorTE.class, new AutoCraftingMonitorRenderer());
    }
}
