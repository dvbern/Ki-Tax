/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.einladung;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.EinladungTyp;

import static java.util.Objects.requireNonNull;

/**
 * This class contains all required objects fro an Einladung.
 */
public class Einladung {

	@NotNull
	private EinladungTyp einladungTyp;

	@Nullable
	private Gemeinde gemeinde;

	@Nullable
	private Institution institution;

	@Nullable
	private Traegerschaft traegerschaft;

	public Einladung(
		@Nonnull EinladungTyp einladungTyp,
		@Nullable Gemeinde gemeinde,
		@Nullable Institution institution,
		@Nullable Traegerschaft traegerschaft
	) {
		this.einladungTyp = einladungTyp;
		this.gemeinde = gemeinde;
		this.institution = institution;
		this.traegerschaft = traegerschaft;
		checkValues();
	}

	public EinladungTyp getEinladungTyp() {
		return einladungTyp;
	}

	public void setEinladungTyp(EinladungTyp einladungTyp) {
		this.einladungTyp = einladungTyp;
	}

	@Nullable
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nullable Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nullable
	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(@Nullable Institution institution) {
		this.institution = institution;
	}

	@Nullable
	public Traegerschaft getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable Traegerschaft traegerschaft) {
		this.traegerschaft = traegerschaft;
	}


	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void checkValues() {
		switch (einladungTyp) {
		case GEMEINDE:
			requireNonNull(gemeinde, "For an Einladung of the type Gemeinde a Gemeinde must be set");
			break;
		case TRAEGERSCHAFT:
			requireNonNull(traegerschaft, "For an Einladung of the type Traegerschaft a Traegerschaft must be set");
			break;
		case INSTITUTION:
			requireNonNull(institution, "For an Einladung of the type Institution an Institution must be set");
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Nonnull
	public Optional<String> getEinladungRelatedObjectId() {
		checkValues();
		switch (einladungTyp) {
		case GEMEINDE:
			return Optional.of(gemeinde.getId());
		case TRAEGERSCHAFT:
			return Optional.of(traegerschaft.getId());
		case INSTITUTION:
			return Optional.of(institution.getId());
		default:
			return Optional.empty();
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Nonnull
	public Optional<String> getEinladungObjectName() {
		switch (getEinladungTyp()) {
		case GEMEINDE:
			return Optional.of(gemeinde.getName());
		case TRAEGERSCHAFT:
			return Optional.of(traegerschaft.getName());
		case INSTITUTION:
			return Optional.of(institution.getName());
		default:
			return Optional.empty();
		}
	}
}
