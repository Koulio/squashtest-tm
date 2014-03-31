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

package org.squashtest.tm.service.internal.batchimport.excel;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Implementation of {@link CellValueCoercer} which tests the cell's type then invokes a specific method. Default
 * implementation of each method throw a {@link CannotCoerceException} and are meant to be overriden in subclasses.
 * 
 * @author Gregory Fouquet
 * 
 */
public abstract class TypeBasedCellValueCoercer<VAL> implements CellValueCoercer<VAL> {

	protected TypeBasedCellValueCoercer() {
		super();
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.CellValueCoercer#coerce(org.apache.poi.ss.usermodel.Cell)
	 */
	@Override
	public final VAL coerce(Cell cell) throws CannotCoerceException {
		int type = cell.getCellType();
		VAL res;

		switch (type) {
		case Cell.CELL_TYPE_BLANK:
			res = coerceBlankCell(cell);
			break;
			
		case Cell.CELL_TYPE_NUMERIC:
			res = coerceNumericCell(cell);
			break;

		case Cell.CELL_TYPE_STRING:
			res = coerceStringCell(cell);
			break;

		case Cell.CELL_TYPE_BOOLEAN:
			res = coerceBooleanCell(cell);
			break;

		case Cell.CELL_TYPE_FORMULA:
			res = coerceFormulaCell(cell);
			break;
			
		case Cell.CELL_TYPE_ERROR:
			res = coerceErrorCell(cell);
			break;
		default:
			// we should never get here, ex should be thrown above
			throw new CannotCoerceException("Funky cell type " + type + " is not coercible to a Boolean");
		}

		return res;
	}

	/**
	 * @param cell
	 * @return
	 */
	private VAL coerceErrorCell(Cell cell) {
		throw cannotCoerce("ERROR", cell);
	}

	/**
	 * @param cell
	 * @return
	 */
	protected VAL coerceFormulaCell(Cell cell) {
		throw cannotCoerce("FORMULA", cell);
	}

	/**
	 * @param cell
	 * @return
	 */
	protected VAL coerceBooleanCell(Cell cell) {
		throw cannotCoerce("BOOLEAN", cell);
	}

	/**
	 * @param cell
	 * @return
	 */
	protected VAL coerceBlankCell(Cell cell) {
		throw cannotCoerce("BLANK", cell);
	}

	/**
	 * @param cell
	 * @return
	 */
	protected VAL coerceStringCell(Cell cell) {
		throw cannotCoerce("STRING", cell);
	}

	/**
	 * @param cell
	 * @return
	 */
	protected VAL coerceNumericCell(Cell cell) {
		throw cannotCoerce("NUMERIC", cell);
	}

	private CannotCoerceException cannotCoerce(String type, Cell cell) {
		return new CannotCoerceException("Cannot coerce cell [R," + cell.getRowIndex() + " C" + cell.getColumnIndex()
				+ "] of type " + type);
	}

	/**
	 * Parses a string into an int. When the string is the representation of a floating point number, it is parsed into
	 * the nearest int.
	 * 
	 * @param s
	 * @return
	 * @throws CannotCoerceException
	 */
	protected int liberallyParseInt(String s) throws CannotCoerceException {
		int res;
		try {
			res = Integer.valueOf(s, 10);
		} catch (NumberFormatException e) {
			try {
				res = round(Double.valueOf(s));
			} catch (NumberFormatException ex) {
				throw new CannotCoerceException(ex);
			}
		}
		return res;
	}

	/**
	 * Utility method which rounds a floating point number to the nearest integer.
	 * 
	 * @param val
	 * @return
	 */
	protected int round(double val) {
		return Integer.valueOf((int) Math.round(val));
	}

}