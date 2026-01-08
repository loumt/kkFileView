package cn.keking.model;

import cn.keking.utils.WebUtils;
import cn.keking.web.exceptions.PreviewException;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @author LouMT
 * @name PreviewOptions
 * @date 2026-01-05 16:05
 * @email lmtemail163@163.com
 * @description 预览参数
 */
@Data
public class PreviewOptions {
    /**
     * 文件地址
     */
    private String url;
    /**
     * 搜索关键字
     */
    private String searchKey;
    /**
     * 文件地址1- 源地址
     */
    private String originUrl;
    /**
     * 文件地址2- 对源地址进行URLEncode操作
     */
    private String encodeUrl;

    public void parse(){
        if(!StringUtils.hasText(url)) throw new PreviewException("无URL参数");

        try {
            this.originUrl = WebUtils.decodeUrl(url);
        }catch (Exception e){
            throw new PreviewException("Base64解码失败，请检查你的 %s 是否采用 Base64 + urlEncode 双重编码了！");
        }

        this.encodeUrl = WebUtils.urlEncoderencode(this.originUrl);
        if(!StringUtils.hasText(this.encodeUrl)) throw new PreviewException("非法路径,不允许访问");
    }
}
