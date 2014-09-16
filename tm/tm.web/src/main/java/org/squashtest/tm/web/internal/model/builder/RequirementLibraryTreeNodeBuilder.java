/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.model.builder;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementLibraryNodeVisitor;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State;

@SuppressWarnings("rawtypes")
@Component
@Scope("prototype")
public class RequirementLibraryTreeNodeBuilder extends LibraryTreeNodeBuilder<RequirementLibraryNode> {
	/**
	 * This visitor is used to populate custom attributes of the {@link JsTreeNode} currently built
	 * 
	 */
	private class CustomAttributesPopulator implements RequirementLibraryNodeVisitor {
		private final JsTreeNode builtNode;

		public CustomAttributesPopulator(JsTreeNode builtNode) {
			super();
			this.builtNode = builtNode;
		}

		/**
		 * 
		 * @see org.squashtest.tm.domain.requirement.RequirementLibraryNodeVisitor#visit(org.squashtest.tm.domain.requirement.RequirementFolder)
		 */
		public void visit(RequirementFolder folder) {
			addFolderAttributes("requirement-folders");
			State state = (folder.hasContent() ? State.closed : State.leaf);
			builtNode.setState(state);

		}

		/**
		 * 
		 * @see org.squashtest.tm.domain.requirement.RequirementLibraryNodeVisitor#visit(org.squashtest.tm.domain.requirement.Requirement)
		 */
		public void visit(Requirement requirement) {
			addLeafAttributes("requirement", "requirements");

			State state = (requirement.hasContent() ? State.closed : State.leaf);
			builtNode.setState(state);
			builtNode.addAttr("category", requirement.getCategory().toString().toLowerCase());

			if (requirement.getReference() != null && requirement.getReference().length() > 0) {
				builtNode.setTitle(requirement.getReference() + " - " + requirement.getName());
				builtNode.addAttr("reference", requirement.getReference());
			} else {
				builtNode.setTitle(requirement.getName());
			}
		}

	}

	@Inject
	public RequirementLibraryTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super(permissionEvaluationService);
	}

	@Override
	protected void addCustomAttributes(RequirementLibraryNode libraryNode, JsTreeNode treeNode) {
		libraryNode.accept(new CustomAttributesPopulator(treeNode));

	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder#doAddChildren(org.squashtest.tm.web.internal.model.jstree.JsTreeNode,
	 *      org.squashtest.tm.domain.Identified)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doAddChildren(JsTreeNode builtNode, RequirementLibraryNode model) {
		NodeContainer<RequirementLibraryNode<?>> container = (NodeContainer<RequirementLibraryNode<?>>) model;

		if (container.hasContent()) {
			builtNode.setState(State.open);
			
			RequirementLibraryTreeNodeBuilder childrenBuilder = new RequirementLibraryTreeNodeBuilder(permissionEvaluationService);
			Collection<RequirementLibraryNode<?>> content = (Collection<RequirementLibraryNode<?>>) container.getOrderedContent();
			
			List<JsTreeNode> children = new JsTreeNodeListBuilder<RequirementLibraryNode<?>>(childrenBuilder)
					.expand(getExpansionCandidates())
					.setModel(content)
					.build();

			builtNode.setChildren(children);
		}
	}


}
