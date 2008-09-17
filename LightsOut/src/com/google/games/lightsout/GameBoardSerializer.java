package com.google.games.lightsout;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class GameBoardSerializer {
  
  public static final String SAVE_FILE_NAME = "gameplay";

  public static void serialize(GamePlay gamePlay, GameBoard gameBoard) {
    StringBuffer pieceListStringBuffer = new StringBuffer();
    for(GamePiece gamePiece : gameBoard.getPieceList()) {
      pieceListStringBuffer.append(gamePiece.isLightOn() ? "1" : "0");
      pieceListStringBuffer.append(",");
    }
    if (pieceListStringBuffer.length() > 0) {
      pieceListStringBuffer.deleteCharAt(pieceListStringBuffer.length() - 1);
    }
    
    StringBuffer buffer = new StringBuffer();
    buffer.append("totalSeconds=" + gameBoard.getTotalSeconds() + "\n");
    buffer.append("totalMoves=" + gameBoard.getTotalMoves() + "\n");
    buffer.append("levelSeconds=" + gameBoard.getLevelSeconds() + "\n");
    buffer.append("levelMoves=" + gameBoard.getLevelMoves() + "\n");
    buffer.append("size=" + gameBoard.getSize() + "\n");
    buffer.append("level=" + gameBoard.getLevel() + "\n");
    buffer.append("pieceList=" + pieceListStringBuffer.toString() + "\n");
    
    
    try {
      DataOutputStream os = new DataOutputStream(gamePlay.openFileOutput(SAVE_FILE_NAME, GamePlay.MODE_PRIVATE));
      os.writeBytes(buffer.toString());
      os.close();
    } catch (IOException e) {
      // Do nothing, fail silently
      // TODO: inform user that save failed.
    }
  }
  
  public static GameBoard deserialize(GamePlay gamePlay) {
    GameBoard gameBoard = new GameBoard(gamePlay);
    String fileContents;
    
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(gamePlay.openFileInput(SAVE_FILE_NAME)));
      StringBuffer buffer = new StringBuffer();
      String line;
      while ((line = reader.readLine()) != null) {
        buffer.append(line);
        buffer.append("\n");
      }
      reader.close();
      fileContents = buffer.toString();
    } catch (IOException e) {
      return gameBoard;
    }
    
    
    String[] lineArray = fileContents.split("\n");
    int size = 5, level = 0, totalSeconds = 0, totalMoves = 0, 
        levelSeconds = 0, levelMoves = 0;
    LinkedList<GamePiece> pieceList = new LinkedList<GamePiece>();
    
    
    for (String line : lineArray) {
      String[] pair = line.split("=");
      if ("totalSeconds".equals(pair[0])) {
        totalSeconds = Integer.parseInt(pair[1]);
      } else if ("totalMoves".equals(pair[0])) {
        totalMoves = Integer.parseInt(pair[1]);
      } else if ("levelSeconds".equals(pair[0])) {
        levelSeconds = Integer.parseInt(pair[1]);
      } else if ("levelMoves".equals(pair[0])) {
        levelMoves = Integer.parseInt(pair[1]);
      } else if ("size".equals(pair[0])) {
        size = Integer.parseInt(pair[1]);
      } else if ("level".equals(pair[0])) {
        level = Integer.parseInt(pair[1]);
      } else if ("pieceList".equals(pair[0])) {
        //String listString = pair[1].replaceFirst("^\\[", "");
        //listString = listString.replaceFirst("\\]$", "");
        String[] pieceListString = pair[1].split(",");
        for (String pieceValue : pieceListString) {
          GamePiece gamePiece = new GamePiece(gamePlay, gameBoard);
          if ("1".equals(pieceValue)) {
            gamePiece.toggleLights();
          }
          pieceList.add(gamePiece);
        }
      }
    }
    
    gameBoard.setProperties(pieceList, totalSeconds, totalMoves, 
        levelSeconds, levelMoves, size, level);
    
    return gameBoard;
  }
}
