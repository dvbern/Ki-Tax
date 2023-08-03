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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
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
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;

/**
 * Tests für die Regeln der Benötigten Dokumenten
 */
@ExtendWith(EasyMockExtension.class)
public class DokumentenverzeichnisEvaluatorTest extends EasyMockSupport {

	private final BigDecimal ZEHN_TAUSEND = BigDecimal.valueOf(100000);

	@TestSubject
	private final DokumentenverzeichnisEvaluator evaluator = new DokumentenverzeichnisEvaluator();

	@Mock
	private EinstellungService einstellungServiceMock;

	private final BernKindDokumente bernKindDokumente = new BernKindDokumente();
	private final LuzernKindDokumente luzernKindDokumente = new LuzernKindDokumente();
	private final BernErwerbspensumDokumente bernErwerbspensumDokumente = new BernErwerbspensumDokumente();
	private final LuzernErwerbspensumDokumente luzernErwerbspensumDokumente = new LuzernErwerbspensumDokumente();
	private Gesuch testgesuch;
	private Gesuch testgesuchLuzern;
	private Mandant mandantBern;
	private Mandant mandantLuzern;

	@BeforeEach
	public void setUpCalculator() {
		testgesuch = new Gesuch();
		testgesuchLuzern = new Gesuch();
		mandantBern = TestDataUtil.getMandantKantonBern();
		mandantLuzern = TestDataUtil.getMandantLuzern();

		setUpTestgesuch(testgesuch, mandantBern, FinanzielleSituationTyp.BERN);
		setUpTestgesuch(testgesuchLuzern, mandantLuzern, FinanzielleSituationTyp.LUZERN);
	}

	private void setUpTestgesuch(Gesuch gesuch, Mandant mandant, FinanzielleSituationTyp finanzielleSituationTyp) {
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
	}

	private void setUpEinstellungMock(@Nonnull Gesuch testgesuch, @Nonnull String anspruchUnabhaengig) {
		var einstellung = new Einstellung();
		einstellung.setValue(anspruchUnabhaengig);
		expect(einstellungServiceMock.findEinstellung(EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
			testgesuch.extractGemeinde(), testgesuch.getGesuchsperiode()))
			.andReturn(einstellung)
			.anyTimes();
		replayAll();
	}

	private Kind createKind(Gesuch gesuch, String vorname, Kinderabzug ganzerAbzug, FachstelleName fachstellename, IntegrationTyp integrationTyp) {
		final KindContainer kindContainer = TestDataUtil.createDefaultKindContainer();
		kindContainer.getKindJA().setNachname("Chavez");
		kindContainer.getKindJA().setVorname(vorname);
		kindContainer.getKindJA().setKinderabzugErstesHalbjahr(ganzerAbzug);
		kindContainer.getKindJA().setKinderabzugZweitesHalbjahr(ganzerAbzug);

		if (fachstellename != null) {
			kindContainer.getKindJA().getPensumFachstelle().clear();
			final PensumFachstelle defaultPensumFachstelle = TestDataUtil.createDefaultPensumFachstelle();
			assert defaultPensumFachstelle.getFachstelle() != null;
			defaultPensumFachstelle.getFachstelle().setName(fachstellename);
			defaultPensumFachstelle.setIntegrationTyp(integrationTyp);
			kindContainer.getKindJA().getPensumFachstelle().add(defaultPensumFachstelle);
		} else {
			kindContainer.getKindJA().setPensumFachstelle(new HashSet());
		}

		gesuch.getKindContainers().add(kindContainer);
		return kindContainer.getKindJA();
	}

	private void clearKinder(Gesuch gesuch) {
		gesuch.getKindContainers().clear();
	}

	private Erwerbspensum createErwerbspensum(
		Gesuch gesuch, String vorname, Taetigkeit taetigkeit,
		boolean gesundheitlicheEinschraenkungen) {
		final ErwerbspensumContainer erwerbspensumContainer = TestDataUtil.createErwerbspensumContainer();

		final Erwerbspensum erwerbspensumJA = erwerbspensumContainer.getErwerbspensumJA();
		Assert.assertNotNull(erwerbspensumJA);
		if (gesundheitlicheEinschraenkungen) {
			erwerbspensumJA.setTaetigkeit(Taetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN);
		} else {
			erwerbspensumJA.setTaetigkeit(taetigkeit);
		}
		erwerbspensumJA.getGueltigkeit().setGueltigAb(LocalDate.of(1980, 1, 1));

		final GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();
		gesuchsteller.getGesuchstellerJA().setNachname("Chavez");
		gesuchsteller.getGesuchstellerJA().setVorname(vorname);

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

	private void createEinkommensverschlechterungGS(int GS, Gesuch gesuch, String vorname, boolean steuerveranlagungErhalten) {
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
		final FamiliensituationContainer famSitContainer = TestDataUtil.createDefaultFamiliensituationContainer();
		Familiensituation famSit = famSitContainer.extractFamiliensituation();
		Objects.requireNonNull(famSit).setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Assert.assertNotNull(famSit);
		famSit.setGemeinsameSteuererklaerung(gemeinsam);
		famSit.setSozialhilfeBezueger(sozialhilfe);
		gesuch.setFamiliensituationContainer(famSitContainer);
	}

	@Test
	public void kindDokumentFachstelleBernTest() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		clearKinder(testgesuch);
		final String kindName = "Jan";
		Kind kind = createKind(testgesuch, kindName, Kinderabzug.GANZER_ABZUG, FachstelleName.ERZIEHUNGSBERATUNG, null);

		Assert.assertTrue(bernKindDokumente.isDokumentNeeded(DokumentTyp.FACHSTELLENBESTAETIGUNG, kind));

		final DokumentGrund dokumentGrund = getDokumentGrund(testgesuch);
		Assert.assertEquals(DokumentTyp.FACHSTELLENBESTAETIGUNG, dokumentGrund.getDokumentTyp());
	}

	@Test
	public void kindDokumentFachstelleLuzernTest() {
		setUpEinstellungMock(testgesuchLuzern, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		clearKinder(testgesuchLuzern);
		final String kindName = "Jan";
		Kind kind = createKind(testgesuchLuzern, kindName, Kinderabzug.GANZER_ABZUG, FachstelleName.ERZIEHUNGSBERATUNG, IntegrationTyp.SOZIALE_INTEGRATION);

		Assert.assertTrue(luzernKindDokumente.isDokumentNeeded(DokumentTyp.FACHSTELLENBESTAETIGUNG, kind));

		final DokumentGrund dokumentGrund = getDokumentGrund(testgesuchLuzern);
		Assert.assertEquals(DokumentTyp.FACHSTELLENBESTAETIGUNG, dokumentGrund.getDokumentTyp());
	}

	@Test
	public void kindDokumentFachstelleSrachlicheIntegrationLuzernTest() {
		setUpEinstellungMock(testgesuchLuzern, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		clearKinder(testgesuchLuzern);
		final String kindName = "Jan";
		Kind kind = createKind(testgesuchLuzern, kindName, Kinderabzug.GANZER_ABZUG, FachstelleName.ERZIEHUNGSBERATUNG, IntegrationTyp.SPRACHLICHE_INTEGRATION);

		kind.getPensumFachstelle().forEach(pensumFachstelle -> {
			Assert.assertFalse(luzernKindDokumente.isDokumentNeeded(DokumentTyp.FACHSTELLENBESTAETIGUNG, kind, pensumFachstelle, LocalDate.MIN));
		});
	}

	@Test
	public void kindDokumentAbsageschreibenHortPlatzShouldBeRequiredIfKindHasKeinPlatzHort() {
		setUpEinstellungMock(testgesuchLuzern, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		clearKinder(testgesuchLuzern);
		final String kindName = "Jan";
		Kind kind = createKind(testgesuchLuzern, kindName, Kinderabzug.GANZER_ABZUG, null, null);
		kind.setKeinPlatzInSchulhort(true);
		kind.setEinschulungTyp(EinschulungTyp.OBLIGATORISCHER_KINDERGARTEN);

		Assert.assertTrue(luzernKindDokumente.isDokumentNeeded(DokumentTyp.ABSAGESCHREIBEN_HORTPLATZ, kind));

		final DokumentGrund dokumentGrund = getDokumentGrund(testgesuchLuzern);
		Assert.assertEquals(DokumentTyp.ABSAGESCHREIBEN_HORTPLATZ, dokumentGrund.getDokumentTyp());
	}


	private DokumentGrund getDokumentGrund(Gesuch gesuch) {
		final Set<DokumentGrund> calculate = evaluator.calculate(gesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(1, calculate.size());
		final DokumentGrund dokumentGrund = calculate.iterator().next();
		Assert.assertEquals(DokumentGrundTyp.KINDER, dokumentGrund.getDokumentGrundTyp());
		Assert.assertEquals(DokumentGrundPersonType.KIND, dokumentGrund.getPersonType());
		Assert.assertEquals(Integer.valueOf(-1), dokumentGrund.getPersonNumber()); //-1 is the default value
		return dokumentGrund;
	}

	private DokumentGrund assertDokumentGrundBern(Erwerbspensum erwerbspensum) {
		return assertDokumentGrund(erwerbspensum, testgesuch, mandantBern);
	}

	private DokumentGrund assertDokumentGrundLuzern(Erwerbspensum erwerbspensum) {
		return assertDokumentGrund(erwerbspensum, testgesuchLuzern, mandantLuzern);
	}

	private DokumentGrund assertDokumentGrund(Erwerbspensum erwerbspensum, Gesuch gesuch, Mandant mandant) {
		final Set<DokumentGrund> calculate = evaluator.calculate(gesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(1, calculate.size());
		final DokumentGrund dokumentGrund = calculate.iterator().next();
		Assert.assertEquals(DokumentGrundTyp.ERWERBSPENSUM, dokumentGrund.getDokumentGrundTyp());
		Assert.assertEquals(DokumentGrundPersonType.GESUCHSTELLER, dokumentGrund.getPersonType());
		Assert.assertEquals(Integer.valueOf(1), dokumentGrund.getPersonNumber());
		Assert.assertEquals(erwerbspensum.getName(Constants.DEFAULT_LOCALE, mandant), dokumentGrund.getTag());
		return dokumentGrund;
	}

	private void assertDokumentGrundCorrect(@Nonnull DokumentGrund dokGrund, @Nonnull String expectedTag, @Nonnull DokumentTyp expectedDokumentTyp) {
		Assert.assertEquals(DokumentGrundTyp.ERWERBSPENSUM, dokGrund.getDokumentGrundTyp());
		Assert.assertEquals(DokumentGrundPersonType.GESUCHSTELLER, dokGrund.getPersonType());
		Assert.assertEquals(Integer.valueOf(1), dokGrund.getPersonNumber());
		Assert.assertEquals(expectedTag, dokGrund.getTag());
		Assert.assertEquals(expectedDokumentTyp, dokGrund.getDokumentTyp());
	}

	@Nonnull
	private DokumentGrund extractDocumentFromList(@Nonnull Set<DokumentGrund> dokumentGrundList, @Nonnull DokumentTyp typOfDocumentToExtract) {
		return dokumentGrundList.stream()
			.filter(dg -> dg.getDokumentTyp() == typOfDocumentToExtract)
			.findFirst()
			.orElseThrow(() -> {
				Assert.fail("Dokument of Type " + typOfDocumentToExtract + " expected");
				return new RuntimeException();
			});
	}

	@Test
	public void erwpDokumentNeueintrittAfterTest() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuch, "Hugo", Taetigkeit.ANGESTELLT, false);

		// Nachweis EP ist neu immer zwingend
		Assert.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum));

		erwerbspensum.getGueltigkeit().setGueltigAb(LocalDate.of(2017, 9, 1));
		Assert.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum, LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 1)));

		final Set<DokumentGrund> calculate = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		final DokumentGrund dokumentGrund = calculate.iterator().next();

		Assert.assertEquals(DokumentTyp.NACHWEIS_ERWERBSPENSUM, dokumentGrund.getDokumentTyp());
	}

	@Test
	public void erwpDokumentNeueintrittBeforeTest() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuch, "Hugo", Taetigkeit.ANGESTELLT, false);

		erwerbspensum.getGueltigkeit().setGueltigAb(LocalDate.of(2000, 7, 1));
		// Nachweis Erwerbspensum wird neu immer benötigt
		Assert.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum, LocalDate.of(2000, 8, 1), LocalDate.of(2000, 8, 1)));

	}

	@Test
	public void erwpDokumentSelbstaendigTest() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuch, "Hugo", Taetigkeit.SELBSTAENDIG, false);

		Assert.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));
		Assert.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assert.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_RAV, erwerbspensum));
		Assert.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.BESTAETIGUNG_ARZT, erwerbspensum));

		final Set<DokumentGrund> dokumentList = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(1, dokumentList.size());
		DokumentGrund nachweisSelbstaendigkeit = extractDocumentFromList(dokumentList, DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT);
		assertDokumentGrundCorrect(nachweisSelbstaendigkeit, erwerbspensum.getName(Constants.DEFAULT_LOCALE,
				mandantBern),
			DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT);
	}

	@Test
	public void erwpDokumentAusbildung() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuch, "Hugo", Taetigkeit.AUSBILDUNG, false);

		Assert.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));
		Assert.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assert.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_RAV, erwerbspensum));
		Assert.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.BESTAETIGUNG_ARZT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrundBern(erwerbspensum);

		Assert.assertEquals(DokumentTyp.NACHWEIS_AUSBILDUNG, dokumentGrund.getDokumentTyp());
	}

	@Test
	public void erwpDokumentRAV() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuch, "Hugo", Taetigkeit.RAV, false);

		Assert.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));
		Assert.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assert.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_RAV, erwerbspensum));
		Assert.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.BESTAETIGUNG_ARZT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrundBern(erwerbspensum);

		Assert.assertEquals(DokumentTyp.NACHWEIS_RAV, dokumentGrund.getDokumentTyp());
	}

	@Test
	public void erwpDokumentArzt() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuch, "Hugo", Taetigkeit.ANGESTELLT, true);

		Assert.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));
		Assert.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assert.assertFalse(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_RAV, erwerbspensum));
		Assert.assertTrue(bernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.BESTAETIGUNG_ARZT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrundBern(erwerbspensum);

		Assert.assertEquals(DokumentTyp.BESTAETIGUNG_ARZT, dokumentGrund.getDokumentTyp());
	}

	@Test
	public void erwpDokumentAngestelltLuzern() {
		setUpEinstellungMock(testgesuchLuzern, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchLuzern, "Hugo", Taetigkeit.ANGESTELLT, false);

		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ARBEITSSUCHEND, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));
	}

	@Test
	public void erwpDokumentArbeitlosLuzern() {
		setUpEinstellungMock(testgesuchLuzern, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchLuzern, "Hugo", Taetigkeit.RAV, false);

		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum));
		Assert.assertTrue(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ARBEITSSUCHEND, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrundLuzern(erwerbspensum);
		Assert.assertEquals(DokumentTyp.NACHWEIS_ARBEITSSUCHEND, dokumentGrund.getDokumentTyp());
	}

	@Test
	public void erwpDokumentInAusbildungLuzern() {
		setUpEinstellungMock(testgesuchLuzern, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchLuzern, "Hugo", Taetigkeit.AUSBILDUNG, false);

		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ARBEITSSUCHEND, erwerbspensum));
		Assert.assertTrue(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrundLuzern(erwerbspensum);
		Assert.assertEquals(DokumentTyp.NACHWEIS_AUSBILDUNG, dokumentGrund.getDokumentTyp());
	}

	@Test
	public void erwpDokumentGesundheitlicheIndikationLuzern() {
		setUpEinstellungMock(testgesuchLuzern, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchLuzern, "Hugo", Taetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN, false);

		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ARBEITSSUCHEND, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assert.assertTrue(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrundLuzern(erwerbspensum);
		Assert.assertEquals(DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, dokumentGrund.getDokumentTyp());
	}

	@Test
	public void erwpDokumentSelbststaendigLuzern() {
		setUpEinstellungMock(testgesuchLuzern, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		final Erwerbspensum erwerbspensum = createErwerbspensum(testgesuchLuzern, "Hugo", Taetigkeit.SELBSTAENDIG, false);

		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ERWERBSPENSUM, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_ARBEITSSUCHEND, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_AUSBILDUNG, erwerbspensum));
		Assert.assertFalse(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, erwerbspensum));
		Assert.assertTrue(luzernErwerbspensumDokumente.isDokumentNeeded(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, erwerbspensum));

		final DokumentGrund dokumentGrund = assertDokumentGrundLuzern(erwerbspensum);
		Assert.assertEquals(DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, dokumentGrund.getDokumentTyp());
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

	private Set<DokumentGrund> getDokumentGrundsForType(DokumentTyp dokumentTyp, Set<DokumentGrund> dokumentGrunds,
		@Nullable DokumentGrundPersonType personType, @Nullable Integer personNumber, @Nullable String year) {
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
	public void finSiSteuerveranlagungGemeinsam() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		createFinanzielleSituationGS(1, testgesuch, "Sämi", true);
		createFinanzielleSituationGS(2, testgesuch, "Alex", true);

		createFamilienSituation(testgesuch, true, false);
		final Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(1, dokumentGrunds.size());

		final DokumentGrund dokumentGrund = dokumentGrunds.iterator().next();
		Assert.assertEquals(DokumentGrundTyp.FINANZIELLESITUATION, dokumentGrund.getDokumentGrundTyp());
		Assert.assertEquals(DokumentTyp.STEUERVERANLAGUNG, dokumentGrund.getDokumentTyp());
	}

	@Test
	public void finSiSteuerveranlagungNichtGemeinsam() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		createFinanzielleSituationGS(1, testgesuch, "Sämi", true);
		createFinanzielleSituationGS(2, testgesuch, "Alex", true);

		createFamilienSituation(testgesuch, false, false);
		final Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(2, dokumentGrunds.size());

		final Set<DokumentGrund> dokumentGrundGS1 = getDokumentGrundsForGS(1, dokumentGrunds);

		assertGundDokumente(dokumentGrundGS1);

		final Set<DokumentGrund> dokumentGrundGS2 = getDokumentGrundsForGS(2, dokumentGrunds);
		Assert.assertNotNull(testgesuch.getGesuchsteller2());
		assertType(dokumentGrundGS2, DokumentTyp.STEUERVERANLAGUNG, testgesuch.getGesuchsteller2().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 2, DokumentGrundTyp.FINANZIELLESITUATION);
	}

	private void assertGundDokumente(Set<DokumentGrund> dokumentGrundGS1) {
		Assert.assertNotNull(testgesuch.getGesuchsteller1());
		assertType(dokumentGrundGS1, DokumentTyp.STEUERVERANLAGUNG, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
	}

	@Test
	public void finSiNichtGemeinsam() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		createFinanzielleSituationGS(1, testgesuch, "Sämi", false);
		createFinanzielleSituationGS(2, testgesuch, "Alex", false);

		createFamilienSituation(testgesuch, false, false);
		final Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(4, dokumentGrunds.size());

		final Set<DokumentGrund> dokumentGrundGS1 = getDokumentGrundsForGS(1, dokumentGrunds);
		Assert.assertEquals(2, dokumentGrundGS1.size());

		Assert.assertNotNull(testgesuch.getGesuchsteller1());
		assertType(dokumentGrundGS1, DokumentTyp.STEUERERKLAERUNG, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);

		final Set<DokumentGrund> dokumentGrundGS2 = getDokumentGrundsForGS(2, dokumentGrunds);
		Assert.assertEquals(2, dokumentGrundGS2.size());

		Assert.assertNotNull(testgesuch.getGesuchsteller2());
		assertType(dokumentGrundGS2, DokumentTyp.STEUERERKLAERUNG, testgesuch.getGesuchsteller2().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 2, DokumentGrundTyp.FINANZIELLESITUATION);
	}

	private void assertType(Set<DokumentGrund> dokumentGrundGS1, DokumentTyp dokumentTyp, @Nullable String fullname, @Nullable String year,
		@Nullable DokumentGrundPersonType personType, @Nullable Integer personNumber, DokumentGrundTyp dokumentGrundTyp) {
		final Set<DokumentGrund> dokumentGrundsForType = getDokumentGrundsForType(dokumentTyp, dokumentGrundGS1, personType, personNumber, year);
		Assert.assertEquals("No document with dokumentGrundTyp: " + dokumentGrundTyp + "; dokumentTyp: " + dokumentTyp + "; fullname: " + fullname + "; year: " + year,
			1, dokumentGrundsForType.size());
		final DokumentGrund dokumentGrund = dokumentGrundsForType.iterator().next();
		Assert.assertEquals(personType, dokumentGrund.getPersonType());
		Assert.assertEquals(personNumber, dokumentGrund.getPersonNumber());
		Assert.assertEquals(dokumentTyp, dokumentGrund.getDokumentTyp());

	}

	@Test
	public void finSiDokumenteTest() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		createFinanzielleSituationGS(1, testgesuch, "Sämi", false);
		createFamilienSituation(testgesuch, false, false);
		Assert.assertNotNull(testgesuch.getGesuchsteller1());
		Assert.assertNotNull(testgesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		final FinanzielleSituation finanzielleSituationJA = testgesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA();

		setAllFinSitJaValue(finanzielleSituationJA);

		//Test wenn Steuererklärung ausgefüllt ist
		Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(9, dokumentGrunds.size());

		Set<DokumentGrund> dokumentGrundGS1 = getDokumentGrundsForGS(1, dokumentGrunds);
		Assert.assertEquals(9, dokumentGrundGS1.size());

		assertType(dokumentGrundGS1, DokumentTyp.STEUERERKLAERUNG, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.JAHRESLOHNAUSWEISE, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_FAMILIENZULAGEN, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_ERSATZEINKOMMEN, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_ERHALTENE_ALIMENTE, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_GELEISTETE_ALIMENTE, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.ERFOLGSRECHNUNGEN_JAHR, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS1, testgesuch.getGesuchsteller1().extractFullName(), "2015",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS2, testgesuch.getGesuchsteller1().extractFullName(), "2014",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);

		//Test wenn Steuererklärung nicht ausgefüllt ist
		finanzielleSituationJA.setSteuererklaerungAusgefuellt(false);
		dokumentGrunds = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(10, dokumentGrunds.size());

		dokumentGrundGS1 = getDokumentGrundsForGS(1, dokumentGrunds);
		Assert.assertEquals(10, dokumentGrundGS1.size());

		assertType(dokumentGrundGS1, DokumentTyp.JAHRESLOHNAUSWEISE, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_FAMILIENZULAGEN, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_ERSATZEINKOMMEN, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_ERHALTENE_ALIMENTE, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_GELEISTETE_ALIMENTE, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_VERMOEGEN, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.NACHWEIS_SCHULDEN, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.ERFOLGSRECHNUNGEN_JAHR, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS1, testgesuch.getGesuchsteller1().extractFullName(), "2015",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
		assertType(dokumentGrundGS1, DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS2, testgesuch.getGesuchsteller1().extractFullName(), "2014",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.FINANZIELLESITUATION);
	}

	@Test
	public void finSiDokumentSteuerabfrageTest() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		createFinanzielleSituationGS(1, testgesuch, "Sämi", false);
		createFamilienSituation(testgesuch, false, false);
		Assert.assertNotNull(testgesuch.getGesuchsteller1());
		Assert.assertNotNull(testgesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		final FinanzielleSituation finanzielleSituationJA =
			testgesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA();
		setAllFinSitJaValue(finanzielleSituationJA);
		//Normal Fall
		Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(9, dokumentGrunds.size());

		finanzielleSituationJA.setSteuerdatenZugriff(true);
		finanzielleSituationJA.setSteuerdatenAbfrageStatus(SteuerdatenAnfrageStatus.PROVISORISCH);
		//Steuerabfrage Erfolgreich
		dokumentGrunds = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(0, dokumentGrunds.size());

		finanzielleSituationJA.setSteuerdatenAbfrageStatus(SteuerdatenAnfrageStatus.FAILED);
		//Steuerabfrage nicht Erfolgreich
		dokumentGrunds = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(9, dokumentGrunds.size());
	}

	@Test
	public void ekvDokumenteTest() {

		createEinkommensverschlechterungGS(1, testgesuch, "Sämi", false);
		createEinkommensverschlechterungGS(2, testgesuch, "Alex", false);
		createEinkommensverschlechterungInfo();
		final Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718();
		gesuchsperiode.setGueltigkeit(new DateRange(LocalDate.of(2016, 8, 1), Constants.GESUCHSPERIODE_17_18_AB));

		testgesuch.setGesuchsperiode(gesuchsperiode);
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());
		createFamilienSituation(testgesuch, true, false);

		Assert.assertNotNull(testgesuch.getGesuchsteller1());
		Assert.assertNotNull(testgesuch.getGesuchsteller1().getEinkommensverschlechterungContainer());
		final Einkommensverschlechterung ekvJABasisJahrPlus1 = testgesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1();
		ekvJABasisJahrPlus1.setNettolohn(ZEHN_TAUSEND);

		final Einkommensverschlechterung ekvJABasisJahrPlus2 = testgesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2();
		ekvJABasisJahrPlus2.setNettolohn(BigDecimal.valueOf(200000));

		Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(8, dokumentGrunds.size()); // Vermoegen wird neu immer gefragt (2 GS x 2 EKVs)

		Set<DokumentGrund> dokumentGrundGS1 = getDokumentGrundsForGS(1, dokumentGrunds);
		Assert.assertEquals(4, dokumentGrundGS1.size());

		assertType(dokumentGrundGS1, DokumentTyp.JAHRESLOHNAUSWEISE, testgesuch.getGesuchsteller1().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);

		assertType(dokumentGrundGS1, DokumentTyp.JAHRESLOHNAUSWEISE, testgesuch.getGesuchsteller1().extractFullName(), "2017",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);

		Set<DokumentGrund> dokumentGrundGS2 = getDokumentGrundsForGS(2, dokumentGrunds);
		Assert.assertEquals(4, dokumentGrundGS2.size());

		Assert.assertNotNull(testgesuch.getGesuchsteller2());
		assertType(dokumentGrundGS2, DokumentTyp.JAHRESLOHNAUSWEISE, testgesuch.getGesuchsteller2().extractFullName(), "2016",
			DokumentGrundPersonType.GESUCHSTELLER, 2, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);

		assertType(dokumentGrundGS2, DokumentTyp.JAHRESLOHNAUSWEISE, testgesuch.getGesuchsteller2().extractFullName(), "2017",
			DokumentGrundPersonType.GESUCHSTELLER, 2, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);

		testgesuch.setFinSitTyp(FinanzielleSituationTyp.SOLOTHURN);

		dokumentGrunds = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		Assert.assertEquals(8, dokumentGrunds.size()); //8 wegen 1 EKV (wird nur von einem Jahr verlangt) x 2 GS x (3 Lohnabrechnungen + 1 Vermögen)
		dokumentGrundGS1 = getDokumentGrundsForGS(1, dokumentGrunds);
		Assert.assertEquals(4, dokumentGrundGS1.size());

		assertTypeForNachweisLohnausweis(dokumentGrundGS1, null, 1);
		assertTypeForNachweisLohnausweis(dokumentGrundGS1, null, 1);

		dokumentGrundGS2 = getDokumentGrundsForGS(2, dokumentGrunds);
		Assert.assertEquals(4, dokumentGrundGS2.size());
		assertTypeForNachweisLohnausweis(dokumentGrundGS2, null, 2);
		assertTypeForNachweisLohnausweis(dokumentGrundGS2, null, 2);
	}

	private void assertTypeForNachweisLohnausweis(Set<DokumentGrund> dokumentGrunds, String year, int gsNumber) {
		Assert.assertNotNull(testgesuch.getGesuchsteller1());
		Assert.assertNotNull(testgesuch.getGesuchsteller2());
		assertType(dokumentGrunds, DokumentTyp.NACHWEIS_LOHNAUSWEIS_1, gsNumber == 1 ? testgesuch.getGesuchsteller1().extractFullName() : testgesuch.getGesuchsteller2().extractFullName(), year,
			DokumentGrundPersonType.GESUCHSTELLER, gsNumber, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);
		assertType(dokumentGrunds, DokumentTyp.NACHWEIS_LOHNAUSWEIS_2, gsNumber == 1 ? testgesuch.getGesuchsteller1().extractFullName() : testgesuch.getGesuchsteller2().extractFullName(), year,
			DokumentGrundPersonType.GESUCHSTELLER, gsNumber, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);
		assertType(dokumentGrunds, DokumentTyp.NACHWEIS_LOHNAUSWEIS_3, gsNumber == 1 ? testgesuch.getGesuchsteller1().extractFullName() : testgesuch.getGesuchsteller2().extractFullName(), year,
			DokumentGrundPersonType.GESUCHSTELLER, gsNumber, DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);

	}

	@Test
	public void familiensituationDokumenteTest() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name());

		createFamilienSituation(testgesuch, true, true);

		Set<DokumentGrund> dokumentGrunds = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);

		assertType(dokumentGrunds, DokumentTyp.UNTERSTUETZUNGSBESTAETIGUNG, null, null,
			null, null, DokumentGrundTyp.FINANZIELLESITUATION);
	}

	@Test
	public void erwerbspensumDokumenteNotRequiredTest() {
		setUpEinstellungMock(testgesuch, AnspruchBeschaeftigungAbhaengigkeitTyp.UNABHAENGING.name());

		createFinanzielleSituationGS(1, testgesuch, "Sämi", true);
		createFinanzielleSituationGS(2, testgesuch, "Alex", true);

		createFamilienSituation(testgesuch, false, false);
		var dokumentGruende = evaluator.calculate(testgesuch, Constants.DEFAULT_LOCALE);
		Assertions.assertEquals(2, dokumentGruende.size());
		boolean found = false;
		for (var grund: dokumentGruende) {
			if (grund.getDokumentTyp().equals(DokumentTyp.NACHWEIS_ERWERBSPENSUM)) {
				found = true;
			}
		}
		Assertions.assertFalse(found);
	}

	private void createEinkommensverschlechterungInfo() {
		final EinkommensverschlechterungInfoContainer einkommensverschlechterungsInfo = TestDataUtil.createDefaultEinkommensverschlechterungsInfoContainer(testgesuch);
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
}
