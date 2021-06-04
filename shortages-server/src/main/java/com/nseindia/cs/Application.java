package com.nseindia.cs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import com.nseindia.csShortages.ClearingSettlementModuleConfiguration;
import com.nseindia.csDashboard.DashboardModuleConfiguration;


@SpringBootApplication
@Import({
	ClearingSettlementModuleConfiguration.class,
		DashboardModuleConfiguration.class,
})
@EnableSwagger2
@EnableFeignClients(basePackages ="com.nseindia")
public class Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
