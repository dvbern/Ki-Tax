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

package ch.dvbern.ebegu.inbox.handler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageContext;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageHandler;
import ch.dvbern.ebegu.persistence.TransactionHelper;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.kibon.exchange.commons.neskovanp.NeueVeranlagungEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class NeueVeranlagungEventHandler extends BaseEventHandler<NeueVeranlagungEventDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(NeueVeranlagungEventHandler.class);

	@Inject
	private GesuchService gesuchService;

	@Inject
	private FinanzielleSituationService finanzielleSituationService;

	@Inject
	private KibonAnfrageHandler kibonAnfrageHandler;

	@Inject
	private EinstellungService einstellungService;

	@Resource
	private EJBContext context;    //fuer rollback

	@Inject
	private TransactionHelper transactionHelper;

	@Inject
	private MitteilungService mitteilungService;

	@Override
	protected void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull String key,
		@Nonnull NeueVeranlagungEventDTO dto,
		@Nonnull String clientName) {
		Processing processing = attemptProcessing(key, dto);
		if (!processing.isProcessingSuccess()) {
			String message = processing.getMessage();
			LOG.warn("Neue Veranlagung Event für Gesuch: {} nicht verarbeitet: {}", key, message);
		}
	}

	@Nonnull
	protected Processing attemptProcessing(@Nonnull String key, @Nonnull NeueVeranlagungEventDTO dto) {
		Optional<Gesuch> gesuchOpt = gesuchService.findGesuch(key);
		if (!gesuchOpt.isPresent()) {
			return Processing.failure("processEvent NeuVeranlagung GesuchId invalid: " + key);
		}
		Gesuch gesuch = gesuchOpt.get();

		// erst die Massgegebenes Einkommens fuer die betroffene Gesuch berechnen
		FinanzielleSituationResultateDTO finSitOrigResult = finanzielleSituationService.calculateResultate(gesuch);

		// --- Neue Zustand abholen ---

		// entscheiden ob es geht um das GS1 oder GS2
		KibonAnfrageContext kibonAnfrageContext = initKibonAnfrageContext(gesuch, dto.getZpvNummer());

		if (kibonAnfrageContext == null) {
			return Processing.failure(
				"NeueVeranlagungEventHandler: Die neue Veranlagung Steuerdaten Abfrage fuer ZPV Nummer: "
					+ dto.getZpvNummer()
					+ ", mit Geburtsdatum: + "
					+ dto.getGeburtsdatum()
					+ ", hat kein mehr gueltige Antragstellende gefunden.");
		}

		Objects.requireNonNull(gesuch.getFamiliensituationContainer());
		Objects.requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());
		boolean gemeinsam =
			gesuch.getFamiliensituationContainer().getFamiliensituationJA().getGemeinsameSteuererklaerung() != null
				? gesuch.getFamiliensituationContainer().getFamiliensituationJA().getGemeinsameSteuererklaerung()
				: false;
		kibonAnfrageContext = kibonAnfrageHandler.handleKibonAnfrage(
			kibonAnfrageContext,
			gemeinsam);

		// Fehlermeldung behandeln
		if (kibonAnfrageContext.getSteuerdatenAnfrageStatus() == null
			|| !kibonAnfrageContext.getSteuerdatenAnfrageStatus().isSteuerdatenAbfrageErfolgreich()) {
			return Processing.failure(
				"NeueVeranlagungEventHandler: es gab einen Problem bei abholen die neue Veranlagung Stand: "
					+ kibonAnfrageContext.getSteuerdatenAnfrageStatus());
		}

		assert kibonAnfrageContext != null;
		FinanzielleSituationResultateDTO finSitNeuResult =
			finanzielleSituationService.calculateResultate(kibonAnfrageContext.getGesuch());

		// Vergleichen
		List<Einstellung> einstellungList = einstellungService.findEinstellungen(
			EinstellungKey.VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK,
			gesuch.getGesuchsperiode());
		if (einstellungList.size() != 1) {
			throw new EbeguRuntimeException(
				"NeueVeranlagungEventHandler: ",
				"Es sollte exakt eine Einstellung für den VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK und die "
					+ "Gesuchsperiode "
					+ gesuch.getGesuchsperiode().getGesuchsperiodeString()
					+ " gefunden werden");
		}
		if (finSitOrigResult.getMassgebendesEinkVorAbzFamGr()
			.compareTo(finSitNeuResult.getMassgebendesEinkVorAbzFamGr()) == 0 ||
			MathUtil.EXACT.subtract(
				finSitNeuResult.getMassgebendesEinkVorAbzFamGr(),
				finSitOrigResult.getMassgebendesEinkVorAbzFamGr())
				.compareTo(einstellungList.get(0).getValueAsBigDecimal()) <= 0
		) {
			return Processing.failure("NeueVeranlagungEventHandler: die neue VeranlagungStand abweich nicht genugen");
		}
		// Meldung erstellen in einen neuen Transaktion
		//transactionHelper.runInNewTransaction(() -> {
		NeueVeranlagungsMitteilung neueVeranlagungsMitteilung = new NeueVeranlagungsMitteilung();
		neueVeranlagungsMitteilung.setDossier(gesuch.getDossier());
		Objects.requireNonNull(kibonAnfrageContext.getSteuerdatenResponse());
		neueVeranlagungsMitteilung.setSubject("Neue Veranlagung");
		neueVeranlagungsMitteilung.setMessage("Neue Veranlagung");
		neueVeranlagungsMitteilung.setSteuerdatenResponse(kibonAnfrageContext.getSteuerdatenResponse());
		mitteilungService.sendNeueVeranlagungsmitteilung(neueVeranlagungsMitteilung);
		LOG.info("NeueVeranlagungEventHandler: IT WORKS");
		//});
		return Processing.success();
	}

	/**
	 * Bestimmen ob der Veranlagung Event betrifft der GS1 oder GS2 und das Context entsprechend initialisieren
	 *
	 * @return KibonAnfrageContext
	 */
	private KibonAnfrageContext initKibonAnfrageContext(@Nonnull Gesuch gesuch, int zpvNummer) {
		KibonAnfrageContext kibonAnfrageContext = null;
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		if (gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.getSteuerdatenResponse() != null && gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.getSteuerdatenResponse()
			.getZpvNrDossiertraeger() != null) {
			if (gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerdatenResponse()
				.getZpvNrDossiertraeger()
				.equals(zpvNummer)) {
				kibonAnfrageContext = new KibonAnfrageContext(
					gesuch,
					gesuch.getGesuchsteller1(),
					gesuch.getGesuchsteller1().getFinanzielleSituationContainer(),
					gesuch.getId());
			}
		} else {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			Objects.requireNonNull(gesuch.getGesuchsteller2()
				.getFinanzielleSituationContainer());
			if (gesuch.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerdatenResponse() != null && gesuch.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerdatenResponse()
				.getZpvNrDossiertraeger() != null) {
				if (gesuch.getGesuchsteller2()
					.getFinanzielleSituationContainer()
					.getFinanzielleSituationJA()
					.getSteuerdatenResponse()
					.getZpvNrDossiertraeger()
					.equals(zpvNummer)) {
					kibonAnfrageContext = new KibonAnfrageContext(
						gesuch,
						gesuch.getGesuchsteller2(),
						gesuch.getGesuchsteller2().getFinanzielleSituationContainer(),
						gesuch.getId());
				}
			}
		}
		return kibonAnfrageContext;
	}
}
