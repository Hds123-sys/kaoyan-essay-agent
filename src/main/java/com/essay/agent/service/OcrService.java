package com.essay.agent.service;

import com.essay.agent.common.exception.BusinessException;
import com.essay.agent.model.OcrResult;
import com.essay.agent.model.OcrWord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OcrService {

    private static final String OCR_TOKEN_KEY = "ocr:access_token";
    private static final String BAIDU_OAUTH_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String BAIDU_OCR_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";

    @Value("${baidu.api-key}")
    private String apiKey;

    @Value("${baidu.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public OcrService(RestTemplate restTemplate, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public OcrResult recognize(String imagePath) {
        try {
            String accessToken = getAccessToken();

            File imageFile = new File(imagePath);
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String url = BAIDU_OCR_URL + "?access_token=" + accessToken;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("image", base64Image);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            return parseOcrResponse(response.getBody(), imageFile.getName());

        } catch (Exception e) {
            log.error("OCR recognition failed, retrying... imagePath={}", imagePath, e);
            try {
                return recognizeWithRetry(imagePath);
            } catch (Exception ex) {
                log.error("OCR recognition failed after retry. imagePath={}", imagePath, ex);
                throw new BusinessException(50002, "OCR识别失败");
            }
        }
    }

    private OcrResult recognizeWithRetry(String imagePath) {
        try {
            String accessToken = getAccessToken();

            File imageFile = new File(imagePath);
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String url = BAIDU_OCR_URL + "?access_token=" + accessToken;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("image", base64Image);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            return parseOcrResponse(response.getBody(), imageFile.getName());

        } catch (Exception e) {
            throw new BusinessException(50002, "OCR识别失败");
        }
    }

    private OcrResult parseOcrResponse(String responseBody, String fileName) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);

        JsonNode wordsResult = root.path("words_result");
        if (!wordsResult.isArray()) {
            throw new BusinessException(50002, "OCR响应格式错误");
        }

        OcrResult ocrResult = new OcrResult();
        ocrResult.setImageUrl("/api/images/" + fileName);

        List<OcrWord> wordList = new ArrayList<>();
        StringBuilder textBuilder = new StringBuilder();
        double totalConfidence = 0;

        for (JsonNode wordNode : wordsResult) {
            String word = wordNode.path("words").asText();
            double probability = wordNode.path("probability").asDouble(0.0);

            textBuilder.append(word).append(" ");

            OcrWord ocrWord = new OcrWord();
            ocrWord.setWord(word);
            ocrWord.setConfidence(probability);
            wordList.add(ocrWord);

            totalConfidence += probability;
        }

        ocrResult.setOcrText(textBuilder.toString().trim());
        ocrResult.setWords(wordList);

        if (!wordList.isEmpty()) {
            ocrResult.setAverageConfidence(totalConfidence / wordList.size());
        } else {
            ocrResult.setAverageConfidence(0.0);
        }

        String ocrText = ocrResult.getOcrText();
        if (ocrText.length() > 0) {
            int englishChars = 0;
            int totalChars = ocrText.replaceAll("\\s", "").length();
            for (char c : ocrText.toCharArray()) {
                if (Character.isLetter(c) && Character.UnicodeBlock.of(c) == Character.UnicodeBlock.BASIC_LATIN) {
                    englishChars++;
                }
            }
            if (totalChars > 0 && (double) englishChars / totalChars < 0.8) {
                ocrResult.setWarning("识别结果可能非英语内容");
            }
        }

        return ocrResult;
    }

    private String getAccessToken() {
        String cachedToken = redisTemplate.opsForValue().get(OCR_TOKEN_KEY);
        if (cachedToken != null) {
            return cachedToken;
        }

        String url = BAIDU_OAUTH_URL + "?grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String accessToken = root.path("access_token").asText();

            redisTemplate.opsForValue().set(OCR_TOKEN_KEY, accessToken, 2592000, TimeUnit.SECONDS);

            return accessToken;
        } catch (IOException e) {
            log.error("Failed to parse access token response", e);
            throw new BusinessException(50002, "获取OCR访问令牌失败");
        }
    }
}