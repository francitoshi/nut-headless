/*
 * VirtualFilePool.java
 *
 * Copyright (c) 2007-2024 francitoshi@gmail.com
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
package io.nut.headless.io.virtual;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 *
 * @author franci
 */
public class VirtualFilePool
{
    private static final boolean zipOpt = true;
    private static final AtomicInteger fisActive = new AtomicInteger();
    public VirtualFilePool()
    {
    }

    private InputStream getEntryInputStream(InputStream in, String entryName) throws IOException, ArchiveException
    {
        final ArchiveInputStream ais = asf.createArchiveInputStream(new BufferedInputStream(in));
        ArchiveEntry ae = null;
        while( (ae=ais.getNextEntry())!=null)
        {
            if(entryName.equals(ae.getName()))
            {
                return ais;
            }
        }
        return null;
    }
    private InputStream getEntryInputStream(File file, String entryName) throws IOException, ArchiveException
    {
        String fileName = file.toString().toLowerCase();
        if( zipOpt && ( fileName.endsWith(".zip") || fileName.endsWith(".jar")) )
        {
            return getZipEntryInputStream(file, entryName);
        }
        final FileInputStream fis = new FileInputStream(file);
        return new FilterInputStream(getEntryInputStream(new FileInputStream(file),entryName))
        {
            final int id = fisActive.getAndIncrement();
            @Override
            public void close() throws IOException
            {
                fis.close();
                fisActive.getAndDecrement();
            }
        };
    }
    private InputStream getZipEntryInputStream(File file, String entryName) throws IOException, ArchiveException
    {
        final ZipFile ze = new ZipFile(file);
        final ZipArchiveEntry zae = ze.getEntry(entryName);
        if(zae==null)
        {
            ze.close();
            return null;
        }
        return new FilterInputStream(ze.getInputStream(zae))
        {
            final int id = fisActive.getAndIncrement();
            @Override
            public void close() throws IOException
            {
                ze.close();
                fisActive.getAndDecrement();
            }
        };
    }

    public InputStream get(VirtualFile file) throws IOException, ArchiveException
    {
        return get(file.splitPath());
    }
    private static final ArchiveStreamFactory asf = new ArchiveStreamFactory();


    InputStream get(String[] paths) throws IOException, ArchiveException
    {
        if(paths.length==0)
        {
            return null;
        }
        if(paths.length==1)
        {
            return new FileInputStream(paths[0]);
        }
        final InputStream fin = getEntryInputStream(new File(paths[0]), paths[1]);
        InputStream in = fin;
        StringBuilder curPath = new StringBuilder(paths[0]).append(paths[1]);
        try
        {
            for(int i=2;i<paths.length;i++)
            {
                curPath.append(VirtualFileSystem.pathSeparator).append(paths[i]);
                in = getEntryInputStream(in, paths[i]);
                if(in==null)
                {
                    throw new FileNotFoundException("'"+curPath+"' not found");
                }
            }
            in = new FilterInputStream(in)
            {
                final int id = fisActive.getAndIncrement();
                @Override
                public void close() throws IOException
                {
                    super.close();
                    fin.close();
                    fisActive.getAndDecrement();
                }
            };
            return in;
        }
        catch(IOException ex)
        {
            fin.close();
            throw ex;
        }
    }
}
