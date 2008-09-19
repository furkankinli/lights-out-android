package com.google.games.lightsout;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.FileInputStream;
import java.io.IOException;

public class GameMenu extends Activity {
  
  private static final int GAME_PLAY = 0;
  
  private HighScoreManager highScoreManager;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      highScoreManager = new HighScoreManager(this);
      
      setContentView(R.layout.game_menu);
      
      Button newGameButton = (Button) findViewById(R.id.new_game);
      Button continueGameButton = (Button) findViewById(R.id.continue_game);
      
      final Activity a = this;
      newGameButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          Intent intent = new Intent(a, GamePlay.class);
          intent.putExtra(GamePlay.NEW_GAME, true);
          startActivityForResult(intent, GAME_PLAY);
        }
      });
      
      if (doesSavedGameExist()) {
        continueGameButton.setEnabled(true);
        continueGameButton.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            Intent intent = new Intent(a, GamePlay.class);
            startActivityForResult(intent, GAME_PLAY);
          }
        });
      }
      
      
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == GAME_PLAY && intent != null) {
      Bundle extras = intent.getExtras();
      int totalSeconds = extras.getInt(GamePlay.TOTAL_TIME);
      int totalMoves = extras.getInt(GamePlay.TOTAL_MOVES);
      if (highScoreManager.isHighScore(totalSeconds, totalMoves)) {
        final Dialog namePromptDialog = new Dialog(this);
        namePromptDialog.setContentView(R.layout.name_prompt_dialog);
        namePromptDialog.setTitle(R.string.new_high_score);
        Button button = (Button) namePromptDialog.findViewById(R.id.ok);
        button.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            namePromptDialog.dismiss();
          }
        });
        
        namePromptDialog.show();
      }
    }
  }
  
  private boolean doesSavedGameExist() {
    try {
      FileInputStream is = this.openFileInput(GameBoardSerializer.SAVE_FILE_NAME);
      if (is.read() != 1) {
        return true;
      }
    } catch (IOException e) {
      // Do nothing, any exceptions just return false
    }
    
    return false;
  }
}