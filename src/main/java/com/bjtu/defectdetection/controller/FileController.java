package com.bjtu.defectdetection.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

@Controller
public class FileController {
    private static long count = 0;

    @RequestMapping("/dfile")
    public String getFileName(@RequestParam("file") String file, Model model) {
        ArrayList<String> mapList = new ArrayList<>(); // 将经纬度分组、
        Integer defectType = 0; // 路况类型，1-平稳-绿色；2-颠簸-蓝色，3-非常颠簸-红色
        Float[] acc = new Float[3]; // 用来获取三轴加速度
        BufferedReader br = null; // 字符输出流
        String str = "";
        long count_1 = 0, count_2 = 0;

        System.out.println(file.toString()); //文件路径及文件名
        try {
            br = new BufferedReader(new FileReader(new File(file)));
            String contentLine = br.readLine(); //逐行读取文件中数据
            while (contentLine != null) {

                // 根据获取的加速度值，判断当前路段颠簸情况
                if (contentLine.startsWith("加速度：")) {//获取加速度数据
                    String[] split = contentLine.split("：")[1].split(",");
                    for (int i = 0; i < 3; i++) {
                        acc[i] = Float.valueOf(String.valueOf(split[i].split("=")[1]));
                    }
                    // 利用加速度实现路况检测,阈值法
                    if ((acc[1] >= 10 || acc[1] <= 8)) {
                        count_1++;
                    } else if ((acc[1] >= 12 || acc[1] <= 6)) {
                        count_2++;
                    }
                }

                // 获取所有地理位置经纬度，并按照一定时间间隔分组
                if (contentLine.startsWith("经纬度：")) {//获取地理位置数据
                    count += 1;
                    String s = contentLine.split("：")[1] + ";";
                    str += s;
                    if (count % 5 == 0) {
                        if (count_2 >= 4) {
                            defectType = 2;
                        } else if (count_1 >= 3) {
                            defectType = 1;
                        }else{
                            defectType = 0;
                        }
                        System.out.println(defectType);
                        str += defectType + "-";
                        count = 0;
                        count_1 = 0;
                        count_2 = 0;
                    }
                }
                contentLine = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 将数据放入内置对象中，转发到页面，待处理
        model.addAttribute("str", str);
        model.addAttribute("path", file);
        return "index";
    }
}
