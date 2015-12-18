package com.google.collinsmith70.diablo;

import com.google.collinsmith70.diablo.command.Command;

import java.util.Collection;

public interface CommandProcessor extends BufferListener {

boolean process(String command);
Collection<Command> getSuggestions(String command, int position);

}
