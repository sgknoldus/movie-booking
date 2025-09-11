window.onload = function() {
  // Custom Swagger UI initialization for microservices
  
  // Fetch the configuration from our config endpoint
  fetch('/api-docs/swagger-config')
    .then(response => response.json())
    .then(config => {
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
        configUrl: config.configUrl,
        // Don't set a default URL - force dropdown usage
        urls: config.urls
      });
    })
    .catch(error => {
      console.error('Failed to load Swagger configuration:', error);
      // Fallback to default configuration
      window.ui = SwaggerUIBundle({
        urls: [
          {name: "User Service", url: "/user-service/api-docs"},
          {name: "Movie Service", url: "/movie-service/api-docs"}, 
          {name: "Theatre Service", url: "/theatre-service/api-docs"},
          {name: "Booking Service", url: "/booking-service/api-docs"},
          {name: "Payment Service", url: "/payment-service/api-docs"},
          {name: "Notification Service", url: "/notification-service/api-docs"}
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
        layout: "StandaloneLayout"
      });
    });
};