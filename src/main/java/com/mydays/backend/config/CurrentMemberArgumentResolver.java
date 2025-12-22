package com.mydays.backend.config;

import com.mydays.backend.domain.Member;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
@Component
public class CurrentMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMember.class)
                && Member.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
        if (req == null) return null;

        Object obj = req.getAttribute("authMember"); // JwtAuthFilter가 넣는 키
        if (obj instanceof Member m) return m;

        // (호환) 혹시 currentMember 키로 넣는 경우 대비
        obj = req.getAttribute("currentMember");
        if (obj instanceof Member m2) return m2;

        return null;
    }
}
