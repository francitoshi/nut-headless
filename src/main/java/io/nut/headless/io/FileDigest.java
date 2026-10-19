/*
 * Copyright (C) 2009-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.io;

import io.nut.headless.io.virtual.VirtualFile;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.ArchiveException;

/**
 *
 * @author franci
 */
public class FileDigest
{
    public static final String SHA256 = "SHA256";
    
    private static int defBufSize = 256*1024;
    private final int bufSize = defBufSize;
    private final Object lock = new Object();
    private final File file;
    private final VirtualFile pf;
    private final long length;
    private final byte[][] hashes = new byte[64][];
    private final long[] sizes = new long[64];
    private byte[] hash = null;
    private int hashCount = 0;
    private long count = 0;
    private final MessageDigest md;
    //resources to free in keepOff
    private int keep = 0;
    private InputStream data = null;
    

    public FileDigest(File file, MessageDigest md) throws NoSuchAlgorithmException
    {
        this.file = file;
        this.pf =null;
        this.length = file.length();
        this.md = md;
    }
    public FileDigest(VirtualFile file, MessageDigest md) throws NoSuchAlgorithmException
    {
        this.file = null;
        this.pf =file;
        this.length = file.length();
        this.md = md;
    }

    public void keepOn()
    {
        synchronized (lock)
        {
            keep++;
        }
    }

    public void keepOff() throws IOException
    {
        synchronized (lock)
        {
            keep--;
            assert (keep>=0);
            if (keep <= 0)
            {
                if (data != null)
                {
                    data.close();
                    data = null;
                }
            }
        }
    }

    public byte[] getHash() throws IOException, CloneNotSupportedException, ArchiveException
    {
        synchronized (lock)
        {
            if(hash==null)
            {
                hash = getHash(length);
            }
            return hash;
        }
    }

    public byte[] getHash(long size) throws IOException, CloneNotSupportedException, ArchiveException
    {
        synchronized (lock)
        {
            size = Math.min(size, length);
            for (int i = 0; i < hashCount && size <= count; i++)
            {
                if (hashes[i] != null && sizes[i]==size)
                {
                    return hashes[i];
                }
            }
            if(size<count)
            {
                return null;
            }
            hashes[hashCount] = buildHash(size);
            sizes[hashCount] = size;
            return hashes[hashCount++];
        }
    }

    private byte[] buildHash(long size) throws IOException, CloneNotSupportedException, ArchiveException
    {
        if( size > 0 )
        {
            if (data == null)
            {
                data = getInputStream();
                data.skip(count);
            }
            byte[] buf = new byte[(int)Math.min(bufSize,length)];
            size = Math.min(size,length);

            while (count < size)
            {
                int r = (int) Math.min(buf.length, size - count);
                assert (r>=0 && r<=buf.length);
                r = data.read(buf, 0, r);

                if(r<0)
                {
                    Logger.getLogger(FileDigest.class.getName()).log(Level.INFO, "can't read from {0}",pf);
                    break;
                }
                
                md.update(buf, 0, r);
                count += r;
            }
        }

        MessageDigest md2 = (count < length) ? (MessageDigest) md.clone() : md;
        return md2.digest();
    }

    public static long[] buildSizes()
    {
        int i;
        long pow2  = 0;
        long[] sizes = new long[64];
        sizes[0] = 1024;
        sizes[1] = 4096;
        pow2=3;
        //fibonacci like computed values
        for(i=2;i<10;i++)
        {
            sizes[i] = sizes[i-1]*2;
            pow2= (pow2<<1)+1;
        }
        //fibonacci like computed values
        for(;i<sizes.length && (sizes[i-1]>pow2) ;i++)
        {
            sizes[i] = sizes[i-1]+sizes[i-2];
            pow2= (pow2<<1)+1;
        }
        //pow2 computed values
        for(;i<sizes.length;i++)
        {
            sizes[i] = pow2;
            pow2= (pow2<<1)+1;
        }
        assert(sizes[63]==Long.MAX_VALUE);
        return sizes;
    }

    private InputStream getInputStream() throws IOException, ArchiveException
    {
        if(file!=null)
        {
            return new BufferedInputStream(new FileInputStream(file));
        }
        if(pf!=null)
        {
            return new BufferedInputStream(pf.getInputStream());
        }
        return null;
    }

}
