/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
export interface DvSimpleTableColumnDefinition {
    // the name of the column header
    displayedName: string;
    // the name of the attribute, used by the cell
    attributeName: string;
    // we can use this function to manipulate the display value of a cell, similar to a pipe. E.g. for dates, we cannot
    // sort the value by the date representation
    displayFunction?: Function;
}
