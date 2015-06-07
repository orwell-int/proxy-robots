package orwell.proxy;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.ServerGame;
import orwell.proxy.robot.RobotGameStateVisitor;

/**
 * Created by Michaël Ludmann on 6/4/15.
 */
public class GameState {
    private final static Logger logback = LoggerFactory.getLogger(GameState.class);
    private ServerGame.GameState serverGameGameState;

    public GameState(final byte[] gameStateMessage) {
        try {
            this.serverGameGameState = ServerGame.GameState.parseFrom(gameStateMessage);
            if (null != serverGameGameState && serverGameGameState.hasWinner()) {
                logback.debug(">>>>>>> We have a winner: " + serverGameGameState.getWinner());
            }
        } catch (final InvalidProtocolBufferException e) {
            logback.error("setGameState protobuf exception: " + e.getMessage());
        }
    }

    public EnumGameState getEnumGameState() {
        if (isGameOnGoing())
            return EnumGameState.PLAYING;
        if (isGameFinished())
            return EnumGameState.FINISHED;
        if (isGameWaitingToStart())
            return EnumGameState.WAITING_TO_START;
        else // Parsing error for instance
            return EnumGameState.UNDEFINED;
    }

    /**
     * @return playing == true
     */
    protected boolean isGameOnGoing() {
        return (null != serverGameGameState &&
                serverGameGameState.getPlaying());
    }

    /**
     * @return playing == false && has winner
     */
    protected boolean isGameFinished() {
        return (null != serverGameGameState &&
                !serverGameGameState.getPlaying() &&
                serverGameGameState.hasWinner());
    }

    /**
     * @return playing == false && has no winner
     */
    private boolean isGameWaitingToStart() {
        return (null != serverGameGameState &&
                !serverGameGameState.getPlaying() &&
                !serverGameGameState.hasWinner());
    }

    protected String getWinningTeam() {
        if (!isGameFinished()) {
            return null;
        }
        return serverGameGameState.getWinner();
    }

    public RobotGameStateVisitor getRobotGameStateVisitor() {
        if (null == serverGameGameState) {
            return null;
        }
        return new RobotGameStateVisitor(getEnumGameState(), getWinningTeam());
    }
}
