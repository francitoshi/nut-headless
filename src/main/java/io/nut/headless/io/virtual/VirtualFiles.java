/*
 *  VirtualFiles.java
 *
 *  Copyright (C) 2010-2024 francitoshi@gmail.com
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

import io.nut.base.io.FileUtils;
import io.nut.base.util.Sorts;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author franci
 */
public class VirtualFiles
{

    public static VirtualFile getNoDotFile(VirtualFile file) throws IOException
    {
        if(!file.isComplex())
        {
            return new VirtualFile(FileUtils.getNoDotFile(file.getBaseFile()));
        }
        return file;
    }

    public static boolean isParentOf(VirtualFile parent, VirtualFile child, boolean canonical) throws IOException
    {
        String[] parentItems = parent.splitPath();
        String[] childItems = child.splitPath();

        if(parentItems.length==1 && parentItems.length==childItems.length)
        {
            return FileUtils.isParentOf(new File(parentItems[0]), new File(childItems[0]), canonical);
        }
        if(parentItems.length >= childItems.length)
        {
            return false;
        }
        for(int i=0;i<parentItems.length;i++)
        {
            if(!parentItems[i].equals(childItems[i]))
                return false;
        }
        return true;
    }
    
    public static VirtualFile[] getParentFiles(VirtualFile file, boolean includeFile)
    {
        VirtualFile item = file;
        ArrayList<VirtualFile> parents = new ArrayList<>();
        if(includeFile)
        {
            parents.add(item);
        }
        while((item = item.getParentFile()) != null)
        {
            parents.add(item);
        }
        return Sorts.reverseOf(parents.toArray(new VirtualFile[0]));
    }

    public static VirtualFile[] getParentFiles(VirtualFile file)
    {
        return getParentFiles(file, false);
    }   
}
