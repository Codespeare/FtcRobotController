package jeff.archive;



//import com.arcrobotics.ftclib.gamepad.GamepadEx;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;

public class JeffGamepad {
    static public GamepadEx gamepadEx;

    public static void init (Gamepad gamepad) {
        gamepadEx = new GamepadEx(gamepad);
    }

    public static void loop () {
        gamepadEx.readButtons();
    }
}
