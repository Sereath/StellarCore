package github.kasuminova.stellarcore.mixin.enderutilities;

import fi.dy.masa.enderutilities.item.ItemNullifier;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import github.kasuminova.stellarcore.common.config.StellarCoreConfig;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemNullifier.class)
public class MixinItemNullifier {

    @Redirect(
            method = "handleItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/enderutilities/util/InventoryUtils;tryInsertItemStackToExistingStacksInInventory(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"
            ),
            remap = false
    )
    private static ItemStack tryFixHandleItems(final IItemHandler handler, final ItemStack stack) {
        if (!StellarCoreConfig.BUG_FIXES.enderUtilities.itemNullifier) {
            InventoryUtils.tryInsertItemStackToExistingStacksInInventory(handler, stack);
        }
        return stack;
    }

}
