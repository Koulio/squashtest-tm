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
package org.squashtest.tm.web.internal.model.datatable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.domain.milestone.Milestone;

/**
 * Superclass to create builders of {@link DataTableModel} AND NOTHING ELSE !
 * 
 * @author Gregory Fouquet
 * 
 * @param <X>
 */
public abstract class DataTableModelBuilder<X> {
	private long currentIndex = 0;

	public final DataTableModel buildDataModel(PagedCollectionHolder<List<X>> holder, String sEcho) {

		currentIndex = holder.getFirstItemIndex() + 1;
		Collection<X> pagedItems = holder.getPagedItems();
		DataTableModel model = createModelFromItems(sEcho, pagedItems);
		model.displayRowsFromTotalOf(holder.getTotalNumberOfItems());

		return model;

	}

	public final Collection<Object> buildRawModel(PagedCollectionHolder<List<X>> holder) {

		currentIndex = holder.getFirstItemIndex() + 1;
		Collection<X> pagedItems = holder.getPagedItems();
		Collection<Object> model = buildRawModel(pagedItems);

		return model;

	}

	public List<Object> buildRawModel(Collection<X> pagedItems) {
		List<Object> model = new ArrayList<Object>(pagedItems.size());

		for (X item : pagedItems) {
			Object itemData = buildItemData(item);
			model.add(itemData);
			currentIndex++;
		}

		return model;
	}

	public List<Object> buildRawModel(Collection<X> pagedItems, Long projectId) {
		List<Object> model = new ArrayList<Object>(pagedItems.size());

		for (X item : pagedItems) {
			Object itemData = buildItemData(item, projectId);
			model.add(itemData);
			currentIndex++;
		}

		return model;
	}

	public List<Object> buildRawModel(Collection<X> pagedItems, int startIndex, Long projectId) {
		currentIndex = startIndex;
		return buildRawModel(pagedItems, projectId);
	}

	public List<Object> buildRawModel(Collection<X> pagedItems, int startIndex) {
		currentIndex = startIndex;
		return buildRawModel(pagedItems);
	}
	private DataTableModel createModelFromItems(String sEcho, Collection<X> pagedItems) {
		DataTableModel model = new DataTableModel(sEcho);

		for (X item : pagedItems) {
			model.addRow(buildItemData(item));
			currentIndex++;
		}
		return model;
	}

	protected final long getCurrentIndex() {
		return currentIndex;
	}

	protected static String formatUsername(String username) {
		if (username == null || "".equals(username.trim())) {
			return "-";
		}
		return username;
	}
	// NOSONAR:START
	// it is here on purpose
	@SuppressWarnings("unused")
	private void setCurrentIndex(int i) {
		// NOOP - No fiddling with the current index ! Use #getCurrentIndex() in #buildItemData(X item) implementations
		// instead.
	}
	// NOSONAR:END

	protected abstract Object buildItemData(X item);

	protected abstract Object buildItemData(X item, Long projectId);

}
