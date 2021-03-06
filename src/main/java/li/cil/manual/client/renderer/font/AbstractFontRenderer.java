package li.cil.manual.client.renderer.font;

import gnu.trove.map.TCharIntMap;
import gnu.trove.map.hash.TCharIntHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Base implementation for texture based font rendering.
 */
public abstract class AbstractFontRenderer implements FontRenderer {
    private final TCharIntMap CHAR_MAP;

    private final int COLUMNS = getResolution() / (getCharWidth() + getGapU());
    private final float U_SIZE = getCharWidth() / (float) getResolution();
    private final float V_SIZE = getCharHeight() / (float) getResolution();
    private final float U_STEP = (getCharWidth() + getGapU()) / (float) getResolution();
    private final float V_STEP = (getCharHeight() + getGapV()) / (float) getResolution();

    protected AbstractFontRenderer() {
        CHAR_MAP = new TCharIntHashMap();
        final CharSequence chars = getCharacters();
        for (int index = 0; index < chars.length(); index++) {
            CHAR_MAP.put(chars.charAt(index), index);
        }
    }

    // --------------------------------------------------------------------- //

    public void drawString(final CharSequence value) {
        drawString(value, value.length());
    }

    public void drawString(final CharSequence value, final int maxChars) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);

        Minecraft.getMinecraft().getTextureManager().bindTexture(getTextureLocation());

        final Tessellator tessellator = Tessellator.getInstance();
        final VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float tx = 0f;
        final int end = Math.min(maxChars, value.length());
        for (int i = 0; i < end; i++) {
            final char ch = value.charAt(i);
            drawChar(tx, ch, buffer);
            tx += getCharWidth() + getGapU();
        }

        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    // --------------------------------------------------------------------- //

    abstract protected CharSequence getCharacters();

    abstract protected ResourceLocation getTextureLocation();

    abstract protected int getResolution();

    abstract protected int getGapU();

    abstract protected int getGapV();

    // --------------------------------------------------------------------- //

    private void drawChar(final float x, final char ch, final VertexBuffer buffer) {
        if (Character.isWhitespace(ch) || Character.isISOControl(ch)) {
            return;
        }
        final int index = getCharIndex(ch);

        final int column = index % COLUMNS;
        final int row = index / COLUMNS;
        final float u = column * U_STEP;
        final float v = row * V_STEP;

        buffer.pos(x, getCharHeight(), 0).tex(u, v + V_SIZE).endVertex();
        buffer.pos(x + getCharWidth(), getCharHeight(), 0).tex(u + U_SIZE, v + V_SIZE).endVertex();
        buffer.pos(x + getCharWidth(), 0, 0).tex(u + U_SIZE, v).endVertex();
        buffer.pos(x, 0, 0).tex(u, v).endVertex();
    }

    private int getCharIndex(final char ch) {
        if (!CHAR_MAP.containsKey(ch)) {
            return CHAR_MAP.get('?');
        }
        return CHAR_MAP.get(ch);
    }
}
