package org.example.gateway.core.filter.user;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import org.apache.commons.lang3.StringUtils;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.common.exception.ResponseException;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FilterAspect(id = FilterConst.USER_AUTH_FILTER_ID, name = FilterConst.USER_AUTH_FILTER_NAME, order = FilterConst.USER_AUTH_FILTER_ORDER)
public class UserAuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthFilter.class);

    private static final String SECRET_KEY = "dasgfawghehasghaertraewt";

    private static final String COOKIE_NAME = "user-jwt";


    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        // 检查是否需要鉴权
        if(ctx.getRule().getFilterConfig(FilterConst.USER_AUTH_FILTER_ID) == null) {
            return;
        }
        final String token = ctx.getRequest().getCookie(COOKIE_NAME).value();
        if(StringUtils.isBlank(token)) {
            throw new ResponseException(ResponseCode.UNAUTHORIZED);
        }
        try{
            // 解析token，拿到用户id
            long userId = parseUserId(token);
            // 将用户id传递给下游服务
            ctx.getRequest().setUserId(userId);
        }catch (Exception e) {
            // 解析token异常说明token不合法
            throw new ResponseException(ResponseCode.UNAUTHORIZED);
        }
    }

    /**
     * 解析token
     * @param token
     * @return
     */
    private long parseUserId(String token) {
        final Jwt jwt = Jwts.parser().setSigningKey(SECRET_KEY).parse(token);
        return Long.parseLong(((DefaultClaims)jwt.getBody()).getSubject());
    }
}
