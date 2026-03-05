package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

public class ElsiTransfer {


    private static boolean intakeOn = false;

    //private static CRServo frontIntake, midIntake1, midIntake2, trigger;

    private static MotorEx rearTransfer;
    private static MotorEx frontIntake;
    private static ServoEx trigger;

    private static final double TriggerOpen = 70;
    private static final double TriggerClosed = 210;

    private static final double TransferOn = 0.7, IntakeOn = 0.7;
    private static final double TransferOff = 0, IntakeOff = 0;

    private static boolean autoMode = false;
    public static boolean autoIntakeOn = false;
    public static boolean autoLaunchOn = false;

    public static void init(HardwareMap hardwareMap, boolean isAuto){
        rearTransfer = new MotorEx(hardwareMap,"rearTransfer", Motor.GoBILDA.RPM_1150);
        rearTransfer.setRunMode(Motor.RunMode.RawPower);

        frontIntake = new MotorEx(hardwareMap,"frontIntake", Motor.GoBILDA.RPM_1150);
        frontIntake.setRunMode(Motor.RunMode.RawPower);
        frontIntake.setInverted(true);

        trigger = new ServoEx(hardwareMap, "trigger",0,300);
        trigger.set(TriggerClosed);

        autoMode = isAuto;
    }

    public static void start () {
        trigger.set(TriggerClosed);
    }

    public static void loop () {
        //telemetry.addLine("Press B to toggle the intake");
        //telemetry.addLine("Press Right Trigger to Launch");

        if(ElsiGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.B)) {
            intakeOn = !intakeOn;
        }

        if(intakeOn || (autoMode && autoIntakeOn)) {
            rearTransfer.set(TransferOn);
            frontIntake.set(IntakeOn);
        } else {
            rearTransfer.set(TransferOff);
            frontIntake.set(IntakeOff);
        }

        /*
        if(ElsiGamepad.gamepadEx.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) == 1){
            trigger.set(TriggerOpen);
        } else {
            trigger.set(TriggerClosed);
        }
        */

        if((ElsiGamepad.gamepadEx.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) == 1 && ElsiLauncher.launchReady())
                || (autoMode && autoLaunchOn && ElsiLauncher.launchReady())) {
            boolean inCorrectPose = ElsiDrivetrain.currentPose.roughlyEquals(ElsiDrivetrain.targetPose,2) || autoMode; //I think it's smaller than 1, but greater than 0.1
            //telemetry.addData("In Correct Pose", inCorrectPose);
            if(inCorrectPose) {
                trigger.set(TriggerOpen);
                rearTransfer.set(TransferOn);
                frontIntake.set(IntakeOn);
            }
            //telemetry.addData("Distance to Target Pose",ElsiDrivetrain.currentPose.distanceFrom(ElsiDrivetrain.targetPose));
        } else {
            trigger.set(TriggerClosed);
        }



    }


}
