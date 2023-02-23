package edu.uob;
import edu.uob.OXOMoveException.*;
public class OXOController {
    OXOModel gameModel;

    public OXOController(OXOModel model) {
        gameModel = model;
    }

    public void handleIncomingCommand(String command) throws OXOMoveException {
        validateCommand(command);
        if(gameModel.getWinner() != null){
            return;
        }
        int rowNumber = Character.toLowerCase(command.charAt(0)) - 'a';
        int colNumber = Character.getNumericValue(command.charAt(1)) - 1;

        gameModel.setCellOwner(rowNumber, colNumber, gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
        checkWinner(rowNumber, colNumber);

        if(gameModel.getCurrentPlayerNumber() == gameModel.getNumberOfPlayers() - 1){
            gameModel.setCurrentPlayerNumber(0);
        }
        else{
            gameModel.setCurrentPlayerNumber(gameModel.getCurrentPlayerNumber() + 1);
        }
    }
    public void validateCommand(String command) throws OXOMoveException{
        if(command.length() != 2){
            throw new InvalidIdentifierLengthException(command.length());
        }
        if(!Character.isLetter(command.charAt(0))){
            throw new InvalidIdentifierCharacterException(RowOrColumn.ROW, command.charAt(0));
        }
        if(!Character.isDigit(command.charAt(1))){
            throw new InvalidIdentifierCharacterException(RowOrColumn.COLUMN, command.charAt(1));
        }
        int rowNumber = Character.toLowerCase(command.charAt(0)) - 'a';
        int colNumber = Character.getNumericValue(command.charAt(1)) - 1;
        if(rowNumber >= gameModel.getNumberOfRows() || rowNumber < 0){
            throw new OutsideCellRangeException(RowOrColumn.ROW, rowNumber);
        }
        if(colNumber >= gameModel.getNumberOfColumns() || colNumber < 0){
            throw new OutsideCellRangeException(RowOrColumn.COLUMN, colNumber);
        }
        if(gameModel.getCellOwner(rowNumber, colNumber) != null){
            throw new CellAlreadyTakenException(rowNumber, colNumber);
        }
    }
    public void addRow() {
        gameModel.addRow();
    }
    public void removeRow() {
        gameModel.removeRow();
    }
    public void addColumn() {
        gameModel.addColumn();
    }
    public void removeColumn() {
        gameModel.removeColumn();
    }
    public void increaseWinThreshold() {
        gameModel.setWinThreshold(gameModel.getWinThreshold() + 1);
    }
    public void decreaseWinThreshold() {
        gameModel.setWinThreshold(gameModel.getWinThreshold() - 1);
    }
    public void reset() {
        while(gameModel.getNumberOfRows() > 3){
            gameModel.removeRow();
        }
        while(gameModel.getNumberOfColumns() > 3){
            gameModel.removeColumn();
        }
        for(int i = 0; i < gameModel.getNumberOfRows(); i++){
            for(int j = 0; j < gameModel.getNumberOfColumns(); j++){
                gameModel.setCellOwner(i, j, null);
            }
        }
        gameModel.setCurrentPlayerNumber(0);
        gameModel.setWinner(null);
        gameModel.setWinThreshold(3);
    }
    public void checkWinner(int rowNumber, int colNumber) {
        if(checkDraw()){
            gameModel.setGameDrawn();
            return;
        }
        checkRow(colNumber);
        checkCol(rowNumber);
        checkDiagonal(rowNumber, colNumber);
        checkAntiDiagonal(rowNumber, colNumber);
    }
    public boolean checkDraw(){
        for(int x = 0; x < gameModel.getNumberOfRows(); x++){
            for(int y = 0; y < gameModel.getNumberOfColumns(); y++){
                if(gameModel.getCellOwner(x, y) == null){
                    return false;
                }
            }
        }
        return true;
    }
    public void checkCol(int rowNumber){
        int colCount = 0;

        for(int y = 0; y < gameModel.getNumberOfColumns(); y++){
            if(gameModel.getCellOwner(rowNumber, y) != null){
                if(gameModel.getCellOwner(rowNumber, y).getPlayingLetter() == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()).getPlayingLetter()){
                    colCount++;
                }
                else{
                    colCount = 0;
                }
            }
            if(colCount == gameModel.getWinThreshold()){
                gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
            }
        }
    }
    public void checkRow(int colNumber){
        int rowCount = 0;
        for(int x = 0; x < gameModel.getNumberOfRows(); x++){
            if(gameModel.getCellOwner(x, colNumber) != null){
                if(gameModel.getCellOwner(x, colNumber).getPlayingLetter() == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()).getPlayingLetter()){
                    rowCount++;
                }
                else{
                    rowCount = 0;
                }
            }
            if(rowCount == gameModel.getWinThreshold()){
                gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
            }
        }
    }
    public void checkAntiDiagonal(int rowNumber, int colNumber){
        int diag_count = 0;
        int start_x = rowNumber;
        int start_y = colNumber;

        while(rowNumber < (gameModel.getNumberOfRows() - 1) && colNumber >= 1){
            rowNumber++;
            colNumber--;
            start_x = rowNumber;
            start_y = colNumber;
        }
        while(start_x >= 0 && start_y < gameModel.getNumberOfColumns()){

            if(gameModel.getCellOwner(start_x, start_y) != null){
                if(gameModel.getCellOwner(start_x, start_y).getPlayingLetter() == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()).getPlayingLetter()){
                    diag_count++;
                }
                else{
                    diag_count = 0;
                }
            }
            if(diag_count == gameModel.getWinThreshold()){
                gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
            }
            start_x--;
            start_y++;
        }
    }
    public void checkDiagonal(int rowNumber, int colNumber){
        int diag_count = 0;
        int start_x = rowNumber;
        int start_y = colNumber;
        while(rowNumber >= 1 && colNumber >= 1){
            rowNumber--;
            colNumber--;
            start_x = rowNumber;
            start_y = colNumber;
        }

        while(start_x < gameModel.getNumberOfRows() && start_y < gameModel.getNumberOfColumns()){
            if(gameModel.getCellOwner(start_x, start_y) != null){
                if(gameModel.getCellOwner(start_x, start_y).getPlayingLetter() == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()).getPlayingLetter()){
                    diag_count++;
                }
                else{
                    diag_count = 0;
                }
            }
            if(diag_count == gameModel.getWinThreshold()){
                gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
            }
            start_x++;
            start_y++;
        }
    }
}
