package org.example.gateway.user.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.gateway.client.core.ApiInvoker;
import org.example.gateway.client.core.ApiProtocol;
import org.example.gateway.client.core.ApiService;
import org.example.gateway.user.dto.UserInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RestController
@ApiService(serviceId = "backend-user-server", protocol = ApiProtocol.HTTP, patternPath = "/user/**")
public class UserController {

    private static final String SECRET_KEY = "dasgfawghehasghaertraewt";
    private static final String COOKIE_NAME = "user-jwt";

    @ApiInvoker(path = "/login")
    @PostMapping("/login")
    public UserInfo login(@RequestParam("phoneNumber") String phoneNumber, @RequestParam("code") String code, HttpServletResponse response) {
        UserInfo userInfo = new UserInfo(1, "zhangsan", phoneNumber);
        if(phoneNumber != null && "13288888888".equalsIgnoreCase(phoneNumber)) {
            // 登录成功会返回token
            final String token = Jwts.builder().setSubject(String.valueOf(userInfo.getId())).setIssuedAt(new Date())
                    .signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
            response.addCookie(new Cookie(COOKIE_NAME, token));
        }
        return userInfo;
    }


    /**
     * 扫描的时候得到映射关系 /user/private/getUserInfo --> /user/getUserInfo
     * @param userId
     * @return
     */
    @GetMapping("/private/getUserInfo")
    public UserInfo getUserInfo(@RequestParam("userId") String userId) {
        final UserInfo userInfo = new UserInfo(Long.parseLong(userId), "zhangsan", "13288888888");
        return userInfo;
    }
}
