/*
 *  ImageClusterer.java
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

import io.nut.base.function.CheckedSupplier2;
import io.nut.headless.io.virtual.VirtualFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.compress.archivers.ArchiveException;

/**
 *
 * @author franci
 */
public class ImageClusterer
{
    static long[] mask(byte[] pixels, int pixelDiff, int failures)
    {
        int accum = 0;
        for(int i=0;i<pixels.length;i++)
        {
            accum += Byte.toUnsignedInt(pixels[i]);
        }
        //not very accurated but very fast
        int diff = pixels.length*pixelDiff + (255-pixelDiff) * failures;
        int min = accum - diff;
        int max = accum + diff;
        return new long[]{min,accum,max};
    }

    static class Image
    {
        final String path;
        final byte[] fileHash;
        final byte[] imageHash;
        final long[] masks;
        volatile Cluster cluster;

        public Image(String path, byte[] fileHash, byte[] imageHash, long[] masks)
        {
            this.path = path;
            this.fileHash = fileHash;
            this.imageHash = imageHash;
            this.masks = masks;
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
        final List<Image> items = new ArrayList<>();

        public Cluster(Image seed)
        {
            items.add(seed);
            masks = seed.masks.clone();
        }
        public void add(Image image)
        {
            items.add(image);
            masks[0] = Math.min(masks[0], image.masks[0]);
            masks[1] = Math.min(masks[1], image.masks[1]);
        }
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

    public boolean similar(Cluster cluster, Image image, int maxFailures)
    {
        if(image.masks[1]<cluster.masks[0])
        {        
            return false;
        }
        if(image.masks[1]>cluster.masks[2])
        {
            return false;
        }
        int count = Integer.MAX_VALUE;
        for(Image item : cluster.items)
        {
            count = Math.min(count, pixelDifferences(item, image));
            if(count<=maxFailures)
            {
                return true;
            }
        }
        return false;
    }     

    final int n;
    final int allowedDiff;
    final int allowedFails;
    final ImageHasher imageHasher;
    final Map<String,Image> images = new HashMap<>();
    final List<Cluster> clusters = new ArrayList<>();
    
    final HashMap<String,Cluster> map = new HashMap<>();
    
    public ImageClusterer(int n, int maxPixelDiff, int maxFailures)
    {
        this.n = n;
        this.allowedDiff = maxPixelDiff;
        this.allowedFails = maxFailures;
        this.imageHasher = new ImageHasher(n);
    }
    
    public boolean add(File file) throws IOException
    {
        try
        {
            return add(file.getAbsolutePath(), () -> new FileInputStream(file));
        }
        catch (ArchiveException ex)
        {
            System.getLogger(ImageClusterer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            throw new RuntimeException("should never happend");
        }
    }
    
    public boolean add(VirtualFile vf) throws IOException, ArchiveException
    {
        return add(vf.getAbsolutePath(), () -> vf.getInputStream());
    }
    
    private boolean add(String path, CheckedSupplier2<InputStream, IOException, ArchiveException> in) throws IOException, ArchiveException
    {
        if(images.getOrDefault(in, null)!=null)
        {
            return false;
        }
        byte[][] hashes = imageHasher.process(in.get());
        
        long[] masks = mask(hashes[1], allowedDiff, allowedFails);
        
        Image img = new Image(path, hashes[0], hashes[1], masks);
        
        images.put(path, img);
        
        for(Cluster cluster : this.clusters)
        {
            if(similar(cluster, img, allowedFails))
            {
                cluster.add(img);
                img.setCluster(cluster);
                return false;
            }
        }
        Cluster cluster = new Cluster(img);
        img.setCluster(cluster);
        this.clusters.add(cluster);
        return true;
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
            System.getLogger(ImageClusterer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return null;
        }
    }
    private Cluster getCluster(String path)
    {
        Image image = this.images.getOrDefault(path, null);
        return image!=null ? image.getCluster() : null;
    }
}
