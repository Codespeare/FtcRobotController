package org.firstinspires.ftc.teamcode;


import static org.firstinspires.ftc.teamcode.ElsiTelemetry.telemetry;
import static org.firstinspires.ftc.teamcode.ElsiTeleop.teamColor;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

/// Drive System
public class ElsiDrivetrain {

    public static Follower follower;

    public static Pose startingPose = new Pose();
    public static Pose currentPose = new Pose();
    public static Pose targetPose = new Pose();
    public static Pose lastTargetPose = new Pose();
    private static boolean holding = false;

    private static final double maxPower = 0.8;


    private static double goalX;
    private static final double goalY = 140;

    public static Pose goalPose;

    private static final double DEADBAND = 0.05;
    //private static final double FRONT_REAR_STRAFE_BIAS = 0.1; // tune 0.01–0.12

    private static double targetAngleRadians = 0;

    public static void setStartingPose (Pose p) {
        startingPose = p;
    }

    public static void init(HardwareMap hardwareMap){
        goalX = teamColor.isMirror() ? 140 : 5;
        goalPose = new Pose(goalX, goalY);

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startingPose);
        Drawing.init();
    }

    public static void stop () {
        //startingPose = follower.getPose();
    }

    public static void start () {
        follower.startTeleopDrive(true);
        follower.setMaxPower(maxPower);

    }

    private static void updateTelemetry () {
        //telemetry.addData("Goal X", goalX);
        //telemetry.addData("Goal Y", goalY);

        /*
        if(holding) {
            telemetry.addData("Target X", targetPose.getX());
            telemetry.addData("Target Y", targetPose.getY());
            telemetry.addData("Target Angle (Rad)", targetPose.getHeading() );
        } else {
            telemetry.addData("Target X", lastTargetPose.getX());
            telemetry.addData("Target Y", lastTargetPose.getY());
            telemetry.addData("Target Angle (Rad)", lastTargetPose.getHeading() );
        }
         */

        telemetry.addData("Current X", currentPose.getX());
        telemetry.addData("Current Y", currentPose.getY());
        telemetry.addData("Current Angle (Degree)", Math.toDegrees(currentPose.getHeading()));
    }

    public static void loop () {
        follower.update();
        currentPose = follower.getPose();
        if(!holding) {
            targetAngleRadians = ElsiRangefind.getTargetAngle(currentPose, goalPose);
            targetPose = new Pose(currentPose.getX(), currentPose.getY(), targetAngleRadians);
        }

        double range = ElsiTeleop.rangefind.getRangeToGoal(currentPose, goalPose);
        double power = ElsiTeleop.rangefind.getPowerByRange(range);

        if(ElsiGamepad.gamepadEx.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) == 1) {

            if(!holding) {
                follower.holdPoint(targetPose,false);
                follower.setMaxPower(1);
                //follower.turnTo(targetAngleRadians);
                holding = true;
                lastTargetPose = targetPose;
            }

        } else {
            if(holding) {
                follower.startTeleopDrive(true);
                follower.setMaxPower(maxPower);
                holding = false;
            }

            double leftX = applyDeadband(ElsiGamepad.gamepadEx.getLeftX(),DEADBAND);
            double leftY = applyDeadband(ElsiGamepad.gamepadEx.getLeftY(),DEADBAND);
            double rightX = applyDeadband(ElsiGamepad.gamepadEx.getRightX(), DEADBAND);

            leftX = cube(leftX);
            leftY = cube(leftY);
            rightX = cube(rightX);

            follower.setTeleOpDrive(leftY, -leftX, -rightX);

            //drive.driveRobotCentric(leftX, leftY, rightX,true);
            //drive(ElsiGamepad.gamepadEx.getLeftY(), ElsiGamepad.gamepadEx.getLeftX(), ElsiGamepad.gamepadEx.getRightX());
        }

        updateTelemetry();
        //Drawing.drawDebug(follower);
        Drawing.drawRobot(currentPose);
        Drawing.sendPacket();
    }

    public static void updatePosition(Pose p) {
        follower.setPose(p);
    }


    /*
    public static void drive(double forward, double right, double rotate) {
        // This calculates the power needed for each wheel based on the amount of forward,
        // strafe right, and rotate
        forward = applyDeadband(forward, DEADBAND);
        right = applyDeadband(right, DEADBAND);
        rotate = applyDeadband(rotate, DEADBAND);

        //right = -right;

        // --- 2. Non-linear shaping (cubic) ---
        double xTarget = cube(right);
        double yTarget = cube(forward);
        double rotTarget = cube(rotate);

        // --- 4. Convert to mecanum wheel powers ---
        double fl = yTarget + xTarget + rotTarget;
        double fr = yTarget - xTarget - rotTarget;
        double br = yTarget + xTarget - rotTarget;
        double bl = yTarget - xTarget + rotTarget;

        double frontScale = 1.0 + FRONT_REAR_STRAFE_BIAS;
        double rearScale  = 1.0 - FRONT_REAR_STRAFE_BIAS;

        fl *= frontScale;
        fr *= frontScale;
        bl *= rearScale;
        br *= rearScale;

        // --- 5. Normalize so we never exceed |1.0| ---
        double max = Math.max(
                1.0,
                Math.max(Math.abs(fl),
                        Math.max(Math.abs(fr),
                                Math.max(Math.abs(bl), Math.abs(br)))));

        fl /= max;
        fr /= max;
        bl /= max;
        br /= max;

        // --- 6. Send to motors ---
        frontLeftDrive.set(fl);
        frontRightDrive.set(fr);
        backLeftDrive.set(bl);
        backRightDrive.set(br);
    }
    */

    private static double applyDeadband(double x, double db) {
        return (Math.abs(x) < db) ? 0.0 : x;
    }

    // Shape input so small stick movements give very fine control
    private static double cube(double x) {
        return x * x * x;
    }

}
