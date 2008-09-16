package com.google.games.lightsout;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GameMenu extends Activity {
  
  private static final int GAME_PLAY = 0;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      
      
      setContentView(R.layout.game_menu);
      
      Button newGameButton = (Button) findViewById(R.id.new_game);
      Button continueGameButton = (Button) findViewById(R.id.continue_game);
      
      final Activity a = this;
      newGameButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          Intent intent = new Intent(a, GamePlay.class);
          intent.putExtra(GamePlay.NEW_GAME, true);
          startActivity(intent);
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
    if (requestCode == GAME_PLAY) {
      Bundle extras = intent.getExtras();
      int totalMoves = extras.getInt(GamePlay.TOTAL_MOVES);
      int totalSeconds = extras.getInt(GamePlay.TOTAL_TIME);
    }
  }
  
  private boolean doesSavedGameExist() {
    try {
      FileInputStream is = this.openFileInput(GamePlay.SAVE_FILE_NAME);
      if (is.read() != 1) {
        return true;
      }
    } catch (FileNotFoundException e) {
    } catch (IOException e) {
      // Do nothing, any exceptions just return false
    }
    
    return false;
  }
}