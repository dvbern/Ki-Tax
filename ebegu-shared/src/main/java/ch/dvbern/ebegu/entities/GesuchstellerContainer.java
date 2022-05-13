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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;

import ch.dvbern.ebegu.dto.suchfilter.lucene.Searchable;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.validationgroups.AntragCompleteValidationGroup;
import ch.dvbern.ebegu.validators.CheckGesuchstellerContainerComplete;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * Entitaet zum Speichern von GesuchContainer in der Datenbank.
 */
@Audited
@CheckGesuchstellerContainerComplete(groups = AntragCompleteValidationGroup.class)
@Entity
@Indexed
@Analyzer(definition = "EBEGUGermanAnalyzer")
public class GesuchstellerContainer extends AbstractMutableEntity implements Searchable {

	private static final long serialVersionUID = -8403117439764700618L;

	@Valid
	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuchsteller_container_gesuchstellergs_id"), nullable = true)
	private Gesuchsteller gesuchstellerGS;

	@Valid
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuchsteller_container_gesuchstellerja_id"), nullable = true)
	@IndexedEmbedded
	private Gesuchsteller gesuchstellerJA;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "gesuchstellerContainer")
	private FinanzielleSituationContainer finanzielleSituationContainer;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "gesuchstellerContainer")
	private EinkommensverschlechterungContainer einkommensverschlechterungContainer;

	@Nonnull
	@Valid
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "gesuchstellerContainer")
	private Set<ErwerbspensumContainer> erwerbspensenContainers = new HashSet<>();

	@Valid
	@Nonnull
	// es handelt sich um eine "private" Relation, das heisst Adressen koennen nie einer anderen Gesuchsteller zugeordnet werden
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "gesuchstellerContainer")
	private List<GesuchstellerAdresseContainer> adressen = new ArrayList<>();

	public GesuchstellerContainer() {
	}

	@CanIgnoreReturnValue
	public boolean addAdresse(@Nonnull final GesuchstellerAdresseContainer gesuchstellerAdresseContainer) {
		gesuchstellerAdresseContainer.setGesuchstellerContainer(this);
		if (adressen.contains(gesuchstellerAdresseContainer)) {
			return false;
		}
		adressen.add(gesuchstellerAdresseContainer);
		return true;
	}

	@Nullable
	public Gesuchsteller getGesuchstellerGS() {
		return gesuchstellerGS;
	}

	public void setGesuchstellerGS(@Nullable Gesuchsteller gesuchstellerGS) {
		this.gesuchstellerGS = gesuchstellerGS;
	}

	public Gesuchsteller getGesuchstellerJA() {
		return gesuchstellerJA;
	}

	public void setGesuchstellerJA(Gesuchsteller gesuchstellerJA) {
		this.gesuchstellerJA = gesuchstellerJA;
	}

	@Nonnull
	public List<GesuchstellerAdresseContainer> getAdressen() {
		return adressen;
	}

	public void setAdressen(@Nonnull final List<GesuchstellerAdresseContainer> adressen) {
		this.adressen = adressen;
	}

	/**
	 * Returns the first korrespondezAdresse found for this GesuchstellerContainer. It should have only one.
	 * If no korrespondezAdresse is set, null is returned
	 */
	@Nullable
	public GesuchstellerAdresseContainer extractKorrespondezAdresse() {
		for (GesuchstellerAdresseContainer adresse : getAdressen()) {
			if (adresse.extractIsKorrespondenzAdresse()) {
				return adresse;
			}
		}
		return null;
	}

	/**
	 * Returns the first rechnungsAdresse found for this GesuchstellerContainer. It should have only one.
	 * If no rechnungsAdresse is set, null is returned
	 */
	@Nullable
	private GesuchstellerAdresseContainer extractRechnungsAdresse() {
		for (GesuchstellerAdresseContainer adresse : getAdressen()) {
			if (adresse.extractIsRechnungsAdresse()) {
				return adresse;
			}
		}
		return null;
	}

	@Nullable
	public FinanzielleSituationContainer getFinanzielleSituationContainer() {
		return finanzielleSituationContainer;
	}

	@Nonnull
	public Set<ErwerbspensumContainer> getErwerbspensenContainers() {
		return erwerbspensenContainers;
	}

	@Nonnull
	public Set<ErwerbspensumContainer> getErwerbspensenContainersNotEmpty() {
		if (!erwerbspensenContainers.isEmpty()) {
			return erwerbspensenContainers;
		}

		final Set<ErwerbspensumContainer> erwerbspensen = new HashSet<>();
		final ErwerbspensumContainer erwerbspensum = new ErwerbspensumContainer();
		Erwerbspensum pensumJA = new Erwerbspensum();
		pensumJA.setGueltigkeit(new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME));
		pensumJA.setPensum(0);
		pensumJA.setTaetigkeit(Taetigkeit.ANGESTELLT);
		erwerbspensum.setErwerbspensumJA(pensumJA);
		erwerbspensen.add(erwerbspensum);
		return erwerbspensen;
	}

	public void setErwerbspensenContainers(@Nonnull final Set<ErwerbspensumContainer> erwerbspensenContainers) {
		this.erwerbspensenContainers = erwerbspensenContainers;
	}

	public void setFinanzielleSituationContainer(@Nullable final FinanzielleSituationContainer
		finanzielleSituationContainer) {
		this.finanzielleSituationContainer = finanzielleSituationContainer;
		if (this.finanzielleSituationContainer != null &&
			(this.finanzielleSituationContainer.getGesuchsteller() == null || !this.finanzielleSituationContainer.getGesuchsteller().equals(this))) {
			this.finanzielleSituationContainer.setGesuchsteller(this);
		}
	}

	@CanIgnoreReturnValue
	public boolean addErwerbspensumContainer(final ErwerbspensumContainer erwerbspensumToAdd) {
		erwerbspensumToAdd.setGesuchsteller(this);
		return !erwerbspensenContainers.contains(erwerbspensumToAdd) && erwerbspensenContainers.add(erwerbspensumToAdd);
	}

	@Nullable
	public EinkommensverschlechterungContainer getEinkommensverschlechterungContainer() {
		return einkommensverschlechterungContainer;
	}

	public void setEinkommensverschlechterungContainer(@Nullable final EinkommensverschlechterungContainer einkommensverschlechterungContainer) {
		this.einkommensverschlechterungContainer = einkommensverschlechterungContainer;
		if (einkommensverschlechterungContainer != null &&
			(einkommensverschlechterungContainer.getGesuchsteller() == null || !einkommensverschlechterungContainer.getGesuchsteller().equals(this))) {
			einkommensverschlechterungContainer.setGesuchsteller(this);
		}
	}

	/**
	 * Gibt den Namen des GesuchstellerJA oder ein Leerzeichen wenn er nicht existiert
	 */
	public String extractNachname() {
		if (this.gesuchstellerJA != null) {
			return this.gesuchstellerJA.getNachname();
		}
		return "";
	}

	/**
	 * Gibt den Vornamen des GesuchstellerJA oder ein Leerzeichen wenn er nicht existiert
	 */
	public String extractVorname() {
		if (this.gesuchstellerJA != null) {
			return this.gesuchstellerJA.getVorname();
		}
		return "";
	}

	/**
	 * Gibt den FullNamen des GesuchstellerJA oder ein Leerzeichen wenn er nicht existiert
	 */
	public String extractFullName() {
		if (this.gesuchstellerJA != null) {
			return this.gesuchstellerJA.getFullName();
		}
		return "";
	}

	@Nonnull
	public GesuchstellerContainer copyGesuchstellerContainer(@Nonnull GesuchstellerContainer target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		target.setGesuchstellerGS(null);

		if (this.getGesuchstellerJA() != null) {
			target.setGesuchstellerJA(this.getGesuchstellerJA().copyGesuchsteller(new Gesuchsteller(), copyType));
		}
		switch (copyType) {
		case MUTATION:
			copyAdressenAll(target, copyType);
			copyFinanzen(target, copyType);
			copyErwerbspensen(target, copyType);
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			copyAdressenAktuellUndZukuenftig(target, copyType);
			break;
		case MUTATION_NEUES_DOSSIER:
			copyAdressenAktuellUndZukuenftig(target, copyType);
			copyFinanzen(target, copyType);
			copyErwerbspensen(target, copyType);
			break;
		}
		return target;
	}

	private void copyFinanzen(@Nonnull GesuchstellerContainer target, @Nonnull AntragCopyType copyType) {
		if (this.getFinanzielleSituationContainer() != null) {
			target.setFinanzielleSituationContainer(this.getFinanzielleSituationContainer()
				.copyFinanzielleSituationContainer(new FinanzielleSituationContainer(), copyType, this));
		}
		if (this.getEinkommensverschlechterungContainer() != null) {
			target.setEinkommensverschlechterungContainer(this.getEinkommensverschlechterungContainer()
				.copyEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer(), copyType, this));
		}
	}

	private void copyErwerbspensen(@Nonnull GesuchstellerContainer target, @Nonnull AntragCopyType copyType) {
		for (ErwerbspensumContainer erwerbspensumContainer : this.getErwerbspensenContainers()) {
			target.addErwerbspensumContainer(erwerbspensumContainer.copyErwerbspensumContainer(new ErwerbspensumContainer(), copyType, this));
		}
	}

	private void copyAdressenAll(@Nonnull GesuchstellerContainer target, @Nonnull AntragCopyType copyType) {
		for (GesuchstellerAdresseContainer gesuchstellerAdresse : this.getAdressen()) {
			if (gesuchstellerAdresse.getGesuchstellerAdresseJA() != null) {
				target.addAdresse(gesuchstellerAdresse.copyGesuchstellerAdresseContainer(new GesuchstellerAdresseContainer(), copyType, this));
			}
		}
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	private void copyAdressenAktuellUndZukuenftig(@Nonnull GesuchstellerContainer target, @Nonnull AntragCopyType copyType) {
		for (GesuchstellerAdresseContainer gesuchstellerAdresse : this.getAdressen()) {
			if (gesuchstellerAdresse.getGesuchstellerAdresseJA() != null) {
				// Nur aktuelle und zukuenftige Adressen kopieren. Aus Sicht HEUTE und nicht per Anfang Gesuchsperiode, da schon vorher Briefe
				// geschickt werden muessen
				if (!Objects.requireNonNull(gesuchstellerAdresse.extractGueltigkeit()).endsBefore(LocalDate.now())) {
					GesuchstellerAdresseContainer adresseContainer = gesuchstellerAdresse.copyGesuchstellerAdresseContainer(
						new GesuchstellerAdresseContainer(), copyType,this);
					target.addAdresse(adresseContainer);
				}
			}
		}
	}

	/**
	 * Will return the korrespondenzadresse if it is defined. If not it will return the current Wohnadresse
	 */
	//TODO (team) so was ähnliches kommt auch im PrintUtil.getGesuchstellerAdresse bzw. in den neuen PDFGenerators vor!
	@Nullable
	public GesuchstellerAdresse extractEffektiveKorrespondezAdresse(LocalDate stichtag) {
		final GesuchstellerAdresseContainer korrespondezAdresse = extractKorrespondezAdresse();
		if (korrespondezAdresse != null) {
			return korrespondezAdresse.getGesuchstellerAdresseJA();
		}
		for (GesuchstellerAdresseContainer adresse : getAdressen()) {
			if (AdresseTyp.WOHNADRESSE == adresse.extractAdresseTyp() && adresse.extractGueltigkeit().contains(stichtag)) {
				return adresse.getGesuchstellerAdresseJA();
			}
		}
		return null;
	}

	@Nullable
	public GesuchstellerAdresse getWohnadresseAm(LocalDate stichtag) {
		for (GesuchstellerAdresseContainer adresse : getAdressen()) {
			if (AdresseTyp.WOHNADRESSE == adresse.extractAdresseTyp() && adresse.extractGueltigkeit().contains(stichtag)) {
				return adresse.getGesuchstellerAdresseJA();
			}
		}
		return null;
	}

	@Nonnull
	public List<Erwerbspensum> getErwerbspensenAm(LocalDate stichtag) {
		List<Erwerbspensum> erwerbspensenInZeitraum = new ArrayList<>();
		for (ErwerbspensumContainer erwerbspensumContainer : getErwerbspensenContainersNotEmpty()) {
			if (erwerbspensumContainer.getErwerbspensumJA().getGueltigkeit().contains(stichtag)) {
				erwerbspensenInZeitraum.add(erwerbspensumContainer.getErwerbspensumJA());
			}
		}
		return erwerbspensenInZeitraum;
	}

	@Nonnull
	@Override
	public String getSearchResultId() {
		return getId();
	}

	@Nonnull
	@Override
	public String getSearchResultSummary() {
		return extractFullName();
	}

	@Nullable
	@Override
	public String getSearchResultAdditionalInformation() {
		return toString();
	}

	@Nullable
	@Override
	public String getOwningGesuchId() {
		return null;   //leider nicht ohne serviceabfrage verfuegbar
	}

	@Nullable
	@Override
	public String getOwningFallId() {
		return null;   //leider nicht ohne serviceabfrage verfuegbar
	}

	@Nullable
	@Override
	public String getOwningDossierId() {
		return null;  //leider nicht ohne serviceabfrage verfuegbar
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final GesuchstellerContainer otherGesuchstellerContainer = (GesuchstellerContainer) other;
		return EbeguUtil.isSame(getGesuchstellerJA(), otherGesuchstellerContainer.getGesuchstellerJA());
	}

	/**
	 * Gibt die Rechnungsadresse zurueck. Sollte diese nicht erfasst sein, gibt die Wohnadresse zurueck, die
	 * am stichtag gilt.
	 */
	@Nullable
	public GesuchstellerAdresse extractEffectiveRechnungsAdresse(LocalDate stichtag) {
		final GesuchstellerAdresseContainer rechnungsadresse = extractRechnungsAdresse();
		if (rechnungsadresse != null) {
			return rechnungsadresse.getGesuchstellerAdresseJA();
		}
		return getWohnadresseAm(stichtag);
	}
}
