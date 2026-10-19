/*
 * Copyright (C) 2010-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.image;

import java.awt.Dimension;

/**
 *
 * @author franci
 */
public class FixedScaleDimension implements ScaleDimension
{
    final int width;
    final int height;

    public FixedScaleDimension(int width, int height)
    {
        this.width = width;
        this.height = height;
    }
    public FixedScaleDimension(int size)
    {
        this.width  = size;
        this.height = size;
    }
    
    @Override
    public Dimension convert(Dimension size)
    {
        return new Dimension(width,height);
    }
}
