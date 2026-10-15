/*
 * Copyright (C) 2010-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */

package io.nut.headless.io.virtual;

import io.nut.base.commons.lang.Sorts;
import io.nut.base.io.FileUtils;
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
