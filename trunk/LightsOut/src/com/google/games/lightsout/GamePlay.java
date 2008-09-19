package com.google.games.lightsout;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GamePlay extends Activity {
  
  public static final String NEW_GAME = "com.google.game.lightsout.ContinueGame";
  public static final String TOTAL_TIME = "com.google.game.lightsout.TotalTime";
  public static final String TOTAL_MOVES = "com.google.game.lightsout.TotalMoves";
  
  private static final int WIN_DIALOG_ID = 1;
  
  private GameBoard gameBoard = null;
  private boolean isWinDialogShowing = false;
  private Dialog winDialog;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.game_play);
    
    if (getIntent().getExtras() != null 
        && getIntent().getExtras().getBoolean(NEW_GAME)
        && savedInstanceState == null) {
      this.gameBoard = new GameBoard(this);
    }
    
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    
    gameBoard.stopTimer();
    GameBoardSerializer.serialize(this, gameBoard);
  }
  
  @Override
  protected void onResume() {
    super.onResume();

    if (gameBoard == null) {
      this.gameBoard = GameBoardSerializer.deserialize(this);
    }
    if (gameBoard.testWin()) {
      this.onPrepareDialog(WIN_DIALOG_ID, this.winDialog);
    } else {
      this.gameBoard.startPlaying();
    }
  }
  
  public void playLevel(int level) {
    int size = this.gameBoard.getSize();
    
    TextView textView = (TextView) findViewById(R.id.level_header);
    textView.setText(getString(R.string.level) + ": " + (level + 1));
    
    LinearLayout boardHolder = (LinearLayout) findViewById(R.id.board_holder);
    boardHolder.removeAllViews();
    
    GameBoardLayout gameBoardLayout = new GameBoardLayout(this, size);
    gameBoardLayout.setLayoutParams(
        new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
   
    boardHolder.addView(gameBoardLayout);
    
    updateMoveCount(gameBoard.getLevelMoves());
    updateSeconds(gameBoard.getLevelSeconds());
    
    for (int i = 0; i < size * size; i++) {
      GamePiece gamePiece = gameBoard.getGamePieceByIndex(i);
      gameBoardLayout.addView(gamePiece);
    }
  }
  
  public void levelWon() {
    showDialog(WIN_DIALOG_ID);
  }
  
  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case WIN_DIALOG_ID:
        final Dialog winDialog = new Dialog(this);
        winDialog.setContentView(R.layout.win_dialog);
        
        Button button = (Button) winDialog.findViewById(R.id.dialog_button);
        button.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            if (gameBoard.getLevel() < 9) {
              gameBoard.playNextLevel();
            } else {
              Intent intent = new Intent();
              Bundle bundle = new Bundle();
              bundle.putInt(TOTAL_TIME, gameBoard.getTotalSeconds());
              bundle.putInt(TOTAL_MOVES, gameBoard.getTotalMoves());
              intent.putExtras(bundle);
              setResult(RESULT_OK, intent);
              finish();
            }
            dismissDialog(WIN_DIALOG_ID);
          }
        });
        this.winDialog = winDialog;
        return winDialog;
    }
    
    // This shouldn't be necessary, but it seems that android doesn't call
    // onPrepareDialog when the app is being restored. So, if the phone is
    // flipped open when the dialog is showing, onPrepareDialog isn't called
    // unless the following line is present.
    //this.onPrepareDialog(id, dialog);
    
    return null;
  }
  
  @Override
  protected void onPrepareDialog(int id, Dialog dialog) {
    super.onPrepareDialog(id, dialog);
    switch (id) {
      case WIN_DIALOG_ID:
        dialog.setTitle(getString(R.string.level_passed).replaceFirst("%n", (gameBoard.getLevel() + 1) +""));
        
        TextView levelTimeTextView = (TextView) dialog.findViewById(R.id.level_time_text);
        TextView levelMovesTextView = (TextView) dialog.findViewById(R.id.level_moves_text);
        TextView totalTimeTextView = (TextView) dialog.findViewById(R.id.total_time_text);
        TextView totalMovesTextView = (TextView) dialog.findViewById(R.id.total_moves_text);
        levelTimeTextView.setText(this.gameBoard.getLevelSeconds() + "");
        levelMovesTextView.setText(this.gameBoard.getLevelMoves() + "");
        totalTimeTextView.setText(this.gameBoard.getTotalSeconds() + "");
        totalMovesTextView.setText(this.gameBoard.getTotalMoves() + "");
        
        Button button = (Button) dialog.findViewById(R.id.dialog_button);
        if (gameBoard.getLevel() < 9) {
          button.setText(getString(R.string.next_level).replaceFirst("%n", (gameBoard.getLevel() + 2) + ""));
        } else {
          button.setText(R.string.game_over);
        }
        
        break;
    }
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
