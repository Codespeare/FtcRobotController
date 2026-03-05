package org.firstinspires.ftc.teamcode;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Practice, Blue, Away From Goal", group = "Practice")
public class ElsiPracticeTeleopLowerBlue extends ElsiTeleop {

    //Upper Red Starting Pose
    private final Pose blueFarStartingPose = new Pose(48, 8.2, Math.toRadians(90));

    @Override
    public void init() {
        teamColor.setBlue();
        teamColor.setLower();

        //ElsiDrivetrain.init(hardwareMap);
        ElsiDrivetrain.setStartingPose(blueFarStartingPose);

        super.init();
    }
}
