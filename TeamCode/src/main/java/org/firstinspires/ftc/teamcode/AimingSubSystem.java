package org.firstinspires.ftc.teamcode;

import static dev.nextftc.extensions.pedro.PedroComponent.follower;

import com.pedropathing.geometry.Pose;

import dev.nextftc.core.commands.Command;
import dev.nextftc.core.commands.utility.LambdaCommand;
import dev.nextftc.core.subsystems.Subsystem;

public class AimingSubSystem implements Subsystem {
    public static final AimingSubSystem INSTANCE = new AimingSubSystem();
    private AimingSubSystem() {}

    private final Pose goal = new Pose(140, 140);



    public Command activateAim = new LambdaCommand()
            .setStart(() -> {Pose current = follower().getPose();
                double angle = getTargetAngle(current,goal);
                follower().turnTo(angle);
                });

    public Command deactivateAim = new LambdaCommand()
            .setStart(() -> {follower().startTeleopDrive(true);});

    private double getTargetAngle (Pose current, Pose goal) {
        //Returns Radians
        double dy = goal.getY() - current.getY();
        double dx = goal.getX() - current.getX();
        double angle = Math.atan2(dy, dx);

        //angle -= Math.PI / 2.0;

        //angle = normalizeAngle(angle);
        //telemetry.addData("Computed Target Angle (Rad)", angle);
        return angle;
        //return normalizeAngle(Math.atan2(dy, dx));
    }

}
