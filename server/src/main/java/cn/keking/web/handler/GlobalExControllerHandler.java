package cn.keking.web.handler;

import cn.keking.service.FilePreview;
import cn.keking.utils.KkFileUtils;
import cn.keking.web.exceptions.PreviewException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

/**
 * @author LouMT
 * @name GlobalExControllerHandler
 * @date 2026-01-05 16:39
 * @email lmtemail163@163.com
 * @description
 */
@ControllerAdvice
public class GlobalExControllerHandler {
    @ExceptionHandler(PreviewException.class)
    public String handlePreviewException(PreviewException exception, Model model) {
        model.addAttribute("msg", KkFileUtils.htmlEscape(exception.getLocalizedMessage()));
        model.addAttribute("fileType", Optional.of(exception.getFileType()).orElse("未知"));
        return FilePreview.NOT_SUPPORTED_FILE_PAGE;
    }
}


