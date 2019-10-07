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
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.ModulTagesschuleIntervall;
import ch.dvbern.ebegu.enums.ModulTagesschuleName;
import ch.dvbern.ebegu.validators.CheckTimeRange;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

/**
 * Entity for the Module of the Tageschulangebote.
 */
@CheckTimeRange
@Audited
@Entity
public class ModulTagesschuleGroup extends AbstractEntity implements Comparable<ModulTagesschuleGroup> {

	private static final long serialVersionUID = -8403411439182708718L;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_modul_tagesschule_einstellungen_tagesschule_id"), nullable = false)
	private EinstellungenTagesschule einstellungenTagesschule;

	@Enumerated(value = EnumType.STRING)
	@NotNull @Nonnull
	@Column(nullable = false)
	private ModulTagesschuleName modulTagesschuleName = ModulTagesschuleName.DYNAMISCH;

	@NotNull @Nonnull
	@Column(nullable = false)
	private String identifier;

	@NotNull @Nonnull
	@Column(nullable = false)
	private String bezeichnung;

	@NotNull @Nonnull
	@Column(nullable = false)
	private LocalTime zeitVon;

	@NotNull @Nonnull
	@Column(nullable = false)
	private LocalTime zeitBis;

	@Nullable
	@Column(nullable = true)
	private BigDecimal verpflegungskosten;

	@Enumerated(value = EnumType.STRING)
	@NotNull @Nonnull
	@Column(nullable = false)
	private ModulTagesschuleIntervall intervall = ModulTagesschuleIntervall.WOECHENTLICH;

	@NotNull @Nonnull
	@Column(nullable = false)
	private boolean wirdPaedagogischBetreut = false;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Integer reihenfolge;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "modulTagesschuleGroup", fetch = FetchType.LAZY)
	@OrderBy("wochentag")
	private Set<ModulTagesschule> module = new LinkedHashSet<>();


	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof ModulTagesschuleGroup)) {
			return false;
		}
		final ModulTagesschuleGroup otherModulTagesschule = (ModulTagesschuleGroup) other;
		return getModulTagesschuleName() == otherModulTagesschule.getModulTagesschuleName() &&
			Objects.equals(getZeitVon(), otherModulTagesschule.getZeitVon()) &&
			Objects.equals(getZeitBis(), otherModulTagesschule.getZeitBis());
	}

	@Nonnull
	public ModulTagesschuleName getModulTagesschuleName() {
		return modulTagesschuleName;
	}

	public void setModulTagesschuleName(@Nonnull ModulTagesschuleName modulname) {
		this.modulTagesschuleName = modulname;
	}

	@Nonnull
	public LocalTime getZeitVon() {
		return zeitVon;
	}

	public void setZeitVon(@Nonnull LocalTime zeitVon) {
		this.zeitVon = zeitVon;
	}

	@Nonnull
	public LocalTime getZeitBis() {
		return zeitBis;
	}

	public void setZeitBis(@Nonnull LocalTime zeitBis) {
		this.zeitBis = zeitBis;
	}

	public EinstellungenTagesschule getEinstellungenTagesschule() {
		return einstellungenTagesschule;
	}

	public void setEinstellungenTagesschule(EinstellungenTagesschule einstellungenTagesschule) {
		this.einstellungenTagesschule = einstellungenTagesschule;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Nonnull
	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(@Nonnull String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	public BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}

	@Nonnull
	public ModulTagesschuleIntervall getIntervall() {
		return intervall;
	}

	public void setIntervall(@Nonnull ModulTagesschuleIntervall intervall) {
		this.intervall = intervall;
	}

	public boolean isWirdPaedagogischBetreut() {
		return wirdPaedagogischBetreut;
	}

	public void setWirdPaedagogischBetreut(boolean wirdPaedagogischBetreut) {
		this.wirdPaedagogischBetreut = wirdPaedagogischBetreut;
	}

	public Integer getReihenfolge() {
		return reihenfolge;
	}

	public void setReihenfolge(Integer reihenfolge) {
		this.reihenfolge = reihenfolge;
	}

	public Set<ModulTagesschule> getModule() {
		return module;
	}

	public void setModule(Set<ModulTagesschule> module) {
		this.module = module;
	}

	@Override
	public int compareTo(@Nonnull ModulTagesschuleGroup o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getEinstellungenTagesschule(), o.getEinstellungenTagesschule());
		builder.append(this.getIdentifier(), o.getIdentifier());
		builder.append(this.getZeitVon(), o.getZeitVon());
		builder.append(this.getZeitBis(), o.getZeitBis());
		builder.append(this.getModulTagesschuleName(), o.getModulTagesschuleName());
		return builder.toComparison();
	}

	public ModulTagesschuleGroup copyForGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		ModulTagesschuleGroup copy = new ModulTagesschuleGroup();
		copy.setModulTagesschuleName(this.getModulTagesschuleName());
		if (ModulTagesschuleName.DYNAMISCH.equals(this.getModulTagesschuleName())) {
			copy.setIdentifier(UUID.randomUUID().toString());
		} else {
			copy.setIdentifier(this.getModulTagesschuleName().name());
		}
		copy.setBezeichnung(this.getBezeichnung());
		copy.setZeitVon(this.getZeitVon());
		copy.setZeitBis(this.getZeitBis());
		copy.setVerpflegungskosten(this.getVerpflegungskosten());
		copy.setIntervall(this.getIntervall());
		copy.setWirdPaedagogischBetreut(this.isWirdPaedagogischBetreut());
		return copy;
	}
}
