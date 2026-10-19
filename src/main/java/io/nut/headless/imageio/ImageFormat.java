/*
 * Copyright (C) 2010-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.imageio;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author franci
 */
public class ImageFormat
{

    public static final String BMP = "bmp";
    public static final String PNG = "png";
    public static final String JPEG = "jpeg";
    public static final String GIF = "gif";
    private static final String[][] EXT_FMT =
    {
        {".bmp", BMP},
        {".png", PNG},
        {".jpg", JPEG},
        {".jpeg", JPEG},
        {".gif", GIF},
    };

    public static String getFormat(String fileName)
    {
        String name = fileName.toLowerCase();
        for (String[] item : EXT_FMT)
        {
            if (name.endsWith(item[0]))
            {
                return item[1];
            }
        }
        return null;
    }

    public static String getFormat(File file)
    {
        return getFormat(file.getName());
    }
    public static FileFilter getImageFileFilter()
    {
        return new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return (getFormat(pathname)!=null);
            }
        };
    }
}
