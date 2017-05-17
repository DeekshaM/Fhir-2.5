package ca.uhn.fhir.config;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ca.uhn.fhir.interceptor.PublicSecurityInterceptor;
import ca.uhn.fhir.jpa.config.BaseJavaConfigDstu3;
import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.jpa.util.SubscriptionsRequireManualActivationInterceptorDstu3;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.validation.ResultSeverityEnum;

@Configuration
@Import(CommonConfig.class)
@EnableTransactionManagement()
public class Dstu3Config extends BaseJavaConfigDstu3 {

	@Bean()
	public DaoConfig daoConfig() {
		DaoConfig retVal = new DaoConfig();
		retVal.setSubscriptionEnabled(true);
		retVal.setSubscriptionPollDelay(5000);
		retVal.setSubscriptionPurgeInactiveAfterMillis(DateUtils.MILLIS_PER_HOUR);
		retVal.setAllowMultipleDelete(true);
		retVal.setAllowInlineMatchUrlReferences(true);
		retVal.setAllowExternalReferences(true);
		retVal.setHardSearchLimit(500);
		return retVal;
	}

	@Override
	@Bean(autowire = Autowire.BY_TYPE)
	public DatabaseBackedPagingProvider databaseBackedPagingProvider() {
		DatabaseBackedPagingProvider retVal = super.databaseBackedPagingProvider();
		retVal.setDefaultPageSize(20);
		retVal.setMaximumPageSize(500);
		return retVal;
	}

	
	@Bean 
	public IServerInterceptor securityInterceptor() {
		return new PublicSecurityInterceptor();
	}
		
	/**
	 * Bean which validates incoming requests
	 */
	@Bean
	@Lazy
	public RequestValidatingInterceptor requestValidatingInterceptor() {
		RequestValidatingInterceptor requestValidator = new RequestValidatingInterceptor();
		requestValidator.setFailOnSeverity(null);
		requestValidator.setAddResponseHeaderOnSeverity(null);
		requestValidator.setAddResponseOutcomeHeaderOnSeverity(ResultSeverityEnum.INFORMATION);
		requestValidator.addValidatorModule(instanceValidatorDstu3());
		requestValidator.setIgnoreValidatorExceptions(true);
		return requestValidator;
	}

	@Bean(autowire = Autowire.BY_TYPE)
	public IServerInterceptor subscriptionSecurityInterceptor() {
		return new SubscriptionsRequireManualActivationInterceptorDstu3();
	}

	@Bean()
	public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager retVal = new JpaTransactionManager();
		retVal.setEntityManagerFactory(entityManagerFactory);
		return retVal;
	}

	/**
	 * This lets the "@Value" fields reference properties from the properties file
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
