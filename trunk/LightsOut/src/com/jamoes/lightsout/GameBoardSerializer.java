package com.jamoes.lightsout;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GameBoardSerializer {
  
  public static final String SAVE_FILE_NAME = "gameplay";
  private LightsOutPlay gamePlay;

  public GameBoardSerializer(LightsOutPlay gamePlay) {
    this.gamePlay = gamePlay;
  }
  
  public void serialize(GameBoard gameBoard) {
    StringBuffer buffer = new StringBuffer();
    
    if (gameBoard != null) {
      
      String pieceListString = getPieceListString(gameBoard.getPieceList());
      String originalPieceListSring = getPieceListString(
          gameBoard.getOriginalPieceList());
      
      buffer.append("totalSeconds=" + gameBoard.getTotalSeconds() + "\n");
      buffer.append("totalMoves=" + gameBoard.getTotalMoves() + "\n");
      buffer.append("levelSeconds=" + gameBoard.getLevelSeconds() + "\n");
      buffer.append("levelMoves=" + gameBoard.getLevelMoves() + "\n");
      buffer.append("size=" + gameBoard.getSize() + "\n");
      buffer.append("level=" + gameBoard.getLevel() + "\n");
      buffer.append("numHints=" + gameBoard.getNumHints() + "\n");
      buffer.append("pieceList=" + pieceListString + "\n");
      buffer.append("originalPieceList=" + originalPieceListSring + "\n");
      buffer.append("solutionSet=" + getSetString(gameBoard.getSolutionSet()) + "\n");
    }
    
    try {
      DataOutputStream os = new DataOutputStream(gamePlay.openFileOutput(SAVE_FILE_NAME, LightsOutPlay.MODE_PRIVATE));
      os.writeBytes(buffer.toString());
      os.close();
    } catch (IOException e) {
      // Do nothing, fail silently
      // TODO: inform user that save failed.
    }
  }
  
  private String getPieceListString(List<GamePiece> pieceList) {
    StringBuffer pieceListStringBuffer = new StringBuffer();
    for(GamePiece gamePiece : pieceList) {
      pieceListStringBuffer.append(gamePiece.isLightOn() ? "1"
          : gamePiece.isBlock() ? "2"
          : "0");
      pieceListStringBuffer.append(",");
    }
    if (pieceListStringBuffer.length() > 0) {
      pieceListStringBuffer.deleteCharAt(pieceListStringBuffer.length() - 1);
    }
    
    return pieceListStringBuffer.toString();
  }
  
  private String getSetString(Set<Integer> set) {
    StringBuffer buffer = new StringBuffer();
    for (int i : set) {
      buffer.append(i);
      buffer.append(',');
    }
    if (buffer.length() > 0) {
      buffer.deleteCharAt(buffer.length() - 1);
    }
    
    return buffer.toString();
  }
  
  public GameBoard deserialize() {
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
        levelSeconds = 0, levelMoves = 0, numHints = 0;
    LinkedList<GamePiece> pieceList = null, originalPieceList = null;
    Set<Integer> solutionSet = null;
    
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
      } else if ("numHints".equals(pair[0])) {
        numHints = Integer.parseInt(pair[1]);
      } else if ("pieceList".equals(pair[0]) && pair[1].length() > 0) {
        pieceList = getPieceList(pair[1], gameBoard);
      } else if ("originalPieceList".equals(pair[0]) && pair[1].length() > 0) {
        originalPieceList = getPieceList(pair[1], gameBoard);
      } else if ("solutionSet".equals(pair[0]) && pair[1].length() > 0) {
        solutionSet = getSolutionSet(pair[1]);
      }
    }
    
    gameBoard.setProperties(pieceList, originalPieceList, solutionSet, 
        totalSeconds, totalMoves, levelSeconds, levelMoves, size, level,
        numHints);
    
    return gameBoard;
  }
  
  private LinkedList<GamePiece> getPieceList(String s, GameBoard gameBoard) {
    LinkedList<GamePiece> pieceList = new LinkedList<GamePiece>();
    String[] pieceListString = s.split(",");
    for (String pieceValue : pieceListString) {
      GamePiece gamePiece = new GamePiece(gamePlay, gameBoard);
      if ("1".equals(pieceValue)) {
        gamePiece.toggleLights();
      } else if ("2".equals(pieceValue)) {
        gamePiece.enableBlock();
      }
      pieceList.add(gamePiece);
    }
    
    return pieceList;
  }
  
  private Set<Integer> getSolutionSet(String s) {
    HashSet<Integer> set = new HashSet<Integer>();
    String[] numbers = s.split(",");
    for (String number : numbers) {
      set.add(Integer.parseInt(number));
    }
    
    return set;
  }
}
