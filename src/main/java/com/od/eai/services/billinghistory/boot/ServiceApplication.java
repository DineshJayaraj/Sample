package com.od.eai.services.billinghistory.boot;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.EndpointMBeanExportAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;


@SpringBootApplication

@ImportResource({"classpath:./META-INF/spring/camel-context.xml", "classpath:billingHistoryObfuscatorConfiguration.xml", "classpath:framework-obfuscator.xml"})
@PropertySource( value = {"application.properties", "framework-core.properties"})
@EnableAutoConfiguration(exclude = {JmxAutoConfiguration.class,
                                    EndpointMBeanExportAutoConfiguration.class,
                                    JmsAutoConfiguration.class})

public class ServiceApplication {
	public static void main(String[] args) {
		if (args.length > 0) {
		  System.getProperties().put("server.address", args[0]);
		  if (args.length > 1) {
		  System.getProperties().put("server.port", args[1]);
		  }
		}

		SpringApplication.run(ServiceApplication.class, args);
	}
	
	@Bean
	ServletRegistrationBean camelServlet() {
		ServletRegistrationBean mapping = new ServletRegistrationBean();
		mapping.setName("CamelServlet");
		mapping.setLoadOnStartup(1);
		mapping.setServlet(new CamelHttpTransportServlet());
		mapping.addUrlMappings("/*");
		return mapping;

	}
	
  }
