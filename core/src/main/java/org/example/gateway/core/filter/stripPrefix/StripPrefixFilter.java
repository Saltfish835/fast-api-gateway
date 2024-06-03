package org.example.gateway.core.filter.stripPrefix;

import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.constants.BasicConst;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 去掉请求的N层前缀
 */
@FilterAspect(id = FilterConst.STRIP_PREFIX_FILTER_ID, name = FilterConst.STRIP_PREFIX_FILTER_NAME, order = FilterConst.STRIP_PREFIX_FILTER_ORDER)
public class StripPrefixFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(StripPrefixFilter.class);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        logger.debug("StripPrefixFilter: {}", ctx.toString());
        StripPrefixFilterConfig stripPrefixConfig = (StripPrefixFilterConfig)ctx.getRule().getFilterConfig(FilterConst.STRIP_PREFIX_FILTER_ID);
        if(stripPrefixConfig == null) {
            return;
        }
        // 要去掉几层前缀
        Integer value = stripPrefixConfig.getValue();
        String modifyPath = ctx.getRequest().getModifyPath();
        String[] items = modifyPath.split(BasicConst.PATH_SEPARATOR);
        StringBuffer newModifyPath = new StringBuffer();
        for(int i=value+1; i<items.length; i++) {
            newModifyPath.append(BasicConst.PATH_SEPARATOR + items[i]);
        }
        ctx.getRequest().setModifyPath(newModifyPath.toString());
    }

    @Override
    public FilterConfig toFilterConfig(JSONObject filterConfigJsonObj) {
        final StripPrefixFilterConfig stripPrefixConfig = new StripPrefixFilterConfig();
        stripPrefixConfig.setId(filterConfigJsonObj.getString("id"));
        stripPrefixConfig.setValue(filterConfigJsonObj.getIntValue("value"));
        return stripPrefixConfig;
    }
}
