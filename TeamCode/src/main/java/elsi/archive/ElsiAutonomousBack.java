package elsi.archive;

/*
@Autonomous(name = "Elsi3AutoBack", group = "Autonomous", preselectTeleOp = "elsi.archive.Elsi3Teleop")
@Disabled
public class ElsiAutonomousBack extends NextFTCOpMode {
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

    private static final Double delaySeconds = 1.0;

    private static final Double farGoalFlywheelSpeed = 805.0;

    private final Pose redGoal = new Pose(138,137);

    private final Pose redFarStartPose = new Pose(96,9.3, Math.toRadians(90));

    private final Pose redFarStartPoseControl = new Pose(97,20);

    private Pose redFarShootPose = new Pose(86.5,20);

    private final Pose redTopSpikePoseA = new Pose(100,83.5, Math.toRadians(0));
    private final Pose redTopSpikePoseB = new Pose(126,83.5, Math.toRadians(0));

    private final Pose redMiddleSpikePoseA = new Pose(100,59.5, Math.toRadians(0));
    private final Pose redMiddleSpikePoseB = new Pose(130,59.5, Math.toRadians(0));

    private final Pose redMiddleSpikeControl = new Pose(82,62);

    private final Pose redDumpGatePose = new Pose(126.5,66, Math.toRadians(270));
    private final Pose redDumpGateControl = new Pose(109,63.5);

    private final Pose redBottomSpikePoseA = new Pose(100,35, Math.toRadians(0));
    private final Pose redBottomSpikePoseB = new Pose(132,35, Math.toRadians(0));

    private final Pose redFarParkPose = new Pose(105.4,33.3, Math.toRadians(90));


    private Path redStartToFirstShoot;
    private Path redPickupFirstSpikeA, redPickupFirstSpikeB;
    private Path redShootFirstSpike;
    private Path redPickupSecondSpikeA, redPickupSecondSpikeB;
    private Path redShootSecondSpike;
    private Path redDumpGate;

    private Path redPickupThirdSpikeA, redPickupThirdSpikeB;
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

    private Path buildBezierCurvePath(Pose start, Pose guide, Pose guide2, Pose end, double endTime) {
        Path path = new Path(new BezierCurve(start, guide, guide2, end));
        path.setLinearHeadingInterpolation(start.getHeading(), end.getHeading(), endTime);
        return path;
    }

    private Command pickupAndShootSpike(Path toSpike, Path fromSpike) {
        return new SequentialGroup(
                intakeSpike(toSpike),
                shootSpike(fromSpike)
        );
    }

    private Command intakeSpike (Path toSpike) {
        return new SequentialGroup(
                new ParallelGroup(
                    new FollowPath(toSpike,true,0.55),
                    FlywheelSubsystem.INSTANCE.spinDown(),
                    IntakeSubsystem.INSTANCE.enable
                ),
                new Delay(0.4)
        );
    }

    private Command shootSpike (Path fromSpike) {
        return new ParallelGroup(
                new FollowPath(fromSpike),
                FlywheelSubsystem.INSTANCE.spinFlywheels(() -> {
                    return farGoalFlywheelSpeed;
                }),
                IntakeSubsystem.INSTANCE.disable);
    }

    private Command shoot () {
        return new SequentialGroup(
                new Delay(0.5),
                new ParallelGroup(
                    IntakeSubsystem.INSTANCE.enable,
                    TriggerSubsystem.INSTANCE.open
                ),
                new Delay(delaySeconds),
                TriggerSubsystem.INSTANCE.close
        );
    }

    @Override public void onInit() {
        redFarShootPose = new Pose (redFarShootPose.getX(), redFarShootPose.getY(),targetAngle(redFarShootPose,redGoal));

        redStartToFirstShoot = buildBezierCurvePath(redFarStartPose, redFarStartPoseControl, redFarShootPose);

        redPickupFirstSpikeA = buildBezierLinePath(redFarShootPose, redTopSpikePoseA);
        redPickupFirstSpikeB = buildBezierLinePath(redTopSpikePoseA, redTopSpikePoseB);
        redShootFirstSpike = buildBezierLinePath(redTopSpikePoseB, redFarShootPose, 1.0);

        redPickupSecondSpikeA = buildBezierLinePath(redFarShootPose, redMiddleSpikePoseA);
        redPickupSecondSpikeB = buildBezierLinePath(redMiddleSpikePoseA, redMiddleSpikePoseB);

        redDumpGate = buildBezierCurvePath(redMiddleSpikePoseB, redDumpGateControl, redDumpGatePose);

        redShootSecondSpike = buildBezierCurvePath(redDumpGatePose, redMiddleSpikeControl, redFarShootPose);

        redPickupThirdSpikeA = buildBezierLinePath(redFarShootPose, redBottomSpikePoseA);
        redPickupThirdSpikeB = buildBezierLinePath(redBottomSpikePoseA, redBottomSpikePoseB);
        redShootThirdSpike = buildBezierLinePath(redBottomSpikePoseB, redFarShootPose);
        redPark = buildBezierLinePath(redFarShootPose, redFarParkPose);
    }
    @Override public void onWaitForStart() { }
    @Override public void onStartButtonPressed() {
        follower().setStartingPose(redFarStartPose);
        new SequentialGroup(
                new ParallelGroup(
                        FlywheelSubsystem.INSTANCE.spinFlywheels(() -> {
                            return farGoalFlywheelSpeed;
                        }),
                        new ParallelRaceGroup(
                                new FollowPath(redStartToFirstShoot),
                                new Delay(1.5)
                        )
                ),

                shoot(),

                new FollowPath(redPickupSecondSpikeA),
                intakeSpike(redPickupSecondSpikeB),

                new ParallelRaceGroup(
                        new FollowPath(redDumpGate),
                        new Delay(1.5)
                ),
                new Delay(0.2),

                shootSpike(redShootSecondSpike),
                shoot(),

                new FollowPath(redPickupFirstSpikeA),
                pickupAndShootSpike(redPickupFirstSpikeB, redShootFirstSpike),
                shoot(),

                new FollowPath(redPickupThirdSpikeA),
                pickupAndShootSpike(redPickupThirdSpikeB, redShootThirdSpike),
                shoot(),
                new ParallelGroup(
                        new FollowPath(redPark),
                        FlywheelSubsystem.INSTANCE.spinDown(),
                        IntakeSubsystem.INSTANCE.disable)
        ).schedule();
    }

    @Override public void onUpdate() {
        Elsi3Static.currentPose = follower().getPose();
    }

    @Override public void onStop() {
        FlywheelSubsystem.INSTANCE.spinDown().run();
        IntakeSubsystem.INSTANCE.disable.run();
        TriggerSubsystem.INSTANCE.close.run();
    }


}
*/