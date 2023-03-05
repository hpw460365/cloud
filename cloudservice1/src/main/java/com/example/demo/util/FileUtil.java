package com.example.demo.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    //测试org.apache.commons.io.FileUtils的writeLines
    public static void main(String[] args) throws IOException {
        List<String> list = new ArrayList<String>();
        list.add("string one");
//        list.add("string two");
        FileUtils.writeLines(new File("E:\\Temp\\hepeiwen\\file.txt"), list, "\n", false);
    }
}
