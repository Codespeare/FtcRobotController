import static dev.nextftc.extensions.pedro.PedroComponent.follower;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import dev.nextftc.core.components.BindingsComponent;
import dev.nextftc.core.components.SubsystemComponent;
import dev.nextftc.extensions.pedro.PedroComponent;
import dev.nextftc.extensions.pedro.PedroDriverControlled;
import dev.nextftc.ftc.Gamepads;
import dev.nextftc.ftc.NextFTCOpMode;
import dev.nextftc.ftc.components.BulkReadComponent;
import dev.nextftc.hardware.driving.DriverControlledCommand;


@TeleOp(name = "Elsi3Teleop", group = "TeleOp")
public class ElsiPrototype3 extends NextFTCOpMode {
    {
        addComponents(
                new SubsystemComponent(AutoTargetSubsystem.INSTANCE),
                new SubsystemComponent(ParkSubsystem.INSTANCE),
                new PedroComponent(Constants::createFollower),
                BulkReadComponent.INSTANCE,
                BindingsComponent.INSTANCE
        );

    }

    @Override public void onInit() { }
    @Override public void onWaitForStart() { }
    @Override public void onStartButtonPressed() {
        //TODO: Replace with real starting poses
        follower().setStartingPose(new Pose(72,72,Math.toRadians(90)));

        DriverControlledCommand driverControlled = new PedroDriverControlled(
                Gamepads.gamepad1().leftStickY().negate().deadZone(0.05).map(this::cube),
                Gamepads.gamepad1().leftStickX().negate().deadZone(0.05).map(this::cube),
                Gamepads.gamepad1().rightStickX().negate().deadZone(0.05).map(this::cube)
        );

        driverControlled.schedule();

        follower().startTeleopDrive(true);

        /*
        Gamepads.gamepad2().dpadUp()
                .whenBecomesTrue(Lift.INSTANCE.toHigh);

         */

        Gamepads.gamepad1().rightTrigger().atLeast(0.8)
                .whenBecomesTrue(AutoTargetSubsystem.INSTANCE.beginAutoTarget)
                .whenBecomesFalse(AutoTargetSubsystem.INSTANCE.endAutoTarget);

        Gamepads.gamepad1().leftTrigger().atLeast(0.8)
                .whenBecomesTrue(ParkSubsystem.INSTANCE.beginParkMode)
                .whenBecomesFalse(ParkSubsystem.INSTANCE.endParkMode);
    }

    @Override public void onUpdate() { }
    @Override public void onStop() { }

    private Double cube (Double value) {
        return value * value * value;
    }
}