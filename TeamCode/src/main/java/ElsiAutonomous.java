import static dev.nextftc.extensions.pedro.PedroComponent.follower;

import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import dev.nextftc.core.commands.Command;
import dev.nextftc.core.commands.delays.Delay;
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
                new PedroComponent(Constants::createFollower),
                BulkReadComponent.INSTANCE,
                BindingsComponent.INSTANCE
        );
    }

    private final Double delaySeconds = 1.5;

    private final Pose redCloseStartPose = new Pose(127.3,110, Math.toRadians(90));
    private final Pose redCloseFirstShoot = new Pose(83,82, Math.toRadians(45));
    private final Pose redCloseFirstSpike = new Pose(127,82, Math.toRadians(0));
    private final Pose redCloseSecondShoot = new Pose(82.8,78, Math.toRadians(45));
    private final Pose redCloseSecondSpike = new Pose(128.8,58.5, Math.toRadians(0));
    private final Pose redCloseSecondSpikeControl = new Pose(80.4,56.4);
    private final Pose redCloseThirdShoot = new Pose(76.9,16.3, Math.toRadians(67));
    private final Pose redCloseThirdShootControl = new Pose(84,58);
    private final Pose redCloseThirdSpike = new Pose(128,34.5, Math.toRadians(0));
    private final Pose redCloseThirdSpikeControl = new Pose(83,37);
    private final Pose redCloseFourthShoot = new Pose(84,12, Math.toRadians(69));
    private final Pose redCloseEndPose = new Pose(83,59, Math.toRadians(-141));


    private Path redStartToFirstShoot;
    private Path FirstShootToFirstSpike;
    private Path FirstSpikeToSecondShoot;
    private Path SecondShootToSecondSpike;
    private Path SecondSpikeToThirdShoot;
    private Path ThirdShootToThirdSpike;
    private Path ThirdSpikeToFourthShoot;
    private Path FourthShootToEnd;



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
                new FollowPath(toSpike),
                new FollowPath(fromSpike)
        );
    }
    @Override public void onInit() {
        redStartToFirstShoot = buildBezierLinePath(redCloseStartPose, redCloseFirstShoot);
        FirstShootToFirstSpike = buildBezierLinePath(redCloseFirstShoot, redCloseFirstSpike, 0.0);
        FirstSpikeToSecondShoot = buildBezierLinePath(redCloseFirstSpike, redCloseSecondShoot);
        SecondShootToSecondSpike = buildBezierCurvePath(redCloseSecondShoot,redCloseSecondSpikeControl, redCloseSecondSpike, 0.0);
        SecondSpikeToThirdShoot = buildBezierCurvePath(redCloseSecondSpike,redCloseThirdShootControl ,redCloseThirdShoot );
        ThirdShootToThirdSpike = buildBezierCurvePath(redCloseThirdShoot, redCloseThirdSpikeControl,redCloseThirdSpike , 0.0);
        ThirdSpikeToFourthShoot = buildBezierLinePath(redCloseThirdSpike, redCloseFourthShoot);
        FourthShootToEnd = buildBezierLinePath(redCloseFourthShoot, redCloseEndPose);
    }
    @Override public void onWaitForStart() { }
    @Override public void onStartButtonPressed() {
        //TODO: Replace with real starting poses
        //follower().setStartingPose(new Pose(128,112.5,Math.toRadians(90)));
        follower().setStartingPose(redCloseStartPose);
        new SequentialGroup(
                new FollowPath(redStartToFirstShoot),
                new Delay(delaySeconds),
                pickupSpike(FirstShootToFirstSpike, FirstSpikeToSecondShoot),
                new Delay(delaySeconds),
                pickupSpike(SecondShootToSecondSpike, SecondSpikeToThirdShoot),
                new Delay(delaySeconds),
                pickupSpike(ThirdShootToThirdSpike, ThirdSpikeToFourthShoot),
                new Delay(delaySeconds),
                new FollowPath(FourthShootToEnd)
        ).schedule();
    }

    @Override public void onUpdate() { }
    @Override public void onStop() { }


}
