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

package ch.dvbern.ebegu.dto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gemeinde;

public class ZahlungenSearchParamsDTO {
	@Nonnull Integer page;
	@Nonnull Integer pageSize;
	@Nullable Gemeinde gemeinde;
	@Nullable String sortPredicate;
	@Nullable Boolean sortReverse;

	public ZahlungenSearchParamsDTO(@Nonnull Integer page, @Nonnull Integer pageSize) {
		this.page = page;
		this.pageSize = pageSize;
	}

	@Nonnull
	public Integer getPage() {
		return page;
	}

	public void setPage(@Nonnull Integer page) {
		this.page = page;
	}

	@Nonnull
	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(@Nonnull Integer pageSize) {
		this.pageSize = pageSize;
	}

	@Nullable
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nullable Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nullable
	public String getSortPredicate() {
		return sortPredicate;
	}

	public void setSortPredicate(@Nullable String sortPredicate) {
		this.sortPredicate = sortPredicate;
	}

	@Nullable
	public Boolean getSortReverse() {
		return sortReverse;
	}

	public void setSortReverse(@Nullable Boolean sortReverse) {
		this.sortReverse = sortReverse;
	}
}
