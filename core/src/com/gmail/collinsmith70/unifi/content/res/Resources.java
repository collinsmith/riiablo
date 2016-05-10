package com.gmail.collinsmith70.unifi.content.res;

import com.google.common.primitives.UnsignedInteger;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.StringBuilder;
import com.gmail.collinsmith70.unifi.content.res.parser.ColorParser;
import com.gmail.collinsmith70.unifi.graphics.ColorUtils;
import com.gmail.collinsmith70.unifi.util.AttributeDecl;
import com.gmail.collinsmith70.unifi.util.AttributeSet;
import com.gmail.collinsmith70.unifi.util.Xml;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    public TypedArray obtainAttributes(@NonNull AttributeSet set, @NonNull AttributeDecl[] attrs) {
        TypedArray array = TypedArray.obtain(this, attrs.length);
        retrieveAttributes(set, attrs, array.data);
        return array;
    }

    public void retrieveAttributes(@NonNull AttributeSet set,
                                   @NonNull AttributeDecl[] attrs,
                                   Object[] data) {
        Class<?> type;
        for (AttributeDecl attr : attrs) {
            type = attr.getValueType();
            if (type == Integer.class) {
                data[attr.getIndex()] = set.getAttributeIntValue(null,
                        attr.getName(), 0);
            } else if (type == UnsignedInteger.class) {
                data[attr.getIndex()] = set.getAttributeUnsignedIntValue(null,
                        attr.getName(), 0xFFFFFFFF);
            }
        }
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

        Gdx.app.debug(TAG, "Parsing resource " + fileHandle.path() + "...");

        try {
            XmlPullParser parser = Xml.newPullParser();
            AttributeSet attrs = Xml.asAttributeSet(parser);
            parser.setInput(fileHandle.reader());

            String tag;
            boolean isRootTag = true;
            ResourceReference.Type resourceType;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                tag = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        resourceType = resourceTypes.get(tag);
                        if (resourceType != null) {
                            switch (resourceType) {
                                case color:
                                    String name = attrs.getAttributeValue(null, "name");
                                    Color color = resourceType.parse(parser, attrs);
                                    colors.put(name, color);
                                    break;
                            }
                        }

                        if (isRootTag) {
                            isRootTag = false;
                        } else if (resourceType == null) {
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

    private static final Trie<String, ResourceReference.Type> resourceTypes;

    static {
        resourceTypes = new PatriciaTrie<ResourceReference.Type>();
        for (ResourceReference.Type resourceType : ResourceReference.Type.values()) {
            resourceTypes.put(resourceType.getTag(), resourceType);
        }
    }

    public static class ResourceReference {

        private boolean isStyleAttributeReference;
        private String mPackage;
        private Type mType;
        private String mName;

        @NonNull
        public static ResourceReference parse(@NonNull String str) throws ParseException {
            return parse(str, new ResourceReference());
        }

        @NonNull
        public static ResourceReference parse(@NonNull String str, @NonNull ResourceReference res)
                throws ParseException {
            try {
                int pos = 0;
                Reader r = new StringReader(str);
                int ch = r.read();
                pos++;
                if (ch != '@' && ch != '?') {
                    throw new ParseException(
                            "Unable to parse resource identifier: " +
                                    "Unexpected char: " + (char)ch,
                            pos);
                }

                res.isStyleAttributeReference = ch == '?';

                String tmp;
                StringBuilder sb = new StringBuilder();

                ch = r.read();
                pos++;
                while (ch != -1 && ch != ':' && ch != '/') {
                    sb.append((char) ch);
                    ch = r.read();
                    pos++;
                }

                switch (ch) {
                    case -1:
                        throw new ParseException(
                                "Unable to parse resource identifier: Unexpected end of string: " +
                                        "Resource identifiers should be formatted like: " +
                                        "@[package:]type/name",
                                pos);
                    case ':':
                        if (sb.length() == 0) {
                            throw new ParseException(
                                    "Unable to parse resource identifier: Invalid format: " +
                                            "':' given without package string preceding: " +
                                            "Resource identifiers should be formatted like: " +
                                            "@[package:]type/name",
                                    pos);
                        }

                        res.mPackage = sb.toString();

                        sb = new StringBuilder();
                        ch = r.read();
                        pos++;
                        while (ch != -1 && ch != '/') {
                            sb.append((char) ch);
                            ch = r.read();
                            pos++;
                        }

                        if (ch == -1) {
                            throw new ParseException(
                                    "Unable to parse resource identifier: Unexpected end of " +
                                            "string: " +
                                            "Resource identifiers should be formatted like: " +
                                            "@[package:]type/name",
                                    pos);
                        }

                    case '/':
                        if (sb.length() == 0) {
                            throw new ParseException(
                                    "Unable to parse resource identifier: Invalid format: " +
                                            "'/' given without type string preceding: " +
                                            "Resource identifiers should be formatted like: " +
                                            "@[package:]type/name",
                                    pos);
                        }

                        tmp = null;
                        try {
                            res.mType = Type.valueOf(tmp = sb.toString());
                        } catch (IllegalArgumentException e) {
                            throw new ParseException(
                                    "Unable to parse resource identifier: Unidentifiable type: "
                                            + tmp,
                                    pos);
                        }

                        break;
                }

                sb = new StringBuilder();
                ch = r.read();
                pos++;
                while (ch != -1) {
                    sb.append((char) ch);
                    ch = r.read();
                    pos++;
                }

                if (sb.length() == 0) {
                    throw new ParseException(
                            "Unable to parse resource identifier: Unexpected end of string: " +
                                    "Resource identifiers should be formatted like: " +
                                    "@[package:]type/name",
                            pos);
                }

                res.mName = sb.toString();
            } catch (IOException e) {
                Gdx.app.error(TAG, e.getMessage(), e);
            }

            return res;
        }

        @Override
        @NonNull
        public String toString() {
            if (mPackage == null) {
                return String.format(Locale.ROOT, "@%s/%s", mType, mName);
            }

            return String.format(Locale.ROOT, "@%s:%s/%s", mPackage, mType, mName);
        }

        public enum Type {
            color() {
                @NonNull
                @Override
                public Color parse(@NonNull XmlPullParser parser, @NonNull AttributeSet attrs)
                        throws IOException, XmlPullParserException {
                    return ColorParser.parse(parser, attrs);
                }
            };

            @Nullable
            private final String tag;

            Type() {
                this.tag = name().toLowerCase(Locale.ROOT);
            }

            Type(@NonNull String tag) {
                Validate.isTrue(tag != null, "tag cannot null");
                Validate.isTrue(!tag.isEmpty(), "tag cannot be empty");
                this.tag = tag;
            }

            @NonNull
            public String getTag() {
                return tag;
            }

            @NonNull
            public abstract <T> T parse(@NonNull XmlPullParser parser, @NonNull AttributeSet attrs)
                    throws IOException, XmlPullParserException;

        }

    }

    private enum ResourceXmlType {

        COLOR() {
            @Override
            protected int parse(@NonNull Resources res, @NonNull XmlPullParser parser)
                    throws IOException, XmlPullParserException {
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

        private Map<String, Object> theme;

        Theme() {
            this.theme = new HashMap<String, Object>();
        }

        @NonNull
        public TypedArray obtainStyledAttributes(@NonNull AttributeDecl[] attrs) {
            Validate.isTrue(attrs != null, "attrs cannot be null");
            final TypedArray array = TypedArray.obtain(Resources.this, attrs.length);
            array.theme = this;
            for (AttributeDecl attr : attrs) {
                Object themeVal = theme.get(attr.getName());
                if (!attr.getValueType().isAssignableFrom(themeVal.getClass())) {
                    throw new RuntimeException(String.format(Locale.ROOT,
                            "theme value at %s is not compatible with attr of type %s",
                            attr.getName(), attr.getValueType().getName()));
                }

                array.data[attr.getIndex()] = themeVal;
            }

            return array;
        }

        @NonNull
        public TypedArray obtainStyledAttributes(@NonNull AttributeSet set,
                                                 @NonNull AttributeDecl[] attrs) {
            Validate.isTrue(attrs != null, "set cannot be null");
            Validate.isTrue(attrs != null, "attrs cannot be null");
            final TypedArray array = TypedArray.obtain(Resources.this, attrs.length);
            retrieveAttributes(set, attrs, array.data);
            return array;
        }

    }

}
