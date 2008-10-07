package com.jamoes.lightsout;


import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LightsOutMenu extends Activity {
  
  public static final int NEW_GAME_DIALOG = 1;
  
  private static final int PLAY_GAME = 1;
  private static final int VIEW_HIGH_SCORES = 2;
  
  private Button continueGameButton;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      setContentView(R.layout.game_menu);
      
      Button newGameButton = (Button) findViewById(R.id.new_game_button);
      this.continueGameButton = (Button) findViewById(R.id.continue_game_button);
      Button highScoresGameButton = (Button) findViewById(R.id.high_scores_button);
      
      final Intent intent = new Intent(this, LightsOutPlay.class);
      newGameButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          newGame();
        }
      });
      
      if (doesSavedGameExist()) {
        continueGameButton.setEnabled(true);
        continueGameButton.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            intent.putExtra(LightsOutPlay.NEW_GAME, false);
            intent.putExtra(LightsOutPlay.HIGH_SCORES, false);
            startActivityForResult(intent, PLAY_GAME);
          }
        });
      }
      highScoresGameButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          intent.putExtra(LightsOutPlay.NEW_GAME, false);
          intent.putExtra(LightsOutPlay.HIGH_SCORES, true);
          startActivityForResult(intent, VIEW_HIGH_SCORES);
        }
      });
  }
  
  @Override
  protected Dialog onCreateDialog(int id) {
    if (id == NEW_GAME_DIALOG) {
      return new AlertDialog.Builder(this).setMessage(R.string.erase_game)
          .setPositiveButton(R.string.ok, new OnClickListener(){
        public void onClick(DialogInterface dialog, int which) {
          startNewGame();
        }})
        .setNegativeButton(R.string.cancel, null)
        .setTitle(R.string.ok_to_erase)
        .create();
    }
    return null;
  }
  
  
  private void newGame() {
    if (doesSavedGameExist()) {
      showDialog(NEW_GAME_DIALOG);
    } else {
      startNewGame();
    }
  }
  
  private void startNewGame() {
    Intent intent = new Intent(this, LightsOutPlay.class);
    intent.putExtra(LightsOutPlay.NEW_GAME, true);
    intent.putExtra(LightsOutPlay.HIGH_SCORES, false);
    startActivityForResult(intent, PLAY_GAME);
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, 
      Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (doesSavedGameExist()) {
      continueGameButton.setEnabled(true);
      if (requestCode == PLAY_GAME) {
        Toast.makeText(this, R.string.game_saved, Toast.LENGTH_SHORT).show();
      }
    } else {
      continueGameButton.setEnabled(false);
    }
  }
  
  private boolean doesSavedGameExist() {
    try {
      FileInputStream is = this.openFileInput(GameBoardSerializer.SAVE_FILE_NAME);
      if (is.available() == 0) {
        return false;
      }
      if (is.read() != 1) {
        return true;
      }
    } catch (IOException e) {
      // Do nothing, any exceptions just return false
    }
    
    return false;
  }
}