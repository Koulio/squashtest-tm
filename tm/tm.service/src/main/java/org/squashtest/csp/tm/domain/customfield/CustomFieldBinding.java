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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.squashtest.csp.tm.domain.project.GenericProject;
import org.squashtest.csp.tm.domain.project.Project;

/**
 * Defines the binding of a {@link CustomField} to instances of {@link BindableEntity}s contained in a {@link Project}
 * 
 * @author Gregory Fouquet
 * 
 */
@Entity
public class CustomFieldBinding {
	@Id
	@GeneratedValue
	@Column(name = "CFB_ID")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "CF_ID", updatable = false)
	@NotNull
	private CustomField customField;

	@Enumerated(EnumType.STRING)
	@Column(updatable = false)
	private BindableEntity boundEntity;

	@ElementCollection
	@CollectionTable(name = "CUSTOM_FIELD_RENDERING_LOCATION", joinColumns = @JoinColumn(name = "CFB_ID"))
	@Enumerated(EnumType.STRING)
	@Column(name = "RENDERING_LOCATION")
	private Set<RenderingLocation> renderingLocations = new HashSet<RenderingLocation>(5);

	@ManyToOne
	@JoinColumn(name = "BOUND_PROJECT_ID", updatable = false)
	@NotNull
	private GenericProject boundProject;
	
	
	@Column
	private int position=0;
	

	/**
	 * @return the renderingLocations
	 */
	public Set<RenderingLocation> getRenderingLocations() {
		return renderingLocations;
	}

	public CustomField getCustomField() {
		return customField;
	}

	public void setCustomField(CustomField customField) {
		this.customField = customField;
	}

	public BindableEntity getBoundEntity() {
		return boundEntity;
	}

	public void setBoundEntity(BindableEntity boundEntity) {
		this.boundEntity = boundEntity;
	}

	public GenericProject getBoundProject() {
		return boundProject;
	}

	public void setBoundProject(GenericProject boundProject) {
		this.boundProject = boundProject;
	}

	public Long getId() {
		return id;
	}

	public void setRenderingLocations(Set<RenderingLocation> renderingLocations) {
		this.renderingLocations = renderingLocations;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public CustomFieldValue createNewValue(){
		CustomFieldValue value = new CustomFieldValue();
		value.setBinding(this);
		value.setValue(customField.getDefaultValue());
		return value;
	}

	
	
	// ******************* static inner classes, publicly available for once ******************
	
	/**
	 * In case you wonder : NOT THREAD SAFE !
	 * @author bsiri
	 *
	 */
	public static class PositionAwareBindingList extends LinkedList<CustomFieldBinding>{
				
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PositionAwareBindingList(Collection<CustomFieldBinding> items){
			super(items);
		}
		
		public void reorderItems(List<Long> itemIds, int newIndex){
			moveCarelessly(itemIds, newIndex);
			reindex();
		}
		
		private void moveCarelessly(List<Long> ids, int newIndex){
			
			Set<Long> _targetIds = new HashSet<Long>(ids);
			LinkedList<CustomFieldBinding> _removed = new LinkedList<CustomFieldBinding>();
			
			ListIterator<CustomFieldBinding> iterator = this.listIterator();
			
			while (iterator.hasNext()){
				
				CustomFieldBinding binding = iterator.next();
				Long id = binding.getId();
				
				//if the id matches, the current binding moves from the current list to the list of removed binding
				//then remove the id from the target id set
				if (_targetIds.contains(id)){
					_removed.add(binding);
					iterator.remove();
					_targetIds.remove(id);
				}
				
			}
			
			//now we reinsert the removed entities to their new position
			this.addAll(newIndex, _removed);
		}
		
		private void reindex(){
			int position=1;
			for (CustomFieldBinding binding : this){
				binding.setPosition(position++);
			}
		}
		
	}
	
	
}
