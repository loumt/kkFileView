package cn.keking.service.impl;

import cn.keking.config.ConfigConstants;
import cn.keking.model.FileAttribute;
import cn.keking.model.PreviewOptions;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FileHandlerService;
import cn.keking.service.FilePreview;
import cn.keking.service.OfficeToPdfService;
import cn.keking.utils.DownloadUtils;
import cn.keking.utils.KkFileUtils;
import cn.keking.utils.OfficeUtils;
import cn.keking.utils.WebUtils;
import cn.keking.web.exceptions.PreviewException;
import cn.keking.web.filter.BaseUrlFilter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jodconverter.core.office.OfficeException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by kl on 2018/1/17.
 * Content :处理office文件
 */
@Service
@AllArgsConstructor
@Slf4j
public class OfficeFilePreviewImpl implements FilePreview {
    public static final String OFFICE_PREVIEW_TYPE_IMAGE = "image";
    public static final String OFFICE_PREVIEW_TYPE_ALL_IMAGES = "allImages";
    private static final String OFFICE_PASSWORD_MSG = "password";
    private final FileHandlerService fileHandlerService;
    private final OfficeToPdfService officeToPdfService;
    private final OtherFilePreviewImpl otherFilePreview;

    @Override
    public String filePreviewHandle(PreviewOptions options, Model model, FileAttribute fileAttribute) {
        // 预览Type，参数传了就取参数的，没传取系统默认
        String officePreviewType = fileAttribute.getOfficePreviewType();
        boolean userToken = fileAttribute.getUsePasswordCache();
        String baseUrl = BaseUrlFilter.getBaseUrl();
        String suffix = fileAttribute.getSuffix();  //获取文件后缀
        String fileName = fileAttribute.getName(); //获取文件原始名称
        String searchKey = options.getSearchKey(); //获取文件搜索关键字
        String cacheName = fileAttribute.getCacheName();  //转换后的文件名
        String filePassword = fileAttribute.getFilePassword(); //获取密码
        boolean forceUpdatedCache = fileAttribute.forceUpdatedCache();  //是否启用强制更新命令
        boolean isHtmlView = fileAttribute.isHtmlView();  //xlsx  转换成html
        String outFilePath = fileAttribute.getOutFilePath();
        if(StringUtils.isNotEmpty(searchKey)){
            outFilePath = KkFileUtils.appendFileName(outFilePath, searchKey);
            cacheName = cacheName + "_" + searchKey;
        }
        //转换后生成文件的路径
        if (!officePreviewType.equalsIgnoreCase("html")) {
            if (ConfigConstants.getOfficeTypeWeb().equalsIgnoreCase("web")) {
                if (suffix.equalsIgnoreCase("xlsx")) {
                    model.addAttribute("pdfUrl", KkFileUtils.htmlEscape(options.getEncodeUrl())); //特殊符号处理
                    return XLSX_FILE_PREVIEW_PAGE;
                }
                if (suffix.equalsIgnoreCase("csv")) {
                    model.addAttribute("csvUrl", KkFileUtils.htmlEscape(options.getEncodeUrl()));
                    return CSV_FILE_PREVIEW_PAGE;
                }
            }
        }

        if (forceUpdatedCache || !fileHandlerService.listConvertedFiles().containsKey(cacheName) || !ConfigConstants.isCacheEnabled()) {
            // 下载远程文件到本地，如果文件在本地已存在不会重复下载
            ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, fileName);
            if (response.isFailure()) {
                return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
            }
            String filePath = response.getContent();
            boolean isPwdProtectedOffice = OfficeUtils.isPwdProtected(filePath);    // 判断是否加密文件
            if (isPwdProtectedOffice && !StringUtils.isNotEmpty(filePassword)) {
                // 加密文件需要密码
                model.addAttribute("needFilePassword", true);
                return EXEL_FILE_PREVIEW_PAGE;
            } else {
                if (StringUtils.isNotEmpty(outFilePath)) {
                    //查询文档中的关键字, 随便搞搞了
                    String searchFilePath = null;
                    if (StringUtils.isNotEmpty(searchKey)) {
                        searchFilePath = KkFileUtils.appendFileName(ConfigConstants.getFileDir() +  fileName, searchKey);

                        if(!KkFileUtils.isExist(searchFilePath)){
                            //不存在缓存文件下
                            if (suffix.equals("doc")) {
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
                                    try (FileOutputStream out = new FileOutputStream(searchFilePath)) {
                                        document.write(out);
                                    }
                                } catch (IOException e) {
                                    throw new PreviewException("文件操作[关键字标注]出错");
                                }
                            }

                            if (suffix.equals("docx")) {
                                try (XWPFDocument document = new XWPFDocument(new FileInputStream(filePath))) {
                                    for (XWPFParagraph paragraph : document.getParagraphs()) {
                                        String paragraphText = paragraph.getText();
                                        if (StringUtils.isNotEmpty(paragraphText) && paragraphText.contains(searchKey)) {
                                            String begin = paragraphText.substring(0, paragraphText.indexOf(searchKey));
                                            String end = paragraphText.substring(paragraphText.indexOf(searchKey) + searchKey.length());
                                            List<XWPFRun> runs = paragraph.getRuns();
                                            for (int i = runs.size() - 1; i >= 0; i--) {
                                                paragraph.removeRun(i);
                                            }
                                            if (!begin.isEmpty()) {
                                                XWPFRun beginRun = paragraph.createRun();
                                                beginRun.setText(begin);
                                            }
                                            XWPFRun highLightRun = paragraph.createRun();
                                            highLightRun.setColor("ff0000");
                                            highLightRun.setText(searchKey);
                                            if (!end.isEmpty()) {
                                                XWPFRun endRun = paragraph.createRun();
                                                endRun.setText(end);
                                            }
                                        }
                                    }
                                    try (FileOutputStream out = new FileOutputStream(searchFilePath)) {
                                        document.write(out);
                                    }
                                } catch (IOException e) {
                                    throw new PreviewException("文件操作[关键字标注]出错");
                                }
                            }

                        }
                    }

                    try {
                        String parseFilePath = filePath;
                        if(StringUtils.isNotEmpty(searchKey)){
                            parseFilePath = searchFilePath;
                        }
                        officeToPdfService.openOfficeToPDF(parseFilePath, outFilePath, fileAttribute);
                    } catch (OfficeException e) {
                        if (isPwdProtectedOffice && !OfficeUtils.isCompatible(filePath, filePassword)) {
                            // 加密文件密码错误，提示重新输入
                            model.addAttribute("needFilePassword", true);
                            model.addAttribute("filePasswordError", true);
                            return EXEL_FILE_PREVIEW_PAGE;
                        }
                        return otherFilePreview.notSupportedFile(model, fileAttribute, "抱歉，该文件版本不兼容，文件版本错误。");
                    }
                    if (isHtmlView) {
                        // 对转换后的文件进行操作(改变编码方式)
                        fileHandlerService.doActionConvertedFile(outFilePath);
                    }
                    //是否保留OFFICE源文件
                    if (!fileAttribute.isCompressFile() && ConfigConstants.getDeleteSourceFile()) {
                        KkFileUtils.deleteFileByPath(filePath);
                        if(StringUtils.isNotEmpty(searchFilePath)) KkFileUtils.deleteFileByPath(searchFilePath);
                    }
                    if (userToken || !isPwdProtectedOffice) {
                        // 加入缓存
                        String relativePath = fileHandlerService.getRelativePath(outFilePath);
                        fileHandlerService.addConvertedFile(cacheName, relativePath);
                    }
                }
            }

        }
        if (!isHtmlView && baseUrl != null && (OFFICE_PREVIEW_TYPE_IMAGE.equals(officePreviewType) || OFFICE_PREVIEW_TYPE_ALL_IMAGES.equals(officePreviewType))) {
            return getPreviewType(model, fileAttribute, officePreviewType, cacheName, outFilePath, fileHandlerService, OFFICE_PREVIEW_TYPE_IMAGE, otherFilePreview);
        }
        model.addAttribute("pdfUrl", WebUtils.encodeFileName(cacheName));  //输出转义文件名 方便url识别
        return isHtmlView ? EXEL_FILE_PREVIEW_PAGE : PDF_FILE_PREVIEW_PAGE;
    }

    static String getPreviewType(Model model, FileAttribute fileAttribute, String officePreviewType, String pdfName, String outFilePath, FileHandlerService fileHandlerService, String officePreviewTypeImage, OtherFilePreviewImpl otherFilePreview) {
        String suffix = fileAttribute.getSuffix();
        boolean isPPT = suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx");
        List<String> imageUrls = null;
        try {
            imageUrls = fileHandlerService.pdf2jpg(outFilePath, outFilePath, pdfName, fileAttribute);
        } catch (Exception e) {
            Throwable[] throwableArray = ExceptionUtils.getThrowables(e);
            for (Throwable throwable : throwableArray) {
                if (throwable instanceof IOException || throwable instanceof EncryptedDocumentException) {
                    if (e.getMessage().toLowerCase().contains(OFFICE_PASSWORD_MSG)) {
                        model.addAttribute("needFilePassword", true);
                        return EXEL_FILE_PREVIEW_PAGE;
                    }
                }
            }
        }
        if (imageUrls == null || imageUrls.size() < 1) {
            return otherFilePreview.notSupportedFile(model, fileAttribute, "office转图片异常，请联系管理员");
        }
        model.addAttribute("imgUrls", imageUrls);
        model.addAttribute("currentUrl", imageUrls.get(0));
        if (officePreviewTypeImage.equals(officePreviewType)) {
            // PPT 图片模式使用专用预览页面
            return (isPPT ? PPT_FILE_PREVIEW_PAGE : OFFICE_PICTURE_FILE_PREVIEW_PAGE);
        } else {
            return PICTURE_FILE_PREVIEW_PAGE;
        }
    }

}
