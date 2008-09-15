package com.google.games.lightsout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class GamePlay extends Activity {
  
  private GameBoard gameBoard;

  public GamePlay() {
    this.gameBoard = new GameBoard(this);
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.game_play);
    
    playLevel(0);
    
  }
  
  public void playLevel(int level) {
    int size = this.gameBoard.setLevel(level);
    
    TableLayout table = (TableLayout) findViewById(R.id.table);
    table.removeAllViews();
    
    updateMoveCount(0);
    updateSeconds(0);
    
    for (int i = 0; i < size; i++) {
      TableRow row = new TableRow(this);
      for (int j = 0; j < size; j++) {
        GamePiece piece = new GamePiece(this, this.gameBoard);
        row.addView(piece);
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
    button.setText(getString(R.string.next_level).replaceFirst("%n", (level + 2) + ""));
    
    final GamePlay self = this;
    button.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        self.playLevel(level + 1);
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
