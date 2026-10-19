/*
 * Copyright (C) 2009-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.image;

import java.awt.Dimension;

/**
 *
 * @author franci
 */
public class FixedRatiosScaleDimension implements ScaleDimension
{
    private final int max;
    private final int min;
    private static final double[] RATIOS = { 1.0, 0.9, 0.8, 0.75, 2.0/3.0, 0.6, 0.5, 0.4, 1.0/3.0, 0.25, 0.2, 0.1, 0.05, 0.025, 0.0125, 0.01};
    private final double[] ratios;

    public FixedRatiosScaleDimension(int max, int min)
    {
        this.max = max;
        this.min = min;
        this.ratios = RATIOS;
    }  
    
    @Override
    public Dimension convert(Dimension size)
    {
        for( double r : ratios)
        {
            Dimension dim = convert(size, r);
            if(dim!=null)
                return dim;
        }
        return size;
    }
    public Dimension convert(Dimension size,double ratio)
    {
        final int w= (int)Math.round(size.width*ratio);
        final int h= (int)Math.round(size.height*ratio);

        if(min>0)
        {
            if(w<min || h<min)
            {
                return null;
            }
        }
        if(max>0)
        {
            if(w>max || h>max)
            {
                return null;
            }
        }
        if(ratio==1.0)
            return size;

        return new Dimension(w,h);
    }

}
