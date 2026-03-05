package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.ElsiTelemetry.panelsTelemetry;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.configurables.annotations.Sorter;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

@Configurable
public class ElsiLauncher {

    @Sorter(sort = 0)
    static public double targetVelocity = 800;

    static private MotorEx top_launcher_motor,bottom_launcher_motor;

    static public ElsiFlywheelController top_flywheel_controller = new ElsiFlywheelController(), bottom_flywheel_controller = new ElsiFlywheelController();

    static public double topPower, bottomPower;

    static public boolean launcherEnabled = false;
    static public boolean autoMode = false;
    static public boolean autoLauncherEnabled = false;


    static public void init (HardwareMap hardwareMap, boolean isAuto) {
        top_launcher_motor = new MotorEx(hardwareMap, "upperFlywheel", Motor.GoBILDA.BARE);
        bottom_launcher_motor = new MotorEx(hardwareMap, "lowerFlywheel", Motor.GoBILDA.BARE);

        top_flywheel_controller = new ElsiFlywheelController(top_launcher_motor);
        bottom_flywheel_controller = new ElsiFlywheelController(bottom_launcher_motor);

        autoMode = isAuto;
    }

    static public void loop () {
        launcherEnabled = ElsiGamepad.gamepadEx.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) == 1 || (autoMode && autoLauncherEnabled);

        if(!autoMode) targetVelocity = ElsiTeleop.rangefind.getCurrentPower();

        if(launcherEnabled) {
            topPower = top_flywheel_controller.update(targetVelocity);
            bottomPower = bottom_flywheel_controller.update(targetVelocity);
        } else {
            top_launcher_motor.set(0);
            bottom_launcher_motor.set(0);
        }

        telemetry();
    }

    static public void telemetry () {
        //telemetry.addData("Trigger", ElsiGamepad.gamepadEx.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER));
        //telemetry.addData("Shooter Enabled", launcherEnabled);
        //telemetry.addData("Target Velocity", targetVelocity);
        //telemetry.addData("Top Power", topPower);
        //telemetry.addData("Bottom Power", bottomPower);
        //telemetry.addData("TOP Velocity", top_launcher_motor.getVelocity());
        //telemetry.addData("BOTTOM Velocity", bottom_launcher_motor.getVelocity());
        //telemetry.addData("Top Ready", top_flywheel_controller.isReady());
        //telemetry.addData("Bottom Ready", bottom_flywheel_controller.isReady());

        //For PANELS Graphing
        panelsTelemetry.addData("Top Velocity", top_launcher_motor.getVelocity());
        panelsTelemetry.addData("Bottom Velocity", bottom_launcher_motor.getVelocity());
    }

    public static boolean launchReady () {
        //if(top_flywheel_controller == null || bottom_flywheel_controller == null) return false;
        return top_flywheel_controller.isReady() && bottom_flywheel_controller.isReady();
    }
}
