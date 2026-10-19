/*
 * Copyright (C) 2010-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.io.virtual;

import io.nut.base.io.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author franci
 */
class FileVirtualFileSystem implements VirtualFileSystem
{
    final File file;

    public FileVirtualFileSystem(File file)
    {
        this.file = file;
    }
    public FileVirtualFileSystem(String fileName)
    {
        this.file = new File(fileName);
    }

    public long length()
    {
        return file.length();
    }

    public InputStream getInputStream() throws IOException
    {
        return new FileInputStream(file);
    }
    public String getAbsolutePath() throws IOException
    {
        return file.getAbsolutePath();
    }

    public String getCanonicalPath() throws IOException
    {
        return file.getCanonicalPath();
    }

    public String getPath()
    {
        return file.getPath();
    }
    public File getBaseFile()
    {
        return file;
    }

    public boolean exists()
    {
        return file.exists();
    }

    public boolean canRead()
    {
        return file.canRead();
    }

    public boolean canWrite()
    {
        return file.canWrite();
    }

    public boolean delete()
    {
        return file.delete();
    }

    public String getName()
    {
        return file.getName();
    }

    public FileVirtualFileSystem getCanonicalFile() throws IOException
    {
        File cf = file.getCanonicalFile();
        if(!cf.equals(file))
        {
            return new FileVirtualFileSystem(cf);
        }
        return this;
    }

    public VirtualFileSystem getAbsoluteFile() throws IOException
    {
        File af = file.getAbsoluteFile();
        if(!af.equals(file))
        {
            return new FileVirtualFileSystem(af);
        }
        return this;
    }

    public boolean isHidden()
    {
        return file.isHidden();
    }

    public boolean isFile()
    {
        return file.isFile();
    }

    public boolean isDirectory()
    {
        return file.isDirectory();
    }

    public boolean isLink() throws IOException
    {
        return FileUtils.isLink(file);
    }

    public boolean isLink(boolean path) throws IOException
    {
        return FileUtils.isLink(file, path);
    }

    public String[] splitPath()
    {
        return new String[]{file.toString()};
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
        final FileVirtualFileSystem other = (FileVirtualFileSystem) obj;
        if (this.file != other.file && (this.file == null || !this.file.equals(other.file)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return (this.file != null ? this.file.hashCode() : 0);
    }

    @Override
    public String toString()
    {
        return file.toString();
    }

    public String getLastPath()
    {
        return file.getPath();
    }

    public boolean isComplex()
    {
        return false;
    }

    public FileVirtualFileSystem getParentFile()
    {
        File parent = file.getParentFile();
        return (parent != null) ? new FileVirtualFileSystem(parent) : null;
    }
   
}
