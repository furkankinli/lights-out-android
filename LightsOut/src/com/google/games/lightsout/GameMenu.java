package com.google.games.lightsout;


import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GameMenu extends Activity {
  
  public static final String HIGH_SCORE_MANAGER = "com.google.game.lightsout.HighScoreManager";
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      setContentView(R.layout.game_menu);
      
      Button newGameButton = (Button) findViewById(R.id.new_game_button);
      Button continueGameButton = (Button) findViewById(R.id.continue_game_button);
      Button highScoresGameButton = (Button) findViewById(R.id.high_scores_button);
      
      final Intent intent = new Intent(this, GamePlay.class);
      newGameButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          intent.putExtra(GamePlay.NEW_GAME, true);
          intent.putExtra(GamePlay.HIGH_SCORES, false);
          startActivity(intent);
        }
      });
      
      if (doesSavedGameExist()) {
        continueGameButton.setEnabled(true);
        continueGameButton.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            intent.putExtra(GamePlay.NEW_GAME, false);
            intent.putExtra(GamePlay.HIGH_SCORES, false);
            startActivity(intent);
          }
        });
      }
      highScoresGameButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          intent.putExtra(GamePlay.NEW_GAME, false);
          intent.putExtra(GamePlay.HIGH_SCORES, true);
          startActivity(intent);
        }
      });
  }
  
  
  
//  @Override
//  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//    super.onActivityResult(requestCode, resultCode, intent);
//    if (requestCode == GAME_PLAY && intent != null) {
//      Bundle extras = intent.getExtras();
//      int totalSeconds = extras.getInt(GamePlay.TOTAL_TIME);
//      int totalMoves = extras.getInt(GamePlay.TOTAL_MOVES);
//      if (highScoreManager.isHighScore(totalSeconds, totalMoves)) {
//        final Dialog namePromptDialog = new Dialog(this);
//        namePromptDialog.setContentView(R.layout.name_prompt_dialog);
//        namePromptDialog.setTitle(R.string.new_high_score);
//        Button button = (Button) namePromptDialog.findViewById(R.id.ok);
//        button.setOnClickListener(new OnClickListener() {
//          public void onClick(View v) {
//            namePromptDialog.dismiss();
//          }
//        });
//        
//        namePromptDialog.show();
//      }
//    }
//  }
  
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