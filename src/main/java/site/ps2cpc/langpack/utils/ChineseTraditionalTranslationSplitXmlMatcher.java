package site.ps2cpc.langpack.utils;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocument;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


//  weblate导出但分割xml文本拼接工具

//  这是赶时间写的，原文用的是xml文本，其实可以直接用dat的，但当时我还没看过dat的文件格式，我懒得写了
public class ChineseTraditionalTranslationSplitXmlMatcher {

    public static void main(String[] args) throws IOException {

        List<String> labelList = new ArrayList<>();
        labelList.add("请输入原文的单xml文件的路径：");
        labelList.add("请输入目标语种导出翻译的xml分段文件夹的路径：");
        labelList.add("输出结果保存文件夹名：");

        List<String> inputResultList = null;
        try {
            inputResultList = ConsoleUtil.getInput(labelList);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //读取英文原文完整的xml文件
        String chineseTraditionXmlSrc = inputResultList.get(0);

        //读取英文原文语料的xml分割文件
        String englishSplitXmlSrc = inputResultList.get(1);

        //输出结果保存的文件夹名
        String outputSaveDir = inputResultList.get(2);


        //把原文xml写入HashMap，便于提高查找效率
        HashMap<String, Element> chineseTraditonMap = new HashMap<>();

        SAXReader traditionXmlReader = new SAXReader();
        Document traditionalXmlDocument = null;
        try {
            traditionalXmlDocument = traditionXmlReader.read(chineseTraditionXmlSrc);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Element traditionXmlRootElement = traditionalXmlDocument.getRootElement();

        traditionXmlRootElement.elements().forEach(item -> {
            chineseTraditonMap.put(item.attributeValue("key"), item);
        });


        //根据英文原文的xml文件，查询创建对应的xml
        File englishSplitXmlDir = new File(englishSplitXmlSrc);
        File traditionSaveDir = new File(new File(chineseTraditionXmlSrc ).getParent()+ File.separator +outputSaveDir);
        if (!traditionSaveDir.exists()) {
            traditionSaveDir.mkdirs();
        }

        File[] engSplitFilesArray = englishSplitXmlDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory())
                    return false;
                return StringUtils.equals("xml", pathname.getName().substring(pathname.getName().lastIndexOf(".") + 1).toLowerCase());
            }
        });

        Arrays.stream(engSplitFilesArray).forEach(engFile -> {


            Document tempTraditionXmlDocument = DocumentHelper.createDocument();
            tempTraditionXmlDocument.setXMLEncoding("UTF-8");
            Element tempTraditionRoot = tempTraditionXmlDocument.addElement("root");


            try {
                SAXReader tempSaxReader = new SAXReader();
                Document tempEngXmlDocument = tempSaxReader.read(engFile);
                Element tempEngRoot = tempEngXmlDocument.getRootElement();

                for (Element engElement : tempEngRoot.elements()) {
                    Element matchedTraditionElement = chineseTraditonMap.get(engElement.attributeValue("key"));
                    if (null == matchedTraditionElement) {
                        System.out.println(String.format("%s : 找不到匹配的element", engElement.attribute("key")));
                        continue;
                    }

                    tempTraditionRoot.addElement("str")
                            .addAttribute("key", engElement.attributeValue("key"))
                            .addAttribute("type", engElement.attributeValue("type"))
                            .addText(matchedTraditionElement.getText());


                    //删除已经使用过的原文语料
                    chineseTraditonMap.remove(engElement.attributeValue("key"));
                }


                //保存为同名文件
                File tempTraditionSplitXmlFile = new File(traditionSaveDir.getAbsolutePath() + File.separator + engFile.getName());

                XmlUtils.saveXML(tempTraditionXmlDocument, tempTraditionSplitXmlFile.getAbsolutePath());
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }


        });



        System.out.println(String.format("剩余未转换数量：%s" , chineseTraditonMap.size()));

        if (chineseTraditonMap.size() > 0) {
            //这里是一些在原文中找不到对应key的语料，可能是已经被移除或者翻译版本里忘记益处的语料
            //他们会呗保存在patch目录下
            System.out.println("一部分语料匹配失败，保存在patch目录下。");

            File traditionImportPatchFileDir = new File(traditionSaveDir.getParent()+File.separator+"patch");
            Document engPatchDocument = DocumentHelper.createDocument();
            Element engPatchRoot = engPatchDocument.addElement("root");

            Document traditionPatchDocument = DocumentHelper.createDocument();
            Element traditionEngPatchRoot = traditionPatchDocument.addElement("root");

            for (Element item : chineseTraditonMap.values()){
                engPatchRoot.addElement("str")
                        .addAttribute("key", item.attributeValue("key"))
                        .addAttribute("type", item.attributeValue("type"));

                traditionEngPatchRoot.addElement("str")
                        .addAttribute("key", item.attributeValue("key"))
                        .addAttribute("type", item.attributeValue("type"))
                        .addText(item.getText());
            }

            //源字符串匹配不到的结果保存在srcLang
            File engPatchSaveFile = new File(traditionImportPatchFileDir+File.separator+"srcLang"+File.separator+"import-patch.xml");
            //目标语言字符串匹配不到的结果保存在destLang
            File traditionPatchSaveFile =new File(traditionImportPatchFileDir+File.separator+"destLang"+File.separator+"import-patch.xml");

            XmlUtils.saveXML(engPatchDocument,engPatchSaveFile.getAbsolutePath());
            XmlUtils.saveXML(traditionPatchDocument, traditionPatchSaveFile.getAbsolutePath());

        }

    }
}
