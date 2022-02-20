package site.ps2cpc.langpack.utils;

import site.ps2cpc.langpack.dto.TranslateLineInfo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


//  dat文件解析器
//  工具类，将dat文件转换成对象列表，方便维护和解析。
public class PlanetSide2DatFileInfoParser {

    public static List<TranslateLineInfo> parse(String src) {
        FileReader englishFIS = null;
        List<TranslateLineInfo> englishLineInfoList = new ArrayList<>();


        TranslateLineInfo lastTranslateLineInfo = null;
        try {
            englishFIS = new FileReader(src);
            BufferedReader englishBR = new BufferedReader(englishFIS);

            String line = englishBR.readLine();
            boolean isFirst = true;
            while (line != null) {

                String[] result = line.split("\t");

                String newLine = line;


                //这里是在判断这个文件是不是带3个字节的BOm头的，如果带，第一个字符串因为含有\uFEFF或\uFFFE，会无法解析成数字
                if (isFirst) {
                    try {
                        Long.parseLong(result[0]);
                    } catch (NumberFormatException e) {
                        byte[] bytes = result[0].getBytes("UTF-8");
                        //  移除bom头，从而保证第一个key正常
                        String str = new String(bytes, 3, bytes.length - 3);
                        result[0] = str;

                        bytes = line.getBytes(StandardCharsets.UTF_8);

                        newLine = new String(bytes, 3, bytes.length - 3);


                    }
                    isFirst = false;
                }


                //这里是针对dat文件里各种奇奇怪怪行为所做的不同解析
                TranslateLineInfo newEngTranslateItem = null;
                if (result.length == 3) {

                    //  最正常普遍的就是这种形式，格式如下
                    //  123456  ugdt    行星边际
                    newEngTranslateItem = new TranslateLineInfo(result[0], result[1], result[2]);
                    newEngTranslateItem.setOriginalLine(newLine);
                    englishLineInfoList.add(newEngTranslateItem);
                } else if (result.length == 2) {


                    //  一种不太常见的例外情况，格式一样，但是这个key但内容为空
                    //  123456  ugdt

                    newEngTranslateItem = new TranslateLineInfo(result[0], result[1]);
                    newEngTranslateItem.setOriginalLine(newLine);
                    englishLineInfoList.add(newEngTranslateItem);
                } else if (result.length == 1) {

                    //  修复英语原始dat文件里存在的奇怪行。
                    //  这是很蛋疼但一种情况，它但内容是多行文本
                    //  但是游戏本身在存储这个文本的时候，没有考虑把换行符进行转义
                    //  而是直接输出了，结果就导致了这种特别蛋疼但情况


//                    325524308	ucdt	随着堡垒统治着战场，[*faction*]的士气提升了。
//                    325567686	ucdt	头戴万圣节面具并使用屠刀击杀敌人。
//
//                    完成本成就奖励头衔“屠夫”
//                    325656111	ugdt	Eternal Loyalty


                    //这种情况下，就只能读一行发现拆不开，就往上一行但信息里拼文本

                    if (null != lastTranslateLineInfo) {
                        String newText = new StringBuilder(lastTranslateLineInfo.getContent())
                                .append("\n")
                                .append(result[0])
                                .toString();
                        lastTranslateLineInfo.setContent(newText);

                        String originLineInfo = new StringBuilder()
                                .append(lastTranslateLineInfo.getOriginalLine())
                                .append("\n")
                                .append(newLine)
                                .toString();

                        lastTranslateLineInfo.setOriginalLine(originLineInfo);
                    }

                } else if (result.length > 3) {
                    //  奇怪的问题，有些文本内容里都有制表符
                    //  内容里但制表符导致文本被分割成了非常多段，又要保留这个符号，所以自己拼回来
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 2; i < result.length; i++) {
                        stringBuilder.append(result[i]).append("\t");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);


                    newEngTranslateItem = new TranslateLineInfo(result[0], result[1], stringBuilder.toString());
                    newEngTranslateItem.setOriginalLine(newLine);
                    englishLineInfoList.add(newEngTranslateItem);
                } else {

                    //如果真到了这里，那就是db又在文本里干逆天但事儿了
                   throw new RuntimeException("What the fuck???????");
                }


                lastTranslateLineInfo = newEngTranslateItem == null ? lastTranslateLineInfo : newEngTranslateItem;
                line = englishBR.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("完成dat导入。");
        return englishLineInfoList;
    }

}
