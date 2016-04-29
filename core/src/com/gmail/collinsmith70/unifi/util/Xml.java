package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Xml {

    private static final String TAG = Xml.class.getSimpleName();

    private Xml() {
    }

    @NonNull
    public static XmlPullParser newPullParser() {
        try {
            KXmlParser parser = new KXmlParser();
            //parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, true);
            //parser.setFeature(XmlPullParser.FEATURE_VALIDATION, true);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            return parser;
        } catch (XmlPullParserException e) {
            Gdx.app.error(TAG, e.getMessage(), e);
            throw new AssertionError("Failed to create XmlPullParser instance!");
        }
    }

    @NonNull
    public static AttributeSet asAttributeSet(XmlPullParser parser) {
        return (parser instanceof AttributeSet)
                ? (AttributeSet) parser
                : new XmlPullAttributes(parser);
    }

}
