/*
 * Copyright (C) 2010-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.image.hash;

import io.nut.base.util.Comparators;
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

    @Override
    public int compareTo(Hash other)
    {
        int cmp = Integer.compare(this.hc, other.hashCode());
        if(cmp!=0)
        {
            if(other instanceof ImageHash)
            {
                final ImageHash o = (ImageHash) other;
                cmp = Comparators.compare(this.hash, o.hash);
            }
            else
            {
                cmp = 1;
            }
        }
        return cmp;
    }
}
