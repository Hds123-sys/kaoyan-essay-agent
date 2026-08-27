package com.essay.agent.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.common.util.LanguageDetector;
import com.essay.agent.model.dto.response.OcrResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service
public class OcrService {

    @Value("${baidu.ocr.api-key}")
    private String apiKey;

    @Value("${baidu.ocr.secret-key}")
    private String secretKey;

    private String accessToken;

    private long tokenExpireTime;

    public OcrResponse ocr(String imageUrl) {
        try {
            ensureAccessToken();

            HttpResponse response = HttpRequest.post("https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=" + accessToken)
                    .form("url", imageUrl)
                    .timeout(30000)
                    .execute();

            if (response.getStatus() != 200) {
                throw new BusinessException(500, "OCR调用失败: " + response.getStatus());
            }

            String responseBody = response.body();
            JSONObject result = JSONUtil.parseObj(responseBody);

            if (result.getInt("error_code", null) != null) {
                throw new BusinessException(500, "OCR错误: " + result.getStr("error_msg"));
            }

            String text = result.getJSONArray("words_result")
                    .stream()
                    .map(obj -> ((JSONObject) obj).getStr("words"))
                    .reduce("", (a, b) -> a + "\n" + b)
                    .trim();

            String language = LanguageDetector.detect(text);

            return OcrResponse.builder()
                    .text(text)
                    .language(language)
                    .confidence(95)
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OCR error", e);
            throw new BusinessException(500, "OCR识别失败: " + e.getMessage());
        }
    }

    private synchronized void ensureAccessToken() {
        long now = System.currentTimeMillis();
        if (accessToken != null && now < tokenExpireTime) {
            return;
        }

        try {
            HttpResponse response = HttpRequest.post("https://aip.baidubce.com/oauth/2.0/token")
                    .form("grant_type", "client_credentials")
                    .form("client_id", apiKey)
                    .form("client_secret", secretKey)
                    .timeout(10000)
                    .execute();

            if (response.getStatus() != 200) {
                throw new BusinessException(500, "获取百度OCR token失败");
            }

            String responseBody = response.body();
            JSONObject result = JSONUtil.parseObj(responseBody);

            accessToken = result.getStr("access_token");
            int expiresIn = result.getInt("expires_in", 2592000);
            tokenExpireTime = now + (expiresIn - 300) * 1000L;

            log.info("Baidu OCR token refreshed, expires in {} seconds", expiresIn);

        } catch (Exception e) {
            log.error("Failed to get Baidu OCR token", e);
            throw new BusinessException(500, "获取百度OCR token失败");
        }
    }
}