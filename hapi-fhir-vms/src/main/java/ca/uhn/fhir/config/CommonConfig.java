package ca.uhn.fhir.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import ca.uhn.fhir.FhirRestfulServer;
import ca.uhn.fhir.interceptor.AnalyticsInterceptor;
import ca.uhn.fhir.joke.HolyFooCowInterceptor;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;

@Configuration
public class CommonConfig {
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	public static Properties prop;
	InputStream inputStream;
	static LocalContainerEntityManagerFactoryBean retVal;
	int count = 0;
	/**
	 * Do some fancy logging to create a nice access log that has details about each incoming request.
	 */
	@Bean
	public IServerInterceptor accessLoggingInterceptor() {
		LoggingInterceptor retVal = new LoggingInterceptor();
		retVal.setLoggerName("fhirtest.access");
		retVal.setMessageFormat(
				"Path[${servletPath}] Source[${requestHeader.x-forwarded-for}] Operation[${operationType} ${operationName} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] ResponseEncoding[${responseEncodingNoDefault}]");
		retVal.setLogExceptions(true);
		retVal.setErrorMessageFormat("ERROR - ${requestVerb} ${requestUrl}");
		return retVal;
	}

	/**
	 * This interceptor pings Google Analytics with usage data for the server
	 */
	@Bean
	public IServerInterceptor analyticsInterceptor() {
		AnalyticsInterceptor retVal = new AnalyticsInterceptor();
		retVal.setAnalyticsTid("UA-1395874-6");
		return retVal;
	}
	
	/**
	 * Do some fancy logging to create a nice access log that has details about each incoming request.
	 */
	@Bean
	public IServerInterceptor requestLoggingInterceptor() {
		LoggingInterceptor retVal = new LoggingInterceptor();
		retVal.setLoggerName("fhirtest.request");
		retVal.setMessageFormat("${requestVerb} ${servletPath} -\n${requestBodyFhir}");
		retVal.setLogExceptions(false);
		return retVal;
	}
	
	/**
	 * This is a joke
	 * 
	 * https://chat.fhir.org/#narrow/stream/implementers/topic/Unsupported.20search.20parameters
	 */
	@Bean
	public IServerInterceptor holyFooCowInterceptor() {
		return new HolyFooCowInterceptor();
	}
	
	public DataSource dataSource() {
		BasicDataSource retVal = new BasicDataSource();
		retVal.setDriverClassName(prop.getProperty("DB.driver"));
	    retVal.setUrl(prop.getProperty("DB.url"));
	    retVal.setUsername(prop.getProperty("DB.username"));
	    retVal.setPassword(prop.getProperty("DB.password"));
		return retVal;
	}

	@Bean()
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		CommonProperties();
		if(retVal == null) {
			retVal = new LocalContainerEntityManagerFactoryBean();
			retVal.setPersistenceUnitName("Fhir");			
			retVal.setDataSource(dataSource());
			retVal.setPackagesToScan("ca.uhn.fhir.jpa.entity");
			retVal.setPersistenceProvider(new HibernatePersistenceProvider());
			retVal.setJpaProperties(jpaProperties());
			System.out.println("Persistance");
		}
		return retVal;
	}
	
	private Properties jpaProperties() {
		Properties extraProperties = new Properties();
		extraProperties.put("hibernate.dialect", prop.getProperty("DB.dialect"));	
		extraProperties.put("hibernate.format_sql", prop.getProperty("DB.format_sql"));
		extraProperties.put("hibernate.show_sql", prop.getProperty("DB.show_sql"));
		extraProperties.put("hibernate.hbm2ddl.auto", prop.getProperty("DB.hbm2ddl_auto"));
		extraProperties.put("hibernate.jdbc.batch_size", prop.getProperty("DB.jdbc_batch_size"));
		extraProperties.put("hibernate.cache.use_query_cache", prop.getProperty("DB.use_query_cache"));
		extraProperties.put("hibernate.cache.use_second_level_cache", prop.getProperty("DB.use_second_level_cache"));
		extraProperties.put("hibernate.cache.use_structured_entries", prop.getProperty("DB.use_structured_entries"));
		extraProperties.put("hibernate.cache.use_minimal_puts", prop.getProperty("DB.use_minimal_puts"));
		extraProperties.put("hibernate.search.default.directory_provider", prop.getProperty("DB.default_directory_provider"));
		extraProperties.put("hibernate.search.default.indexBase", prop.getProperty("DB.default_indexBase"));
		extraProperties.put("hibernate.search.lucene_version", prop.getProperty("DB.lucene_version"));
		return extraProperties;
	}
	
	public void CommonProperties(){
		Properties prop1 = new Properties();
		try {		
			String osVersion = System.getProperty("os.name");
			String filename = "/usr/local/tomcat/webapps/config.properties";
			if(osVersion.startsWith("Windows")){
				inputStream = this.getClass().getClassLoader().getResourceAsStream("config.properties");				
			} else {
				inputStream = new FileInputStream(filename);
			}			
			if (inputStream != null) {
				prop1.load(inputStream);
			} else {
				throw new FileNotFoundException("property file (config.properties) not found in the classpath");
			}
			System.out.println("OS Version : "+ osVersion);
		} catch (IOException e) {
			e.printStackTrace();
		}
		prop = prop1;
		
	}

}
