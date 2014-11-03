/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.domain.customfield;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;

@Entity
@DiscriminatorValue("MFV")
public class MultiSelectFieldValue extends CustomFieldValue implements MultiValuedCustomFieldValue {

	@ElementCollection
	@CollectionTable(name = "CUSTOM_FIELD_VALUE_OPTION", joinColumns = @JoinColumn(name = "CFV_ID"))
	@OrderColumn(name = "POSITION")
	private List<CustomFieldValueOption> selectedOptions = new ArrayList<CustomFieldValueOption>();

	public List<CustomFieldValueOption> getOptions() {
		return selectedOptions;
	}

	public void addCUFieldValueOption(CustomFieldValueOption cufVO){
		selectedOptions.add(cufVO);
	}

	public void removeCUFValueOption(CustomFieldValueOption cufVO){
		selectedOptions.remove(cufVO);
	}


	@Override
	public void setValues(List<String> values) {
		selectedOptions.clear();
		for (String option : values){
			selectedOptions.add(new CustomFieldValueOption(option));
		}
	}

	@Override
	public List<String> getValues() {
		List<String> result = new ArrayList<String>(selectedOptions.size());
		for (CustomFieldValueOption option : selectedOptions){
			result.add(option.getLabel());
		}
		return result;
	}



	@Override
	public String getValue(){
		String result = "";
		if (! selectedOptions.isEmpty()){
			StringBuilder builder = new StringBuilder();
			for (CustomFieldValueOption option : selectedOptions){
				builder.append(option.getLabel()+";");
			}
			int lastidx = builder.lastIndexOf(";");
			result = builder.substring(0,lastidx);
		}
		return result;
	}

	/**
	 * Not the preferred way to set the values of this field,
	 * use adCUFieldValueOption when possible.
	 */
	@Deprecated
	public void setValue(String value){
		selectedOptions.clear();
		String[] atoms = value.split(";");
		int i = 0;
		for (String atom : atoms){
			selectedOptions.add(new CustomFieldValueOption(atom));
		}
	}

	@Override
	public CustomFieldValue copy(){
		MultiSelectFieldValue copy = new MultiSelectFieldValue();
		copy.setBinding(getBinding());

		for (CustomFieldValueOption option : selectedOptions){
			copy.addCUFieldValueOption(option.copy());
		}

		return copy;
	}


}
