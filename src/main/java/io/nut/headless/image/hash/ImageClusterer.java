/*
 * Copyright (C) 2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.image.hash;

import io.nut.base.cache.Cache;
import io.nut.base.cache.TinyLFUCache;
import io.nut.base.crypto.Kripto;
import io.nut.base.function.CheckedSupplier2;
import io.nut.base.keyarray.KeyBytes;
import io.nut.headless.io.virtual.VirtualFile;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.compress.archivers.ArchiveException;

/**
 *
 * @author franci
 */
public class ImageClusterer
{
    static final Kripto KRIPTO = Kripto.getInstance();
    
    public static class Image
    {
        public final String path;
        public final int w;
        public final int h;
        public final double ratio;
        final byte[] fileHash;
        final byte[] imageHash;
        public final long mask;
        volatile Cluster cluster;

        public Image(String path, int w, int h, byte[] fileHash, byte[] imageHash, long mask)
        {
            this.path = path;
            this.w = w;
            this.h = h;
            this.ratio = w / (double)h;
            this.fileHash = fileHash;
            this.imageHash = imageHash;
            this.mask = mask;
        }

        public Cluster getCluster()
        {
            return cluster;
        }

        public void setCluster(Cluster cluster)
        {
            this.cluster = cluster;
        }

    }
    
    public static class Cluster
    {
        static final AtomicInteger COUNTER = new AtomicInteger();
        public final int id = COUNTER.getAndIncrement();
        final long[] masks;
        public final double[] ratios;
        final List<Image> items = new ArrayList<>();

        public Cluster(Image seed)
        {
            items.add(seed);
            masks = new long[]{seed.mask,seed.mask};
            ratios = new double[] {seed.ratio, seed.ratio};
        }

        public void add(Image image)
        {
            items.add(image);
            masks[0] = Math.min(masks[0], image.mask);
            masks[1] = Math.max(masks[1], image.mask);
            
            ratios[0] = Math.min(ratios[0], image.ratio);
            ratios[1] = Math.max(ratios[1], image.ratio);
        }
    }
    
    private final int size;
    private final int allowedDiff;
    private final int allowedFails;
    private final double ratioDelta;
    private final int maskDelta;
    
    private final Map<String,Image> images = new HashMap<>();
    private final List<Cluster> clusters = new ArrayList<>();
    
    private final Cache<KeyBytes,byte[]> cache = new TinyLFUCache(100_000, Long.MAX_VALUE, true).synchronizedCache();
        
    public ImageClusterer(int size, int maxPixelDiff, int maxFailures, double ratioDelta)
    {
        this.size = size;
        this.allowedDiff = maxPixelDiff;
        this.allowedFails = maxFailures;
        this.ratioDelta = ratioDelta;
        this.maskDelta = size*size*allowedDiff + (255-allowedDiff) * allowedFails;
    }

    public BufferedImage load(InputStream in) throws IOException 
    {
        BufferedImage img = ImageIO.read(in);
        if (img == null) 
        {
            throw new IllegalArgumentException("Unsupported or unreadable image format");
        }
        return img;
    }

    public BufferedImage resize(BufferedImage src) throws IOException 
    {
        return Thumbnails.of(src)
                .size(size, size)
                .keepAspectRatio(false)
                .asBufferedImage();
    }
    
    public byte[] toGrayscaleBytes(BufferedImage src) 
    {
        byte[] pixels = new byte[size * size];
        int index = 0;

        for (int row = 0; row < size; row++) 
        {
            for (int col = 0; col < size; col++) 
            {
                pixels[index++] = (byte) toGray(src.getRGB(col, row));
            }
        }
        return pixels;
    }

    private static int toGray(int rgb) 
    {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >>  8) & 0xFF;
        int b =  rgb        & 0xFF;
        return (299 * r + 587 * g + 114 * b) / 1000;
    }
    
    long mask(byte[] pixels)
    {
        int accum = 0;
        for(int i=0;i<pixels.length;i++)
        {
            accum += Byte.toUnsignedInt(pixels[i]);
        }
        return accum;
    }

    public int pixelDifferences(Image a, Image b)
    {
        if(Arrays.equals(a.fileHash, b.fileHash))
        {
            return 0;
        }
        int count = 0;
        for(int i=0;i<a.imageHash.length;i++)
        {
            int pa = Byte.toUnsignedInt(a.imageHash[i]);
            int pb = Byte.toUnsignedInt(b.imageHash[i]);
            int diff = Math.abs(pa-pb);
            if(diff>allowedDiff)
            {
                count++;
            }
        }
        return count;
    }
    
    public boolean similar(Cluster cluster, Image image)
    {
        if(image.mask < cluster.masks[0]-maskDelta)
        {        
            return false;
        }
        if(image.mask > cluster.masks[1]+maskDelta)
        {
            return false;
        }
        if(image.ratio < cluster.ratios[0]-ratioDelta)
        {        
            return false;
        }
        if(image.ratio > cluster.ratios[1]+ratioDelta)
        {
            return false;
        }
        int count = Integer.MAX_VALUE;
        for(Image item : cluster.items)
        {
            count = Math.min(count, pixelDifferences(item, image));
            if(count<=allowedFails)
            {
                return true;
            }
        }
        return false;
    }     
    
    public Image process(File file) throws IOException 
    {
        return process(file.getAbsolutePath(), new FileInputStream(file));
    }
    
    Image process(String path, InputStream in) throws IOException 
    {
        MessageDigest digest = KRIPTO.sha256.get();
        DigestInputStream dis  = new DigestInputStream(in, digest);
        final BufferedImage original = load(dis);
        byte[] sha256 = digest.digest();
        int w = original.getWidth();
        int h = original.getHeight();
        KeyBytes key = new KeyBytes(sha256);
        byte[] pixels = cache.get(key);
        if(pixels==null)
        {
            BufferedImage resized  = resize(original);
            pixels = toGrayscaleBytes(resized);
        }
        else
        {
            System.err.println(path);
        }
        
        return new Image(path, w, h, sha256, pixels, mask(pixels));
    }
    
    public Cluster add(File file) throws IOException
    {
        try
        {
            return add(file.getAbsolutePath(), () -> new FileInputStream(file));
        }
        catch (ArchiveException ex)
        {
            Logger.getLogger(ImageClusterer.class.getName()).log(Level.SEVERE, (String) null, ex);
            throw new RuntimeException("should never happend");
        }
    }
    
    public Cluster add(VirtualFile vf) throws IOException, ArchiveException
    {
        return add(vf.getAbsolutePath(), () -> vf.getInputStream());
    }
    
    private Cluster add(String path, CheckedSupplier2<InputStream, IOException, ArchiveException> in) throws IOException, ArchiveException
    {
        Image image = images.getOrDefault(path, null);
        if(image!=null)
        {
            return image.cluster;
        }
        
        image = this.process(path, in.get());
        
        images.put(path, image);
        
        for(Cluster cluster : this.clusters)
        {
            if(similar(cluster, image))
            {
                cluster.add(image);
                image.setCluster(cluster);
                return cluster;
            }
        }
        Cluster cluster = new Cluster(image);
        image.setCluster(cluster);
        this.clusters.add(cluster);
        return cluster;
    }
    
    String[][] getClusteredPaths()
    {
        ArrayList<String[]> list =new ArrayList<>();
        for( Cluster c : clusters)
        {
            ArrayList<String> paths = new ArrayList<>();
            for(Image i : c.items)
            {
                paths.add(i.path);
            }
            list.add(paths.toArray(new String[0]));
        }
        return list.toArray(new String[0][]);
    }
    public Cluster getCluster(File file)
    {
        return getCluster(file.getAbsolutePath());
    }
    public Cluster getCluster(VirtualFile file)
    {
        try
        {
            return getCluster(file.getAbsolutePath());
        }
        catch (IOException ex)
        {
            Logger.getLogger(ImageClusterer.class.getName()).log(Level.SEVERE, (String) null, ex);
            return null;
        }
    }
    private Cluster getCluster(String path)
    {
        Image image = this.images.getOrDefault(path, null);
        return image!=null ? image.getCluster() : null;
    }
}
