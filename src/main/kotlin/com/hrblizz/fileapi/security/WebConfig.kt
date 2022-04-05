package com.hrblizz.fileapi.security

import com.hrblizz.fileapi.library.LoggerRequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
class WebConfig(
    private val loggerRequestInterceptor: LoggerRequestInterceptor
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(loggerRequestInterceptor)
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/docs").setViewName("forward:/docs/index.html")
    }

    @Bean
    fun api(): Docket? {
        return Docket(DocumentationType.SWAGGER_2).select()
            .apis(RequestHandlerSelectors.basePackage("com.hrblizz.fileapi.controller"))
            .paths(PathSelectors.regex("/.*")).build().apiInfo(metaInfo())
    }

    private fun metaInfo(): ApiInfo? {
        return ApiInfoBuilder().description("File API").title("File Management System").build()
    }
}
