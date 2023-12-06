/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.activation.MimeTypeParseException;

import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KitaxUebergangsloesungInstitutionOeffnungszeiten;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.WriteProtectedDokument;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinConfigurator;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.RuleParameterUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;

@ExtendWith(EasyMockExtension.class)
public class VerfuegungServiceBeanUnitTest extends EasyMockSupport {

	@Mock
	GesuchService gesuchServiceMock;

	@Mock
	private Authorizer authorizer;

	@Mock
	FinanzielleSituationService finanzielleSituationServiceMock;

	@Mock
	GemeindeService gemeindeServiceMock;

	@Mock
	CriteriaQueryHelper criteriaQueryHelperMock;

	@Mock
	ApplicationPropertyService applicationPropertyServiceMock;

	@Mock
	RulesService rulesServiceMock;

	@Mock
	EinstellungService einstellungServiceMock;

	@Mock
	GeneratedDokumentService generatedDokumentServiceMock;

	@Mock
	Persistence persistenceMock;

	@Mock
	WizardStepService wizardStepServiceMock;

	@Mock
	MailServiceBean mailServiceBeanMock;

	@Mock
	BetreuungService betreuungServiceMock;

	@TestSubject
	private VerfuegungServiceBean verfuegungServiceBean = new VerfuegungServiceBean();
	@Test
	public void testMutationAutomatischVerfuegtMischgesuchShouldVerfuegenTagesschulAnmeldung() {
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(2020, 2021);
		Gesuch gesuch = TestDataUtil.createTestgesuchDagmar(new FinanzielleSituationBernRechner());
		gesuch.setEingangsart(Eingangsart.ONLINE);
		gesuch.setFinSitStatus(FinSitStatus.AKZEPTIERT);

		Gesuch mutation = TestDataUtil.createTestgesuchDagmar(new FinanzielleSituationBernRechner());
		final KindContainer kindContainer = mutation.getKindContainers().stream().findFirst().orElseThrow();

		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		betreuungsmitteilung.setBetreuung(mutation.getFirstBetreuung());

		AnmeldungTagesschule anmeldungTagesschule = TestDataUtil.createAnmeldungTagesschule(kindContainer, gesuchsperiode);
		anmeldungTagesschule.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN);
		kindContainer.getAnmeldungenTagesschule().add(anmeldungTagesschule);

		mutation.setVorgaengerId(gesuch.getId());
		mutation.setTyp(AntragTyp.MUTATION);
		mutation.setLaufnummer(1);
		mutation.setNewlyCreatedMutation(true);
		mutation.setVorgaengerId("UUID");
		mutation.setFinanzDatenDTO_alleine(new FinanzDatenDTO(BigDecimal.ZERO));


		kindContainer.getBetreuungen().forEach(betreuung -> {
			expectVerfuegungOfOnePlatzCalls(betreuung);
			try {
				expect(generatedDokumentServiceMock.getFinSitDokumentAccessTokenGeneratedDokument(mutation, true)).andReturn(new WriteProtectedDokument());
				expect(generatedDokumentServiceMock.getVerfuegungDokumentAccessTokenGeneratedDokument(mutation, betreuung ,"", true)).andReturn(new WriteProtectedDokument());
			} catch (MimeTypeParseException | MergeDocException | IOException e) {
				Assertions.fail();
			}
			mailServiceBeanMock.sendInfoBetreuungVerfuegt(betreuung);
			final Verfuegung verfuegung = new Verfuegung();
			verfuegung.setPlatz(betreuung);
			expect(persistenceMock.persist(anyObject(Verfuegung.class))).andReturn(verfuegung);

		});

		expectVerfuegungOfOnePlatzCalls(anmeldungTagesschule);
		final Verfuegung verfuegung = new Verfuegung();
		verfuegung.setPlatz(anmeldungTagesschule);
		anmeldungTagesschule.setVerfuegungPreview(verfuegung);
		expect(persistenceMock.merge(anyObject(Verfuegung.class))).andReturn(verfuegung);
		expect(persistenceMock.persist(anyObject(Verfuegung.class))).andReturn(verfuegung);
		expect(persistenceMock.merge(anyObject(AnmeldungTagesschule.class))).andReturn(anmeldungTagesschule);
		expect(betreuungServiceMock.saveAnmeldungTagesschule(anmeldungTagesschule)).andReturn(anmeldungTagesschule);
		try {
			expect(generatedDokumentServiceMock.getAnmeldeBestaetigungDokumentAccessTokenGeneratedDokument(mutation, anmeldungTagesschule, true, true)).andReturn(new WriteProtectedDokument());
		} catch (MimeTypeParseException | MergeDocException e) {
			Assertions.fail();
		}
		replayAll();
		verfuegungServiceBean.gesuchAutomatischVerfuegen(mutation);
		MatcherAssert.assertThat(anmeldungTagesschule.getVerfuegung(), Matchers.notNullValue());
	}

	private void expectVerfuegungOfOnePlatzCalls(AbstractPlatz platz) {
		Gesuch gesuch = platz.extractGesuch();

		expect(gesuchServiceMock.findGesuch(anyString())).andReturn(Optional.of(gesuch));
		authorizer.checkReadAuthorization(gesuch);
		finanzielleSituationServiceMock.calculateFinanzDaten(gesuch);
		final KitaxUebergangsloesungInstitutionOeffnungszeiten oeffnungszeiten = new KitaxUebergangsloesungInstitutionOeffnungszeiten();
		oeffnungszeiten.setNameKibon("nameKibon");
		oeffnungszeiten.setNameKitax("nameKitax");
		expect(criteriaQueryHelperMock.getAll(KitaxUebergangsloesungInstitutionOeffnungszeiten.class)).andReturn(Set.of(oeffnungszeiten));
		expect(gemeindeServiceMock.getGemeindeStammdatenByGemeindeId(anyString())).andReturn(Optional.of(new GemeindeStammdaten()));
		expect(applicationPropertyServiceMock.getStadtBernAsivStartDatum(anyObject())).andReturn(LocalDate.MIN);
		expect(applicationPropertyServiceMock.isStadtBernAsivConfigured(anyObject())).andReturn(true);
		expect(rulesServiceMock.getRulesForGesuchsperiode(anyObject(), anyObject(), anyObject(), anyObject())).andReturn(
			new BetreuungsgutscheinConfigurator().configureRulesForMandant(
				gesuch.extractGemeinde(),
				new RuleParameterUtil(TestDataUtil.prepareParameterMap(
					gesuch.getGesuchsperiode()),
					new KitaxUebergangsloesungParameter(LocalDate.MIN, false, Set.of(oeffnungszeiten)))));
		expect(applicationPropertyServiceMock.findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED,
			null,
			true)).andReturn(false);
		expect(einstellungServiceMock.loadRuleParameters(gesuch.extractGemeinde(), gesuch.getGesuchsperiode(), Set.of(
			EinstellungKey.EINGEWOEHNUNG_TYP, EinstellungKey.FKJV_PAUSCHALE_RUECKWIRKEND, EinstellungKey.ANSPRUCH_MONATSWEISE))).andReturn(TestDataUtil.prepareParameterMap(
			gesuch.getGesuchsperiode()));
		expect(einstellungServiceMock.getAllEinstellungenByGemeindeAsMap(gesuch.extractGemeinde(), gesuch.getGesuchsperiode())).andReturn(TestDataUtil.prepareParameterMap(
			gesuch.getGesuchsperiode()));
		authorizer.checkWriteAuthorization(anyObject(Verfuegung.class));
		expect(persistenceMock.merge(anyObject(AbstractPlatz.class))).andReturn(platz);
		expect(wizardStepServiceMock.updateSteps(gesuch.getId(), null, null, WizardStepName.VERFUEGEN)).andReturn(List.of());
		authorizer.checkReadAuthorizationForAnyPlaetze(gesuch.extractAllPlaetze());
		expect(applicationPropertyServiceMock.isPublishSchnittstelleEventsAktiviert(gesuch.extractMandant())).andReturn(false);
	}
}
