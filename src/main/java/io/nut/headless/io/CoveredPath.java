/*
 * Copyright (C) 2010-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.io;

import io.nut.headless.io.virtual.VirtualFile;
import io.nut.headless.io.virtual.VirtualFiles;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author franci
 */
class CoveredPath
{
    private final Object lock = new Object();
    private final boolean symlinks;
    private final HashSet<VirtualFile> paths = new HashSet<>();

    public CoveredPath(boolean symlinks)
    {
        this.symlinks = symlinks;
    }

    public boolean add(VirtualFile file, boolean base) throws IOException
    {
        return add(file,base,file.isLink());
    }
    public boolean add(VirtualFile file, boolean base, boolean link) throws IOException
    {
        final VirtualFile canonical = file.getCanonicalFile();
        synchronized(lock)
        {
            if(paths.contains(canonical))
            {
                Logger.getLogger(CoveredPath.class.getName()).log(Level.FINE,"refused=''{0}'' as ''{1}''",new Object[]{file,canonical});
                return false;
            }
            //if( base || link || symlinks)
            if( base || link )
            {
                VirtualFile[] parents = VirtualFiles.getParentFiles(canonical);
                for (VirtualFile item : parents)
                {
                    if(paths.contains(item))
                    {
                        Logger.getLogger(CoveredPath.class.getName()).log(Level.FINE,"refused=''{0}'' as ''{1}'' by ''{2}''",new Object[]{file,canonical,item});
                        return false;
                    }
                }
                
                for (VirtualFile item : paths)
                {
                    if (VirtualFiles.isParentOf(item, canonical, true))
                    {
                        Logger.getLogger(CoveredPath.class.getName()).log(Level.FINE,"refused=''{0}'' as ''{1}'' by ''{2}''",new Object[]{file,canonical,item});
                        return false;
                    }
                }
                paths.add(canonical);
                Logger.getLogger(CoveredPath.class.getName()).log(Level.FINE,"path=''{0}'' as ''{1}''",new Object[]{file,canonical});
                return true;
            }
        }
        return true;
    }
}
