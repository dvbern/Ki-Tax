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

package ch.dvbern.ebegu.rest.test.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.api.dtos.JaxAdresse;
import ch.dvbern.ebegu.api.dtos.JaxAdresseContainer;
import ch.dvbern.ebegu.api.dtos.JaxBenutzer;
import ch.dvbern.ebegu.api.dtos.JaxBenutzerNoDetails;
import ch.dvbern.ebegu.api.dtos.JaxBerechtigung;
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensum;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxDossier;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterung;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterungContainer;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterungInfo;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.api.dtos.JaxErwerbspensum;
import ch.dvbern.ebegu.api.dtos.JaxErwerbspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxFachstelle;
import ch.dvbern.ebegu.api.dtos.JaxFall;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxGesuch;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsperiode;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsteller;
import ch.dvbern.ebegu.api.dtos.JaxGesuchstellerContainer;
import ch.dvbern.ebegu.api.dtos.JaxInstitution;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdatenSummary;
import ch.dvbern.ebegu.api.dtos.JaxKind;
import ch.dvbern.ebegu.api.dtos.JaxKindContainer;
import ch.dvbern.ebegu.api.dtos.JaxMandant;
import ch.dvbern.ebegu.api.dtos.JaxPensumFachstelle;
import ch.dvbern.ebegu.api.dtos.JaxTraegerschaft;
import ch.dvbern.ebegu.entities.AdresseTyp;
import ch.dvbern.ebegu.enums.AntragStatusDTO;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.FachstelleName;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.Land;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.util.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Generiert Testdaten fuer JAX DTOs
 */
public final class TestJaxDataUtil {

	private TestJaxDataUtil() {
	}

	public static JaxGesuchstellerContainer createTestJaxGesuchsteller() {
		JaxGesuchstellerContainer jaxGesuchstellerContainer = new JaxGesuchstellerContainer();
		JaxGesuchsteller jaxGesuchsteller = new JaxGesuchsteller();
		jaxGesuchsteller.setNachname("Jaxter");
		jaxGesuchsteller.setVorname("Jack");
		jaxGesuchsteller.setGeburtsdatum(LocalDate.now().minusYears(18));
		jaxGesuchsteller.setMail("jax.jaxter@example.com");
		jaxGesuchsteller.setGeschlecht(Geschlecht.MAENNLICH);
		jaxGesuchsteller.setMobile("+41 78 987 65 54");
		jaxGesuchsteller.setTelefonAusland("+49 12 123 42 12");

		jaxGesuchstellerContainer.setAdressen(createTestJaxAdressenList(null));
		jaxGesuchstellerContainer.setGesuchstellerJA(jaxGesuchsteller);

		return jaxGesuchstellerContainer;

	}

	public static JaxGesuchstellerContainer createTestJaxGesuchstellerWithUmzug() {
		JaxGesuchstellerContainer jaxGesuchsteller = createTestJaxGesuchsteller();

		JaxAdresseContainer jaxAdresseContainer = new JaxAdresseContainer();
		JaxAdresse umzugAdr = new JaxAdresse();
		umzugAdr.setAdresseTyp(AdresseTyp.WOHNADRESSE);
		umzugAdr.setGemeinde("neue gemeinde");
		umzugAdr.setHausnummer("99");
		umzugAdr.setLand(Land.CH);
		umzugAdr.setOrt("Umzugsort");
		umzugAdr.setPlz("999");
		umzugAdr.setZusatzzeile("Testzusatz");
		umzugAdr.setStrasse("neue Strasse");
		umzugAdr.setGueltigAb(LocalDate.now().plusMonths(1));  //gueltig 1 monat in zukunft
		jaxAdresseContainer.setAdresseJA(umzugAdr);

		jaxGesuchsteller.addAdresse(jaxAdresseContainer);

		JaxAdresseContainer jaxAltAdresseContainer = createTestJaxAdr("alternativ");
		jaxAltAdresseContainer.getAdresseJA().setAdresseTyp(AdresseTyp.KORRESPONDENZADRESSE);
		jaxGesuchsteller.setAlternativeAdresse(jaxAltAdresseContainer);

		JaxAdresseContainer jaxRechnungsAdresseContainer = createTestJaxAdr("rechnung");
		jaxRechnungsAdresseContainer.getAdresseJA().setAdresseTyp(AdresseTyp.RECHNUNGSADRESSE);
		jaxGesuchsteller.setRechnungsAdresse(jaxRechnungsAdresseContainer);

		return jaxGesuchsteller;

	}

	public static JaxGesuchstellerContainer createTestJaxGesuchstellerWithErwerbsbensum() {
		JaxGesuchstellerContainer testJaxGesuchsteller = createTestJaxGesuchsteller();
		JaxErwerbspensumContainer container = createTestJaxErwerbspensumContainer();
		JaxErwerbspensumContainer container2 = createTestJaxErwerbspensumContainer();
		Objects.requireNonNull(container2.getErwerbspensumGS());
		container2.getErwerbspensumGS().setGueltigAb(LocalDate.now().plusYears(1));
		container2.getErwerbspensumGS().setGueltigBis(null);

		Collection<JaxErwerbspensumContainer> list = new ArrayList<>();
		list.add(container);
		list.add(container2);
		testJaxGesuchsteller.setErwerbspensenContainers(list);
		return testJaxGesuchsteller;

	}

	public static JaxErwerbspensumContainer createTestJaxErwerbspensumContainer() {
		JaxErwerbspensum testJaxErwerbspensum = createTestJaxErwerbspensum();
		JaxErwerbspensumContainer container = new JaxErwerbspensumContainer();
		container.setErwerbspensumGS(testJaxErwerbspensum);
		return container;
	}

	public static JaxErwerbspensum createTestJaxErwerbspensum() {
		JaxErwerbspensum jaxErwerbspensum = new JaxErwerbspensum();
		jaxErwerbspensum.setTaetigkeit(Taetigkeit.ANGESTELLT);
		jaxErwerbspensum.setGueltigAb(LocalDate.now().minusYears(1));
		jaxErwerbspensum.setPensum(85);
		return jaxErwerbspensum;

	}

	public static List<JaxAdresseContainer> createTestJaxAdressenList(@Nullable String postfix) {
		final List<JaxAdresseContainer> adressen = new ArrayList<>();
		adressen.add(createTestJaxAdr(postfix));
		return adressen;
	}

	public static JaxAdresseContainer createTestJaxAdr(@Nullable String postfix) {
		JaxAdresseContainer jaxAdresseContainer = new JaxAdresseContainer();

		postfix = StringUtils.isEmpty(postfix) ? "" : postfix;
		JaxAdresse jaxAdresse = new JaxAdresse();
		jaxAdresse.setAdresseTyp(AdresseTyp.WOHNADRESSE);
		jaxAdresse.setGemeinde("Bern" + postfix);
		jaxAdresse.setHausnummer('1' + postfix);
		jaxAdresse.setLand(Land.CH);
		jaxAdresse.setOrt("Bern" + postfix);
		jaxAdresse.setPlz("3014" + postfix);
		jaxAdresse.setZusatzzeile("Test" + postfix);
		jaxAdresse.setStrasse("Nussbaumstrasse" + postfix);
		jaxAdresse.setNichtInGemeinde(false);

		jaxAdresseContainer.setAdresseJA(jaxAdresse);

		return jaxAdresseContainer;
	}

	public static JaxFall createTestJaxFall() {
		JaxFall jaxFall = new JaxFall();
		jaxFall.setFallNummer(1);
		return jaxFall;
	}

	public static JaxDossier createTestJaxDossier(@Nullable JaxGemeinde gemeinde) {
		JaxDossier jaxDossier = new JaxDossier();
		jaxDossier.setVerantwortlicherBG(createTestJaxBenutzerNoDetails());
		jaxDossier.setFall(createTestJaxFall());
		if (gemeinde != null) {
			jaxDossier.setGemeinde(gemeinde);
		}
		return jaxDossier;
	}

	public static JaxBenutzer createTestJaxBenutzer() {
		JaxBenutzer jaxBenutzer = new JaxBenutzer();
		JaxBerechtigung jaxBerechtigung = createTestJaxBerechtigung();
		jaxBenutzer.getBerechtigungen().add(jaxBerechtigung);
		jaxBenutzer.setCurrentBerechtigung(jaxBerechtigung);
		jaxBenutzer.setUsername("testuser");
		jaxBenutzer.setPassword("1234");
		jaxBenutzer.setEmail("testuser@example.com");
		jaxBenutzer.setNachname("NachnameTest");
		jaxBenutzer.setVorname("VornameTest");
		return jaxBenutzer;
	}

	public static JaxBenutzerNoDetails createTestJaxBenutzerNoDetails() {
		JaxBenutzerNoDetails jaxBenutzer = new JaxBenutzerNoDetails();
		jaxBenutzer.setUsername("testuser");
		jaxBenutzer.setNachname("NachnameTest");
		jaxBenutzer.setVorname("VornameTest");
		return jaxBenutzer;
	}

	public static JaxBerechtigung createTestJaxBerechtigung() {
		JaxBerechtigung berechtigung = new JaxBerechtigung();
		berechtigung.setRole(UserRole.ADMIN_BG);
		berechtigung.setGueltigAb(LocalDate.now());
		berechtigung.setGueltigBis(Constants.END_OF_TIME);
		return berechtigung;
	}

	public static JaxGesuch createTestJaxGesuch(
		@Nullable JaxGesuchsperiode jaxGesuchsperiode,
		@Nullable JaxGemeinde gemeinde
	) {
		JaxGesuch jaxGesuch = new JaxGesuch();
		jaxGesuch.setEingangsart(Eingangsart.PAPIER);
		jaxGesuch.setDossier(createTestJaxDossier(gemeinde));
		jaxGesuch.setGesuchsperiode(jaxGesuchsperiode != null ? jaxGesuchsperiode : createTestJaxGesuchsperiode());
		jaxGesuch.setGesuchsteller1(createTestJaxGesuchsteller());
		jaxGesuch.setEingangsdatum(LocalDate.now());
		jaxGesuch.setStatus(AntragStatusDTO.IN_BEARBEITUNG_JA);
		JaxGesuchstellerContainer testJaxGesuchsteller = createTestJaxGesuchsteller();
		testJaxGesuchsteller.getGesuchstellerJA().setNachname("Gesuchsteller2");
		jaxGesuch.setGesuchsteller2(testJaxGesuchsteller);
		return jaxGesuch;
	}

	public static JaxFachstelle createTestJaxFachstelle() {
		JaxFachstelle jaxFachstelle = new JaxFachstelle();
		jaxFachstelle.setName(FachstelleName.FRUEHERZIEHUNGSDIENST_KANTON_BERN);
		return jaxFachstelle;
	}

	public static JaxKind createTestJaxKind() {
		JaxKind jaxKind = new JaxKind();
		jaxKind.setNachname("Kind_Mustermann");
		jaxKind.setVorname("Kind_Max");
		jaxKind.setGeburtsdatum(LocalDate.now().minusYears(18));
		jaxKind.setGeschlecht(Geschlecht.WEIBLICH);
		jaxKind.setPensumFachstellen(createTestJaxPensumFachstellenSet());
		jaxKind.setSprichtAmtssprache(false);
		jaxKind.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		jaxKind.setFamilienErgaenzendeBetreuung(true);
		jaxKind.setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		jaxKind.setKinderabzugZweitesHalbjahr(Kinderabzug.GANZER_ABZUG);
		return jaxKind;
	}

	public static Collection<JaxPensumFachstelle> createTestJaxPensumFachstellenSet() {
		JaxPensumFachstelle jaxPensumFachstelle = new JaxPensumFachstelle();
		jaxPensumFachstelle.setGueltigBis(LocalDate.now().plusMonths(1));
		jaxPensumFachstelle.setGueltigAb(LocalDate.now());
		jaxPensumFachstelle.setPensum(50);
		jaxPensumFachstelle.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		jaxPensumFachstelle.setFachstelle(createTestJaxFachstelle());
		return Set.of(jaxPensumFachstelle);
	}

	public static JaxKindContainer createTestJaxKindContainer() {
		JaxKindContainer jaxKindContainer = new JaxKindContainer();
		jaxKindContainer.setKindGS(createTestJaxKind());
		jaxKindContainer.setKindJA(createTestJaxKind());
		return jaxKindContainer;
	}

	public static JaxBetreuungspensum createTestJaxBetreuungspensum(LocalDate from, LocalDate to) {
		JaxBetreuungspensum jaxBetreuungspensum = new JaxBetreuungspensum();
		jaxBetreuungspensum.setGueltigAb(from);
		jaxBetreuungspensum.setGueltigBis(to);

		jaxBetreuungspensum.setPensum(BigDecimal.valueOf(40));
		jaxBetreuungspensum.setMonatlicheBetreuungskosten(new BigDecimal(2000));
		jaxBetreuungspensum.setMonatlicheHauptmahlzeiten(BigDecimal.ZERO);
		jaxBetreuungspensum.setMonatlicheNebenmahlzeiten(BigDecimal.ZERO);
		jaxBetreuungspensum.setTarifProHauptmahlzeit(BigDecimal.ZERO);
		jaxBetreuungspensum.setTarifProNebenmahlzeit(BigDecimal.ZERO);
		return jaxBetreuungspensum;
	}

	public static JaxBetreuungspensumContainer createBetreuungspensumContainer(int year) {

		LocalDate from = LocalDate.of(year, 8, 1);
		LocalDate to = LocalDate.of(year + 1, 7, 31);

		JaxBetreuungspensumContainer jaxBetrPenCnt = new JaxBetreuungspensumContainer();
		jaxBetrPenCnt.setBetreuungspensumJA(createTestJaxBetreuungspensum(from, to));
		jaxBetrPenCnt.setBetreuungspensumGS(createTestJaxBetreuungspensum(from, to));
		return jaxBetrPenCnt;
	}

	public static JaxBetreuung createTestJaxBetreuung() {
		JaxBetreuung betreuung = new JaxBetreuung();
		JaxInstitutionStammdatenSummary jaxInst = createTestJaxInstitutionsStammdatenSummary();
		betreuung.setInstitutionStammdaten(jaxInst);
		betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
		betreuung.setBetreuungspensumContainers(new ArrayList<>());
		return betreuung;
	}

	public static JaxInstitutionStammdaten createTestJaxInstitutionsStammdaten() {
		JaxInstitutionStammdaten institutionStammdaten = new JaxInstitutionStammdaten();
		institutionStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		institutionStammdaten.setGueltigAb(Constants.DEFAULT_GUELTIGKEIT.getGueltigAb());
		institutionStammdaten.setMail("mail@example.com");
		institutionStammdaten.setAdresse(createTestJaxAdr("JA").getAdresseJA());
		return institutionStammdaten;
	}

	public static JaxInstitutionStammdatenSummary createTestJaxInstitutionsStammdatenSummary() {
		JaxInstitutionStammdatenSummary institutionStammdaten = new JaxInstitutionStammdatenSummary();
		institutionStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		institutionStammdaten.setGueltigAb(Constants.DEFAULT_GUELTIGKEIT.getGueltigAb());
		institutionStammdaten.setMail("mail@example.com");
		institutionStammdaten.setAdresse(createTestJaxAdr("JA").getAdresseJA());
		return institutionStammdaten;
	}

	public static JaxGesuchsperiode createTestJaxGesuchsperiode() {
		JaxGesuchsperiode jaxGesuchsperiode = new JaxGesuchsperiode();
		jaxGesuchsperiode.setGueltigAb(LocalDate.now());
		jaxGesuchsperiode.setGueltigBis(LocalDate.now().plusMonths(1));
		jaxGesuchsperiode.setStatus(GesuchsperiodeStatus.AKTIV);
		return jaxGesuchsperiode;
	}

	public static JaxEinkommensverschlechterungInfoContainer createTestJaxEinkommensverschlechterungInfoContainer() {
		JaxEinkommensverschlechterungInfoContainer jaxEinkommensverschlechterungInfoContainer = new JaxEinkommensverschlechterungInfoContainer();
		jaxEinkommensverschlechterungInfoContainer.setEinkommensverschlechterungInfoJA(createTestJaxEinkommensverschlechterungInfo());
		return jaxEinkommensverschlechterungInfoContainer;
	}

	public static JaxEinkommensverschlechterungInfo createTestJaxEinkommensverschlechterungInfo() {
		JaxEinkommensverschlechterungInfo jaxEinkommensverschlechterungInfo = new JaxEinkommensverschlechterungInfo();
		jaxEinkommensverschlechterungInfo.setEinkommensverschlechterung(true);
		jaxEinkommensverschlechterungInfo.setEkvFuerBasisJahrPlus1(true);
		jaxEinkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(false);
		jaxEinkommensverschlechterungInfo.setEkvBasisJahrPlus1Annulliert(Boolean.FALSE);
		jaxEinkommensverschlechterungInfo.setEkvBasisJahrPlus2Annulliert(Boolean.FALSE);
		return jaxEinkommensverschlechterungInfo;
	}

	public static JaxEinkommensverschlechterungContainer createTestJaxEinkommensverschlechterungContianer() {
		JaxEinkommensverschlechterungContainer einkommensverschlechterungContainer = new JaxEinkommensverschlechterungContainer();

		einkommensverschlechterungContainer.setEkvGSBasisJahrPlus1(createDefaultJaxEinkommensverschlechterungs());

		final JaxEinkommensverschlechterung ekvGSBasisJahrPlus2 = createDefaultJaxEinkommensverschlechterungs();
		ekvGSBasisJahrPlus2.setNettolohn(BigDecimal.valueOf(2));
		einkommensverschlechterungContainer.setEkvGSBasisJahrPlus2(ekvGSBasisJahrPlus2);

		final JaxEinkommensverschlechterung ekvJABasisJahrPlus1 = createDefaultJaxEinkommensverschlechterungs();
		ekvJABasisJahrPlus1.setNettolohn(BigDecimal.valueOf(3));
		einkommensverschlechterungContainer.setEkvJABasisJahrPlus1(ekvJABasisJahrPlus1);

		final JaxEinkommensverschlechterung ekvJABasisJahrPlus2 = createDefaultJaxEinkommensverschlechterungs();
		ekvJABasisJahrPlus2.setNettolohn(BigDecimal.valueOf(4));
		einkommensverschlechterungContainer.setEkvJABasisJahrPlus2(ekvJABasisJahrPlus2);

		return einkommensverschlechterungContainer;
	}

	public static JaxEinkommensverschlechterung createDefaultJaxEinkommensverschlechterungs() {
		JaxEinkommensverschlechterung einkommensverschlechterung = new JaxEinkommensverschlechterung();
		einkommensverschlechterung.setNettolohn(BigDecimal.ONE);
		return einkommensverschlechterung;
	}

	public static JaxMandant createTestMandant() {
		JaxMandant mandant = new JaxMandant();
		mandant.setName("TestMandant");
		return mandant;
	}

	public static JaxGemeinde createTestGemeinde() {
		JaxGemeinde gemeinde = new JaxGemeinde();
		gemeinde.setName("TestGemeinde");
		gemeinde.setGemeindeNummer(1);
		gemeinde.setBfsNummer(1L);
		return gemeinde;
	}

	public static JaxInstitution createTestJaxInstitution() {
		JaxInstitution institution = new JaxInstitution();
		institution.setMandant(createTestMandant());
		institution.setName("Inst1");
		institution.setTraegerschaft(createJaxTestTraegerschaft());
		return institution;
	}

	public static JaxTraegerschaft createJaxTestTraegerschaft() {
		JaxTraegerschaft jaxTraegerschaft = new JaxTraegerschaft();
		jaxTraegerschaft.setName("Test_Traegerschaft");
		jaxTraegerschaft.setActive(true);
		return jaxTraegerschaft;
	}
}
