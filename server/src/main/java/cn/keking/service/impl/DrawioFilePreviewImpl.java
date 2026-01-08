package cn.keking.service.impl;

import cn.keking.model.FileAttribute;
import cn.keking.model.PreviewOptions;
import cn.keking.service.FilePreview;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 * Drawio 文件处理
 */
@Service
@AllArgsConstructor
@Slf4j
public class DrawioFilePreviewImpl implements FilePreview {

    private final CommonPreviewImpl commonPreview;

    @Override
    public String filePreviewHandle(PreviewOptions options, Model model, FileAttribute fileAttribute) {
        commonPreview.filePreviewHandle(options,model,fileAttribute);
        return DRAWUI_FILE_PREVIEW_PAGE;
    }
}
