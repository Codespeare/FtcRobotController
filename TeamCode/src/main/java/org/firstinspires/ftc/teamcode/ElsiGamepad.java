package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;

public class ElsiGamepad {

    static public GamepadEx gamepadEx;

    public static void init (Gamepad gamepad) {
        gamepadEx = new GamepadEx(gamepad);
    }

    public static void loop () {
        gamepadEx.readButtons();
    }
}
