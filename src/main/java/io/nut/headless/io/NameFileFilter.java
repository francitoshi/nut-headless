/*
 * Copyright (C) 2009-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.nut.headless.io;

import io.nut.base.util.regex.RegExs;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 *
 * @author franci
 */
public class NameFileFilter implements FileFilter
{
    final String filter;
    final boolean ignoreCase;
    final boolean usePath;
    final Rule[] rules;
    
    static public class Rule 
    {
        final String fileName;
        final boolean dir;
        final boolean file;
        final boolean required;

        Rule(String name, boolean dir, boolean file, boolean required)
        {
            this.fileName = name;
            this.dir = dir;
            this.file = file;
            this.required = required;
        }
        boolean verify(File file)
        {
            File fd = new File(file.getParentFile(),fileName);
            return fd.exists();
        }
        static boolean verify(File file, Rule[] rules)
        {
            if(rules==null || rules.length==0)
            {
                return true;
            }
            int count=0;
            for(Rule r : rules)
            {
                boolean ok = r.verify(file);
                if(!ok && r.required)
                    return false;
                count++;
            }
            return true;
        }
    }

    public static NameFileFilter getStringInstance(String filter)
    {
        return new NameFileFilter(filter,false,false);
    }
    public static NameFileFilter getStringInstance(String filter,Rule[] rules)
    {
        return new NameFileFilter(filter,false,false,rules);
    }
    public static NameFileFilter getWildCardInstance(String filter)
    {
        return getRegExInstance(RegExs.wildcardToRegex(filter));
    }
    public static NameFileFilter getWildCardInstance(String filter,Rule[] rules)
    {
        return getRegExInstance(RegExs.wildcardToRegex(filter),rules);
    }
    public static NameFileFilter getRegExInstance(String filter)
    {
        return getRegExInstance(filter,null);
    }
    public static NameFileFilter getRegExInstance(String filter,Rule[] rules)
    {
        class NameFileFilterRegEx extends NameFileFilter
        {
            final Pattern pattern;
            public NameFileFilterRegEx(String filter, boolean ignoreCase, boolean usePath, Rule[] rules)
            {
                super(filter, ignoreCase, usePath, rules);
                this.pattern = Pattern.compile(filter);
            }           
            @Override
            public boolean accept(File file)
            {
                String name = usePath? file.getAbsolutePath():file.getName();
                return pattern.matcher(name).matches();
            }
        }
        return new NameFileFilterRegEx(filter,false,false,rules);
    }

    protected NameFileFilter(String filter, boolean ignoreCase,boolean usePath, Rule[] rules)
    {
        this.filter = filter;
        this.ignoreCase = ignoreCase;
        this.usePath = usePath;
        this.rules=rules;
    }
    protected NameFileFilter(String filter, boolean ignoreCase,boolean usePath)
    {
        this(filter, ignoreCase, usePath, null);
    }
  
    public boolean accept(File file)
    {
        String name = usePath? file.getAbsolutePath():file.getName();
        boolean ret = ignoreCase?filter.equalsIgnoreCase(name):filter.equals(name);
        return (ret && Rule.verify(file, rules));
    }
    static public Rule getDir(String name, boolean all)
    {
        return new Rule(name, true, false, all);
    }
    static public Rule getFile(String name, boolean all)
    {
        return new Rule(name, false, true, all);
    }
    @Override
    public String toString()
    {
        return "NameFileFilter{" + "filter=" + filter + ", ignoreCase=" + ignoreCase + ", usePath=" + usePath + ", rules=" + Arrays.deepToString(rules) + '}';
    }
    
}
