import com.pedropathing.geometry.Pose;

import dev.nextftc.core.commands.utility.LambdaCommand;


enum TeamColor {red, blue}

enum StartingPosition {close, far}

public class OpalStatic {
    public static TeamColor teamColor = TeamColor.red;
    public static StartingPosition startingPosition = StartingPosition.close;

    public static Pose currentPose = (teamColor == TeamColor.red)? new Pose(111.8,136, Math.toRadians(270)) : new Pose(111.8,136, Math.toRadians(270)).mirror();

    //public static Pose goalPose = (teamColor == TeamColor.red)? new Pose(135,135) : new Pose(135,135).mirror();
    public static Pose redGoalPose = new Pose(135,135);

    public static Pose currentGoalPose () {
        if(teamColor == TeamColor.red) return redGoalPose;
        return redGoalPose.mirror();
    }

    public static LambdaCommand swapTeamColor = new LambdaCommand()
            .setStart(() -> {
                if(teamColor == TeamColor.red) teamColor = TeamColor.blue;
                else teamColor = TeamColor.red;
            })
            .setIsDone(() -> true);

}
