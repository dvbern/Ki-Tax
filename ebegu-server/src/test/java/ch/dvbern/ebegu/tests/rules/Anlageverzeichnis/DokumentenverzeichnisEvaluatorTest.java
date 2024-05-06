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

package ch.dvbern.ebegu.tests.rules.Anlageverzeichnis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.AnspruchBeschaeftigungAbhaengigkeitTyp;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.FachstelleName;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.rules.anlageverzeichnis.BernErwerbspensumDokumente;
import ch.dvbern.ebegu.rules.anlageverzeichnis.BernKindDokumente;
import ch.dvbern.ebegu.rules.anlageverzeichnis.DokumentenverzeichnisEvaluator;
import ch.dvbern.ebegu.rules.anlageverzeichnis.LuzernErwerbspensumDokumente;
import ch.dvbern.ebegu.rules.anlageverzeichnis.LuzernKindDokumente;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.dataprovider.SchwyzTestfallDataProvider;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import com.spotify.hamcrest.pojo.IsPojo;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.stream.Collectors;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static java.util.Objects.requireNonNull;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests für die Regeln der Benötigten Dokumenten
 */
@ExtendWith(EasyMockExtension.class)
class DokumentenverzeichnisEvaluatorTest extends EasyMockSupport {

	private static final BigDecimal ZEHN_TAUSEND = BigDecimal.valueOf(100000);

	@TestSubject
	private final DokumentenverzeichnisEvaluator evaluator = new DokumentenverzeichnisEvaluator();

	@Mock
	private EinstellungService einstellungServiceMock;

	private final BernKindDokumente bernKindDokumente = new BernKindDokumente();
	private final LuzernKindDokumente luzernKindDokumente = new LuzernKindDokumente();
	private final BernErwerbspensumDokumente bernErwerbspensumDokumente = new BernErwerbspensumDokumente();
	private final LuzernErwerbspensumDokumente luzernErwerbspensumDokumente = new LuzernErwerbspensumDokumente();
	private Gesuch testgesuchBern;
	private Gesuch testgesuchLuzern;

	@BeforeEach
	void setUpCalculator() {
		testgesuchBern = setUpTestgesuch(TestDataUtil.getMandantKantonBern(), FinanzielleSituationTyp.BERN);
		testgesuchLuzern = setUpTestgesuch(TestDataUtil.getMandantLuzern(), FinanzielleSituationTyp.LUZERN);
	}

	@AfterEach
	void tearDown() {
		verifyAll();
	}

	private Gesuch setUpTestgesuch(Mandant mandant, FinanzielleSituationTyp finanzielleSituationTyp) {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		gesuch.getGesuchsperiode().getGueltigkeit().setGueltigAb(Constants.GESUCHSPERIODE_17_18_AB);
		gesuch.getGesuchsperiode().getGueltigkeit().setGueltigBis(Constants.GESUCHSPERIODE_17_18_BIS);
		gesuch.setKindContainers(new HashSet<>());
		gesuch.setDossier(new Dossier());
		gesuch.setFinSitTyp(finanzielleSituationTyp);

		Fall fall = new Fall();
		fall.setMandant(mandant);
		Dossier dossier = new Dossier();
		dossier.setFall(fall);
		gesuch.setDossier(dossier);

		return gesuch;
	}

	@Nonnull
	private Einstellung createEinstellung(EinstellungKey key, String value) {
		Einstellung einstellung = new Einstellung();
		einstellung.setKey(key);
		einstellung.setValue(value);

		return einstellung;
	}

	private void setupEinstellungBeschaeftigungsPensumAbhaengig(@Nonnull Gesuch gesuch) {
		Einstellung einstellung = createEinstellung(
			EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
			AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name()
		);

		setUpEinstellungMock(gesuch, einstellung);
	}

	private void setUpEinstellungMock(@Nonnull Gesuch gesuch, @Nonnull Einstellung einstellung) {
		Gemeinde gemeinde = gesuch.extractGemeinde();
		Gesuchsperiode gesuchsperiode = gesuch.getGesuchsperiode();

		expect(einstellungServiceMock.findEinstellung(einstellung.getKey(), gemeinde, gesuchsperiode))
			.andReturn(einstellung)
			.anyTimes();
		replayAll();
	}

	private Kind createKind(Gesuch gesuch, @Nullable FachstelleName fachstellename, @Nullable IntegrationTyp integrationTyp) {
		final KindContainer kindContainer = TestDataUtil.createDefaultKindContainer();
		kindContainer.getKindJA().setNachname("Chavez");
		kindContainer.getKindJA().setVorname("Jan");
		kindContainer.getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		kindContainer.getKindJA().setKinderabzugZweitesHalbjahr(Kinderabzug.GANZER_ABZUG);

		if (fachstellename != null) {
			kindContainer.getKindJA().getPensumFachstelle().clear();
			PensumFachstelle defaultPensumFachstelle = TestDataUtil.createDefaultPensumFachstelle(kindContainer.getKindJA());
			requireNonNull(defaultPensumFachstelle.getFachstelle()).setName(fachstellename);
			defaultPensumFachstelle.setIntegrationTyp(integrationTyp);
			kindContainer.getKindJA().getPensumFachstelle().add(defaultPensumFachstelle);
		} else {
			kindContainer.getKindJA().setPensumFachstelle(new HashSet<>());
		}

		gesuch.getKindContainers().add(kindContainer);
		return kindContainer.getKindJA();
	}

	private void clearKinder(Gesuch gesuch) {
		gesuch.getKindContainers().clear();
	}

	private Erwerbspensum createErwerbspensum(Gesuch gesuch, Taetigkeit taetigkeit, boolean gesundheitlicheEinschraenkungen) {
		final ErwerbspensumContainer erwerbspensumContainer = TestDataUtil.createErwerbspensumContainer();
		final Erwerbspensum erwerbspensumJA = requireNonNull(erwerbspensumContainer.getErwerbspensumJA());

		if (gesundheitlicheEinschraenkungen) {
			erwerbspensumJA.setTaetigkeit(Taetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN);
		} else {
			erwerbspensumJA.setTaetigkeit(taetigkeit);
		}
		erwerbspensumJA.getGueltigkeit().setGueltigAb(LocalDate.of(1980, 1, 1));

		final GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();
		gesuchsteller.getGesuchstellerJA().setNachname("Chavez");
		gesuchsteller.getGesuchstellerJA().setVorname("Hugo");

		gesuchsteller.getErwerbspensenContainers().add(erwerbspensumContainer);
		gesuch.setGesuchsteller1(gesuchsteller);

		return erwerbspensumJA;
	}

	private void createFinanzielleSituationGS(int GS, Gesuch gesuch, String vorname, boolean steuerveranlagungErhalten) {
		final FinanzielleSituationContainer finanzielleSituationContainer = TestDataUtil.createFinanzielleSituationContainer();
		final FinanzielleSituation finanzielleSituation = TestDataUtil.createDefaultFinanzielleSituation();

		finanzielleSituation.setSteuerveranlagungErhalten(steuerveranlagungErhalten);
		finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituation);

		final GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();
		gesuchsteller.getGesuchstellerJA().setNachname("Chavez");
		gesuchsteller.getGesuchstellerJA().setVorname(vorname);

		gesuchsteller.setFinanzielleSituationContainer(finanzielleSituationContainer);
		if (GS == 2) {
			gesuch.setGesuchsteller2(gesuchsteller);
		} else {
			gesuch.setGesuchsteller1(gesuchsteller);
		}
	}

	private void createEinkommensverschlechterungGS(int GS, Gesuch gesuch, String vorname) {
		final EinkommensverschlechterungContainer einkommensverschlechterungsContainer = TestDataUtil.createDefaultEinkommensverschlechterungsContainer();
		final Einkommensverschlechterung einkommensverschlechterung1 = TestDataUtil.createDefaultEinkommensverschlechterung();
		final Einkommensverschlechterung einkommensverschlechterung2 = TestDataUtil.createDefaultEinkommensverschlechterung();

		einkommensverschlechterungsContainer.setEkvJABasisJahrPlus1(einkommensverschlechterung1);
		einkommensverschlechterungsContainer.setEkvJABasisJahrPlus2(einkommensverschlechterung2);

		final GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();
		gesuchsteller.getGesuchstellerJA().setNachname("Chavez");
		gesuchsteller.getGesuchstellerJA().setVorname(vorname);

		gesuchsteller.setEinkommensverschlechterungContainer(einkommensverschlechterungsContainer);
		if (GS == 2) {
			gesuch.setGesuchsteller2(gesuchsteller);
		} else {
			gesuch.setGesuchsteller1(gesuchsteller);
		}
	}

	private void createFamilienSituation(Gesuch gesuch, boolean gemeinsam, boolean sozialhilfe) {
		FamiliensituationContainer famSitContainer = TestDataUtil.createDefaultFamiliensituationContainer();

		Familiensituation famSit = requireNonNull(famSitContainer.extractFamiliensituation());
		famSit.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		famSit.setGemeinsameSteuererklaerung(gemeinsam);
		famSit.setSozialhilfeBezueger(sozialhilfe);

		gesuch.setFamiliensituationContainer(famSitContainer);
	}

	@Test
	void kindDokumentFachstelleBernTest() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		clearKinder(testgesuchBern);
		Kind kind = createKind(testgesuchBern, FachstelleName.ERZIEHUNGSBERATUNG, null);

		Assertions.assertTrue(bernKindDokumente.isDokumentNeeded(DokumentTyp.FACHSTELLENBESTAETIGUNG, kind));

		final DokumentGrund dokumentGrund = getDokumentGrund(testgesuchBern);
		Assertions.assertEquals(DokumentTyp.FACHSTELLENBESTAETIGUNG, dokumentGrund.getDokumentTyp());
	}

	@Test
	void kindDokumentFachstelleLuzernTest() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchLuzern);

		clearKinder(testgesuchLuzern);
		Kind kind = createKind(testgesuchLuzern, FachstelleName.ERZIEHUNGSBERATUNG, IntegrationTyp.SOZIALE_INTEGRATION);

		Assertions.assertTrue(luzernKindDokumente.isDokumentNeeded(DokumentTyp.FACHSTELLENBESTAETIGUNG, kind));

		final DokumentGrund dokumentGrund = getDokumentGrund(testgesuchLuzern);
		Assertions.assertEquals(DokumentTyp.FACHSTELLENBESTAETIGUNG, dokumentGrund.getDokumentTyp());
	}

	@Test
	void kindDokumentFachstelleSrachlicheIntegrationLuzernTest() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchLuzern);

		clearKinder(testgesuchLuzern);
		Kind kind = createKind(testgesuchLuzern, FachstelleName.ERZIEHUNGSBERATUNG, IntegrationTyp.SPRACHLICHE_INTEGRATION);

		kind.getPensumFachstelle().forEach(pensumFachstelle -> {
			Assertions.assertFalse(luzernKindDokumente.isDokumentNeeded(DokumentTyp.FACHSTELLENBESTAETIGUNG, kind, pensumFachstelle, LocalDate.MIN));
		});
	}

	@Test
	void kindDokumentAbsageschreibenHortPlatzShouldBeRequiredIfKindHasKeinPlatzHort() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchLuzern);

		clearKinder(testgesuchLuzern);
		Kind kind = createKind(testgesuchLuzern, null, null);
		kind.setKeinPlatzInSchulhort(true);
		kind.setEinschulungTyp(EinschulungTyp.OBLIGATORISCHER_KINDERGARTEN);

		Assertions.assertTrue(luzernKindDokumente.isDokumentNeeded(DokumentTyp.ABSAGESCHREIBEN_HORTPLATZ, kind));

		final DokumentGrund dokumentGrund = getDokumentGrund(testgesuchLuzern);
		Assertions.assertEquals(DokumentTyp.ABSAGESCHREIBEN_HORTPLATZ, dokumentGrund.getDokumentTyp());
	}


	private DokumentGrund getDokumentGrund(Gesuch gesuch) {
		final Set<DokumentGrund> calculate = evaluator.calculate(gesuch, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(1, calculate.size());
		final DokumentGrund dokumentGrund = calculate.iterator().next();
		Assertions.assertEquals(DokumentGrundTyp.KINDER, dokumentGrund.getDokumentGrundTyp());
		Assertions.assertEquals(DokumentGrundPersonType.KIND, dokumentGrund.getPersonType());
		Assertions.assertEquals(Integer.valueOf(-1), dokumentGrund.getPersonNumber()); //-1 is the default value
		return dokumentGrund;
	}

	private DokumentGrund assertDokumentGrund(Erwerbspensum erwerbspensum, Gesuch gesuch) {
		final Set<DokumentGrund> calculate = evaluator.calculate(gesuch, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(1, calculate.size());
		final DokumentGrund dokumentGrund = calculate.iterator().next();
		Assertions.assertEquals(DokumentGrundTyp.ERWERBSPENSUM, dokumentGrund.getDokumentGrundTyp());
		Assertions.assertEquals(DokumentGrundPersonType.GESUCHSTELLER, dokumentGrund.getPersonType());
		Assertions.assertEquals(Integer.valueOf(1), dokumentGrund.getPersonNumber());
		String erwerbspensumName = erwerbspensum.getName(Constants.DEFAULT_LOCALE, gesuch.extractMandant());
		Assertions.assertEquals(erwerbspensumName, dokumentGrund.getTag());
		return dokumentGrund;
	}

	private void assertDokumentGrundCorrect(@Nonnull DokumentGrund dokGrund, @Nonnull String expectedTag, @Nonnull DokumentTyp expectedDokumentTyp) {
		Assertions.assertEquals(DokumentGrundTyp.ERWERBSPENSUM, dokGrund.getDokumentGrundTyp());
		Assertions.assertEquals(DokumentGrundPersonType.GESUCHSTELLER, dokGrund.getPersonType());
		Assertions.assertEquals(Integer.valueOf(1), dokGrund.getPersonNumber());
		Assertions.assertEquals(expectedTag, dokGrund.getTag());
		Assertions.assertEquals(expectedDokumentTyp, dokGrund.getDokumentTyp());
	}

	@Nonnull
	private DokumentGrund extractDocumentFromList(@Nonnull Set<DokumentGrund> dokumentGrundList, @Nonnull DokumentTyp typOfDocumentToExtract) {
		return dokumentGrundList.stream()
			.filter(dg -> dg.getDokumentTyp() == typOfDocumentToExtract)
			.findAny()
			.orElseThrow(() -> new IllegalStateException("Dokument of Type " + typOfDocumentToExtract + " expected"));
	}

	@Test
	void erwpDokumentNeueintrittAfterTest() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchBern, Taetigkeit.ANGESTELLT, false);

		// Nachweis EP ist neu immer zwingend
		Assertions.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum));

		erwerbspensum.getGueltigkeit().setGueltigAb(LocalDate.of(2017, 9, 1));
		Assertions.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum, LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 1)));

		final Set<DokumentGrund> calculate = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		final DokumentGrund dokumentGrund = calculate.iterator().next();

		Assertions.assertEquals(DokumentTyp.NACHWEIS_ERWERBSPENSUM, dokumentGrund.getDokumentTyp());
	}

	@Test
	void erwpDokumentNeueintrittBeforeTest() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchBern, Taetigkeit.ANGESTELLT, false);

		erwerbspensum.getGueltigkeit().setGueltigAb(LocalDate.of(2000, 7, 1));
		// Nachweis Erwerbspensum wird neu immer benötigt
		Assertions.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum, LocalDate.of(2000, 8, 1), LocalDate.of(2000, 8, 1)));

	}

	@Test
	void erwpDokumentSelbstaendigTest() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchBern, Taetigkeit.SELBSTAENDIG, false);

		Assertions.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));
		Assertions.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assertions.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_RAV, erwerbspensum));
		Assertions.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.BESTAETIGUNG_ARZT, erwerbspensum));

		final Set<DokumentGrund> dokumentList = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(1, dokumentList.size());
		DokumentGrund nachweisSelbstaendigkeit = extractDocumentFromList(dokumentList, DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT);
		String erwerbspensumName = erwerbspensum.getName(Constants.DEFAULT_LOCALE, testgesuchBern.extractMandant());
		assertDokumentGrundCorrect(nachweisSelbstaendigkeit, erwerbspensumName, DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT);
	}

	@Test
	void erwpDokumentAusbildung() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchBern, Taetigkeit.AUSBILDUNG, false);

		Assertions.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));
		Assertions.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assertions.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_RAV, erwerbspensum));
		Assertions.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.BESTAETIGUNG_ARZT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrund(erwerbspensum, testgesuchBern);

		Assertions.assertEquals(DokumentTyp.NACHWEIS_AUSBILDUNG, dokumentGrund.getDokumentTyp());
	}

	@Test
	void erwpDokumentRAV() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchBern, Taetigkeit.RAV, false);

		Assertions.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));
		Assertions.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assertions.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_RAV, erwerbspensum));
		Assertions.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.BESTAETIGUNG_ARZT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrund(erwerbspensum, testgesuchBern);

		Assertions.assertEquals(DokumentTyp.NACHWEIS_RAV, dokumentGrund.getDokumentTyp());
	}

	@Test
	void erwpDokumentArzt() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchBern, Taetigkeit.ANGESTELLT, true);

		Assertions.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));
		Assertions.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assertions.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_RAV, erwerbspensum));
		Assertions.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.BESTAETIGUNG_ARZT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrund(erwerbspensum, testgesuchBern);

		Assertions.assertEquals(DokumentTyp.BESTAETIGUNG_ARZT, dokumentGrund.getDokumentTyp());
	}

	@Test
	void erwpDokumentAngestelltLuzern() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchLuzern);

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchLuzern, Taetigkeit.ANGESTELLT, false);

		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ARBEITSSUCHEND, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));
	}

	@Test
	void erwpDokumentArbeitlosLuzern() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchLuzern);

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchLuzern, Taetigkeit.RAV, false);

		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum));
		Assertions.assertTrue(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ARBEITSSUCHEND, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrund(erwerbspensum, testgesuchLuzern);
		Assertions.assertEquals(DokumentTyp.NACHWEIS_ARBEITSSUCHEND, dokumentGrund.getDokumentTyp());
	}

	@Test
	void erwpDokumentInAusbildungLuzern() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchLuzern);

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchLuzern, Taetigkeit.AUSBILDUNG, false);

		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ARBEITSSUCHEND, erwerbspensum));
		Assertions.assertTrue(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrund(erwerbspensum, testgesuchLuzern);
		Assertions.assertEquals(DokumentTyp.NACHWEIS_AUSBILDUNG, dokumentGrund.getDokumentTyp());
	}

	@Test
	void erwpDokumentGesundheitlicheIndikationLuzern() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchLuzern);

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchLuzern,
			Taetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN, false);

		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ARBEITSSUCHEND, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assertions.assertTrue(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrund(erwerbspensum, testgesuchLuzern);
		Assertions.assertEquals(DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, dokumentGrund.getDokumentTyp());
	}

	@Test
	void erwpDokumentSelbststaendigLuzern() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchLuzern);

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchLuzern, Taetigkeit.SELBSTAENDIG, false);

		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ARBEITSSUCHEND, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assertions.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, erwerbspensum));
		Assertions.assertTrue(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrund(erwerbspensum, testgesuchLuzern);
		Assertions.assertEquals(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, dokumentGrund.getDokumentTyp());
	}

	private Set<DokumentGrund> getDokumentGrundsForGS(int gesuchstellerNumber, Set<DokumentGrund> dokumentGrunds) {
		Set<DokumentGrund> grunds = new HashSet<>();

		for (DokumentGrund dokumentGrund : dokumentGrunds) {
			if (dokumentGrund.getPersonType() == DokumentGrundPersonType.GESUCHSTELLER
				&& Objects.equals(dokumentGrund.getPersonNumber(), gesuchstellerNumber)) {
				grunds.add(dokumentGrund);
			}
		}
		return grunds;
	}

	private Set<DokumentGrund> getDokumentGrundsForType(
		DokumentTyp dokumentTyp,
		Set<DokumentGrund> dokumentGrunds,
		@Nullable DokumentGrundPersonType personType,
		@Nullable Integer personNumber,
		@Nullable String year) {

		Set<DokumentGrund> grunds = new HashSet<>();

		for (DokumentGrund dokumentGrund : dokumentGrunds) {

			if (personType != null) {
				if (year != null) {
					if (personType != dokumentGrund.getPersonType() || !Objects.equals(personNumber, dokumentGrund.getPersonNumber())
						|| !year.equals(dokumentGrund.getTag())) {
						continue;
					}
				} else {
					if (personType != dokumentGrund.getPersonType() || !Objects.equals(personNumber, dokumentGrund.getPersonNumber())
						|| dokumentGrund.getTag() != null) {
						continue;
					}
				}
			} else {
				if (dokumentGrund.getPersonType() != null || dokumentGrund.getPersonNumber() != null || dokumentGrund.getTag() != null) {
					continue;
				}
			}

			if (dokumentGrund.getDokumentTyp() == dokumentTyp) {
				grunds.add(dokumentGrund);
				break;
			}

		}
		return grunds;
	}

	@Test
	void finSiSteuerveranlagungGemeinsam() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		createFinanzielleSituationGS(1, testgesuchBern, "Sämi", true);
		createFinanzielleSituationGS(2, testgesuchBern, "Alex", true);

		createFamilienSituation(testgesuchBern, true, false);
		final Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);

		assertThat(dokumentGrunds, contains(
			steuerveranlagungForGesuchsteller(0)
		));
	}

	@Test
	void finSiSteuerveranlagungNichtGemeinsam() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		createFinanzielleSituationGS(1, testgesuchBern, "Sämi", true);
		createFinanzielleSituationGS(2, testgesuchBern, "Alex", true);

		createFamilienSituation(testgesuchBern, false, false);
		Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);

		assertThat(dokumentGrunds, containsInAnyOrder(
			steuerveranlagungForGesuchsteller(1),
			steuerveranlagungForGesuchsteller(2)
		));
	}

	private IsPojo<DokumentGrund> steuerveranlagungForGesuchsteller(int GS) {
		return steuerveranlagungForGesuchsteller(GS, "2016");
	}

	private IsPojo<DokumentGrund> steuerveranlagungForGesuchsteller(int GS, @Nullable String tag) {
		return dokumentOfTyp(DokumentTyp.STEUERVERANLAGUNG)
			.where(DokumentGrund::getDokumentGrundTyp, is(DokumentGrundTyp.FINANZIELLESITUATION))
			.where(DokumentGrund::getPersonType, is(DokumentGrundPersonType.GESUCHSTELLER))
			.where(DokumentGrund::getPersonNumber, is(GS))
			.where(DokumentGrund::getTag, is(tag));
	}

	private IsPojo<DokumentGrund> quellensteuerForGesuchsteller(int GS) {
		return dokumentOfTyp(DokumentTyp.NACHWEIS_ABGERECHNETE_QUELLENSTEUERN)
			.where(DokumentGrund::getDokumentGrundTyp, is(DokumentGrundTyp.FINANZIELLESITUATION))
			.where(DokumentGrund::getPersonType, is(DokumentGrundPersonType.GESUCHSTELLER))
			.where(DokumentGrund::getPersonNumber, is(GS))
			.where(DokumentGrund::getTag, is(nullValue()));
	}

	@Test
	void finSiNichtGemeinsam() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		createFinanzielleSituationGS(1, testgesuchBern, "Sämi", false);
		createFinanzielleSituationGS(2, testgesuchBern, "Alex", false);

		createFamilienSituation(testgesuchBern, false, false);
		final Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(6, dokumentGrunds.size());

		final Set<DokumentGrund> dokumentGrundGS1 = getDokumentGrundsForGS(1, dokumentGrunds);
		Assertions.assertEquals(3, dokumentGrundGS1.size());

		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1());
		assertType(dokumentGrundGS1, DokumentTyp.STEUERERKLAERUNG, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);

		final Set<DokumentGrund> dokumentGrundGS2 = getDokumentGrundsForGS(2, dokumentGrunds);
		Assertions.assertEquals(3, dokumentGrundGS2.size());

		Assertions.assertNotNull(testgesuchBern.getGesuchsteller2());
		assertType(dokumentGrundGS2, DokumentTyp.STEUERERKLAERUNG, testgesuchBern.getGesuchsteller2().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 2, DokumentGrundTyp.FINANZIELLESITUATION);
	}

	private void assertType(
		Set<DokumentGrund> dokumentGrundGS1,
		DokumentTyp dokumentTyp,
		@Nullable String fullname,
		@Nullable String year,
		@Nullable DokumentGrundPersonType personType,
		@Nullable Integer personNumber,
		DokumentGrundTyp dokumentGrundTyp
	) {
		final Set<DokumentGrund> dokumentGrundsForType = getDokumentGrundsForType(dokumentTyp, dokumentGrundGS1, personType, personNumber, year);
		Assertions.assertEquals(
			1,
			dokumentGrundsForType.size(),
			"No document with dokumentGrundTyp: " + dokumentGrundTyp + "; dokumentTyp: " + dokumentTyp + "; fullname: " + fullname + "; year: " + year);

		assertThat(dokumentGrundsForType.iterator().next(), dokumentOfTyp(dokumentTyp)
			.where(DokumentGrund::getDokumentGrundTyp, is(dokumentGrundTyp))
			.where(DokumentGrund::getPersonType, is(personType))
			.where(DokumentGrund::getPersonNumber, is(personNumber)));
	}

	@Test
	void finSiDokumenteTest() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		createFinanzielleSituationGS(1, testgesuchBern, "Sämi", false);
		createFamilienSituation(testgesuchBern, false, false);
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer());
		final FinanzielleSituation finanzielleSituationJA = testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA();

		setAllFinSitJaValue(finanzielleSituationJA);

		//Test wenn Steuererklärung ausgefüllt ist
		Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(11, dokumentGrunds.size());

		Set<DokumentGrund> dokumentGrundGS1 = getDokumentGrundsForGS(1, dokumentGrunds);
		Assertions.assertEquals(11, dokumentGrundGS1.size());

		assertType(dokumentGrundGS1, DokumentTyp.STEUERERKLAERUNG, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.JAHRESLOHNAUSWEISE, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_FAMILIENZULAGEN, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_ERSATZEINKOMMEN, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_ERHALTENE_ALIMENTE, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_GELEISTETE_ALIMENTE, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.ERFOLGSRECHNUNGEN_JAHR, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS1, testgesuchBern.getGesuchsteller1().extractFullName(), "2015",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS2, testgesuchBern.getGesuchsteller1().extractFullName(), "2014",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_VERMOEGEN, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_SCHULDEN, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);

		//Test wenn Steuererklärung nicht ausgefüllt ist
		finanzielleSituationJA.setSteuererklaerungAusgefuellt(false);
		dokumentGrunds = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(8, dokumentGrunds.size());

		dokumentGrundGS1 = getDokumentGrundsForGS(1, dokumentGrunds);
		Assertions.assertEquals(8, dokumentGrundGS1.size());

		assertType(dokumentGrundGS1, DokumentTyp.JAHRESLOHNAUSWEISE, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_FAMILIENZULAGEN, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_ERSATZEINKOMMEN, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_ERHALTENE_ALIMENTE, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_GELEISTETE_ALIMENTE, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.ERFOLGSRECHNUNGEN_JAHR, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS1, testgesuchBern.getGesuchsteller1().extractFullName(), "2015",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS2, testgesuchBern.getGesuchsteller1().extractFullName(), "2014",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
	}

	@Test
	void finSiDokumentSteuerabfrageTest() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		createFinanzielleSituationGS(1, testgesuchBern, "Sämi", false);
		createFamilienSituation(testgesuchBern, false, false);
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer());
		final FinanzielleSituation finanzielleSituationJA =
			testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA();
		setAllFinSitJaValue(finanzielleSituationJA);
		//Normal Fall
		Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(11, dokumentGrunds.size());

		finanzielleSituationJA.setSteuerdatenZugriff(true);
		finanzielleSituationJA.setSteuerdatenAbfrageStatus(SteuerdatenAnfrageStatus.PROVISORISCH);
		//Steuerabfrage Erfolgreich
		dokumentGrunds = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(0, dokumentGrunds.size());

		finanzielleSituationJA.setSteuerdatenAbfrageStatus(SteuerdatenAnfrageStatus.FAILED);
		//Steuerabfrage nicht Erfolgreich
		dokumentGrunds = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(11, dokumentGrunds.size());
	}

	@Test
	void ekvDokumenteTest() {

		createEinkommensverschlechterungGS(1, testgesuchBern, "Sämi");
		createEinkommensverschlechterungGS(2, testgesuchBern, "Alex");
		createEinkommensverschlechterungInfo();
		final Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718();
		gesuchsperiode.setGueltigkeit(new DateRange(LocalDate.of(2016, 8, 1), Constants.GESUCHSPERIODE_17_18_AB));

		testgesuchBern.setGesuchsperiode(gesuchsperiode);
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);
		createFamilienSituation(testgesuchBern, true, false);

		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1().getEinkommensverschlechterungContainer());
		final Einkommensverschlechterung ekvJABasisJahrPlus1 = testgesuchBern.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1();
		ekvJABasisJahrPlus1.setNettolohn(ZEHN_TAUSEND);

		final Einkommensverschlechterung ekvJABasisJahrPlus2 = testgesuchBern.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2();
		ekvJABasisJahrPlus2.setNettolohn(BigDecimal.valueOf(200000));

		Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(8, dokumentGrunds.size()); // Vermoegen wird neu immer gefragt (2 GS x 2 EKVs)

		Set<DokumentGrund> dokumentGrundGS1 = getDokumentGrundsForGS(1, dokumentGrunds);
		Assertions.assertEquals(4, dokumentGrundGS1.size());

		assertType(dokumentGrundGS1, DokumentTyp.JAHRESLOHNAUSWEISE, testgesuchBern.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);

		assertType(dokumentGrundGS1, DokumentTyp.JAHRESLOHNAUSWEISE, testgesuchBern.getGesuchsteller1().extractFullName(), "2017",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);

		Set<DokumentGrund> dokumentGrundGS2 = getDokumentGrundsForGS(2, dokumentGrunds);
		Assertions.assertEquals(4, dokumentGrundGS2.size());

		Assertions.assertNotNull(testgesuchBern.getGesuchsteller2());
		assertType(dokumentGrundGS2, DokumentTyp.JAHRESLOHNAUSWEISE, testgesuchBern.getGesuchsteller2().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 2, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);

		assertType(dokumentGrundGS2, DokumentTyp.JAHRESLOHNAUSWEISE, testgesuchBern.getGesuchsteller2().extractFullName(), "2017",
			DokumentGrundPersonType.GESUCHSTELLER, 2, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);

		testgesuchBern.setFinSitTyp(FinanzielleSituationTyp.SOLOTHURN);

		dokumentGrunds = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(8, dokumentGrunds.size()); //8 wegen 1 EKV (wird nur von einem Jahr verlangt) x 2 GS x (3 Lohnabrechnungen + 1 Vermögen)
		dokumentGrundGS1 = getDokumentGrundsForGS(1, dokumentGrunds);
		Assertions.assertEquals(4, dokumentGrundGS1.size());

		assertTypeForNachweisLohnausweis(dokumentGrundGS1, null, 1);
		assertTypeForNachweisLohnausweis(dokumentGrundGS1, null, 1);

		dokumentGrundGS2 = getDokumentGrundsForGS(2, dokumentGrunds);
		Assertions.assertEquals(4, dokumentGrundGS2.size());
		assertTypeForNachweisLohnausweis(dokumentGrundGS2, null, 2);
		assertTypeForNachweisLohnausweis(dokumentGrundGS2, null, 2);
	}

	private void assertTypeForNachweisLohnausweis(Set<DokumentGrund> dokumentGrunds, @Nullable String year, int gsNumber) {
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller2());
		assertType(dokumentGrunds, DokumentTyp.NACHWEIS_LOHNAUSWEIS_1, gsNumber == 1 ? testgesuchBern.getGesuchsteller1().extractFullName() : testgesuchBern.getGesuchsteller2().extractFullName(), year,
			DokumentGrundPersonType.GESUCHSTELLER, gsNumber, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);
		assertType(dokumentGrunds, DokumentTyp.NACHWEIS_LOHNAUSWEIS_2, gsNumber == 1 ? testgesuchBern.getGesuchsteller1().extractFullName() : testgesuchBern.getGesuchsteller2().extractFullName(), year,
			DokumentGrundPersonType.GESUCHSTELLER, gsNumber, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);
		assertType(dokumentGrunds, DokumentTyp.NACHWEIS_LOHNAUSWEIS_3, gsNumber == 1 ? testgesuchBern.getGesuchsteller1().extractFullName() : testgesuchBern.getGesuchsteller2().extractFullName(), year,
			DokumentGrundPersonType.GESUCHSTELLER, gsNumber, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);

	}

	@Test
	void familiensituationDokumenteTest() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);

		createFamilienSituation(testgesuchBern, true, true);

		Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);

		assertType(dokumentGrunds, DokumentTyp.UNTERSTUETZUNGSBESTAETIGUNG, null, null,
			null, null, DokumentGrundTyp.FINANZIELLESITUATION);
	}

	@Test
	void erwerbspensumDokumenteNotRequiredTest() {
		Einstellung einstellung = createEinstellung(
			EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
			AnspruchBeschaeftigungAbhaengigkeitTyp.UNABHAENGING.name()
		);
		setUpEinstellungMock(testgesuchBern, einstellung);

		createFinanzielleSituationGS(1, testgesuchBern, "Sämi", true);
		createFinanzielleSituationGS(2, testgesuchBern, "Alex", true);

		createFamilienSituation(testgesuchBern, false, false);
		var dokumentGruende = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(2, dokumentGruende.size());
		boolean found = false;
		for (var grund: dokumentGruende) {
			if (grund.getDokumentTyp() == DokumentTyp.NACHWEIS_ERWERBSPENSUM) {
				found = true;
			}
		}
		Assertions.assertFalse(found);
	}

	@Test
	void ersatzeinkommenSelbststaendigkeit_DokumenteRequired_1GS() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);
		createFinanzielleSituationGS(1, testgesuchBern, "Sämi", true);
		createFamilienSituation(testgesuchBern, false, false);

		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer());

		final FinanzielleSituation finanzielleSituationJA =
			testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA();

		finanzielleSituationJA.setGeschaeftsgewinnBasisjahr(ZEHN_TAUSEND);
		finanzielleSituationJA.setGeschaeftsgewinnBasisjahrMinus1(ZEHN_TAUSEND);
		finanzielleSituationJA.setGeschaeftsgewinnBasisjahrMinus2(ZEHN_TAUSEND);
		finanzielleSituationJA.setErsatzeinkommenSelbststaendigkeitBasisjahr(ZEHN_TAUSEND);
		finanzielleSituationJA.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(ZEHN_TAUSEND);
		finanzielleSituationJA.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus2(ZEHN_TAUSEND);

		var dokumentGruende = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(4, dokumentGruende.size());
		assertType(
			dokumentGruende,
			DokumentTyp.NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR,
			testgesuchBern.getGesuchsteller1().extractFullName(),
			"2016",
			DokumentGrundPersonType.GESUCHSTELLER,
			1,
			DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(
			dokumentGruende,
			DokumentTyp.NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS1,
			testgesuchBern.getGesuchsteller1().extractFullName(),
			"2015",
			DokumentGrundPersonType.GESUCHSTELLER,
			1,
			DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(
			dokumentGruende,
			DokumentTyp.NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS2,
			testgesuchBern.getGesuchsteller1().extractFullName(),
			"2014",
			DokumentGrundPersonType.GESUCHSTELLER,
			1,
			DokumentGrundTyp.FINANZIELLESITUATION);
	}

	@Test
	void ersatzeinkommenSelbststaendigkeit_DokumenteRequired_2GS() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);
		createFinanzielleSituationGS(1, testgesuchBern, "Sämi", true);
		createFinanzielleSituationGS(2, testgesuchBern, "Alex", true);
		createFamilienSituation(testgesuchBern, false, false);

		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller2());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller2().getFinanzielleSituationContainer());

		testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setErsatzeinkommenSelbststaendigkeitBasisjahr(ZEHN_TAUSEND);
		testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setGeschaeftsgewinnBasisjahr(ZEHN_TAUSEND);

		testgesuchBern.getGesuchsteller2().getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(ZEHN_TAUSEND);
		testgesuchBern.getGesuchsteller2().getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setGeschaeftsgewinnBasisjahrMinus1(ZEHN_TAUSEND);

		var dokumentGruende = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(4, dokumentGruende.size());
		assertType(
			dokumentGruende,
			DokumentTyp.NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR,
			testgesuchBern.getGesuchsteller1().extractFullName(),
			"2016",
			DokumentGrundPersonType.GESUCHSTELLER,
			1,
			DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(
			dokumentGruende,
			DokumentTyp.NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS1,
			testgesuchBern.getGesuchsteller2().extractFullName(),
			"2015",
			DokumentGrundPersonType.GESUCHSTELLER,
			2,
			DokumentGrundTyp.FINANZIELLESITUATION);
	}

	@Test
	void ersatzeinkommenSelbststaendigkeit_DokumenteNotRequiredWhenNoGeschaeftsGewinn() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);
		createFinanzielleSituationGS(1, testgesuchBern, "Sämi", true);
		createFinanzielleSituationGS(2, testgesuchBern, "Alex", true);
		createFamilienSituation(testgesuchBern, false, false);

		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller2());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller2().getFinanzielleSituationContainer());


		testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer()
			.getFinanzielleSituationJA().setErsatzeinkommenSelbststaendigkeitBasisjahr(BigDecimal.ZERO);

		var dokumentGruende = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		var dokumentGruendeErsatzeinkommen = getDokumentGrundsForType(
			DokumentTyp.NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR,
			dokumentGruende, DokumentGrundPersonType.GESUCHSTELLER, 1, "2016");
		Assertions.assertTrue(dokumentGruendeErsatzeinkommen.isEmpty());
	}

	@Test
	void nachweisEkInVerinfachtemVerfahren_DokumentRequiredWhenVeranlagt() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);
		createFinanzielleSituationGS(1, testgesuchBern, "Sämi", true);
		createFamilienSituation(testgesuchBern, false, false);

		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer());

		testgesuchBern.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer()
			.getFinanzielleSituationJA().setEinkommenInVereinfachtemVerfahrenAbgerechnet(true);
		testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer()
			.getFinanzielleSituationJA().setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(ZEHN_TAUSEND);

		var dokumentGruende = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		assertType(dokumentGruende, DokumentTyp.NACHWEIS_EINKOMMEN_VERFAHREN, testgesuchBern.getGesuchsteller1().extractFullName(),
			"2016", DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
	}

	@Test
	void nachweisEkInVerinfachtemVerfahren_DokumenteRequiredWhenNotVeranlagt() {
		setupEinstellungBeschaeftigungsPensumAbhaengig(testgesuchBern);
		createFinanzielleSituationGS(1, testgesuchBern, "Sämi", false);
		createFamilienSituation(testgesuchBern, false, false);

		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1());
		Assertions.assertNotNull(testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer());

		testgesuchBern.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer()
			.getFinanzielleSituationJA().setEinkommenInVereinfachtemVerfahrenAbgerechnet(true);
		testgesuchBern.getGesuchsteller1().getFinanzielleSituationContainer()
			.getFinanzielleSituationJA().setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(ZEHN_TAUSEND);

		var dokumentGruende = evaluator.calculate(testgesuchBern, Constants.DEFAULT_LOCALE);
		assertType(dokumentGruende, DokumentTyp.NACHWEIS_EINKOMMEN_VERFAHREN, testgesuchBern.getGesuchsteller1().extractFullName(),
			"2016", DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
	}

	private void createEinkommensverschlechterungInfo() {
		final EinkommensverschlechterungInfoContainer einkommensverschlechterungsInfo = TestDataUtil.createDefaultEinkommensverschlechterungsInfoContainer(
			testgesuchBern);
		einkommensverschlechterungsInfo.getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus1(true);
		einkommensverschlechterungsInfo.getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(true);
	}

	private void setAllFinSitJaValue(FinanzielleSituation finanzielleSituationJA) {
		finanzielleSituationJA.setFamilienzulage(ZEHN_TAUSEND);
		finanzielleSituationJA.setErsatzeinkommen(ZEHN_TAUSEND);
		finanzielleSituationJA.setErhalteneAlimente(ZEHN_TAUSEND);
		finanzielleSituationJA.setGeleisteteAlimente(ZEHN_TAUSEND);
		finanzielleSituationJA.setBruttovermoegen(ZEHN_TAUSEND);
		finanzielleSituationJA.setSchulden(ZEHN_TAUSEND);
		finanzielleSituationJA.setGeschaeftsgewinnBasisjahr(ZEHN_TAUSEND);
		finanzielleSituationJA.setGeschaeftsgewinnBasisjahrMinus1(ZEHN_TAUSEND);
		finanzielleSituationJA.setGeschaeftsgewinnBasisjahrMinus2(ZEHN_TAUSEND);
	}

	@SuppressWarnings("EmptyClass")
	@Nested
	class SchwyzTest {

		private void setUpFamiliensituationAlleine(Gesuch testGesuch) {
			final FamiliensituationContainer familiensituationContainer = new FamiliensituationContainer();
			final Familiensituation familiensituationJA = new Familiensituation();
			familiensituationJA.setSozialhilfeBezueger(false);
			familiensituationJA.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
			familiensituationJA.setGemeinsameSteuererklaerung(false);
			familiensituationContainer.setFamiliensituationJA(familiensituationJA);
			testGesuch.setFamiliensituationContainer(familiensituationContainer);
		}

		private void setUpFamiliensituationZuZweit(boolean gemeinsameFinSit, Gesuch testGesuch) {
			final FamiliensituationContainer familiensituationContainer = new FamiliensituationContainer();
			final Familiensituation familiensituationJA = new Familiensituation();
			familiensituationJA.setSozialhilfeBezueger(false);
			familiensituationJA.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
			familiensituationJA.setGemeinsameSteuererklaerung(gemeinsameFinSit);
			familiensituationContainer.setFamiliensituationJA(familiensituationJA);
			testGesuch.setFamiliensituationContainer(familiensituationContainer);
		}

		@Nested
		class ErwebspensumDokumenteTest {

			@Test
			void erwerbspensumRAV_shouldOnlyHaveNachweisRAV() {
				Set<DokumentGrund> dokumentGruende = test(Taetigkeit.RAV);

				assertThat(dokumentGruende, contains(
					dokumentOfTyp(DokumentTyp.NACHWEIS_RAV).where(DokumentGrund::getPersonNumber, is(1))
				));
			}

			@Test
			void erwerbspensumAngestellt_shouldOnlyHaveNachweisAngestellt() {
				Set<DokumentGrund> dokumentGruende = test(Taetigkeit.ANGESTELLT);

				assertThat(dokumentGruende, contains(
					dokumentOfTyp(DokumentTyp.NACHWEIS_ERWERBSPENSUM)
				));
			}

			@Test
			void erwerbspensumInAusbildung_shouldOnlyHaveNachweisInAusbildung() {
				Set<DokumentGrund> dokumentGruende = test(Taetigkeit.AUSBILDUNG);

				assertThat(dokumentGruende, contains(
					dokumentOfTyp(DokumentTyp.NACHWEIS_AUSBILDUNG)
				));
			}

			@Test
			void erwerbspensumSelbststaendig_shouldOnlyHaveNachweisSelbststaendig() {
				Set<DokumentGrund> dokumentGruende = test(Taetigkeit.SELBSTAENDIG);

				assertThat(dokumentGruende, contains(
					dokumentOfTyp(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT)
				));
			}

			private Set<DokumentGrund> test(Taetigkeit taetigkeit) {
				Gesuch gesuch = setUpGesuchForErwerbspensumTest(taetigkeit);
				setupEinstellungBeschaeftigungsPensumAbhaengig(gesuch);

				return evaluator.calculate(gesuch, Constants.DEFAULT_LOCALE);
			}

			private Gesuch setUpGesuchForErwerbspensumTest(Taetigkeit rav) {
				Gesuch gesuch = setUpTestgesuch(TestDataUtil.getMandantSchwyz(), FinanzielleSituationTyp.SCHWYZ);

				GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();
				gesuch.setGesuchsteller1(gesuchsteller);

				ErwerbspensumContainer erwerbspensumContainer = createErwerbspensumContainer(rav, gesuchsteller, gesuch);
				gesuchsteller.setErwerbspensenContainers(Set.of(erwerbspensumContainer));

				return gesuch;
			}

			private ErwerbspensumContainer createErwerbspensumContainer(
				Taetigkeit taetigkeit,
				GesuchstellerContainer gesuchstellerContainer,
				Gesuch gesuch
			) {
				final Erwerbspensum erwerbspensum = createErwerbspensum(gesuch, taetigkeit, false);
				final ErwerbspensumContainer erwerbspensumContainer = new ErwerbspensumContainer();
				erwerbspensumContainer.setErwerbspensumJA(erwerbspensum);
				erwerbspensumContainer.setGesuchsteller(gesuchstellerContainer);

				return erwerbspensumContainer;
			}
		}

		@Nested
		class EinkommensverschlechterungDokumenteTest {

			@Test
			void noEKVInfoContainer_shouldNotRequireAnyNachweis() {
				Gesuch testGesuch = setUpGesuchForEKV();
				setUpFamiliensituationAlleine(testGesuch);
				testGesuch.setEinkommensverschlechterungInfoContainer(null);

				final Set<DokumentGrund> dokumentGruende = evaluator.calculate(testGesuch, Constants.DEFAULT_LOCALE);

				assertThat(dokumentGruende.size(), is(0));
			}

			@Test
			void ekvInfoContainerNoEkv_shouldNotRequireAnyNachweis() {
				Gesuch testGesuch = setUpGesuchForEKV();
				setUpFamiliensituationZuZweit(false, testGesuch);
				setUpEkvInfoContainer(false, testGesuch);

				final Set<DokumentGrund> dokumentGruende = evaluator.calculate(testGesuch, Constants.DEFAULT_LOCALE);

				assertThat(dokumentGruende.size(), is(0));
			}

			@Test
			void oneGSNoEKVContainer_ShouldNotRequireAnyNachweis() {
				Gesuch testGesuch = setUpGesuchForEKV();
				setUpEkvInfoContainer(true, testGesuch);
				Objects.requireNonNull(testGesuch.getGesuchsteller1());
				testGesuch.getGesuchsteller1().setEinkommensverschlechterungContainer(null);

				final Set<DokumentGrund> dokumentGruende = evaluator.calculate(testGesuch, Constants.DEFAULT_LOCALE);

				assertThat(dokumentGruende.size(), is(0));
			}

			@Test
			void ekvOneGSQuellbesteuertBruttolohn_ShouldRequireNachweisBruttolohn() {
				Gesuch testGesuch = setUpGesuchForEKV();
				setUpFamiliensituationAlleine(testGesuch);
				setUpEkvInfoContainer(true, testGesuch);
				final Einkommensverschlechterung ekv = new Einkommensverschlechterung();
				ekv.setBruttoLohn(BigDecimal.valueOf(120000));
				setUpEkvGS(ekv, testGesuch.getGesuchsteller1());

				final Set<DokumentGrund> dokumentGruende = evaluator.calculate(testGesuch, Constants.DEFAULT_LOCALE);
				final DokumentGrund dokumentGrund = dokumentGruende.stream().findFirst().orElse(new DokumentGrund());

				assertThat(dokumentGruende.size(), is(1));
				assertThat(dokumentGrund.getDokumentTyp(), is(DokumentTyp.NACHWEIS_BRUTTOLOHN));
			}

			@Test
			void ekvOneGSNotQuellbesteuertBruttolohn_ShouldRequireNachweiseForAllFields() {
				Gesuch testGesuch = setUpGesuchForEKV();
				setUpFamiliensituationAlleine(testGesuch);
				setUpEkvInfoContainer(true, testGesuch);
				final Einkommensverschlechterung ekv = new Einkommensverschlechterung();
				ekv.setSteuerbaresEinkommen(BigDecimal.valueOf(120000));
				ekv.setSteuerbaresVermoegen(BigDecimal.valueOf(100000));
				ekv.setEinkaeufeVorsorge(BigDecimal.valueOf(0));
				ekv.setAbzuegeLiegenschaft(BigDecimal.valueOf(0));
				setUpEkvGS(ekv, testGesuch.getGesuchsteller1());

				final Set<DokumentGrund> dokumentGruende = evaluator.calculate(testGesuch, Constants.DEFAULT_LOCALE);

				assertThat(dokumentGruende.size(), is(4));
				List.of(
						DokumentTyp.NACHWEIS_NETTOLOHN,
						DokumentTyp.NACHWEIS_EINKAEUFE_VORSORGE,
						DokumentTyp.NACHWEIS_ABZUEGE_LIEGENSCHAFT,
						DokumentTyp.NACHWEIS_VERMOEGEN)
					.forEach(dokumentTyp -> {
						var gruendeOfTyp = dokumentGruende.stream()
							.filter(dokumentGrund -> dokumentGrund.getDokumentTyp() == dokumentTyp)
							.collect(
								Collectors.toList());
						assertThat(gruendeOfTyp.size(), is(1));
					});
			}

			@Test
			void ekvTwoGSQuellbesteuertBruttolohn_ShouldRequireEachNachweisPerGS() {
				Gesuch testGesuch = setUpGesuchForEKV();
				setUpFamiliensituationZuZweit(false, testGesuch);
				setUpEkvInfoContainer(true, testGesuch);
				testGesuch.setGesuchsteller2(new GesuchstellerContainer());
				setUpEkvGS(setUpEkvQuellbesteuert(), testGesuch.getGesuchsteller1());
				setUpEkvGS(setUpEkvQuellbesteuert(), testGesuch.getGesuchsteller2());

				final Set<DokumentGrund> dokumentGruende = evaluator.calculate(testGesuch, Constants.DEFAULT_LOCALE);
				final DokumentGrund dokumentGrundGS1 = dokumentGruende.stream()
					.filter(dokumentGrund -> dokumentGrund.getPersonNumber() != null)
					.filter(dokumentGrund -> dokumentGrund.getPersonNumber() == 1)
					.findFirst()
					.orElse(new DokumentGrund());
				final DokumentGrund dokumentGrundGS2 = dokumentGruende.stream()
					.filter(dokumentGrund -> dokumentGrund.getPersonNumber() != null)
					.filter(dokumentGrund -> dokumentGrund.getPersonNumber() == 2)
					.findFirst()
					.orElse(new DokumentGrund());

				assertThat(dokumentGruende.size(), is(2));
				assertThat(dokumentGrundGS1.getDokumentTyp(), is(DokumentTyp.NACHWEIS_BRUTTOLOHN));
				assertThat(dokumentGrundGS2.getDokumentTyp(), is(DokumentTyp.NACHWEIS_BRUTTOLOHN));
			}

			private Gesuch setUpGesuchForEKV() {
				Gesuch gesuchToCreate = setUpTestgesuch(TestDataUtil.getMandantSchwyz(), FinanzielleSituationTyp.SCHWYZ);
				gesuchToCreate.setGesuchsteller1(TestDataUtil.createDefaultGesuchstellerContainer());
				setupEinstellungBeschaeftigungsPensumAbhaengig(gesuchToCreate);

				return gesuchToCreate;
			}

			@Nonnull
			private Einkommensverschlechterung setUpEkvQuellbesteuert() {
				final Einkommensverschlechterung ekvGS = new Einkommensverschlechterung();
				ekvGS.setBruttoLohn(BigDecimal.valueOf(10000));
				return ekvGS;
			}

			private void setUpEkvInfoContainer(boolean einkommensverschlechterung, Gesuch testGesuch) {
				final EinkommensverschlechterungInfoContainer ekvInfoContainer = new EinkommensverschlechterungInfoContainer();
				final EinkommensverschlechterungInfo ekvInfoJA = new EinkommensverschlechterungInfo();
				ekvInfoJA.setEkvFuerBasisJahrPlus1(true);
				ekvInfoJA.setEinkommensverschlechterung(einkommensverschlechterung);
				ekvInfoContainer.setEinkommensverschlechterungInfoJA(ekvInfoJA);
				testGesuch.setEinkommensverschlechterungInfoContainer(ekvInfoContainer);
			}

			private void setUpEkvGS(Einkommensverschlechterung ekv, @Nullable GesuchstellerContainer gesuchsteller) {
				final EinkommensverschlechterungContainer ekvContainer = new EinkommensverschlechterungContainer();
				ekvContainer.setEkvJABasisJahrPlus1(ekv);
				Objects.requireNonNull(gesuchsteller);
				gesuchsteller.setEinkommensverschlechterungContainer(ekvContainer);
			}

		}

		@Nested
		class FinSitDokumenteTest {

			@Nested
			class WhenSingleGesuchsteller {

				@Test
				void verheirated_veranlagt() {
					var dokumente = runSingleGesuchsteller(
						SchwyzTestfallDataProvider::createVerheiratet,
						SchwzyTestData::createFinanzielleSituationVeranlagt
					);

					assertThat(dokumente, contains(
						steuerveranlagungForGesuchsteller(0, null)
					));
				}

				@Test
				void alleinerziehend_veranlagt() {
					var dokumente = runSingleGesuchsteller(
						SchwyzTestfallDataProvider::createAlleinerziehend,
						SchwzyTestData::createFinanzielleSituationVeranlagt
					);

					assertThat(dokumente, contains(
						steuerveranlagungForGesuchsteller(1, null)
					));
				}

				@Test
				void alleinerziehend_quellenbesteuert() {
					var dokumente = runSingleGesuchsteller(
						SchwyzTestfallDataProvider::createAlleinerziehend,
						SchwzyTestData::createFinanzielleSituationQuellenbesteuert
					);

					assertThat(dokumente, contains(
						quellensteuerForGesuchsteller(1)
					));
				}

				Set<DokumentGrund> runSingleGesuchsteller(
					Function<SchwzyTestData, Familiensituation> famSit,
					Function<SchwzyTestData, FinanzielleSituation> finSit
				) {
					var gesuch = setUpTestgesuch(TestDataUtil.getMandantSchwyz(), FinanzielleSituationTyp.SCHWYZ);
					var schwyzTestData = new SchwzyTestData(gesuch.getGesuchsperiode());

					var gesuchsteller = createGesuchsteller(finSit.apply(schwyzTestData));
					gesuch.setGesuchsteller1(gesuchsteller);

					addFamiliensituation(famSit.apply(schwyzTestData), gesuch);

					setupEinstellungBeschaeftigungsPensumAbhaengig(gesuch);

					return evaluator.calculate(gesuch, Constants.DEFAULT_LOCALE);
				}
			}

			@Nested
			class WhenMultipleGesuchsteller {

				@Test
				void gemeinsamVeranlagt() {
					var dokumente = runGemeinsameSteuererklaerung(
						SchwzyTestData::createFinanzielleSituationVeranlagt
					);

					assertThat(dokumente, contains(
						steuerveranlagungForGesuchsteller(0, null)
					));
				}

				@Test
				void beideQuellenbesteuert() {
					var dokumente = runInviduelleSteuererklaerungen(
						SchwzyTestData::createFinanzielleSituationQuellenbesteuert,
						SchwzyTestData::createFinanzielleSituationQuellenbesteuert
					);

					assertThat(dokumente, containsInAnyOrder(
						quellensteuerForGesuchsteller(1),
						quellensteuerForGesuchsteller(2)
					));
				}

				@Test
				void mixed() {
					var dokumentGrunds = runInviduelleSteuererklaerungen(
						SchwzyTestData::createFinanzielleSituationQuellenbesteuert,
						SchwzyTestData::createFinanzielleSituationVeranlagt
					);

					assertThat(dokumentGrunds, containsInAnyOrder(
						quellensteuerForGesuchsteller(1),
						steuerveranlagungForGesuchsteller(2, null)
					));
				}

				Set<DokumentGrund> runGemeinsameSteuererklaerung(
					Function<SchwzyTestData, FinanzielleSituation> finSitGs
				) {
					return runMultipleGesuchsteller(true, finSitGs, a -> Optional.empty());
				}

				Set<DokumentGrund> runInviduelleSteuererklaerungen(
					Function<SchwzyTestData, FinanzielleSituation> finSitGs1,
					Function<SchwzyTestData, FinanzielleSituation> finSitGs2
				) {
					return runMultipleGesuchsteller(false, finSitGs1, finSitGs2.andThen(Optional::of));
				}

				Set<DokumentGrund> runMultipleGesuchsteller(
					boolean gemeinsameSteuererklaerung,
					Function<SchwzyTestData, FinanzielleSituation> finSitGs1,
					Function<SchwzyTestData, Optional<FinanzielleSituation>> finSitGs2
				) {
					var gesuch = setUpTestgesuch(TestDataUtil.getMandantSchwyz(), FinanzielleSituationTyp.SCHWYZ);
					var schwyzTestData = new SchwzyTestData(gesuch.getGesuchsperiode());

					gesuch.setGesuchsteller1(createGesuchsteller(finSitGs1.apply(schwyzTestData)));

					GesuchstellerContainer gesuchsteller2 = finSitGs2.apply(schwyzTestData)
						.map(FinSitDokumenteTest.this::createGesuchsteller)
						.orElseGet(TestDataUtil::createDefaultGesuchstellerContainer);
					gesuch.setGesuchsteller2(gesuchsteller2);

					// nur in diesem Fall darf es 2 Gesuchsteller geben (Verheirated / Konkubinat  ist ebenbürtig)
					Familiensituation verheiratet = schwyzTestData.createVerheiratet();
					verheiratet.setGemeinsameSteuererklaerung(gemeinsameSteuererklaerung);
					addFamiliensituation(verheiratet, gesuch);

					setupEinstellungBeschaeftigungsPensumAbhaengig(gesuch);

					return evaluator.calculate(gesuch, Constants.DEFAULT_LOCALE);
				}
			}

			private void addFamiliensituation(Familiensituation familiensituation, Gesuch gesuch) {
				FamiliensituationContainer famSitContainer = new FamiliensituationContainer();
				famSitContainer.setFamiliensituationJA(familiensituation);

				gesuch.setFamiliensituationContainer(famSitContainer);
			}

			class SchwzyTestData extends SchwyzTestfallDataProvider {

				protected SchwzyTestData(Gesuchsperiode gesuchsperiode) {
					super(gesuchsperiode);
				}

				FinanzielleSituation createFinanzielleSituationQuellenbesteuert() {
					FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
					finanzielleSituation.setQuellenbesteuert(true);
					finanzielleSituation.setBruttoLohn(BigDecimal.valueOf(100_000));

					return finanzielleSituation;
				}

				FinanzielleSituation createFinanzielleSituationVeranlagt() {
					return createFinanzielleSituation(BigDecimal.valueOf(123_456), BigDecimal.valueOf(88_000));
				}
			}

			GesuchstellerContainer createGesuchsteller(FinanzielleSituation finSit) {
				GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();

				FinanzielleSituationContainer finSitContainer = new FinanzielleSituationContainer();
				finSitContainer.setFinanzielleSituationJA(finSit);
				gesuchsteller.setFinanzielleSituationContainer(finSitContainer);

				return gesuchsteller;
			}
		}
	}

	private IsPojo<DokumentGrund> dokumentOfTyp(DokumentTyp typ) {
		return pojo(DokumentGrund.class)
			.where(DokumentGrund::getDokumentTyp, is(typ));
	}
}
