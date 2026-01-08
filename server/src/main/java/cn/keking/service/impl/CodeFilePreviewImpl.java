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
 * @since 2021/2/18
 */
@Component
@AllArgsConstructor
@Slf4j
public class CodeFilePreviewImpl implements FilePreview {
   private final SimTextFilePreviewImpl filePreviewHandle;

    @Override
    public String filePreviewHandle(PreviewOptions options, Model model, FileAttribute fileAttribute) {
        filePreviewHandle.filePreviewHandle(options, model, fileAttribute);
        return CODE_FILE_PREVIEW_PAGE;
    }
}
