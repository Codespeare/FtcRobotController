package elsi.archive;

/*
@TeleOp(name = "elsi.archive.Elsi3Teleop", group = "TeleOp")
@Disabled
public class Elsi3Teleop extends NextFTCOpMode {
    {
        addComponents(
                //new SubsystemComponent(AutoTargetSubsystem.INSTANCE),
                //new SubsystemComponent(ParkSubsystem.INSTANCE),
                //new SubsystemComponent(FlywheelSubsystem.INSTANCE),
                //new SubsystemComponent(IntakeSubsystem.INSTANCE),
                //new SubsystemComponent(TriggerSubsystem.INSTANCE),
                //new SubsystemComponent(MagazineSubsystem.INSTANCE),
                //new PedroComponent(Constants::createFollower),
                BulkReadComponent.INSTANCE,
                BindingsComponent.INSTANCE
        );

    }

    @Override public void onInit() {
        TriggerSubsystem.INSTANCE.close.run();
        follower().setStartingPose(Elsi3Static.currentPose);


        // Wait 120 seconds (2 minutes) then stop the OpMode
        /*
        new SequentialGroup(new Delay(128),
                new InstantCommand(ActiveOpMode::terminateOpModeNow)
        ).run();

    }

    @Override public void onWaitForStart() {
        ActiveOpMode.telemetry().addData("Start Pose", Elsi3Static.currentPose);
        ActiveOpMode.telemetry().update();
    }
    @Override public void onStartButtonPressed() {
        ActiveOpMode.telemetry().setAutoClear(true);

        DriverControlledCommand driverControlled = new PedroDriverControlled(
                Gamepads.gamepad1().leftStickY().negate().deadZone(0.05).map(this::cube),
                Gamepads.gamepad1().leftStickX().negate().deadZone(0.05).map(this::cube),
                Gamepads.gamepad1().rightStickX().negate().deadZone(0.05).map(this::cube)
        );

        driverControlled.schedule();

        follower().startTeleopDrive(true);

        Gamepads.gamepad1().rightTrigger().atLeast(0.8)
                .whenBecomesTrue(new SequentialGroup(
                        new ParallelGroup(
                            AutoTargetSubsystem.INSTANCE.beginAutoTarget,
                            FlywheelSubsystem.INSTANCE.spinFlywheelsByDistance(() -> {
                                return getRangeToGoal(follower().getPose(), Elsi3Static.goalPose);}) //TODO: Replace with real target velocity
                        ),
                        IntakeSubsystem.INSTANCE.enable,
                        TriggerSubsystem.INSTANCE.open
                        )
                        .setInterruptible(true)
                        .addRequirements(AutoTargetSubsystem.INSTANCE, FlywheelSubsystem.INSTANCE, IntakeSubsystem.INSTANCE, TriggerSubsystem.INSTANCE)
                )
                .whenBecomesFalse(
                        new ParallelGroup(
                                FlywheelSubsystem.INSTANCE.spinDown(),
                                AutoTargetSubsystem.INSTANCE.endAutoTarget,
                                IntakeSubsystem.INSTANCE.disable,
                                TriggerSubsystem.INSTANCE.close)
                                .setInterruptible(true)
                                .addRequirements(AutoTargetSubsystem.INSTANCE, FlywheelSubsystem.INSTANCE, IntakeSubsystem.INSTANCE, TriggerSubsystem.INSTANCE)

                )
                .whenFalse(autoIntake());                ;

        Gamepads.gamepad1().leftTrigger().atLeast(0.8)
                .whenBecomesTrue(ParkSubsystem.INSTANCE.beginParkMode)
                .whenBecomesFalse(ParkSubsystem.INSTANCE.endParkMode);

    }

    private Command autoIntake () {
        return new IfElseCommand(
                MagazineSubsystem.INSTANCE::getBallDetected,
                IntakeSubsystem.INSTANCE.disable,
                IntakeSubsystem.INSTANCE.enable
        )
        .setInterruptible(true)
        .addRequirements(MagazineSubsystem.INSTANCE, IntakeSubsystem.INSTANCE)
        ;
    }

    public static double getRangeToGoal (Pose current, Pose goal) {
        double x2 = goal.getX();
        double y2 = goal.getY();
        double x1 = current.getX();
        double y1 = current.getY();

        return Math.hypot(x2 - x1, y2 - y1);
    }

    @Override public void onUpdate() {
        ActiveOpMode.telemetry().addData("Start Pose", Elsi3Static.currentPose);
        ActiveOpMode.telemetry().addData("Start Angle", Math.toDegrees(Elsi3Static.currentPose.getHeading()));
        ActiveOpMode.telemetry().addData("Goal Pose", Elsi3Static.goalPose);
        ActiveOpMode.telemetry().addData("Target Pose", AutoTargetSubsystem.targetPose);
        ActiveOpMode.telemetry().addData("Current Pose", follower().getPose());
        //ActiveOpMode.telemetry().addData("Target Angle", Math.toDegrees(targetPose.getHeading()));

        ActiveOpMode.telemetry().update();

    }

    @Override public void onStop() {
        FlywheelSubsystem.INSTANCE.spinDown().run();
        IntakeSubsystem.INSTANCE.disable.run();
        TriggerSubsystem.INSTANCE.close.run();
    }

    private Double cube (Double value) {
        return value * value * value;
    }
}
*/