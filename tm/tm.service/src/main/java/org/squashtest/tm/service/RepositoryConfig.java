/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.validation.ValidatorFactory;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.context.annotation.aspectj.SpringConfiguredConfiguration;
import org.springframework.core.Ordered;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaSessionFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.squashtest.tm.service.internal.hibernate.UberCustomPersistenceProvider;

/**
 * Configuration for repository layer.
 *
 * "implements TransactionManagementConfigurer" seems necessary because we dont't use standard JPA. As a consequence,
 * @EnableTransactionManagement magic don't seem to properly kick in.
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE + 100, mode = AdviceMode.PROXY, proxyTargetClass = false)
@Import(AspectJTransactionManagementConfiguration.class)
@EnableJpaRepositories("org.squashtest.tm.service.internal.repository")
public class RepositoryConfig implements TransactionManagementConfigurer{
	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryConfig.class);

	@Inject
	private DataSource dataSource;

	@Inject
	private AbstractEnvironment env;

	@Value("${hibernate.dialect}")
	private String hibernateDialect;

	public RepositoryConfig() {
		super();
	}

	@Bean
	public DefaultLobHandler lobHandler() {
		return new DefaultLobHandler();
	}

	/*
	@Bean(name = "squashtest.tm.persistence.hibernate.SessionFactory")
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	@DependsOn(SpringConfiguredConfiguration.BEAN_CONFIGURER_ASPECT_BEAN_NAME)
	public LocalSessionFactoryBean sessionFactory() {
		if (StringUtils.defaultString(hibernateDialect).toLowerCase().contains("h2")) {
			LOGGER.warn("I'm configured to use the '{}' H2 dialect. H2 is not to be used as a production database !", hibernateDialect);
		}

		LocalSessionFactoryBean sessionFactoryBean = new SquashSessionFactoryBean();

		sessionFactoryBean.setDataSource(dataSource);
		sessionFactoryBean.setAnnotatedPackages("org.squashtest.tm.service.internal.repository.hibernate",
				"org.squashtest.tm.service.internal.hibernate");
		sessionFactoryBean.setPackagesToScan("org.squashtest.tm.domain",
				"org.squashtest.csp.core.bugtracker.domain");
		sessionFactoryBean.setNamingStrategy(new UppercaseUnderscoreNamingStrategy());
		sessionFactoryBean.setEntityInterceptor(new AuditLogInterceptor());
		sessionFactoryBean.setHibernateProperties(hibernateProperties());

		return sessionFactoryBean;
	}
	*/
	
	@Bean(name="entityManagerFactory")
	@DependsOn(SpringConfiguredConfiguration.BEAN_CONFIGURER_ASPECT_BEAN_NAME)
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public EntityManagerFactory entityManagerFactory(){

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		UberCustomPersistenceProvider provider = new UberCustomPersistenceProvider();
		
		
	    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

	    factory.setJpaVendorAdapter(vendorAdapter);
	    factory.setPersistenceProvider(provider);

	    factory.setDataSource(dataSource);
	    factory.setPackagesToScan(
	    		// annotated packages (scanned in this method since Spring 4.1)
	    		"org.squashtest.tm.service.internal.repository.hibernate", 
	    		"org.squashtest.tm.service.internal.hibernate",
	    		
	    		// annotated classes
	    		"org.squashtest.tm.domain",
				"org.squashtest.csp.core.bugtracker.domain"
	    );	   
	    
	    // naming strategy and interceptor are set in SquashEntityManagerFactoryBuilderImpl
	    
	    // setting the properties
	    Properties hibProperties = hibernateProperties();
	    factory.setJpaProperties(hibProperties);

	    factory.getJpaPropertyMap().put("hibernate.current_session_context_class", "org.springframework.orm.hibernate4.SpringSessionContext");
	    
	    factory.afterPropertiesSet();

	    return factory.getObject();
	  }
	
	
	@Bean(name = "squashtest.tm.persistence.hibernate.SessionFactory")
	//@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	@DependsOn("entityManagerFactory")
	public SessionFactory sessionFactory(){
		HibernateJpaSessionFactoryBean fbean = new HibernateJpaSessionFactoryBean();
		fbean.setEntityManagerFactory(entityManagerFactory());
		return fbean.getObject();
	}

	
	@Bean
	//@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	@DependsOn("squashtest.tm.persistence.hibernate.SessionFactory")
	public HibernateTransactionManager transactionManager() {
		HibernateTransactionManager hibernateTransactionManager = new HibernateTransactionManager(sessionFactory());
		// Below is useful to be able to perform direct JDBC operations using this same tx mgr.
		hibernateTransactionManager.setDataSource(dataSource);
		return hibernateTransactionManager;
	}

	/**
	 * TODO nosgi that's kind of ugly.. find something better
	 *
	 * @return
	 */
	@Bean
	public Properties hibernateProperties() {
		Set<String> names = new HashSet<>();

		for (PropertySource ps : env.getPropertySources()) {
			if (ps instanceof EnumerablePropertySource) {
				for (String name : ((EnumerablePropertySource) ps).getPropertyNames()) {
					if (name.toLowerCase().startsWith("hibernate")) {
						names.add(name);
						// Don't directly get the property because in case of duplicate props, it would short-circuit
						// property priority, which is managed by Environment object
					}
				}
			}
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Filtering hibernate properties from environment : {}", names);
		}

		Properties props = new Properties();

		for (String name : names) {
			props.put(name, env.getProperty(name));
		}


		return props;
	}

	@Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static ValidatorFactory validatorFactory() {
		return new LocalValidatorFactoryBean();
	}

	@Override	
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return transactionManager();
	}
}
