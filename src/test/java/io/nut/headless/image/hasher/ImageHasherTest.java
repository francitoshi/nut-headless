/*
 *  ImageHasherTest.java
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

import java.io.File;
import org.junit.jupiter.api.Test;

/**
 *
 * @author franci
 */
public class ImageHasherTest
{

    public ImageHasherTest()
    {
    }
    
    static final String[] FILES = 
    {
        "/home/franci/Downloads/robot-visions-224x300-2.png",
        "/home/franci/Downloads/robot-visions-224x300-2.png",
        "/home/franci/Downloads/robot-visions-224x300-1.png",
        "/home/franci/Downloads/robot-visions-224x300-1.jpg",
        "/home/franci/Downloads/robot-visions-224x300-0.jpg",
        "/home/franci/Downloads/robot-visions-224x300-0.png"
    };

    /**
     * Test of process method, of class ImageHasher.
     */
    @Test
    public void testProcess() throws Exception
    {
        ImageHasher hasher = new ImageHasher(16);
        
        for(String s : FILES)
        {
            byte[][] pixels = hasher.process(new File(s));
        }
        
    }

}
