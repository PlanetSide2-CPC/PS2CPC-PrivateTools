package site.ps2cpc.langpack.utils;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import site.ps2cpc.langpack.dto.TranslateLineInfo;

import java.io.*;
import java.util.*;


// dat 文件转换器
// 使用英文原文dat文件和weblate导出的对应语种翻译语料目录，生成对应语种的dat文件，供游戏使用
public class DatFileGenerator {

    public static void main(String[] args) throws IOException {
        List<String> labelList = new ArrayList<>();
        labelList.add("请输入英文dat文件的路径：");
        labelList.add("请输入目标语言分段文件夹的路径：");

        List<String> inputResultList = null;
        try {
            inputResultList = ConsoleUtil.getInput(labelList);
        } catch (IOException e) {
            e.printStackTrace();
        }





        //英语dat文件路径
        String englishDatFileSrc = inputResultList.get(0);

        //目标语言weblate导出语料的目录位置
        String chineseTranslationDir = inputResultList.get(1);


        //读取英文dat文件


        List<TranslateLineInfo> englishLineInfoList =PlanetSide2DatFileInfoParser.parse(englishDatFileSrc);

        //导入目标语种翻译的信息，便于检索
        Map<String, TranslateLineInfo> chineseTranslateLineInfoMap = new HashMap<>();



        //筛选目录中的xml文件进行解析和导入
        File chineseXmlDirFile = new File(chineseTranslationDir);
        File[] chineseXmlFileArray = chineseXmlDirFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory())
                    return false;
                return StringUtils.equals("xml", pathname.getName().substring(pathname.getName().lastIndexOf(".") + 1).toLowerCase());
            }
        });



        Arrays.stream(chineseXmlFileArray).forEach(chineseXmlItem -> {

            try {
                Document tempXmlDocument = new SAXReader().read(chineseXmlItem);
                Element tempXmlRootElement = tempXmlDocument.getRootElement();


                //  由于XML文件中 > < & 这三个符号有特殊含义
                //  所以在dat转xml的时候这些字符被转义了，现在进行反转义恢复回来
                tempXmlRootElement.elements().forEach(element -> {

                    TranslateLineInfo tempLineInfo = new TranslateLineInfo();
                    tempLineInfo.setKey(element.attributeValue("key"));
                    tempLineInfo.setType(element.attributeValue("type"));


                    //replace invalid "<", ">" and "&" in xml
                    String tempLineInfoText = element.getText()
                            .replaceAll("&lt;", "<")
                            .replaceAll("&gt;", ">")
                            .replaceAll("&amp;", "&");

                    tempLineInfo.setContent(tempLineInfoText);
                    chineseTranslateLineInfoMap.put(tempLineInfo.getKey(), tempLineInfo);
                });
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        });


        //m遍历匹配和生成dat文件


        File chineseDatFile = new File(new File(englishDatFileSrc).getParent() + "zh_cn-data.dat");
        FileWriter chineseFW = null;
        try {
            chineseFW = new FileWriter(chineseDatFile);


            //  这是一饿很奇怪的bug
            //  游戏要求语言文件必须是UTF-8-SIG编码，它比UTF-8文件多一个三字节的BOM头
            //  文件头的内容是 \uFEFF，所以这里手动添加这个BOM头。

            //  也尝试过在dir和dat都不加文件头，且dir文件不增加这个三字节偏移的方式
            //  但游戏会直接崩溃，并不会正常即诶下
            //  所以姑且认为游戏本身强制要求了UTF-8-SIG
            chineseFW.write("\uFEFF");

            Iterator<TranslateLineInfo> engIterator = englishLineInfoList.iterator();
            while (engIterator.hasNext()) {
                TranslateLineInfo engSrcTranslateLineInfo = engIterator.next();
                TranslateLineInfo matchedChineseTranslateLineInfo = chineseTranslateLineInfoMap.get(engSrcTranslateLineInfo.getKey());


                if (null == matchedChineseTranslateLineInfo) {
                    //no chinese translate here.
                    chineseFW.write(String.format("%s\t%s\t%s\r\n", engSrcTranslateLineInfo.getKey(), engSrcTranslateLineInfo.getType(), engSrcTranslateLineInfo.getContent()));
                } else {
                    chineseFW.write(String.format("%s\t%s\t%s\r\n", engSrcTranslateLineInfo.getKey(), engSrcTranslateLineInfo.getType(), matchedChineseTranslateLineInfo.getContent()));

                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            chineseFW.close();
        }


    }


}
