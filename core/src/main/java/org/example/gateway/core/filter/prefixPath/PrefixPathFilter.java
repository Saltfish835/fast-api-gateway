package org.example.gateway.core.filter.prefixPath;


import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 给请求path加上前缀
 */
@FilterAspect(id = FilterConst.PREFIX_PATH_FILTER_ID, name = FilterConst.PREFIX_PATH_FILTER_NAME, order = FilterConst.PREFIX_PATH_FILTER_ORDER)
public class PrefixPathFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(PrefixPathFilter.class);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        // 拿到配置
        PrefixPathFilterConfig prefixPathConfig = (PrefixPathFilterConfig)ctx.getRule().getFilterConfig(FilterConst.PREFIX_PATH_FILTER_ID);
        // 要添加的前缀
        String prefix = prefixPathConfig.getValue();
        String modifyPath = ctx.getRequest().getModifyPath();
        ctx.getRequest().setModifyPath(prefix + modifyPath);
        logger.debug("prefix is {}, modify path is {}",prefix, prefix + modifyPath);
    }

    @Override
    public FilterConfig toFilterConfig(JSONObject filterConfigJsonObj) {
        final PrefixPathFilterConfig prefixPathConfig = new PrefixPathFilterConfig();
        prefixPathConfig.setId(filterConfigJsonObj.getString("id"));
        prefixPathConfig.setValue(filterConfigJsonObj.getString("value"));
        return prefixPathConfig;
    }
}
