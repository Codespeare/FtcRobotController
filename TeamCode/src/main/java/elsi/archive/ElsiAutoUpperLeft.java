package elsi.archive;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Autonomous(name = "Blue Close", group = "Blue Auto", preselectTeleOp = "Elsi")
@Disabled
public class ElsiAutoUpperLeft extends ElsiAutoBase {
    @Override
    public void init() {
        teamColor.setBlue();
        teamColor.setUpper();
        super.init();
    }
}
