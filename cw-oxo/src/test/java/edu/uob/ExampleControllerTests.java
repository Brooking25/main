package edu.uob;

import edu.uob.OXOMoveException.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ExampleControllerTests {
  private OXOModel model;
  private OXOController controller;

  // Make a new "standard" (3x3) board before running each test case (i.e. this method runs before every `@Test` method)
  // In order to test boards of different sizes, winning thresholds or number of players, create a separate test file (without this method in it !)
  @BeforeEach
  void setup() {
    model = new OXOModel(3, 3, 3);
    model.addPlayer(new OXOPlayer('X'));
    model.addPlayer(new OXOPlayer('O'));
    controller = new OXOController(model);
  }

  // This next method is a utility function that can be used by any of the test methods to _safely_ send a command to the controller
  void sendCommandToController(String command) {
      // Try to send a command to the server - call will timeout if it takes too long (in case the server enters an infinite loop)
      // Note: this is ugly code and includes syntax that you haven't encountered yet
      String timeoutComment = "Controller took too long to respond (probably stuck in an infinite loop)";
      assertTimeoutPreemptively(Duration.ofMillis(1000), ()-> controller.handleIncomingCommand(command), timeoutComment);
  }

  // Test simple move taking and cell claiming functionality
  @Test
  void testBasicMoveTaking() throws OXOMoveException {
    // Find out which player is going to make the first move
    OXOPlayer firstMovingPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());
    // Make a move
    sendCommandToController("a1");
    // Check that A1 (cell [0,0] on the board) is now "owned" by the first player
    String failedTestComment = "Cell a1 wasn't claimed by the first player";
    assertEquals(firstMovingPlayer, controller.gameModel.getCellOwner(0, 0), failedTestComment);
    // Check that the current player changes with each valid move
    String failedPlayerSwitch = "Current player did not switch";
    assertNotEquals(controller.gameModel.getCellOwner(0, 0), model.getPlayerByNumber(model.getCurrentPlayerNumber()), failedPlayerSwitch);
    // Check for valid Uppercase row coordinate
    OXOPlayer secondMovingPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());
    sendCommandToController("B1");
    assertEquals(secondMovingPlayer, controller.gameModel.getCellOwner(1, 0), failedTestComment);
  }

  // Test out basic win detection
  @Test
  void testBasicWin() throws OXOMoveException {
    // Find out which player is going to make the first move (they should be the eventual winner)
    OXOPlayer firstMovingPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());
    // Make a bunch of moves for the two players
    sendCommandToController("a1"); // First player
    sendCommandToController("b1"); // Second player
    sendCommandToController("a2"); // First player
    sendCommandToController("b2"); // Second player
    sendCommandToController("a3"); // First player

    // a1, a2, a3 should be a win for the first player (since players alternate between moves)
    // Let's check to see whether the first moving player is indeed the winner
    String failedTestComment = "Winner was expected to be " + firstMovingPlayer.getPlayingLetter() + " but wasn't";
    assertEquals(firstMovingPlayer, model.getWinner(), failedTestComment);
  }

  @Test
  void testDiagWin() throws OXOMoveException {
    // Find out which player is going to make the first move (they should be the eventual winner)
    OXOPlayer firstMovingPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());
    // Make a bunch of moves for the two players
    sendCommandToController("b2"); // First player
    sendCommandToController("b1"); // Second player
    sendCommandToController("a1"); // First player
    sendCommandToController("c1"); // Second player
    sendCommandToController("c3"); // First player

    String failedTestComment = "Winner was expected to be " + firstMovingPlayer.getPlayingLetter() + " but wasn't";
    assertEquals(firstMovingPlayer, model.getWinner(), failedTestComment);
  }

  @Test
  void testAntiDiagWin() throws OXOMoveException {
    // Find out which player is going to make the first move (they should be the eventual winner)
    OXOPlayer firstMovingPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());
    // Make a bunch of moves for the two players
    sendCommandToController("c1"); // First player
    sendCommandToController("c3"); // Second player
    sendCommandToController("b2"); // First player
    sendCommandToController("a1"); // Second player
    sendCommandToController("a3"); // First player

    String failedTestComment = "Winner was expected to be " + firstMovingPlayer.getPlayingLetter() + " but wasn't";
    assertEquals(firstMovingPlayer, model.getWinner(), failedTestComment);
  }

  @Test
  void testDraw() throws OXOMoveException {
    // Find out which player is going to make the first move (they should be the eventual winner)
    OXOPlayer firstMovingPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());
    // Make a bunch of moves for the two players
    sendCommandToController("a1"); // First player
    sendCommandToController("B2"); // Second player
    sendCommandToController("c3"); // First player
    sendCommandToController("B1"); // Second player
    sendCommandToController("b3"); // First player
    sendCommandToController("A3"); // Second player
    sendCommandToController("c1"); // First player
    sendCommandToController("C2"); // Second player
    sendCommandToController("a2"); // First player

    String failedTestComment = "Was meant to be a draw but isn't";
    assertTrue(model.isGameDrawn(), failedTestComment);
  }

  // Example of how to test for the throwing of exceptions
  @Test
  void testInvalidIdentifierException() throws OXOMoveException {
    // Check that the controller throws a suitable exception when it gets an invalid command
    String failedTestComment = "Controller failed to throw an InvalidIdentifierLengthException for command `abc123`";
    // The next line is a bit ugly, but it is the easiest way to test exceptions (soz)
    assertThrows(InvalidIdentifierLengthException.class, ()-> sendCommandToController("abc123"), failedTestComment);
    // Check for commands less than 2 characters
    String failedShortComment = "Controller failed to throw an InvalidIdentifierLengthException for command `a`";
    assertThrows(InvalidIdentifierLengthException.class, ()-> sendCommandToController("a"), failedShortComment);
    // Check for adding extra spaces in commands
    String failedExtraSpaceComment = "Controller failed to throw an InvalidIdentifierLengthException for command `a 3`";
    assertThrows(InvalidIdentifierLengthException.class, ()-> sendCommandToController("a 3"), failedExtraSpaceComment);
    // Check for commands with invalid letters or digits
    String failedSyntaxComment = "Controller failed to throw an InvalidIdentifierLengthException for invalid syntax";
    assertThrows(InvalidIdentifierCharacterException.class, ()-> sendCommandToController("!7"), failedSyntaxComment);
    assertThrows(InvalidIdentifierCharacterException.class, ()-> sendCommandToController("c."), failedSyntaxComment);
    assertThrows(InvalidIdentifierCharacterException.class, ()-> sendCommandToController("13"), failedSyntaxComment);
    assertThrows(InvalidIdentifierCharacterException.class, ()-> sendCommandToController("??"), failedSyntaxComment);
  }

  @Test
  void testOutsideRange() throws OXOMoveException{
    String failedTestComment = "Controller failed to throw an OutsideCellRangeException";
    assertThrows(OutsideCellRangeException.class, ()-> sendCommandToController("d3"), failedTestComment);
    assertThrows(OutsideCellRangeException.class, ()-> sendCommandToController("b4"), failedTestComment);
    assertThrows(OutsideCellRangeException.class, ()-> sendCommandToController("g7"), failedTestComment);
    assertThrows(OutsideCellRangeException.class, ()-> sendCommandToController("a0"), failedTestComment);
  }

  @Test
  void testCellAlreadyTaken() throws OXOMoveException{
    String failedTestComment = "Controller failed to throw a CellAlreadyTakenException";
    sendCommandToController("b2");
    // Check Player 2 cannot claim opponents cell
    assertThrows(CellAlreadyTakenException.class, ()-> sendCommandToController("b2"), failedTestComment);
    sendCommandToController("a1");
    sendCommandToController("c2");
    // Check Player 2 cannot claim a cell they already own
    assertThrows(CellAlreadyTakenException.class, ()-> sendCommandToController("a1"), failedTestComment);
  }
}
