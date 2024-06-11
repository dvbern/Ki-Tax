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

package ch.dvbern.ebegu.testfaelle;

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.testfealleschwyz.BetreuungData;
import ch.dvbern.ebegu.testfaelle.testfealleschwyz.GesuchstellerData;
import ch.dvbern.ebegu.testfaelle.testfealleschwyz.KindData;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Superklasse für Schwyz-Testfaelle.
 * Diese Testfälle werden verwendet für die Schulung von Schwyz. Wir werden danach die Testfälle überarbeiten.
 */
public abstract class AbstractSZTestfall extends AbstractTestfall {


	protected AbstractSZTestfall(
			@Nonnull Gesuchsperiode gesuchsperiode,
			boolean betreuungenBestaetigt,
			@Nonnull Gemeinde gemeinde,
			InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	protected AbstractSZTestfall(
			@Nonnull Gesuchsperiode gesuchsperiode,
			boolean betreuungenBestaetigt,
			InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, institutionStammdatenBuilder);
	}

	protected Gesuch createAlleinerziehend(Gesuch gesuch, LocalDate ereignisdatum) {
		// Familiensituation
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		FamiliensituationContainer familiensituationContainer = new FamiliensituationContainer();
		familiensituationContainer.setFamiliensituationJA(familiensituation);
		familiensituation.setAenderungPer(ereignisdatum);

		Familiensituation familiensituationErstgesuch = new Familiensituation();
		familiensituationErstgesuch.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		familiensituationErstgesuch.setGemeinsameSteuererklaerung(Boolean.TRUE);
		familiensituationContainer.setFamiliensituationErstgesuch(familiensituationErstgesuch);

		gesuch.setFamiliensituationContainer(familiensituationContainer);
		return gesuch;
	}

	protected Gesuch createVerheiratet(Gesuch gesuch, LocalDate ereignisdatum) {
		// Familiensituation
		assert gesuch.getFamiliensituationContainer() != null;
		assert gesuch.getFamiliensituationContainer().getFamiliensituationJA() != null;

		Familiensituation familiensituation = gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
		familiensituation.setSozialhilfeBezueger(gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				.getSozialhilfeBezueger());
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		familiensituation.setGemeinsameSteuererklaerung(gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				.getGemeinsameSteuererklaerung());
		FamiliensituationContainer familiensituationContainer = new FamiliensituationContainer();
		familiensituationContainer.setFamiliensituationJA(familiensituation);
		familiensituation.setAenderungPer(ereignisdatum);

		familiensituationContainer.setFamiliensituationErstgesuch(gesuch.extractFamiliensituationErstgesuch());

		gesuch.setFamiliensituationContainer(familiensituationContainer);
		return gesuch;
	}

	protected GesuchstellerContainer createGesuchsteller(GesuchstellerData gsData) {
		Gesuchsteller gs = testfallDataProvider.createGesuchsteller(
			gsData.getNachname(),
			gsData.getVorname(),
			gsData.getGesuchstellerNummer());

		gs.setGeburtsdatum(gsData.getGeburtsdatum());
		gs.setSozialversicherungsnummer(gsData.getSvNummer());
		gs.setGeschlecht(gsData.getGeschlecht());

		GesuchstellerContainer gesuchstellerCont = new GesuchstellerContainer();
		gesuchstellerCont.setGesuchstellerJA(gs);
		gesuchstellerCont.setAdressen(new ArrayList<>());

		if (gsData.hasAdress()) {
			GesuchstellerAdresseContainer adresse = createWohnadresseContainer(gesuchstellerCont);
			Objects.requireNonNull(adresse.getGesuchstellerAdresseJA());
			adresse.getGesuchstellerAdresseJA().setStrasse(gsData.getStrasse());
			adresse.getGesuchstellerAdresseJA().setHausnummer(gsData.getHausnummer());
			adresse.getGesuchstellerAdresseJA().setPlz(gsData.getPlz());
			adresse.getGesuchstellerAdresseJA().setOrt(gsData.getOrt());
			gesuchstellerCont.getAdressen().add(adresse);
		}

		return gesuchstellerCont;
	}

	protected KindContainer createKindContainer(KindData kindData) {
		KindContainer kind = createKind(
			kindData.getGeschlecht(),
			kindData.getNachname(),
			kindData.getVorname(),
			kindData.getGeburtsdatum(),
			kindData.getKinderabzug(),
			kindData.getFamilienergaenzendBetreuug());

		kind.getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(kindData.getHohereBeitraege());
		kind.getKindJA().setUnterhaltspflichtig(kindData.getUnterhaltspflichtig());
		kind.getKindJA().setLebtKindAlternierend(kindData.getLebtAlternierend());
		kind.getKindJA().setGemeinsamesGesuch(kindData.getGemeinsamesGesuch());
		kind.getKindJA().setFamilienErgaenzendeBetreuung(kindData.getFamilienergaenzendBetreuug());
		kind.getKindJA().setEinschulungTyp(kindData.getEinschulungTyp());
		kind.getKindJA().setKinderabzugErstesHalbjahr(kindData.getKinderabzug());

		kindData.getBetreuungDataList().forEach(betreuungData -> {
			createBetreuungContainer(kind, betreuungData);
		});
		return kind;
	}

	private void createBetreuungContainer(KindContainer kind, BetreuungData betreuungData) {
		Betreuung betreuung = createBetreuung(betreuungData.getInstiutionId(), betreuungData.isBestaetigt());
		betreuung.setAuszahlungAnEltern(betreuungData.isAuszahlungAnEltern());
		betreuung.setBegruendungAuszahlungAnInstitution(betreuungData.getBegruendung());
		betreuung.setKind(kind);
		kind.getBetreuungen().add(betreuung);

		betreuungData.getBetreuungspensum()
			.stream()
			.map(pensum -> {
				BetreuungspensumContainer betreuungspensum = createBetreuungspensum(
					BigDecimal.valueOf(pensum.getPensum()),
					pensum.getGueltigAb(),
					pensum.getGueltigBis());
				betreuungspensum.getBetreuungspensumJA().setMonatlicheBetreuungskosten(pensum.getMonatlicheBetreuungskosten());
				betreuungspensum.setBetreuung(betreuung);
				betreuungspensum.getBetreuungspensumJA().setBetreuungInFerienzeit(pensum.isBetreuungInFerienzeit());
				betreuungspensum.getBetreuungspensumJA().setMonatlicheHauptmahlzeiten(pensum.getMonatlicheHauptmahlzeiten());
				betreuungspensum.getBetreuungspensumJA().setTarifProHauptmahlzeit(pensum.getTarifProMahlzeit());
				return betreuungspensum;
			})
			.collect(Collectors.toCollection(betreuung::getBetreuungspensumContainers));
	}

	protected ErwerbspensumContainer createErwerbspensumContainer(GesuchstellerData gsData) {
		ErwerbspensumContainer erwerbspensum = createErwerbspensum(gsData.getErwerbspensum());
		Objects.requireNonNull(erwerbspensum.getErwerbspensumJA());
		erwerbspensum.getErwerbspensumJA().setTaetigkeit(gsData.getTaetigkeit());
		erwerbspensum.getErwerbspensumJA().setBezeichnung(gsData.getErwerbsBezeichnung());
		erwerbspensum.getErwerbspensumJA().setGueltigkeit(gsData.getErwerbGueltigkeit());
		return erwerbspensum;
	}

	protected FinanzielleSituationContainer createFinSit(GesuchstellerData gsData) {
		FinanzielleSituationContainer finsit = createFinanzielleSituationContainer(gsData.getReinvermoegen(), gsData.getReineinkommen());
		if (gsData.isQuellenbesteuert()) {
			finsit.getFinanzielleSituationJA().setQuellenbesteuert(true);
			finsit.getFinanzielleSituationJA().setBruttoLohn(gsData.getBruttoLohn());
			finsit.getFinanzielleSituationJA().setSteuerbaresVermoegen(BigDecimal.ZERO);
			finsit.getFinanzielleSituationJA().setSteuerbaresEinkommen(BigDecimal.ZERO);
		}
		finsit.getFinanzielleSituationJA().setGemeinsameStekVorjahr(gsData.isGemeinsameSteuererklaerung());
		return finsit;
	}

	protected void setAuszahlungsdaten(Gesuch erstgesuch, GesuchstellerData gsData) {
		Objects.requireNonNull(erstgesuch.getFamiliensituationContainer());
		Objects.requireNonNull(erstgesuch.getFamiliensituationContainer().getFamiliensituationJA());
		Objects.requireNonNull(erstgesuch.getFamiliensituationContainer().getFamiliensituationJA().getAuszahlungsdaten());

		Auszahlungsdaten a = erstgesuch.getFamiliensituationContainer().getFamiliensituationJA().getAuszahlungsdaten();
		a.setIban(gsData.getIban());
		a.setKontoinhaber(gsData.getKontoinhaber());
	}

	@Override
	public String getNachname() {
		return "";
	}

	@Override
	public String getVorname() {
		return "";
	}

}
