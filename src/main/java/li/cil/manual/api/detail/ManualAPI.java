package li.cil.manual.api.detail;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/**
 * @author Vexatos
 */
public interface ManualAPI {

    Manual createManual(ResourceLocation manualID); //TODO Document this

    @Nullable
    Manual manualFor(ItemStack stack); //TODO Document this

    @Nullable
    ResourceLocation getManualID(ItemStack stack); //TODO Document this
}
