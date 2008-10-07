package com.jamoes.lightsout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TableLayout;

public class GamePiece extends Button {
  private static final int PADDING = 3; 

  private GameBoard gameBoard;
  private boolean isLightOn, isBlock;
  
  public GamePiece(Context context, GameBoard gameBoard) {
    this(context, gameBoard, false, false);
  }
  
  public GamePiece(Context context, GameBoard gameBoard, boolean isLightOn,
      boolean isBlock) {
    super(context);
    this.gameBoard = gameBoard;
    this.isLightOn = isLightOn;
    this.isBlock = isBlock;
  }
  
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(0, 0);
  }
  
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), new Paint());
    
    if (isFocused()) {
      drawImage(canvas, R.drawable.red_block, 0);
    }

    if (isBlock) {
      drawImage(canvas, R.drawable.block, PADDING);
    } else if (isLightOn) {
      drawImage(canvas, R.drawable.lights_on, PADDING);
    } else {
      drawImage(canvas, R.drawable.lights_off, PADDING);
    }
  }
  
  private void drawImage(Canvas canvas, int imageId, int padding) {
    int edgeSize = getWidth() - padding * 2;
    
    String key = edgeSize + "," + imageId;
    
    Bitmap bitmap = this.gameBoard.pieceBitmapMap.get(key);
    if (bitmap == null) {
      bitmap = BitmapFactory.decodeResource(getResources(), imageId);
      bitmap = Bitmap.createScaledBitmap(bitmap, edgeSize, edgeSize, true);
      this.gameBoard.pieceBitmapMap.put(key, bitmap);
    }
    
    canvas.drawBitmap(bitmap, padding, padding, null);
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        return true;
      case MotionEvent.ACTION_UP:
        this.gameBoard.togglePiece(this);
        this.invalidate();
        return true;
    }
    return false;
  }
  
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
        || keyCode == KeyEvent.KEYCODE_ENTER) {
      this.gameBoard.togglePiece(this);
    }
    return super.onKeyUp(keyCode, event);
  }
  
  public void toggleLights() {
    if (this.isBlock) {
      return;
    }
    this.isLightOn = !this.isLightOn;
    this.invalidate();
  }
  
  public boolean isLightOn() {
    return isLightOn;
  }
  
  public void enableBlock() {
    this.isBlock = true;
    this.invalidate();
  }
  
  public boolean isBlock() {
    return this.isBlock;
  }
  
}
