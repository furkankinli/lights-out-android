package com.google.games.lightsout;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.TreeMap;

import android.app.Activity;

public class HighScoreManager {

  private static final String MOVES_FILE_NAME = "moves_high_scores";
  private static final String TIME_FILE_NAME = "time_high_scores";

  private static final String SEP = "=";
  
  private boolean isLoaded;
  private TreeMap<Integer, String> timeMap;
  private TreeMap<Integer, String> movesMap;
  private Activity activity;

  public HighScoreManager(Activity activity) {
    this.activity = activity;
    this.isLoaded = false;
    this.timeMap = new TreeMap<Integer, String>();
    this.movesMap = new TreeMap<Integer, String>();
  }

  public void addTimeScore(int time, String name) {
    load();
    name = name.replaceAll(SEP, "");
    timeMap.put(time, name);
    if (timeMap.size() > 5 ) {
      timeMap.remove(timeMap.lastKey());
    }
    save();
  }
  
  public void addMovesScore(int moves, String name) {
    load();
    name = name.replaceAll(SEP, "");
    movesMap.put(moves, name);
    if (movesMap.size() > 5) {
      movesMap.remove(movesMap.lastKey());
    }
    save();
  }
  
  public boolean isHighScore(int time, int moves) {
    load();
    return this.timeMap.size() < 5
        || this.timeMap.lastKey() > time
        || this.movesMap.size() < 5
        || this.movesMap.lastKey() > moves;
  }
  
  private void save() {
    writeFile(MOVES_FILE_NAME, getMapInStringForm(movesMap));
    writeFile(TIME_FILE_NAME, getMapInStringForm(timeMap));
  }

  private String getMapInStringForm(TreeMap<Integer, String> map) {
    StringBuffer buffer = new StringBuffer();

    for (Integer key : map.keySet()) {
      buffer.append(key + SEP + map.get(key));
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
    
    String movesFileContents = readFile(MOVES_FILE_NAME);
    String timeFileContents = readFile(TIME_FILE_NAME);
    
    this.timeMap = parseContents(movesFileContents);
    this.movesMap = parseContents(timeFileContents);
    
    isLoaded = true;
  }
  
  private TreeMap<Integer, String> parseContents(String fileContents) {
    TreeMap<Integer, String> map = new TreeMap<Integer, String>();
    String[] lineArray = fileContents.split("\n");
    for (String line : lineArray) {
      if (line.length() > 0) {
        String[] pair = line.split(SEP);
        map.put(Integer.parseInt(pair[0]), pair[1]);
      }
    }
    return map;
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
