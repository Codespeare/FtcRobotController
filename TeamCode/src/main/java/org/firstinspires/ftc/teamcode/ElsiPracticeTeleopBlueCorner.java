package org.firstinspires.ftc.teamcode;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Practice, Blue, Corner", group = "Practice")
public class ElsiPracticeTeleopBlueCorner extends ElsiTeleop {

    //Upper Red Starting Pose
    private final Pose blueCornerStartingPose = new Pose(8.5, 8.5, Math.toRadians(90));

    @Override
    public void init() {
        teamColor.setBlue();
        teamColor.setLower();

        //ElsiDrivetrain.init(hardwareMap);
        ElsiDrivetrain.setStartingPose(blueCornerStartingPose);

        super.init();
    }
}
