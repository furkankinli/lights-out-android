package com.google.games.lightsout;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

public class GameBoardLayout extends LinearLayout {

  private int size;

  public GameBoardLayout(Context context, int size) {
    super(context);
    this.size = size;
  }
  
  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int availWidth = r - l;
    int availHeight = b - t;
    int edgeSize = Math.min(availHeight, availWidth);
    int pieceEdgeSize = edgeSize / size;
    edgeSize = pieceEdgeSize * size;
    int startX = (availWidth - edgeSize) / 2;
    int startY = (availHeight - edgeSize) / 2;
    
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      int xOffset = (i % size) * pieceEdgeSize;
      int yOffset = (i / size) * pieceEdgeSize;
      child.layout(
          startX + xOffset, 
          startY + yOffset, 
          startX + xOffset + pieceEdgeSize, 
          startY + yOffset + pieceEdgeSize);
    }
  }

}
