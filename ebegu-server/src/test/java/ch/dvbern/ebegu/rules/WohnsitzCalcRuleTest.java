package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
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

	private final DateRange TEST_PERIODE = new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.START_PERIODE);
	private Supplier<GesuchService> gesuchServiceSupplier;

	@TestSubject
	private WohnsitzCalcRule wohnsitzCalcRule = new WohnsitzCalcRule(TEST_PERIODE, Locale.GERMAN, gesuchServiceSupplier);


	@Before
	public void setUp() {
		GesuchService gesuchService= EasyMock.createMock(GesuchService.class);
		EasyMock.expect(gesuchService.getAllGesuchForFallAndGesuchsperiodeInUnterschiedlichenGemeinden(
				EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject())).andReturn(populateGesuchsliste());

		EasyMock.replay(gesuchService);
		this.gesuchServiceSupplier = () ->  gesuchService;
		wohnsitzCalcRule = new WohnsitzCalcRule(TEST_PERIODE, Locale.GERMAN, gesuchServiceSupplier);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testExecuteRule() {
		BGCalculationInput inputData = prepareInputData();
		wohnsitzCalcRule.executeRule(preparePlatzWithSameKind(), inputData);
		assertNotNull(inputData);
		assertFalse(inputData.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.UMZUG_BG_BEREITS_IN_ANDERER_GEMEINDE));
	}

	@Test
	public void testNoPotentielleDoppelbetreuung() {
		assertNotNull(wohnsitzCalcRule);
		BGCalculationInput inputData = prepareInputData();
		inputData.setPotentielleDoppelBetreuung(false);
		wohnsitzCalcRule.executeRule(preparePlatzWithSameKind(), inputData);
		assertFalse(inputData.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.UMZUG_BG_BEREITS_IN_ANDERER_GEMEINDE));
	}

	@Test
	public void testNotSameKindInOtherBetreuung() {
		assertNotNull(wohnsitzCalcRule);
		BGCalculationInput inputData = prepareInputData();
		inputData.setPotentielleDoppelBetreuung(true);
		wohnsitzCalcRule.executeRule(preparePlatzWithOtherKind(), inputData);
		assertFalse(inputData.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.UMZUG_BG_BEREITS_IN_ANDERER_GEMEINDE));
	}

	@Test
	public void testExecuteRuleWithDossierservice() {
		BGCalculationInput inputData = prepareInputData();
		inputData.setWohnsitzNichtInGemeindeGS1(true);
		inputData.setPotentielleDoppelBetreuung(true);
		assertFalse(inputData.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.UMZUG_BG_BEREITS_IN_ANDERER_GEMEINDE));
		wohnsitzCalcRule.executeRule(preparePlatzWithSameKind(), inputData);
		assertTrue(inputData.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.UMZUG_BG_BEREITS_IN_ANDERER_GEMEINDE));
	}

	@Test
	public void testExecuteRuleWithDossierserviceNichtInGemeindeFalse() {
		BGCalculationInput inputData = prepareInputData();
		inputData.setWohnsitzNichtInGemeindeGS1(false);
		inputData.setPotentielleDoppelBetreuung(true);
		assertTrue(inputData.getParent().getBemerkungenDTOList().isEmpty());
		wohnsitzCalcRule.executeRule(preparePlatzWithSameKind(), inputData);
		assertTrue(inputData.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.UMZUG_BG_BEREITS_IN_ANDERER_GEMEINDE));
	}

	@Test
	public void testZuzug() {
		Betreuung betreuung = (Betreuung) preparePlatzWithSameKind();
		BGCalculationInput bgCalculationInput = prepareInputData();
		bgCalculationInput.setPotentielleDoppelBetreuung(true);
		assertFalse(bgCalculationInput.isAnspruchSinktDuringMonat());
		assertFalse(bgCalculationInput.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.UMZUG_BG_BEREITS_IN_ANDERER_GEMEINDE));
		wohnsitzCalcRule.executeRule(betreuung, bgCalculationInput);
		assertTrue(bgCalculationInput.isAnspruchSinktDuringMonat());
		assertEquals(bgCalculationInput.getAnspruchspensumProzent(), 0);
		assertTrue(bgCalculationInput.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.UMZUG_BG_BEREITS_IN_ANDERER_GEMEINDE));
	}

	private BGCalculationInput prepareInputData() {
		return new BGCalculationInput(new VerfuegungZeitabschnitt(TEST_PERIODE), RuleValidity.ASIV);
	}

	private AbstractPlatz preparePlatz(KindContainer kindMock) {
		AbstractPlatz betreuungMock  = EasyMock.createMock(Betreuung.class);
		Gesuch gesuchMock = EasyMock.createMock(Gesuch.class);
		Dossier dossierMock = EasyMock.createMock(Dossier.class);
		Gemeinde gemeindeMock = EasyMock.createMock(Gemeinde.class);
		Gesuchsperiode gesuchsPeriodeMock = EasyMock.createMock(Gesuchsperiode.class);
		Fall fallMock = EasyMock.createMock(Fall.class);
		InstitutionStammdaten institutionStammdaten = EasyMock.createMock(InstitutionStammdaten.class);
		Institution institution = EasyMock.createMock(Institution.class);

		EasyMock.expect(kindMock.getGesuch()).andReturn(gesuchMock);
		EasyMock.expect(kindMock.getBetreuungen()).andReturn(Set.of((Betreuung) betreuungMock)).anyTimes();
		EasyMock.expect(gesuchMock.getKindContainers()).andReturn(Set.of(kindMock));
		EasyMock.expect(gemeindeMock.getName()).andReturn("TEST_GEMAINDE_32");
		EasyMock.expect(dossierMock.getGemeinde()).andReturn(gemeindeMock);
		EasyMock.expect(dossierMock.getFall()).andReturn(fallMock);
		EasyMock.expect(fallMock.getFallNummer()).andReturn(5007L);
		EasyMock.expect(gesuchMock.getDossier()).andReturn(dossierMock);
		EasyMock.expect(gesuchMock.getFall()).andReturn(fallMock);
		EasyMock.expect(gesuchMock.getKindContainers()).andReturn(Set.of(kindMock)).anyTimes();
		EasyMock.expect(betreuungMock.extractGesuchsperiode()).andReturn(gesuchsPeriodeMock).anyTimes();
		EasyMock.expect(betreuungMock.extractGesuch()).andReturn(gesuchMock).anyTimes();
		EasyMock.expect(betreuungMock.extractGemeinde()).andReturn(gemeindeMock).anyTimes();
		EasyMock.expect(betreuungMock.getKind()).andReturn(kindMock).anyTimes();
		EasyMock.expect(betreuungMock.getInstitutionStammdaten()).andReturn(institutionStammdaten).anyTimes();
		Verfuegung verfuegung = EasyMock.createMock(Verfuegung.class);
		EasyMock.expect(betreuungMock.getVerfuegung()).andReturn(verfuegung).anyTimes();

		EasyMock.expect(institutionStammdaten.getInstitution()).andReturn(institution).anyTimes();
		EasyMock.expect(institution.getId()).andReturn("veryUniqueID").anyTimes();
		EasyMock.replay(verfuegung);
		EasyMock.replay(betreuungMock);
		EasyMock.replay(gesuchMock);
		EasyMock.replay(gemeindeMock);
		EasyMock.replay(dossierMock);
		EasyMock.replay(fallMock);
		EasyMock.replay(gesuchsPeriodeMock);
		EasyMock.replay(institution);
		EasyMock.replay(institutionStammdaten);
		EasyMock.replay(kindMock);
		return betreuungMock;
	}

	private AbstractPlatz preparePlatzWithSameKind() {
		return preparePlatz(mockKind("hans-ueli", "Tester", LocalDate.of(2018, 3, 15)));
	}

	private AbstractPlatz preparePlatzWithOtherKind() {
		return preparePlatz(mockKind("Mia", "Tester", LocalDate.of(2018, 3, 15)));
	}

	private KindContainer mockKind(String vorname, String nachname, LocalDate geburtsdatum) {
		Kind kind = EasyMock.createMock(Kind.class);
		EasyMock.expect(kind.getNachname()).andReturn(nachname).anyTimes();
		EasyMock.expect(kind.getVorname()).andReturn(vorname).anyTimes();
		EasyMock.expect(kind.getGeburtsdatum()).andReturn(geburtsdatum).anyTimes();

		KindContainer kindContainerMock = EasyMock.createMock(KindContainer.class);
		EasyMock.expect(kindContainerMock.getKindJA()).andReturn(kind).anyTimes();

		EasyMock.replay(kind);
		return kindContainerMock;
	}

	private List<Gesuch> populateGesuchsliste() {
		Betreuung betreuung = (Betreuung) preparePlatzWithSameKind();

		List<Gesuch>  gesuchListe = new LinkedList<>();
		gesuchListe.add(betreuung.extractGesuch());

		return gesuchListe;
	}

}
