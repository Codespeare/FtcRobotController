package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Autonomous(name = "Elsi Auto Base", group = "Auto", preselectTeleOp = "Elsi")
@Disabled
public class ElsiAutoBase extends OpMode {

    public TeamColor teamColor = new TeamColor();
    public ElsiRangefind rangefind = new ElsiRangefind();

    private Follower follower;
    private Timer pathTimer, actionTimer, opmodeTimer;
    private int pathState;

    //Upper Poses
    public static  Pose upperStartPose = new Pose(24, 127, Math.toRadians(144));
    public static  Pose upperShootPose; //= new Pose(57, 83, ElsiRangefind.getTargetAngle(new Pose(57,83),ElsiRangefind.goalPose));
    public static Pose upperPickupLineOne = new Pose(16.4, 83.5, Math.toRadians(180));
    public static  Pose upperLeaveLine = new Pose(53, 131, Math.toRadians(270));

    //Lower Poses
    private  Pose lowerStartPose = new Pose(48, 9.2, Math.toRadians(90));
    private  Pose lowerShootPose;// = new Pose(58, 20, ElsiRangefind.getTargetAngle(new Pose(58,20),ElsiRangefind.goalPose));
    private  Pose lowerLeaveLine = new Pose(36, 12, Math.toRadians(90));

    private Path startingPath;
    private PathChain pickupLine1, shootLine2, leaveLine;

    private final int finished = -1;

    private final double upperVelocity = 775;
    private final double lowerVelocity = 1200;

    @Override
    public void init() {
        ElsiReadCache.init(hardwareMap);
        ElsiGamepad.init(gamepad1); //TODO: Refactor to remove the need for this
        ElsiTelemetry.init(telemetry);
        rangefind.setGoal(teamColor);
        initGoalPoses();

        pathTimer = new Timer();
        opmodeTimer = new Timer();
        opmodeTimer.resetTimer();

        follower = Constants.createFollower(hardwareMap);

        mirrorPoses();

        if(teamColor.isUpper()) {
            buildUpper();
            follower.setStartingPose(upperStartPose);
        } else {
            buildLower();
            follower.setStartingPose(lowerStartPose);
        }

        ElsiAutoLauncherStateMachine.init(hardwareMap);
    }

    private void initGoalPoses () {
        upperShootPose = new Pose(57, 83, ElsiRangefind.getTargetAngle(new Pose (57,83), new Pose (5,140)));
        lowerShootPose = new Pose(58, 20, Math.toRadians(125));
    }

    @Override
    public void start() {
        ElsiTransfer.start();

        opmodeTimer.resetTimer();
        setPathState(0);
    }

    @Override
    public void stop() {
        ElsiDrivetrain.setStartingPose(follower.getPose());
        ElsiTeleop.teamColor = teamColor;
    }

    @Override
    public void loop() {
        ElsiReadCache.loop();

        follower.update();

        ElsiAutoLauncherStateMachine.loop();

        autoPathUpdate();

        // Feedback to Driver Hub for debugging
        telemetry.addData("path state", pathState);
        telemetry.addData("x", follower.getPose().getX());
        telemetry.addData("y", follower.getPose().getY());
        telemetry.addData("heading", follower.getPose().getHeading());

        ElsiTelemetry.loop();
    }

    public void autoPathUpdate () {
        switch (pathState) {
            case 0:
                follower.followPath(startingPath,true);
                setPathState(1);
                break;
            case 1:
                if(!follower.isBusy()) {
                    launchSequence();
                    setPathState(2);
                }
                break;
            case 2:
                if(!ElsiAutoLauncherStateMachine.isBusy()) {
                    if(pickupLine1 != null) {
                        follower.followPath(pickupLine1,true);
                        ElsiTransfer.autoIntakeOn = true;
                    }
                    setPathState(3);
                }
                break;
            case 3:
                if(!follower.isBusy()) {
                    if(pickupLine1 != null) {
                        follower.followPath(shootLine2,true);
                    }
                    setPathState(4);
                }
            case 4:
                if(!follower.isBusy()) {
                    if(pickupLine1 != null) {
                        launchSequence();
                    }
                    setPathState(5);
                }
                break;
            case 5:
                if(!ElsiAutoLauncherStateMachine.isBusy()) {
                    follower.followPath(leaveLine,true);
                    setPathState(finished);
                }
                break;

        }
    }

    private double getTargetVelocity () {

        return 0;
    }

    public void setPathState(int pState) {
        pathState = pState;
        pathTimer.resetTimer();
    }

    private Pose mirror (Pose p) {
        return teamColor.isMirror() ? p.mirror() : p;
    }


    private void launchSequence () {
        double range = ElsiRangefind.getRangeToGoal(follower.getPose(), ElsiRangefind.goalPose);
        double power = ElsiTeleop.rangefind.getPowerByRange(range);

        ElsiAutoLauncherStateMachine.requestFire(power);
    }

    private void mirrorPoses () {
        lowerStartPose = mirror(lowerStartPose);
        lowerShootPose = mirror(lowerShootPose);
        lowerLeaveLine = mirror(lowerLeaveLine);

        upperStartPose = mirror(upperStartPose);
        upperShootPose = mirror(upperShootPose);
        upperPickupLineOne = mirror(upperPickupLineOne);
        upperLeaveLine = mirror(upperLeaveLine);
    }

    private void buildLower () {
        startingPath = new Path(new BezierLine(lowerStartPose, lowerShootPose));
        startingPath.setLinearHeadingInterpolation(lowerStartPose.getHeading(), lowerShootPose.getHeading(),0.95);

        leaveLine = follower.pathBuilder()
                .addPath(new BezierLine(lowerShootPose, lowerLeaveLine))
                .setLinearHeadingInterpolation(lowerShootPose.getHeading(), lowerLeaveLine.getHeading(), 0.9)
                .build();
    }

    private void buildUpper () {
        startingPath = new Path(new BezierLine(upperStartPose, upperShootPose));
        startingPath.setLinearHeadingInterpolation(upperStartPose.getHeading(), upperShootPose.getHeading(), 0.8);

        pickupLine1 = follower.pathBuilder()
                .addPath(new BezierLine(upperShootPose, upperPickupLineOne))
                .setLinearHeadingInterpolation(upperShootPose.getHeading(), upperPickupLineOne.getHeading(),0.4)
                .build();

        shootLine2 = follower.pathBuilder()
                .addPath(new BezierLine(upperPickupLineOne, upperShootPose))
                .setLinearHeadingInterpolation(upperPickupLineOne.getHeading(), upperShootPose.getHeading(), 0.8)
                .build();

        leaveLine = follower.pathBuilder()
                .addPath(new BezierLine(upperShootPose, upperLeaveLine))
                .setLinearHeadingInterpolation(upperShootPose.getHeading(), upperLeaveLine.getHeading(),0.8)
                .build();
    }
}
