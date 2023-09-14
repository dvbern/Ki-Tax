/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.Sprache;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import static ch.dvbern.ebegu.util.Constants.TEN_MB;

/**
 * Stammdaten der Gemeinde die nur fuer eine bestimmte Gesuchsperiode gelten
 */
@Audited
@Entity
public class GemeindeStammdatenGesuchsperiode extends AbstractEntity {

	private static final long serialVersionUID = -6627279554105679588L;
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(updatable = false, foreignKey = @ForeignKey(name = "FK_gemeinde_stammdaten_gesuchsperiode_gemeinde_id"))
	private Gemeinde gemeinde;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(updatable = false, foreignKey = @ForeignKey(name = "FK_gemeinde_stammdaten_gesuchsperiode_gesuchsperiode_id"))
	private Gesuchsperiode gesuchsperiode;

	@Nullable
	@Column(nullable = true, length = TEN_MB) // 10 megabytes
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@NotAudited
	private byte[] merkblattAnmeldungTagesschuleDe;

	@Nullable
	@Column(nullable = true, length = TEN_MB) // 10 megabytes
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@NotAudited
	private byte[] merkblattAnmeldungTagesschuleFr;

	@Nullable
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "gemeindeStammdatenGesuchsperiode")
	private List<GemeindeStammdatenGesuchsperiodeFerieninsel> gemeindeStammdatenGesuchsperiodeFerieninsel;

	@Override
	public boolean isSame(AbstractEntity other) {
		return false;
	}

	@Nonnull
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nonnull Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nonnull
	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nullable
	public List<GemeindeStammdatenGesuchsperiodeFerieninsel> getGemeindeStammdatenGesuchsperiodeFerieninseln() {
		return gemeindeStammdatenGesuchsperiodeFerieninsel;
	}

	public void setGemeindeStammdatenGesuchsperiodeFerieninsel(@Nullable List<GemeindeStammdatenGesuchsperiodeFerieninsel> gemeindeStammdatenGesuchsperiodeFerieninsel) {
		this.gemeindeStammdatenGesuchsperiodeFerieninsel = gemeindeStammdatenGesuchsperiodeFerieninsel;
	}

	@Nonnull
	public byte[] getMerkblattAnmeldungTagesschuleDe() {
		if(merkblattAnmeldungTagesschuleDe == null) {
			return EMPTY_BYTE_ARRAY;
		}
		return  Arrays.copyOf(merkblattAnmeldungTagesschuleDe, merkblattAnmeldungTagesschuleDe.length);
	}

	public void setMerkblattAnmeldungTagesschuleDe(@Nullable byte[] merkblattAnmeldungTagesschuleDe) {
		if (merkblattAnmeldungTagesschuleDe == null) {
			this.merkblattAnmeldungTagesschuleDe = null;
		} else {
			this.merkblattAnmeldungTagesschuleDe = Arrays.copyOf(merkblattAnmeldungTagesschuleDe, merkblattAnmeldungTagesschuleDe.length);
		}
	}

	@Nonnull
	public byte[] getMerkblattAnmeldungTagesschuleFr() {
		if(merkblattAnmeldungTagesschuleFr == null) {
			return EMPTY_BYTE_ARRAY;
		}
		return  Arrays.copyOf(merkblattAnmeldungTagesschuleFr, merkblattAnmeldungTagesschuleFr.length);
	}

	public void setMerkblattAnmeldungTagesschuleFr(@Nullable byte[] merkblattAnmeldungTagesschuleFr) {
		if (merkblattAnmeldungTagesschuleFr == null) {
			this.merkblattAnmeldungTagesschuleFr = null;
		} else {
			this.merkblattAnmeldungTagesschuleFr = Arrays.copyOf(merkblattAnmeldungTagesschuleFr, merkblattAnmeldungTagesschuleFr.length);
		}
	}

	/**
	 * Returns the correct VerfuegungErlaeuterung for the given language
	 */
	@Nonnull
	public byte[] getMerkblattAnmeldungTagesschuleWithSprache(
		@Nonnull Sprache sprache
	) {
		switch (sprache) {
		case DEUTSCH:
			return this.getMerkblattAnmeldungTagesschuleDe();
		case FRANZOESISCH:
			return this.getMerkblattAnmeldungTagesschuleFr();
		default:
			return EMPTY_BYTE_ARRAY;
		}
	}

	@Nonnull
	public GemeindeStammdatenGesuchsperiode copyForGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiodeToCreate){
		GemeindeStammdatenGesuchsperiode copy = new GemeindeStammdatenGesuchsperiode();
		copy.setGemeinde(this.gemeinde);
		copy.setMerkblattAnmeldungTagesschuleDe(this.merkblattAnmeldungTagesschuleDe);
		copy.setMerkblattAnmeldungTagesschuleFr(this.merkblattAnmeldungTagesschuleFr);
		copy.setGesuchsperiode(gesuchsperiodeToCreate);
		if (this.getGemeindeStammdatenGesuchsperiodeFerieninseln() != null) {
			final List<GemeindeStammdatenGesuchsperiodeFerieninsel> gpFerieninselStammdaten = new ArrayList<>();
			this.getGemeindeStammdatenGesuchsperiodeFerieninseln().forEach(stammdaten -> {
				gpFerieninselStammdaten.add(stammdaten.copyForGesuchsperiode(copy));
			});
			copy.setGemeindeStammdatenGesuchsperiodeFerieninsel(gpFerieninselStammdaten);
		}
		return copy;
	}
}
