package jeff.archive;

import static jeff.archive.JeffTelemetry.telemetry;
import static jeff.archive.RobotVariables.TargetVelocity;
import static jeff.archive.RobotVariables.bottomPIDF;
import static jeff.archive.RobotVariables.topPIDF;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

public class JeffLauncher {

     static private DcMotorEx top_launcher_motor;
     static private DcMotorEx bottom_launcher_motor;

     static private JeffFlywheelController top_flywheel_controller;
    static private JeffFlywheelController bottom_flywheel_controller;

    static private double topPower;
    static private double bottomPower;

    static private boolean launcherEnabled = false;


    static public void init (HardwareMap hardwareMap) {

        top_launcher_motor = new MotorEx(hardwareMap, "topLauncher", Motor.GoBILDA.BARE).motorEx;
        bottom_launcher_motor = new MotorEx(hardwareMap, "bottomLauncher", Motor.GoBILDA.BARE).motorEx;

        top_flywheel_controller = new JeffFlywheelController(top_launcher_motor);
        bottom_flywheel_controller = new JeffFlywheelController(bottom_launcher_motor);

        top_flywheel_controller.setGains(topPIDF.P(), topPIDF.I(), topPIDF.D(), topPIDF.F());
        bottom_flywheel_controller.setGains(bottomPIDF.P(), bottomPIDF.I(), bottomPIDF.D(), bottomPIDF.F());
    }

    static public void loop () {
        if(JeffGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.A)) {
            launcherEnabled = !launcherEnabled;
        }

        //if(JeffGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.X)) {
            top_flywheel_controller.setGains(topPIDF.P(), topPIDF.I(), topPIDF.D(), topPIDF.F());
            top_flywheel_controller.setGains(bottomPIDF.P(), bottomPIDF.I(), bottomPIDF.D(), bottomPIDF.F());
        //}

        if(launcherEnabled) {
            topPower = top_flywheel_controller.update();
            bottomPower = bottom_flywheel_controller.update();
        } else {
            top_launcher_motor.setPower(0);
            bottom_launcher_motor.setPower(0);
        }

        telemetry();
    }

    static public void telemetry () {
        if(launcherEnabled) {
            telemetry.addData("Shooter", "ON");
            telemetry.addData("Target RPM", TargetVelocity);
            telemetry.addData("Top Power", topPower);
            telemetry.addData("Bottom Power", bottomPower);
            telemetry.addData("TOP RPM", top_flywheel_controller.getRawRpm());
            telemetry.addData("TOP Velocity", top_launcher_motor.getVelocity());
            telemetry.addData("TOP RPM filt", top_flywheel_controller.getFilteredRpm());
            telemetry.addData("BOTTOM RPM", bottom_flywheel_controller.getRawRpm());
            telemetry.addData("BOTTOM Velocity", bottom_launcher_motor.getVelocity());
            telemetry.addData("BOTTOM RPM filt", bottom_flywheel_controller.getFilteredRpm());
            telemetry.addData("Top Ready", top_flywheel_controller.isReady(System.currentTimeMillis()));
            telemetry.addData("Bottom Ready", bottom_flywheel_controller.isReady(System.currentTimeMillis()));
        }
        else {
            telemetry.addData("Shooter", "OFF");
            telemetry.addData("Target RPM", TargetVelocity);
            telemetry.addData("TOP RPM", top_flywheel_controller.getRawRpm());
            telemetry.addData("BOTTOM RPM", bottom_flywheel_controller.getRawRpm());
        }
    }
}
