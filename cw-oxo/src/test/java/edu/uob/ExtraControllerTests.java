package edu.uob;

import edu.uob.OXOMoveException.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class ExtraControllerTests {
    private OXOModel model;
    private OXOController controller;

    @BeforeEach
    void setup() {
        model = new OXOModel(9, 9, 5);
        model.addPlayer(new OXOPlayer('X'));
        model.addPlayer(new OXOPlayer('O'));
        model.addPlayer(new OXOPlayer('H'));
        controller = new OXOController(model);
    }

    void sendCommandToController(String command) {
        // Try to send a command to the server - call will timeout if it takes too long (in case the server enters an infinite loop)
        // Note: this is ugly code and includes syntax that you haven't encountered yet
        String timeoutComment = "Controller took too long to respond (probably stuck in an infinite loop)";
        assertTimeoutPreemptively(Duration.ofMillis(1000), ()-> controller.handleIncomingCommand(command), timeoutComment);
    }

    @Test
    void testLowerWinCondition(){
        // Check lower win condition
        controller.gameModel.setWinThreshold(2);
        OXOPlayer firstMovingPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());
        sendCommandToController("e1");
        sendCommandToController("f1");
        sendCommandToController("g1");
        // Player 1 winning move
        sendCommandToController("e2");
        String failedTestComment = "Winner was expected to be " + firstMovingPlayer.getPlayingLetter() + " but wasn't";
        assertEquals(firstMovingPlayer, model.getWinner(), failedTestComment);
    }

    @Test
    void testHigherWinCondition() {
        //Check higher win condition
        controller.gameModel.setWinThreshold(5);
        OXOPlayer firstMovingPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());
        sendCommandToController("e1");
        sendCommandToController("f1");
        sendCommandToController("g1");
        sendCommandToController("e2");
        sendCommandToController("f2");
        sendCommandToController("g2");
        sendCommandToController("e3");
        sendCommandToController("f3");
        sendCommandToController("g3");
        sendCommandToController("e4");
        sendCommandToController("f4");
        sendCommandToController("g4");
        // Player 1 winning move
        sendCommandToController("e5");
        String failedTestComment = "Winner was expected to be " + firstMovingPlayer.getPlayingLetter() + " but wasn't";
        assertEquals(firstMovingPlayer, model.getWinner(), failedTestComment);
    }
    @Test
    void testMultiplePlayers(){
        sendCommandToController("a1");
        sendCommandToController("i9");
        // Check third player can make a move
        OXOPlayer thirdMovingPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());
        String failedTestComment = "Player 3 does not exist";
        assertEquals(thirdMovingPlayer.getPlayingLetter(), 'H', failedTestComment);
        // Check third player can claim a cell
        sendCommandToController("f4");
        String failedClaimComment = "Cell f4 wasn't claimed by the third player";
        assertEquals(thirdMovingPlayer, controller.gameModel.getCellOwner(5, 3), failedClaimComment);
    }
}
