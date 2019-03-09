package com.riiablo.codec;

import com.badlogic.gdx.Gdx;
import com.riiablo.mpq.MPQFileHandleResolver;

public class StringTBLs {
  private static final String TAG = "StringTBLs";
  private static final boolean DEBUG = false;

  public final StringTBL string, expansionstring, patchstring;

  public StringTBLs(MPQFileHandleResolver resolver) {
    string          = StringTBL.loadFromFile(resolver.resolve("data\\local\\lng\\eng\\string.tbl"));
    expansionstring = StringTBL.loadFromFile(resolver.resolve("data\\local\\lng\\eng\\expansionstring.tbl"));
    patchstring     = StringTBL.loadFromFile(resolver.resolve("data\\local\\lng\\eng\\patchstring.tbl"));

    int duplicates = 0;
    duplicates += patch(StringTBL.CLASSIC_OFFSET, string, StringTBL.EXPANSION_OFFSET, expansionstring);
    duplicates += patch(StringTBL.CLASSIC_OFFSET, string, StringTBL.PATCH_OFFSET, patchstring);
    duplicates += patch(StringTBL.EXPANSION_OFFSET, expansionstring, StringTBL.PATCH_OFFSET, patchstring);
    if (DEBUG) Gdx.app.debug(TAG, "Duplicates Found: " + duplicates);
  }

  private int patch(short offset1, StringTBL strings, short offset, StringTBL patch) {
    int duplicates = 0;
    for (int i = 0; i < patch.indexes.length; i++) {
      int index = patch.indexes[i];
      StringTBL.HashTable.Entry patchEntry = patch.hashTable.entries[index];
      String key = new String(patch.text, patchEntry.keyOffset - patch.header.startIndex, patchEntry.strOffset - patchEntry.keyOffset - 1);
      int stringHash = strings.lookupHash(key);
      if (stringHash == -1) {
        continue;
      }

      duplicates++;
      StringTBL.HashTable.Entry stringEntry = strings.hashTable.entries[stringHash];
      if (DEBUG) Gdx.app.debug(TAG, "duplicate " + (offset1 + stringEntry.index) + "(" + stringEntry.ptr + ")->" + (offset + patchEntry.index) + ":" + key);
      stringEntry.ptr = (short) (offset + patchEntry.index);
    }

    return duplicates;
  }

  public String lookup(int index) {
    if (index >= StringTBL.EXPANSION_OFFSET) {
      index -= StringTBL.EXPANSION_OFFSET;
      int ptr = expansionstring.lookupPtr(index);
      if (ptr == -1) {
        return expansionstring.lookup(index);
      }

      return lookup(ptr);
    } else if (index >= StringTBL.PATCH_OFFSET) {
      index -= StringTBL.PATCH_OFFSET;
      return patchstring.lookup(index);
    } else {
      int ptr = string.lookupPtr(index);
      if (ptr == -1) {
        return string.lookup(index);
      }

      return lookup(ptr);
    }
  }

  public String format(int index, Object... args) {
    return String.format(lookup(index), args);
  }

  public String lookup(String key) {
    String str;
    if ((str = patchstring.lookup(key)) != null) return str;
    if ((str = expansionstring.lookup(key)) != null) return str;
    if ((str = string.lookup(key)) != null) return str;
    return "ERROR: " + key;
  }
}
