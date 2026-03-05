package org.firstinspires.ftc.teamcode;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Practice, Blue, Near Goal", group = "Practice")
public class ElsiPracticeTeleopUpperBlue extends ElsiTeleop {

    //Upper Red Starting Pose
    private final Pose blueNearStartingPose = ElsiAutoBase.upperStartPose;


    @Override
    public void init() {
        teamColor.setBlue();
        teamColor.setUpper();

        //ElsiDrivetrain.init(hardwareMap);
        ElsiDrivetrain.setStartingPose(blueNearStartingPose);

        super.init();
    }
}
