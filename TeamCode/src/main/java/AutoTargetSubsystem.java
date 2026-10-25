import static dev.nextftc.extensions.pedro.PedroComponent.follower;

import com.pedropathing.geometry.Pose;

import dev.nextftc.core.commands.Command;
import dev.nextftc.core.commands.utility.LambdaCommand;
import dev.nextftc.core.subsystems.Subsystem;
import dev.nextftc.ftc.ActiveOpMode;

public class AutoTargetSubsystem implements Subsystem {
    public static final AutoTargetSubsystem INSTANCE = new AutoTargetSubsystem();

    private AutoTargetSubsystem() { }

    public static Pose targetPose;

    public Command beginTargeting (boolean autoTarget) {
        if (autoTarget) return beginAutoTarget;
        return new LambdaCommand()
                .setStart(()->{follower().holdPoint(follower().getPose(),false);})
                .setIsDone(()->{return true;})
                .setInterruptible(true)
                .setStop((interrupted) -> {
                    if(interrupted) {
                        follower().breakFollowing();
                        follower().startTeleopDrive(true);
                    }
                })
                .requires(this);
    }

    public LambdaCommand beginAutoTarget= new LambdaCommand()
            .setStart(() -> {
                final Pose current = follower().getPose();
                final double targetAngle = getTargetAngle(current);
                targetPose = new Pose (current.getX(), current.getY(), targetAngle);
                follower().holdPoint(targetPose,false);

            })
            .setUpdate(() -> {

            })
            .setIsDone(() -> {
                return comparePositionAndHeading(follower().getPose(), targetPose);
                /*
                return follower().atPose(
                        targetPose,
                        5,
                        5,
                        5
                );
                 */
            })
            .setInterruptible(true)
            .setStop((interrupted) -> {
                if(interrupted) {
                    follower().breakFollowing();
                    follower().startTeleopDrive(true);
                }
            })
            .requires(this);

    private boolean comparePositionAndHeading (Pose current, Pose target) {
        double currentHeading = current.getHeading();
        double targetHeading = target.getHeading();
        double headingVariance = Math.abs(targetHeading - currentHeading) % (2 * Math.PI);
        if (headingVariance > Math.PI) headingVariance = 2 * Math.PI - headingVariance;
        boolean heading = headingVariance <= Math.toRadians(5);
        boolean position = Math.abs(target.getX() - current.getX()) <= 2 && Math.abs(target.getY() - current.getY()) <= 2;
        ActiveOpMode.telemetry().addData("Heading Variance", Math.toDegrees(headingVariance) );
        ActiveOpMode.telemetry().addData("Position Variance X", Math.abs(target.getX() - current.getX()));
        ActiveOpMode.telemetry().addData("Position Variance Y", Math.abs(target.getY() - current.getY()));
        return heading && position;
    }

    public LambdaCommand endAutoTarget = new LambdaCommand()
            .setStart(() -> {
                follower().breakFollowing();
                follower().startTeleopDrive(true);
            })
            .setIsDone(() -> {return true;})
            .setInterruptible(true)
            .requires(this);

    private double getTargetAngle (Pose current) {
        Pose myCurrentGoal = OpalStatic.currentGoalPose();
        double angle = Math.atan2(myCurrentGoal.getY() - current.getY(), myCurrentGoal.getX() - current.getX());
        return (angle + Math.PI) % (2 * Math.PI); //reverse the angle
    }

    @Override
    public void initialize() {

    }

    @Override
    public void periodic() {
        // periodic logic (runs every loop)
    }
}