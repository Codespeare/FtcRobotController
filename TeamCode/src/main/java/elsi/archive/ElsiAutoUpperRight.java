package elsi.archive;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Autonomous(name = "Red Close", group = "Red Auto",preselectTeleOp = "Elsi")
@Disabled
public class ElsiAutoUpperRight extends ElsiAutoBase {
    @Override
    public void init() {
        teamColor.setRed();
        teamColor.setUpper();
        super.init();
    }
}


