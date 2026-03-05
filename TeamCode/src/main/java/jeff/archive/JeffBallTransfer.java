package jeff.archive;


import static jeff.archive.JeffTelemetry.telemetry;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;
import com.seattlesolvers.solverslib.hardware.motors.CRServo;
import com.seattlesolvers.solverslib.hardware.motors.CRServoEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

/// Transfer System from Spindexer to Launcher
public class JeffBallTransfer {
    private static ServoEx lowFeeder;
    private static CRServo highFeeder1;
    private static CRServo highFeeder2;
    private static double lowLaunchAngle = 40;
    private static double lowHoldAngle = 0;

    private static boolean launch = false;

    private static boolean isLaunching = false;

    public static void init (HardwareMap hardwareMap) {
        lowFeeder = new ServoEx(hardwareMap, "lowFeeder", 0,360);
        highFeeder1 = new CRServoEx(hardwareMap,"highFeeder1");
        highFeeder2 = new CRServoEx(hardwareMap,"highFeeder2");
        highFeeder1.setInverted(true);
        highFeeder2.setInverted(true);
        lowFeeder.set(lowHoldAngle);
    }

    public static void loop () {
        telemetry.addData("Low Servo Angle:",lowFeeder.get());


        if(JeffGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.RIGHT_BUMPER) && !isLaunching) {
            launch = true;
        }

        if(JeffGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.LEFT_BUMPER)) {
            launch = false;
        }



        if(launch && !isLaunching) {
            isLaunching = true;
            lowFeeder.set(lowLaunchAngle);
            highFeeder1.set(1);
            highFeeder2.set(1);
        } else if (!launch) {
            isLaunching = false;
            lowFeeder.set(lowHoldAngle);
            highFeeder1.set(0);
            highFeeder2.set(0);
        }

        if(lowFeeder.get() == lowLaunchAngle) {
            isLaunching = false;
        }

    }
}
