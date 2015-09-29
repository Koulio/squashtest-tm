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
package org.squashtest.tm.domain.chart;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.users.User;

@Entity
@Table(name = "CHART_DEFINITION")
public class ChartDefinition {

	@Id
	@Column(name = "CHART_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_def_id_seq")
	@SequenceGenerator(name = "chart_def_id_seq", sequenceName = "chart_def_id_seq")
	private long Id;

	@NotBlank
	@Size(min = 0, max = 30)
	private String name;

	@JoinColumn(name = "USER_ID")
	@ManyToOne
	private User owner;

	@Enumerated(EnumType.STRING)
	private Visibility visibility;

	@Enumerated(EnumType.STRING)
	private ChartType type;


	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
	private String description;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "CHART_FILTER", joinColumns = @JoinColumn(name = "CHART_ID") , inverseJoinColumns = @JoinColumn(name = "FILTER_ID") )
	private List<Filter> filters;

	@ElementCollection
	@CollectionTable(name = "CHART_AXIS_COLUMN", joinColumns = @JoinColumn(name = "CHART_ID") )
	@OrderColumn(name = "RANK")
	private List<AxisColumn> axis;

	@ElementCollection
	@JoinTable(name = "CHART_MEASURE_COLUMN", joinColumns = @JoinColumn(name = "CHART_ID") )
	@OrderColumn(name = "RANK")
	private List<MeasureColumn> measures;

	public List<Filter> getFilters() {
		return filters;
	}

	public List<AxisColumn> getAxis() {
		return axis;
	}

	public List<MeasureColumn> getMeasures() {
		return measures;
	}


	/**
	 * Returns which entities are covered by this chart, sorted by roles
	 *
	 * @return
	 */
	public Map<ColumnRole, Set<EntityType>> getInvolvedEntities(){

		Map<ColumnRole, Set<EntityType>> result = new HashMap<ColumnRole, Set<EntityType>>(3);

		if (! filters.isEmpty()){
			Set<EntityType> filterTypes = collectTypes(filters);
			result.put(ColumnRole.FILTER, filterTypes);
		}

		if (! axis.isEmpty()){
			Set<EntityType> axisTypes = collectTypes(axis);
			result.put(ColumnRole.AXIS, axisTypes);
		}

		if (! measures.isEmpty()){
			Set<EntityType> measureTypes = collectTypes(measures);
			result.put(ColumnRole.MEASURE, measureTypes);
		}

		return result;

	}

	private Set<EntityType> collectTypes(Collection<? extends ColumnPrototypeInstance> columns){
		Set<EntityType> types = new HashSet<>();
		for (ColumnPrototypeInstance col : columns){
			types.add(col.getEntityType());
		}
		return types;
	}
























}
