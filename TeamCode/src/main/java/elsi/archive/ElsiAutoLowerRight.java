package elsi.archive;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Autonomous(name = "Red Far", group = "Red Auto",preselectTeleOp = "Elsi")
@Disabled
public class ElsiAutoLowerRight extends ElsiAutoBase {
    @Override
    public void init() {
        teamColor.setRed();
        teamColor.setLower();
        super.init();
    }
}
