package com.sweet.apple.sweetapple.test;

import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 描述
 *
 * @author yangqifan004
 * @date 2022/4/7 16:35
 */
public class WashData {

    public static void main(String[] args) throws Exception {
        //wenzhou();
        shanghai();
        //checkshanghai();
    }

    private static void checkshanghai() throws IOException {
        BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("D:\\yangqifan004\\Desktop\\shanghai.csv")), "UTF-8"));
        String str;
        int count = 0;
        //System.out.println("select * from construction_site where address in (");
        while ((str = bf.readLine()) != null) {
            if (count++ == 0) {
                continue;
            }

            if (!str.endsWith(",,")) {
                System.out.println(str);
            }

        }
        bf.close();
    }

    private static void wenzhou() throws IOException {
        BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("D:\\yangqifan004\\Desktop\\wenzhou.csv")), "UTF-8"));
        String str;
        int count = 0;
        //System.out.println("select * from construction_site where address in (");
        while ((str = bf.readLine()) != null) {
            if (count++ == 0) {
                continue;
            }
            String[] arr = str.split(",");
            String nccode = arr[0];
            String address = arr[3];
            System.out.println("update construction_site set nc_code = \"" + nccode + "\" where address = \"" + address + "\";");
            //System.out.println("\"" + address + "\",");
        }
        bf.close();
    }

    private static void shanghai() throws IOException {
        BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("D:\\yangqifan004\\Desktop\\shanghai.csv")), "UTF-8"));
        String str;
        int count = 0;
        //System.out.println("select * from construction_site where address in (");
        while ((str = bf.readLine()) != null) {
            if (count++ == 0) {
                continue;
            }
            String[] arr = str.split(",");
            if (arr.length < 5 || !StringUtils.hasLength(arr[0]) || !StringUtils.hasLength(arr[5])) {
                continue;
            }

            String nccode = arr[0];
            String address = arr[5];
            System.out.println("update construction_site set nc_code = \"" + nccode + "\" where address = \"" + address + "\";");
            //System.out.println("\"" + address + "\",");
        }
        bf.close();
    }
}
