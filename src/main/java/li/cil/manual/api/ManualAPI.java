package li.cil.manual.api;

import li.cil.manual.api.detail.Manual;
import li.cil.manual.api.manual.ContentProvider;
import li.cil.manual.api.manual.ImageProvider;
import li.cil.manual.api.manual.ImageRenderer;
import li.cil.manual.api.manual.PathProvider;
import li.cil.manual.api.manual.TabIconRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * This API allows interfacing with the in-game manual of RTFM.
 * <p>
 * It allows opening the manual at a desired specific page, as well as
 * registering custom tabs and content callback handlers.
 * <p>
 * Note: this is a <em>client side only</em> API. It will do nothing on
 * dedicated servers (i.e. <tt>API.manual</tt> will be <tt>null</tt>).
 */
public final class ManualAPI {

    @Nullable
    public static Manual createManual(ResourceLocation manualID) { //TODO Document this
        if (API.manualAPI != null) {
            return API.manualAPI.createManual(manualID);
        }
        return null;
    }

    @Nullable
    public static Manual manualFor(ResourceLocation manualID) { //TODO Document this
        if (API.manuals != null) {
            return API.manuals.getValue(manualID);
        }
        return null;
    }

    @Nullable
    public static Manual manualFor(ItemStack stack) { //TODO Document this
        if (API.manualAPI != null) {
            return API.manualAPI.manualFor(stack);
        }
        return null;
    }

    @Nullable
    public ResourceLocation getManualID(ItemStack stack) { //TODO Document this
        if (API.manualAPI != null) {
            return API.manualAPI.getManualID(stack);
        }
        return null;
    }

    /**
     * Register a tab to be displayed next to the manual.
     * <p>
     * These are intended to link to index pages, and for the time being there
     * a relatively low number of tabs that can be displayed, so I'd ask you to
     * only register as many tabs as actually, technically *needed*. Which will
     * usually be one, for your main index page.
     *
     * @param manualID The registry name of the manual.
     * @param renderer the renderer used to render the icon on your tab.
     * @param tooltip  the unlocalized tooltip of the tab, or <tt>null</tt>.
     * @param path     the path to the page to open when the tab is clicked.
     */
    public static void addTab(final ResourceLocation manualID, final TabIconRenderer renderer, final String tooltip, final String path) {
        if (API.manuals != null) {
            Optional.ofNullable(API.manuals.getValue(manualID))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid manual ID: " + manualID.toString()))
                    .addTab(renderer, tooltip, path);
        }
    }

    /**
     * Register a path provider.
     * <p>
     * Path providers are used to find documentation entries for item stacks
     * and blocks in the world.
     *
     * @param manualID The registry name of the manual.
     * @param provider the provider to register.
     */
    public static void addProvider(final ResourceLocation manualID, final PathProvider provider) {
        if (API.manuals != null) {
            Optional.ofNullable(API.manuals.getValue(manualID))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid manual ID: " + manualID.toString()))
                    .addProvider(provider);
        }
    }

    /**
     * Register a content provider.
     * <p>
     * Content providers are used to resolve paths to page content, if the
     * standard system (using Minecraft's resource loading facilities) fails.
     * <p>
     * This can be useful for providing dynamic content, for example.
     *
     * @param manualID The registry name of the manual.
     * @param provider the provider to register.
     */
    public static void addProvider(final ResourceLocation manualID, final ContentProvider provider) {
        if (API.manuals != null) {
            Optional.ofNullable(API.manuals.getValue(manualID))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid manual ID: " + manualID.toString()))
                    .addProvider(provider);
        }
    }

    /**
     * Register an image provider.
     * <p>
     * Image providers are used to render custom content in a page. These are
     * selected via the standard image tag of Markdown, based on the prefix of
     * the image URL, i.e. <tt>![tooltip](prefix:data)</tt> will select the
     * image provider registered for the prefix <tt>prefix</tt>, and pass to
     * it the argument <tt>data</tt>, then use the returned renderer to draw
     * an element in the place of the tag. The provided prefix is expected to
     * be <em>without</em> the colon (<tt>:</tt>).
     * <p>
     * Custom providers are only selected if a prefix is matched, otherwise
     * it'll treat it as a relative path to an image to load via Minecraft's
     * resource providing facilities, and display that.
     *
     * @param manualID The registry name of the manual.
     * @param prefix   the prefix on which to use the provider.
     * @param provider the provider to register.
     */
    public static void addProvider(final ResourceLocation manualID, final String prefix, final ImageProvider provider) {
        if (API.manuals != null) {
            Optional.ofNullable(API.manuals.getValue(manualID))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid manual ID: " + manualID.toString()))
                    .addProvider(prefix, provider);
        }
    }

    /**
     * Get the image renderer for the specified image path.
     * <p>
     * This will look for {@link ImageProvider}s registered for a prefix in the
     * specified path. If there is no match, or the matched content provider
     * does not provide a renderer, this will return <tt>null</tt>.
     *
     * @param manualID The registry name of the manual.
     * @param path     the path to the image to get the renderer for.
     * @return the custom renderer for that path.
     */
    @Nullable
    public static ImageRenderer imageFor(final ResourceLocation manualID, final String path) {
        if (API.manuals != null) {
            return Optional.ofNullable(API.manuals.getValue(manualID))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid manual ID: " + manualID.toString()))
                    .imageFor(path);
        }
        return null;
    }

    // ----------------------------------------------------------------------- //

    /**
     * Look up the documentation path for the specified item stack.
     *
     * @param manualID The registry name of the manual.
     * @param stack    the stack to find the documentation path for.
     * @return the path to the page, <tt>null</tt> if none is known.
     */
    @Nullable
    public static String pathFor(final ResourceLocation manualID, final ItemStack stack) {
        if (API.manuals != null) {
            return Optional.ofNullable(API.manuals.getValue(manualID))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid manual ID: " + manualID.toString()))
                    .pathFor(stack);
        }
        return null;
    }

    /**
     * Look up the documentation for the specified block in the world.
     *
     * @param manualID The registry name of the manual.
     * @param world    the world containing the block.
     * @param pos      the position of the block.
     * @return the path to the page, <tt>null</tt> if none is known.
     */
    @Nullable
    public static String pathFor(final ResourceLocation manualID, final World world, final BlockPos pos) {
        if (API.manuals != null) {
            return Optional.ofNullable(API.manuals.getValue(manualID))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid manual ID: " + manualID.toString()))
                    .pathFor(world, pos);
        }
        return null;
    }

    /**
     * Get the content of the documentation page at the specified location.
     *
     * @param manualID The registry name of the manual.
     * @param path     the path of the page to get the content of.
     * @return the content of the page, or <tt>null</tt> if none exists.
     */
    @Nullable
    public static Iterable<String> contentFor(final ResourceLocation manualID, final String path) {
        if (API.manuals != null) {
            Optional.ofNullable(API.manuals.getValue(manualID))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid manual ID: " + manualID.toString()))
                    .contentFor(path);
        }
        return null;
    }

    // ----------------------------------------------------------------------- //

    /**
     * Open the manual for the specified player.
     * <p>
     * If you wish to display a specific page, call {@link #navigate(ResourceLocation, String)}
     * after this function returns, with the path to the page to show.
     *
     * @param manualID The registry name of the manual.
     * @param player   the player to open the manual for.
     */
    public static void openFor(final ResourceLocation manualID, final EntityPlayer player) {
        if (API.manuals != null) {
            Optional.ofNullable(API.manuals.getValue(manualID))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid manual ID: " + manualID.toString()))
                    .openFor(player);
        }
    }

    /**
     * Reset the history of the manual.
     *
     * @param manualID The registry name of the manual.
     */
    public static void reset(final ResourceLocation manualID) {
        if (API.manuals != null) {
            Optional.ofNullable(API.manuals.getValue(manualID))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid manual ID: " + manualID.toString()))
                    .reset();
        }
    }

    /**
     * Navigate to a page in the manual.
     *
     * @param manualID The registry name of the manual.
     * @param path     the path to navigate to.
     */
    public static void navigate(final ResourceLocation manualID, final String path) {
        if (API.manuals != null) {
            Optional.ofNullable(API.manuals.getValue(manualID))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid manual ID: " + manualID.toString()))
                    .navigate(path);
        }
    }

    // ----------------------------------------------------------------------- //

    private ManualAPI() {
    }
}
