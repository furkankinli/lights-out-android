package com.google.games.lightsout;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import android.app.Activity;

public class HighScoreManager {

  private static final long serialVersionUID = 1L;
  
  private static final String MOVES_FILE_NAME = "moves_high_scores";
  private static final String TIME_FILE_NAME = "time_high_scores";

  private static final String SEP = "=";
  public static final int NUM_SCORES = 5;
  
  private LinkedList<NamePair> timeList;
  private LinkedList<NamePair> movesList;
  
  private boolean isLoaded;
  private Activity activity;
  
  public HighScoreManager(Activity activity){
    this.activity = activity;
    this.isLoaded = false;
    this.timeList = new LinkedList<NamePair>();
    this.movesList = new LinkedList<NamePair>();
  }
  
  public static class NamePair {
    public String name;
    public int score, time, moves;

    public NamePair(String name, int score, int time, int moves) {
      this.name = name;
      this.score = score;
      this.time = time;
      this.moves = moves;
    }
  }

  public int addTimeScore(String name, int time, int moves) {
    return this.addScore(new NamePair(name, time, time, moves), timeList);
  }
  
  public int addMovesScore(String name, int time, int moves) {
    return this.addScore(new NamePair(name, moves, time, moves), movesList);
  }
  
  private int addScore(NamePair namePair, LinkedList<NamePair> list) {
    load();
    namePair.name = namePair.name.replaceAll(SEP, "");
    int position = list.size();
    for (int i = 0; i < list.size(); i++) {
      if (namePair.score < list.get(i).score) {
        list.add(i, namePair);
        position = i;
        break;
      }
    }
    if (position == list.size() && list.size() < NUM_SCORES) {
      list.add(namePair);
      position = list.size() - 1;
    }
    
    if (list.size() > NUM_SCORES) {
      list.removeLast();
    }
    save();
    
    return position;
  }
  
  public boolean isHighScore(int time, int moves) {
    load();
    return this.timeList.size() < NUM_SCORES
        || this.timeList.getLast().score > time
        || this.movesList.size() < NUM_SCORES
        || this.movesList.getLast().score > moves;
  }
  
  private void save() {
    writeFile(MOVES_FILE_NAME, getListInStringForm(movesList));
    writeFile(TIME_FILE_NAME, getListInStringForm(timeList));
  }

  private String getListInStringForm(List<NamePair> list) {
    StringBuffer buffer = new StringBuffer();
    
    for (int i = 0; i < list.size(); i++) {
      NamePair namePair = list.get(i);
      buffer.append(namePair.name + SEP + namePair.time + SEP + namePair.moves);
      buffer.append('\n');
    }
    
    if (buffer.length() > 0) {
      buffer.deleteCharAt(buffer.length() - 1);
    }
    
    return buffer.toString();
  }
  
  private void load() {
    if (isLoaded) {
      return;
    }
    
    String timeFileContents = readFile(TIME_FILE_NAME);
    String movesFileContents = readFile(MOVES_FILE_NAME);
    
    this.timeList = parseContents(timeFileContents, 1);
    this.movesList = parseContents(movesFileContents, 2);
    
    isLoaded = true;
  }
  
  private LinkedList<NamePair> parseContents(String fileContents, int scoreIndex) {
    LinkedList<NamePair> list = new LinkedList<NamePair>();
    String[] lineArray = fileContents.split("\n");
    for (String line : lineArray) {
      if (line.length() > 0) {
        String[] pair = line.split(SEP);
        list.add(new NamePair(pair[0], Integer.parseInt(pair[scoreIndex]),
            Integer.parseInt(pair[1]),Integer.parseInt(pair[2])));
      }
    }
    return list;
  }

  public LinkedList<NamePair> getTimeList() {
    load();
    return timeList;
  }

  public LinkedList<NamePair> getMovesList() {
    load();
    return movesList;
  }

  private String readFile(String fileName) {
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(activity.openFileInput(fileName)));
      StringBuffer buffer = new StringBuffer();
      String line;
      while ((line = reader.readLine()) != null) {
        buffer.append(line);
        buffer.append("\n");
      }
      reader.close();
//      return "";
      return(buffer.toString());
    } catch (IOException e) {
      return "";
    }
  }
  
  private void writeFile(String fileName, String contents) {
    try {
      DataOutputStream os = new DataOutputStream(
          activity.openFileOutput(fileName, GamePlay.MODE_PRIVATE));
      os.writeBytes(contents);
      os.close();
    } catch (IOException e) {
    }
  }
}
