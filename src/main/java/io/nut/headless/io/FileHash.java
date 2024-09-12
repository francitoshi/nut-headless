/*
 *  FileHash.java
 *
 *  Copyright (C) 2007-2024 francitoshi@gmail.com
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
package io.nut.headless.io;

import io.nut.base.crypto.Digest;
import io.nut.base.util.Hash;
import io.nut.headless.io.virtual.VirtualFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author franci
 */
public class FileHash implements Comparable<FileHash>, Hash
{   
    private final VirtualFile file;
    private final long size;
    private boolean exception = false;
    private FileDigest digest = null;
    private final Object lock = new Object();

    /**
     * Creates a new PackedFileHash instance from a File object.
     * @param file
     */
    public FileHash(VirtualFile file)
    {
        this.file = file;
        this.size = file.length();
    }
    public int compareTo(FileHash other)
    {
        if(file.equals(other.file))
        {
            return 0;
        }
        if (this.size != other.size)
        {
            return (this.size<other.size?-1:1);
        }
        //two files of size 0 are always equal
        if (this.size == 0)
        {
            return 0;
        }
        try
        {
            // exception
            if (this.exception || other.exception)
            {
                return file.compareTo(other.file);
            }
            //to determine if both points to the same taget
            if (this.file.getCanonicalPath().equals(other.file.getCanonicalPath()))
            {
                return 0;
            }
            return compareHash(other);
        }
        catch (IOException ex)
        {
            Logger.getLogger(FileHash.class.getName()).log(Level.SEVERE, null, ex);
        }
        return file.compareTo(other.file);
    }

    public long getSize()
    {
        return size;
    }

    public VirtualFile getFile()
    {
        return file;
    }

    private static final long[] SIZES = FileDigest.buildSizes();

    private int compareHash(FileHash other)
    {
        VirtualFile cause = null;
        try
        {
            final FileDigest digestA = this.getDigest();
            final FileDigest digestB = other.getDigest();
            digestA.keepOn();
            try
            {
                digestB.keepOn();
                try
                {
                    for (int i = 0; i < SIZES.length - 1 && SIZES[i]<size ; i++)
                    {
                        // avoid reading just few bytes in the next iteration
                        if( size<(SIZES[i]+SIZES[i+1])/2 )
                        {
                            break;
                        }
                        final long s = SIZES[i];

                        cause = this.file;
                        byte[] digA = digestA.getHash(s);
                        cause = other.file;
                        byte[] digB = digestB.getHash(s);
                        cause = null;
                        if (digA != null && digB != null)
                        {
                            int cmp = Arrays.compare(digA, digB);
                            if(cmp!=0)
                            {
                                return cmp;
                            }
                        }
                    }
                    cause = this.file;
                    byte[] digA = digestA.getHash();
                    cause = other.file;
                    byte[] digB = digestB.getHash();
                    cause = null;
                    if (digA != null && digB != null)
                    {
                        return Arrays.compare(digA, digB);
                    }
                    return 0;

                }
                finally
                {
                    digestB.keepOff();
                }
            }
            finally
            {
                digestA.keepOff();
            }
        }
        catch (ArchiveException | FileNotFoundException ex)
        {
            Logger.getLogger(FileHash.class.getName()).log(Level.WARNING, cause == null ? null : cause.getPath(), ex);
        }
        catch (IOException | CloneNotSupportedException | NoSuchAlgorithmException ex)
        {
            Logger.getLogger(FileHash.class.getName()).log(Level.SEVERE, cause == null ? null : cause.getPath(), ex);
        }
        exception = true;
        return file.compareTo(other.file);
    }

    private FileDigest getDigest() throws NoSuchAlgorithmException
    {
        synchronized (lock)
        {
            if (digest == null)
            {
                MessageDigest md = MessageDigest.getInstance(Digest.SHA256);
                digest = new FileDigest(file, md);
            }
            return digest;
        }
    }

    @Override
    public String toString()
    {
        return file.toString();
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
        final FileHash other = (FileHash) obj;

        if (this.file == other.file || (this.file != null && this.file.equals(other.file)))
        {
            return true;
        }
        if (this.size != other.size)
        {
            return false;
        }
        int cmp = compareHash(other);
        return (cmp==0);
    }

    @Override
    public int hashCode()
    {
        return (int)this.size;
    }

}
