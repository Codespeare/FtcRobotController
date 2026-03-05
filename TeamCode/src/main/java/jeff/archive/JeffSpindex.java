package jeff.archive;

import static jeff.archive.JeffTelemetry.telemetry;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

/// Spindexer Control
public class JeffSpindex {
    private static ServoEx spin;

    private static double target = 0;
    private static double lastTarget = 0;

    private static double zeroOffset = 0;

    public static void init(HardwareMap hardwareMap){
        spin = new ServoEx(hardwareMap,"spindexer",0,1800);


        spin.set(0 + zeroOffset);
    }

    public static void loop () {
        telemetry.addLine("Use Left and Right to move the Spindexer");

        if(JeffGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.DPAD_LEFT)) {
            target -= 30;
        } else if (JeffGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.DPAD_RIGHT)) {
            target += 30;
        }

        if(JeffGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.DPAD_UP)) {
            zeroOffset += 1;
        } else if (JeffGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.DPAD_DOWN)) {
            zeroOffset -= 1;
        }

        target = Math.min(1800 - zeroOffset, target);
        target = Math.max(0 + zeroOffset,target);

        if(target != lastTarget) {
            lastTarget = target;

            spin.set(target);

        }

        telemetry.addData("Spindexer z-Offset",zeroOffset);
        telemetry.addData("Spindexer target", target);
        telemetry.addData("Spindexer offset-target", target - zeroOffset);
        telemetry.addData("Spindexer pos", spin.get());
    }


}
