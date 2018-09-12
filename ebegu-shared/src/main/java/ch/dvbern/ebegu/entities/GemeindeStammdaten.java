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

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

@Audited
@Entity
@Table
public class GemeindeStammdaten extends AbstractMutableEntity{

	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_defaultbenutzerbg_id"), nullable = true)
	private Benutzer defaultBenutzerBG;

	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_defaultbenutzerts_id"), nullable = true)
	private Benutzer defaultBenutzerTS;

	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeindestammdaten_gemeinde_id"), nullable = true)
	private Gemeinde gemeinde;

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof GemeindeStammdaten)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		GemeindeStammdaten gemeindeStammdaten = (GemeindeStammdaten) other;
		return Objects.equals(this.getGemeinde(), gemeindeStammdaten.getGemeinde());
	}

	public Benutzer getDefaultBenutzerBG() {
		return defaultBenutzerBG;
	}

	public void setDefaultBenutzerBG(Benutzer defaultBenutzerBG) {
		this.defaultBenutzerBG = defaultBenutzerBG;
	}

	public Benutzer getDefaultBenutzerTS() {
		return defaultBenutzerTS;
	}

	public void setDefaultBenutzerTS(Benutzer defaultBenutzerTS) {
		this.defaultBenutzerTS = defaultBenutzerTS;
	}

	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}
}
