package elsi.archive;

/*
@Autonomous(name = "Elsi3AutoFront", group = "Autonomous", preselectTeleOp = "elsi.archive.Elsi3Teleop")
@Disabled
public class ElsiAutonomousFront extends NextFTCOpMode {
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

    private static final Double closeGoalFlywheelSpeed = 650.0;

    private final Pose redGoal = new Pose(135,135);

    private final Pose redCloseStartPose = new Pose(128.5,112.5, Math.toRadians(270));
    //private final Pose redCloseStartPose = new Pose(123,123.5, Math.toRadians(216.5));

    //private Pose redCloseShootPose = new Pose(96,96);
    private Pose redCloseShootPose = new Pose(90,90);

    //private final Pose redTopSpikePose = new Pose(126,83.5, Math.toRadians(0));
    private final Pose redTopSpikePoseA = new Pose(100,83.5, Math.toRadians(0));
    private final Pose redTopSpikePoseB = new Pose(126,83.5, Math.toRadians(0));
    private final Pose redTopSpikeControl = new Pose(82,79.5);

    private final Pose redMiddleSpikePoseA = new Pose(100,59.5, Math.toRadians(0));
    private final Pose redMiddleSpikePoseB = new Pose(130,59.5, Math.toRadians(0));

    private final Pose redMiddleSpikeControl = new Pose(82,62);

    private final Pose redDumpGatePose = new Pose(126.3,66, Math.toRadians(270));
    private final Pose redDumpGateControl = new Pose(109,63.5);

    private final Pose redBottomSpikePoseA = new Pose(100,35, Math.toRadians(0));
    private final Pose redBottomSpikePoseB = new Pose(132,35, Math.toRadians(0));

    //private final Pose redBottomSpikeControl = new Pose(79.5,28.7);

    private final Pose redCloseParkPose = new Pose(120,72, Math.toRadians(270));


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
                    //new FollowPath(toSpike,true,0.55),
                    IntakeSubsystem.INSTANCE.enable,
                    new ParallelRaceGroup(
                            new FollowPath(toSpike,true,0.6),
                            new Delay(1.5)
                    ),
                    FlywheelSubsystem.INSTANCE.spinDown()
                ),
                new Delay(0.3)
        );
    }

    private Command shootSpike (Path fromSpike) {
        return new ParallelGroup(
                new FollowPath(fromSpike),
                FlywheelSubsystem.INSTANCE.spinFlywheels(() -> {
                    return closeGoalFlywheelSpeed;
                }),
                IntakeSubsystem.INSTANCE.disable);
    }

    private Command shoot () {
        return new SequentialGroup(
                new ParallelGroup(
                    IntakeSubsystem.INSTANCE.enable,
                    TriggerSubsystem.INSTANCE.open
                ),
                new Delay(delaySeconds),
                TriggerSubsystem.INSTANCE.close
        );
    }

    @Override public void onInit() {
        redCloseShootPose = new Pose (redCloseShootPose.getX(),redCloseShootPose.getY(),targetAngle(redCloseShootPose,redGoal));

        redStartToFirstShoot = buildBezierLinePath(redCloseStartPose, redCloseShootPose);
        //redPickupFirstSpike = buildBezierCurvePath(redCloseShootPose, redTopSpikeControl, redTopSpikePose, .1);


        redPickupFirstSpikeA = buildBezierLinePath(redCloseShootPose, redTopSpikePoseA);
        redPickupFirstSpikeB = buildBezierLinePath(redTopSpikePoseA, redTopSpikePoseB);
        redShootFirstSpike = buildBezierLinePath(redTopSpikePoseB, redCloseShootPose);

        redPickupSecondSpikeA = buildBezierLinePath(redCloseShootPose, redMiddleSpikePoseA);
        redPickupSecondSpikeB = buildBezierLinePath(redMiddleSpikePoseA, redMiddleSpikePoseB);

        redDumpGate = buildBezierCurvePath(redMiddleSpikePoseB, redDumpGateControl, redDumpGatePose);

        redShootSecondSpike = buildBezierCurvePath(redDumpGatePose, redMiddleSpikeControl, redCloseShootPose, 1);

        redPickupThirdSpikeA = buildBezierLinePath(redCloseShootPose, redBottomSpikePoseA);
        redPickupThirdSpikeB = buildBezierLinePath(redBottomSpikePoseA, redBottomSpikePoseB);
        redShootThirdSpike = buildBezierLinePath(redBottomSpikePoseB, redCloseShootPose);
        redPark = buildBezierLinePath(redCloseShootPose, redCloseParkPose);
    }
    @Override public void onWaitForStart() { }
    @Override public void onStartButtonPressed() {
        follower().setStartingPose(redCloseStartPose);
        new SequentialGroup(
                new ParallelGroup(
                        FlywheelSubsystem.INSTANCE.spinFlywheels(() -> {
                            return closeGoalFlywheelSpeed;
                        })),
                        new FollowPath(redStartToFirstShoot),
                shoot(),

                new FollowPath(redPickupSecondSpikeA),
                intakeSpike(redPickupSecondSpikeB),

                new ParallelRaceGroup(
                        new FollowPath(redDumpGate),
                        new Delay(1.5)
                ),
                new Delay(0.25),

                shootSpike(redShootSecondSpike),
                //pickupAndShootSpike(redPickupSecondSpikeB, redShootSecondSpike),
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
        Pose currentPose = follower().getPose();
        if(currentPose.getY() != 0 && currentPose.getX() != 0) {
            Elsi3Static.currentPose = follower().getPose();
        }
    }

    @Override public void onStop() {
        FlywheelSubsystem.INSTANCE.spinDown().run();
        IntakeSubsystem.INSTANCE.disable.run();
        TriggerSubsystem.INSTANCE.close.run();
    }


}
*/