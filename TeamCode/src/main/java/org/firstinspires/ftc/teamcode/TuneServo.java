package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

@TeleOp(name = "TuneServo", group = "Config")
public class TuneServo extends OpMode {
    private static ServoEx trigger;

    private static double angle = 0;

    @Override
    public void init() {
        ElsiGamepad.init(gamepad1);
        ElsiTelemetry.init(telemetry);

        trigger = new ServoEx(hardwareMap, "trigger",0,300);

        trigger.set(angle);
    }

    @Override
    public void loop() {
        ElsiGamepad.loop();

        if(ElsiGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.DPAD_UP)) {
            angle++;
            trigger.set(angle);
        } else if (ElsiGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.DPAD_DOWN)) {
            angle--;
            trigger.set(angle);
        } else if (ElsiGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.DPAD_RIGHT)) {
            angle += 10;
            trigger.set(angle);
        } else if (ElsiGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.DPAD_LEFT)) {
            angle -= 10;
            trigger.set(angle);
        }


        ElsiTelemetry.telemetry.addData("Angle", angle);

        ElsiTelemetry.loop();
    }
}
