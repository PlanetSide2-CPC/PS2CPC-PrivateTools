package site.ps2cpc.langpack.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


//  命令行工具累，方便控制输入内容提示
public class ConsoleUtil {


    public static List<String> getInput(List<String> labelList) throws IOException {

        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        List<String> resultList = new ArrayList<>();

        try {

            for (String label : labelList) {

                String fileSrc = "";
                while (true) {
                    System.out.print(label);
                    fileSrc = br.readLine();

                    if (!StringUtils.isEmpty(fileSrc)) {
                        resultList.add(fileSrc);
                        break;
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isr.close();
            br.close();
        }

        return resultList;
    }

}
