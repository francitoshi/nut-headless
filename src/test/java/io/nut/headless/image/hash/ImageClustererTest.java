/*
 *  ImageClustererTest.java
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
package io.nut.headless.image.hash;

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
