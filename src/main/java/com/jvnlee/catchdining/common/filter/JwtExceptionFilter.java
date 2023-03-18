package com.jvnlee.catchdining.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvnlee.catchdining.common.web.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;
import static org.springframework.http.MediaType.*;

@RequiredArgsConstructor
public class JwtExceptionFilter extends GenericFilterBean {

    private final ObjectMapper om;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            HttpServletResponse res = (HttpServletResponse) response;
            res.setStatus(SC_BAD_REQUEST);
            res.setContentType(APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            om.writeValue(res.getWriter(), new Response<>(e.getMessage()));
        }
    }

}
