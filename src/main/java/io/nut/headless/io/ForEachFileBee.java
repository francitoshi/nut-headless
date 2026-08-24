/*
 * Copyright (C) 2009-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.io;

import io.nut.base.util.concurrent.hive.Bee;
import io.nut.headless.io.virtual.VirtualFile;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 *
 * @author franci
 */
public class ForEachFileBee extends ForEachFile
{
    private final boolean eof;
    private final Bee<VirtualFile> fileBee;
    private final Bee<String> nameBee;

    public ForEachFileBee(File[] file, FileFilter filter, ForEachFileOptions opt, Bee<VirtualFile> fileBee, Bee<String> nameBee, boolean eof) throws IOException
    {
        super(file, filter,opt);
        this.eof      = eof;
        this.fileBee = fileBee;
        this.nameBee = nameBee;
    }

    public ForEachFileBee(File[] file, ForEachFileOptions opt, Bee<VirtualFile> bee, boolean eof) throws IOException
    {
        this(file, null, opt, bee, null, eof);
    }

    @Override
    protected void doForEach(VirtualFile fe)
    {
        if (fe == null)
        {
            return;
        }
        if (fileBee != null)
        {
            fileBee.accept(fe);
        }
        if (nameBee != null)
        {
            nameBee.accept(fe.toString());
        }
    }

    @Override
    public void run()
    {
        super.run();
        if (eof)
        {
            if (fileBee != null)
            {
                fileBee.shutdown();
            }
            if (nameBee != null)
            {
                nameBee.shutdown();
            }
        }
    }

    public Bee<VirtualFile> getFileBee()
    {
        return fileBee;
    }

    public Bee<String> getNameBee()
    {
        return nameBee;
    }
}
