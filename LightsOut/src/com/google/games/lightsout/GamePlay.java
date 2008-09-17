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
import android.widget.TableRow;
import android.widget.TextView;

public class GamePlay extends Activity {
  
  public static final String NEW_GAME = "com.google.game.lightsout.ContinueGame";
  public static final String TOTAL_TIME = "com.google.game.lightsout.TotalTime";
  public static final String TOTAL_MOVES = "com.google.game.lightsout.TotalMoves";
  
  private GameBoard gameBoard = null;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.game_play);
    
    if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(NEW_GAME)
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
    this.gameBoard.setLevel(gameBoard.getLevel());
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
  
  public void levelWon(final int level) {
    final Dialog winDialog = new Dialog(this);
    winDialog.setContentView(R.layout.win_dialog);
    winDialog.setTitle(getString(R.string.level_passed).replaceFirst("%n", (level + 1) +""));
    
    TextView levelTimeTextView = (TextView) winDialog.findViewById(R.id.level_time_text);
    TextView levelMovesTextView = (TextView) winDialog.findViewById(R.id.level_moves_text);
    TextView totalTimeTextView = (TextView) winDialog.findViewById(R.id.total_time_text);
    TextView totalMovesTextView = (TextView) winDialog.findViewById(R.id.total_moves_text);
    levelTimeTextView.setText(this.gameBoard.getLevelSeconds() + "");
    levelMovesTextView.setText(this.gameBoard.getLevelMoves() + "");
    totalTimeTextView.setText(this.gameBoard.getTotalSeconds() + "");
    totalMovesTextView.setText(this.gameBoard.getTotalMoves() + "");
//    textView.setText(
//        getString(R.string.level_time) + ": " + this.gameBoard.getLevelSeconds() + "\n"  +
//        getString(R.string.level_moves) + ": " + this.gameBoard.getLevelMoves() + "\n" +
//        getString(R.string.total_time) + ": " + this.gameBoard.getTotalSeconds() + "\n" +
//        getString(R.string.total_moves) + ": " + this.gameBoard.getTotalMoves() + "\n");
    
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
          Bundle bundle = new Bundle();
          bundle.putInt(TOTAL_TIME, gameBoard.getTotalSeconds());
          bundle.putInt(TOTAL_MOVES, gameBoard.getTotalMoves());
          intent.putExtras(bundle);
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
