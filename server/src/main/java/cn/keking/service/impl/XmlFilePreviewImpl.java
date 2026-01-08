package cn.keking.service.impl;

import cn.keking.model.FileAttribute;
import cn.keking.model.PreviewOptions;
import cn.keking.service.FilePreview;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 * @author kl (http://kailing.pub)
 * @since 2020/12/25
 */
@Service
@AllArgsConstructor
@Slf4j
public class XmlFilePreviewImpl implements FilePreview {
    private final SimTextFilePreviewImpl simTextFilePreview;

    @Override
    public String filePreviewHandle(PreviewOptions options, Model model, FileAttribute fileAttribute) {
        simTextFilePreview.filePreviewHandle(options, model, fileAttribute);
        return XML_FILE_PREVIEW_PAGE;
    }
}
