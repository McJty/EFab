package mcjty.efab.compat.jei.grid;

import mcjty.efab.EFab;
import mcjty.efab.blocks.grid.GridGui;
import mcjty.efab.recipes.IEFabRecipe;
import mcjty.efab.recipes.RecipeTier;
import mcjty.efab.render.RenderTools;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class GridCraftingRecipeWrapper implements IRecipeWrapper, IShapedCraftingRecipeWrapper {

    private final IEFabRecipe recipe;
    private final List<List<ItemStack>> inputs;
    private final List<List<FluidStack>> fluidInputs;
    private final ItemStack output;
    private final List<Tooltip> tooltips = new ArrayList<>();

    private static final ResourceLocation ICONS = new ResourceLocation(EFab.MODID, "textures/gui/icons.png");


    public GridCraftingRecipeWrapper(IEFabRecipe recipe) {
        this.recipe = recipe;

        this.inputs = recipe.getInputLists();
        this.fluidInputs = Collections.singletonList(recipe.getRequiredFluids());
        this.output = recipe.cast().getRecipeOutput();
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setOutput(ItemStack.class, output);
        ingredients.setInputLists(ItemStack.class, inputs);
        ingredients.setInputLists(FluidStack.class, fluidInputs);
    }

    private static DecimalFormat format = new DecimalFormat("#.##");

    private static String formatPower(long l) {
        if (l < 10000) {
            return Long.toString(l);
        } else if (l < 1000000) {
            Double d = l / 1000.0;
            return format.format(d)+"K";
        } else if (l < 1000000000L) {
            Double d = l / 1000000.0;
            return format.format(d)+"M";
        } else {
            Double d = l / 1000000000.0;
            return format.format(d)+"G";
        }
    }



    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        tooltips.clear();

        Set<RecipeTier> tiers = recipe.getRequiredTiers();
        int y = 60;
        minecraft.fontRenderer.drawString("Time", 0, y, Color.black.getRGB());
        minecraft.fontRenderer.drawString("" + GridGui.getTime(recipe.getCraftTime(), false), 28, y, Color.blue.getRGB());
        GlStateManager.color(1, 1, 1);
        int x = 0;

        y += 12;

        int perTick = recipe.getRequiredRfPerTick();
        if (perTick > 0) {
            minecraft.fontRenderer.drawString("RF", 0, y, Color.black.getRGB());
            long total = perTick * (long) recipe.getCraftTime();
            minecraft.fontRenderer.drawString("" + formatPower(total) + "RF @ " + formatPower(perTick) + "RF/t", 28, y, Color.blue.getRGB());
            GlStateManager.color(1, 1, 1);
            x = 0;
            y += 12;
        }

        if (recipe.getRequiredManaPerTick() > 0) {
            minecraft.fontRenderer.drawString("Mana", 0, y, Color.black.getRGB());
            minecraft.fontRenderer.drawString("" + recipe.getRequiredManaPerTick() + " mana/tick", 28, y, Color.blue.getRGB());
            GlStateManager.color(1, 1, 1);
            x = 0;
            y += 12;
        }

        int cury = y;
        for (RecipeTier tier : tiers) {
            minecraft.getTextureManager().bindTexture(ICONS);
            RenderTools.drawTexturedModalRect(x, y, tier.getIconX(), tier.getIconY(), 16, 16);
            tooltips.add(new Tooltip(x, y, 16, 16).add(tier.name()));
            x += 20;
            if (x + 18 >= 80) {
                x = 0;
                y += 20;
            }
        }

        y = cury;
        if (!recipe.getRequiredFluids().isEmpty()) {
            x = 80;
            for (FluidStack stack : recipe.getRequiredFluids()) {
                String name = stack.getLocalizedName();
                GlStateManager.disableTexture2D();
                RenderTools.drawBox(x-1, y-1, 18, 18, 0, 0, 0, 1f);
                GlStateManager.enableTexture2D();
                RenderTools.renderFluidStack(minecraft, stack, x, y);
                if (recipe.getRequiredFluids().size() <= 1) {
                    minecraft.fontRenderer.drawString(name, x+20, y+5, Color.blue.getRGB());
                }
                tooltips.add(new Tooltip(x, y, 16, 16)
                        .add(TextFormatting.BLUE + "Fluid: " + TextFormatting.WHITE + name)
                        .add(TextFormatting.BLUE + "Amount: " + TextFormatting.WHITE + stack.amount + " mb"));
                x += 20;
            }
        }
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        for (Tooltip tooltip : tooltips) {
            if (tooltip.in(mouseX, mouseY)) {
                return tooltip.getTooltips();
            }
        }

        return Collections.emptyList();
    }
}
