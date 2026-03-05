package org.firstinspires.ftc.teamcode;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Practice, Red Near Goal", group = "Practice")
public class ElsiPracticeTeleopUpperRed extends ElsiTeleop {

    //Upper Red Starting Pose
    private final Pose redNearStartingPose = ElsiAutoBase.upperStartPose.mirror();

    @Override
    public void init() {
        teamColor.setRed();
        teamColor.setUpper();

        //ElsiDrivetrain.init(hardwareMap);
        ElsiDrivetrain.setStartingPose(redNearStartingPose);

        super.init();
    }
}
