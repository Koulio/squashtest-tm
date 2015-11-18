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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.internal.repository.CustomCustomReportLibraryNodeDao;

@Repository("CustomCustomReportLibraryNodeDao")
public class HibernateCustomCustomReportLibraryNodeDao extends HibernateEntityDao<CustomReportLibraryNode> implements CustomCustomReportLibraryNodeDao {

	@Override
	public List<TreeLibraryNode> findChildren(Long parentId) {
		CustomReportLibraryNode node = findById(parentId);
		return node.getChildren();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAllDescendantIds(List<Long> nodesIds) {
		Query query = currentSession().getNamedQuery("CustomReportLibraryNodePathEdge.findAllDescendantIds");
		query.setParameterList("ids", nodesIds,LongType.INSTANCE);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomReportLibraryNode> findAllDescendants(List<Long> nodesIds) {
		Query query = currentSession().getNamedQuery("CustomReportLibraryNodePathEdge.findAllDescendant");
		query.setParameterList("ids", nodesIds,LongType.INSTANCE);
		return query.list();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAllFirstLevelDescendantIds(List<Long> nodesIds) {
		Query query = currentSession().getNamedQuery("CustomReportLibraryNodePathEdge.findAllFirstLevelDescendantIds");
		query.setParameterList("ids", nodesIds,LongType.INSTANCE);
		return query.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAncestorIds(Long nodeId) {
		Query query = currentSession().getNamedQuery("CustomReportLibraryNodePathEdge.findAllAncestorIds");
		query.setParameter("id", nodeId);
		return query.list();
	}

	@Override
	public List<Long> findAllFirstLevelDescendantIds(Long nodeId) {
		List<Long> ids = new ArrayList<Long>();
		ids.add(nodeId);
		return findAllFirstLevelDescendantIds(ids);
	}


	
}