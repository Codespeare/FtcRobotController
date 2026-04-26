import static dev.nextftc.extensions.pedro.PedroComponent.follower;

import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import dev.nextftc.core.commands.Command;
import dev.nextftc.core.commands.delays.Delay;
import dev.nextftc.core.commands.groups.ParallelGroup;
import dev.nextftc.core.commands.groups.SequentialGroup;
import dev.nextftc.core.components.BindingsComponent;
import dev.nextftc.core.components.SubsystemComponent;
import dev.nextftc.extensions.pedro.FollowPath;
import dev.nextftc.extensions.pedro.PedroComponent;
import dev.nextftc.ftc.NextFTCOpMode;
import dev.nextftc.ftc.components.BulkReadComponent;

@Autonomous(name = "Elsi3Auto", group = "Autonomous", preselectTeleOp = "Elsi3Teleop")
public class ElsiAutonomous extends NextFTCOpMode {
    {
        addComponents(
                new SubsystemComponent(AutoTargetSubsystem.INSTANCE),
                new SubsystemComponent(ParkSubsystem.INSTANCE),
                new SubsystemComponent(FlywheelSubsystem.INSTANCE),
                new SubsystemComponent(IntakeSubsystem.INSTANCE),
                new SubsystemComponent(TriggerSubsystem.INSTANCE),
                new PedroComponent(Constants::createFollower),
                BulkReadComponent.INSTANCE,
                BindingsComponent.INSTANCE
        );
    }

    private final Double delaySeconds = 2.0;

    private final Pose redGoal = new Pose(135,135);

    private final Pose redCloseStartPose = new Pose(128.5,112.5, Math.toRadians(90));
    private Pose redCloseShootPose = new Pose(96,96);
    private final Pose redTopSpikePose = new Pose(128,83.5, Math.toRadians(0));
    private final Pose redTopSpikeControl = new Pose(93,72);

    private final Pose redMiddleSpikePose = new Pose(128,58.5, Math.toRadians(0));
    private final Pose redMiddleSpikeControl = new Pose(87,41);

    private final Pose redBottomSpikePose = new Pose(128,5, Math.toRadians(0));
    private final Pose redBottomSpikeControl = new Pose(79.5,35);

    private final Pose redCloseParkPose = new Pose(120,72, Math.toRadians(270));


    private Path redStartToFirstShoot;
    private Path redPickupFirstSpike;
    private Path redShootFirstSpike;
    private Path redPickupSecondSpike;
    private Path redShootSecondSpike;
    private Path redPickupThirdSpike;
    private Path redShootThirdSpike;
    private Path redPark;


    private double targetAngle (Pose current, Pose goal) {
        return Math.atan2(goal.getY() - current.getY(), goal.getX() - current.getX());
    }

    private Path buildBezierLinePath(Pose start, Pose end) {
        return buildBezierLinePath(start, end, 0.8);
    }

    private Path buildBezierLinePath(Pose start, Pose end, double endTime) {
        Path path = new Path(new BezierLine(start, end));
        path.setLinearHeadingInterpolation(start.getHeading(), end.getHeading(), endTime);
        return path;
    }

    private Path buildBezierCurvePath(Pose start, Pose guide, Pose end) {
        return buildBezierCurvePath(start, guide, end, 0.8);
    }

    private Path buildBezierCurvePath(Pose start, Pose guide, Pose end, double endTime) {
        Path path = new Path(new BezierCurve(start, guide, end));
        path.setLinearHeadingInterpolation(start.getHeading(), end.getHeading(), endTime);
        return path;
    }

    private Command pickupSpike (Path toSpike, Path fromSpike) {
        return new SequentialGroup(
                new ParallelGroup(
                        new FollowPath(toSpike),
                        FlywheelSubsystem.INSTANCE.spinDown(),
                        IntakeSubsystem.INSTANCE.enable),
                new ParallelGroup(
                        new FollowPath(fromSpike),
                        FlywheelSubsystem.INSTANCE.spinFlywheels(800),
                        IntakeSubsystem.INSTANCE.disable)
        );
    }

    private Command shoot () {
        return new ParallelGroup(
                IntakeSubsystem.INSTANCE.enable,
                new Delay(delaySeconds)
        );
    }

    @Override public void onInit() {
        redCloseShootPose = new Pose (redCloseShootPose.getX(),redCloseShootPose.getY(),targetAngle(redCloseShootPose,redGoal));

        redStartToFirstShoot = buildBezierLinePath(redCloseStartPose, redCloseShootPose);
        redPickupFirstSpike = buildBezierCurvePath(redCloseStartPose, redTopSpikeControl, redTopSpikePose);
        redShootFirstSpike = buildBezierLinePath(redTopSpikePose, redCloseShootPose);
        redPickupSecondSpike = buildBezierCurvePath(redCloseShootPose, redMiddleSpikeControl, redMiddleSpikePose);
        redShootSecondSpike = buildBezierLinePath(redMiddleSpikePose, redCloseShootPose);
        redPickupThirdSpike = buildBezierCurvePath(redCloseShootPose, redBottomSpikeControl, redBottomSpikePose);
        redShootThirdSpike = buildBezierLinePath(redBottomSpikePose, redCloseShootPose);
        redPark = buildBezierLinePath(redCloseShootPose, redCloseParkPose);
    }
    @Override public void onWaitForStart() { }
    @Override public void onStartButtonPressed() {
        follower().setStartingPose(redCloseStartPose);
        new SequentialGroup(
                new ParallelGroup(
                        new FollowPath(redStartToFirstShoot),
                        //TODO: Actual target velocity adjusted for position
                        FlywheelSubsystem.INSTANCE.spinFlywheels(800)),
                shoot(),
                pickupSpike(redPickupFirstSpike, redShootFirstSpike),
                shoot(),
                pickupSpike(redPickupSecondSpike, redShootSecondSpike),
                shoot(),
                pickupSpike(redPickupThirdSpike, redShootThirdSpike),
                shoot(),
                new ParallelGroup(
                        new FollowPath(redPark),
                        FlywheelSubsystem.INSTANCE.spinDown(),
                        IntakeSubsystem.INSTANCE.disable)
        ).schedule();
    }

    @Override public void onUpdate() {
    }

    @Override public void onStop() { }


}
