package li.cil.manual.common.api;

import li.cil.manual.api.detail.Manual;
import li.cil.manual.api.detail.ManualAPI;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/**
 * @author Vexatos
 */
public class ManualAPIImpl implements ManualAPI {

    public static final ManualAPIImpl INSTANCE = new ManualAPIImpl();

    @Override
    public Manual createManual(final ResourceLocation manualID) {
        return null; //TODO
    }

    @Nullable
    @Override
    public Manual manualFor(final ItemStack stack) {
        return null; //TODO
    }

    @Override
    public ResourceLocation getManualID(final ItemStack stack) {
        return null; //TODO
    }
}
