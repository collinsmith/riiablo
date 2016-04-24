package com.gmail.collinsmith70.unifi.content.res;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.gmail.collinsmith70.unifi.graphics.ColorUtils;
import com.gmail.collinsmith70.unifi.util.Xml;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

public class Resources {

    private static final String TAG = Resources.class.getSimpleName();

    private static final FileHandle COLORS_XML = Gdx.files.internal("unifi/colors.xml");

    @NonNull
    private final AssetManager assetManager;

    private Trie<String, Color> colors = new PatriciaTrie<Color>();
    private boolean colorsLoaded;

    public Resources(@NonNull AssetManager assetManager) {
        Validate.isTrue(assetManager != null, "assetManager cannot be null");
        this.assetManager = assetManager;
    }

    @NonNull
    public AssetManager getAssets() {
        return assetManager;
    }

    @Nullable
    public Color getColor(@NonNull String id) {
        if (!StringUtils.startsWithIgnoreCase(id, "@color/")) {
            throw new IllegalArgumentException(
                    "id should be a reference to a color: \"@color/<name>\"");
        }

        String colorName = id.substring(7);
        if (!colorsLoaded) {
            try {
                parseResourceXml(COLORS_XML);
            } catch (FileNotFoundException e) {
                Gdx.app.debug(TAG, "File not found: " + COLORS_XML.path());
            }

            colorsLoaded = true;
        }

        return colors.get(colorName);
    }

    private void parseResourceXml(@NonNull FileHandle fileHandle) throws FileNotFoundException {
        Validate.isTrue(fileHandle != null, "file handle cannot be null");
        if (!fileHandle.exists()) {
            throw new FileNotFoundException("File not found: " + fileHandle.path());
        }

        Gdx.app.debug(TAG, "Parsing resource xml file " + fileHandle.path() + "...");

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fileHandle.reader());

            String tag;
            boolean isRootTag = true;
            ResourceXmlType resourceXmlType;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                tag = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        resourceXmlType = resourceXmlTypes.get(tag);
                        if (resourceXmlType != null) {
                            eventType = resourceXmlType.parse(this, parser);
                            break;
                        }

                        if (isRootTag) {
                            isRootTag = false;
                        } else {
                            Gdx.app.debug(TAG, "Unknown tag found: " + parser.getName());
                        }
                    default:
                        eventType = parser.next();
                        break;
                }
            }
        } catch (XmlPullParserException e) {
            Gdx.app.error(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Gdx.app.error(TAG, e.getMessage(), e);
        }
    }

    private static final Trie<String, ResourceXmlType> resourceXmlTypes;
    static {
        resourceXmlTypes = new PatriciaTrie<Resources.ResourceXmlType>();
        for (ResourceXmlType resourceXmlType : ResourceXmlType.values()) {
            resourceXmlTypes.put(resourceXmlType.getTag(), resourceXmlType);
        }
    }

    private enum ResourceXmlType {

        COLOR() {
            @Override
            protected int parse(@NonNull Resources res, @NonNull XmlPullParser parser)
                    throws XmlPullParserException, IOException {
                String name = parser.getAttributeValue(null, "name");
                if (name == null) {
                    throw new XmlPullParserException(parser.getPositionDescription()
                            + ": Invalid color format within " + COLORS_XML.path());
                }

                boolean done = false;
                int eventType = parser.next();
                while (eventType != XmlPullParser.END_DOCUMENT
                        && eventType != XmlPullParser.END_TAG) {
                    if (done) {
                        eventType = parser.next();
                        continue;
                    }

                    switch (eventType) {
                        case XmlPullParser.TEXT:
                            String text = parser.getText();
                            Color color = new Color();
                            Color.argb8888ToColor(color, ColorUtils.parseColor(text));
                            res.colors.put(name, color);
                            done = true;
                            break;
                        default:
                            break;
                    }

                    eventType = parser.next();
                }

                return eventType;
            }
        };

        @Nullable
        private final String tag;

        ResourceXmlType() {
            this.tag = null;
        }

        ResourceXmlType(@NonNull String tag) {
            Validate.isTrue(tag != null, "tag cannot null");
            Validate.isTrue(!tag.isEmpty(), "tag cannot be empty");
            this.tag = tag;
        }

        @NonNull
        public String getTag() {
            if (tag == null) {
                return this.name().toLowerCase(Locale.ROOT);
            }

            return tag;
        }

        protected int parse(@NonNull Resources res, @NonNull XmlPullParser parser)
                throws XmlPullParserException, IOException {
            throw new AbstractMethodError();
        }

    }

    public final class Theme {

        public TypedArray obtainStyledAttributes(int[] attrs) {
            return null;
        }

    }

}
