package com.gmail.collinsmith70.unifi.content.res.parser;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;
import com.gmail.collinsmith70.unifi.graphics.ColorUtils;
import com.gmail.collinsmith70.unifi.util.AttributeSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class ColorParser {

    public static Color parse(@NonNull XmlPullParser parser, @NonNull AttributeSet attrs)
            throws IOException, XmlPullParserException {
        String name = attrs.getAttributeValue(null, "name");
        if (name == null) {
            throw new XmlPullParserException(parser.getPositionDescription()
                    + ": Color elements must have a \"name\" attribute");
        }

        String text = parser.nextText();
        Color color = new Color();
        Color.argb8888ToColor(color, ColorUtils.parseColor(text));

        int eventType = parser.next();
        while (eventType != XmlPullParser.END_DOCUMENT
                && eventType != XmlPullParser.END_TAG) {
            eventType = parser.next();
        }

        return color;
    }

}
