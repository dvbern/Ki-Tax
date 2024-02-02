package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FamiliensituationBeendetAbschnittRuleTest {

	public static final int DAYS_TO_ADD = 86;
	private Betreuung erstGesuch;
	private List<VerfuegungZeitabschnitt> zeitAbschnitteErstgesuch;
	private Map<EinstellungKey, Einstellung> einstellungen;

	@BeforeEach
	public void setup() {
		erstGesuch = TestDataUtil.createGesuchWithBetreuungspensum(true);

		einstellungen = EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(erstGesuch.extractGesuchsperiode());
		einstellungen.get(EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2).setValue(String.valueOf(true));

		zeitAbschnitteErstgesuch = EbeguRuleTestsHelper.calculateWithCustomEinstellungen(erstGesuch, einstellungen);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(zeitAbschnitteErstgesuch);
		erstGesuch.setVerfuegung(verfuegungErstgesuch);
	}

	@Test
	void deactivatedRule() {
		assertNotNull(erstGesuch, "Erstgesuch darf nicht Null sein.");
		assertNotNull(zeitAbschnitteErstgesuch, "ZeitAbschnitte erstgesuch müssen gesetzt sein.");
		Assertions.assertEquals(1, zeitAbschnitteErstgesuch.size(), "Es darf nur einen geben");
		assertNotNull(zeitAbschnitteErstgesuch.get(0));
		Betreuung mutation = createMutationMitAndererFamiliensituation(erstGesuch, Boolean.FALSE);
		assertNotNull(mutation);
		assertNotNull(mutation.extractGesuch());
		assertNotNull(mutation.extractGesuch().getGesuchsteller1());
		assertNotNull(mutation.extractGesuch().getGesuchsteller2());
		TestDataUtil.createDefaultGesuchstellerAdresseContainer(Objects.requireNonNull(mutation.extractGesuch()
			.getGesuchsteller1()));
		TestDataUtil.createDefaultGesuchstellerAdresseContainer(Objects.requireNonNull(mutation.extractGesuch()
			.getGesuchsteller2()));

		einstellungen.get(EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2).setValue(String.valueOf(false));
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculateWithCustomEinstellungen(mutation, einstellungen);
		assertNotNull(zeitabschnitteMutation);
		assertEquals(1, zeitabschnitteMutation.size()); //kein neuer Zeitabschnitt erstellt
	}

	@Test
	void createVerfuegungsZeitabschnitte() {
		assertNotNull(erstGesuch, "Erstgesuch darf nicht Null sein.");
		assertNotNull(zeitAbschnitteErstgesuch, "ZeitAbschnitte erstgesuch müssen gesetzt sein.");
		Assertions.assertEquals(1, zeitAbschnitteErstgesuch.size(), "Es darf nur einen geben");
		assertNotNull(zeitAbschnitteErstgesuch.get(0));
		Betreuung mutation = createMutationMitAndererFamiliensituation(erstGesuch, Boolean.FALSE);
		assertNotNull(mutation);
		assertNotNull(mutation.extractGesuch());
		assertNotNull(mutation.extractGesuch().getGesuchsteller1());
		assertNotNull(mutation.extractGesuch().getGesuchsteller2());
		TestDataUtil.createDefaultGesuchstellerAdresseContainer(Objects.requireNonNull(mutation.extractGesuch()
			.getGesuchsteller1()));
		TestDataUtil.createDefaultGesuchstellerAdresseContainer(Objects.requireNonNull(mutation.extractGesuch()
			.getGesuchsteller2()));

		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculateWithCustomEinstellungen(mutation, einstellungen);
		assertNotNull(zeitabschnitteMutation);
		assertEquals(2, zeitabschnitteMutation.size());
		LocalDate firstOfNextMonth = TestDataUtil.START_PERIODE.plusDays(DAYS_TO_ADD).with(TemporalAdjusters.firstDayOfNextMonth());
		assertEquals(firstOfNextMonth, zeitabschnitteMutation.get(1).getGueltigkeit().getGueltigAb());
		assertEquals(TestDataUtil.ENDE_PERIODE, zeitabschnitteMutation.get(1).getGueltigkeit().getGueltigBis());
	}


	@Test
	void doNotcreateVerfuegungsZeitabschnitte_PartnerIdentischNotSet() {
		assertNotNull(erstGesuch, "Erstgesuch darf nicht Null sein.");
		assertNotNull(zeitAbschnitteErstgesuch, "ZeitAbschnitte erstgesuch müssen gesetzt sein.");
		Assertions.assertEquals(1, zeitAbschnitteErstgesuch.size(), "Es darf nur einen geben");
		assertNotNull(zeitAbschnitteErstgesuch.get(0));
		Betreuung mutation = createMutationMitAndererFamiliensituation(erstGesuch, null);
		assertNotNull(mutation);
		assertNotNull(mutation.extractGesuch());
		assertNotNull(mutation.extractGesuch().getGesuchsteller1());
		assertNotNull(mutation.extractGesuch().getGesuchsteller2());
		TestDataUtil.createDefaultGesuchstellerAdresseContainer(Objects.requireNonNull(mutation.extractGesuch()
				.getGesuchsteller1()));
		TestDataUtil.createDefaultGesuchstellerAdresseContainer(Objects.requireNonNull(mutation.extractGesuch()
				.getGesuchsteller2()));

		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculateWithCustomEinstellungen(mutation, einstellungen);
		assertNotNull(zeitabschnitteMutation);
		assertEquals(1, zeitabschnitteMutation.size());
	}

	@Test
	void doNotcreateVerfuegungsZeitabschnitte_PartnerIdentischTRUE() {
		assertNotNull(erstGesuch, "Erstgesuch darf nicht Null sein.");
		assertNotNull(zeitAbschnitteErstgesuch, "ZeitAbschnitte erstgesuch müssen gesetzt sein.");
		Assertions.assertEquals(1, zeitAbschnitteErstgesuch.size(), "Es darf nur einen geben");
		assertNotNull(zeitAbschnitteErstgesuch.get(0));
		Betreuung mutation = createMutationMitAndererFamiliensituation(erstGesuch, Boolean.TRUE);
		assertNotNull(mutation);
		assertNotNull(mutation.extractGesuch());
		assertNotNull(mutation.extractGesuch().getGesuchsteller1());
		assertNotNull(mutation.extractGesuch().getGesuchsteller2());
		TestDataUtil.createDefaultGesuchstellerAdresseContainer(Objects.requireNonNull(mutation.extractGesuch()
				.getGesuchsteller1()));
		TestDataUtil.createDefaultGesuchstellerAdresseContainer(Objects.requireNonNull(mutation.extractGesuch()
				.getGesuchsteller2()));

		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculateWithCustomEinstellungen(mutation, einstellungen);
		assertNotNull(zeitabschnitteMutation);
		assertEquals(1, zeitabschnitteMutation.size());
	}

	private Betreuung createMutationMitAndererFamiliensituation(final Betreuung erstGesuch, final Boolean partnerIdentisch) {
		VerfuegungZeitabschnitt ersterZeitabschnitt = zeitAbschnitteErstgesuch.get(0);
		LocalDate erstGesuchGueltigAb = ersterZeitabschnitt.getGueltigkeit().getGueltigAb();
		Betreuung mutation = TestDataUtil.createGesuchWithBetreuungspensum(true);
		mutation.initVorgaengerVerfuegungen(erstGesuch.getVerfuegung(), null);
		assertNotNull(erstGesuch.extractGesuch().getFamiliensituationContainer());
		assertNotNull(erstGesuch.extractGesuch().getFamiliensituationContainer().getFamiliensituationJA());
		assertNotNull(mutation.extractGesuch().getFamiliensituationContainer());
		assertNotNull(mutation.extractGesuch().getFamiliensituationContainer().getFamiliensituationJA());
		mutation.extractGesuch().getFamiliensituationContainer().setFamiliensituationErstgesuch(erstGesuch.extractGesuch().getFamiliensituationContainer().getFamiliensituationJA());
		mutation.extractGesuch().getFamiliensituationContainer().getFamiliensituationJA().setAenderungPer(erstGesuchGueltigAb.plusDays(DAYS_TO_ADD));
		mutation.extractGesuch().getFamiliensituationContainer().getFamiliensituationJA().setPartnerIdentischMitVorgesuch(partnerIdentisch);
		mutation.extractGesuch().getFamiliensituationContainer().getFamiliensituationJA().setFamilienstatus(EnumFamilienstatus.KONKUBINAT);
		mutation.setVorgaengerId(erstGesuch.getId());
		return mutation;
	}

}
