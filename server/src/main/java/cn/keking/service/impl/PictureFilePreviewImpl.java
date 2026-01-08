package cn.keking.service.impl;

import cn.keking.model.FileAttribute;
import cn.keking.model.PreviewOptions;
import cn.keking.service.FileHandlerService;
import cn.keking.utils.KkFileUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kl on 2018/1/17.
 * Content :图片文件处理
 */
@Service
@Slf4j
public class PictureFilePreviewImpl extends CommonPreviewImpl {

    private final FileHandlerService fileHandlerService;

    public PictureFilePreviewImpl(FileHandlerService fileHandlerService, OtherFilePreviewImpl otherFilePreview) {
        super(fileHandlerService, otherFilePreview);
        this.fileHandlerService = fileHandlerService;
    }

    @Override
    public String filePreviewHandle(PreviewOptions options, Model model, FileAttribute fileAttribute) {
        String url= KkFileUtils.htmlEscape(options.getEncodeUrl());
        List<String> imgUrls = new ArrayList<>();
        imgUrls.add(url);
        String compressFileKey = fileAttribute.getCompressFileKey();
        List<String> zipImgUrls = fileHandlerService.getImgCache(compressFileKey);
        if (!CollectionUtils.isEmpty(zipImgUrls)) {
            imgUrls.addAll(zipImgUrls);
        }
        // 不是http开头，浏览器不能直接访问，需下载到本地
        options.setEncodeUrl(url);
        super.filePreviewHandle(options, model, fileAttribute);
        model.addAttribute("imgUrls", imgUrls);
        return PICTURE_FILE_PREVIEW_PAGE;
    }
}
