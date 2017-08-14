package li.cil.manual.common.api;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import li.cil.manual.api.detail.Manual;
import li.cil.manual.api.manual.ContentProvider;
import li.cil.manual.api.manual.ImageProvider;
import li.cil.manual.api.manual.ImageRenderer;
import li.cil.manual.api.manual.PathProvider;
import li.cil.manual.api.manual.TabIconRenderer;
import li.cil.manual.client.gui.GuiHandlerClient;
import li.cil.manual.client.gui.GuiManual;
import li.cil.manual.common.RTFM;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Pattern;

public final class ManualImpl extends Manual {
    // Language placeholder replacement.
    private static final String LANGUAGE_KEY = "%LANGUAGE%";
    private static final String FALLBACK_LANGUAGE = "en_us";
    private static final Pattern PATTERN_LANGUAGE_KEY = Pattern.compile(LANGUAGE_KEY);

    // Error messages.
    private static final String MESSAGE_CONTENT_LOOKUP_EXCEPTION = "A content provider threw an error when queried.";
    private static final String MESSAGE_IMAGE_PROVIDER_EXCEPTION = "An image provider threw an error when queried.";
    private static final String MESSAGE_PATH_PROVIDER_ITEM_EXCEPTION = "A path provider threw an error when queried with an item.";
    private static final String MESSAGE_PATH_PROVIDER_BLOCK_EXCEPTION = "A path provider threw an error when queried with a block.";

    // --------------------------------------------------------------------- //

    public int getHistorySize() {
        return this.history.size();
    }

    public void pushPath(final String path) {
        this.history.push(new ManualImpl.History(path));
    }

    public String peekPath() {
        return this.history.peek().path;
    }

    public int peekOffset() {
        return this.history.peek().offset;
    }

    public void setOffset(final int offset) {
        this.history.peek().offset = offset;
    }

    public void popPath() {
        this.history.pop();
    }

    public List<Tab> getTabs() {
        return this.tabs;
    }

    // --------------------------------------------------------------------- //

    /**
     * The history of pages the player navigated through (like browser history).
     */
    private final Stack<History> history = new Stack<>();

    /**
     * The registered tabs of the manual, which are really just glorified links.
     */
    private final List<Tab> tabs = new ArrayList<>();

    /**
     * The list of registered path providers, used for resolving items/blocks to paths.
     */
    private final List<PathProvider> pathProviders = new ArrayList<>();

    /**
     * The list of registered content providers, used for resolving paths to page content.
     */
    private final List<ContentProvider> contentProviders = new ArrayList<>();

    /**
     * The list of registered image providers, used for drawing images.
     */
    private final List<PrefixedImageProvider> imageProviders = new ArrayList<>();

    /**
     * The default page to open when resetting / the history becomes empty.
     */
    private String defaultPage = String.format("%s/index.md", LANGUAGE_KEY);

    // --------------------------------------------------------------------- //

    private ManualImpl() {
        reset();
    }

    // --------------------------------------------------------------------- //

    @Override
    public void addTab(final TabIconRenderer renderer, @Nullable final String tooltip, final String path) {
        tabs.add(new Tab(renderer, tooltip, path));
        if (tabs.size() > 7) {
            RTFM.getLog().warn("Gosh I'm popular! Too many tabs were added to the in-game manual, so some won't be shown. In case this actually happens, let me know and I'll look into making them scrollable or something...");
        }
    }

    @Override
    public void addProvider(final PathProvider provider) {
        if (!pathProviders.contains(provider)) {
            pathProviders.add(provider);
        }
    }

    @Override
    public void addProvider(final ContentProvider provider) {
        if (!contentProviders.contains(provider)) {
            contentProviders.add(provider);
        }
    }

    @Override
    public void addProvider(final String prefix, final ImageProvider provider) {
        final String actualPrefix = (Strings.isNullOrEmpty(prefix)) ? "" : prefix + ":";
        for (final PrefixedImageProvider entry : imageProviders) {
            if (entry.prefix.equals(actualPrefix) && entry.provider == provider) {
                return;
            }
        }
        imageProviders.add(new PrefixedImageProvider(actualPrefix, provider));
    }

    @Override
    @Nullable
    public String pathFor(final ItemStack stack) {
        return pathFor(p -> p.pathFor(stack), MESSAGE_PATH_PROVIDER_ITEM_EXCEPTION);
    }

    @Override
    @Nullable
    public String pathFor(final World world, final BlockPos pos) {
        return pathFor(p -> p.pathFor(world, pos), MESSAGE_PATH_PROVIDER_BLOCK_EXCEPTION);
    }

    @Override
    @Nullable
    public Iterable<String> contentFor(final String path) {
        final String cleanPath = Files.simplifyPath(path);
        final String language = FMLCommonHandler.instance().getCurrentLanguage();
        final Optional<Iterable<String>> result = contentForWithRedirects(PATTERN_LANGUAGE_KEY.matcher(cleanPath).replaceAll(language));
        if (result.isPresent()) {
            return result.get();
        }
        return contentForWithRedirects(PATTERN_LANGUAGE_KEY.matcher(cleanPath).replaceAll(FALLBACK_LANGUAGE)).orElse(null);
    }

    @Override
    @Nullable
    public ImageRenderer imageFor(final String href) {
        for (int i = imageProviders.size() - 1; i >= 0; i--) {
            final PrefixedImageProvider entry = imageProviders.get(i);
            if (href.startsWith(entry.prefix)) {
                try {
                    final ImageRenderer image = entry.provider.getImage(href.substring(entry.prefix.length()));
                    if (image != null) {
                        return image;
                    }
                } catch (final Throwable t) {
                    RTFM.getLog().warn(MESSAGE_IMAGE_PROVIDER_EXCEPTION, t);
                }
            }
        }

        return null;
    }

    @Override
    public void openFor(final EntityPlayer player) {
        if (player.getEntityWorld().isRemote) {
            player.openGui(RTFM.instance, GuiHandlerClient.GuiId.BOOK_MANUAL.ordinal(), player.getEntityWorld(), 0, 0, 0);
        }
    }

    @Override
    public void reset() {
        history.clear();
        history.push(new History(defaultPage));
    }

    @Override
    public void navigate(final String path) {
        final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiManual) {
            ((GuiManual) screen).pushPage(path);
        } else {
            history.push(new History(path));
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Makes the specified path relative to the specified base path.
     *
     * @param path the path to make relative.
     * @param base the path to make it relative to.
     * @return the relative path.
     */
    public static String makeRelative(final String path, final String base) {
        if (path.startsWith("/")) {
            return path;
        } else {
            final int lastSlash = base.lastIndexOf('/');
            if (lastSlash >= 0) {
                return base.substring(0, lastSlash + 1) + path;
            } else {
                return path;
            }
        }
    }

    public void setDefaultPage(final String defaultPage) {
        this.defaultPage = defaultPage;
        this.reset();
    }

    // --------------------------------------------------------------------- //

    @Nullable
    private String pathFor(final ProviderQuery query, final String warning) {
        for (final PathProvider provider : pathProviders) {
            try {
                final String path = query.pathFor(provider);
                if (path != null) {
                    return path;
                }
            } catch (final Throwable t) {
                RTFM.getLog().warn(warning, t);
            }
        }
        return null;
    }

    private Optional<Iterable<String>> contentForWithRedirects(final String path) {
        return contentForWithRedirects(path, new ArrayList<>());
    }

    private Optional<Iterable<String>> contentForWithRedirects(final String path, final List<String> seen) {
        if (seen.contains(path)) {
            final List<String> message = new ArrayList<>();
            message.add("Redirection loop: ");
            message.addAll(seen);
            message.add(path);
            return Optional.of(message);
        }
        final Optional<Iterable<String>> content = doContentLookup(path);

        if (content.isPresent()) {
            final Iterable<String> lines = content.get();
            final Iterator<String> iterator = lines.iterator();
            if (iterator.hasNext()) {
                final String line = iterator.next();
                if (line.toLowerCase(Locale.US).startsWith("#redirect ")) {
                    final List<String> newSeen = new ArrayList<>(seen);
                    newSeen.add(path);
                    return contentForWithRedirects(makeRelative(line.substring("#redirect ".length()), path), newSeen);
                }
            }
        }
        return content; // Empty.
    }

    private Optional<Iterable<String>> doContentLookup(final String path) {
        for (final ContentProvider provider : contentProviders) {
            try {
                final Iterable<String> lines = provider.getContent(path);
                if (lines != null) {
                    return Optional.of(lines);
                }
            } catch (final Throwable t) {
                RTFM.getLog().warn(MESSAGE_CONTENT_LOOKUP_EXCEPTION, t);
            }
        }
        return Optional.empty();
    }

    // --------------------------------------------------------------------- //

    public static final class History {
        public final String path;
        public int offset = 0;

        public History(final String path) {
            this.path = path;
        }
    }

    public static final class Tab {
        public final TabIconRenderer renderer;
        public final String tooltip;
        public final String path;

        public Tab(final TabIconRenderer renderer, @Nullable final String tooltip, final String path) {
            this.renderer = renderer;
            this.tooltip = tooltip;
            this.path = path;
        }
    }

    private static final class PrefixedImageProvider {
        public final String prefix;
        public final ImageProvider provider;

        private PrefixedImageProvider(final String prefix, final ImageProvider provider) {
            this.prefix = prefix;
            this.provider = provider;
        }
    }

    @FunctionalInterface
    private interface ProviderQuery {
        @Nullable
        String pathFor(PathProvider provider);
    }
}
