/*
 * Copyright (C) 2010-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.headless.image.hash;

import io.github.francitoshi.nut.base.commons.jca.Digest;
import io.github.francitoshi.nut.base.commons.jca.Kr;
import io.github.francitoshi.nut.base.util.Byter;
import io.github.francitoshi.nut.base.util.Hash;
import io.github.francitoshi.nut.headless.image.ScaleDimension;
import io.github.francitoshi.nut.headless.image.SimpleScaleDimension;
import io.github.francitoshi.nut.headless.imageio.ScaleImage;
import io.github.francitoshi.nut.headless.io.virtual.VirtualFile;
import io.github.francitoshi.nut.headless.io.virtual.VirtualFilePool;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.compress.archivers.ArchiveException;
/**
 *
 * @author franci
 */
public class ImageHashBuilder
{
    private static final AtomicInteger count = new AtomicInteger();
    private static final VirtualFilePool pool = new VirtualFilePool();
    private static final Kr KRIPTO = Kr.getInstance();
    private static final Digest SHA256 = KRIPTO.sha256;

    private final ScaleImage scale;
    private final float colorThreshold;
    private final float countThresold;
    private final boolean gray;
 
    public ImageHashBuilder(boolean gray, int size, float colorThreshold, float countThreshold)
    {
        ScaleDimension sd = (size == 0) ? null : new SimpleScaleDimension(size,size);
        this.scale = new ScaleImage(sd, gray);
        this.colorThreshold = colorThreshold;
        this.countThresold = countThreshold;
        this.gray = gray;
    }
    
    public Hash buildHash(VirtualFile pf) throws FileNotFoundException, IOException, ArchiveException
    {
        if(pf.length()==0)
        {
            return null;
        }
        InputStream in = pool.get(pf);
        try
        {
            BufferedImage image = ImageIO.read(in);
            if(image!=null)
            {
                return buildHash(image);
            }
            return null;
        }
        catch(Exception ex)
        {
            Logger.getLogger(ImageHashBuilder.class.getName()).log(Level.WARNING, pf.toString(), ex);
            return null;
        }
        finally
        {
            in.close();
        }
    }
    public Hash buildHash(BufferedImage image)
    {
        count.incrementAndGet();
        image = scale.filter(image);

        int w = image.getWidth();
        int h = image.getHeight();

//        JFrame frame = new JFrame();
//        frame.setTitle("["+w+"x"+h+"]"+" ");
//        frame.add(new JLabel(new ImageIcon(image)));
//        frame.setVisible(true);
//        frame.pack();

        int[] pixels = null;
        pixels = image.getRGB(0, 0, w, h, pixels, 0, w);
        if(colorThreshold>0 && gray)
        {
            byte[] hash = new byte[pixels.length];
            for(int i=0;i<hash.length;i++)
            {
                hash[i] = (byte) (pixels[i] & 0xff);
            }
            int hc = (w*h) + (w-h);
//            System.out.println(Arrays.toString(hash));
            return new StickyImageHash(w, h, hc, hash, colorThreshold, countThresold);
        }
        if(colorThreshold > 0)
        {
            byte[]hash = Byter.bytes(pixels);
            int hc = (w*h) + (w-h);
            System.out.println(Arrays.toString(hash));
            return new StickyImageHash(w, h, hc, hash, colorThreshold, countThresold);
        }
        byte[]hash = buildDigest(pixels);
        int hc = (w*h) + (w-h) + Arrays.hashCode(hash);
        System.out.println(Arrays.toString(hash));
        return new ImageHash(w, h, hc, hash);
    }
    protected static byte[] buildDigest(int[] pixels)
    {
        return SHA256.digest(Byter.bytes(pixels));
    }
}
