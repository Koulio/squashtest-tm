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
package org.squashtest.tm.web.internal.model.builder;

import java.util.List;

import javax.inject.Provider;

import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.helper.HyphenedStringHelper;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State;

/**
 * Builds a {@link JsTreeNode} representing a "drive" from a {@link Library}
 * 
 * @author Gregory Fouquet
 * 
 */
public class DriveNodeBuilder<LN extends LibraryNode> extends
GenericJsTreeNodeBuilder<Library<LN>, DriveNodeBuilder<LN>> {

	private final Provider<LibraryTreeNodeBuilder<LN>> childrenBuilderProvider;


	public DriveNodeBuilder(PermissionEvaluationService permissionEvaluationService,
			Provider<LibraryTreeNodeBuilder<LN>> childrenBuilderProvider) {
		super(permissionEvaluationService);
		this.childrenBuilderProvider = childrenBuilderProvider;
	}

	/**
	 * 
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder#doBuild(org.squashtest.tm.web.internal.model.jstree.JsTreeNode,
	 *      org.squashtest.tm.domain.Identified)
	 */
	@Override
	protected JsTreeNode doBuild(JsTreeNode node, Library<LN> model) {
		node.addAttr("rel", "drive");
		node.addAttr("resId", String.valueOf(model.getId()));
		node.addAttr("resType", buildResourceType(model.getClassSimpleName()));
		node.setState(model.hasContent() ? State.closed : State.leaf);
		node.setTitle(model.getProject().getName());
		node.addAttr("name", model.getClassSimpleName());
		node.addAttr("id", model.getClassSimpleName() + '-' + model.getId());
		node.addAttr("title", model.getProject().getLabel());
		node.addAttr("project", model.getProject().getId());
		node.addAttr("wizards", model.getEnabledPlugins());

		// milestone attributes : libraries are yes-men
		node.addAttr("milestone-creatable-deletable", "true");
		node.addAttr("milestone-editable", "true");

		return node;
	}

	private String buildResourceType(String classSimpleName) {
		String singleResourceType = HyphenedStringHelper.camelCaseToHyphened(classSimpleName);
		return singleResourceType.replaceAll("y$", "ies");
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder#doAddChildren(org.squashtest.tm.web.internal.model.jstree.JsTreeNode,
	 *      java.lang.Object)
	 */
	@Override
	protected void doAddChildren(JsTreeNode node, Library<LN> model) {
		if (model.hasContent()) {

			LibraryTreeNodeBuilder<LN> builder = childrenBuilderProvider.get();
			if (milestoneFilter != null){
				builder.filterByMilestone(milestoneFilter);
			}

			List<JsTreeNode> children = new JsTreeNodeListBuilder<LN>(builder)
					.expand(getExpansionCandidates())
					.setModel(model.getOrderedContent())
					.build();

			node.setChildren(children);

			// because of the milestoneFilter it may happen that the children collection ends up empty.
			// in that case we must set the state of the node accordingly
			State state =  (children.isEmpty()) ? State.leaf : State.open;
			node.setState(state);
		}

	}

}
