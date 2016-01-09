package com.gmail.collinsmith70.cvar;

public interface CvarManagerListener {

void onCommit();
void onSave(Cvar cvar);
void onLoad(Cvar cvar);



}
