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
package org.squashtest.csp.tm.internal.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang.NullArgumentException;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.CannotMoveNodeException;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.customfield.BoundEntity;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;
import org.squashtest.csp.tm.domain.library.ExportData;
import org.squashtest.csp.tm.domain.library.Folder;
import org.squashtest.csp.tm.domain.library.Library;
import org.squashtest.csp.tm.domain.library.LibraryNode;
import org.squashtest.csp.tm.internal.repository.FolderDao;
import org.squashtest.csp.tm.internal.repository.LibraryDao;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.internal.service.customField.PrivateCustomFieldValueService;
import org.squashtest.csp.tm.internal.utils.library.LibraryUtils;
import org.squashtest.csp.tm.internal.utils.security.PermissionsUtils;
import org.squashtest.csp.tm.internal.utils.security.SecurityCheckableObject;
import org.squashtest.csp.tm.service.LibraryNavigationService;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

/**
 * Generic implementation of a library navigation service.
 * 
 * @author Gregory Fouquet
 * 
 * @param <LIBRARY>
 * @param <FOLDER>
 * @param <NODE>
 */

/*
 * Security Implementation note :
 * 
 * this is sad but we can't use the annotations here. We would need the actual type of the entities we need to check
 * instead of the generics. So we'll call the PermissionEvaluationService explicitly once we've fetched the entities
 * ourselves.
 * 
 * 
 * @author bsiri
 */

/*
 * Note : about methods moving entities from source to destinations :
 * 
 * Basically such operations need to be performed in a precise order, that is : 1) remove the entity from the source
 * collection and 2) insert it in the new one.
 * 
 * However Hibernate performs batch updates in the wrong order, ie it inserts new data before deleting the former ones,
 * thus violating many unique constraints DB side. So we explicitly flush the session between the removal and the
 * insertion.
 * 
 * 
 * @author bsiri
 */

/*
 * Note regarding type safety when calling checkPermission(SecurityCheckableObject...) : see bug at
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6227971
 * 
 * @author bsiri
 */

@Transactional
public abstract class AbstractLibraryNavigationService<LIBRARY extends Library<NODE>, FOLDER extends Folder<NODE>, NODE extends LibraryNode>
		implements LibraryNavigationService<LIBRARY, FOLDER, NODE> {

	private static final String CREATE = "CREATE";
	private static final String READ = "READ";

	/**
	 * token appended to the name of a copy
	 */
	protected static final String COPY_TOKEN = "-Copie";

	private PermissionEvaluationService permissionService;
	
	protected abstract FolderDao<FOLDER, NODE> getFolderDao();

	protected abstract LibraryDao<LIBRARY, NODE> getLibraryDao();

	protected abstract LibraryNodeDao<NODE> getLibraryNodeDao();

	protected abstract NodeDeletionHandler<NODE, FOLDER> getDeletionHandler();
	
	protected abstract PasteStrategy<FOLDER, NODE> getPasteToFolderStrategy();
	
	protected abstract PasteStrategy<LIBRARY, NODE> getPasteToLibraryStrategy();
	
	@Inject
	protected PrivateCustomFieldValueService customFieldValuesService;
	
	//never used really, only for groovy
	void setCustomFieldValueManagerService(PrivateCustomFieldValueService service){
		this.customFieldValuesService = service;
	}
	
	@ServiceReference
	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

	public AbstractLibraryNavigationService() {
		super();
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public final List<NODE> findLibraryRootContent(long libraryId) {
		return getLibraryDao().findAllRootContentById(libraryId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public final List<NODE> findFolderContent(long folderId) {
		return getFolderDao().findAllContentById(folderId);
	}

	@Override
	public final LIBRARY findLibrary(long libraryId) {
		// fetch
		LIBRARY library = getLibraryDao().findById(libraryId);
		// check
		checkPermission(new SecurityCheckableObject(library, READ));
		// proceed
		return library;
	}

	@Override
	public final LIBRARY findCreatableLibrary(long libraryId) {
		// fetch
		LIBRARY library = getLibraryDao().findById(libraryId);
		// check
		checkPermission(new SecurityCheckableObject(library, CREATE));
		// proceed
		return library;
	}

	@Override
	public final FOLDER findFolder(long folderId) {
		// fetch
		FOLDER folder = getFolderDao().findById(folderId);
		// check
		checkPermission(new SecurityCheckableObject(folder, READ));
		// proceed
		return getFolderDao().findById(folderId);
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	@Override
	public final void renameFolder(long folderId, String newName) {
		// fetch
		FOLDER folder = getFolderDao().findById(folderId);
		// check
		checkPermission(new SecurityCheckableObject(folder, "SMALL_EDIT"));

		// proceed
		LIBRARY library = getLibraryDao().findByRootContent((NODE) folder);

		if (library != null) {
			if (!library.isContentNameAvailable(newName)) {
				throw new DuplicateNameException(folder.getName(), newName);
			}
		} else {
			FOLDER parentFolder = getFolderDao().findByContent((NODE) folder);

			if (parentFolder != null && !parentFolder.isContentNameAvailable(newName)) {
				throw new DuplicateNameException(folder.getName(), newName);
			}
		}

		folder.setName(newName);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final void addFolderToLibrary(long destinationId, FOLDER newFolder) {
		// fetch
		LIBRARY container = getLibraryDao().findById(destinationId);
		// check
		checkPermission(new SecurityCheckableObject(container, CREATE));

		// proceed
		container.addContent((NODE) newFolder);
		getFolderDao().persist(newFolder);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final void addFolderToFolder(long destinationId, FOLDER newFolder) {
		// fetch
		FOLDER container = getFolderDao().findById(destinationId);
		// check
		checkPermission(new SecurityCheckableObject(container, CREATE));

		container.addContent((NODE) newFolder);
		getFolderDao().persist(newFolder);

	}

	@Override
	public FOLDER findParentIfExists(LibraryNode node) {
		return getFolderDao().findParentOf(node.getId());
	}

	@Override
	public LIBRARY findLibraryOfRootNodeIfExist(NODE node) {
		return getLibraryDao().findByRootContent(node);
	}
	
	
	// ************************* custom field values *************************

	protected void createCustomFieldValues(BoundEntity entity) {
		customFieldValuesService.createAllCustomFieldValues(entity);
	}
	
	protected void createCustomFieldValues(Collection<? extends BoundEntity> entities){
		for (BoundEntity entity : entities){
			createCustomFieldsValues(entity);
		}
	}
	
	//initialCustomFieldValues maps the id of a CustomField to the value of the corresponding CustomFieldValues for that BoundEntity.
	//read it again until it makes sense.
	//it assumes that the CustomFieldValues instances already exists.
	protected void initCustomFieldValues(BoundEntity entity, Map<Long, String> initialCustomFieldValues){
		
		List<CustomFieldValue> persistentValues = customFieldValuesService.findAllCustomFieldValues(entity);
		
		for (CustomFieldValue value : persistentValues){
			Long customFieldId = value.getCustomField()
									  .getId();
			
			if (initialCustomFieldValues.containsKey(customFieldId)){
				String newValue = initialCustomFieldValues.get(customFieldId);
				value.setValue(newValue);
			}
			
		}
	}
	

	/* ********************** move operations *************************** */

	private void removeFromLibrary(LIBRARY library, NODE node) {
		try {
			library.removeContent(node);
		} catch (NullArgumentException dne) {
			throw new CannotMoveNodeException();
		}
	}

	private void addNodesToLibrary(LIBRARY library, Long[] targetIds) {
		try {
			for (Long id : targetIds) {
				NODE node = getLibraryNodeDao().findById(id);
				library.addContent(node);
			}
		} catch (DuplicateNameException dne) {
			throw new CannotMoveNodeException();
		}
	}

	private void removeFromFolder(FOLDER folder, NODE node) {
		folder.removeContent(node);

	}

	private void addNodesToFolder(FOLDER folder, Long[] targetIds) {
		for (Long id : targetIds) {
			NODE node = getLibraryNodeDao().findById(id);
			folder.addContent(node);
		}
	}

	@Override
	public void modeNodesToFolder(long destinationId, Long[] targetIds) {

		if (targetIds.length == 0) {
			return;
		}

		// fetch
		FOLDER destinationFolder = getFolderDao().findById(destinationId);
		Map<NODE, Object> nodesAndTheirParents = new HashMap<NODE, Object>();

		// security check
		for (Long id : targetIds) {
			NODE node = getLibraryNodeDao().findById(id);
			LIBRARY parentLib = getLibraryDao().findByRootContent(node);

			Object parentObject = (parentLib != null) ? parentLib : getFolderDao().findByContent(node);

			checkPermission(new SecurityCheckableObject(destinationFolder, CREATE), new SecurityCheckableObject(
					parentObject, "DELETE"), new SecurityCheckableObject(node, READ));

			nodesAndTheirParents.put(node, parentObject);

		}
		removeNodesFromTheirParents(nodesAndTheirParents);

		getFolderDao().flush();
		addNodesToFolder(destinationFolder, targetIds);

	}

	@Override
	public void moveNodesToLibrary(long destinationId, Long[] targetIds) {

		if (targetIds.length == 0) {
			return;
		}

		// fetch
		LIBRARY destinationLibrary = getLibraryDao().findById(destinationId);
		Map<NODE, Object> nodesAndTheirParents = new HashMap<NODE, Object>();

		// security check
		for (Long id : targetIds) {
			NODE node = getLibraryNodeDao().findById(id);
			LIBRARY parentLib = getLibraryDao().findByRootContent(node);
			Object parentObject = (parentLib != null) ? parentLib : getFolderDao().findByContent(node);

			checkPermission(new SecurityCheckableObject(destinationLibrary, CREATE), new SecurityCheckableObject(
					parentObject, "DELETE"), new SecurityCheckableObject(node, READ));

			nodesAndTheirParents.put(node, parentObject);
		}

		// proceed
		removeNodesFromTheirParents(nodesAndTheirParents);

		getFolderDao().flush();

		addNodesToLibrary(destinationLibrary, targetIds);
	}

	@SuppressWarnings("unchecked")
	private void removeNodesFromTheirParents(Map<NODE, Object> nodesAndTheirParents) {
		for (Entry<NODE, Object> nodeAndItsParent : nodesAndTheirParents.entrySet()) {
			NODE node = nodeAndItsParent.getKey();
			try {
				LIBRARY parentLib = (LIBRARY) nodeAndItsParent.getValue();
				removeFromLibrary(parentLib, node);
			} catch (Exception e) {
				FOLDER parentFolder = (FOLDER) nodeAndItsParent.getValue();
				removeFromFolder(parentFolder, node);
			}
		}
	}

	/* ********************************* copy operations ****************************** */

	@Override
	public List<NODE> copyNodesToFolder(long destinationId, Long[] sourceNodesIds) {
		return getPasteToFolderStrategy().pasteNodes(destinationId, Arrays.asList(sourceNodesIds));
	}

	@Override
	public List<NODE> copyNodesToLibrary(long destinationId, Long[] targetIds) {
		return getPasteToLibraryStrategy().pasteNodes(destinationId, Arrays.asList(targetIds));
	}

	public int generateUniqueCopyNumber(List<String> copiesNames, String sourceName) {

		return LibraryUtils.generateUniqueCopyNumber(copiesNames, sourceName, COPY_TOKEN);
	}

	@SuppressWarnings("unchecked")
	public FOLDER createCopyFolder(long folderId) {
		FOLDER original = getFolderDao().findById(folderId);
		return (FOLDER) original.createCopy();
	}

	/* ***************************** deletion operations *************************** */

	@Override
	public List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds) {
		return getDeletionHandler().simulateDeletion(targetIds);
	}

	@Override
	public List<Long> deleteNodes(List<Long> targetIds) {

		// check. Note : we wont recursively check for the whole hierarchy as it's supposed to have the same
		// identity holder
		for (Long id : targetIds) {
			NODE node = getLibraryNodeDao().findById(id);
			checkPermission(new SecurityCheckableObject(node, "DELETE"));
		}

		return getDeletionHandler().deleteNodes(targetIds);
	}

	/* ************************* private stuffs ************************* */

	protected void createCustomFieldsValues(BoundEntity entity) {

	}

	protected void checkPermission(SecurityCheckableObject... checkableObjects) {
		PermissionsUtils.checkPermission(permissionService, checkableObjects);
	}

	protected List<? extends ExportData> setFullFolderPath(List<? extends ExportData> dataset) {
		for (ExportData data : dataset) {
			// get folder id
			Long id = data.getFolderId();
			// set the full path attribute
			StringBuilder path = new StringBuilder();

			// if the requirement is not directly located under
			if (!id.equals(ExportData.NO_FOLDER)) {
				for (String name : getLibraryNodeDao().getParentsName(id)) {
					path.append('/' + name);
				}
				path.deleteCharAt(0);
			}
			data.setFolderName(path.toString());
		}
		return dataset;
	}

}