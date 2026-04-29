import static dev.nextftc.extensions.pedro.PedroComponent.follower;

import com.pedropathing.geometry.Pose;

import dev.nextftc.core.commands.utility.LambdaCommand;
import dev.nextftc.core.subsystems.Subsystem;

public class AutoTargetSubsystem implements Subsystem {
    public static final AutoTargetSubsystem INSTANCE = new AutoTargetSubsystem();
    private AutoTargetSubsystem() { }

    private final Pose goalPose = new Pose(144, 144);

    private Pose targetPose;

    private static double targetAngle = 0;

    public LambdaCommand beginAutoTarget = new LambdaCommand()
            .setStart(() -> {
                final Pose current = follower().getPose();
                final double targetAngle = getTargetAngle(current, goalPose);
                //follower().turnTo(targetAngle);
                targetPose = new Pose (current.getX(), current.getY(), targetAngle);
                follower().holdPoint(targetPose,false);
            })
            .setUpdate(() -> {

            })
            .setIsDone(() -> {
                return follower().atPose(
                        targetPose,
                        5,
                        5,
                        5
                );
            })
            .setInterruptible(true)
            .setStop((interrupted) -> {
                if(interrupted) {
                    follower().breakFollowing();
                    follower().startTeleopDrive(true);
                }
            });

    public LambdaCommand endAutoTarget = new LambdaCommand().setStart(() -> {
        follower().breakFollowing();
        follower().startTeleopDrive(true);
    }).setIsDone(() -> {return true;});

    private static double getTargetAngle (Pose current, Pose goal) {
        //Returns Radians
        double dy = goal.getY() - current.getY();
        double dx = goal.getX() - current.getX();
        targetAngle = Math.atan2(dy, dx);

        //angle -= Math.PI / 2.0;

        //angle = normalizeAngle(angle);
        //telemetry.addData("Computed Target Angle (Rad)", angle);
        return targetAngle;
        //return normalizeAngle(Math.atan2(dy, dx));
    }


    @Override
    public void initialize() {
        // initialization logic (runs on init)
    }

    @Override
    public void periodic() {
        // periodic logic (runs every loop)

    }
}