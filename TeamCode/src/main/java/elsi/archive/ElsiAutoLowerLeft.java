package elsi.archive;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Autonomous(name = "Blue Far", group = "Blue Auto",preselectTeleOp = "Elsi")
@Disabled
public class ElsiAutoLowerLeft extends ElsiAutoBase {
    @Override
    public void init() {
        teamColor.setBlue();
        teamColor.setLower();
        super.init();
    }
}
