package jeff.archive;

import com.bylazar.configurables.annotations.Configurable;

@Configurable
public class RobotVariables {

    public static volatile double TargetVelocity = 3000;

    public static volatile PIDF topPIDF = new PIDF();
    public static volatile PIDF bottomPIDF = new PIDF();

}

