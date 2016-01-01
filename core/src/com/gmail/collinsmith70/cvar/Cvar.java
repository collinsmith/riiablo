package com.gmail.collinsmith70.cvar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.HashMap;
import java.util.Map;

public class Cvar<T> {

private static final String TAG = Cvar.class.getSimpleName();
private static final Preferences PREFERENCES = Gdx.app.getPreferences(Cvar.class.getName());
private static final Trie<String, Cvar<?>> CVARS = new PatriciaTrie<Cvar<?>>();

private static final Map<Class<?>, Serializer<?, String>> SERIALIZERS
        = new HashMap<Class<?>, Serializer<?, String>>();

private static <T> Serializer<T, String> getSerializer(Class<T> type) {
    return (Serializer<T, String>)SERIALIZERS.get(type);
}

public Cvar(String key, Class<T> type, T defaultValue) {
    this(key, type, defaultValue, getSerializer(type));
}

public Cvar(String key, Class<T> type, T defaultValue, Serializer<T, String> serializer) {
    //this(key, type, defaultValue, serializer, null);
}

}
