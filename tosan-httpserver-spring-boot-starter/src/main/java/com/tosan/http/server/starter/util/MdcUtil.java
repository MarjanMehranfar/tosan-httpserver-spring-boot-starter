package com.tosan.http.server.starter.util;

import com.tosan.http.server.starter.config.HttpHeaderMdcParameter;
import com.tosan.http.server.starter.config.MdcFilterConfig;
import com.tosan.http.server.starter.config.RandomGenerationType;
import com.tosan.http.server.starter.config.RandomParameter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author mina khoshnevisan
 * @since 7/16/2022
 */
public class MdcUtil {

    private final MdcFilterConfig mdcFilterConfig;

    public MdcUtil(MdcFilterConfig mdcFilterConfig) {
        this.mdcFilterConfig = mdcFilterConfig;
    }

    public void extractHeaderMdcParameters(HttpServletRequest request) {
        List<HttpHeaderMdcParameter> parameters = mdcFilterConfig.getParameters();
        if (parameters != null && parameters.size() > 0) {
            for (HttpHeaderMdcParameter headerMdcParameter : parameters) {
                String value = request.getHeader(headerMdcParameter.getHeaderParameterName());
                processMdcParameter(headerMdcParameter, value);
            }
        }
    }

    public void processMdcParameter(HttpHeaderMdcParameter headerMdcParameter, String value) {
        if (headerMdcParameter.isReplaceUnfreeCharacters()) {
            value = replaceUnfreeChars(value);
        }
        value = checkAndApplyRandomParameter(headerMdcParameter, value);
        MDC.put(headerMdcParameter.getMdcParametersName(), value);
    }

    protected String checkAndApplyRandomParameter(HttpHeaderMdcParameter headerMdcParameter, String value) {
        RandomParameter randomParameter = headerMdcParameter.getRandomParameter();
        if (randomParameter != null && StringUtils.isEmpty(value)) {
            RandomGenerationType generationType = randomParameter.getGenerationType();
            if (generationType != null) {
                switch (generationType) {
                    case ALPHANUMERIC:
                        value = randomParameter.getPrefix() + RandomStringUtils.randomAlphanumeric(randomParameter.getLength());
                        break;
                    case NUMERIC:
                        value = randomParameter.getPrefix() + RandomStringUtils.randomNumeric(randomParameter.getLength());
                        break;
                }
            }
        }
        return value;
    }

    public String replaceUnfreeChars(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }
        char[] unFreeChars = mdcFilterConfig.getUnFreeChars();
        if (unFreeChars != null) {
            for (char unFreeChar : unFreeChars) {
                inputString = inputString.replace(unFreeChar, mdcFilterConfig.getNewChar());
            }
        }
        return inputString;
    }

    public void fillRemoteClientIp() {
        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRemoteAddr();
        if (remoteAddress != null) {
            MDC.put(Constants.REMOTE_USER_IP_PARAMETER_NAME, remoteAddress);
        }
    }

    public void clear() {
        MDC.clear();
    }
}