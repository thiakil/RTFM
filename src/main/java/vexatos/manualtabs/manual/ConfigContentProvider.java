package vexatos.manualtabs.manual;

import com.google.common.base.Charsets;
import li.cil.manual.api.API;
import li.cil.manual.api.manual.ContentProvider;
import li.cil.manual.common.RTFM;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author Vexatos
 */
public class ConfigContentProvider implements ContentProvider {
    private final File directory;

    public ConfigContentProvider() {
        directory = new File(Loader.instance().getConfigDir() + File.separator + API.MOD_ID);
        if (!directory.exists()) {
            directory.mkdir();
            File defLangDir = new File(directory, "en_us");
            if (!defLangDir.exists()) {
                defLangDir.mkdir();
                File defIndexFile = new File(defLangDir, "index.md");
                if (!defIndexFile.exists()) {
                    InputStream indexStream = RTFM.class.getResourceAsStream("/assets/rtfm/doc/default/index.md");
                    if (indexStream != null) {
                        try {
                            FileUtils.copyInputStreamToFile(indexStream, defIndexFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public Iterable<String> getContent(String path) {
        path = path.startsWith("/") ? path.substring(1) : path;
        InputStream stream = null;
        try {
            File file = new File(directory, (path.startsWith("/") ? path.substring(1) : path));
            if (file.exists()) {
                stream = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
                ArrayList<String> lines = new ArrayList<String>();
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                return lines;
            }
        } catch (Throwable t) {
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }
}
