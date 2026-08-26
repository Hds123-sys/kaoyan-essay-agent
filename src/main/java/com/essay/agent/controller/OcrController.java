package com.essay.agent.controller;

import com.essay.agent.common.constant.ErrorCodeConstants;
import com.essay.agent.common.context.SessionContext;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.model.OcrResult;
import com.essay.agent.model.dto.response.ApiResponse;
import com.essay.agent.service.OcrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Value("${upload.image-dir:./uploads/images}")
    private String imageDir;

    @Autowired
    private OcrService ocrService;

    @PostMapping("/upload")
    public ApiResponse<OcrResult> upload(@RequestParam("file") MultipartFile file) {
        String sessionId = SessionContext.getSessionId();

        if (file.isEmpty()) {
            throw new BusinessException(ErrorCodeConstants.IMAGE_UPLOAD_ERROR, "图片上传失败");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/jpeg") && !contentType.startsWith("image/jpg") && !contentType.startsWith("image/png"))) {
            throw new BusinessException(ErrorCodeConstants.BAD_REQUEST, "仅支持JPG、JPEG、PNG格式的图片");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCodeConstants.BAD_REQUEST, "图片大小不能超过10MB");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            Path uploadDir = Paths.get(imageDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(newFilename);
            Files.write(filePath, file.getBytes());

            log.info("Image uploaded successfully. sessionId={}, filename={}, size={}",
                    sessionId, newFilename, file.getSize());

            OcrResult ocrResult = ocrService.recognize(filePath.toString());

            log.info("OCR recognition completed successfully. sessionId={}, filename={}, textLength={}",
                    sessionId, newFilename, ocrResult.getOcrText() != null ? ocrResult.getOcrText().length() : 0);

            return ApiResponse.success(ocrResult);

        } catch (IOException e) {
            log.error("Failed to save uploaded image. sessionId={}, filename={}", sessionId, newFilename, e);
            throw new BusinessException(ErrorCodeConstants.IMAGE_UPLOAD_ERROR, "图片上传失败");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OCR processing failed. sessionId={}, filename={}", sessionId, newFilename, e);
            throw new BusinessException(ErrorCodeConstants.OCR_ERROR, "OCR处理失败");
        }
    }
}