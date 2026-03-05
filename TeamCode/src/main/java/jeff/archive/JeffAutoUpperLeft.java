package jeff.archive;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Autonomous(name = "Jeff Auto Upper Blue", group = "Prototype",preselectTeleOp = "Jeff")
@Disabled
public class JeffAutoUpperLeft extends JeffAuto{
    @Override
    public void init() {
        teamColor.setBlue();
        teamColor.setUpper();
        super.init();
    }
}
