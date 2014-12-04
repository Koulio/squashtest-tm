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
package org.squashtest.tm.web.internal.model.json;

import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.SystemListItem;



public class JsonInfoListItem {

	private long id;
	private String uri;
	private String code;
	private String label;
	private boolean isDefault;
	private boolean isSystem;
	private String iconName;


	public JsonInfoListItem(){
		super();
	}

	public static JsonInfoListItem toJson(InfoListItem item){
		JsonInfoListItem res = new JsonInfoListItem();
		res.id = item.getId();
		res.uri = "todo";
		res.code = item.getCode();
		res.label = item.getLabel();
		res.isDefault = item.isDefault();
		res.iconName = item.getIconName();
		// TODO : something less sloppy once we have time for something better
		res.isSystem = (SystemListItem.class.isAssignableFrom(item.getClass()));
		return res;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean isSystem() {
		return isSystem;
	}

	public void setSystem(boolean isSystem) {
		this.isSystem = isSystem;
	}


}
