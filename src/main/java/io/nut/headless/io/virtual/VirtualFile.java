/*
 * Copyright (C) 2010-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.io.virtual;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;

/**
 *
 * @author franci
 */
public class VirtualFile implements Comparable<VirtualFile>, Cloneable
{
    private final VirtualFileSystem fs;

    public VirtualFile(File file)
    {
        this.fs  = new FileVirtualFileSystem(file);
    }
    public VirtualFile(String pathname)
    {
        String[] items = pathname.split(VirtualFileSystem.pathSeparator);
        this.fs   = pathname.contains(VirtualFileSystem.pathSeparator)?new ZipVirtualFileSystem(items,pathname):new FileVirtualFileSystem(pathname);
    }
    public VirtualFile(VirtualFile parent, String child)
    {
        String[] parentItems = parent.splitPath();
        String[] items = Arrays.copyOf(parentItems, parentItems.length+1);
        items[parentItems.length]=child;
        this.fs = new ZipVirtualFileSystem(items);
    }
    public VirtualFile(VirtualFile parent, ArchiveEntry child)
    {
        String[] parentItems = parent.splitPath();
        String[] items = Arrays.copyOf(parentItems, parentItems.length+1);
        items[parentItems.length]=child.toString();
        this.fs = new ZipVirtualFileSystem(items,child);
    }
    VirtualFile(VirtualFileSystem fs)
    {
        this.fs = fs;
    }
    public String[] splitPath()
    {
        return fs.splitPath();
    }
    public long length()
    {
        return fs.length();
    }
    public InputStream getInputStream() throws IOException, ArchiveException
    {
        return fs.getInputStream();
    }
    public String getCanonicalPath() throws IOException
    {
        return fs.getCanonicalPath();
    }
    public String getAbsolutePath() throws IOException
    {
        return fs.getAbsolutePath();
    }
    public VirtualFile getCanonicalFile() throws IOException
    {
        VirtualFileSystem cf = fs.getCanonicalFile();
        if(fs!=cf)
        {
            return new VirtualFile(cf);
        }
        return this;
    }
    public VirtualFile getAbsoluteFile() throws IOException
    {
        VirtualFileSystem af = fs.getAbsoluteFile();
        if(fs!=af)
        {
            return new VirtualFile(af);
        }
        return this;
    }

    public String getPath()
    {
        return fs.getPath();
    }
    public File getBaseFile()
    {
        return fs.getBaseFile();
    }
    public boolean exists()
    {
        return fs.exists();
    }
    public boolean canRead()
    {
        return fs.canRead();
    }
    public boolean canWrite()
    {
        return fs.canWrite();
    }
    public boolean delete()
    {
        return fs.delete();
    }
    public boolean isHidden()
    {
        return fs.isHidden();
    }
    public boolean isFile()
    {
        return fs.isFile();
    }
    public boolean isDirectory()
    {
        return fs.isDirectory();
    }
    public boolean isComplex()
    {
        return fs.isComplex();
    }

    @Override
    public String toString()
    {
        return fs.toString();
    }

    public String getName()
    {
        return fs.getName();
    }

    public String getLastPath()
    {
        return fs.getLastPath();
    }

    @Override
    public VirtualFile clone() throws CloneNotSupportedException
    {
        return (VirtualFile) super.clone();
    }
    public static VirtualFileFilter buildFilter(final FileFilter filter)
    {
        if (filter != null)
        {
            return new VirtualFileFilter()
            {
                public boolean accept(VirtualFile pathname)
                {
                    return filter.accept(new File(pathname.getLastPath()));
                }
            };
        }
        return null;
    }
    public static VirtualFile[] asVirtualFile(File[] files)
    {
        if(files!=null)
        {
            VirtualFile[] pf = new VirtualFile[files.length];
            for(int i=0;i<files.length;i++)
            {
                pf[i] = new VirtualFile(files[i]);
            }
            return pf;
        }
        return null;
    }

    public boolean isLink() throws IOException
    {
        return fs.isLink();
    }
    //
    public int compareTo(VirtualFile file)
    {
        // ES NECESARIO REPENSAR COMO COMPARAR FICHEROS DE DISTINTA INDOLE
        return fs.toString().compareTo(file.toString());
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
        final VirtualFile other = (VirtualFile) obj;
        if (this.fs != other.fs && (this.fs == null || !this.fs.equals(other.fs)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return (this.fs != null ? this.fs.hashCode() : 0);
    }

    VirtualFile getParentFile()
    {
        VirtualFileSystem parent = fs.getParentFile();
        return (parent != null) ? new VirtualFile(parent) : null;
    }



}
