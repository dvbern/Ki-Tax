/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.bridge.builtin.LongBridge;

@Audited
@Entity
public class Dossier extends AbstractEntity {

	private static final long serialVersionUID = -2511152887055775241L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_dossier_fall_id"))
	@IndexedEmbedded
	private Fall fall;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_dossier_gemeinde_id"))
	private Gemeinde gemeinde = null;

	@NotNull
	@Column(nullable = false)
	@Field(bridge = @FieldBridge(impl = LongBridge.class))
	private long dossierNummer = 0;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_dossier_verantwortlicher_bg_id"))
	private Benutzer verantwortlicherBG = null; // Mitarbeiter des JA

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_dossier_verantwortlicher_ts_id"))
	private Benutzer verantwortlicherTS = null; // Mitarbeiter des SCH

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_dossier_verantwortlicher_gmde_id"))
	private Benutzer verantwortlicherGMDE = null; // Mitarbeiter der Gemeinde

	public Fall getFall() {
		return fall;
	}

	public void setFall(Fall fall) {
		this.fall = fall;
	}

	@Nullable
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nullable Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	public long getDossierNummer() {
		return dossierNummer;
	}

	public void setDossierNummer(long dossierNummer) {
		this.dossierNummer = dossierNummer;
	}

	@Nullable
	public Benutzer getVerantwortlicherBG() {
		return verantwortlicherBG;
	}

	public void setVerantwortlicherBG(@Nullable Benutzer verantwortlicherBG) {
		this.verantwortlicherBG = verantwortlicherBG;
	}

	@Nullable
	public Benutzer getVerantwortlicherTS() {
		return verantwortlicherTS;
	}

	public void setVerantwortlicherTS(@Nullable Benutzer verantwortlicherTS) {
		this.verantwortlicherTS = verantwortlicherTS;
	}

	@Nullable
	public Benutzer getVerantwortlicherGMDE() {
		return verantwortlicherGMDE;
	}

	public void setVerantwortlicherGMDE(@Nullable Benutzer verantwortlicherGMDE) {
		this.verantwortlicherGMDE = verantwortlicherGMDE;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Dossier)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		Dossier dossier = (Dossier) other;
		return dossierNummer == dossier.dossierNummer &&
			Objects.equals(fall, dossier.fall) &&
			Objects.equals(gemeinde, dossier.gemeinde);
	}
}
