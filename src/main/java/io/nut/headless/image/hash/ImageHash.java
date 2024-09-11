/*
 *  ImageHash.java
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
package io.nut.headless.image.hash;

import io.nut.base.util.Hash;
import java.util.Arrays;

/**
 *
 * @author franci
 */
public class ImageHash implements Hash
{
    private final int w;
    private final int h;
    private final byte[] hash;
    private final int hc;

    public ImageHash(int w, int h, int hc, byte[] hash)
    {
        this.w    = w;
        this.h    = h;
        this.hc   = hc;
        this.hash = hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ImageHash other = (ImageHash) obj;
        if (this.w != other.w)
        {
            return false;
        }
        if (this.h != other.h)
        {
            return false;
        }
        return Arrays.equals(this.hash, other.hash);
    }
    @Override
    public int hashCode()
    {
        return hc;
    }
}
