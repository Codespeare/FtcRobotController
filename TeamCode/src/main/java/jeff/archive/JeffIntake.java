package jeff.archive;

import static jeff.archive.JeffTelemetry.telemetry;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

public class JeffIntake {
    private static boolean intakeOn = false;
    private static DcMotor intakeMotor;

    public static void init(HardwareMap hardwareMap){
        intakeMotor = hardwareMap.get(DcMotor.class, "intake");
        intakeMotor.setDirection(DcMotor.Direction.REVERSE);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public static void loop () {
        telemetry.addLine("Press B to toggle the intake");

        if(JeffGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.B)) {
         intakeOn = !intakeOn;
        }

        if(intakeOn) {
            intakeMotor.setPower(0.8);
        } else {
            intakeMotor.setPower(0.0);
        }
    }
}
