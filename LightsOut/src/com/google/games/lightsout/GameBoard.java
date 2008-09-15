package com.google.games.lightsout;

import java.util.HashMap;
import java.util.LinkedList;

import android.os.Handler;
import android.os.SystemClock;

public class GameBoard {

  private int size, level;
  private int totalSeconds, totalMoves, levelSeconds, levelMoves;

  private boolean isTimerActive;
  
  private LinkedList<GamePiece> pieceList;
  private HashMap<GamePiece, Integer> pieceToIndexMap;
  private int currentIndex;
  private GamePlay gamePlay;
  
  private final Handler handler = new Handler();
  
  public GameBoard(GamePlay gamePlay) {
    this.gamePlay = gamePlay;
    this.pieceList = new LinkedList<GamePiece>();
    this.pieceToIndexMap = new HashMap<GamePiece, Integer>();
    this.currentIndex = 0;
    
    this.totalSeconds = 0;
    this.totalMoves = 0;
    this.levelSeconds = 0;
    this.levelMoves = 0;
    
    
  }
  
  public int setLevel(int level) {
    this.level = level;
    this.pieceList = new LinkedList<GamePiece>();
    this.pieceToIndexMap = new HashMap<GamePiece, Integer>();
    this.currentIndex = 0;
    
    this.levelSeconds = 0;
    this.levelMoves = 0;
    
    startTimer();
    
    this.size = 5;
    
    return this.size;
  }
  
  private void startTimer() {
    isTimerActive = true;
    new Thread(new Runnable() {
      public void run() {
        final long startTime = SystemClock.uptimeMillis();
        while (isTimerActive) {
          handler.post(new Runnable() {
            public void run() {
              setLevelSeconds((int)(SystemClock.uptimeMillis() - startTime) / 1000);
            }
          });
          
          SystemClock.sleep(100);
        }
      }
    }).start();
  }
  
  private void stopTimer() {
    isTimerActive = false;
  }
  
  public void registerGamePiece(GamePiece gamePiece) {
    this.pieceList.add(gamePiece);
    this.pieceToIndexMap.put(gamePiece, this.currentIndex);
    this.currentIndex++;
  }
  
  public void togglePiece(GamePiece gamePiece) {
    assert(this.currentIndex == this.size * this.size);
    
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
    
    this.levelMoves++;
    this.gamePlay.updateMoveCount(levelMoves);
    
    if (testWin()) {
      stopTimer();
      totalMoves = totalMoves + levelMoves;
      totalSeconds = totalSeconds + levelSeconds;
      
      this.gamePlay.levelWon(this.level);
    }
  }
  
  private boolean testWin() {
    for (GamePiece gamePiece : pieceList) {
      if (gamePiece.isLightOn()) {
        return true;
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
  
  public void setLevelSeconds(int seconds) {
    this.levelSeconds = seconds;
    this.gamePlay.updateSeconds(seconds);
  }
}
