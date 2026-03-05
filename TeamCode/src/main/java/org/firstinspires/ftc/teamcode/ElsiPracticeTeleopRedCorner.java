package org.firstinspires.ftc.teamcode;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Practice, Red, Corner", group = "Practice")
public class ElsiPracticeTeleopRedCorner extends ElsiTeleop {

    //Upper Red Starting Pose
    private final Pose redCornerStartingPose = new Pose(135.5, 8.5, Math.toRadians(90));

    @Override
    public void init() {
        teamColor.setRed();
        teamColor.setLower();

        //ElsiDrivetrain.init(hardwareMap);
        ElsiDrivetrain.setStartingPose(redCornerStartingPose);

        super.init();
    }
}
