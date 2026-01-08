package cn.keking.service.impl;

import cn.keking.model.FileAttribute;
import cn.keking.model.PreviewOptions;
import cn.keking.service.FilePreview;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 * EML 文件处理
 */
@Service
@AllArgsConstructor
@Slf4j
public class EmlFilePreviewImpl implements FilePreview {

    private final CommonPreviewImpl commonPreview;


    @Override
    public String filePreviewHandle(PreviewOptions options, Model model, FileAttribute fileAttribute) {
        commonPreview.filePreviewHandle(options ,model,fileAttribute);
        return EML_FILE_PREVIEW_PAGE;
    }
}
