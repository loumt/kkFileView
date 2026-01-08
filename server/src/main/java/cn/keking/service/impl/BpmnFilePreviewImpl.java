package cn.keking.service.impl;

import cn.keking.model.FileAttribute;
import cn.keking.model.PreviewOptions;
import cn.keking.service.FilePreview;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

/**
 * @author kl (http://kailing.pub)
 * @since 2023/3/9
 */
@Slf4j
@Component
@AllArgsConstructor
public class BpmnFilePreviewImpl implements FilePreview {
    private final CommonPreviewImpl commonPreview;

    @Override
    public String filePreviewHandle(PreviewOptions options, Model model, FileAttribute fileAttribute) {
        commonPreview.filePreviewHandle(options, model, fileAttribute);
        model.addAttribute("fileName", fileAttribute.getName());
        return FilePreview.BPMN_FILE_PREVIEW_PAGE;
    }
}
