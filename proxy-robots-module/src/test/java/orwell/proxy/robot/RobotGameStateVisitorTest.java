package orwell.proxy.robot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.EnumGameState;
import orwell.proxy.mock.MockedTank;

import static org.junit.Assert.assertEquals;

/**
 * Created by Michaël Ludmann on 6/5/15.
 */
@RunWith(JUnit4.class)
public class RobotGameStateVisitorTest {
    private final static Logger logback = LoggerFactory.getLogger(RobotGameStateVisitorTest.class);
    private final static String WINNING_TEAM = "RED";
    private RobotGameStateVisitor gameStateVisitor;
    private RobotsMap robotsMap;
    private MockedTank mockedTank;

    @Before
    public void setUp() throws Exception {
        logback.debug(">>>>>>>>> IN");
        robotsMap = new RobotsMap();
        mockedTank = new MockedTank();
        // A robot must be registered to see its VictoryState change
        mockedTank.setRegistrationState(EnumRegistrationState.REGISTERED);
        robotsMap.add(mockedTank);
    }

    @Test
    public void testVisit_WaitingToStart() throws Exception {
        gameStateVisitor = new RobotGameStateVisitor(
                EnumGameState.WAITING_TO_START, WINNING_TEAM
        );
        gameStateVisitor.visit(robotsMap);

        assertEquals(EnumRobotVictoryState.WAITING_FOR_START, mockedTank.getVictoryState());
    }

    @Test
    public void testVisit_Playing() throws Exception {
        gameStateVisitor = new RobotGameStateVisitor(EnumGameState.PLAYING);
        gameStateVisitor.visit(robotsMap);

        assertEquals(EnumRobotVictoryState.PLAYING, mockedTank.getVictoryState());
    }

    @Test
    public void testVisit_Finished_Draw() throws Exception {
        gameStateVisitor = new RobotGameStateVisitor(EnumGameState.FINISHED);
        gameStateVisitor.visit(robotsMap);

        assertEquals(EnumRobotVictoryState.DRAW, mockedTank.getVictoryState());
    }

    @Test
    public void testVisit_Finished_Victory() throws Exception {
        gameStateVisitor = new RobotGameStateVisitor(
                EnumGameState.FINISHED, mockedTank.getTeamName()
        );
        gameStateVisitor.visit(robotsMap);

        assertEquals(EnumRobotVictoryState.WINNER, mockedTank.getVictoryState());
    }

    @Test
    public void testVisit_Finished_Defeat() throws Exception {
        gameStateVisitor = new RobotGameStateVisitor(
                EnumGameState.FINISHED, WINNING_TEAM
        );
        gameStateVisitor.visit(robotsMap);

        assertEquals(EnumRobotVictoryState.DEFEATED, mockedTank.getVictoryState());
    }

    @Test
    public void testVisit_Undefined() throws Exception {
        gameStateVisitor = new RobotGameStateVisitor(
                EnumGameState.UNDEFINED, WINNING_TEAM
        );
        gameStateVisitor.visit(robotsMap);

        assertEquals(EnumRobotVictoryState.WAITING_FOR_START, mockedTank.getVictoryState());
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}

