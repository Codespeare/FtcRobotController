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
import dev.nextftc.core.commands.groups.ParallelRaceGroup;
import dev.nextftc.core.commands.groups.SequentialGroup;
import dev.nextftc.core.components.BindingsComponent;
import dev.nextftc.core.components.SubsystemComponent;
import dev.nextftc.extensions.pedro.FollowPath;
import dev.nextftc.extensions.pedro.PedroComponent;
import dev.nextftc.ftc.ActiveOpMode;
import dev.nextftc.ftc.Gamepads;
import dev.nextftc.ftc.NextFTCOpMode;
import dev.nextftc.ftc.components.BulkReadComponent;

@Autonomous(name = "OpalAutonomous", group = "Opal", preselectTeleOp = "OpalTeleop")
public class OpalAutonomous extends NextFTCOpMode {
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

    private static final Double delaySeconds = 1.5;
    private static boolean waitBeforeShooting = false; //For far auto - false if we shoot immediately, true if we wait to shoot until the end
    private static boolean waitBeforeParking = true; //For far auto - true if we park immediately after shooting, false if we wait until the very end

    private static final Double closeGoalFlywheelSpeed = 780.0;
    private static final Double farGoalFlywheelSpeed = 930.0;


    //private final Pose redCloseStartPose = new Pose(128.5,112.5, Math.toRadians(270));
    //private final Pose redCloseStartPose = new Pose(130,114.2, Math.toRadians(180));
    private final Pose redCloseStartPose = new Pose(111.8,136, Math.toRadians(270));
    //private final Pose redCloseStartPose = new Pose(123,123.5, Math.toRadians(216.5));

    //private Pose redCloseShootPose = new Pose(96,96);
    private Pose redCloseShootPose = new Pose(88,88);

    private final Pose redFarStartPose = new Pose(96,7.5, Math.toRadians(90));

    private final Pose redFarStartPoseControl = new Pose(96,18);

    private Pose redFarShootPose = new Pose(87,15);

    private final Pose redTopSpikePoseA = new Pose(102,83.5, Math.toRadians(0));
    private final Pose redTopSpikePoseB = new Pose(127.5,83.5, Math.toRadians(0));

    private final Pose redMiddleSpikePoseA = new Pose(102,59, Math.toRadians(0));
    private final Pose redMiddleSpikePoseB = new Pose(134,59, Math.toRadians(0));

    private final Pose redMiddleSpikeControl = new Pose(100,48);

    private final Pose redDumpGatePose = new Pose(129,66, Math.toRadians(270));
    private final Pose redDumpGateControl = new Pose(109,63.5);

    private final Pose redBottomSpikePoseA = new Pose(102,35, Math.toRadians(0));
    private final Pose redBottomSpikePoseB = new Pose(134,35, Math.toRadians(0));


    private final Pose redCloseParkPose = new Pose(120,72, Math.toRadians(270));
    private final Pose redFarParkPose = new Pose(105.4,33.3, Math.toRadians(90));

    private final Pose redHumanSpikePose = new Pose (135, 8.5, Math.toRadians(0));


    private Path startToFirstShoot;
    private Path pickupFirstSpikeA, pickupFirstSpikeB;
    private Path shootFirstSpike;
    private Path pickupSecondSpikeA, pickupSecondSpikeB;
    private Path shootSecondSpike;
    private Path dumpGate;

    private Path pickupThirdSpikeA, pickupThirdSpikeB;
    private Path shootThirdSpike;

    private Path pickupHumanSpike;
    private Path shootHumanSpike;

    private Path parkPath;

    private Pose redShootPose () {
        return OpalStatic.startingPosition == StartingPosition.close ? redCloseShootPose : redFarShootPose;
    }
    private Pose redStartPose () {
        return OpalStatic.startingPosition == StartingPosition.close ? redCloseStartPose : redFarStartPose;
    }
    private Pose redParkPose () {
        return OpalStatic.startingPosition == StartingPosition.close ? redCloseParkPose : redFarParkPose;
    }
    private Pose shootPose () {
        return OpalStatic.teamColor == TeamColor.red ? redShootPose() : redShootPose().mirror();
    }
    private Pose startPose () {
        return OpalStatic.teamColor == TeamColor.red ? redStartPose() : redStartPose().mirror();
    }
    private Pose parkPose () {
        return OpalStatic.teamColor == TeamColor.red ? redParkPose() : redParkPose().mirror();
    }


    private double targetAngle (Pose current, Pose goal) {
        double angle = Math.atan2(goal.getY() - current.getY(), goal.getX() - current.getX());
        return (angle + Math.PI) % (2 * Math.PI); //reverse the angle
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

    private Command pickupAndShootSpikeWithDelay(Path toSpike, Path fromSpike, double delay) {
        return new SequentialGroup(
                intakeSpike(toSpike, delay),
                toShootSpike(fromSpike)
        );
    }

    private Command pickupAndShootSpike(Path toSpike, Path fromSpike) {
        return new SequentialGroup(
                intakeSpike(toSpike, 0.25),
                toShootSpike(fromSpike)
        );
    }

    private Command intakeSpike (Path toSpike, double delay) {
        return new SequentialGroup(
                IntakeSubsystem.INSTANCE.enable,
                new ParallelRaceGroup(
                        //new FollowPath(toSpike,true,0.6), //Elsi
                        new FollowPath(toSpike,true), //Opal
                        new Delay(1.5)
                ),
                new Delay(delay)
        );
    }

    private Command toShootSpike(Path fromSpike) {
        return new SequentialGroup(
                IntakeSubsystem.INSTANCE.disable,
                new ParallelGroup(new FollowPath(fromSpike),
                        FlywheelSubsystem.INSTANCE.spinFlywheels(() -> {
                            return OpalStatic.startingPosition == StartingPosition.close ?
                            closeGoalFlywheelSpeed : farGoalFlywheelSpeed;
                        })),
                TriggerSubsystem.INSTANCE.open
        );

    }

    private Command shoot () {
        return new SequentialGroup(
                //TriggerSubsystem.INSTANCE.open,
                //new Delay(.1),
                IntakeSubsystem.INSTANCE.enableTransferForLaunch,
                new Delay(delaySeconds),
                TriggerSubsystem.INSTANCE.close,
                IntakeSubsystem.INSTANCE.enable
        );
    }

    @Override public void onInit() {
        ActiveOpMode.telemetry().setAutoClear(true);

        redCloseShootPose = new Pose (redCloseShootPose.getX(),redCloseShootPose.getY(),targetAngle(redCloseShootPose, OpalStatic.redGoalPose));
        redFarShootPose = new Pose (redFarShootPose.getX(), redFarShootPose.getY(),targetAngle(redFarShootPose, OpalStatic.redGoalPose));

        preparePaths();

        Gamepads.gamepad1().dpadUp()
                .whenBecomesTrue(() -> {
                    OpalStatic.startingPosition = StartingPosition.close;
                    preparePaths();
                });

        Gamepads.gamepad1().dpadDown()
                .whenBecomesTrue(() -> {
                    OpalStatic.startingPosition = StartingPosition.far;
                    preparePaths();
                });

        Gamepads.gamepad1().dpadLeft()
                .whenBecomesTrue(() -> {
                    OpalStatic.teamColor = TeamColor.blue;
                    preparePaths();
                });

        Gamepads.gamepad1().dpadRight()
                .whenBecomesTrue(() -> {
                    OpalStatic.teamColor = TeamColor.red;
                    preparePaths();
                });

        /*
        Gamepads.gamepad1().leftBumper()
                .whenBecomesTrue(() -> {
                    if(!waitBeforeShooting) {
                        waitBeforeShooting = true;
                        waitBeforeParking = false;
                    } else {
                        if(!waitBeforeParking) {
                            waitBeforeParking = true;
                        } else {
                            waitBeforeParking = false;
                            waitBeforeShooting = false;
                        }
                    }
                });

         */
    }

    private void preparePaths () {
        boolean r = OpalStatic.teamColor == TeamColor.red;

        if(OpalStatic.startingPosition == StartingPosition.close) {
            startToFirstShoot = buildBezierLinePath(startPose(), shootPose());
        } else
        {
            startToFirstShoot = buildBezierCurvePath(
                    startPose(),
                    r ? redFarStartPoseControl : redFarStartPoseControl.mirror(),
                    shootPose());
        }

        pickupFirstSpikeA = buildBezierLinePath(
                shootPose(),
                r ? redTopSpikePoseA : redTopSpikePoseA.mirror());
        pickupFirstSpikeB = buildBezierLinePath(
                r ? redTopSpikePoseA : redTopSpikePoseA.mirror(),
                r ? redTopSpikePoseB : redTopSpikePoseB.mirror());
        shootFirstSpike = buildBezierLinePath(
                r ? redTopSpikePoseB : redTopSpikePoseB.mirror(),
                shootPose());

        pickupSecondSpikeA = buildBezierLinePath(
                shootPose(),
                r ? redMiddleSpikePoseA : redMiddleSpikePoseA.mirror());
        pickupSecondSpikeB = buildBezierLinePath(
                r ? redMiddleSpikePoseA : redMiddleSpikePoseA.mirror(),
                r ? redMiddleSpikePoseB : redMiddleSpikePoseB.mirror());

        dumpGate = buildBezierCurvePath(
                r ? redMiddleSpikePoseB : redMiddleSpikePoseB.mirror(),
                r ? redDumpGateControl : redDumpGateControl.mirror(),
                r ? redDumpGatePose : redDumpGatePose.mirror());

        shootSecondSpike = buildBezierCurvePath(
                r ? redDumpGatePose : redDumpGatePose.mirror(),
                r ? redMiddleSpikeControl : redMiddleSpikeControl.mirror(),
                shootPose(), 1);

        pickupThirdSpikeA = buildBezierLinePath(
                shootPose(),
                r ? redBottomSpikePoseA : redBottomSpikePoseA.mirror());
        pickupThirdSpikeB = buildBezierLinePath(
                r ? redBottomSpikePoseA : redBottomSpikePoseA.mirror(),
                r ? redBottomSpikePoseB : redBottomSpikePoseB.mirror());
        shootThirdSpike = buildBezierLinePath(
                r ? redBottomSpikePoseB : redBottomSpikePoseB.mirror(),
                shootPose());

        pickupHumanSpike = buildBezierLinePath(
                shootPose(),
                r ? redHumanSpikePose : redHumanSpikePose.mirror(),
                0.5
        );
        shootHumanSpike = buildBezierLinePath(
                r ? redHumanSpikePose : redHumanSpikePose.mirror(),
                shootPose()
        );

        parkPath = buildBezierLinePath(shootPose(), parkPose());
    }

    @Override public void onWaitForStart() {
        if(OpalStatic.startingPosition == StartingPosition.close) {
            ActiveOpMode.telemetry().addLine("   [Close]   ");
        } else {
            ActiveOpMode.telemetry().addLine("    Close    ");
        }

        if(OpalStatic.teamColor == TeamColor.red) {
            ActiveOpMode.telemetry().addLine(" Blue   [Red]");
        } else {
            ActiveOpMode.telemetry().addLine("[Blue]   Red ");
        }

        if(OpalStatic.startingPosition == StartingPosition.far) {
            ActiveOpMode.telemetry().addLine("    [Far]    ");
        } else {
            ActiveOpMode.telemetry().addLine("     Far     ");
        }

        /*
        if(OpalStatic.startingPosition == StartingPosition.far) {
            ActiveOpMode.telemetry().addData("Wait to shoot: ", waitBeforeShooting);
            if(waitBeforeShooting) {
                ActiveOpMode.telemetry().addData("Wait to park: ", waitBeforeParking);
            }
        }
         */

        ActiveOpMode.telemetry().update();
    }

    @Override public void onStartButtonPressed() {
        follower().setStartingPose(startPose());
        if(OpalStatic.startingPosition == StartingPosition.close) {
            closeAuto();
        } else {
            farAuto();
        }
    }

    public void closeAuto () {
        new SequentialGroup(
                new ParallelGroup(
                        FlywheelSubsystem.INSTANCE.spinFlywheels(() -> {
                            return closeGoalFlywheelSpeed;
                        }),
                        TriggerSubsystem.INSTANCE.open,
                        new FollowPath(startToFirstShoot)
                ),
                new Delay(0.2),
                shoot(),

                new FollowPath(pickupSecondSpikeA),
                intakeSpike(pickupSecondSpikeB, 0.25),

                new ParallelRaceGroup(
                        new FollowPath(dumpGate),
                        new Delay(1.5)
                ),
                new Delay(0.5),

                toShootSpike(shootSecondSpike),
                //pickupAndShootSpike(redPickupSecondSpikeB, redShootSecondSpike),
                shoot(),

                new FollowPath(pickupFirstSpikeA),
                pickupAndShootSpike(pickupFirstSpikeB, shootFirstSpike),
                shoot(),

                new FollowPath(pickupThirdSpikeA),
                pickupAndShootSpike(pickupThirdSpikeB, shootThirdSpike),
                shoot(),
                new ParallelGroup(
                        new FollowPath(parkPath),
                        FlywheelSubsystem.INSTANCE.spinDown(),
                        IntakeSubsystem.INSTANCE.disable)
        ).schedule();
    }

    public void farAuto () {

        new SequentialGroup(
                new ParallelGroup(
                        FlywheelSubsystem.INSTANCE.spinFlywheels(() -> {
                            return farGoalFlywheelSpeed;
                        }),
                        TriggerSubsystem.INSTANCE.open,
                        new FollowPath(startToFirstShoot)
                ),
                new Delay(0.2),
                shoot(),

                new FollowPath(pickupThirdSpikeA),
                pickupAndShootSpike(pickupThirdSpikeB, shootThirdSpike),
                shoot(),

                pickupAndShootSpikeWithDelay(pickupHumanSpike, shootHumanSpike, 5),
                shoot(),

                new ParallelGroup(
                        new FollowPath(parkPath),
                        FlywheelSubsystem.INSTANCE.spinDown(),
                        IntakeSubsystem.INSTANCE.disable)

                ).schedule();
                /*
                new FollowPath(pickupSecondSpikeA),
                intakeSpike(pickupSecondSpikeB),

                new ParallelRaceGroup(
                        new FollowPath(dumpGate),
                        new Delay(1.5)
                ),
                new Delay(0.2),

                toShootSpike(shootSecondSpike),
                shoot(),


                new FollowPath(pickupFirstSpikeA),
                pickupAndShootSpike(pickupFirstSpikeB, shootFirstSpike),
                shoot(),

                new FollowPath(pickupThirdSpikeA),
                pickupAndShootSpike(pickupThirdSpikeB, shootThirdSpike),
                shoot(),
                new ParallelGroup(
                        new FollowPath(parkPath),
                        FlywheelSubsystem.INSTANCE.spinDown(),
                        IntakeSubsystem.INSTANCE.disable)
        ).schedule();

        new SequentialGroup(
            waitBeforeShooting ? new Delay(20) : new NullCommand(),
                new ParallelGroup(
                        FlywheelSubsystem.INSTANCE.spinFlywheels(() -> {
                            return farGoalFlywheelSpeed;
                        }),
                        TriggerSubsystem.INSTANCE.open,
                        new FollowPath(startToFirstShoot)
                ),
                new Delay(0.2),
                shoot(),
                waitBeforeParking ? new Delay(5) : new NullCommand(),
                new ParallelGroup(
                        new FollowPath(parkPath),
                        FlywheelSubsystem.INSTANCE.spinDown(),
                        IntakeSubsystem.INSTANCE.disable)
        ).schedule();

         */
    }

    @Override public void onUpdate() {
        Pose currentPose = follower().getPose();
        if(currentPose.getY() != 0 && currentPose.getX() != 0) {
            OpalStatic.currentPose = follower().getPose();
        }
    }

    @Override public void onStop() {
        FlywheelSubsystem.INSTANCE.spinDown().run();
        IntakeSubsystem.INSTANCE.disable.run();
        TriggerSubsystem.INSTANCE.close.run();
    }


}
