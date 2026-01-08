package cn.keking.service.impl;

import cn.keking.config.ConfigConstants;
import cn.keking.model.FileAttribute;
import cn.keking.model.PreviewOptions;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FileHandlerService;
import cn.keking.service.FilePreview;
import cn.keking.utils.DownloadUtils;
import cn.keking.utils.EncodingDetects;
import cn.keking.utils.KkFileUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.util.HtmlUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Created by kl on 2018/1/17.
 * Content :处理文本文件
 */
@Service
@Slf4j
@AllArgsConstructor
public class SimTextFilePreviewImpl implements FilePreview {
    private final FileHandlerService fileHandlerService;
    private final OtherFilePreviewImpl otherFilePreview;

    @Override
    public String filePreviewHandle(PreviewOptions options, Model model, FileAttribute attribute) {
        String fileName = attribute.getName();
        boolean forceUpdatedCache= attribute.forceUpdatedCache();
        String filePath = attribute.getOriginFilePath();
        if (forceUpdatedCache || !fileHandlerService.listConvertedFiles().containsKey(fileName) || !ConfigConstants.isCacheEnabled()) {
            ReturnResponse<String> response = DownloadUtils.downLoad(attribute, fileName);
            if (response.isFailure()) {
                return otherFilePreview.notSupportedFile(model, attribute, response.getMsg());
            }
            filePath = response.getContent();
            if (ConfigConstants.isCacheEnabled()) {
                fileHandlerService.addConvertedFile(fileName, filePath);  //加入缓存
            }
            try {
                String  fileData = HtmlUtils.htmlEscape(textData(filePath,fileName));

                //高亮处理
                if(!StringUtils.isAnyEmpty(options.getSearchKey(), fileData)){
                    fileData = highLight(fileData, options.getSearchKey());
                }
                model.addAttribute("textData", Base64.encodeBase64String(fileData.getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                return otherFilePreview.notSupportedFile(model, attribute, e.getLocalizedMessage());
            }
            return TXT_FILE_PREVIEW_PAGE;
        }
        String  fileData = null;
        try {
            fileData = HtmlUtils.htmlEscape(textData(filePath,fileName));
        } catch (IOException e) {
            log.error("读取文本文件失败: {}", filePath, e);
        }

        //高亮处理
        if(!StringUtils.isAnyEmpty(options.getSearchKey(), fileData)){
            fileData = highLight(fileData, options.getSearchKey());
        }
        model.addAttribute("textData", Base64.encodeBase64String(fileData.getBytes(StandardCharsets.UTF_8)));
        return TXT_FILE_PREVIEW_PAGE;
    }

    private String highLight(String fileData, String searchKey) {
        if(!StringUtils.isAnyEmpty(fileData, searchKey)){
            return fileData.replaceAll(searchKey, "<mark>" + searchKey + "</mark>");
        }else{
            return fileData;
        }
    }

    private String textData(String filePath,String fileName) throws IOException {
        File file = new File(filePath);
        if (KkFileUtils.isIllegalFileName(fileName)) {
            return null;
        }
        if (!file.exists() || file.length() == 0) {
            return "";
        } else {
            String charset = EncodingDetects.getJavaEncode(filePath);
            if ("ASCII".equals(charset)) {
                charset = StandardCharsets.US_ASCII.name();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), charset));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line).append("\r\n");
            }
            br.close();
            return result.toString();
        }
    }
}
