package ca.uhn.fhir.config;

import javax.persistence.EntityManagerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ca.uhn.fhir.jpa.config.BaseJavaConfigDstu1;
import ca.uhn.fhir.jpa.dao.DaoConfig;

@Configuration
@Import(CommonConfig.class)
@EnableTransactionManagement()
public class Dstu1Config extends BaseJavaConfigDstu1 {

	
	/**
	 * This lets the "@Value" fields reference properties from the properties file
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean()
	public DaoConfig daoConfig() {
		DaoConfig retVal = new DaoConfig();
		retVal.setSubscriptionEnabled(false);
		retVal.setAllowMultipleDelete(false);
		retVal.setAllowExternalReferences(true);
		return retVal;
	}

	@Bean()
	public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager retVal = new JpaTransactionManager();
		retVal.setEntityManagerFactory(entityManagerFactory);
		return retVal;
	}
	
}
