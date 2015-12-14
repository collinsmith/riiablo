package com.google.collinsmith70.diablo;

import java.util.Set;

public interface CommandProcessor extends BufferListener {

boolean process(String command);
Set<String> getSuggestions(String command, int position);

}
