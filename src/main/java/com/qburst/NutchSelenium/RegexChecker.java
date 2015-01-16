package com.qburst.NutchSelenium;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by albin on 10/4/15.
 */
public class RegexChecker {
    public static void main(String[] args) {
        RegexChecker regexChecker = new RegexChecker();
        regexChecker.getFile();
    }
    private String matchPattern(String content){
        String reg = "href=\"(java.|com.|android|org|junit|net|hudson|redis|ch|sun|jline|processing|antlr|me|ij|gnu|play|edu|akka|cpw|dalvik).*\"";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(content);
        if(matcher.find()){
            String[] separator = content.split("\">");
            return separator[0].concat(".html").concat("\">").concat(separator[1]);
        }
        return content;
    }

    private void getFile() {
        Configuration conf = new Configuration();
        Path path = new Path("/home/albin/index");
        FileSystem fileSystem;
        FileWriter fileWriter;
        BufferedWriter bufferedWriter;
        File newFile = new File("/home/albin/","newFile");
        try {
            fileSystem = path.getFileSystem(conf);
            FSDataInputStream inputStream = fileSystem.open(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String currentLine = "";
            newFile.createNewFile();
            fileWriter = new FileWriter(newFile);
            bufferedWriter = new BufferedWriter(fileWriter);
            while ((currentLine = br.readLine()) != null) {
                bufferedWriter.write( matchPattern(currentLine));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
