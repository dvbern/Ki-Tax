/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class AnmeldungTagesschuleZeitabschnitt extends AbstractDateRangedEntity implements Comparable<AnmeldungTagesschuleZeitabschnitt>{

	private static final long serialVersionUID = -9047857320548372570L;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal massgebendesEinkommenInklAbzugFamgr = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal verpflegungskosten = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal betreuungsstundenProWoche;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal betreuungsminutenProWoche;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal gebuehrProStunde = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal totalKostenProWoche = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private boolean pedagogischBetreut;

	@NotNull @Nonnull
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_anmeldung_tagesschule_id"), nullable = false)
	@ManyToOne(optional = false)
	private AnmeldungTagesschule anmeldungTagesschule;

	@Nonnull
	public BigDecimal getMassgebendesEinkommenInklAbzugFamgr() {
		return massgebendesEinkommenInklAbzugFamgr;
	}

	public void setMassgebendesEinkommenInklAbzugFamgr(@Nonnull BigDecimal massgebendesEinkommenInklAbzugFamgr) {
		this.massgebendesEinkommenInklAbzugFamgr = massgebendesEinkommenInklAbzugFamgr;
	}

	@Nonnull
	public BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(@Nonnull BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}

	@Nonnull
	public BigDecimal getGebuehrProStunde() {
		return gebuehrProStunde;
	}

	public void setGebuehrProStunde(@Nonnull BigDecimal gebuehrProStunde) {
		this.gebuehrProStunde = gebuehrProStunde;
	}

	@Nonnull
	public BigDecimal getTotalKostenProWoche() {
		return totalKostenProWoche;
	}

	public void setTotalKostenProWoche(@Nonnull BigDecimal totalKostenProWoche) {
		this.totalKostenProWoche = totalKostenProWoche;
	}

	public boolean isPedagogischBetreut() {
		return pedagogischBetreut;
	}

	public void setPedagogischBetreut(boolean pedagogischBetreut) {
		this.pedagogischBetreut = pedagogischBetreut;
	}

	@Nonnull
	public AnmeldungTagesschule getAnmeldungTagesschule() {
		return anmeldungTagesschule;
	}

	public void setAnmeldungTagesschule(@Nonnull AnmeldungTagesschule anmeldungTagesschule) {
		this.anmeldungTagesschule = anmeldungTagesschule;
	}

	@Nonnull
	public BigDecimal getBetreuungsstundenProWoche() {
		return betreuungsstundenProWoche;
	}

	public void setBetreuungsstundenProWoche(@Nonnull BigDecimal betreuungsstundenProWoche) {
		this.betreuungsstundenProWoche = betreuungsstundenProWoche;
	}

	@Nonnull
	public BigDecimal getBetreuungsminutenProWoche() {
		return betreuungsminutenProWoche;
	}

	public void setBetreuungsminutenProWoche(@Nonnull BigDecimal betreuungsminutenProWoche) {
		this.betreuungsminutenProWoche = betreuungsminutenProWoche;
	}

	@Override
	public int compareTo(@Nonnull AnmeldungTagesschuleZeitabschnitt other) {
		CompareToBuilder compareToBuilder = new CompareToBuilder();
		compareToBuilder.append(this.getGueltigkeit(), other.getGueltigkeit());
		compareToBuilder.append(this.isPedagogischBetreut(), other.isPedagogischBetreut());
		// wenn ids nicht gleich sind wollen wir auch compare to nicht gleich
		compareToBuilder.append(this.getId(), other.getId());
		return compareToBuilder.toComparison();
	}

	@Nonnull
	public String getBetreuungszeitFormatted() {
		return StringUtils.leftPad(betreuungsstundenProWoche.toString(), 2, '0')
			+ ':' + StringUtils.leftPad(betreuungsminutenProWoche.toString(), 2, '0');
	}
}
