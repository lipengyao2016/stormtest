package com.bigdata;


import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FileSystem;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        boolean bStr = StringUtils.isNotEmpty("tt");
        System.out.println( "Hello World! str:" + bStr );
        for (int i = 0; i< 10;i++)
        {
            int randNum = RandomUtils.nextInt(1,100);
            System.out.println( "Hello World! randNum:" + randNum );
        }

    }
}
