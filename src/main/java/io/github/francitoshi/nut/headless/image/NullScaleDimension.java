/*
 * Copyright (C) 2010-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.headless.image;

import java.awt.Dimension;

/**
 *
 * @author franci
 */
public class NullScaleDimension implements ScaleDimension
{
    @Override
    public Dimension convert(Dimension size)
    {
        return size;
    }
}
