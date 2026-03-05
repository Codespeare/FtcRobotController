package jeff.archive;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Autonomous(name = "Jeff Auto Upper Red", group = "Prototype",preselectTeleOp = "Jeff")
@Disabled
public class JeffAutoUpperRight extends  JeffAuto {
    @Override
    public void init() {
        teamColor.setRed();
        teamColor.setUpper();
        super.init();
    }
}


