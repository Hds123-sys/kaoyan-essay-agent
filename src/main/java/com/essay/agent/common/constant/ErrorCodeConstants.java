package com.essay.agent.common.constant;

public interface ErrorCodeConstants {

    int BAD_REQUEST = 400;
    int FORBIDDEN = 403;
    int TOO_MANY_REQUESTS = 429;
    int INTERNAL_ERROR = 500;

    int SESSION_INVALID = 40001;
    int ESSAY_EMPTY = 40002;
    int NON_ENGLISH = 40003;
    int ESSAY_TOO_LONG = 40004;
    int SENSITIVE_CONTENT = 40005;
    int ESSAY_TYPE_REQUIRED = 40006;
    int UNAUTHORIZED_ACCESS = 40301;
    int RATE_LIMIT_EXCEEDED = 42901;
    int REQUEST_IN_PROGRESS = 42902;
    int CLAUDE_API_ERROR = 50001;
    int OCR_ERROR = 50002;
    int IMAGE_UPLOAD_ERROR = 50003;

}