package ch.dvbern.ebegu.rules;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(EasyMockRunner.class)
public class WohnsitzCalcRuleTest {

	DateRange range = new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.START_PERIODE);
	private Supplier<GesuchService> gesuchServiceSupplier;

	@TestSubject
	WohnsitzCalcRule wohnsitzCalcRule = new WohnsitzCalcRule(range, Locale.GERMAN, gesuchServiceSupplier);


	@Before
	public void setUp() {
		GesuchService gesuchService= EasyMock.createMock(GesuchService.class);
		EasyMock.expect(gesuchService.getAllGesuchForFallAndGesuchsperiodeInUnterschiedlichenGemeinden(
				EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject())).andReturn(populateGesuchsliste());

		EasyMock.replay(gesuchService);
		this.gesuchServiceSupplier = () ->  gesuchService;

	}

	@After
	public void tearDown() {
	}


	@Test
	public void testExecuteRule() {
		DateRange dateRange = new DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2022, 1, 1));
		assertNotNull(wohnsitzCalcRule);
		BGCalculationInput inputData = prepareInputData(dateRange);
		wohnsitzCalcRule.executeRule(preparePlatz(), inputData);
		assertNotNull(inputData);
	}

	@Test
	public void testExecuteRuleWithDossierservice() throws IOException {
		DateRange dateRange = new DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2022, 1, 1));
		BGCalculationInput inputData = prepareInputData(dateRange);
		inputData.setWohnsitzNichtInGemeindeGS1(true);
		inputData.setPotentielleDoppelBetreuung(true);
		assertNotNull(inputData.getParent());
		assertTrue(inputData.getParent().getBemerkungenDTOList().isEmpty());
		wohnsitzCalcRule = new WohnsitzCalcRule(range, Locale.GERMAN, gesuchServiceSupplier);
		assertNotNull(wohnsitzCalcRule);
		wohnsitzCalcRule.executeRule(preparePlatz(), inputData);
		assertFalse(inputData.getParent().getBemerkungenDTOList().isEmpty());
	}

	@Test
	public void testExecuteRuleWithDossierserviceNichtInGemeindeFalse() throws IOException {
		DateRange dateRange = new DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2022, 1, 1));
		BGCalculationInput inputData = prepareInputData(dateRange);
		inputData.setWohnsitzNichtInGemeindeGS1(false);
		inputData.setPotentielleDoppelBetreuung(true);
		assertNotNull(inputData.getParent());
		assertTrue(inputData.getParent().getBemerkungenDTOList().isEmpty());
		wohnsitzCalcRule = new WohnsitzCalcRule(range, Locale.GERMAN, gesuchServiceSupplier);
		assertNotNull(wohnsitzCalcRule);
		wohnsitzCalcRule.executeRule(preparePlatz(), inputData);
		assertFalse(inputData.getParent().getBemerkungenDTOList().isEmpty());
	}

	@Test
	public void testExecuteRuleWithDossierEmptyList() throws IOException {
		DateRange dateRange = new DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2022, 1, 1));
		BGCalculationInput inputData = prepareInputData(dateRange);
		inputData.setPotentielleDoppelBetreuung(true);
		try{
			wohnsitzCalcRule.executeRule(preparePlatz(), inputData);
		} catch (NullPointerException expected){
			//ignore
		}
	}

	private BGCalculationInput prepareInputData(DateRange dateRange) {
		return new BGCalculationInput(new VerfuegungZeitabschnitt(dateRange), RuleValidity.ASIV);
	}

	private AbstractPlatz preparePlatz() {
		AbstractPlatz betreuungMock  = EasyMock.createMock(Betreuung.class);
		Gesuch gesuchMock = EasyMock.createMock(Gesuch.class);
		Dossier dossierMock = EasyMock.createMock(Dossier.class);
		Gemeinde gemeindeMock = EasyMock.createMock(Gemeinde.class);
		Gesuchsperiode gesuchsPeriodeMock = EasyMock.createMock(Gesuchsperiode.class);
		KindContainer kindContainerMock = EasyMock.createMock(KindContainer.class);
		Fall fallMock = EasyMock.createMock(Fall.class);
		Kind kind = EasyMock.createMock(Kind.class);
		InstitutionStammdaten institutionStammdaten = EasyMock.createMock(InstitutionStammdaten.class);
		Institution institution = EasyMock.createMock(Institution.class);

		EasyMock.expect(gemeindeMock.getName()).andReturn("TEST_GEMAINDE_32");
		EasyMock.expect(dossierMock.getGemeinde()).andReturn(gemeindeMock);
		EasyMock.expect(dossierMock.getFall()).andReturn(fallMock);
		EasyMock.expect(fallMock.getFallNummer()).andReturn(5007L);
		EasyMock.expect(gesuchMock.getDossier()).andReturn(dossierMock);
		EasyMock.expect(gesuchMock.getFall()).andReturn(fallMock);
		EasyMock.expect(betreuungMock.extractGesuchsperiode()).andReturn(gesuchsPeriodeMock).anyTimes();
		EasyMock.expect(betreuungMock.extractGesuch()).andReturn(gesuchMock).anyTimes();
		EasyMock.expect(betreuungMock.extractGemeinde()).andReturn(gemeindeMock).anyTimes();
		EasyMock.expect(betreuungMock.getKind()).andReturn(kindContainerMock).anyTimes();
		EasyMock.expect(betreuungMock.getInstitutionStammdaten()).andReturn(institutionStammdaten).anyTimes();
		Verfuegung verfuegung = EasyMock.createMock(Verfuegung.class);
		EasyMock.expect(betreuungMock.getVerfuegung()).andReturn(verfuegung).anyTimes();

		EasyMock.expect(institutionStammdaten.getInstitution()).andReturn(institution).anyTimes();
		EasyMock.expect(institution.getId()).andReturn("veryUniqueID").anyTimes();
		EasyMock.expect(kindContainerMock.getGesuch()).andReturn(gesuchMock);
		EasyMock.expect(kindContainerMock.getKindJA()).andReturn(kind).anyTimes();
		EasyMock.expect(kind.getNachname()).andReturn("Tester").anyTimes();
		EasyMock.expect(kind.getVorname()).andReturn("hans-ueli").anyTimes();
		EasyMock.expect(kind.getGeburtsdatum()).andReturn(LocalDate.of(2028, 3, 7)).anyTimes();
		EasyMock.replay(verfuegung);
		EasyMock.replay(betreuungMock);
		EasyMock.replay(gesuchMock);
		EasyMock.replay(gemeindeMock);
		EasyMock.replay(dossierMock);
		EasyMock.replay(kindContainerMock);
		EasyMock.replay(fallMock);
		EasyMock.replay(gesuchsPeriodeMock);
		EasyMock.replay(kind);
		EasyMock.replay(institution);
		EasyMock.replay(institutionStammdaten);
		return betreuungMock;
	}

	private AbstractPlatz prepareZweiPlaetzeEinerVerfuegt() {
		AbstractPlatz betreuungMock  = EasyMock.createMock(Betreuung.class);
		AbstractPlatz betreuungMock2  = EasyMock.createMock(Betreuung.class);

		Gesuch gesuchMock = EasyMock.createMock(Gesuch.class);
		Dossier dossierMock = EasyMock.createMock(Dossier.class);
		Gemeinde gemeindeMock = EasyMock.createMock(Gemeinde.class);
		Gesuchsperiode gesuchsPeriodeMock = EasyMock.createMock(Gesuchsperiode.class);
		KindContainer kindContainerMock = EasyMock.createMock(KindContainer.class);
		Fall fallMock = EasyMock.createMock(Fall.class);
		Kind kind = EasyMock.createMock(Kind.class);
		InstitutionStammdaten institutionStammdaten = EasyMock.createMock(InstitutionStammdaten.class);
		Institution institution = EasyMock.createMock(Institution.class);
		Verfuegung verfuegungBetreuung1Mock = EasyMock.createMock(Verfuegung.class);

		EasyMock.expect(gemeindeMock.getName()).andReturn("TEST_GEMAINDE_32");
		EasyMock.expect(dossierMock.getGemeinde()).andReturn(gemeindeMock);
		EasyMock.expect(dossierMock.getFall()).andReturn(fallMock);
		EasyMock.expect(fallMock.getFallNummer()).andReturn(5007L);
		EasyMock.expect(gesuchMock.getDossier()).andReturn(dossierMock);
		EasyMock.expect(gesuchMock.getFall()).andReturn(fallMock);
		EasyMock.expect(betreuungMock.extractGesuchsperiode()).andReturn(gesuchsPeriodeMock).anyTimes();
		EasyMock.expect(betreuungMock.extractGesuch()).andReturn(gesuchMock).anyTimes();
		EasyMock.expect(betreuungMock.extractGemeinde()).andReturn(gemeindeMock).anyTimes();
		EasyMock.expect(betreuungMock.getKind()).andReturn(kindContainerMock).anyTimes();
		EasyMock.expect(betreuungMock.getInstitutionStammdaten()).andReturn(institutionStammdaten).anyTimes();
		EasyMock.expect(betreuungMock.getVerfuegung()).andReturn(verfuegungBetreuung1Mock).anyTimes();

		EasyMock.expect(betreuungMock2.extractGesuchsperiode()).andReturn(gesuchsPeriodeMock).anyTimes();
		EasyMock.expect(betreuungMock2.extractGesuch()).andReturn(gesuchMock).anyTimes();
		EasyMock.expect(betreuungMock2.extractGemeinde()).andReturn(gemeindeMock).anyTimes();
		EasyMock.expect(betreuungMock2.getKind()).andReturn(kindContainerMock).anyTimes();
		EasyMock.expect(betreuungMock2.getInstitutionStammdaten()).andReturn(institutionStammdaten).anyTimes();
		EasyMock.expect(betreuungMock2.getVerfuegung()).andReturn(null);

		EasyMock.expect(institutionStammdaten.getInstitution()).andReturn(institution).anyTimes();
		EasyMock.expect(institution.getId()).andReturn("veryUniqueID").anyTimes();
		EasyMock.expect(kindContainerMock.getGesuch()).andReturn(gesuchMock);
		EasyMock.expect(kindContainerMock.getKindJA()).andReturn(kind).anyTimes();
		EasyMock.expect(kind.getNachname()).andReturn("Tester").anyTimes();
		EasyMock.expect(kind.getVorname()).andReturn("hans-ueli").anyTimes();
		EasyMock.expect(kind.getGeburtsdatum()).andReturn(LocalDate.of(2028, 3, 7)).anyTimes();

		EasyMock.replay(verfuegungBetreuung1Mock);
		EasyMock.replay(betreuungMock);
		EasyMock.replay(betreuungMock2);
		EasyMock.replay(gesuchMock);
		EasyMock.replay(gemeindeMock);
		EasyMock.replay(dossierMock);
		EasyMock.replay(kindContainerMock);
		EasyMock.replay(fallMock);
		EasyMock.replay(gesuchsPeriodeMock);
		EasyMock.replay(kind);
		EasyMock.replay(institution);
		EasyMock.replay(institutionStammdaten);
		return betreuungMock;
	}

	@Test
	public void testZuzug() {
		DateRange dateRange = new DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2022, 1, 1));
		wohnsitzCalcRule= new WohnsitzCalcRule(range, Locale.GERMAN, gesuchServiceSupplier);
		Betreuung betreuung = (Betreuung) prepareZweiPlaetzeEinerVerfuegt();
		BGCalculationInput bgCalculationInput = prepareInputData(dateRange);
		bgCalculationInput.setPotentielleDoppelBetreuung(true);
		assertFalse(bgCalculationInput.isAnspruchSinktDuringMonat());
		wohnsitzCalcRule.executeRule(betreuung, bgCalculationInput);
		assertTrue(bgCalculationInput.isAnspruchSinktDuringMonat());
		assertEquals(bgCalculationInput.getAnspruchspensumProzent(), 0);

	}

	private List<Gesuch> populateGesuchsliste() {
		Kind kindMock = EasyMock.createMock(Kind.class);
		Gesuch gesuch1 = EasyMock.createMock(Gesuch.class);
		Gesuch gesuch2 = EasyMock.createMock(Gesuch.class);
		KindContainer kindContainerMock = EasyMock.createMock(KindContainer.class);
		List<Gesuch>  gesuchListe = new LinkedList<>();
		Set<KindContainer> kinderListe = new HashSet<>();
		kinderListe.add(kindContainerMock);
		EasyMock.expect(kindContainerMock.getKindJA()).andReturn(kindMock);
		EasyMock.expect(gesuch1.getKindContainers()).andReturn(kinderListe);
		EasyMock.expect(gesuch2.getKindContainers()).andReturn(kinderListe);
		Betreuung betreuung = (Betreuung) preparePlatz();


		Set<Betreuung> betreungsList = new HashSet<>();
		betreungsList.add(betreuung);
		EasyMock.expect(kindContainerMock.getBetreuungen()).andReturn(betreungsList).anyTimes();


		EasyMock.replay(gesuch1);
		EasyMock.replay((gesuch2));
		EasyMock.replay(kindContainerMock);
		EasyMock.replay(kindMock);
		gesuchListe.add(gesuch1);
		gesuchListe.add(gesuch2);

		return gesuchListe;
	}

	@Test
	public void testWegzug() {
		LocalDate wegzugsDatum = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.OCTOBER, 16);
		Betreuung betreuung = createTestdata_withZweiGesuchsteller();
		DateRange dateRange = new DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2022, 1, 1));
		BGCalculationInput inputData = prepareInputData(dateRange);

		final Gesuch gesuch = betreuung.extractGesuch();

		gesuch.getGesuchsteller1().addAdresse(createGesuchstellerAdresse(
				TestDataUtil.START_PERIODE,
				wegzugsDatum.minusDays(1),
				false,
				gesuch.getGesuchsteller1()));
		gesuch.getGesuchsteller1().addAdresse(createGesuchstellerAdresse(
				wegzugsDatum,
				TestDataUtil.ENDE_PERIODE,
				true,
				gesuch.getGesuchsteller1()));

		gesuch.getGesuchsteller2().addAdresse(createGesuchstellerAdresse(
				TestDataUtil.START_PERIODE,
				TestDataUtil.ENDE_PERIODE,
				true,
				gesuch.getGesuchsteller2()));

		createDossier(gesuch);

		WohnsitzCalcRule rule = new WohnsitzCalcRule(dateRange, Locale.GERMAN);
		rule.executeRule(betreuung, inputData);


	}

	private Betreuung createTestdata_withZweiGesuchsteller() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(true);
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		betreuung.setBetreuungspensumContainers(new LinkedHashSet<>());
		betreuung.setVerfuegung(new Verfuegung());
		LinkedList<VerfuegungZeitabschnitt> zeitabschnitte = new LinkedList<>();
		zeitabschnitte.add(new VerfuegungZeitabschnitt());
		zeitabschnitte.get(0).setGueltigkeit(new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE));
		betreuung.getVerfuegung().setZeitabschnitte(zeitabschnitte);
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuung(betreuung);
		DateRange gueltigkeit = new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE);
		betreuungspensumContainer.setBetreuungspensumJA(new Betreuungspensum(gueltigkeit));
		betreuungspensumContainer.getBetreuungspensumJA().setPensum(MathUtil.DEFAULT.from(100));
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheHauptmahlzeiten(BigDecimal.ZERO);
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheNebenmahlzeiten(BigDecimal.ZERO);
		betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);
		betreuung.getKind()
				.getGesuch()
				.getGesuchsteller1()
				.addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
						TestDataUtil.START_PERIODE,
						TestDataUtil.ENDE_PERIODE,
						100));
		if (true) {
			betreuung.getKind()
					.getGesuch()
					.getGesuchsteller2()
					.addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
							TestDataUtil.START_PERIODE,
							TestDataUtil.ENDE_PERIODE,
							100));
		}
		return betreuung;
	}

	private GesuchstellerAdresseContainer createGesuchstellerAdresse(
			LocalDate von,
			LocalDate bis,
			boolean nichtInGemeinde,
			GesuchstellerContainer gesuchsteller) {
		GesuchstellerAdresseContainer adresse = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuchsteller);
		adresse.getGesuchstellerAdresseJA().setNichtInGemeinde(nichtInGemeinde);
		adresse.extractGueltigkeit().setGueltigAb(von);
		adresse.extractGueltigkeit().setGueltigBis(bis);
		return adresse;
	}

	private void createDossier(Gesuch gesuch) {
		Dossier dossier = TestDataUtil.createDefaultDossier();
		gesuch.setDossier(dossier);
	}

	private Betreuung createTestdata(boolean zweigesuchsteller) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(zweigesuchsteller);
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		betreuung.setBetreuungspensumContainers(new LinkedHashSet<>());
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuung(betreuung);
		DateRange gueltigkeit = new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE);
		betreuungspensumContainer.setBetreuungspensumJA(new Betreuungspensum(gueltigkeit));
		betreuungspensumContainer.getBetreuungspensumJA().setPensum(MathUtil.DEFAULT.from(100));
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheHauptmahlzeiten(BigDecimal.ZERO);
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheNebenmahlzeiten(BigDecimal.ZERO);
		betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);
		betreuung.getKind()
				.getGesuch()
				.getGesuchsteller1()
				.addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
						TestDataUtil.START_PERIODE,
						TestDataUtil.ENDE_PERIODE,
						100));
		if (zweigesuchsteller) {
			betreuung.getKind()
					.getGesuch()
					.getGesuchsteller2()
					.addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
							TestDataUtil.START_PERIODE,
							TestDataUtil.ENDE_PERIODE,
							100));
		}
		return betreuung;
	}

}