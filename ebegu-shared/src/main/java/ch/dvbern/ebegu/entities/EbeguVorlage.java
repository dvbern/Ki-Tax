/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.EbeguVorlageKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von zeitabhängigen Vorlagen in Ki-Tax
 */
@Audited
@Entity
public class EbeguVorlage extends AbstractDateRangedEntity implements Comparable<EbeguVorlage> {

	private static final long serialVersionUID = 8704632842261673111L;

	@NotNull
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Enumerated(EnumType.STRING)
	private EbeguVorlageKey name;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ebeguvorlage_vorlage_id"), nullable = true)
	private Vorlage vorlage;

	public EbeguVorlage() {
	}

	public EbeguVorlage(EbeguVorlageKey name) {
		this(name, Constants.DEFAULT_GUELTIGKEIT);
	}

	public EbeguVorlage(EbeguVorlageKey name, DateRange gueltigkeit) {
		this.name = name;
		this.setGueltigkeit(gueltigkeit);
	}

	@Nonnull
	public EbeguVorlageKey getName() {
		return name;
	}

	public void setName(@Nonnull EbeguVorlageKey name) {
		this.name = name;
	}

	@Nullable
	public Vorlage getVorlage() {
		return vorlage;
	}

	public void setVorlage(@Nullable Vorlage vorlage) {
		this.vorlage = vorlage;
	}

	/**
	 * @return a copy of the current Param with the gueltigkeit set to the passed DateRange
	 */
	public EbeguVorlage copy(DateRange gueltigkeit) {
		EbeguVorlage copiedParam = new EbeguVorlage();
		copiedParam.setGueltigkeit(new DateRange(gueltigkeit.getGueltigAb(), gueltigkeit.getGueltigBis()));
		copiedParam.setName(this.getName());
		return copiedParam;
	}

	@Override
	public int compareTo(EbeguVorlage o) {
		return this.getName().compareTo(o.getName());
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!super.isSame(other)) {
			return false;
		}
		final EbeguVorlage otherEbeguVorlage = (EbeguVorlage) other;
		return Objects.equals(getName(), otherEbeguVorlage.getName()) &&
			EbeguUtil.isSame(getVorlage(), otherEbeguVorlage.getVorlage());
	}
}
