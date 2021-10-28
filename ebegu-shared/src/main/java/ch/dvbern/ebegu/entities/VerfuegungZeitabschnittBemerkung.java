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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.envers.Audited;
import org.jetbrains.annotations.NotNull;

/**
 * Dieses Objekt repraesentiert eine Bemerkung eines Zeitabschnitts wahrend eines Betreeungsgutscheinantrags
 */
@Entity
@Audited
public class VerfuegungZeitabschnittBemerkung extends AbstractDateRangedEntity {

	private static final long serialVersionUID = 4621569356897563374L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verfuegung_zeitabschnitt_bemerkung_zeitabschnitt_id"), nullable = false)
	private VerfuegungZeitabschnitt verfuegungZeitabschnitt;

	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	@NotNull
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkung = "";

	public VerfuegungZeitabschnittBemerkung() {
	}

	public VerfuegungZeitabschnittBemerkung(@Nonnull String bemerkung, VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		this.bemerkung = bemerkung;
		this.verfuegungZeitabschnitt = verfuegungZeitabschnitt;
		//Im Moment entspricht die Gültigkeit der Bemerkung immer der Gültigkeit des Zeitabschnittes. Dies wird später (KIBON-2095) geändert
		this.setGueltigkeit(verfuegungZeitabschnitt.getGueltigkeit());
	}

	@SuppressWarnings({ "OverlyComplexBooleanExpression", "OverlyComplexMethod" })
	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}

		//noinspection ConstantConditions: Sonst motzt PMD
		if (!(other instanceof VerfuegungZeitabschnittBemerkung)) {
			return false;
		}
		final VerfuegungZeitabschnittBemerkung that = (VerfuegungZeitabschnittBemerkung) other;
		return StringUtils.equals(this.bemerkung, that.bemerkung) &&
			   Objects.equals(this.verfuegungZeitabschnitt, that.verfuegungZeitabschnitt);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append(super.toString())
			.append("bemerkung", bemerkung)
			.append("verfügungZeitabschnitt", verfuegungZeitabschnitt)
			.toString();
	}

	public VerfuegungZeitabschnitt getVerfuegungZeitabschnitt() {
		return verfuegungZeitabschnitt;
	}

	public void setVerfuegungZeitabschnitt(VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		this.verfuegungZeitabschnitt = verfuegungZeitabschnitt;
	}

	public String getBemerkung() {
		return bemerkung;
	}

	public void setBemerkung(String bemerkungen) {
		this.bemerkung = bemerkungen;
	}
}
