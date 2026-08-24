/*
 * Copyright (C) 2010-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.image.hash;

import java.util.ArrayList;
import io.nut.base.util.Hash;

/**
 *
 * @author franci
 */
public class StickyImageHash implements Hash
{
    static private final Object lock = new Object();
    private final int w;
    private final int h;
    private final int hc;
    private final int colorThreshold;
    private final int countThreshold;
    private ArrayList<byte[]> hash;


    public StickyImageHash(int w, int h, int hc, byte[] hash, float colorThresold, float countThresold)
    {
        this.w    = w;
        this.h    = h;
        this.hc   = hc;
        this.colorThreshold = (int) (colorThresold * 256);
        this.countThreshold = (int) (countThresold * hash.length);
        this.hash = new ArrayList<>();
        this.hash.add(hash);
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
        final StickyImageHash other = (StickyImageHash) obj;
        if (this.w != other.w)
        {
            return false;
        }
        if (this.h != other.h)
        {
            return false;
        }
        synchronized(lock)
        {
            if (this.hash.equals(other.hash))
            {
                return true;
            }
            
            for(byte[] thisItem : this.hash)
            {
                for(byte[] otherItem : other.hash)
                {
                    if(thisItem == otherItem)
                    {
                        return true;
                    }
                    int count = 0;
                    for(int i=0;i<thisItem.length && count<=countThreshold;i++)
                    {
                        int thisColor = thisItem[i]>=0?thisItem[i]:thisItem[i]+256;
                        int otherColor= otherItem[i]>=0?otherItem[i]:otherItem[i]+256;
                        int colorDiff = thisColor-otherColor;
                        colorDiff = Math.max(colorDiff,-colorDiff);
                        if(colorDiff>colorThreshold)
                        {
                            count++;
                        }
                    }
                    if(count<countThreshold)
                    {
                        this.hash = merge(this.hash,other.hash);
                        other.hash=this.hash;
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @Override
    public int hashCode()
    {
        return hc;
    }

    private static ArrayList<byte[]> merge(ArrayList<byte[]>... lists)
    {
        ArrayList<byte[]> hash = new ArrayList<>();
        for(int i=0;i<lists.length;i++)
        {
            hash.addAll(lists[i]);
        }
        return hash;
    }

    @Override
    public int compareTo(Hash other)
    {
        if(this.equals(other))
        {
            return 0;
        }
        int cmp = Integer.compare(this.hc, other.hashCode());
        if(cmp == 0 && other instanceof StickyImageHash)
        {
            final StickyImageHash o = (StickyImageHash) other;
            cmp = Integer.compare(this.hash.size(), o.hash.size());
        }
        return cmp;
    }
}
