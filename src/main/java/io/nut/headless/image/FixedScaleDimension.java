/*
 *  FixedScaleDimension.java
 *
 *  Copyright (C) 2010-2023 francitoshi@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Report bugs or new features to: francitoshi@gmail.com
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
