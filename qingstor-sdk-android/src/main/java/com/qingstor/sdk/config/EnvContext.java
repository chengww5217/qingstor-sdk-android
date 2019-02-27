// +-------------------------------------------------------------------------
// | Copyright (C) 2016 Yunify, Inc.
// +-------------------------------------------------------------------------
// | Licensed under the Apache License, Version 2.0 (the "License");
// | you may not use this work except in compliance with the License.
// | You may obtain a copy of the License in the LICENSE file, or at:
// |
// | http://www.apache.org/licenses/LICENSE-2.0
// |
// | Unless required by applicable law or agreed to in writing, software
// | distributed under the License is distributed on an "AS IS" BASIS,
// | WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// | See the License for the specific language governing permissions and
// | limitations under the License.
// +-------------------------------------------------------------------------

package com.qingstor.sdk.config;

import com.qingstor.sdk.constants.QSConstant;
import com.qingstor.sdk.request.ParamValidate;
import com.qingstor.sdk.utils.QSStringUtil;

public class EnvContext implements ParamValidate {

    public static String qingcloudStorHost = "qingstor.com";
    public static String default_protocal = "https";

    private String accessKey;

    private String accessSecret;

    private String host;
    private String port;
    private String protocol = default_protocal;
    private String uri;
    private String log_level = QSConstant.LOGGER_ERROR;
    private String additionalUserAgent;

    //default style, like this: https://bucket-name.zone-id.qingstor.com/object-name
    private String requestUrlStyle;

    /**
     * {@link
     * com.qingstor.sdk.constants.QSConstant#VIRTUAL_HOST_STYLE}:<br>
     * https://bucket-name.zone-id.qingstor.com/object-name <br>
     * {@link
     * com.qingstor.sdk.constants.QSConstant#PATH_STYLE}: <br>
     * https://zone-id.qingstor.com/bucket-name/object-name <br>
     * @return request url style
     */
    public String getRequestUrlStyle() {
        return requestUrlStyle;
    }

    /**
     * You can use this method to change the url style. <br>
     * Now available style: <br>
     * One is the default, when requestUrlStyle != {@link
     * com.qingstor.sdk.constants.QSConstant#PATH_STYLE} <br>
     * You may see the url like this({@link
     * com.qingstor.sdk.constants.QSConstant#VIRTUAL_HOST_STYLE}): <br>
     * https://bucket-name.zone-id.qingstor.com/object-name <br>
     * Otherwise you may see the url like this({@link
     * com.qingstor.sdk.constants.QSConstant#PATH_STYLE}): <br>
     * https://zone-id.qingstor.com/bucket-name/object-name <br>
     * @param requestUrlStyle set QSConstant.PATH_STYLE or QSConstant.VIRTUAL_HOST_STYLE
     */
    public void setRequestUrlStyle(String requestUrlStyle) {
        this.requestUrlStyle = requestUrlStyle;
    }

    private boolean safeOkHttp = true;

    public boolean isSafeOkHttp() {
        return safeOkHttp;
    }


    /**
     * This method will be deleted in subsequent releases
     *
     * @param safeOkHttp is safe okHttp or not
     */
    @Deprecated
    public void setSafeOkHttp(boolean safeOkHttp) {
        this.safeOkHttp = safeOkHttp;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public String getHost() {
        return host;
    }

    /**
     * @param host example: qingstor.com
     */
    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    /**
     * @param port example: 8080
     */
    public void setPort(String port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    /**
     * @param protocol example: https or http
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getUri() {
        return uri;
    }

    public String getRequestUrl() {
        String joinUrl = this.getProtocol() + "://" + this.getHost();
        if (this.getPort() != null) {
            joinUrl += ":" + this.getPort();
        }
        if (this.getUri() != null) {
            joinUrl += this.getUri();
        }
        return joinUrl;
    }

    /**
     * @param uri example: /iaas
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    private EnvContext() {
    }

    public EnvContext(String accessKey, String accessSecret) {
        this.setAccessKey(accessKey);
        this.setAccessSecret(accessSecret);
        this.setHost(qingcloudStorHost);
        QSConstant.LOGGER_LEVEL = this.getLog_level();
    }

    public String getLog_level() {
        return log_level;
    }

    public void setLog_level(String log_level) {
        if (!QSStringUtil.isEmpty(log_level)) {
            QSConstant.LOGGER_LEVEL = log_level;
        }
        this.log_level = log_level;
    }


    /**
     * @return the additionalUserAgent
     */
    public String getAdditionalUserAgent() {
        return additionalUserAgent;
    }

    /**
     * @param additionalUserAgent the additionalUserAgent to set
     */
    public void setAdditionalUserAgent(String additionalUserAgent) {
        this.additionalUserAgent = additionalUserAgent;
    }

    @Override
    public String validateParam() {
        if (QSStringUtil.isEmpty(getAccessKey())) {
            return QSStringUtil.getParameterRequired("AccessKey", "EnvContext");
        }

        if (QSStringUtil.isEmpty(getRequestUrl())) {
            return QSStringUtil.getParameterRequired("host", "EnvContext");
        }
        if (!QSStringUtil.isEmpty(getAdditionalUserAgent())) {
            for (int i = 0; i < getAdditionalUserAgent().length(); i++) {
                char temp = getAdditionalUserAgent().charAt(i);
                int value = (int) temp;
                // Allow space(32) to ~(126) in ASCII Table, exclude "(34).
                if (value < 32 || value > 126 || value == 32 || value == 34) {
                    return "additional User-Agent contains characters that not allowed :"
                            + getAdditionalUserAgent().substring(i, i + 1);
                }
            }
        }
        return null;
    }

}
