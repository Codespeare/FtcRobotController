package jeff.archive;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Autonomous(name = "Jeff Auto Lower Blue", group = "Prototype",preselectTeleOp = "Jeff")
@Disabled
public class JeffAutoLowerLeft extends JeffAuto {
    @Override
    public void init() {
        teamColor.setBlue();
        teamColor.setLower();
        super.init();
    }
}
