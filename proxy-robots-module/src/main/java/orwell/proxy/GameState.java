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
        } catch (final InvalidProtocolBufferException e) {
            logback.error("setGameState protobuf exception: " + e.getMessage());
        }
    }

    public EnumGameState getEnumGameState() {
        if (isGameOnGoing())
            return EnumGameState.PLAYING;
        if (isGameFinished())
            return EnumGameState.FINISHED;
        return EnumGameState.WAITING_TO_START;
    }

    public boolean isGameOnGoing() {
        return (null != serverGameGameState &&
                serverGameGameState.getPlaying() &&
                0 < serverGameGameState.getSeconds());
    }

    public boolean isGameFinished() {
        return (null != serverGameGameState &&
                0 == serverGameGameState.getSeconds() && ! isGameOnGoing());
    }

    public String getWinningTeam() {
        if (! isGameFinished()) {
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
