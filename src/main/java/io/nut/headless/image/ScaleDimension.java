/*
 * Copyright (C) 2009-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.image;

import java.awt.Dimension;

/**
 *
 * @author franci
 */
public interface ScaleDimension
{
    Dimension convert(Dimension size);
}
