/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.domain.customfield;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class CustomFieldValue {

	@Id
	@GeneratedValue
	@Column(name="CFV_ID")
	private Long id;
	
	@Embedded
	private BindableEntityInstance boundEntityInstance;

	@ManyToOne
	@JoinColumn(name="CFB_ID")
	private CustomFieldBinding binding;
		
	private String value;
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public BindableEntityInstance getBoundEntityInstance() {
		return boundEntityInstance;
	}


	public void setBoundEntityInstance(BindableEntityInstance boundEntityInstance) {
		this.boundEntityInstance = boundEntityInstance;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}

	public CustomFieldBinding getBinding() {
		return binding;
	}

	public void setBinding(CustomFieldBinding binding) {
		this.binding = binding;
	}
	
	
	
	
}
