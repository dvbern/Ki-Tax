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
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.dto.suchfilter.lucene.EBEGUGermanAnalyzer;
import ch.dvbern.ebegu.dto.suchfilter.lucene.Searchable;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.validators.CheckPensumFachstelle;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.annotations.SortNatural;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * Container-Entity fuer die Kinder: Diese muss für jeden Benutzertyp (GS, JA) einzeln gefuehrt werden,
 * damit die Veraenderungen / Korrekturen angezeigt werden koennen.
 */
@CheckPensumFachstelle
@Audited
@Entity
@Table(
	uniqueConstraints = @UniqueConstraint(columnNames = { "kindNummer", "gesuch_id" }, name = "UK_kindcontainer_gesuch_kind_nummer")
)
@Indexed
@Analyzer(impl = EBEGUGermanAnalyzer.class)
public class KindContainer extends AbstractMutableEntity implements Comparable<KindContainer>, Searchable {

	private static final long serialVersionUID = -6784985260190035840L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_kind_container_gesuch_id"), nullable = false)
	private Gesuch gesuch;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_kind_container_kindgs_id"), nullable = true)
	private Kind kindGS;

	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_kind_container_kindja_id"), nullable = true)
	@IndexedEmbedded
	private Kind kindJA;

	@NotNull
	@Min(1)
	@Column(nullable = false)
	private Integer kindNummer = -1; // by default ungueltig, sodass wir wissen wann es neu ist

	/**
	 * nextNumberBetreuung ist die Nummer, die die naechste Betreuung bekommen wird. Aus diesem Grund ist es by default 1
	 * Dieses Feld darf nicht mit der Anzahl der Betreuungen verwechselt werden, da sie sehr unterschiedlich sein koennen falls mehrere Betreuungen geloescht wurden
	 */
	@NotNull
	@Min(1)
	@Column(nullable = false)
	private Integer nextNumberBetreuung = 1;

	@Nonnull
	@Valid
	@SortNatural
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "kind")
	private Set<Betreuung> betreuungen = new TreeSet<>();

	@Column(nullable = true)
	@Nullable
	private Boolean kindMutiert;

	public KindContainer() {
	}

	public Gesuch getGesuch() {
		return gesuch;
	}

	public void setGesuch(Gesuch gesuch) {
		this.gesuch = gesuch;
	}

	@Nullable
	public Kind getKindGS() {
		return kindGS;
	}

	public void setKindGS(@Nullable Kind kindGS) {
		this.kindGS = kindGS;
	}

	public Kind getKindJA() {
		return kindJA;
	}

	public void setKindJA(Kind kindJA) {
		this.kindJA = kindJA;
	}

	public Integer getKindNummer() {
		return kindNummer;
	}

	public void setKindNummer(Integer kindNummer) {
		this.kindNummer = kindNummer;
	}

	@Nonnull
	public Integer getNextNumberBetreuung() {
		return nextNumberBetreuung;
	}

	public void setNextNumberBetreuung(@Nonnull Integer nextNumberBetreuung) {
		this.nextNumberBetreuung = nextNumberBetreuung;
	}

	@Nonnull
	public Set<Betreuung> getBetreuungen() {
		return betreuungen;
	}

	public void setBetreuungen(@Nonnull Set<Betreuung> betreuungen) {
		this.betreuungen = betreuungen;
	}

	@Nullable
	public Boolean getKindMutiert() {
		return kindMutiert;
	}

	public void setKindMutiert(@Nullable Boolean kindMutiert) {
		this.kindMutiert = kindMutiert;
	}

	@Override
	public int compareTo(@Nonnull KindContainer other) {
		CompareToBuilder compareToBuilder = new CompareToBuilder();
		compareToBuilder.append(this.getKindNummer(), other.getKindNummer());
		compareToBuilder.append(this.getId(), other.getId());
		return compareToBuilder.toComparison();
	}

	@Nonnull
	public KindContainer copyKindContainer(@Nonnull KindContainer target, @Nonnull AntragCopyType copyType, @Nonnull Gesuch targetGesuch,
			@Nonnull Gesuchsperiode gesuchsperiode) {
		super.copyAbstractEntity(target, copyType);
		target.setGesuch(targetGesuch);
		target.setKindGS(null);
		target.setKindNummer(this.getKindNummer());
		target.setKindMutiert(null);
		target.setKindJA(this.getKindJA().copyKind(new Kind(), copyType, gesuchsperiode));

		switch (copyType) {
		case MUTATION:
			target.setNextNumberBetreuung(this.getNextNumberBetreuung());
			copyBetreuungen(target, copyType, targetGesuch);
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			target.setNextNumberBetreuung(1); // Betreuungen werden wieder ab 1 durchnummeriert
			break;
		}
		return target;
	}

	private void copyBetreuungen(@Nonnull KindContainer target, @Nonnull AntragCopyType copyType, @Nonnull Gesuch targetGesuch) {
		target.setBetreuungen(new TreeSet<>());
		for (Betreuung betreuung : this.getBetreuungen()) {
			target.getBetreuungen().add(betreuung.copyBetreuung(new Betreuung(), copyType, target, targetGesuch.getEingangsart()));
		}
	}

	@Nonnull
	@Override
	public String getSearchResultId() {
		return this.getId();
	}

	@Nonnull
	@Override
	public String getSearchResultSummary() {
		if (getKindJA() != null) {
			return getKindJA().getFullName();
		}
		return "-";
	}

	@Nullable
	@Override
	public String getSearchResultAdditionalInformation() {
		return this.toString();
	}

	@Override
	public String getOwningGesuchId() {
		return getGesuch().getId();
	}

	@Override
	public String getOwningFallId() {
		return getGesuch().getFall().getId();
	}

	@Nullable
	@Override
	public String getOwningDossierId() {
		return getGesuch().getDossier().getId();
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof KindContainer)) {
			return false;
		}
		final KindContainer otherKindContainer = (KindContainer) other;
		return EbeguUtil.isSameObject(getKindJA(), otherKindContainer.getKindJA()) &&
			Objects.equals(getKindNummer(), otherKindContainer.getKindNummer());
	}
}
