package com.google.games.lightsout;

import java.util.List;

import com.google.games.lightsout.HighScoreManager.NamePair;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class GamePlay extends Activity {
  
  public static final String HIGH_SCORES = "com.google.game.lightsout.HIGH_SCORES";
  public static final String NEW_GAME = "com.google.game.lightsout.NEW_GAME";
  public static final String TOTAL_TIME = "com.google.game.lightsout.TotalTime";
  public static final String TOTAL_MOVES = "com.google.game.lightsout.TotalMoves";
  
  private GameBoard gameBoard = null;
  private HighScoreManager highScoreManager;
  private Dialog dialog;
  private boolean isHighScores = false;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.game_play);
    
    this.highScoreManager = new HighScoreManager(this);
    
    if (getIntent().getExtras() != null 
        && getIntent().getExtras().getBoolean(NEW_GAME)
        && savedInstanceState == null) {
      this.gameBoard = new GameBoard(this);
    } else if (getIntent().getExtras() != null 
        && getIntent().getExtras().getBoolean(HIGH_SCORES)) {
      isHighScores = true;
    }
    
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    
    if (gameBoard != null) {
      gameBoard.stopTimer();
      GameBoardSerializer.serialize(this, gameBoard);
    }
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    
    if (this.isHighScores) {
      showHighScores();
      return;
    }

    if (gameBoard == null) {
      this.gameBoard = GameBoardSerializer.deserialize(this);
    }
    if (gameBoard.testWin()) {
      updateMoveCount(gameBoard.getLevelMoves());
      updateSeconds(gameBoard.getLevelSeconds());
      levelWon();
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
    this.dialog = new Dialog(this);
    dialog.setContentView(R.layout.win_dialog);
    
    TextView levelTimeTextView = (TextView) dialog.findViewById(R.id.level_time_text);
    TextView levelMovesTextView = (TextView) dialog.findViewById(R.id.level_moves_text);
    TextView totalTimeTextView = (TextView) dialog.findViewById(R.id.total_time_text);
    TextView totalMovesTextView = (TextView) dialog.findViewById(R.id.total_moves_text);
    levelTimeTextView.setText(this.gameBoard.getLevelSeconds() + "");
    levelMovesTextView.setText(this.gameBoard.getLevelMoves() + "");
    totalTimeTextView.setText(this.gameBoard.getTotalSeconds() + "");
    totalMovesTextView.setText(this.gameBoard.getTotalMoves() + "");
    
    Button button = (Button) dialog.findViewById(R.id.dialog_button);
    if (gameBoard.getLevel() < 0) {
      button.setText(getString(R.string.next_level).replaceFirst("%n", (gameBoard.getLevel() + 2) + ""));
      dialog.setTitle(getString(R.string.level_passed).replaceFirst("%n", (gameBoard.getLevel() + 1) +""));
    } else {
      button.setText(R.string.ok);
      dialog.setTitle(R.string.game_over);
      if (highScoreManager.isHighScore(gameBoard.getTotalSeconds(), gameBoard.getTotalMoves())) {
        View highScoreInput = dialog.findViewById(R.id.high_score_input);
        highScoreInput.setVisibility(View.VISIBLE);
      }
    }
    
    button.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        dialog.dismiss();
      }
    });
    dialog.setOnDismissListener(new OnDismissListener() {
      public void onDismiss(DialogInterface dialogInterface) {
        if (gameBoard.getLevel() < 0) {
          gameBoard.playNextLevel();
        } else {
          EditText highScoreEditText = (EditText) dialog.findViewById(R.id.high_score_name);
          gameOver(highScoreEditText.getText().toString());
        }
      }
    });
    
    dialog.show();
  }
  
  public void gameOver(String name) {
    showHighScores(
        highScoreManager.addTimeScore(name, 
            gameBoard.getLevelSeconds(), gameBoard.getTotalMoves()),
        highScoreManager.addMovesScore(name, 
            gameBoard.getLevelSeconds(), gameBoard.getTotalMoves()));
  }
  
  public void showHighScores() {
    showHighScores(-1, -1);
  }
  
  public void showHighScores(int timePosition, int movesPosition) {
    setContentView(R.layout.high_scores);
    isHighScores = true;
    
    TableLayout timeTable = (TableLayout) findViewById(R.id.time_table);
    addScoresToHighScoreTable(timeTable, highScoreManager.getTimeList());
    TableLayout movesTable = (TableLayout) findViewById(R.id.moves_table);
    addScoresToHighScoreTable(movesTable, highScoreManager.getMovesList());
    
    Button button = (Button) findViewById(R.id.high_scores_button);
    button.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });
  }
  
  private void addScoresToHighScoreTable(TableLayout table, List<NamePair> list) {
    for (int i = 0; i < HighScoreManager.NUM_SCORES; i++) {
      String name = "", timeScore = "", movesScore = "";
      String position = (i + 1) + ". ";
      if (i < list.size()) {
        NamePair namePair = list.get(i);
        name =  namePair.name;
        timeScore = namePair.time + "";
        movesScore = namePair.moves + "";
      }
      
      TableRow row = new TableRow(this);
      TextView positionView = new TextView(this);
      positionView.setText(position);
      TextView nameView = new TextView(this);
      nameView.setText(name);
      TextView timeScoreView = new TextView(this);
      timeScoreView.setText(timeScore);
      TextView movesScoreView = new TextView(this);
      movesScoreView.setText(movesScore);
      
      row.addView(positionView);
      row.addView(nameView);
      row.addView(timeScoreView);
      row.addView(movesScoreView);
      
      if (i % 2 == 0) {
        row.setBackgroundColor(Color.rgb(50, 50, 50));
      }
      
      table.addView(row);
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
