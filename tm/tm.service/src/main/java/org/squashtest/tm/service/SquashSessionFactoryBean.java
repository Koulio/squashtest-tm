/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.squashtest.tm.service.internal.hibernate.GroupConcatFunction;
import org.squashtest.tm.service.internal.hibernate.StringAggFunction;

/**
 * Specialization of LocalSessionFactoryBean which registers a "group_concat" hsl function for any known dialect.
 * <p/>
 * Note : I would have inlined this class in #sessionFactory() if I could. But Spring enhances RepositoryConfig in
 * a way that the product of #sessionFactory() is expected to have a null-arg constructor. Yet, the default
 * constructor of an inner / anonymous class is a 1-param ctor which receives the outer instance. This leads to
 * arcane reflection errors (NoSuchMethodException "There is no no-arg ctor") in lines that seem completely unrelated
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
public class SquashSessionFactoryBean extends LocalSessionFactoryBean {
	private static final String FN_NAME_GROUP_CONCAT = "group_concat";

	private static final Logger LOGGER = LoggerFactory.getLogger(SquashSessionFactoryBean.class);

	@Override
	protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
		String dialect = sfb.getProperty("hibernate.dialect");
		sfb.addSqlFunction(FN_NAME_GROUP_CONCAT, groupConcatFunction(dialect));
		return super.buildSessionFactory(sfb);
	}

	/**
	 * Creates an adapter to the underlying DB group_concat (or equivalent) function with a regular syntax which
	 * can be used in hql regardless of the underlaying DB
	 *
	 * @param dialectProp value of the dialect Hibernate property
	 * @return a group concat function which suits the dialect
	 */
	private SQLFunction groupConcatFunction(String dialectProp) {
		String dialect = ConfigurationHelper.resolvePlaceHolder(StringUtils.defaultString(dialectProp)).toLowerCase();

		if (StringUtils.contains(dialect, "postgresql")) {
			return new StringAggFunction(FN_NAME_GROUP_CONCAT, new StringType());
		}
		if (!StringUtils.contains(dialect, "h2") && !StringUtils.contains(dialect, "mysql")) {
			LOGGER.warn("Selected hibernate Dialect '{}' is not known to support the sql function 'group_concat()'. Application will certainly not properly work. Maybe you configured a wrong dialect ?", dialectProp);
		}

		return new GroupConcatFunction(FN_NAME_GROUP_CONCAT, new StringType());
	}

}
