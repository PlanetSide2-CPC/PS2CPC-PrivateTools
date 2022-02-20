package site.ps2cpc.langpack.utils;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.*;
//  XML工具类，方便xml的保存
public class XmlUtils {
    static void saveXML(Document targetChineseXML, String fileSavePath) throws IOException {
        File xmlSavePath = new File(fileSavePath).getParentFile();
        if (!xmlSavePath.exists()){
            xmlSavePath.mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileSavePath);
            OutputFormat xmlFormat = new OutputFormat();
            xmlFormat.setEncoding("UTF-8");
            // 设置换行
            xmlFormat.setNewlines(true);
            // 生成缩进
            xmlFormat.setIndent(true);
            // 使用4个空格进行缩进, 可以兼容文本编辑器
            xmlFormat.setIndent("    ");

            XMLWriter xmlWriter = new XMLWriter(fos, xmlFormat);
            xmlWriter.write(targetChineseXML);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fos.close();
        }
        System.out.println("文件已保存到 " + fileSavePath);
    }
}
