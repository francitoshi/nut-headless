/*
 * Copyright (C) 2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.headless.image.hash;

import io.github.francitoshi.nut.headless.image.hash.ImageClusterer;
import java.io.File;
import org.junit.jupiter.api.Test;

/**
 *
 * @author franci
 */
public class ImageClustererTest
{
    
    static final String PATH = "src/test/resources/io/nut/headless/image/hasher/"; 

    /**
     * Test of process method, of class ImageHasher.
     */
    @Test
    public void testProcess() throws Exception
    {
        ImageClusterer clusterer = new ImageClusterer(32, 64, 128, 0.1);
        
        for(int i=0;i<10;i++)
        {
            File jpg = new File(PATH,"a"+i+".jpg");
        }
        
        for(int i=0;i<10;i++)
        {
            File png = new File(PATH,"a"+i+".png");
        }
        for(int i=0;i<10;i++)
        {
            File png = new File(PATH,"b"+i+".png");

            File jpg = new File(PATH,"b"+i+".jpg");

        }
        
        String[][] paths = clusterer.getClusteredPaths();
        for(String[] c : paths)
        {
            for(String p : c)
            {
                System.out.println(p);
            }
            System.out.println();
        }
    }
}
