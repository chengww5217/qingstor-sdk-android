/*
 * Copyright 2018 chengww
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chengww.qingstor_sdk_android.exception;

import com.chengww.qingstor_sdk_android.utils.ResUtils;
import com.qingstor.sdk.constants.QSConstant;
import com.qingstor.sdk.exception.QSException;
import com.qingstor.sdk.model.OutputModel;

import java.io.IOException;

/**
 * Created by chengww on 2018/12/28.
 */
public class TaskException extends Exception {

    private int statusCode;                         //HTTP status code
    private String code;
    private String message;                         //HTTP status message
    private String requestID;
    private String helpUrl;
    private String i18nHint;


    public TaskException(String message) {
        super(message);
        this.message = message;
        i18nHint = message;
    }

    public TaskException(OutputModel outputModel) {
        super(outputModel == null ? null : outputModel.getMessage());
        generateErrorMessage(outputModel);
    }

    public TaskException(QSException e) {
        super(e == null ? null : e.getMessage());
        if (e == null) return;
        statusCode = 1000;
        code = "network_error";
        message = e.getMessage();
        i18nHint = ResUtils.string(code);
    }

    private void generateErrorMessage(OutputModel outputModel) {
        if (outputModel == null) return;
        statusCode = outputModel.getStatueCode();
        code = outputModel.getCode();
        if (statusCode == QSConstant.REQUEST_ERROR_CODE || statusCode == 0)
            code = "network_error";
        message = outputModel.getMessage();
        requestID = outputModel.getRequestId();
        helpUrl = outputModel.getUrl();
        i18nHint = ResUtils.string(code);
    }

    @Override
    public String getMessage() {
        return toString();
    }

    public static TaskException BREAKPOINT_NOT_EXIST() {
        TaskException exception = new TaskException("Breakpoint file does not exist!");
        exception.setStatusCode(2000);
        exception.setCode("breakpoint_file_does_not_exist");
        exception.setI18nHint(ResUtils.string("breakpoint_file_does_not_exist"));
        return exception;
    }

    public static TaskException BREAKPOINT_EXPIRED() {
        TaskException exception = new TaskException("Breakpoint file has expired!");
        exception.setStatusCode(2001);
        exception.setCode("breakpoint_file_expired");
        exception.setI18nHint(ResUtils.string("breakpoint_file_expired"));
        return exception;
    }

    public static TaskException FILE_INVALID(String message) {
        TaskException exception = new TaskException(message);
        exception.setStatusCode(2002);
        exception.setCode("file_invalid");
        exception.setI18nHint(ResUtils.string("file_invalid"));
        return exception;
    }

    public static TaskException NOT_AVAILABLE() {
        TaskException exception = new TaskException("SDCard isn't available, please check SD card and permission: WRITE_EXTERNAL_STORAGE, and you must pay attention to Android6.0 RunTime Permissions!");
        exception.setStatusCode(2003);
        exception.setCode("write_permission_defined");
        exception.setI18nHint(ResUtils.string("write_permission_defined"));
        return exception;
    }

    public static TaskException UNKNOWN() {
        String message = "An unknown error occurred when download";
        TaskException exception = new TaskException(message);
        exception.setStatusCode(3000);
        exception.setCode("unknown_error");
        exception.setI18nHint(ResUtils.string("unknown_error"));
        return exception;
    }

    public static TaskException IO_EXCEPTION(IOException e) {
        TaskException exception = new TaskException(e.getMessage());
        exception.setStatusCode(2004);
        exception.setCode("io_exception");
        exception.setI18nHint(ResUtils.string("io_exception"));
        return exception;
    }


    @Override
    public String toString() {
        return "TaskException{" +
                "statusCode=" + statusCode +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", requestID='" + requestID + '\'' +
                ", helpUrl='" + helpUrl + '\'' +
                '}';
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public void setHelpUrl(String helpUrl) {
        this.helpUrl = helpUrl;
    }

    public String getI18nHint() {
        return i18nHint;
    }

    public void setI18nHint(String i18nHint) {
        this.i18nHint = i18nHint;
    }
}
