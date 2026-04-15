import static dev.nextftc.extensions.pedro.PedroComponent.follower;

import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import dev.nextftc.core.components.BindingsComponent;
import dev.nextftc.core.components.SubsystemComponent;
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

    private final Pose redCloseStartPose = new Pose(128,112.5,Math.toRadians(90));
    private final Pose redCloseShootPose = new Pose(96,96, Math.toRadians(50));

    private final Path redStartToShoot = new Path(new BezierLine(redCloseStartPose, redCloseShootPose));


    @Override public void onInit() {
        redStartToShoot.setLinearHeadingInterpolation(redCloseStartPose.getHeading(),redCloseShootPose.getHeading());
    }
    @Override public void onWaitForStart() { }
    @Override public void onStartButtonPressed() {
        //TODO: Replace with real starting poses
        follower().setStartingPose(new Pose(128,112.5,Math.toRadians(90)));

        follower().followPath(redStartToShoot,true);
    }

    @Override public void onUpdate() { }
    @Override public void onStop() { }


}
