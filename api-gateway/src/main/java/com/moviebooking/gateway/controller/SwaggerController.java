package com.moviebooking.gateway.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SwaggerController {

    @GetMapping(value = "/docs", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> swaggerUi() {
        return Mono.just(getCustomSwaggerHtml());
    }

    @GetMapping(value = "/api-docs/swagger-config", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> swaggerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("configUrl", "/api-docs/swagger-config");
        config.put("oauth2RedirectUrl", "http://localhost:8080/webjars/swagger-ui/oauth2-redirect.html");
        config.put("tryItOutEnabled", true);
        config.put("validatorUrl", "");
        
        List<Map<String, String>> urls = List.of(
            Map.of("url", "/user-service/api-docs", "name", "User Service"),
            Map.of("url", "/theatre-service/api-docs", "name", "Theatre Service"),
            Map.of("url", "/booking-service/api-docs", "name", "Booking Service"),
            Map.of("url", "/payment-service/api-docs", "name", "Payment Service"),
            Map.of("url", "/notification-service/api-docs", "name", "Notification Service"),
            Map.of("url", "/search-service/api-docs", "name", "Search Service")
        );
        config.put("urls", urls);
        
        return Mono.just(config);
    }

    private String getCustomSwaggerHtml() {
        return """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8">
                <title>Movie Booking System - API Documentation</title>
                <link rel="stylesheet" type="text/css" href="/webjars/swagger-ui/swagger-ui.css" />
                <link rel="stylesheet" type="text/css" href="/webjars/swagger-ui/index.css" />
                <link rel="icon" type="image/png" href="/webjars/swagger-ui/favicon-32x32.png" sizes="32x32" />
                <link rel="icon" type="image/png" href="/webjars/swagger-ui/favicon-16x16.png" sizes="16x16" />
                <style>
                  html {
                    box-sizing: border-box;
                    overflow: -moz-scrollbars-vertical;
                    overflow-y: scroll;
                  }
                  *, *:before, *:after {
                    box-sizing: inherit;
                  }
                  body {
                    margin:0;
                    background: #fafafa;
                  }
                </style>
              </head>
            
              <body>
                <div id="swagger-ui"></div>
            
                <script src="/webjars/swagger-ui/swagger-ui-bundle.js" charset="UTF-8"> </script>
                <script src="/webjars/swagger-ui/swagger-ui-standalone-preset.js" charset="UTF-8"> </script>
                <script>
                  window.onload = function() {
                    // Fetch the configuration from our config endpoint
                    fetch('/api-docs/swagger-config')
                      .then(response => response.json())
                      .then(config => {
                        console.log('Loaded config:', config);
                        // Initialize Swagger UI with the fetched configuration
                        window.ui = SwaggerUIBundle({
                          urls: config.urls,
                          dom_id: '#swagger-ui',
                          deepLinking: true,
                          presets: [
                            SwaggerUIBundle.presets.apis,
                            SwaggerUIStandalonePreset
                          ],
                          plugins: [
                            SwaggerUIBundle.plugins.DownloadUrl
                          ],
                          layout: "StandaloneLayout",
                          validatorUrl: config.validatorUrl || "",
                          tryItOutEnabled: config.tryItOutEnabled || true,
                          // Explicitly set to null to force dropdown mode
                          url: null,
                          // Ensure the first spec is not loaded by default
                          "urls.primaryName": null
                        });
                      })
                      .catch(error => {
                        console.error('Failed to load Swagger configuration:', error);
                        // Fallback to hardcoded configuration
                        window.ui = SwaggerUIBundle({
                          urls: [
                            {name: "User Service", url: "/user-service/api-docs"},
                            {name: "Theatre Service", url: "/theatre-service/api-docs"},
                            {name: "Booking Service", url: "/booking-service/api-docs"},
                            {name: "Payment Service", url: "/payment-service/api-docs"},
                            {name: "Notification Service", url: "/notification-service/api-docs"},
                            {name: "Search Service", url: "/search-service/api-docs"}
                          ],
                          dom_id: '#swagger-ui',
                          deepLinking: true,
                          presets: [
                            SwaggerUIBundle.presets.apis,
                            SwaggerUIStandalonePreset
                          ],
                          plugins: [
                            SwaggerUIBundle.plugins.DownloadUrl
                          ],
                          layout: "StandaloneLayout",
                          // Explicitly set to null to force dropdown mode
                          url: null,
                          // Ensure the first spec is not loaded by default
                          "urls.primaryName": null
                        });
                      });
                  };
                </script>
              </body>
            </html>
            """;
    }
}