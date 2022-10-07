/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

package ch.dvbern.ebegu.tests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.TestdataCreationService;
import ch.dvbern.ebegu.services.ZahlungService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.util.TestfallName;
import ch.dvbern.ebegu.util.testdata.ErstgesuchConfig;
import ch.dvbern.ebegu.util.testdata.MutationConfig;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests fuer den Zahlungsservice
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
public class ZahlungsauftragSonderfaelleTest extends AbstractTestdataCreationTest {

	private static final Logger LOG = LoggerFactory.getLogger(ZahlungsauftragSonderfaelleTest.class);

	@Inject
	private ZahlungService zahlungService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private TestdataCreationService testdataCreationService;

	private Gesuch erstgesuch;
	private Zahlungsauftrag lastZahlungsauftrag;
	private LocalDateTime datumGeneriertErsterZahlungsauftrag;
	private LocalDate eingangsdatum;


	@Override
	@Before
	public void init() {
		super.init();

		datumGeneriertErsterZahlungsauftrag = LocalDateTime.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 20, 0, 0);
		eingangsdatum = LocalDate.of(gesuchsperiode.getBasisJahr(), Month.JULY, 20);

		// *** Erstgesuch mit EWP 70%, verfuegen und auszahlen
		ErstgesuchConfig config = ErstgesuchConfig.createErstgesuchVerfuegt(
			TestfallName.LUETHI_MERET, gesuchsperiode, eingangsdatum, datumGeneriertErsterZahlungsauftrag.minusDays(1));
		erstgesuch = testdataCreationService.createErstgesuch(config, mandant);

		Gemeinde gemeinde = erstgesuch.extractGemeinde();

		lastZahlungsauftrag = zahlungService
			.zahlungsauftragErstellen(
				ZahlungslaufTyp.GEMEINDE_INSTITUTION,
				gemeinde.getId(),
				LocalDate.now().plusDays(3),
				"Zahlung Normal August",
				false,
				mandant);
		lastZahlungsauftrag = zahlungService.zahlungsauftragAusloesen(lastZahlungsauftrag.getId());

		erstgesuch = gesuchService.findGesuch(erstgesuch.getId()).orElseThrow(() -> new EbeguEntityNotFoundException("findGesuch",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, erstgesuch.getId()));
		Assert.assertNotNull(erstgesuch);
		List<VerfuegungZeitabschnitt> alleZeitabschnitte = getAllZeitabschnitteOrderedByGesuchAndDatum();
		Assert.assertNotNull(alleZeitabschnitte);
		Assert.assertEquals(12, alleZeitabschnitte.size());
		// (1) Erstgesuch
		assertZahlungsstatus(alleZeitabschnitte, 0, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET,
			VerfuegungsZeitabschnittZahlungsstatus.NEU);
	}

	@Test
	public void leereMutationDarfNichtsAendern() {
		// *** Mutation 1: Keine Anpassungen. Flag Ignorieren? kommt nicht. Verrechnungszustand bleibt gleich
		MutationConfig configEmptyMutation = MutationConfig.createEmptyMutationVerfuegt(
			eingangsdatum.plusDays(2), lastZahlungsauftrag.getDatumGeneriert().plusDays(1));
		Gesuch mutation = testdataCreationService.createMutation(configEmptyMutation, erstgesuch);

		Assert.assertNotNull(mutation);
		List<VerfuegungZeitabschnitt> alleZeitabschnitte = getAllZeitabschnitteOrderedByGesuchAndDatum();
		Assert.assertNotNull(alleZeitabschnitte);
		Assert.assertEquals(24, alleZeitabschnitte.size());
		// (1) Erstgesuch
		assertZahlungsstatus(alleZeitabschnitte, 0, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET,
			VerfuegungsZeitabschnittZahlungsstatus.NEU);
		// (2) M1: Da es keine Anpassungen gab, bleibt der "Verrechnungszustand" gleich
		assertZahlungsstatus(alleZeitabschnitte, 12, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNEND,
			VerfuegungsZeitabschnittZahlungsstatus.NEU);
	}

	@Test
	public void mutationSameDataIgnorierenNichtMoeglich() {
		// *** Mutation 2: Die Anpassungen bleiben gleich. Das Ignorieren-Flag darf keine Auswirkungen haben
		MutationConfig configMutationSameData = MutationConfig.createMutationVerfuegt(
			eingangsdatum.plusDays(2), lastZahlungsauftrag.getDatumGeneriert().plusDays(1), 70, true);
		Gesuch mutation = testdataCreationService.createMutation(configMutationSameData, erstgesuch);

		Assert.assertNotNull(mutation);
		List<VerfuegungZeitabschnitt> alleZeitabschnitte = getAllZeitabschnitteOrderedByGesuchAndDatum();
		Assert.assertNotNull(alleZeitabschnitte);
		Assert.assertEquals(24, alleZeitabschnitte.size());
		// (1) Erstgesuch
		assertZahlungsstatus(alleZeitabschnitte, 0, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET,
			VerfuegungsZeitabschnittZahlungsstatus.NEU);
		// (2) M1: Da es keine Anpassungen gab, bleibt der "Verrechnungszustand" gleich
		assertZahlungsstatus(alleZeitabschnitte, 12, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNEND,
			VerfuegungsZeitabschnittZahlungsstatus.NEU);
	}

	@Test
	public void mutationErwerbspensumIgnorierenNichtInZukunft() {
		// *** Mutation 3: Mit Anpassung, Flag Ignorieren? gesetzt. Nur die bereits ausbezahlten dürfen ignoriert werden
		MutationConfig configMutationIgnoriert = MutationConfig.createMutationVerfuegt(
			eingangsdatum.plusDays(2), lastZahlungsauftrag.getDatumGeneriert().plusDays(1), 20, true);
		Gesuch mutation = testdataCreationService.createMutation(configMutationIgnoriert, erstgesuch);

		Assert.assertNotNull(mutation);
		List<VerfuegungZeitabschnitt> alleZeitabschnitte = getAllZeitabschnitteOrderedByGesuchAndDatum();
		Assert.assertNotNull(alleZeitabschnitte);
		Assert.assertEquals(24, alleZeitabschnitte.size());
		// (1) Erstgesuch ist verrechnet
		assertZahlungsstatus(alleZeitabschnitte, 0, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET,
			VerfuegungsZeitabschnittZahlungsstatus.NEU);
		// (2) M1: Da es keine Anpassungen gab, bleibt der "Verrechnungszustand" gleich
		assertZahlungsstatus(alleZeitabschnitte, 12, VerfuegungsZeitabschnittZahlungsstatus.IGNORIEREND,
			VerfuegungsZeitabschnittZahlungsstatus.NEU);
	}

	@Test
	public void zahlungslaufSetztIgnorierendAufIgnoriertRepetition() {
		// *** Mutation 3: Mit Anpassung, Flag Ignorieren? gesetzt. Nur die bereits ausbezahlten dürfen ignoriert werden
		MutationConfig configMutationIgnoriert = MutationConfig.createMutationVerfuegt(
			eingangsdatum.plusDays(2), lastZahlungsauftrag.getDatumGeneriert().plusDays(1), 20, true);
		Gesuch mutation = testdataCreationService.createMutation(configMutationIgnoriert, erstgesuch);
		Gemeinde gemeinde = mutation.extractGemeinde();

		// Zahlung im gleichen Monat
		zahlungService.zahlungsauftragErstellen(
			ZahlungslaufTyp.GEMEINDE_INSTITUTION,
			gemeinde.getId(),
			LocalDate.now().plusDays(3),
			"Zahlung Repetition August",
			false,
			mandant);

		Assert.assertNotNull(mutation);
		List<VerfuegungZeitabschnitt> alleZeitabschnitte = getAllZeitabschnitteOrderedByGesuchAndDatum();
		Assert.assertNotNull(alleZeitabschnitte);
		Assert.assertEquals(24, alleZeitabschnitte.size());
		// (1) Erstgesuch
		assertZahlungsstatus(alleZeitabschnitte, 0, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET_KORRIGIERT,
			VerfuegungsZeitabschnittZahlungsstatus.NEU);
		// (2) M1: Da es keine Anpassungen gab, bleibt der "Verrechnungszustand" gleich
		assertZahlungsstatus(alleZeitabschnitte, 12, VerfuegungsZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT,
			VerfuegungsZeitabschnittZahlungsstatus.NEU);
	}

	@Test
	public void zahlungslaufSetztIgnorierendAufIgnoriertNaechsteZahlung() {
		// *** Mutation 3: Mit Anpassung, Flag Ignorieren? gesetzt. Nur die bereits ausbezahlten dürfen ignoriert werden
		MutationConfig configMutationIgnoriert = MutationConfig.createMutationVerfuegt(
			eingangsdatum.plusDays(2), lastZahlungsauftrag.getDatumGeneriert().plusDays(1), 20, true);
		Gesuch mutation = testdataCreationService.createMutation(configMutationIgnoriert, erstgesuch);
		Gemeinde gemeinde = mutation.extractGemeinde();

		// Zahlung im gleichen Monat
		zahlungService.zahlungsauftragErstellen(
			ZahlungslaufTyp.GEMEINDE_INSTITUTION,
			gemeinde.getId(),
			LocalDate.now().plusDays(3),
			"Zahlung Normal September",
			false,
			mandant);

		Assert.assertNotNull(mutation);
		List<VerfuegungZeitabschnitt> alleZeitabschnitte = getAllZeitabschnitteOrderedByGesuchAndDatum();
		Assert.assertNotNull(alleZeitabschnitte);
		Assert.assertEquals(24, alleZeitabschnitte.size());
		// (1) Erstgesuch
		assertZahlungsstatus(alleZeitabschnitte, 0, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET_KORRIGIERT,
			VerfuegungsZeitabschnittZahlungsstatus.NEU); // EG
		// (2) M1: Da es keine Anpassungen gab, bleibt der "Verrechnungszustand" gleich
		assertZahlungsstatus(alleZeitabschnitte, 12, 12, VerfuegungsZeitabschnittZahlungsstatus.IGNORIERT); // M August
		assertZahlungsstatus(alleZeitabschnitte, 13, 13, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET); // M Septempber
		assertZahlungsstatus(alleZeitabschnitte, 14, 23, VerfuegungsZeitabschnittZahlungsstatus.NEU); // M Rest
	}

	private void assertZahlungsstatus(List<VerfuegungZeitabschnitt> alleZeitabschnitte, int startIndex, int endIndex,
			VerfuegungsZeitabschnittZahlungsstatus status) {
		for (int i = startIndex; i < endIndex; i++) {
			Assert.assertSame(status, alleZeitabschnitte.get(i).getZahlungsstatusInstitution());
		}
	}

	private void assertZahlungsstatus(List<VerfuegungZeitabschnitt> alleZeitabschnitte, int startIndex,
		VerfuegungsZeitabschnittZahlungsstatus statusFirstMonth, VerfuegungsZeitabschnittZahlungsstatus statusRestOfMonths) {
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : alleZeitabschnitte) {
			LOG.warn(verfuegungZeitabschnitt.toStringWithoutBemerkung());
		}
		Assert.assertSame(statusFirstMonth, alleZeitabschnitte.get(startIndex).getZahlungsstatusInstitution());
		Assert.assertSame(statusRestOfMonths, alleZeitabschnitte.get(startIndex+1).getZahlungsstatusInstitution());
		Assert.assertSame(statusRestOfMonths, alleZeitabschnitte.get(startIndex+2).getZahlungsstatusInstitution());
		Assert.assertSame(statusRestOfMonths, alleZeitabschnitte.get(startIndex+3).getZahlungsstatusInstitution());
		Assert.assertSame(statusRestOfMonths, alleZeitabschnitte.get(startIndex+4).getZahlungsstatusInstitution());
		Assert.assertSame(statusRestOfMonths, alleZeitabschnitte.get(startIndex+5).getZahlungsstatusInstitution());
		Assert.assertSame(statusRestOfMonths, alleZeitabschnitte.get(startIndex+6).getZahlungsstatusInstitution());
		Assert.assertSame(statusRestOfMonths, alleZeitabschnitte.get(startIndex+7).getZahlungsstatusInstitution());
		Assert.assertSame(statusRestOfMonths, alleZeitabschnitte.get(startIndex+8).getZahlungsstatusInstitution());
		Assert.assertSame(statusRestOfMonths, alleZeitabschnitte.get(startIndex+9).getZahlungsstatusInstitution());
		Assert.assertSame(statusRestOfMonths, alleZeitabschnitte.get(startIndex+10).getZahlungsstatusInstitution());
		Assert.assertSame(statusRestOfMonths, alleZeitabschnitte.get(startIndex+11).getZahlungsstatusInstitution());
	}

	private List<VerfuegungZeitabschnitt> getAllZeitabschnitteOrderedByGesuchAndDatum() {
		ArrayList<VerfuegungZeitabschnitt> all = new ArrayList<>(criteriaQueryHelper.getAll(VerfuegungZeitabschnitt.class));
		all.sort((o1, o2) -> {
			Assert.assertNotNull(o1.getVerfuegung().getBetreuung());
			Assert.assertNotNull(o2.getVerfuegung().getBetreuung());
			Integer ln1 = o1.getVerfuegung().getBetreuung().getKind().getGesuch().getLaufnummer();
			Integer ln2 = o2.getVerfuegung().getBetreuung().getKind().getGesuch().getLaufnummer();
			int i = ln1.compareTo(ln2);
			if (i == 0) {
				i = o1.getGueltigkeit().getGueltigAb().compareTo(o2.getGueltigkeit().getGueltigAb());
			}
			return i;
		});
		return all;
	}
}
