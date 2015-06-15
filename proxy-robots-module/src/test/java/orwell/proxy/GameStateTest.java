package orwell.proxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Test
    public void testGetRobotGameStateVisitor_NotNull() throws Exception {
        // Create GameState from a message stating the game is on
        gameState = new GameState(ProtobufTest.getTestGameState_Playing().toByteArray());
        assertNotNull(gameState.getRobotGameStateVisitor());
    }

    @Test
    public void testGetRobotGameStateVisitor_EmptyParameter() throws Exception {
        // Create GameState from bad byte[]
        gameState = new GameState(new byte[]{});
        assertNotNull(gameState.getRobotGameStateVisitor());
        assertEquals(EnumGameState.UNDEFINED, gameState.getEnumGameState());
        assertNull(gameState.getWinningTeam());
    }

    @Test
    public void testGetEnumGameState_WaitingToStart() throws Exception {
        // Create GameState from a message stating the game has not started yet
        gameState = new GameState(ProtobufTest.getTestGameState_WaitingForStart().toByteArray());
        assertEquals(EnumGameState.WAITING_TO_START, gameState.getEnumGameState());
    }

    @Test
    public void testGetEnumGameState_Playing() throws Exception {
        // Create GameState from a message stating the game is on
        gameState = new GameState(ProtobufTest.getTestGameState_Playing().toByteArray());
        assertEquals(EnumGameState.PLAYING, gameState.getEnumGameState());
    }

    @Test
    public void testGetEnumGameState_Finished() throws Exception {
        // Create GameState from a message stating the game has a winner (so it is over)
        gameState = new GameState(ProtobufTest.getTestGameState_Winner().toByteArray());
        assertEquals(EnumGameState.FINISHED, gameState.getEnumGameState());
    }

    @Test
    public void testGetEnumGameState_Undefined() throws Exception {
        // Create GameState from bad byte[]
        gameState = new GameState(new byte[]{});
        assertEquals(EnumGameState.UNDEFINED, gameState.getEnumGameState());
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}
