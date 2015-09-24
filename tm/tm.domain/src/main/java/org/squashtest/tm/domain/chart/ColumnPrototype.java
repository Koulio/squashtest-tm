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

import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "CHART_COLUMN_PROTOTYPE")
public class ColumnPrototype {

	@Id
	@javax.persistence.Column(name = "CHART_COLUMN_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_column_id_seq")
	@SequenceGenerator(name = "chart_column_id_seq", sequenceName = "chart_column_id_seq")
	private Long id;

	@NotBlank
	@Size(min = 0, max = 30)
	private String label;

	@Enumerated(EnumType.STRING)
	private EntityType entityType;

	@Enumerated(EnumType.STRING)
	private DataType dataType;

	@CollectionTable(name = "COLUMN_ROLE", joinColumns = @JoinColumn(name = "CHART_COLUMN_ID") )
	@ElementCollection
	@Enumerated(EnumType.STRING)
	private Set<ColumnRole> role;

	public String getLabel() {
		return label;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public DataType getDataType() {
		return dataType;
	}

	public Set<ColumnRole> getRole() {
		return role;
	}

}
