package site.ps2cpc.langpack.utils;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.*;


//  当有人提供了weblate可以接受的新宇宙观资料时
//  这个工具可以根据weblate项目中已经分割好的英文原味xml的key分布，创建对应的同名文件
//  方便直接扔到git中，使weblate利用自动发现机制自动添加新语种。

public class ChineseTranslationSplitXmlGenerator {

    public static void main(String[] args) throws IOException {
        List<String> labelList = new ArrayList<>();
        labelList.add("请输入中文xml文件的路径：");
        labelList.add("请输入英文分段文件夹的路径：");

        List<String> inputResultList = null;
        try {
            inputResultList = ConsoleUtil.getInput(labelList);
        } catch (IOException e) {
            e.printStackTrace();
        }


        String chineseXmlFilePath = inputResultList.get(0);

        //把ps2cn的翻译xml解析为哈希表，以id为key，element对象为value，便于保存所有信息
        HashMap<String, Element> chineseTranslationMap = new HashMap<>();

        try {
            Document ps2cnChineseSrc = new SAXReader().read(new File(chineseXmlFilePath));
            Element ps2cnChineseSrcRoot = ps2cnChineseSrc.getRootElement();

            ps2cnChineseSrcRoot.elements().stream().forEach(element -> {
                chineseTranslationMap.put(element.attributeValue("key"), element);
            });
        } catch (DocumentException e) {
            e.printStackTrace();
        }


        //遍历英文分段文件目录并筛选出xml文件
        String targetEnglishFileParentPath = inputResultList.get(1);
        File targetEnglishFileDir = new File(targetEnglishFileParentPath);
        File[] engSplitFiles = targetEnglishFileDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory())
                    return false;
                return StringUtils.equals("xml", pathname.getName().substring(pathname.getName().lastIndexOf(".")+1).toLowerCase());
            }
        });

        //针对每一个xml分段，进行筛选和匹配
        for (File file : engSplitFiles) {
            try {
                generateChineseXmlFile(chineseTranslationMap, file);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }

    }

    private static void generateChineseXmlFile(HashMap<String, Element> chineseTranslationMap, File srcFile) throws IOException {

        Document targetChineseXML = DocumentHelper.createDocument();
        targetChineseXML.setXMLEncoding("UTF-8");
        Element chineseRootElement = targetChineseXML.addElement("root");

        try {
            Document targetEnglishSrc = new SAXReader().read(srcFile);
            Element targetEnglishSrcRoot = targetEnglishSrc.getRootElement();


            for (Element element : targetEnglishSrcRoot.elements()) {

                String tempkey = element.attributeValue("key");
                Element translateElement = chineseTranslationMap.get(tempkey);
                if (null == translateElement) {
                    continue;
                }

                chineseRootElement.addElement("str")
                        .addAttribute("key", tempkey)
                        .addAttribute("type", element.attributeValue("type"))
                        .addText(translateElement.getText());
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }


        String fileSaveParentPath = srcFile.getParent() + File.separator + "zh-hans";
        File fileSaveDir = new File(fileSaveParentPath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdirs();
        }
        String filename = srcFile.getName();

        XmlUtils.saveXML(targetChineseXML, fileSaveDir + File.separator + filename);
    }


}
