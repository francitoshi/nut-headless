/*
 * Copyright (C) 2010-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.io.virtual;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.archivers.ArchiveException;

/**
 *
 * @author franci
 */
interface VirtualFileSystem //extends Comparable<VirtualFileSystem>
{
    String pathSeparator   = "!";
    char pathSeparatorChar = '!';

    boolean canRead();
    boolean canWrite();
    boolean delete();
    boolean exists();
    String getCanonicalPath() throws IOException;
    VirtualFileSystem getCanonicalFile() throws IOException;
    String getAbsolutePath() throws IOException;
    VirtualFileSystem getAbsoluteFile() throws IOException;
    File getBaseFile();
    InputStream getInputStream() throws IOException, ArchiveException;
    String getName();
    String getPath();
    String[] splitPath();
    boolean isHidden();
    boolean isFile();
    boolean isDirectory();
    long length();
    boolean isLink() throws IOException;
    boolean isLink(boolean path) throws IOException;
    String getLastPath();
    boolean isComplex();
    VirtualFileSystem getParentFile();
}
