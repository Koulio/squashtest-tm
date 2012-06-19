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
package org.squashtest.csp.tm.web.internal.model.customeditors;

import org.squashtest.csp.core.bugtracker.domain.Category;

public class CategoryPropertyEditorSupport extends BugTrackerPropertyEditorSupport<Category> {

	
	@Override
	public void setValue(Object value) {
		if (value instanceof String) {
			String strObject = (String) value;

			String id = getAttribute("id",strObject);
			String name = getAttribute("name", strObject);
			
			Category entity;
			if (id.equals(Category.NO_CATEGORY.getId())){
				entity=Category.NO_CATEGORY;
			}
			else{
				entity = new Category(id, name);
			}
			
			super.setValue(entity);

			
		}
		else{
			super.setValue(Category.NO_CATEGORY);
		}

	}
	
	@Override
	public String getAsText() {
		Category entity =(Category) getValue();
		return "id="+entity.getId()+",name="+entity.getName();
	}
	
	@Override
	public void setAsText(String text){		
		String strObject = text;

		String id = getAttribute("id",strObject);
		String name = getAttribute("name", strObject);
		
		Category entity = new Category(id, name);
		super.setValue(entity);		
	}
	

	
}
