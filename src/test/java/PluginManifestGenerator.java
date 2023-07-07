/*
LandSAR Motion Model Software Development Kit
Copyright (c) 2023 Raytheon Technologies 

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
https://github.com/atapas/add-copyright.git
*/



import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import org.junit.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.roger.annotation.Plugin;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * This can be run as a Junit Test, and generates a plugin manifest for plugins 
 */
@SuppressWarnings("PMD.CollapsibleIfStatements")
public class PluginManifestGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManifestGenerator.class);
    private static final String PLUGIN_MANIFEST_FILENAME = "/plugin-manifest.json";
    private static final String PLUGIN_MANIFEST_FOLDER = "src/main/resources";
    
    //TODO when writing your own motion model plugins, replace this with their package name
    private static final String YOUR_PACKAGE_NAME = "com.bbn.landsar.motionmodel";

    @Test
    public void generateManifest() {
        //printClassPath();
        //printResources();
        //System.setProperty("user.dir", currentDirectory);
        LOGGER.info("User directory: " + System.getProperty("user.dir"));
        LOGGER.info("Starting Plugin Manifest Generator...");
        // ** Access the index of classes annotated with the specified annotation at runtime.
        // Uses the Class Index library.
        File manifestFolder = new File(PLUGIN_MANIFEST_FOLDER);
        if (!manifestFolder.exists()) {
            if (manifestFolder.mkdirs()) {
                LOGGER.info("Created directory structure for plugin manifest");
            }
        }
//      File manifest = new File(currentDirectory + "/" + PLUGIN_MANIFEST_FOLDER + PLUGIN_MANIFEST_FILENAME);
        File manifest = new File(PLUGIN_MANIFEST_FOLDER + PLUGIN_MANIFEST_FILENAME);
        if (!manifest.exists()) {
            LOGGER.info("Manifest does not exist, so creating a new one");
            try {
                if (!manifest.createNewFile()) {
                    LOGGER.error("Could not create manifest file at " + PLUGIN_MANIFEST_FILENAME);
                }
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        } else {
            LOGGER.info("Updating manifest file...");
        }
        JsonFactory factory = new JsonFactory();
        try (JsonGenerator generator = factory.createGenerator(manifest, JsonEncoding.UTF8)) {
            generator.useDefaultPrettyPrinter(); // Enable pretty printing
            generator.writeStartObject();
            generator.writeStringField("pluginContainerName", "LandSAR Motion Model Example Plugins");
            generator.writeStringField("description", "A collection of plugins to support Search and Rescue / Personnel Recovery.");
            generator.writeArrayFieldStart("plugins");
            Reflections reflections = new Reflections(YOUR_PACKAGE_NAME);
            for (Class pluginClass : reflections.getTypesAnnotatedWith(Plugin.class)) {
                LOGGER.info("Plugin class: {}", pluginClass.getName());
//            for (Class pluginClass : ClassIndex.getAnnotated(com.bbn.roger.annotation.Plugin.class, ClassLoader.getSystemClassLoader())) {
                generator.writeStartObject();
                Plugin annotation = requireNonNull(
                        (Plugin) pluginClass.getAnnotation(Plugin.class),
                        "Plugin annotation should not be null");
                generator.writeStringField("className", pluginClass.getName());
                generator.writeStringField("author", annotation.author());
                generator.writeStringField("description", annotation.description());
                generator.writeStringField("version", annotation.version());
                generator.writeArrayFieldStart("dependencies");
                for (String dependency : annotation.dependencies()) {
                    generator.writeString(dependency);
                }
                generator.writeEndArray();
                generator.writeEndObject();
            }
            generator.writeEndArray();
            generator.writeEndObject();
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        LOGGER.info("Plugin Manifest Generator has finished.");
    }

    private void printClassPath() {
        //https://www.mkyong.com/java/how-to-print-out-the-current-project-classpath/
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)cl).getURLs();
        for(URL url: urls){
            LOGGER.info("CLASSPATH ENTRY: {}", url.getFile());
        }
    }

    private void printResources() {
        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            String name = "META-INF/annotations/" + Plugin.class.getCanonicalName();
            Enumeration<URL> resources = cl.getResources(name);
            if (!resources.hasMoreElements()) {
                LOGGER.warn("Did not find resource with name {}", name);
            }
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                LOGGER.info("RESOURCE: {}", resource.getFile());
            }
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

//    public static void main(String [] args) {
//        PluginManifestGenerator generator = new PluginManifestGenerator();
//        generator.generateManifest(args[0]);
//    }
}
