package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Blue Far", group = "Blue Auto",preselectTeleOp = "Elsi")
public class ElsiAutoLowerLeft extends ElsiAutoBase {
    @Override
    public void init() {
        teamColor.setBlue();
        teamColor.setLower();
        super.init();
    }
}
