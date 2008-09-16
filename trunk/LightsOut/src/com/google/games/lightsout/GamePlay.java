package com.google.games.lightsout;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class GamePlay extends Activity {
  
  public static final String SAVE_FILE_NAME = "gameplay";
  
  public static final String NEW_GAME = "com.google.game.lightsout.ContinueGame";
  public static final String TOTAL_TIME = "com.google.game.lightsout.TotalTime";
  public static final String TOTAL_MOVES = "com.google.game.lightsout.TotalMoves";
  
  private GameBoard gameBoard;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.game_play);
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    
    try {
      DataOutputStream os = new DataOutputStream(this.openFileOutput(SAVE_FILE_NAME, MODE_PRIVATE));
      os.writeBytes(GameBoardSerializer.serialize(gameBoard));
      os.close();
    } catch (FileNotFoundException e) {
       e.printStackTrace();
    } catch (IOException e) {
      
    }
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    boolean isNewGame = false;
    if (getIntent().getExtras() == null || !getIntent().getExtras().getBoolean(NEW_GAME)) {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.openFileInput(SAVE_FILE_NAME)));
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
          buffer.append(line);
          buffer.append("\n");
        }
        reader.close();
        
        this.gameBoard = GameBoardSerializer.deserialize(this, buffer.toString());
        playLevel(gameBoard.getLevel());
        this.gameBoard.startTimer();
        
      } catch (IOException e) {
        isNewGame = true;
      }
    }
    
    if (isNewGame) {
      this.gameBoard = new GameBoard(this);
      this.gameBoard.setLevel(0);
    }
  }
  
  public void playLevel(int level) {
    int size = this.gameBoard.getSize();
    
    TextView textView = (TextView) findViewById(R.id.level_header);
    textView.setText(getString(R.string.level) + ": " + (level + 1));
    
    TableLayout table = (TableLayout) findViewById(R.id.table);
    table.removeAllViews();
    
    updateMoveCount(gameBoard.getLevelMoves());
    updateSeconds(gameBoard.getLevelSeconds());
    
    for (int i = 0; i < size; i++) {
      TableRow row = new TableRow(this);
      for (int j = 0; j < size; j++) {
        //GamePiece piece = new GamePiece(this, this.gameBoard);
        GamePiece gamePiece = gameBoard.getGamePieceByIndex(i * size + j);
        
        row.addView(gamePiece);
      }
      table.addView(row);
    }
  }
  
  public void levelWon(final int level) {
    final Dialog winDialog = new Dialog(this);
    winDialog.setContentView(R.layout.win_dialog);
    winDialog.setTitle(getString(R.string.level_passed).replaceFirst("%n", (level + 1) +""));
    
    TextView textView = (TextView) winDialog.findViewById(R.id.dialog_text);
    textView.setText(
        getString(R.string.level_time) + ": " + this.gameBoard.getLevelSeconds() + "\n"  +
        getString(R.string.level_moves) + ": " + this.gameBoard.getLevelMoves() + "\n" +
        getString(R.string.total_time) + ": " + this.gameBoard.getTotalSeconds() + "\n" +
        getString(R.string.total_moves) + ": " + this.gameBoard.getTotalMoves() + "\n");
    
    Button button = (Button) winDialog.findViewById(R.id.dialog_button);
    if (level < 9) {
      button.setText(getString(R.string.next_level).replaceFirst("%n", (level + 2) + ""));
    } else {
      button.setText(R.string.game_over);
    }
    
    button.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (level < 9) {
          gameBoard.setLevel(level + 1);
        } else {
          Intent intent = new Intent();
          intent.putExtra(TOTAL_TIME, gameBoard.getTotalSeconds());
          intent.putExtra(TOTAL_MOVES, gameBoard.getTotalMoves());
          setResult(RESULT_OK, intent);
          finish();
        }
        winDialog.dismiss();
      }
    });
    
    winDialog.show();
  }
  
  public void updateMoveCount(int moveCount) {
    TextView textView = (TextView) findViewById(R.id.moves_header);
    textView.setText(getString(R.string.moves) + ": " + this.gameBoard.getLevelMoves());
  }
  
  public void updateSeconds(int seconds) {
    TextView textView = (TextView) findViewById(R.id.time_header);
    textView.setText(getString(R.string.time) + ": " + this.gameBoard.getLevelSeconds());
  }
}
