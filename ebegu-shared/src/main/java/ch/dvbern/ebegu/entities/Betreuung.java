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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.dto.suchfilter.lucene.BGNummerBridge;
import ch.dvbern.ebegu.dto.suchfilter.lucene.EBEGUGermanAnalyzer;
import ch.dvbern.ebegu.dto.suchfilter.lucene.Searchable;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.validationgroups.BetreuungBestaetigenValidationGroup;
import ch.dvbern.ebegu.validators.CheckAbwesenheitDatesOverlapping;
import ch.dvbern.ebegu.validators.CheckBetreuungZeitraumInGesuchsperiode;
import ch.dvbern.ebegu.validators.CheckBetreuungZeitraumInstitutionsStammdatenZeitraum;
import ch.dvbern.ebegu.validators.CheckBetreuungspensum;
import ch.dvbern.ebegu.validators.CheckBetreuungspensumDatesOverlapping;
import ch.dvbern.ebegu.validators.CheckGrundAblehnung;
import org.hibernate.annotations.SortNatural;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Indexed;

/**
 * Entity fuer Betreuungen.
 */
@Audited
@Entity
@CheckGrundAblehnung
@CheckBetreuungspensum
@CheckBetreuungspensumDatesOverlapping
@CheckAbwesenheitDatesOverlapping
@CheckBetreuungZeitraumInGesuchsperiode (groups = BetreuungBestaetigenValidationGroup.class)
@CheckBetreuungZeitraumInstitutionsStammdatenZeitraum (groups = BetreuungBestaetigenValidationGroup.class)
// Der ForeignKey-Name wird leider nicht richtig generiert, muss von Hand angepasst werden!
@AssociationOverrides({
	@AssociationOverride(name = "kind", joinColumns = @JoinColumn(name = "kind_id"), foreignKey = @ForeignKey(name = "FK_betreuung_kind_id")),
	@AssociationOverride(name="institutionStammdaten", joinColumns=@JoinColumn(name="institutionStammdaten_id"), foreignKey = @ForeignKey(name = "FK_betreuung_institution_stammdaten_id"))
})
@Table(
	uniqueConstraints =
	@UniqueConstraint(columnNames = { "betreuungNummer", "kind_id" }, name = "UK_betreuung_kind_betreuung_nummer")
)
@Indexed
@Analyzer(impl = EBEGUGermanAnalyzer.class)
@ClassBridge(name = "bGNummer", impl = BGNummerBridge.class, analyze = Analyze.NO)
public class Betreuung extends AbstractPlatz implements Searchable {

	private static final long serialVersionUID = -6776987863150835840L;

	/**
	 * Contains the VorgaengerVerfuegung that has already been paid. It can be null even in Mutationen if there was no Zahlung zet
	 */
	@Transient
	@Nullable
	private Verfuegung vorgaengerAusbezahlteVerfuegung;

	/**
	 * It will always contain the vorganegerVerfuegung, regardless it has been paid or not
	 */
	@Transient
	@Nullable
	private Verfuegung vorgaengerVerfuegung;

	@Valid
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "betreuung")
	@SortNatural
	private Set<BetreuungspensumContainer> betreuungspensumContainers = new TreeSet<>();

	@NotNull
	@Valid
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "betreuung")
	private ErweiterteBetreuungContainer erweiterteBetreuungContainer = new ErweiterteBetreuungContainer(this);

	@Valid
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "betreuung")
	private Set<AbwesenheitContainer> abwesenheitContainers = new TreeSet<>();

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String grundAblehnung;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.REMOVE,  orphanRemoval = true, mappedBy = "betreuung")
	private Verfuegung verfuegung;

	@NotNull
	@Column(nullable = false)
	private Boolean vertrag = false;

	@Nullable
	@Column(nullable = true)
	private LocalDate datumAblehnung;

	@Nullable
	@Column(nullable = true)
	private LocalDate datumBestaetigung;

	@Nullable
	@Column(nullable = true)
	private Boolean betreuungMutiert;

	@Nullable
	@Column(nullable = true)
	private Boolean abwesenheitMutiert;


	public Betreuung() {
	}

	public Set<BetreuungspensumContainer> getBetreuungspensumContainers() {
		return betreuungspensumContainers;
	}

	public void setBetreuungspensumContainers(Set<BetreuungspensumContainer> betreuungspensumContainers) {
		this.betreuungspensumContainers = betreuungspensumContainers;
	}

	public Set<AbwesenheitContainer> getAbwesenheitContainers() {
		return abwesenheitContainers;
	}

	public void setAbwesenheitContainers(Set<AbwesenheitContainer> abwesenheiten) {
		this.abwesenheitContainers = abwesenheiten;
	}

	@Nonnull
	public ErweiterteBetreuungContainer getErweiterteBetreuungContainer() {
		return erweiterteBetreuungContainer;
	}

	public void setErweiterteBetreuungContainer(@Nonnull ErweiterteBetreuungContainer erweiterteBetreuungContainer) {
		this.erweiterteBetreuungContainer = erweiterteBetreuungContainer;
	}

	@Nullable
	public String getGrundAblehnung() {
		return grundAblehnung;
	}

	public void setGrundAblehnung(@Nullable String grundAblehnung) {
		this.grundAblehnung = grundAblehnung;
	}

	@Nullable
	public Verfuegung getVerfuegung() {
		return verfuegung;
	}

	public void setVerfuegung(@Nullable Verfuegung verfuegung) {
		this.verfuegung = verfuegung;
	}

	@Nonnull
	public Boolean getVertrag() {
		return vertrag;
	}

	public void setVertrag(@Nonnull Boolean vertrag) {
		this.vertrag = vertrag;
	}

	@Nullable
	public LocalDate getDatumAblehnung() {
		return datumAblehnung;
	}

	public void setDatumAblehnung(@Nullable LocalDate datumAblehnung) {
		this.datumAblehnung = datumAblehnung;
	}

	@Nullable
	public LocalDate getDatumBestaetigung() {
		return datumBestaetigung;
	}

	public void setDatumBestaetigung(@Nullable LocalDate datumBestaetigung) {
		this.datumBestaetigung = datumBestaetigung;
	}

	@Nullable
	public Boolean getBetreuungMutiert() {
		return betreuungMutiert;
	}

	public void setBetreuungMutiert(@Nullable Boolean betreuungMutiert) {
		this.betreuungMutiert = betreuungMutiert;
	}

	@Nullable
	public Boolean getAbwesenheitMutiert() {
		return abwesenheitMutiert;
	}

	public void setAbwesenheitMutiert(@Nullable Boolean abwesenheitMutiert) {
		this.abwesenheitMutiert = abwesenheitMutiert;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//by default just the fields that belong to the Betreuung itself
		return this.isSame(other, false, false);
	}

	public boolean isSame(AbstractEntity other, boolean inklAbwesenheiten, boolean inklStatus) {
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
		if (!(other instanceof Betreuung)) {
			return false;
		}
		final Betreuung otherBetreuung = (Betreuung) other;

		boolean pensenSame = this.getBetreuungspensumContainers().stream().allMatch(
			pensCont -> otherBetreuung.getBetreuungspensumContainers().stream()
				.anyMatch(otherPensenCont -> otherPensenCont.isSame(pensCont)));

		boolean abwesenheitenSame = true;
		if (inklAbwesenheiten) {
			abwesenheitenSame = this.getAbwesenheitContainers().stream().allMatch(
				abwesenheitCont -> otherBetreuung.getAbwesenheitContainers().stream()
					.anyMatch(otherAbwesenheitCont -> otherAbwesenheitCont.isSame(abwesenheitCont)));
		}
		boolean statusSame = true;
		if (inklStatus) {
			statusSame = this.getBetreuungsstatus() == otherBetreuung.getBetreuungsstatus();
		}

		boolean sameErweiterteBeduerfnisse =
			getErweiterteBetreuungContainer().isSame(otherBetreuung.getErweiterteBetreuungContainer());

		return pensenSame && abwesenheitenSame && statusSame && sameErweiterteBeduerfnisse;
	}

	@Transient
	public boolean isAngebotKita() {
		return BetreuungsangebotTyp.KITA == getBetreuungsangebotTyp();
	}

	@Transient
	public boolean isAngebotAuszuzahlen() {
		return EnumUtil.isOneOf(getBetreuungsangebotTyp(), BetreuungsangebotTyp.KITA, BetreuungsangebotTyp.TAGESFAMILIEN);
	}

	@Transient
	public boolean isAngebotTagesfamilien() {
		return BetreuungsangebotTyp.TAGESFAMILIEN == getBetreuungsangebotTyp();
	}

	@Transient
	public boolean isAngebotSchulamt() {
		return BetreuungsangebotTyp.TAGESSCHULE == getBetreuungsangebotTyp() || BetreuungsangebotTyp.FERIENINSEL == getBetreuungsangebotTyp();
	}

	/**
	 * Since it is used in email templates we need to pass the language as a String parameter
	 */
	@Transient
	public String getBetreuungsangebotTypTranslated(@Nonnull String sprache) {
		return ServerMessageUtil.translateEnumValue(getBetreuungsangebotTyp(), Locale.forLanguageTag(sprache));
	}

	/**
	 * @return die Verfuegung oder Vorgaengerverfuegung dieser Betreuung
	 */
	@Nullable
	public Verfuegung getVerfuegungOrVorgaengerAusbezahlteVerfuegung() {
		if (getVerfuegung() != null) {
			return getVerfuegung();
		}
		return getVorgaengerAusbezahlteVerfuegung();
	}

	@Nullable
	public Verfuegung getVorgaengerAusbezahlteVerfuegung() {
		return vorgaengerAusbezahlteVerfuegung;
	}

	public void setVorgaengerAusbezahlteVerfuegung(@Nullable Verfuegung vorgaengerAusbezahlteVerfuegung) {
		this.vorgaengerAusbezahlteVerfuegung = vorgaengerAusbezahlteVerfuegung;
	}

	@Nullable
	public Verfuegung getVorgaengerVerfuegung() {
		return vorgaengerVerfuegung;
	}

	public void setVorgaengerVerfuegung(@Nullable Verfuegung vorgaengerVerfuegung) {
		this.vorgaengerVerfuegung = vorgaengerVerfuegung;
	}

	@Nonnull
	public Betreuung copyBetreuung(@Nonnull Betreuung target, @Nonnull AntragCopyType copyType, @Nonnull KindContainer targetKindContainer, @Nonnull Eingangsart targetEingangsart) {
		super.copyAbstractPlatz(target, copyType, targetKindContainer);
		switch (copyType) {
		case MUTATION:
			for (BetreuungspensumContainer betreuungspensumContainer : this.getBetreuungspensumContainers()) {
				target.getBetreuungspensumContainers().add(betreuungspensumContainer
					.copyBetreuungspensumContainer(new BetreuungspensumContainer(), copyType, target));
			}
			for (AbwesenheitContainer abwesenheitContainer : this.getAbwesenheitContainers()) {
				target.getAbwesenheitContainers().add(abwesenheitContainer.copyAbwesenheitContainer(new AbwesenheitContainer(), copyType, target));
			}

			target.setErweiterteBetreuungContainer(erweiterteBetreuungContainer
				.copyErweiterteBetreuungContainer(new ErweiterteBetreuungContainer(), copyType, target));

			target.setGrundAblehnung(this.getGrundAblehnung());
			target.setVerfuegung(null);
			target.setVertrag(this.getVertrag());
			target.setDatumAblehnung(this.getDatumAblehnung());
			target.setDatumBestaetigung(this.getDatumBestaetigung());
			target.setBetreuungMutiert(null);
			target.setAbwesenheitMutiert(null);
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@Override
	protected boolean hasAnyNonZeroPensum() {
		for (BetreuungspensumContainer betreuungspensumContainer : betreuungspensumContainers) {
			if (MathUtil.isPositive(betreuungspensumContainer.getBetreuungspensumJA().getPensum())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasAnspruch() {
		if (getVerfuegung() != null) {
			List<VerfuegungZeitabschnitt> vzList = getVerfuegung().getZeitabschnitte();
			BigDecimal value = vzList.stream()
				.map(VerfuegungZeitabschnitt::getBgPensum)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			return MathUtil.isPositive(value);
		}
		return false;
	}

	public boolean hasErweiterteBetreuung() {
		return getErweiterteBetreuungContainer().getErweiterteBetreuungJA() != null
			&& getErweiterteBetreuungContainer().getErweiterteBetreuungJA().getErweiterteBeduerfnisse();
	}

	public boolean isErweiterteBeduerfnisseBestaetigt() {
		return getErweiterteBetreuungContainer().getErweiterteBetreuungJA() != null
			&& getErweiterteBetreuungContainer().getErweiterteBetreuungJA().isErweiterteBeduerfnisseBestaetigt();
	}

	@Override
	public String getMessageForAccessException() {
		return "bgNummer: " + this.getBGNummer()
			+ ", gesuchInfo: " + this.extractGesuch().getMessageForAccessException();
	}
}
