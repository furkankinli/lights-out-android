package com.google.games.lightsout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.SystemClock;

public class GameBoard {

  private int size, level;
  private int totalSeconds, totalMoves, levelSeconds, levelMoves, 
      levelSecondsOffset = 0;

  private boolean isTimerActive;
  
  private LinkedList<GamePiece> pieceList;
  private HashMap<GamePiece, Integer> pieceToIndexMap;
  private GamePlay gamePlay;
  
  public HashMap<String, Bitmap> pieceBitmapMap; 
  
  private final Handler handler = new Handler();
  
  public GameBoard(GamePlay gamePlay) {
    this.gamePlay = gamePlay;
    this.setProperties(new LinkedList<GamePiece>(), 0, 0, 0, 0, 5, 0);
    pieceBitmapMap = new HashMap<String, Bitmap>();
  }
  
  public void setProperties(LinkedList<GamePiece> pieceList, int totalSeconds,
      int totalMoves, int levelSeconds, int levelMoves, int size, int level) {
    this.pieceList = pieceList;
    this.pieceToIndexMap = new HashMap<GamePiece, Integer>();
    for (int i = 0; i < pieceList.size(); i++) {
      this.pieceToIndexMap.put(pieceList.get(i), i);
    }
    
    this.totalSeconds = totalSeconds;
    this.totalMoves = totalMoves;
    this.levelSeconds = levelSeconds;
    this.levelSecondsOffset = levelSeconds;
    this.levelMoves = levelMoves;
    this.size = size;
    this.level = level;
  }
  
  public void setLevel(int level) {
    this.level = level;
    
    this.levelSeconds = 0;
    this.levelMoves = 0;
    
    
    if (level < 4) {
      this.size = 5;
    } else if (level < 8) {
      this.size = 6;
    } else {
      this.size = 7;
    }
    
    if (pieceList.size() == 0) {
      this.pieceBitmapMap.clear();
      for (int i = 0; i < size * size; i++) {
        GamePiece gamePiece = new GamePiece(gamePlay, this);
        this.pieceList.add(gamePiece);
        this.pieceToIndexMap.put(gamePiece, i);
      }
      
      Random random = new Random();
      for (int i = 0; i < 2 * (level + 1) + 1; i++) {
        doTogglePiece(pieceList.get(random.nextInt(size * size)));
      }
    }
    
    gamePlay.playLevel(level);
    startTimer();
  }
  
  public GamePiece getGamePieceByIndex(int index) {
    return this.pieceList.get(index);
  }
  
  public void startTimer() {
    isTimerActive = true;
    new Thread(new Runnable() {
      public void run() {
        final long startTime = SystemClock.uptimeMillis();
        while (isTimerActive) {
          handler.post(new Runnable() {
            public void run() {
              setLevelSeconds(((int)(SystemClock.uptimeMillis() - startTime) / 1000) 
                  + levelSecondsOffset);
            }
          });
          
          SystemClock.sleep(100);
        }
      }
    }).start();
  }
  
  public void stopTimer() {
    isTimerActive = false;
  }
  
  public void togglePiece(GamePiece gamePiece) {
    this.doTogglePiece(gamePiece);
    
    this.levelMoves++;
    this.gamePlay.updateMoveCount(levelMoves);
    
    if (testWin()) {
      stopTimer();
      totalMoves = totalMoves + levelMoves;
      totalSeconds = totalSeconds + levelSeconds;
      this.pieceList.clear();
      
      this.gamePlay.levelWon(this.level);
    }
  }
  
  private void doTogglePiece(GamePiece gamePiece) {
    gamePiece.toggleLights();
    int index = pieceToIndexMap.get(gamePiece);
    LinkedList<Integer> neighborIndeces = new LinkedList<Integer>();
    if (index % size != 0 && index > 0) {
      neighborIndeces.add(index - 1);
    }
    if ((index + 1) % size != 0 && index < this.pieceList.size() - 1) {
      neighborIndeces.add(index + 1);
    }
    if (index - size >= 0) {
      neighborIndeces.add(index - size);
    }
    if (index + size < this.pieceList.size()) {
      neighborIndeces.add(index + size);
    }
    
    for (int i : neighborIndeces) {
      pieceList.get(i).toggleLights();
    }
  }
  
  private boolean testWin() {
    for (GamePiece gamePiece : pieceList) {
      if (gamePiece.isLightOn()) {
        return false;
      }
    }
    return true;
  }
  
  public int getTotalSeconds() {
    return totalSeconds;
  }

  public int getTotalMoves() {
    return totalMoves;
  }

  public int getLevelSeconds() {
    return levelSeconds;
  }

  public int getLevelMoves() {
    return levelMoves;
  }
  
  public int getSize() {
    return size;
  }
  
  public int getLevel() {
    return level;
  }
  
  public LinkedList<GamePiece> getPieceList() {
    return pieceList;
  }

  public void setLevelSeconds(int seconds) {
    this.levelSeconds = seconds;
    this.gamePlay.updateSeconds(seconds);
  }
}
