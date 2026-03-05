package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Blue Close", group = "Blue Auto", preselectTeleOp = "Elsi")
public class ElsiAutoUpperLeft extends ElsiAutoBase {
    @Override
    public void init() {
        teamColor.setBlue();
        teamColor.setUpper();
        super.init();
    }
}
