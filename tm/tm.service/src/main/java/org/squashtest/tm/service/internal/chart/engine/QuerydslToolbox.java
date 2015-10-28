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
package org.squashtest.tm.service.internal.chart.engine;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.chart.ChartQuery;
import org.squashtest.tm.domain.chart.ChartQuery.QueryStrategy;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.ColumnPrototypeInstance;
import org.squashtest.tm.domain.chart.ColumnType;
import org.squashtest.tm.domain.chart.DataType;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.chart.Operation;
import org.squashtest.tm.domain.chart.SpecializedEntityType;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.infolist.InfoListItem;

import com.querydsl.core.JoinExpression;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Ops.AggOps;
import com.querydsl.core.types.Ops.DateTimeOps;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.TemplateExpression;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimpleOperation;
import com.querydsl.jpa.hibernate.HibernateQuery;

class QuerydslToolbox {

	private String subContext;

	private Map<InternalEntityType, String> nondefaultPath = new HashMap<InternalEntityType, String>();

	/**
	 * Default constructor with default context
	 */
	QuerydslToolbox(){
		super();
	}

	/**
	 * Constructor with explicit context name
	 * 
	 * @param subContext
	 */
	QuerydslToolbox(String subContext){
		super();
		this.subContext = subContext;
	}

	/**
	 * Constructor with context name driven by the given column
	 * 
	 * @param column
	 */
	QuerydslToolbox(ColumnPrototypeInstance column ){
		super();
		this.subContext = "subcolumn_"+column.getColumn().getId();
	}

	void setSubContext(String subContext) {
		this.subContext = subContext;
	}

	String getSubContext(){
		return subContext;
	}

	/**
	 * his method will affect the behavior of {@link #getQName(InternalEntityType)} and {@link #getQBean(InternalEntityType)} :
	 * the returned path will use the supplied alias instead of the default ones
	 * 
	 * @param type
	 */
	void forceAlias(InternalEntityType type, String alias){
		nondefaultPath.put(type, alias);
	}

	// ************** info retrievers ***************************

	/**
	 *	The following methods ensure that the entities are aliased appropriately
	 *	according to a context.
	 * 
	 * @param type
	 * @return
	 */
	String getQName(InternalEntityType type){

		EntityPathBase<?> path = type.getQBean();

		String name;

		if (nondefaultPath.containsKey(type)){
			name = nondefaultPath.get(type);
		}
		else if (subContext == null){
			name = path.getMetadata().getName();
		}
		else{
			name = path.getMetadata().getName()+"_"+subContext;
		}

		return name;

	}

	EntityPathBase<?> getQBean(InternalEntityType type){
		String name = getQName(type);
		return type.getAliasedQBean(name);
	}

	EntityPathBase<?> getQBean(SpecializedEntityType domainType){
		InternalEntityType type = InternalEntityType.fromSpecializedType(domainType);
		return getQBean(type);
	}

	EntityPathBase<?> getQBean(ColumnPrototypeInstance column){
		InternalEntityType type = InternalEntityType.fromSpecializedType(column.getSpecializedType());
		return getQBean(type);
	}

	String getAlias(EntityPathBase<?> path){
		return path.getMetadata().getName();
	}


	/**
	 * Returns the aliases registered in the "from" clause of
	 * the given query
	 * 
	 * @param query
	 * @return
	 */
	Set<String> getJoinedAliases(HibernateQuery<?> query){
		AliasCollector collector = new AliasCollector();
		for (JoinExpression join : query.getMetadata().getJoins()){
			join.getTarget().accept(collector, collector.getAliases());
		}
		return collector.getAliases();
	}


	boolean isAggregate(Operation operation){
		boolean res = false;
		switch(operation){
		case COUNT :
		case SUM :
			res = true;
			break;
		default :
			res = false;
			break;
		}
		return res;
	}

	/**
	 * Tells whether the given filter is part of a where clause - or a having component
	 * 
	 * @param filter
	 * @return
	 */
	/*
	 * technically a filter is a 'having' component only if :
	 * 
	 * 1 - this this filter applies to a column of of type CALCULATED,
	 * 2 - that happens to have a subquery of strategy INLINED,
	 * 3 - and the measure of that subquery has an aggregate operation
	 * 
	 * Indeed :
	 * 
	 * - if the column is an ATTRIBUTE, per construction the data is scalar (therefore no aggregate)
	 * - if the column is a calculated of subquery, the filter will be handled from within the subquery, then
	 * 	the whole subquery will be converted to a where clause for the outerquery. see #createAsPredicate()
	 * to see how it's done.
	 * - if the column is a custom field, the said custom field is likely a scalar too (unless one day we
	 * want to count how many tags a given cuf taglist contains).
	 * 
	 */
	boolean isWhereClauseComponent(Filter filter){
		ColumnPrototypeInstance column = filter;

		while (column.getColumn().getColumnType() == ColumnType.CALCULATED &&
				subQueryStrategy(column) == QueryStrategy.INLINED
				){
			column = column.getColumn().getSubQuery().getMeasures().get(0);
		}

		return ! isAggregate(column.getOperation());
	}

	boolean isHavingClauseComponent(Filter filter){
		return ! isWhereClauseComponent(filter);
	}

	// ***************************** high level API ***********************


	Expression<?> createAsSelect(ColumnPrototypeInstance col){

		Expression<?> selectElement = null;

		ColumnPrototype proto = col.getColumn();

		switch(proto.getColumnType()){
		case ATTRIBUTE :
			selectElement = createAttributeSelect(col);
			break;

		case CALCULATED :
			selectElement = createSubquerySelect(col);
			break;

		default :
			throw new IllegalArgumentException("columns of column type '"+proto.getColumnType()+"' are not yet supported");
		}

		return selectElement;
	}



	/**
	 * Creates an Expression like 'baseExp' 'operation' 'operand1', 'operand2' ... suitable for a 'where' or 'having' clause.
	 * Note that  the caller is responsible of the usage of this expression - 'where' or 'having'.
	 * 
	 * @param filter
	 * @return
	 */
	BooleanExpression createAsPredicate(Filter filter){
		BooleanExpression predicate = null;

		ColumnPrototype proto = filter.getColumn();

		switch(proto.getColumnType()){
		case ATTRIBUTE :
			predicate = createAttributePredicate(filter);
			break;

		case CALCULATED :
			predicate = createSubqueryPredicate(filter);
			break;

		default :
			throw new IllegalArgumentException("columns of column type '"+proto.getColumnType()+"' are not yet supported");
		}

		return predicate;
	}



	// ********************* low level API *********************


	@SuppressWarnings("rawtypes")
	PathBuilder makePath(InternalEntityType src, InternalEntityType dest, String attribute){

		Class<?> srcClass = src.getEntityClass();
		Class<?> destClass = dest.getEntityClass();
		String srcAlias = getQName(src);

		return new PathBuilder<>(srcClass, srcAlias).get(attribute, destClass);
	}

	@SuppressWarnings("rawtypes")
	PathBuilder makePath(EntityPathBase<?> src, EntityPathBase<?> dest, String attribute){
		Class<?> srcClass = src.getType();
		Class<?> destClass = dest.getType();
		String srcAlias = src.getMetadata().getName();

		return new PathBuilder<>(srcClass, srcAlias).get(attribute, destClass);
	}



	/**
	 * Creates an expression fit for a "select" clause,  for columns of ColumnType = ATTRIBUTE
	 * 
	 * @param column
	 * @return
	 */
	Expression<?> createAttributeSelect(ColumnPrototypeInstance column){
		Expression attribute = attributePath(column);
		Operation operation = column.getOperation();

		if (operation != Operation.NONE){
			attribute = applyOperation(operation, attribute);
		}

		return attribute;

	}


	/**
	 * Creates an expression fit for a "select" clause,  for columns of ColumnType = CALCULATED
	 * 
	 * @param column
	 * @return
	 */
	Expression<?> createSubquerySelect(ColumnPrototypeInstance col) {
		Expression<?> expression = null;

		switch(subQueryStrategy(col)){

		// create a subselect statement
		case SUBQUERY :
			EntityPathBase<?> colBean = getQBean(col);
			SubQueryBuilder qbuilder = createSubquery(col).asSubselectQuery().joinAxesOn(colBean);
			expression = qbuilder.createQuery();
			break;

			// fetches the measure from the subquery
		case INLINED :
			QuerydslToolbox subtoolbox = new QuerydslToolbox(col);
			MeasureColumn submeasure = col.getColumn().getSubQuery().getMeasures().get(0);	// take that Demeter !
			expression = subtoolbox.createAsSelect(submeasure);
			break;


		case MAIN : throw new IllegalArgumentException(
				"Attempted to create a subquery for column '"+col.getColumn().getLabel()+
				"' from what appears to be a main query. " +
				"This is probably due to an ill-inserted entry in the database, please report this to the suppport.");
		}

		return expression;
	}


	/**
	 * Creates an expression fit for a "where" clause,  for columns of ColumnType = ATTRIBUTE
	 * 
	 * @param column
	 * @return
	 */
	BooleanExpression createAttributePredicate(Filter filter){
		DataType datatype = filter.getDataType();
		Operation operation = filter.getOperation();

		// make the expression on which the filter is applied
		Expression<?> attrExpr = attributePath(filter);

		// convert the operands
		List<Expression<?>> valExpr = makeOperands(operation, datatype, filter.getValues());
		Expression<?>[] operands = valExpr.toArray(new Expression[]{});

		return createPredicate(operation, attrExpr, operands);
	}

	/**
	 * Creates an expression fit for a "where" or "having" clause. It's up to the caller to
	 * know what to do with that.
	 * 
	 * @param column
	 * @return
	 */
	BooleanExpression createSubqueryPredicate(Filter filter) {
		BooleanExpression predicate = null;

		switch(subQueryStrategy(filter)){

		// create "where entity.id in (subquery)" expression
		case SUBQUERY :
			//create the subquery
			QueryBuilder qbuilder = createSubquery(filter).asSubwhereQuery().filterMeasureOn(filter);
			Expression<?> subquery = qbuilder.createQuery();

			// now integrate the subquery
			Expression<?> entityIdPath = idPath(filter);

			predicate = Expressions.predicate(Ops.IN, entityIdPath, subquery);
			break;

		case INLINED :
			MeasureColumn submeasure = filter.getColumn().getSubQuery().getMeasures().get(0);	// and take that again !
			QuerydslToolbox subtoolbox = new QuerydslToolbox(filter);	// create a new toolbox configured with a proper subcontext

			//ok, it is semantically sloppy. But for now the produced element is what we need :-S
			Expression<?> subexpr = subtoolbox.createAsSelect(submeasure);

			List<Expression<?>> valExpr = makeOperands(filter.getOperation(), filter.getDataType(), filter.getValues());
			Expression<?>[] operands = valExpr.toArray(new Expression[]{});

			predicate = createPredicate(filter.getOperation(), subexpr, operands);

			break;

		case MAIN :throw new IllegalArgumentException(
				"Attempted to create a subquery for column '"+filter.getColumn().getLabel()+
				"' from what appears to be a main query. " +
				"This is probably due to an ill-inserted entry in the database, please report this to the suppport.");
		}


		return predicate;
	}


	SimpleOperation<?> applyOperation(Operation operation, Expression<?> baseExp, Expression... operands){

		Operator operator = getOperator(operation);

		Expression[] expressions = prepend(baseExp, operands);

		return Expressions.operation(operator.getType(), operator, expressions);

	}

	/**
	 * creates an Expression like 'baseExp' 'operation' 'operand1', 'operand2' ...
	 * 
	 * @param filter
	 * @return
	 */
	BooleanExpression createPredicate(Operation operation, Expression<?> baseExp, Expression... operands){

		Operator operator = getOperator(operation);

		Expression[] expressions = prepend(baseExp, operands);

		return Expressions.predicate(operator, expressions);
	}

	List<Expression<?>> createOperands(Filter filter, Operation operation) {
		DataType type = filter.getDataType();
		List<String> values = filter.getValues();
		return makeOperands(operation, type, values);
	}


	// ******************************* private stuffs *********************


	@SuppressWarnings("rawtypes")
	private PathBuilder makePath(Class<?> srcClass, String srcAlias, Class<?> attributeClass, String attributeAlias){
		return new PathBuilder<>(srcClass, srcAlias).get(attributeAlias, attributeClass);
	}


	/*
	 * should be invoked only on columns of AttributeType = ATTRIBUTE
	 * 
	 */
	private PathBuilder attributePath(ColumnPrototypeInstance column){

		ColumnPrototype prototype = column.getColumn();

		InternalEntityType type = InternalEntityType.fromSpecializedType(column.getSpecializedType());

		String alias = getQName(type);
		Class<?> clazz = type.getClass();
		String attribute = prototype.getAttributeName();
		Class<?> attributeType = classFromDatatype(prototype.getDataType());

		return makePath(clazz, alias, attributeType, attribute);

	}

	// returns the path to the ID of the entity
	private PathBuilder idPath(ColumnPrototypeInstance column){

		ColumnPrototype prototype = column.getColumn();

		InternalEntityType type = InternalEntityType.fromSpecializedType(column.getSpecializedType());

		String alias = getQName(type);
		Class<?> clazz = type.getClass();

		return makePath(clazz, alias, Long.class, "id");
	}



	List<Expression<?>> makeOperands(Operation operation, DataType type, List<String> values) {
		try{

			List<Expression<?>> expressions = new ArrayList<>(values.size());

			for (String val : values) {// NOSONAR that's a fucking switch it's not complex !
				Object operand;
				switch(type){
				case INFO_LIST_ITEM:
				case STRING :
					operand = val;
					break;
				case NUMERIC :
					operand = Long.valueOf(val);
					break;
				case DATE :
					operand = DateUtils.parseIso8601Date(val);
					break;
				case EXECUTION_STATUS:
					operand = ExecutionStatus.valueOf(val);
					break;
				case LEVEL_ENUM :
					operand = LevelEnumHelper.valueOf(val);
					break;
				default : throw new IllegalArgumentException("type '"+type+"' not yet supported");
				}

				expressions.add(Expressions.constant(operand));
			}

			if (operation == Operation.IN) {
				List<Expression<?>> listeExpression = new ArrayList<>(1);
				listeExpression.add(ExpressionUtils.list(Object.class, expressions.toArray(new Expression[] {})));
				return listeExpression;
			}
			return expressions;

		}catch(ParseException ex){
			throw new RuntimeException(ex);
		}
	}


	private SubQueryBuilder createSubquery(ColumnPrototypeInstance col){
		ColumnPrototype prototype = col.getColumn();
		ChartQuery queryDef = prototype.getSubQuery();
		DetailedChartQuery detailedDef = new DetailedChartQuery(queryDef);

		return new SubQueryBuilder(detailedDef);
	}


	private Operator getOperator(Operation operation){
		Operator operator;

		switch (operation) {// NOSONAR that's a fucking switch it's not complex !
		case EQUALS : operator = Ops.EQ; break;
		case LIKE : operator = Ops.LIKE; break;
		case BY_YEAR : operator = DateTimeOps.YEAR; break;
		case BY_MONTH : operator = DateTimeOps.YEAR_MONTH; break;
		case COUNT : operator = AggOps.COUNT_DISTINCT_AGG; break;
		case SUM : operator = AggOps.SUM_AGG; break;
		case GREATER : operator = Ops.GT; break;
		case IN : operator = Ops.IN; break;
		case BETWEEN: operator = Ops.BETWEEN; break;
		case AVG: operator = AggOps.AVG_AGG; break;
		case BY_DAY: operator = DateTimeOps.DAY_OF_YEAR; break;
		case GREATER_EQUAL: operator = Ops.GOE; break;
		case LOWER: operator = Ops.LT; break;
		case LOWER_EQUAL: operator = Ops.LOE; break;
		case MAX: operator = AggOps.MAX_AGG; break;
		case MIN: operator = AggOps.MIN_AGG; break;
		default : throw new IllegalArgumentException("Operation '"+operation+"' not yet supported");
		}

		return operator;
	}

	private Expression[] prepend(Expression head, Expression... tail){
		Expression[] res = new Expression[tail.length+1];
		res[0] = head;
		System.arraycopy(tail, 0, res, 1, tail.length);
		return res;
	}


	private Class<?> classFromDatatype(DataType type){
		Class<?> result;

		switch(type){
		case DATE : result = Date.class; break;
		case STRING : result = String.class; break;
		case NUMERIC : result = Long.class; break;
		case EXECUTION_STATUS : result = ExecutionStatus.class; break;
		case INFO_LIST_ITEM : result = InfoListItem.class; break;
		case LEVEL_ENUM:
			result = Level.class;
			break;

		default : throw new IllegalArgumentException("datatype '"+type+"' is not yet supported");
		}

		return result;
	}


	// warning : should be called on columns that have a ColumnType = CALCULATED only
	private QueryStrategy subQueryStrategy(ColumnPrototypeInstance col){
		ColumnPrototype proto = col.getColumn();
		if (proto.getColumnType() != ColumnType.CALCULATED){
			throw new IllegalArgumentException("column '"+proto.getLabel()+"' has a column type of '"+proto.getColumnType()+"', therefore it has no subquery");
		}
		return proto.getSubQuery().getStrategy();
	}


	private static final class AliasCollector implements Visitor<Void, Set<String>>{

		private Set<String> aliases = new HashSet<>();


		@Override
		public Void visit(Constant<?> expr, Set<String> context) {
			return null;
		}

		@Override
		public Void visit(FactoryExpression<?> expr, Set<String> context) {
			return null;
		}

		@Override
		public Void visit(com.querydsl.core.types.Operation<?> expr, Set<String> context) {
			for (Expression<?> subexpr : expr.getArgs()){
				subexpr.accept(this, context);
			}
			return null;
		}

		@Override
		public Void visit(ParamExpression<?> expr, Set<String> context) {
			return null;
		}

		@Override
		public Void visit(Path<?> expr, Set<String> context) {
			PathMetadata metadata = expr.getMetadata();
			if (metadata.isRoot()){
				context.add(expr.getMetadata().getName());
			}
			else{
				metadata.getParent().accept(this, context);
			}

			return null;
		}

		@Override
		public Void visit(SubQueryExpression<?> expr, Set<String> context) {
			return null;
		}

		@Override
		public Void visit(TemplateExpression<?> expr, Set<String> context) {
			return null;
		}

		Set<String> getAliases(){
			return aliases;
		}

	}

}
