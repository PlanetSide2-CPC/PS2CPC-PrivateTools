package site.ps2cpc.langpack.utils;

import org.apache.commons.codec.digest.DigestUtils;
import site.ps2cpc.langpack.dto.TranslateLineInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



//  dir文件生成器
//  使用dat文件自动计算dir文件。
public class DirFileGenerator {

    public static void main(String[] args) throws IOException {

        List<String> labelList = new ArrayList<>();
        labelList.add("请输入英文dat文件的路径：");

        List<String> inputResultList = null;
        try {
            inputResultList = ConsoleUtil.getInput(labelList);
        } catch (IOException e) {
            e.printStackTrace();
        }



        //dat file path
        //.dat文件路径
        String datFilePath = inputResultList.get(0);

        //dat文件解析列表
        List<TranslateLineInfo> datList = PlanetSide2DatFileInfoParser.parse(datFilePath);

        //生成的dir文件存放位置
        //默认与dat文件同路径且同名，只是扩展名是dir

        File datFile = new File(datFilePath);
        String fileName = datFile.getName().substring(0, datFile.getName().lastIndexOf("."));
        File dirFile = new File(datFile.getParent() + File.separator + fileName + ".dir");

        if (dirFile.exists()) {
            dirFile.delete();
        }
        FileWriter fileWriter = new FileWriter(dirFile);

        //  计算dat文件的MD5
        //  并没有发现有什么实际作用，只是为了保持格式统一
        FileInputStream datFIS = new FileInputStream(datFile.getAbsolutePath());
        String datMD5 = DigestUtils.md5Hex(datFIS).toUpperCase();
        datFIS.close();

        StringBuilder headBuilder = new StringBuilder();
        headBuilder.append("## CidLength:\t63\r\n")
                .append(String.format("## Count:\t%s\r\n", datList.size()))
                .append("## Date:\tTue Jan 25 10:20:19 PST 2022\r\n")
                .append("## Game:\tPSNXCN\r\n")
                .append("## Locale:\tzh_CN\r\n")
                .append(String.format("## MD5Checksum:\t%s\r\n", datMD5))
                .append("## T4Version:\t2.1.0047\r\n")
                .append("## TextLength:\t1594\r\n")
                .append("## Version:\t2.1.1293249\r\n");

        fileWriter.write(headBuilder.toString());

        Iterator<TranslateLineInfo> translateLineInfoIterator = datList.iterator();

        //  这个偏移数字默认值是3，含义是UTF-8-SIG编码方式的要求中，文件的前三个字节是BOM头信息
        //  所以对我们需要的内容来说，是从第四个字节才开始的

        //  另外，很奇怪的一点是，本来尝试用UTF-8编码，dat和dir文件都不要这个文件头
        //  理论上讲，这样生成dir文件时，第一条记录的偏移为0就应该可以正常工作
        //  但实际测试之后游戏会在启动阶段崩溃
        //  只能将dir和dat文件设置为UTF-8-SIG编码，即同时保留3个字节但BOM头才能正常工作
        //  百思不得其解，唯一觉得合理但解释是，游戏的解析器限制了必须要有这个bom头。
        long latestPosition = 3l;

        while (translateLineInfoIterator.hasNext()) {
            TranslateLineInfo tempLineInfo = translateLineInfoIterator.next();

            String key = tempLineInfo.getKey();
            String startPosition = String.valueOf(latestPosition);

            //长度指的是dat文件里完整的一行内容（但不包含行末尾CRLF这两个字符）存储占用的字节数，而不是中文翻译部分的字节数。
            //不要用字符串的length，因为不同语言中，一个字符存储所要求的字节数是不一样的
            //例如UTF-8中，简体中文字符要求使用3个字节存储，阿拉伯数字和英文字母只使用一个字节存储。
            String lengthStr = String.valueOf(tempLineInfo.getOriginalLine().getBytes(StandardCharsets.UTF_8).length);

            //格式化写入

            String writeString = String.format("%s\t%s\t%s\td\r\n", key, startPosition, lengthStr, "d");
            fileWriter.write(writeString);

            //这里加2指的是回车换行符CRLF要占用的两个字节
            latestPosition += Long.parseLong(lengthStr) + 2l;


        }

        fileWriter.close();

    }
}
