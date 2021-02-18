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

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Displayable;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.EinladungTyp;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This class contains all required objects fro an Einladung.
 */
public class Einladung {

	@Nonnull
	private final EinladungTyp einladungTyp;

	@Nonnull
	private final Benutzer eingeladener;

	@Nullable
	private final Displayable associatedEntity;

	private Einladung(@Nonnull EinladungTyp einladungTyp, @Nonnull Benutzer eingeladener) {
		this.einladungTyp = einladungTyp;
		this.eingeladener = eingeladener;
		this.associatedEntity = null;
	}

	private Einladung(
		@Nonnull EinladungTyp einladungTyp,
		@Nonnull Benutzer eingeladener,
		@Nullable Displayable associatedEntity) {
		checkArgument(einladungTyp.getAssociatedEntityClass()
			.map(clazz -> clazz.isInstance(associatedEntity))
			.orElseGet(() -> associatedEntity == null)
		);
		this.einladungTyp = einladungTyp;
		this.eingeladener = eingeladener;
		this.associatedEntity = associatedEntity;
	}

	public static Einladung forRolle(@Nonnull Benutzer eingeladener) {
		switch (eingeladener.getRole()) {

		case SUPER_ADMIN:
		case ADMIN_MANDANT:
		case SACHBEARBEITER_MANDANT:
		case SACHBEARBEITER_INSTITUTION:
		case SACHBEARBEITER_TRAEGERSCHAFT:
		case SACHBEARBEITER_GEMEINDE:
		case ADMIN_BG:
		case SACHBEARBEITER_BG:
		case ADMIN_TS:
		case SACHBEARBEITER_TS:
		case JURIST:
		case REVISOR:
		case STEUERAMT:
			return new Einladung(EinladungTyp.MITARBEITER, eingeladener);
		case ADMIN_GEMEINDE:
			return new Einladung(
				EinladungTyp.GEMEINDE,
				eingeladener,
				eingeladener.getCurrentBerechtigung().getGemeindeList().iterator().next());
		case ADMIN_TRAEGERSCHAFT:
			return new Einladung(EinladungTyp.TRAEGERSCHAFT, eingeladener, eingeladener.getTraegerschaft());
		case ADMIN_INSTITUTION:
			return new Einladung(EinladungTyp.INSTITUTION, eingeladener, eingeladener.getInstitution());
		case GESUCHSTELLER:
			throw new IllegalArgumentException();
		default:
			throw new IllegalArgumentException();
		}
	}

	@Nonnull
	public static Einladung forMitarbeiter(@Nonnull Benutzer eingeladener) {
		return new Einladung(EinladungTyp.MITARBEITER, eingeladener);
	}

	@Nonnull
	public static Einladung forGemeinde(@Nonnull Benutzer eingeladener, @Nonnull Gemeinde gemeinde) {
		return new Einladung(EinladungTyp.GEMEINDE, eingeladener, gemeinde);
	}

	@Nonnull
	public static Einladung forInstitution(@Nonnull Benutzer eingeladener, @Nonnull Institution institution) {
		return new Einladung(EinladungTyp.INSTITUTION, eingeladener, institution);
	}

	@Nonnull
	public static Einladung forTraegerschaft(@Nonnull Benutzer eingeladener, @Nonnull Traegerschaft traegerschaft) {
		return new Einladung(EinladungTyp.TRAEGERSCHAFT, eingeladener, traegerschaft);
	}

	@Nonnull
	public static Einladung forSozialdienst(@Nonnull Benutzer eingeladener, @Nonnull Sozialdienst sozialdienst) {
		return new Einladung(EinladungTyp.SOZIALDIENST, eingeladener, sozialdienst);
	}

	@Nonnull
	public EinladungTyp getEinladungTyp() {
		return einladungTyp;
	}

	@Nonnull
	public Benutzer getEingeladener() {
		return eingeladener;
	}

	@Nonnull
	public Optional<String> getEinladungRelatedObjectId() {
		return Optional.ofNullable(associatedEntity)
			.map(Displayable::getId);
	}

	@Nonnull
	public Optional<String> getEinladungObjectName() {
		return Optional.ofNullable(associatedEntity)
			.map(Displayable::getName);
	}
}
