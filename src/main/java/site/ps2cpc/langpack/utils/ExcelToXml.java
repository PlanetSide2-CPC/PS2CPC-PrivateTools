package site.ps2cpc.langpack.utils;


import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import site.ps2cpc.langpack.dto.Ps2cnTextTranslate;
import site.ps2cpc.langpack.exception.InvalidKeyException;

import java.io.*;
import java.math.BigDecimal;

public class ExcelToXml {


    public static void main(String[] args) throws IOException {

        generate();

    }

    public static void generate() throws IOException{

        String excelFilePath = getExcelFilePath();
        generate(excelFilePath);
    }

    public static void generate(String excelFilePath) throws IOException {

        Workbook workbook = openExcel(excelFilePath);

        Document targetChineseXML = parseExcelAndGenerateXml(workbook);


        File excelFile = new File(excelFilePath);

        saveXML(targetChineseXML,excelFile+".xml");
    }

    private static Document parseExcelAndGenerateXml(Workbook workbook) {
        //默认第一个表为数据源
        Sheet sourceSheet = workbook.getSheetAt(0);
        int totalRowCount = sourceSheet.getPhysicalNumberOfRows();
        int currentRowNum = 0;


        Document targetChineseXML = DocumentHelper.createDocument();
        targetChineseXML.setXMLEncoding("UTF-8");
        Element chineseRootElement = targetChineseXML.addElement("root");


        StringBuilder stringBuilder = new StringBuilder();
        Ps2cnTextTranslate translateItem = null;

        System.out.println("开始解析");
        for (Row row : sourceSheet) {

            currentRowNum++;
            if (currentRowNum%1000==0){

                System.out.println(String.format("正在处理中…… (%s/%s)",currentRowNum,totalRowCount));
            }
            String key = null;
            try {
                //尝试用字符串的方式取值
                key = row.getCell(1).getStringCellValue();

                //如果能取到，那这一行就是有问题的，要么是标表头，要么是上一个key的附加行
                throw new InvalidKeyException("不存在的key，应特殊处理该行");

            } catch (IllegalStateException e) {
                //无法用字符串解析，证明这里保存的是数字。
                //如果是数字，那肯定是一个key,证明该行需要解析，所以在这里什么都不做
                key = Double.toString(row.getCell(1).getNumericCellValue());
                if (!key.contains("E")) {
                    key = key.substring(0, key.indexOf("."));
                } else {
                    BigDecimal num = new BigDecimal(key);
                    key = num.toPlainString();
                }
            } catch (Exception e) {
                //该行没有保存key，检查其上一行是否有信息在。
                if (null == translateItem) {
                    //上一行信息不存在，那证明这是表头，直接跳过解析下一行
                    System.out.println(String.format("%s: \"%s\" - 跳过解析", currentRowNum, key));
                    continue;
                } else {
                    //这一行是上一个key文本的扩充
                    stringBuilder.append("\r\n");
                    stringBuilder.append(row.getCell(6).getStringCellValue());
                    continue;
                }
            }

            //该行保存了key，所以在解析之前应先将上一个key但数据写入xml
            if (null != translateItem) {
                chineseRootElement.addElement("str")
                        .addAttribute("key", translateItem.getReleaseTextKey())
                        .addAttribute("type", translateItem.getType())
                        .addText(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }

            //开始解析当前行的数据

            translateItem = new Ps2cnTextTranslate();
            translateItem.setReleaseTextKey(key);
            translateItem.setType(row.getCell(2).getStringCellValue());
            try {

                translateItem.setBetaChineseReleaseText(row.getCell(6).getStringCellValue());
            } catch (IllegalStateException e) {
                translateItem.setBetaChineseReleaseText("");
            }

            stringBuilder.append(translateItem.getBetaChineseReleaseText());


        }
        System.out.println("=================");
        System.out.println("=    处理完成    =");
        System.out.println("=================");
        return targetChineseXML;
    }

    private static String getExcelFilePath() throws IOException {
        String fileSrc = "";
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        try {
            while (true) {
                System.out.print("请输入xml文件所在的位置:");
                fileSrc = br.readLine();

                if (!StringUtils.isEmpty(fileSrc)){
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            isr.close();
            br.close();
        }
        return fileSrc;
    }

    private static void saveXML(Document targetChineseXML,String fileSavePath) throws IOException {
        File xmlSavePath = new File(fileSavePath);
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
        System.out.println("文件已保存到 "+ fileSavePath);
    }

    private static Workbook openExcel(String fileSrc) throws FileNotFoundException {

        File target = new File(fileSrc);
        System.out.println("正在打开文件，请稍后……");
        FileInputStream fis = new FileInputStream(target);
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }


}
