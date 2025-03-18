package com.autoprov.autoprov.security.jwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.autoprov.autoprov.services.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

  @Autowired
  private LogService logService;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException, ServletException {

    logService.logApiError(request.getParameter("user"), request.getParameter("action"), request.getMethod(), request.getRequestURI(), null,
    String.valueOf(HttpStatus.UNAUTHORIZED.value()), request.getHeader("Authorization") +"/"+ authException.getMessage(), authException.getStackTrace(),
    request.getRemoteAddr(),
    request.getHeader("Authorization"),
    request.getHeader("User-Agent"));

    // apiErrorLogger.error(String.format(
    //         "User: %s | Action: %s | Method: %s | Endpoint: %s | Payload: %s | Status: %s | Message: %s | StackTrace: %s | IP: %s | Client: %s | Agent: %s",
    //         request.getParameter("action"), request.getParameter("action"), request.getMethod(), request.getRequestURI(), null, String.valueOf(HttpStatus.UNAUTHORIZED.value()), authException.getMessage(), authException.getStackTrace(), request.getRemoteAddr(), request.getHeader("Authorization"), request.getHeader("User-Agent")
    //     ));

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    final Map<String, Object> body = new HashMap<>();
    body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
    body.put("error", "Unauthorized");
    body.put("message", authException.getMessage());
    body.put("path", request.getServletPath());

    final ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getOutputStream(), body);
  }

}