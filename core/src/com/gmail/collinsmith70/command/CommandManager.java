package com.gmail.collinsmith70.command;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

public class CommandManager {

private final Trie<String, Command> COMMANDS;

public CommandManager() {
    this.COMMANDS = new PatriciaTrie<Command>();
}

}
