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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageContext;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageHandler;
import ch.dvbern.ebegu.nesko.utils.KibonAnfrageUtil;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.kibon.exchange.commons.neskovanp.NeueVeranlagungEventDTO;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class NeueVeranlagungEventHandler extends BaseEventHandler<NeueVeranlagungEventDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(NeueVeranlagungEventHandler.class);
	private static final String BETREFF_KEY = "neue_veranlagung_mitteilung_betreff";
	private static final String MESSAGE_KEY = "neue_veranlagung_mitteilung_message";


	@Inject
	private GesuchService gesuchService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private FinanzielleSituationService finanzielleSituationService;

	@Inject
	private KibonAnfrageHandler kibonAnfrageHandler;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private Persistence persistence;

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
			LOG.warn("NeueVeranlagungEventHandler: Neue Veranlagung Event für ZPV-Nummer {} und Gesuch: {} nicht verarbeitet: {}",
				dto.getZpvNummer(), key, message);
		} else {
			LOG.info("NeueVeranlagungEventHandler: Neue Veranlagung Event für ZPV-Nummer {} und Gesuch: {} verarbeitet",
				dto.getZpvNummer(), key);
		}
	}


	@Nonnull
	protected Processing attemptProcessing(@Nonnull String key, @Nonnull NeueVeranlagungEventDTO dto) {
		Gesuch gesuch = findDetachedGesuchByKey(key);

		if (gesuch == null) {
			return Processing.failure("Kein Gesuch für Key gefunen. Key: " + key);
		}

		// erst die Massgegebenes Einkommens fuer das betroffenes Gesuch berechnen
		FinanzielleSituationResultateDTO finSitOriginalResult = finanzielleSituationService.calculateResultate(gesuch);

		KibonAnfrageContext kibonAnfrageContext = requestCurrentSteuerdaten(gesuch, dto.getZpvNummer());

		if (kibonAnfrageContext == null) {
			return Processing.failure(
				"Die neue Veranlagung mit Geburtsdatum: "
					+ dto.getGeburtsdatum()
					+ ", koennte nicht mit einer gueltige Antragstellende verlinket werden.");
		}

		if (kibonAnfrageContext.getSteuerdatenAnfrageStatus() == null
			|| !kibonAnfrageContext.getSteuerdatenAnfrageStatus().isSteuerdatenAbfrageErfolgreich()) {
			return Processing.failure("Keine neue Veranlagung gefunden");
		}

		// Nur RECHTSKRAEFTIGE SteuerResponse sind zu betrachten
		if (kibonAnfrageContext.getSteuerdatenAnfrageStatus() != SteuerdatenAnfrageStatus.RECHTSKRAEFTIG) {
			return Processing.failure("Die neue Veranlagung ist noch nicht Rechtskraeftig");
		}

		FinanzielleSituationResultateDTO finSitNeuResult =
			finanzielleSituationService.calculateResultate(kibonAnfrageContext.getGesuch());

		// Vergleichen
		if (finSitOriginalResult.getMassgebendesEinkVorAbzFamGr().compareTo(finSitNeuResult.getMassgebendesEinkVorAbzFamGr()) == 0) {
			return Processing.failure("Keine Meldung erstellt, da das massgebende Einkommen sich mit der neuen Verfügung nicht verändert hat");
		}

		BigDecimal minUnterschiedEinkommen = getEinstelungMinUnterschiedEinkommen(kibonAnfrageContext.getGesuch().getGesuchsperiode());
		BigDecimal unterschiedEinkommen = MathUtil.EXACT.subtract(
			finSitNeuResult.getMassgebendesEinkVorAbzFamGr(),
			finSitOriginalResult.getMassgebendesEinkVorAbzFamGr());
		if (unterschiedEinkommen.compareTo(minUnterschiedEinkommen) <= 0) {
			String unterschiedEinkommenString = unterschiedEinkommen.stripTrailingZeros().toPlainString();
			return Processing.failure("Keine Meldung erstellt. Das massgebende Einkommen hat sich um " + unterschiedEinkommenString +
				" Franken verändert. Der konfigurierte Schwellenwert zur Benachrichtigung liegt bei " + minUnterschiedEinkommen +
				" Franken");
		}

		createAndSendNeueVeranlagungsMitteilung(kibonAnfrageContext);

		return Processing.success();
	}

	@SuppressWarnings("PMD.CloseResource")
	@Nullable
	private Gesuch findDetachedGesuchByKey(String key) {
		Optional<Gesuch> gesuchOpt = gesuchService.findGesuch(key);
		if (gesuchOpt.isEmpty()) {
			return null;
		}

		Gesuch gesuch = gesuchOpt.get();

		// Wir werden das Gesuch FinSit ersetzen mit die neue Steuerdaten, es muss unbedingt nicht persistiert werden
		// deswegen ist das Gesuch als detached gesetzt
		Session session = persistence.getEntityManager().unwrap(Session.class);
		session.evict(gesuch);
		return gesuch;
	}

	@Nullable
	private KibonAnfrageContext requestCurrentSteuerdaten(Gesuch gesuch, int zpvNummer) {
		KibonAnfrageContext kibonAnfrageContext = KibonAnfrageUtil.initKibonAnfrageContext(gesuch, zpvNummer);

		if (kibonAnfrageContext == null) {
			return null;
		}

		// entscheiden, ob es geht um das GS1 oder GS2
		Objects.requireNonNull(gesuch.getFamiliensituationContainer());
		Objects.requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());

		boolean gemeinsam = Boolean.TRUE
			.equals(gesuch.getFamiliensituationContainer().getFamiliensituationJA().getGemeinsameSteuererklaerung());

		return kibonAnfrageHandler.handleKibonAnfrage(kibonAnfrageContext, gemeinsam);
	}

	private void createAndSendNeueVeranlagungsMitteilung(KibonAnfrageContext kibonAnfrageContext) {
		Gesuch gesuch = kibonAnfrageContext.getGesuch();

		Locale locale = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService).getLocale();
		NeueVeranlagungsMitteilung neueVeranlagungsMitteilung = new NeueVeranlagungsMitteilung();
		neueVeranlagungsMitteilung.setDossier(gesuch.getDossier());
		Objects.requireNonNull(kibonAnfrageContext.getSteuerdatenResponse());
		neueVeranlagungsMitteilung.setSubject(ServerMessageUtil.getMessage(
			BETREFF_KEY,
			locale,
			gesuch.extractMandant()));
		neueVeranlagungsMitteilung.setMessage(ServerMessageUtil.getMessage(
			MESSAGE_KEY,
			locale,
			gesuch.extractMandant()));
		neueVeranlagungsMitteilung.setSteuerdatenResponse(kibonAnfrageContext.getSteuerdatenResponse());
		mitteilungService.sendNeueVeranlagungsmitteilung(neueVeranlagungsMitteilung);
	}

	private BigDecimal getEinstelungMinUnterschiedEinkommen(Gesuchsperiode gesuchsperiode) {
		List<Einstellung> einstellungList = einstellungService.findEinstellungen(
			EinstellungKey.VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK,
			gesuchsperiode);

		if (einstellungList.size() != 1) {
			throw new EbeguRuntimeException(
				"NeueVeranlagungEventHandler: ",
				"Es sollte exakt eine Einstellung für den VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK und die "
					+ "Gesuchsperiode "
					+ gesuchsperiode.getGesuchsperiodeString()
					+ " gefunden werden");
		}

		return einstellungList.get(0).getValueAsBigDecimal();
	}
}
