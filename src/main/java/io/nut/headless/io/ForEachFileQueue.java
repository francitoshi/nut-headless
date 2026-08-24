/*
 * Copyright (C) 2009-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.io;

import io.nut.headless.io.virtual.VirtualFile;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author franci
 */
public class ForEachFileQueue extends ForEachFile
{

    private final File eof;
    private final BlockingQueue<File> fileQueue;
    private final BlockingQueue<String> nameQueue;

    public ForEachFileQueue(File[] file, FileFilter filter, BlockingQueue<File> fileQueue, BlockingQueue<String> nameQueue, File eof,ForEachFileOptions opt) throws IOException
    {
        super(file, filter,opt);
        this.eof = eof;
        this.fileQueue = fileQueue;
        this.nameQueue = nameQueue;
    }

    public ForEachFileQueue(File[] file, BlockingQueue<File> rawQueue, File eof) throws IOException
    {
        this(file, null, rawQueue, null, eof,null);
    }

    public ForEachFileQueue(File[] file, FileFilter filter, BlockingQueue<File> fileQueue, File eof) throws IOException
    {
        this(file, filter, fileQueue, null, eof,null);
    }

    public ForEachFileQueue(File[] file, FileFilter filter, BlockingQueue<File> fileQueue) throws IOException
    {
        this(file, filter, fileQueue, null, null,null);
    }

    public ForEachFileQueue(File[] file, int recursive, BlockingQueue<File> fileQueue) throws IOException
    {
        this(file, null, fileQueue, null, null, buildOptions(recursive));
    }

    private static ForEachFileOptions buildOptions(int recursive)
    {
        ForEachFileOptions opt = new ForEachFileOptions();
        opt.setRecursive(recursive);
        return opt;
    }

    @Override
    protected void doForEach(File file, String name)
    {
        try
        {
            if (fileQueue != null)
            {
                fileQueue.put(file);
            }
            if (nameQueue != null)
            {
                nameQueue.put(name);
            }
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(ForEachFileQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run()
    {
        super.run();
        try
        {
            if (eof != null)
            {
                if (fileQueue != null)
                {
                    fileQueue.put(eof);
                }
                if (nameQueue != null)
                {
                    nameQueue.put(eof.toString());
                }
            }
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(ForEachFileQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public File getEofFile()
    {
        return eof;
    }

    public String getEofName()
    {
        return (eof==null)?null:eof.toString();
    }

    public BlockingQueue<File> getFileQueue()
    {
        return fileQueue;
    }

    public BlockingQueue<String> getNameQueue()
    {
        return nameQueue;
    }

    @Override
    protected void doForEach(VirtualFile fe)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
