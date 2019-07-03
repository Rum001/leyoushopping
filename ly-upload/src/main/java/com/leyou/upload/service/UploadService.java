package com.leyou.upload.service;


import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;


@Service
@Slf4j
@EnableConfigurationProperties({UploadProperties.class})
public class UploadService {
//    private final static List<String>ALLOW_TYPE= Arrays.asList("image/jpg","image/jpeg","image/png","image/pneg");

    @Autowired
    private UploadProperties uploadProperties;

    @Autowired
    private FastFileStorageClient fileStorageClient;
    public String uploadFile(MultipartFile file) {
        try {
        //文件的类型
        String contentType = file.getContentType();
        //文件的校验
        if (!uploadProperties.getAllowTypes().contains(contentType)){
                throw new LyException(ExceptionEnum.FILE_TYPE_MATCHING);
        }
        //文件的内容检查
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image==null){
            throw new LyException(ExceptionEnum.FILE_TYPE_MATCHING);
        }
        String suffix= StringUtils.substringAfterLast(file.getOriginalFilename(),".");
        //上传到FastDFS
        StorePath storePath = fileStorageClient.uploadFile(file.getInputStream(), file.getSize(), suffix, null);
        return uploadProperties.getBaseUrl()+storePath.getFullPath();
        } catch (IOException e) {
            log.error("文件上传失败"+e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
