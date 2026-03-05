package org.firstinspires.ftc.teamcode;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Practice, Red, Away From Goal", group = "Practice")
public class ElsiPracticeTeleopLowerRed extends ElsiTeleop {

    //Upper Red Starting Pose
    private final Pose redFarStartingPose = new Pose(96, 8.2, Math.toRadians(90));

    @Override
    public void init() {
        teamColor.setRed();
        teamColor.setLower();

        //ElsiDrivetrain.init(hardwareMap);
        ElsiDrivetrain.setStartingPose(redFarStartingPose);

        super.init();
    }
}
