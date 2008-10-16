package com.jamoes.lightsout;

import java.util.List;

import com.jamoes.lightsout.HighScoreManager.NamePair;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.Toast;

public class LightsOutPlay extends Activity {
  
  public static final String HIGH_SCORES = "com.jamoes.lightsout.HIGH_SCORES";
  public static final String NEW_GAME = "com.jamoes.lightsout.NEW_GAME";
  public static final String IS_HIGH_SCORES = "com.jamoes.lightsout.IS_HIGH_SCORES";
  
  public static final int MENU_HINT = 0;
  public static final int MENU_RESTART = 1;
  
  private GameBoard gameBoard = null;
  private HighScoreManager highScoreManager;
  private GameBoardSerializer gameBoardSerializer;
  private Dialog dialog;
  private boolean isHighScores = false;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.game_play);
    
    this.highScoreManager = new HighScoreManager(this);
    this.gameBoardSerializer = new GameBoardSerializer(this);
    
    isHighScores = false;
    
    if (savedInstanceState != null) {
      this.isHighScores = savedInstanceState.getBoolean(IS_HIGH_SCORES);
    } else if (getIntent().getExtras() != null 
        && getIntent().getExtras().getBoolean(NEW_GAME)) {
      this.gameBoard = new GameBoard(this);
    } else if (getIntent().getExtras() != null 
        && getIntent().getExtras().getBoolean(HIGH_SCORES)) {
      isHighScores = true;
    }
    
  }
  
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    
    outState.putBoolean(IS_HIGH_SCORES, isHighScores);
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    
    if (gameBoard != null) {
      gameBoard.stopTimer();
      gameBoardSerializer.serialize(gameBoard);
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
      this.gameBoard = gameBoardSerializer.deserialize();
    }
    if (gameBoard.testWin()) {
      updateMoveCount();
      updateSeconds();
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
    
    updateMoveCount();
    updateSeconds();
    
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
    if (gameBoard.getLevel() < GameBoard.LAST_LEVEL) {
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
        if (gameBoard.getLevel() < GameBoard.LAST_LEVEL) {
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
            gameBoard.getTotalSeconds(), gameBoard.getTotalMoves()),
        highScoreManager.addMovesScore(name, 
            gameBoard.getTotalSeconds(), gameBoard.getTotalMoves()));
    gameBoardSerializer.serialize(null);
    this.gameBoard = null;
  }
  
  public void showHighScores() {
    showHighScores(-1, -1);
  }
  
  public void showHighScores(int timePosition, int movesPosition) {
    setContentView(R.layout.high_scores);
    isHighScores = true;
    
    TableLayout timeTable = (TableLayout) findViewById(R.id.time_table);
    addScoresToHighScoreTable(timeTable, highScoreManager.getTimeList(),
        timePosition);
    TableLayout movesTable = (TableLayout) findViewById(R.id.moves_table);
    addScoresToHighScoreTable(movesTable, highScoreManager.getMovesList(),
        movesPosition);
    
    Button button = (Button) findViewById(R.id.high_scores_button);
    button.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });
  }
  
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    
    menu.clear();
    if (isHighScores) {
      menu.add(R.string.reset_high_scores);
    } else {
      menu.add(Menu.NONE, MENU_HINT, Menu.NONE, R.string.hint);
      menu.add(Menu.NONE, MENU_RESTART, Menu.NONE, R.string.restart_level);
    }
    
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (isHighScores) {
      this.highScoreManager.resetHighScores();
      showHighScores();
    } else {
      if (item.getItemId() == MENU_HINT) {
        this.gameBoard.giveHint();
      } else if (item.getItemId() == MENU_RESTART) {
        this.gameBoard.restartLevel();
      }
    }
    
    return super.onOptionsItemSelected(item);
  }
  
  private void addScoresToHighScoreTable(TableLayout table, List<NamePair> list,
      int highlightPosition) {
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
      
      if (i == highlightPosition) {
        row.setBackgroundColor(Color.rgb(150, 100, 0));
      }
      
      table.addView(row);
    }
  }
  
  public void updateMoveCount() {
    TextView textView = (TextView) findViewById(R.id.moves_header);
    textView.setText(getString(R.string.moves) + ": " + this.gameBoard.getLevelMoves());
  }
  
  public void updateSeconds() {
    TextView textView = (TextView) findViewById(R.id.time_header);
    textView.setText(getString(R.string.time) + ": " + this.gameBoard.getLevelSeconds());
  }

  public void showLevelStartMessage(int level) {
    if (level == 0) {
      Toast toast = Toast.makeText(this, 
          getString(R.string.level_start_message), 
          Toast.LENGTH_LONG);
      toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
      toast.show();
    }
  }
  
  public void showHintPenaltyMessage() {
    Toast toast = Toast.makeText(this, 
        getString(R.string.hint_penalty), 
        Toast.LENGTH_LONG);
    toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
    toast.show();
  }
}
