package com.google.games.lightsout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.widget.Button;

public class GamePiece extends Button {

  private GameBoard gameBoard;
  private boolean isLightOn, isTouchDown; 
  
  public GamePiece(Context context, GameBoard gameBoard) {
    super(context);
    this.gameBoard = gameBoard;
    this.isLightOn = true;
    this.isTouchDown = false;
    
    gameBoard.registerGamePiece(this);
  }
  
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    
    setMeasuredDimension(320 / gameBoard.getSize(), 320 / gameBoard.getSize());
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), new Paint());
    
    if (this.isTouchDown) {
      drawImage(canvas, R.drawable.red_block, new Rect(0, 0, getWidth(), getHeight()));
    }
    
    if (isLightOn) {
      drawImage(canvas, R.drawable.lights_on, new Rect(3, 3, getWidth() - 3, getHeight() - 3));
    } else {
      drawImage(canvas, R.drawable.lights_off, new Rect(3, 3, getWidth() - 3, getHeight() - 3));
    }
  }
  
  private void drawImage(Canvas canvas, int imageId, Rect location) {
    int width = location.right - location.left;
    int height = location.bottom - location.top;
    
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageId);
    bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
    canvas.drawBitmap(bitmap, location.left, location.top, null);
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
    case MotionEvent.ACTION_DOWN:
      this.isTouchDown = true;
      this.invalidate();
      return true;
    case MotionEvent.ACTION_UP:
      this.isTouchDown = false;
      this.gameBoard.togglePiece(this);
      this.invalidate();
      return true;
    }
    return false;
  }
  
  
  public void toggleLights() {
    this.isLightOn = !this.isLightOn;
    this.invalidate();
  }
  
  public boolean isLightOn() {
    return isLightOn;
  }
  
}
