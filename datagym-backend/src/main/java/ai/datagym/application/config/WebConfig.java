package ai.datagym.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web MVC configuration.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // font files for caching for internet explorer
        registry.addResourceHandler("/**/*.eot", "/**/*.woff", "/**/*.ttf", "/**/*.woff2", "/**/*.otf")
                .addResourceLocations("classpath:/web/").setCachePeriod(1);

        // add frontend to resource-location
        registry.addResourceHandler("/**").addResourceLocations("classpath:/web/").setCachePeriod(0);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:index.html");
    }
}
