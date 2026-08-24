/*
 * Copyright (C) 2007-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.io;

import io.nut.headless.io.virtual.VirtualFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.ArchiveException;

/**
 *
 * @author franci
 */
public class PackedFileHash
{
    private static final String SHA256 = "SHA-256";
    private static final int BUF_SIZE = 64 * 1024;
    private final VirtualFile file;
    private final long size;
    private byte[] fastHash = null;
    private byte[] fullHash = null;
    private boolean exception = false;
    private FileDigest digest = null;
    private final Object lock = new Object();
    private static final boolean pow2 = true;

    /**
     * Creates a new PackedFileHash instance from a File object.
     * @param file
     */
    public PackedFileHash(VirtualFile file)
    {
        this.file = file;
        this.size = file.length();
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
        final PackedFileHash other = (PackedFileHash) obj;
        if (file.equals(other.file))
        {
            return true;
        }
        if (this.size != other.size)
        {
            return false;
        }
        //two files of size 0 are always equal
        if (this.size == 0)
        {
            return true;
        }
        try
        {
            // exception
            if (this.exception || other.exception)
            {
                return false;
            }

            //to determine if both points to the same taget
            if (this.file.getCanonicalPath().equals(other.file.getCanonicalPath()))
            {
                return true;
            }
            if (pow2)
            {
                return equalsHash(other);
            }
            else
            {
                if (!Arrays.equals(this.getFastSHA256(), other.getFastSHA256()))
                {
                    return false;
                }
                if (!Arrays.equals(this.getFullSHA256(), other.getFullSHA256()))
                {
                    return false;
                }
            }
        }
        catch (ArchiveException ex)
        {
            Logger.getLogger(PackedFileHash.class.getName()).log(Level.WARNING, null, ex);
            exception = true;
            return false;
        }
        catch (IOException ex)
        {
            Logger.getLogger(PackedFileHash.class.getName()).log(Level.SEVERE, null, ex);
            exception = true;
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return (int) (file.length() % Integer.MAX_VALUE);
    }

    private void buildFastHash() throws IOException, ArchiveException
    {
        synchronized (lock)
        {
            if (fastHash != null)
            {
                return;
            }
            boolean error = true;
            InputStream fis = null;
            try
            {
                MessageDigest sha256 = MessageDigest.getInstance(SHA256);
                if (size > 0)
                {
                    byte[] buf = new byte[1024];
                    fis = file.getInputStream();
                    int r = fis.read(buf);
                    fis.close();
                    if (r > 0)
                    {
                        sha256.update(buf, 0, r);
                    }
                }

                fastHash = sha256.digest();
                error = false;
            }
            catch (NoSuchAlgorithmException ex)
            {
                Logger.getLogger(PackedFileHash.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally
            {
                if (error)
                {
                    this.exception = true;
                }
                try
                {
                    if (fis != null)
                    {
                        fis.close();
                    }
                }
                catch (IOException ex)
                {
                    Logger.getLogger(PackedFileHash.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void buildFullHash() throws IOException, ArchiveException
    {
        synchronized (lock)
        {
            if (fullHash != null)
            {
                return;
            }

            if (size <= 1024)
            {
                fullHash = fastHash;
                return;
            }
            boolean error = true;
            try (InputStream fis = file.getInputStream())
            {
                MessageDigest sha256 = MessageDigest.getInstance(SHA256);
                byte[] buf = new byte[BUF_SIZE];
                int r;

                while ((r = fis.read(buf)) > 0)
                {
                    sha256.update(buf, 0, r);
                }

                fullHash = sha256.digest();
                error = false;
            }
            catch (NoSuchAlgorithmException ex)
            {
                Logger.getLogger(PackedFileHash.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally
            {
                if (error)
                {
                    this.exception = true;
                }
            }
        }
    }

    public long getSize()
    {
        return size;
    }

    public VirtualFile getFile()
    {
        return file;
    }

    public byte[] getFastSHA256() throws IOException, ArchiveException
    {
        if (fastHash == null)
        {
            buildFastHash();
        }
        return fastHash;
    }

    public byte[] getFullSHA256() throws IOException, ArchiveException
    {
        if (fullHash == null)
        {
            buildFullHash();
        }
        return fullHash;
    }
    private static final long[] SIZES = FileDigest.buildSizes();

    private boolean equalsHash(PackedFileHash other)
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
                            if (!Arrays.equals(digA, digB))
                            {
                                return false;
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
                        return Arrays.equals(digA, digB);
                    }
                    return false;

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




        catch (ArchiveException ex)
        {
            Logger.getLogger(PackedFileHash.class.getName()).log(Level.WARNING, cause == null ? null : cause.getPath(), ex);
        }        catch (FileNotFoundException ex)
        {
            Logger.getLogger(PackedFileHash.class.getName()).log(Level.WARNING, cause == null ? null : cause.getPath(), ex);
        }        catch (IOException ex)
        {
            Logger.getLogger(PackedFileHash.class.getName()).log(Level.SEVERE, cause == null ? null : cause.getPath(), ex);
        }        catch (CloneNotSupportedException ex)
        {
            Logger.getLogger(PackedFileHash.class.getName()).log(Level.SEVERE, cause == null ? null : cause.getPath(), ex);
        }        catch (NoSuchAlgorithmException ex)
        {
            Logger.getLogger(PackedFileHash.class.getName()).log(Level.SEVERE, cause == null ? null : cause.getPath(), ex);
        }
        exception = true;
        return false;
    }

    private FileDigest getDigest() throws NoSuchAlgorithmException
    {
        synchronized (lock)
        {
            if (digest == null)
            {
                MessageDigest md = MessageDigest.getInstance(FileDigest.SHA256);
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

}
