package cn.keking.web.exceptions;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author LouMT
 * @name GlobalException
 * @date 2026-01-05 16:41
 * @email lmtemail163@163.com
 * @description
 */
@Getter
public class PreviewException extends RuntimeException {
    private String fileType;
    public PreviewException(String msg) {
        super(msg);
    }
    public PreviewException(String msg, String fileType) {
        super(msg);
        this.fileType = fileType;
    }
}
