package com.hive.codegen;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Command {
    public static void main(String[] args) {
        System.out.println(args[1]);
        GenEngine engine = new GenEngine();
        String result = null;
        try {
            result = engine.render(getTxtFromFile(args[1]), getTxtFromFile(args[2]), false);
        } catch (Exception e) {
            result = e.getMessage();
        }
        System.out.println(result);
    }

    private static String getTxtFromFile(String fileName) throws IOException {
        return FileUtils.readFileToString(new File(fileName));
    }

}