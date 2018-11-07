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

package ch.dvbern.ebegu.tests;

import java.time.LocalDate;

import javax.annotation.Nullable;
import javax.inject.Inject;

import ch.dvbern.ebegu.dto.dataexport.v1.VerfuegungExportDTO;
import ch.dvbern.ebegu.dto.dataexport.v1.VerfuegungenExportDTO;
import ch.dvbern.ebegu.dto.dataexport.v1.ZeitabschnittExportDTO;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.services.ExportService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.TestfaelleService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.StreamsUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests fuer die Klasse AdresseService
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class ExportServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private ExportService exportService;

	@Inject
	private Persistence persistence;

	@Inject
	private InstitutionService instService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private TestfaelleService testfaelleService;

	@Nullable
	private Gemeinde gemeinde;
	private Gesuchsperiode gesuchsperiode;

	@Before
	public void init() {
		gesuchsperiode = createGesuchsperiode();
		gemeinde = TestDataUtil.getGemeindeBern(persistence);
		final Mandant mandant = insertInstitutionen();
		createBenutzer(mandant);
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);
	}

	/**
	 * Helper für init. Speichert Gesuchsperiode in DB
	 */
	@Override
	protected Gesuchsperiode createGesuchsperiode() {
		Gesuchsperiode gesuchsperiode1617 = TestDataUtil.createGesuchsperiode1718();
		gesuchsperiode1617.setStatus(GesuchsperiodeStatus.AKTIV);
		gesuchsperiode1617 = gesuchsperiodeService.saveGesuchsperiode(gesuchsperiode1617);
		return gesuchsperiode1617;
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Test
	public void exportTest() {

		Gesuch yvonneGesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence, LocalDate.now(), null, gesuchsperiode);
		VerfuegungenExportDTO verfuegungenExportDTO = exportService.exportAllVerfuegungenOfAntrag(yvonneGesuch.getId());
		Assert.assertNotNull(verfuegungenExportDTO);
		Assert.assertNotNull(verfuegungenExportDTO.getVerfuegungen());
		Assert.assertEquals(0, verfuegungenExportDTO.getVerfuegungen().size());
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Test
	public void exportTestVorVerfuegt() {

		Assert.assertNotNull(gemeinde);
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.WAELTI_DAGMAR,
			true, true, gemeinde.getId(), gesuchsperiode);
		Assert.assertNotNull(gesuch.getKindContainers().stream().findFirst());
		Assert.assertTrue(gesuch.getKindContainers().stream().findFirst().isPresent());
		KindContainer container = gesuch.getKindContainers().stream().findFirst().get();
		Assert.assertTrue(container.getBetreuungen().stream().findFirst().isPresent());
		Assert.assertNotNull(container.getBetreuungen().stream().findFirst().get().getVerfuegung());
		VerfuegungenExportDTO exportedVerfuegungen = exportService.exportAllVerfuegungenOfAntrag(gesuch.getId());
		Assert.assertNotNull(exportedVerfuegungen);
		Assert.assertNotNull(exportedVerfuegungen.getVerfuegungen());
		Assert.assertEquals(2, exportedVerfuegungen.getVerfuegungen().size());
		Assert.assertEquals(12, exportedVerfuegungen.getVerfuegungen().iterator().next().getZeitabschnitte().size());
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				Assert.assertNotNull(betreuung.getVerfuegung());
				@SuppressWarnings("ConstantConditions")
				VerfuegungExportDTO matchingVerfuegungExport = exportedVerfuegungen.getVerfuegungen().stream()
					.filter(verfuegungExportDTO -> verfuegungExportDTO.getRefnr().equals(betreuung.getBGNummer())).reduce(StreamsUtil.toOnlyElement()).get();
				checkExportedValuesCorrect(betreuung.getVerfuegung(), matchingVerfuegungExport);
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	private void checkExportedValuesCorrect(Verfuegung verfuegung, VerfuegungExportDTO matchingVerfuegungExport) {
		Assert.assertEquals(verfuegung.getZeitabschnitte().size(), matchingVerfuegungExport.getZeitabschnitte().size());
		Assert.assertEquals(verfuegung.getBetreuung().getBetreuungsangebotTyp(), matchingVerfuegungExport.getBetreuung().getBetreuungsArt());

		Assert.assertEquals(verfuegung.getBetreuung().getInstitutionStammdaten().getInstitution().getId(), matchingVerfuegungExport.getBetreuung().getInstitution().getId());
		Assert.assertNotNull(verfuegung.getBetreuung().extractGesuch().getGesuchsteller1());
		Assert.assertEquals(verfuegung.getBetreuung().extractGesuch().getGesuchsteller1().getGesuchstellerJA()
			.getMail(), matchingVerfuegungExport.getGesuchsteller().getEmail());
		Assert.assertEquals(verfuegung.getBetreuung().extractGesuch().getGesuchsteller1().getGesuchstellerJA().getNachname(), matchingVerfuegungExport.getGesuchsteller().getNachname());
		Assert.assertEquals(verfuegung.getBetreuung().extractGesuch().getGesuchsteller1().getGesuchstellerJA().getVorname(), matchingVerfuegungExport.getGesuchsteller().getVorname());
		Assert.assertEquals(verfuegung.getBetreuung().getKind().getKindJA().getNachname(), matchingVerfuegungExport.getKind().getNachname());

		for (VerfuegungZeitabschnitt verfZeitabschn : verfuegung.getZeitabschnitte()) {
			ZeitabschnittExportDTO matchingZeitAbschn = matchingVerfuegungExport.getZeitabschnitte().stream().filter(zeitabschnExpo -> zeitabschnExpo.getVon().equals(verfZeitabschn.getGueltigkeit().getGueltigAb())).reduce(StreamsUtil.toOnlyElement()).get();
			Assert.assertEquals(matchingZeitAbschn.getBis(), (verfZeitabschn.getGueltigkeit().getGueltigBis()));
			Assert.assertEquals(0, verfZeitabschn.getVerguenstigung().compareTo(matchingZeitAbschn.getVerguenstigung()));
			Assert.assertEquals(verfZeitabschn.getAnspruchberechtigtesPensum(), matchingZeitAbschn.getAnspruchPct());
		}
	}
}
