package com.fanda.auth.annotation;

import com.fanda.auth.entity.User;
import com.fanda.auth.jwt.exception.SecurityCustomException;
import com.fanda.auth.jwt.exception.TokenErrorCode;
import com.fanda.auth.jwt.userdetails.CustomUserDetails;
import com.fanda.auth.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserQueryService userQueryService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasParameterAnnotation = parameter.hasParameterAnnotation(UserResolver.class);
        boolean isOrganizationParameterType = parameter.getParameterType().isAssignableFrom(User.class);
        return hasParameterAnnotation && isOrganizationParameterType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Object userDetails = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            return userQueryService.findByUserName(((CustomUserDetails) userDetails).getUsername());
        } catch (ClassCastException e) {
            // 로그아웃된 토큰
            throw new SecurityCustomException(TokenErrorCode.UNAUTHORIZED);
        }
    }
}
