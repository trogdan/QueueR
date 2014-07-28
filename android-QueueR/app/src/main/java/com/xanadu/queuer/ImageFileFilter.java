package com.xanadu.queuer;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by dan on 7/6/14.
 */
public class ImageFileFilter implements FileFilter
{
    private final String[] okFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};

    public boolean accept(File file)
    {
        if(file.isDirectory()) {
            return true;
        }

        for (String extension : okFileExtensions)
        {
            if (file.getName().toLowerCase().endsWith(extension))
            {
                return true;
            }
        }
        return false;
    }

}
