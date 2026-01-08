package cn.keking;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.ParagraphProperties;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author LouMT
 * @name DocRewriteTest
 * @date 2026-01-06 16:51
 * @email lmtemail163@163.com
 * @description 查找并标记关键词
 */
public class DocRewriteTest {
    public static void main(String[] args) {
        runWithDoc();
//        runWithDocx();
    }

    private static void runWithDoc() {
        String filePath = "D:\\20260102.doc";
        String searchKey = "开发环境";

        try (HWPFDocument document = new HWPFDocument(new FileInputStream(filePath))) {
            Range documentRange = document.getRange();
            // 处理段落中的搜索关键词
            for (int paraIndex = 0; paraIndex < documentRange.numParagraphs(); paraIndex++) {
                org.apache.poi.hwpf.usermodel.Paragraph paragraph = documentRange.getParagraph(paraIndex);
                String paragraphText = paragraph.text();
                if (StringUtils.isNotEmpty(paragraphText) && paragraphText.contains(searchKey)) {
                    for(int runIndex = 0; runIndex < paragraph.numCharacterRuns(); runIndex ++) {
                        System.out.println("runIndex: " + runIndex);
                        CharacterRun characterRun = paragraph.getCharacterRun(runIndex);
                        characterRun.setColor(6);
                    }
                }
            }

            try (FileOutputStream out = new FileOutputStream("D://tttttttttttttttt.doc")) {
                document.write(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runWithDocx() {
        String filePath = "D:\\20260101.docx";
        String searchKey = "开源社区";

        try (XWPFDocument document = new XWPFDocument(new FileInputStream(filePath))) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String paragraphText = paragraph.getText();

                if (StringUtils.isNotEmpty(paragraphText) && paragraphText.contains(searchKey)) {
                    String begin = paragraphText.substring(0, paragraphText.indexOf(searchKey));
                    String end = paragraphText.substring(paragraphText.indexOf(searchKey) + searchKey.length());

                    System.out.println("begin: " + begin);
                    System.out.println("end: " + end);

                    // 清空段落原有内容
                    List<XWPFRun> runs = paragraph.getRuns();
                    for (int i = runs.size() - 1; i >= 0; i--) {
                        paragraph.removeRun(i);
                    }


                    // 添加开始部分
                    if (!begin.isEmpty()) {
                        XWPFRun beginRun = paragraph.createRun();
                        beginRun.setText(begin);
                    }

                    // 添加高亮关键词
                    XWPFRun highLightRun = paragraph.createRun();
                    highLightRun.setColor("ff0000");
                    highLightRun.setText(searchKey);
                    highLightRun.setBold(true);

                    // 设置背景色
//                    CTShd hd = highLightRun.getCTR().addNewRPr().addNewShd();
//                    hd.setVal(STShd.CLEAR);
//                    hd.setColor("auto");
//                    hd.setFill("ffff00");

                    // 添加结束部分
                    if (!end.isEmpty()) {
                        XWPFRun endRun = paragraph.createRun();
                        endRun.setText(end);
                    }
                }
            }
            try (FileOutputStream out = new FileOutputStream("D://eeeeeeeeeeeeeeeeeeee.docx")) {
                document.write(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
