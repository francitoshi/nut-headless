/*
 *  ForEachFileBee.java
 *
 *  Copyright (C) 2009-2024 francitoshi@gmail.com
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
        if (fileBee != null)
        {
            if(fe==null)
            {
                System.out.println("fe=null");
            }
            fileBee.send(fe);
        }
        if (nameBee != null)
        {
            nameBee.send(fe.toString());
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
