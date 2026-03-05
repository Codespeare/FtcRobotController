package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Red Far", group = "Red Auto",preselectTeleOp = "Elsi")
public class ElsiAutoLowerRight extends ElsiAutoBase {
    @Override
    public void init() {
        teamColor.setRed();
        teamColor.setLower();
        super.init();
    }
}
