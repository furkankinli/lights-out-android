package com.google.games.lightsout;

import java.util.LinkedList;

public class HighScoreManager {

  private boolean isLoaded;
  private LinkedList<NamePair> timeList;
  private LinkedList<NamePair> movesList;

  public HighScoreManager() {
    this.isLoaded = false;
    this.timeList = new LinkedList<NamePair>();
    this.movesList = new LinkedList<NamePair>();
  }
  
  private void load() {
    if (isLoaded) {
      return;
    }
    
  }
  
  private class NamePair {
    public String name;
    public int score;
  }
}
