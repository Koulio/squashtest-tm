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
package org.squashtest.tm.domain.jpql;

import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import java.util.List;


// TODO : move in there the group concat functions, and maybe share them with QueryDSL ?

/**
 * <p>
 * 	This class defines some extensions to JPQL language, that must be registered in the HibernateSessionFactory,
 * 	and can also be used in QueryDSL if needed.
 * </p>
 *
 * <h4>group_concat</h4>
 *
 * <p>
 * 	This section factory declares a custom function for the support of group_concat - which is not a JPQL standard function.
 * 	The implementation for this custom function depends on the target database. See GroupConcatFunction for h2 and mysql,
 * 	StringAggFunction for Postgre
 * </p>
 *
 * <p>
 * 	Syntax is as follow :
 *  <ul>
 *  	<li><pre>group_concat(<it>col identifier</it>)</pre>will concatenate as expected over the column identifier.</li>
 *  	<li><pre>group_concat(<it>col identifier 1</it>, 'order by', <it>col identifier 2</it>, ['asc|desc']) </pre>will send this to the target db as 'group_concat(id1 order by id2 [asc|desc])'
 *  </ul>
 * </p>
 *
 * 	<h4>Aggregate function wrappers</h4>
 *
 *  <p>
 * 	The standard aggregate functions : count, sum, avg, min and max, used in a JPA query, cannot apply on subqueries.
 * 	For instance one cannot sum((select id from entity)). The antlr parser used by Hibernate will just dismiss such nodes
 * 	although the would-be SQL would run just fine on the target database.
 * </p>
 *
 * <p>By chance custom sql function can do so. So, by registering the standard aggregate functions as custom functions we
 * can trick Hibernate parser into thinking that the jpa item it reads is a custom function while it will just really print
 * a regular function in the sql output, and so offer us free support for aggregate over subqueries.</p>
 *
 * <p>The function names are :
 *
 * <ul>
 * 	<li>'s_avg',</li>
 * 	<li>'s_sum',</li>
 * 	<li>'s_min',</li>
 * 	<li>'s_max',</li>
 * 	<li>'s_count',</li>
 * </ul>
 *
 * Those wrapper use distinct names so that the native aggregate functions are not overridden : depending on you needs
 * you may use the regular 'count', or the corresponding wrapper 's_count'.</p>
 * @author bsiri
 *
 */
public class SessionFactoryEnhancer {

	public static final String FN_NAME_GROUP_CONCAT = "group_concat";


	public static final String FN_NAME_SUM = "s_sum";
	public static final String FN_NAME_CNT = "s_count";
	public static final String FN_NAME_MIN = "s_min";
	public static final String FN_NAME_MAX = "s_max";
	public static final String FN_NAME_AVG = "s_avg";

	public static enum FnSupport {
		GROUP_CONCAT,
		STR_AGG
	}

	/* ************************************************
	 * API for Hibernate SessionFactory
	 * ************************************************/

	public static void registerExtensions(Configuration hibConfig, FnSupport... fnSupports) {

		// aggregate function wrappers
		hibConfig.addSqlFunction(FN_NAME_SUM, new StandardSQLFunction("sum"));
		hibConfig.addSqlFunction(FN_NAME_MIN, new StandardSQLFunction("min"));
		hibConfig.addSqlFunction(FN_NAME_MAX, new StandardSQLFunction("max"));
		hibConfig.addSqlFunction(FN_NAME_AVG, new StandardSQLFunction("avg"));
		hibConfig.addSqlFunction(FN_NAME_CNT, new SCountDistinctFunction());

		// boolean case when


		// database-specific functions
		for (FnSupport support : fnSupports) {
			switch (support) {
				case GROUP_CONCAT:
					hibConfig.addSqlFunction(FN_NAME_GROUP_CONCAT, new GroupConcatFunction(FN_NAME_GROUP_CONCAT, StringType.INSTANCE));
					break;
				case STR_AGG:
					hibConfig.addSqlFunction(FN_NAME_GROUP_CONCAT, new StringAggFunction(FN_NAME_GROUP_CONCAT, StringType.INSTANCE));
					break;
			}
		}

	}


	/* **************************************************
	 *  Inner classes
	 * *************************************************/

	private static class SCountDistinctFunction extends StandardSQLFunction {

		SCountDistinctFunction() {
			super("count", LongType.INSTANCE);
		}

		@Override
		public final String render(Type firstArgumentType, List arguments, SessionFactoryImplementor sessionFactory) {
			return "count(distinct " + arguments.get(0) + " )";
		}


	}

	/**
	 * <p>This custom implementation of group_concat. Because it can contain an embedded expression, hibernate will try to parse it just as if
	 * it was within the scope of the main query - thus causing parsing exception.</p>
	 *
	 *  <p>To prevent this we had to make the awkward syntax as follow:</p>
	 *
	 *  <ul>
	 *  	<li>group_concat(<it>col identifier</it>) : will concatenate as expected over the column identifier.</li>
	 *  	<li>group_concat(<it>col identifier 1</it>, 'order by', <it>col identifier 2</it>, ['asc|desc']) : will send this to the target db as 'group_concat(id1 order by id2 [asc|desc])'
	 *  </ul>
	 *
	 *
	 *
	 *
	 * @author bsiri
	 *
	 */
	private static class GroupConcatFunction extends StandardSQLFunction {

		GroupConcatFunction(String name, Type registeredType) {
			super(name, registeredType);
		}

		GroupConcatFunction(String name) {
			super(name);
		}

		@Override
		public final String render(Type firstArgumentType, List arguments, SessionFactoryImplementor sessionFactory) {

			if (arguments.size() == 1) {
				return super.render(firstArgumentType, arguments, sessionFactory);
			} else {
				try {
					// validation
					String direction = (arguments.size() >= 4) ? ((String) arguments.get(3)).replaceAll("'", "") : "asc";
					String separator = (arguments.size() >= 5) ? ((String) arguments.get(4)).replaceAll("'", "") : ",";
					if (!(direction.equalsIgnoreCase("asc") || direction.equalsIgnoreCase("desc"))) {
						throw new IllegalArgumentException();
					}
					if (!((String) arguments.get(1)).equalsIgnoreCase("'order by'")) {
						throw new IllegalArgumentException();
					}

					// expression
					return createSqlQuery(arguments, direction, separator);
				} catch (IllegalArgumentException ex) {
					throw new IllegalArgumentException("usage of custom hql group_concat : group_concat(col id, [ 'order by', col id2, ['asc|desc']]", ex);
				}
			}

		}

		protected String createSqlQuery(List<?> arguments, String direction, String separator) {
			return "group_concat(" + arguments.get(0) + " order by " + arguments.get(2) + " " + direction + " separator '" + separator + "')";
		}

	}

	/**
	 * Equivalent of GroupConcatFunction for Postgresql
	 *
	 */
	private static final class StringAggFunction extends GroupConcatFunction {

		StringAggFunction(String name) {
			super(name);
		}

		StringAggFunction(String name, Type registeredType) {
			super(name, registeredType);
		}

		@Override
		public String createSqlQuery(List<?> arguments, String direction, String separator) {
			return "string_agg( cast(" + arguments.get(0) + " as text),'" + separator + "' order by " + arguments.get(2)
				+ " " + direction + ")";
		}
	}


}
