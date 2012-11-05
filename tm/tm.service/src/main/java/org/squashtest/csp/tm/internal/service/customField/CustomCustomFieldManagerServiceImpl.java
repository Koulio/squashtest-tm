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
package org.squashtest.csp.tm.internal.service.customField;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.CustomFieldOption;
import org.squashtest.csp.tm.domain.customfield.SingleSelectField;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.CustomFieldDao;
import org.squashtest.csp.tm.internal.repository.CustomFieldDeletionDao;
import org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService;

/**
 * Implementations for (non dynamically generated) custom-field management services.
 * @author mpagnon
 * 
 */
@Service("CustomCustomFieldManagerService")
public class CustomCustomFieldManagerServiceImpl implements CustomCustomFieldManagerService {

	@Inject
	private CustomFieldDao customFieldDao;

	@Override
	public List<CustomField> findAllOrderedByName() {
		return customFieldDao.finAllOrderedByName();
	}

	@Override
	public FilteredCollectionHolder<List<CustomField>> findSortedCustomFields(CollectionSorting filter) {
		List<CustomField> customFields = customFieldDao.findSortedCustomFields(filter);
		long count = customFieldDao.countCustomFields();
		return new FilteredCollectionHolder<List<CustomField>>(count, customFields);
	}

	@Override
	public void deleteCustomField(long customFieldId) {
		CustomField customField = customFieldDao.findById(customFieldId);
		customFieldDao.remove(customField);

	}

	/**
	 * @see org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService#persist(org.squashtest.csp.tm.domain.customfield.CustomField)
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void persist(CustomField newCustomField) {
		CustomField cf = customFieldDao.findByName(newCustomField.getName());

		if (cf != null) {
			throw new NameAlreadyInUseException("CustomField", newCustomField.getName());
		} else {
			customFieldDao.persist(newCustomField);
		}
	}

	@Override
	public void changeName(long customFieldId, String newName) {
		CustomField customField = customFieldDao.findById(customFieldId);
		String oldName = customField.getName();
		if(customFieldDao.findByName(newName) != null){
			throw new DuplicateNameException(oldName, newName);
		}else{
			customField.setName(newName);
		}
		
	}

	@Override
	public void changeOptional(Long customFieldId, Boolean optional) {
		if(!optional){
			//TODO add all necessary customFieldValues
		}
		CustomField customField = customFieldDao.findById(customFieldId);
		customField.setOptional(optional);		
	}

	@Override
	public void changeOptionLabel(Long customFieldId, String optionLabel, String newLabel) {
			SingleSelectField customField = customFieldDao.findSingleSelectFieldById(customFieldId);
			customField.changeOption(optionLabel, newLabel);
	}

	@Override
	public void addOption(Long customFieldId, String label) {
		SingleSelectField customField = customFieldDao.findSingleSelectFieldById(customFieldId);
		customField.addOption(label);
		
	}

	@Override
	public SingleSelectField findSingleSelectFieldById(Long customFieldId) {
		return customFieldDao.findSingleSelectFieldById(customFieldId);
	}

	@Override
	public void removeOption(long customFieldId, String optionLabel) {
		SingleSelectField customField = customFieldDao.findSingleSelectFieldById(customFieldId);
		customField.removeOption(optionLabel);		
	}

	
}
