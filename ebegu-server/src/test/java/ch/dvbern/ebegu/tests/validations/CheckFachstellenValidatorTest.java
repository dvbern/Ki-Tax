/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.tests.validations;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.validators.CheckFachstellenValidator;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static ch.dvbern.ebegu.enums.EinstellungKey.FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE;
import static ch.dvbern.ebegu.enums.EinstellungKey.SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE;
import static org.easymock.EasyMock.expect;

/**
 * Tests fuer {@link CheckFachstellenValidator}
 */
@ExtendWith(EasyMockExtension.class)
class CheckFachstellenValidatorTest extends EasyMockSupport {

	@TestSubject
	private final CheckFachstellenValidator validator = new CheckFachstellenValidator();

	@Mock
	private EinstellungService einstellungServiceMock;

	@Test
	void checkKindWithoutFachstelleIsValid() {
		var kindContainer = createKindContainer(false, EinschulungTyp.KINDERGARTEN2, IntegrationTyp.SOZIALE_INTEGRATION);
		createEinstellungMock(kindContainer, FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE, "KINDERGARTEN2", 1);
		replayAll();
		var isValid = validator.isValid(kindContainer, null);
		Assertions.assertTrue(isValid);
	}

	@Test
	void checkMaxFachstelleEinstellungOk() {
		var kindContainer = createKindContainer(true, EinschulungTyp.KINDERGARTEN2, IntegrationTyp.SOZIALE_INTEGRATION);
		createEinstellungMock(kindContainer, FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE, "KINDERGARTEN2", 1);
		replayAll();
		var isValid = validator.isValid(kindContainer, null);
		Assertions.assertTrue(isValid);
	}

	@Test
	void checkMaxFachstelleEinstellungOkTwoOfTwo() {
		var kindContainer = createKindContainer(true, EinschulungTyp.KINDERGARTEN2, IntegrationTyp.SOZIALE_INTEGRATION);
		kindContainer.getKindJA().getPensumFachstelle().add(createPensumFachstelleWithIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION));
		createEinstellungMock(kindContainer, FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE, "KINDERGARTEN2", 2);
		replayAll();
		var isValid = validator.isValid(kindContainer, null);
		Assertions.assertTrue(isValid);
	}

	@Test
	void checkMaxFachstelleEinstellungNotOk() {
		var kindContainer = createKindContainer(true, EinschulungTyp.KINDERGARTEN2, IntegrationTyp.SOZIALE_INTEGRATION);
		createEinstellungMock(kindContainer, FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE, "KINDERGARTEN1", 1);
		replayAll();
		var isValid = validator.isValid(kindContainer, null);
		Assertions.assertFalse(isValid);
	}

	@Test
	void checkMaxFachstelleEinstellungNotOkTwoOfTwo() {
		var kindContainer = createKindContainer(true, EinschulungTyp.KINDERGARTEN2, IntegrationTyp.SOZIALE_INTEGRATION);
		kindContainer.getKindJA().getPensumFachstelle().add(createPensumFachstelleWithIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION));
		createEinstellungMock(kindContainer, FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE, "KINDERGARTEN1", 1);
		replayAll();
		var isValid = validator.isValid(kindContainer, null);
		Assertions.assertFalse(isValid);
	}

	@Test
	void checkMaxFachstelleEinstellungNotOkOneOfTwo() {
		var kindContainer = createKindContainer(true, EinschulungTyp.KINDERGARTEN2, IntegrationTyp.SOZIALE_INTEGRATION);
		kindContainer.getKindJA().getPensumFachstelle().add(createPensumFachstelleWithIntegrationTyp(IntegrationTyp.SPRACHLICHE_INTEGRATION));
		createEinstellungMock(kindContainer, FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE, "KINDERGARTEN1", 1);
		createEinstellungMock(kindContainer, SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE, "KINDERGARTEN1", 1);
		replayAll();
		var isValid = validator.isValid(kindContainer, null);
		Assertions.assertFalse(isValid);
	}

	private static PensumFachstelle createPensumFachstelleWithIntegrationTyp(IntegrationTyp integrationTyp) {
		PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setFachstelle(new Fachstelle());
		pensumFachstelle.setIntegrationTyp(integrationTyp);
		return pensumFachstelle;
	}

	@Test()
	void checkWrongEinstellung() {
		var kindContainer = createKindContainer(true, EinschulungTyp.KINDERGARTEN2, IntegrationTyp.SOZIALE_INTEGRATION);
		createEinstellungMock(kindContainer, FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE,"wrong", 1);
		replayAll();
		Assertions.assertThrows(EbeguRuntimeException.class, () -> {
			validator.isValid(kindContainer, null);
		});
	}

	@Test()
	void sprachlicheIntegrationVorschulalterValid() {
		var kindContainer = createKindContainer(true, EinschulungTyp.VORSCHULALTER, IntegrationTyp.SPRACHLICHE_INTEGRATION);
		createEinstellungMock(kindContainer, SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE,"VORSCHULALTER", 1);
		replayAll();
		var isValid = validator.isValid(kindContainer, null);
		Assertions.assertTrue(isValid);
	}

	@Test()
	void sprachlicheIntegrationVorschulalterNotValid() {
		var kindContainer = createKindContainer(true, EinschulungTyp.KINDERGARTEN1, IntegrationTyp.SPRACHLICHE_INTEGRATION);
		createEinstellungMock(kindContainer, SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE,"VORSCHULALTER", 1);
		replayAll();
		var isValid = validator.isValid(kindContainer, null);
		Assertions.assertFalse(isValid);
	}

	private void createEinstellungMock(KindContainer kindContainer, EinstellungKey key, String stufe, int times) {
		expect(einstellungServiceMock.findEinstellung(
			key,
			kindContainer.getGesuch().extractGemeinde(),
			kindContainer.getGesuch().getGesuchsperiode(),
			null
		))
			.andReturn(new Einstellung(
				key,
				stufe,
				kindContainer.getGesuch().getGesuchsperiode()
			)).times(times) ;
	}

	private KindContainer createKindContainer(
		boolean hasFachstelle,
		@Nonnull EinschulungTyp einschulungTyp,
		@Nonnull IntegrationTyp integrationTyp
	) {
		var fachstelle = hasFachstelle ? new Fachstelle() : null;
		var pensumFachstelle = new PensumFachstelle();
		var kind = new Kind();
		var kindContainer = new KindContainer();
		var gemeinde = new Gemeinde();
		var gesuchsperiode = new Gesuchsperiode();
		var gesuch = new Gesuch();
		var dossier = new Dossier();
		var fall = new Fall();
		var mandant = TestDataUtil.createDefaultMandant();

		fall.setMandant(mandant);
		pensumFachstelle.setFachstelle(fachstelle);
		pensumFachstelle.setIntegrationTyp(integrationTyp);
		kind.getPensumFachstelle().add(pensumFachstelle);
		kind.setEinschulungTyp(einschulungTyp);
		kindContainer.setKindJA(kind);
		gesuch.setDossier(dossier);
		dossier.setGemeinde(gemeinde);
		dossier.setFall(fall);
		gesuch.setGesuchsperiode(gesuchsperiode);
		kindContainer.setGesuch(gesuch);
		return kindContainer;
	}
}
