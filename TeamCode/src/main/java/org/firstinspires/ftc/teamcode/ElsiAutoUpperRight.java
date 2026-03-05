package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Red Close", group = "Red Auto",preselectTeleOp = "Elsi")
public class ElsiAutoUpperRight extends ElsiAutoBase {
    @Override
    public void init() {
        teamColor.setRed();
        teamColor.setUpper();
        super.init();
    }
}


