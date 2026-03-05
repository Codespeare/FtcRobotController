package jeff.archive;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Autonomous(name = "Jeff Auto Lower Red", group = "Prototype",preselectTeleOp = "Jeff")
@Disabled
public class JeffAutoLowerRight extends JeffAuto {
    @Override
    public void init() {
        teamColor.setRed();
        teamColor.setLower();
        super.init();
    }
}
