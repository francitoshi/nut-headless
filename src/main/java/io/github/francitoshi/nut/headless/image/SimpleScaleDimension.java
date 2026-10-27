/*
 * Copyright (C) 2009-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.headless.image;

import java.awt.Dimension;

/**
 *
 * @author franci
 */
public class SimpleScaleDimension implements ScaleDimension
{
    final int minWidth;
    final int minHeight;
    final int maxWidth;
    final int maxHeight;
    final double ratio;

    public SimpleScaleDimension(int minWidth, int minHeight, int maxWidth, int maxHeight, double ratio) 
    {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.ratio = ratio;
    }

    public SimpleScaleDimension(int minSize, int maxSize, double ratio)
    {
        this(minSize,minSize,maxSize,maxSize,ratio);
    }
    public SimpleScaleDimension(int minSize, int maxSize)
    {
        this(minSize,minSize,maxSize,maxSize,1.0);
    }
    
    @Override
    public Dimension convert(Dimension size)
    {
        double minWidthRatio = (double)minWidth / (double)size.width;
        double minHeightRatio = (double)minHeight / (double)size.height;

        double maxWidthRatio = (double)maxWidth / (double)size.width;
        double maxHeightRatio = (double)maxHeight / (double)size.height;
        
        double ratio  = this.ratio;
        
        //grow
        if(minWidthRatio>ratio)
            ratio = minWidthRatio;
        if(minHeightRatio>ratio)
            ratio = minHeightRatio;

        //shrink
        if(maxWidthRatio<ratio)
            ratio = maxWidthRatio;
        if(maxHeightRatio<ratio)
            ratio = maxHeightRatio;
        
        if(ratio==1.0)
            return size;

        final int w= (int)Math.round(size.width*ratio);
        final int h= (int)Math.round(size.height*ratio);
        return new Dimension(w,h);
    }

}
