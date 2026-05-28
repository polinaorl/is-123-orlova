package com.workout.app.patterns.factory;

import com.workout.app.view.console.*;

public class ConsoleGUIFactory {
    public void run() {
        ConsoleLoginView login = new ConsoleLoginView();
        login.show();
    }
}