/*
 *  ImageHasher.java
 *
 *  Copyright (C) 2026 francitoshi@gmail.com
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
package io.nut.headless.image.hasher;


import io.nut.base.crypto.Kripto;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;

/**
 * Loads an image (JPEG, BMP, PNG, GIF, …), resizes it to N×N pixels using
 * <a href="https://github.com/coobird/thumbnailator">Thumbnailator</a>, and
 * returns a flat {@code byte[N*N]} of grayscale values (1 byte per pixel).
 *
 * <p>Thumbnailator handles format detection, colour-space conversion, and
 * high-quality downscaling internally, removing the need for manual
 * {@code Graphics2D} setup.
 *
 * <p>Compatible with Java 8+. Required dependency (Maven):
 * <pre>
 *   &lt;dependency&gt;
 *     &lt;groupId&gt;net.coobird&lt;/groupId&gt;
 *     &lt;artifactId&gt;thumbnailator&lt;/artifactId&gt;
 *     &lt;version&gt;0.4.20&lt;/version&gt;
 *   &lt;/dependency&gt;
 * </pre>
 */
public class ImageHasher 
{
    static final Kripto KRIPTO = Kripto.getInstance();
    /** Target dimension: the image is resized to {@code size × size} pixels. */
    private final int size;

    /**
     * @param size target dimension N (N×N grid). Must be &gt;= 1.
     * @throws IllegalArgumentException if {@code size} &lt; 1.
     */
    public ImageHasher(int size) 
    {
        if (size < 1) 
        {
            throw new IllegalArgumentException("size must be >= 1, got: " + size);
        }
        this.size = size;
    }

    // -------------------------------------------------------------------------
    // Public pipeline entry point
    // -------------------------------------------------------------------------

    /**
     * Full pipeline: load → resize → grayscale bytes, while computing the SHA-256
     * of the raw file bytes as a side effect of reading.
     *
     * @param file image file to process.
     * @return two-element array:
     *         {@code [0]} SHA-256 digest of the raw file (32 bytes),
     *         {@code [1]} grayscale pixels ({@code size*size} bytes, use {@code & 0xFF}
     *         to recover the 0–255 range, row-major top-left first).
     * @throws IOException if the file cannot be read or the format is unsupported.
     */
    public byte[][] process(File file) throws IOException 
    {
        return process(new FileInputStream(file));
    }
    public byte[][] process(InputStream in) throws IOException 
    {
        MessageDigest digest = KRIPTO.sha256.get();
        
        DigestInputStream dis  = new DigestInputStream(in, digest);
        BufferedImage original = load(dis);
        
        byte[]        sha256   = digest.digest();
        BufferedImage resized  = resize(original);
        
        byte[]        pixels   = toGrayscaleBytes(resized);
        
        return new byte[][] { sha256, pixels };
    }

    // -------------------------------------------------------------------------
    // Pipeline stages
    // -------------------------------------------------------------------------

    /**
     * Stage 1 – Decode an image from an {@link InputStream}.
     *
     * <p>Callers should wrap the underlying stream in a {@link DigestInputStream}
     * before passing it here so that the digest accumulates as bytes are read.
     * {@code ImageIO} is used directly to decode at original dimensions without scaling.
     *
     * @param in source stream (typically a {@link DigestInputStream}).
     * @return decoded image in its original size.
     * @throws IOException              if the stream cannot be read.
     * @throws IllegalArgumentException if the format is not recognised by {@link ImageIO}.
     */
    public BufferedImage load(InputStream in) throws IOException 
    {
        BufferedImage img = ImageIO.read(in);
        if (img == null) 
        {
            throw new IllegalArgumentException("Unsupported or unreadable image format");
        }
        return img;
    }

    /**
     * Stage 2 – Resize to {@code size × size} pixels.
     *
     * <p>Thumbnailator selects an appropriate interpolation algorithm
     * (progressive bilinear for large downscales) automatically.
     * {@code keepAspectRatio(false)} ensures an exact N×N output regardless
     * of the source proportions.
     *
     * @param src source image (any size/type).
     * @return new {@link BufferedImage} of exactly {@code size × size} pixels.
     * @throws IOException if the resize operation fails internally.
     */
    public BufferedImage resize(BufferedImage src) throws IOException {
        return Thumbnails.of(src)
                .size(size, size)
                .keepAspectRatio(false)
                .asBufferedImage();
    }

    /**
     * Stage 3 – Convert the resized image to a flat grayscale byte array.
     *
     * <p>Luminance formula (ITU-R BT.601):
     * <pre>Y = (299·R + 587·G + 114·B) / 1000</pre>
     *
     * @param src resized image ({@code size × size}).
     * @return {@code byte[size * size]} grayscale values (row-major, top-left first).
     */
    public byte[] toGrayscaleBytes(BufferedImage src) {
        byte[] pixels = new byte[size * size];
        int index = 0;

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                pixels[index++] = (byte) toGray(src.getRGB(col, row));
            }
        }
        return pixels;
    }

    // -------------------------------------------------------------------------
    // Internal helper
    // -------------------------------------------------------------------------

    /**
     * Converts a packed ARGB int to an 8-bit luminance value (ITU-R BT.601).
     *
     * @param rgb packed ARGB pixel.
     * @return luminance in [0, 255].
     */
    private static int toGray(int rgb) 
    {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >>  8) & 0xFF;
        int b =  rgb        & 0xFF;
        return (299 * r + 587 * g + 114 * b) / 1000;
    }
}
