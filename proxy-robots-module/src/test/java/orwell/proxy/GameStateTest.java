package orwell.proxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.ServerGame;

import static org.junit.Assert.*;

/**
 * Created by Michaël Ludmann on 6/5/15.
 */
@RunWith(JUnit4.class)
public class GameStateTest {
    private final static Logger logback = LoggerFactory.getLogger(GameStateTest.class);
    private GameState gameState;

    @Before
    public void setUp() throws Exception {
        logback.debug(">>>>>>>>> IN");

    }

    @Test
    public void testIsGameOnGoing_True() throws Exception {
        // Create GameState from a message stating the game is on
        gameState = new GameState(ProtobufTest.getTestGameState_Playing().toByteArray());
        assertTrue(gameState.isGameOnGoing());
    }

    @Test
    public void testIsGameOnGoing_False() throws Exception {
        // Create GameState from a message stating the game has a winner (so it is over)
        gameState = new GameState(ProtobufTest.getTestGameState_Winner().toByteArray());
        assertFalse(gameState.isGameOnGoing());
    }

    @Test
    public void testIsGameFinished_False() throws Exception {
        // Create GameState from a message stating the game is on
        gameState = new GameState(ProtobufTest.getTestGameState_Playing().toByteArray());
        assertFalse(gameState.isGameFinished());
    }

    @Test
    public void testIsGameFinished_True() throws Exception {
        // Create GameState from a message stating the game has a winner (so it is over)
        gameState = new GameState(ProtobufTest.getTestGameState_Winner().toByteArray());
        assertTrue(gameState.isGameFinished());
    }

    @Test
    public void testGetWinningTeam_NotNull() throws Exception {
        // Create GameState from a message stating the game has a winner (so it is over)
        gameState = new GameState(ProtobufTest.getTestGameState_Winner().toByteArray());
        assertNotNull(gameState.getWinningTeam());
        assertTrue(gameState.getWinningTeam().equals(ProtobufTest.GAME_STATE_WINNER_STRING));
    }

    @Test
    public void testGetWinningTeam_Null() throws Exception {
        // Create GameState from a message stating the game is on
        gameState = new GameState(ProtobufTest.getTestGameState_Playing().toByteArray());
        assertNull(gameState.getWinningTeam());
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}
