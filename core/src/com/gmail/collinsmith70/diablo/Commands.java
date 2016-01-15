package com.gmail.collinsmith70.diablo;

import com.gmail.collinsmith70.command.Action;
import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.command.CommandInstance;

public class Commands {

public static final Command Command = new Command("command", new Action() {
    @Override
    public void onActionExecuted(CommandInstance command, Object obj) {

    }
});

}
