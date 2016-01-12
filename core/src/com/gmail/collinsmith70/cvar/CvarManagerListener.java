package com.gmail.collinsmith70.cvar;

public interface CvarManagerListener {

void onCommit();
<T> void onSave(Cvar<T> cvar);
<T> void onLoad(Cvar<T> cvar);

<T> void onCvarChanged(Cvar<T> cvar, T from, T to);
<T> void onDefaultValueInvalidated(Cvar<T> cvar);

}
