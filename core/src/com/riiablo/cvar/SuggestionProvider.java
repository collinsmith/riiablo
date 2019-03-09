package com.riiablo.cvar;

import android.support.annotation.NonNull;

import java.util.Collection;

public interface SuggestionProvider {
  Collection<String> suggest(@NonNull String str);
}
