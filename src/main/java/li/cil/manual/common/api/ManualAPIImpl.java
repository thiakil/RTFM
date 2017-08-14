package li.cil.manual.common.api;

import li.cil.manual.api.API;
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
        Manual manual = new ManualImpl().setRegistryName(manualID);
        API.manuals.register(manual);
        return manual;
    }

    @Override
    public Manual createManual(final ResourceLocation manualID, final String unlocalizedName) {
        Manual manual = li.cil.manual.api.ManualAPI.createManual(manualID);
        return null; //TODO create and register itemstack
    }

    @Nullable
    @Override
    public ItemStack stackFor(final ResourceLocation manualID) {
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

    @Nullable
    @Override
    public ResourceLocation getManualID(final Manual manual) {
        return API.manuals.getKey(manual);
    }
}
