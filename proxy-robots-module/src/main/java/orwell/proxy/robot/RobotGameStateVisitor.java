package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.EnumGameState;

/**
 * Created by Michaël Ludmann on 6/4/15.
 */
public class RobotGameStateVisitor {
    private final static Logger logback = LoggerFactory.getLogger(RobotGameStateVisitor.class);
    private final static String VICTORY_PAYLOAD_HEADER = "game vict ";
    private final static String DEFEAT_PAYLOAD_HEADER = "game fail ";
    private final static String DRAW_PAYLOAD_HEADER = "game draw ";
    private final String winningTeam;
    private final EnumGameState gameState;

    public RobotGameStateVisitor(final EnumGameState gameState,
                                 final String winningTeam) {
        this.gameState = gameState;
        this.winningTeam = winningTeam;
    }

    public RobotGameStateVisitor(final EnumGameState gameState) {
        this(gameState, null);
    }

    public void visit(final RobotsMap robotsMap) throws MessageNotSentException {
        for (final IRobot robot : robotsMap.getRegisteredRobots()) {
            setGameState(robot);
        }
    }

    private void setGameState(final IRobot robot) throws MessageNotSentException {
        switch (gameState) {
            case WAITING_TO_START:
                setWaitingToStart(robot);
                break;
            case PLAYING:
                setPlaying(robot);
                break;
            case FINISHED:
                setFinished(robot);
                break;
            case UNDEFINED:
                setUndefined(robot);
                break;
        }
    }

    private void setFinished(final IRobot robot) throws MessageNotSentException {
        if (null == winningTeam || winningTeam.equals("")) {
            setDraw(robot);
        } else if (winningTeam.equals(robot.getTeamName())) {
            setVictory(robot);
        } else {
            setDefeat(robot);
        }
    }

    private void setUndefined(final IRobot robot) {
        logback.warn("Undefined victory state for robot " + robot.getRoutingId() + ". Ignored.");
    }

    private void setDraw(final IRobot robot) throws MessageNotSentException {
        robot.setVictoryState(EnumRobotVictoryState.DRAW);
        robot.sendUnitMessage(new UnitMessage(UnitMessageType.Command, DRAW_PAYLOAD_HEADER));
        logback.info("Draw info sent to robot " + robot.getRoutingId());
    }

    private void setPlaying(final IRobot robot) {
        robot.setVictoryState(EnumRobotVictoryState.PLAYING);
    }

    private void setWaitingToStart(final IRobot robot) {
        robot.setVictoryState(EnumRobotVictoryState.WAITING_FOR_START);
        logback.info("Robot " + robot.getRoutingId() + " is waiting for the game to start");
    }

    private void setVictory(final IRobot robot) throws MessageNotSentException {
        robot.setVictoryState(EnumRobotVictoryState.WINNER);
        robot.sendUnitMessage(new UnitMessage(UnitMessageType.Command, VICTORY_PAYLOAD_HEADER));
        logback.info("Victory info sent to robot " + robot.getRoutingId());
    }

    private void setDefeat(final IRobot robot) throws MessageNotSentException {
        robot.setVictoryState(EnumRobotVictoryState.DEFEATED);
        robot.sendUnitMessage(new UnitMessage(UnitMessageType.Command, DEFEAT_PAYLOAD_HEADER));
        logback.info("Defeat info sent to robot " + robot.getRoutingId());
    }
}
