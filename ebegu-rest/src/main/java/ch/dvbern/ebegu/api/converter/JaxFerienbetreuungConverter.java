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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.converter;

import ch.dvbern.ebegu.api.dtos.JaxAdresse;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.*;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.*;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.hibernate.StaleObjectStateException;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@RequestScoped
public class JaxFerienbetreuungConverter extends AbstractConverter {

	@Inject
	private Persistence persistence;

	@Inject
	private JaxBenutzerConverter jaxBenutzerConverter;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private BenutzerService benutzerService;

	/**
	 * Behandlung des Version-Attributes fuer OptimisticLocking.
	 * Nachdem die Business-Logik durchgefuehrt worden ist, stimmt moeglicherweise die
	 * Version bereits wieder nicht mehr. Darum muss am Schluss, also beim Konvertieren
	 * von Entity zurueck zu Jax, nochmals geflusht werden, damit der Client die
	 * richtige Version zurueckerhaelt, sonst klappt das naechste Speichern nicht mehr.
	 */
	private void flush() {
		persistence.getEntityManager().flush(); // FLUSH -- otherwise the version is not incremented yet
	}

	@Nonnull
	public FerienbetreuungAngabenContainer ferienbetreuungenAngabenContainerToEntity(
		@Nonnull JaxFerienbetreuungAngabenContainer jaxContainer,
		@Nonnull FerienbetreuungAngabenContainer container
	) {
		requireNonNull(jaxContainer.getGemeinde().getId());

		convertAbstractFieldsToEntity(jaxContainer, container);

		// never set status, gemeinde and gesuchsperiode from client

		gemeindeService.findGemeinde(jaxContainer.getGemeinde().getId())
			.ifPresent(container::setGemeinde);


		container.setAngabenDeklaration(ferienbetreuungenAngabenToEntity(
			jaxContainer.getAngabenDeklaration(),
			container.getAngabenDeklaration()
		));

		if (container.getAngabenKorrektur() != null && jaxContainer.getAngabenKorrektur() != null) {
			container.setAngabenKorrektur(ferienbetreuungenAngabenToEntity(
				jaxContainer.getAngabenKorrektur(),
				//TODO sollte hier nicht Angaben Korrektur sein?
				container.getAngabenDeklaration()
			));
		}

		container.setInternerKommentar(jaxContainer.getInternerKommentar());

		if (jaxContainer.getVerantwortlicher() != null) {
			benutzerService.findBenutzer(jaxContainer.getVerantwortlicher().getUsername(),
					container.getGemeinde().getMandant())
				.ifPresent(container::setVerantwortlicher);
		}

		return container;
	}

	@Nonnull
	public FerienbetreuungAngaben ferienbetreuungenAngabenToEntity(
		@Nonnull JaxFerienbetreuungAngaben jaxContainer,
		@Nonnull FerienbetreuungAngaben ferienbetreuungAngaben
	) {
		convertAbstractFieldsToEntity(jaxContainer, ferienbetreuungAngaben);

		// stammdaten
		ferienbetreuungAngaben.setFerienbetreuungAngabenStammdaten(ferienbetreuungAngabenStammdatenToEntity(
			jaxContainer.getStammdaten(),
			ferienbetreuungAngaben.getFerienbetreuungAngabenStammdaten()
		));
		// angebot
		ferienbetreuungAngaben.setFerienbetreuungAngabenAngebot(ferienbetreuungAngabenAngebotToEntity(
			jaxContainer.getAngebot(),
			ferienbetreuungAngaben.getFerienbetreuungAngabenAngebot()
		));
		// nutzung
		ferienbetreuungAngaben.setFerienbetreuungAngabenNutzung(ferienbetreuungAngabenNutzungToEntity(
			jaxContainer.getNutzung(),
			ferienbetreuungAngaben.getFerienbetreuungAngabenNutzung()
		));
		// kosten und einnahmen
		ferienbetreuungAngaben.setFerienbetreuungAngabenKostenEinnahmen(ferienbetreuungAngabenKostenEinnahmenToEntity(
			jaxContainer.getKostenEinnahmen(),
			ferienbetreuungAngaben.getFerienbetreuungAngabenKostenEinnahmen()
		));
		//berechnungen
		ferienbetreuungAngaben.setFerienbetreuungBerechnungen(ferienbetreuungBerechnungentoEntity(
			jaxContainer.getBerechnungen(),
			new FerienbetreuungBerechnungen()
		));


		// never save resultate from client

		return ferienbetreuungAngaben;

	}

	public FerienbetreuungBerechnungen ferienbetreuungBerechnungentoEntity(
		@Nonnull JaxFerienbetreuungBerechnungen jaxBerechnungen,
		@Nonnull FerienbetreuungBerechnungen ferienbetreuungBerechnungen) {

		convertAbstractFieldsToEntity(jaxBerechnungen, ferienbetreuungBerechnungen);

		ferienbetreuungBerechnungen.setTotalKosten(jaxBerechnungen.getTotalKosten());
		ferienbetreuungBerechnungen.setBetreuungstageKinderAndererGemeindeMinusSonderschueler(jaxBerechnungen.getBetreuungstageKinderAndererGemeindeMinusSonderschueler());
		ferienbetreuungBerechnungen.setBetreuungstageKinderDieserGemeindeMinusSonderschueler(jaxBerechnungen.getBetreuungstageKinderDieserGemeindeMinusSonderschueler());
		ferienbetreuungBerechnungen.setTotalKantonsbeitrag(jaxBerechnungen.getTotalKantonsbeitrag());
		ferienbetreuungBerechnungen.setTotalEinnahmen(jaxBerechnungen.getTotalEinnahmen());
		ferienbetreuungBerechnungen.setBeitragKinderAnbietendenGemeinde(jaxBerechnungen.getBeitragKinderAnbietendenGemeinde());
		ferienbetreuungBerechnungen.setBeteiligungZuTief(jaxBerechnungen.getBeteiligungZuTief());
		ferienbetreuungBerechnungen.setBeteiligungAnbietendenGemeinde(jaxBerechnungen.getBeteiligungAnbietendenGemeinde());

		return ferienbetreuungBerechnungen;
	}

	public FerienbetreuungAngabenStammdaten ferienbetreuungAngabenStammdatenToEntity(
		@Nonnull JaxFerienbetreuungAngabenStammdaten jaxStammdaten,
		@Nonnull FerienbetreuungAngabenStammdaten stammdaten
	) {
		if (stammdaten.getVersion() != jaxStammdaten.getVersion()) {
			throw new WebApplicationException(new StaleObjectStateException("Die FerienbetreuungAngabenStammdaten Versionen stimmen nicht",
				stammdaten.getId()), Status.CONFLICT);
		}

		convertAbstractFieldsToEntity(jaxStammdaten, stammdaten);

		if (jaxStammdaten.getAmAngebotBeteiligteGemeinden() != null) {
			stammdaten.setAmAngebotBeteiligteGemeinden(jaxStammdaten.getAmAngebotBeteiligteGemeinden());
		} else {
			stammdaten.setAmAngebotBeteiligteGemeinden(Collections.emptySet());
		}
		stammdaten.setSeitWannFerienbetreuungen(jaxStammdaten.getSeitWannFerienbetreuungen());
		stammdaten.setTraegerschaft(jaxStammdaten.getTraegerschaft());
		if (jaxStammdaten.getStammdatenAdresse() != null) {
			if (stammdaten.getStammdatenAdresse() == null) {
				stammdaten.setStammdatenAdresse(new Adresse());
			}
			stammdaten.setStammdatenAdresse(
				adresseToEntity(jaxStammdaten.getStammdatenAdresse(), stammdaten.getStammdatenAdresse())
			);
		}
		stammdaten.setStammdatenKontaktpersonVorname(jaxStammdaten.getStammdatenKontaktpersonVorname());
		stammdaten.setStammdatenKontaktpersonNachname(jaxStammdaten.getStammdatenKontaktpersonNachname());
		stammdaten.setStammdatenKontaktpersonFunktion(jaxStammdaten.getStammdatenKontaktpersonFunktion());
		stammdaten.setStammdatenKontaktpersonTelefon(jaxStammdaten.getStammdatenKontaktpersonTelefon());
		stammdaten.setStammdatenKontaktpersonEmail(jaxStammdaten.getStammdatenKontaktpersonEmail());
		if (jaxStammdaten.getIban() != null && jaxStammdaten.getKontoinhaber() != null) {
			Auszahlungsdaten auszahlungsdaten = stammdaten.getAuszahlungsdaten();
			if (auszahlungsdaten == null) {
				auszahlungsdaten = new Auszahlungsdaten();
			}
			auszahlungsdaten.setIban(new IBAN(jaxStammdaten.getIban()));
			auszahlungsdaten.setKontoinhaber(jaxStammdaten.getKontoinhaber());
			if (jaxStammdaten.getAdresseKontoinhaber() != null) {
				Adresse adresse = auszahlungsdaten.getAdresseKontoinhaber();
				if (adresse == null) {
					adresse = new Adresse();
				}
				auszahlungsdaten.setAdresseKontoinhaber(super.adresseToEntity(
					jaxStammdaten.getAdresseKontoinhaber(),
					adresse));
			}
			stammdaten.setAuszahlungsdaten(auszahlungsdaten);
		} else {
			stammdaten.setAuszahlungsdaten(null);
		}
		stammdaten.setVermerkAuszahlung(jaxStammdaten.getVermerkAuszahlung());

		return stammdaten;
	}

	public FerienbetreuungAngabenAngebot ferienbetreuungAngabenAngebotToEntity(
		@Nonnull JaxFerienbetreuungAngabenAngebot jaxAngebot,
		@Nonnull FerienbetreuungAngabenAngebot angebot
	) {
		if (angebot.getVersion() != jaxAngebot.getVersion()) {
			throw new WebApplicationException(new StaleObjectStateException("Die FerienbetreuungAngabenAngebot Versionen stimmen nicht",
				angebot.getId()), Status.CONFLICT);
		}
		convertAbstractFieldsToEntity(jaxAngebot, angebot);

		angebot.setAngebot(jaxAngebot.getAngebot());
		angebot.setAngebotKontaktpersonVorname(jaxAngebot.getAngebotKontaktpersonVorname());
		angebot.setAngebotKontaktpersonNachname(jaxAngebot.getAngebotKontaktpersonNachname());
		if (jaxAngebot.getAngebotAdresse() != null) {
			if (angebot.getAngebotAdresse() == null) {
				angebot.setAngebotAdresse(new Adresse());
			}
			angebot.setAngebotAdresse(
				adresseToEntity(jaxAngebot.getAngebotAdresse(), angebot.getAngebotAdresse())
			);
		}
		angebot.setAnzahlFerienwochenHerbstferien(jaxAngebot.getAnzahlFerienwochenHerbstferien());
		angebot.setAnzahlFerienwochenWinterferien(jaxAngebot.getAnzahlFerienwochenWinterferien());
		angebot.setAnzahlFerienwochenSportferien(jaxAngebot.getAnzahlFerienwochenSportferien());
		angebot.setAnzahlFerienwochenFruehlingsferien(jaxAngebot.getAnzahlFerienwochenFruehlingsferien());
		angebot.setAnzahlFerienwochenSommerferien(jaxAngebot.getAnzahlFerienwochenSommerferien());
		angebot.setAnzahlTage(jaxAngebot.getAnzahlTage());
		angebot.setBemerkungenAnzahlFerienwochen(jaxAngebot.getBemerkungenAnzahlFerienwochen());
		angebot.setAnzahlStundenProBetreuungstag(jaxAngebot.getAnzahlStundenProBetreuungstag());
		angebot.setBetreuungErfolgtTagsueber(jaxAngebot.getBetreuungErfolgtTagsueber());
		angebot.setBemerkungenOeffnungszeiten(jaxAngebot.getBemerkungenOeffnungszeiten());

		if (jaxAngebot.getFinanziellBeteiligteGemeinden() != null) {
			angebot.setFinanziellBeteiligteGemeinden(jaxAngebot.getFinanziellBeteiligteGemeinden());
		} else {
			angebot.setFinanziellBeteiligteGemeinden(Collections.emptySet());
		}

		angebot.setGemeindeFuehrtAngebotSelber(jaxAngebot.getGemeindeFuehrtAngebotSelber());
		angebot.setGemeindeFuehrtAngebotInKooperation(jaxAngebot.getGemeindeFuehrtAngebotInKooperation());
		angebot.setGemeindeBeauftragtExterneAnbieter(jaxAngebot.getGemeindeBeauftragtExterneAnbieter());
		angebot.setAngebotVereineUndPrivateIntegriert(jaxAngebot.getAngebotVereineUndPrivateIntegriert());
		angebot.setBemerkungenKooperation(jaxAngebot.getBemerkungenKooperation());
		angebot.setLeitungDurchPersonMitAusbildung(jaxAngebot.getLeitungDurchPersonMitAusbildung());
		angebot.setBetreuungDurchPersonenMitErfahrung(jaxAngebot.getBetreuungDurchPersonenMitErfahrung());
		angebot.setAnzahlKinderAngemessen(jaxAngebot.getAnzahlKinderAngemessen());
		angebot.setBetreuungsschluessel(jaxAngebot.getBetreuungsschluessel());
		angebot.setBemerkungenPersonal(jaxAngebot.getBemerkungenPersonal());
		angebot.setFixerTarifKinderDerGemeinde(jaxAngebot.getFixerTarifKinderDerGemeinde());
		angebot.setEinkommensabhaengigerTarifKinderDerGemeinde(jaxAngebot.getEinkommensabhaengigerTarifKinderDerGemeinde());
		angebot.setTagesschuleTarifGiltFuerFerienbetreuung(jaxAngebot.getTagesschuleTarifGiltFuerFerienbetreuung());
		angebot.setFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(jaxAngebot.getFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet());
		angebot.setKinderAusAnderenGemeindenZahlenAnderenTarif(jaxAngebot.getKinderAusAnderenGemeindenZahlenAnderenTarif());
		angebot.setBemerkungenTarifsystem(jaxAngebot.getBemerkungenTarifsystem());

		return angebot;
	}

	public FerienbetreuungAngabenNutzung ferienbetreuungAngabenNutzungToEntity(
		@Nonnull JaxFerienbetreuungAngabenNutzung jaxNutzung,
		@Nonnull FerienbetreuungAngabenNutzung nutzung
	) {
		if (nutzung.getVersion() != jaxNutzung.getVersion()) {
			throw new WebApplicationException(new StaleObjectStateException("Die FerienbetreuungAngabenNutzung Versionen stimmen nicht",
				nutzung.getId()), Status.CONFLICT);
		}
		convertAbstractFieldsToEntity(jaxNutzung, nutzung);

		nutzung.setAnzahlBetreuungstageKinderBern(jaxNutzung.getAnzahlBetreuungstageKinderBern());
		nutzung.setBetreuungstageKinderDieserGemeinde(jaxNutzung.getBetreuungstageKinderDieserGemeinde());
		nutzung.setBetreuungstageKinderDieserGemeindeSonderschueler(jaxNutzung.getBetreuungstageKinderDieserGemeindeSonderschueler());
		nutzung.setDavonBetreuungstageKinderAndererGemeinden(jaxNutzung.getDavonBetreuungstageKinderAndererGemeinden());
		nutzung.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(jaxNutzung.getDavonBetreuungstageKinderAndererGemeindenSonderschueler());
		nutzung.setAnzahlBetreuteKinder(jaxNutzung.getAnzahlBetreuteKinder());
		nutzung.setAnzahlBetreuteKinderSonderschueler(jaxNutzung.getAnzahlBetreuteKinderSonderschueler());
		nutzung.setAnzahlBetreuteKinder1Zyklus(jaxNutzung.getAnzahlBetreuteKinder1Zyklus());
		nutzung.setAnzahlBetreuteKinder2Zyklus(jaxNutzung.getAnzahlBetreuteKinder2Zyklus());
		nutzung.setAnzahlBetreuteKinder3Zyklus(jaxNutzung.getAnzahlBetreuteKinder3Zyklus());

		return nutzung;
	}

	public FerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngabenKostenEinnahmenToEntity(
		@Nonnull JaxFerienbetreuungAngabenKostenEinnahmen jaxKostenEinnahmen,
		@Nonnull FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen
	) {
		if (kostenEinnahmen.getVersion() != jaxKostenEinnahmen.getVersion()) {
			throw new WebApplicationException(new StaleObjectStateException("Die FerienbetreuungAngabenKostenEinnahmen Versionen stimmen nicht",
				kostenEinnahmen.getId()), Status.CONFLICT);
		}
		convertAbstractFieldsToEntity(jaxKostenEinnahmen, kostenEinnahmen);

		kostenEinnahmen.setPersonalkosten(jaxKostenEinnahmen.getPersonalkosten());
		kostenEinnahmen.setPersonalkostenLeitungAdmin(jaxKostenEinnahmen.getPersonalkostenLeitungAdmin());
		kostenEinnahmen.setSachkosten(jaxKostenEinnahmen.getSachkosten());
		kostenEinnahmen.setVerpflegungskosten(jaxKostenEinnahmen.getVerpflegungskosten());
		kostenEinnahmen.setWeitereKosten(jaxKostenEinnahmen.getWeitereKosten());
		kostenEinnahmen.setBemerkungenKosten(jaxKostenEinnahmen.getBemerkungenKosten());
		kostenEinnahmen.setElterngebuehren(jaxKostenEinnahmen.getElterngebuehren());
		kostenEinnahmen.setWeitereEinnahmen(jaxKostenEinnahmen.getWeitereEinnahmen());
		kostenEinnahmen.setSockelbeitrag(jaxKostenEinnahmen.getSockelbeitrag());
		kostenEinnahmen.setBeitraegeNachAnmeldungen(jaxKostenEinnahmen.getBeitraegeNachAnmeldungen());
		kostenEinnahmen.setVorfinanzierteKantonsbeitraege(jaxKostenEinnahmen.getVorfinanzierteKantonsbeitraege());
		kostenEinnahmen.setEigenleistungenGemeinde(jaxKostenEinnahmen.getEigenleistungenGemeinde());

		return kostenEinnahmen;
	}

	@Nonnull
	public JaxFerienbetreuungAngabenContainer ferienbetreuungAngabenContainerToJax(
		@Nonnull final FerienbetreuungAngabenContainer container
	) {
		flush();

		JaxFerienbetreuungAngabenContainer jaxContainer = new JaxFerienbetreuungAngabenContainer();
		convertAbstractFieldsToJAX(container, jaxContainer);

		jaxContainer.setStatus(container.getStatus());
		jaxContainer.setGemeinde(gemeindeToJAX(container.getGemeinde()));
		jaxContainer.setGesuchsperiode(gesuchsperiodeToJAX(container.getGesuchsperiode()));
		jaxContainer.setAngabenDeklaration(ferienbetreuungAngabenToJax(container.getAngabenDeklaration()));
		if (container.getAngabenKorrektur() != null) {
			jaxContainer.setAngabenKorrektur(ferienbetreuungAngabenToJax(container.getAngabenKorrektur()));
		}
		jaxContainer.setInternerKommentar(container.getInternerKommentar());

		if (container.getVerantwortlicher() != null) {
			jaxContainer.setVerantwortlicher(jaxBenutzerConverter.benutzerToJaxBenutzerNoDetails(container.getVerantwortlicher()));
		}
		return jaxContainer;
	}

	@Nonnull
	public JaxFerienbetreuungAngaben ferienbetreuungAngabenToJax(
		@Nonnull final FerienbetreuungAngaben ferienbetreuungAngaben
	) {
		flush();

		JaxFerienbetreuungAngaben jaxFerienbetreuungAngaben = new JaxFerienbetreuungAngaben();
		convertAbstractFieldsToJAX(ferienbetreuungAngaben, jaxFerienbetreuungAngaben);

		// stammdaten
		JaxFerienbetreuungAngabenStammdaten jaxStammdaten = ferienbetreuungAngabenStammdatenToJax(
			ferienbetreuungAngaben.getFerienbetreuungAngabenStammdaten()
		);
		jaxFerienbetreuungAngaben.setStammdaten(jaxStammdaten);

		// angebot
		JaxFerienbetreuungAngabenAngebot angebot = ferienbetreuungAngabenAngebotToJax(
			ferienbetreuungAngaben.getFerienbetreuungAngabenAngebot()
		);
		jaxFerienbetreuungAngaben.setAngebot(angebot);

		// nutzung
		JaxFerienbetreuungAngabenNutzung nutzung = ferienbetreuungAngabenNutzungToJax(
			ferienbetreuungAngaben.getFerienbetreuungAngabenNutzung()
		);
		jaxFerienbetreuungAngaben.setNutzung(nutzung);

		// kosten und einnahmen
		JaxFerienbetreuungAngabenKostenEinnahmen kostenEinnahmen = ferienbetreuungAngabenKostenEinnahmenToJax(
			ferienbetreuungAngaben.getFerienbetreuungAngabenKostenEinnahmen()
		);
		jaxFerienbetreuungAngaben.setKostenEinnahmen(kostenEinnahmen);

		// berechnungen
		JaxFerienbetreuungBerechnungen berechnungen = ferienbetreuungBerechnungenToJax(
			ferienbetreuungAngaben.getFerienbetreuungBerechnungen()
		);
		jaxFerienbetreuungAngaben.setBerechnungen(berechnungen);


		// resultate
		jaxFerienbetreuungAngaben.setGemeindebeitrag(ferienbetreuungAngaben.getGemeindebeitrag());
		jaxFerienbetreuungAngaben.setKantonsbeitrag(ferienbetreuungAngaben.getKantonsbeitrag());

		return jaxFerienbetreuungAngaben;

	}

	public JaxFerienbetreuungBerechnungen ferienbetreuungBerechnungenToJax(FerienbetreuungBerechnungen ferienbetreuungBerechnungen) {
		flush();

		JaxFerienbetreuungBerechnungen jaxBerechnungen = new JaxFerienbetreuungBerechnungen();

		convertAbstractFieldsToJAX(ferienbetreuungBerechnungen, jaxBerechnungen);

		jaxBerechnungen.setTotalKosten(ferienbetreuungBerechnungen.getTotalKosten());
		jaxBerechnungen.setBetreuungstageKinderDieserGemeindeMinusSonderschueler(ferienbetreuungBerechnungen.getBetreuungstageKinderDieserGemeindeMinusSonderschueler());
		jaxBerechnungen.setBetreuungstageKinderAndererGemeindeMinusSonderschueler(ferienbetreuungBerechnungen.getBetreuungstageKinderAndererGemeindeMinusSonderschueler());
		jaxBerechnungen.setTotalKantonsbeitrag(ferienbetreuungBerechnungen.getTotalKantonsbeitrag());
		jaxBerechnungen.setTotalEinnahmen(ferienbetreuungBerechnungen.getTotalEinnahmen());
		jaxBerechnungen.setBeitragKinderAnbietendenGemeinde(ferienbetreuungBerechnungen.getBeitragKinderAnbietendenGemeinde());
		jaxBerechnungen.setBeteiligungAnbietendenGemeinde(ferienbetreuungBerechnungen.getBeteiligungAnbietendenGemeinde());
		jaxBerechnungen.setBeteiligungZuTief(ferienbetreuungBerechnungen.getBeteiligungZuTief());

		return jaxBerechnungen;
	}

	@Nonnull
	public JaxFerienbetreuungAngabenStammdaten ferienbetreuungAngabenStammdatenToJax(
		@Nonnull FerienbetreuungAngabenStammdaten stammdaten
	) {
		flush();

		JaxFerienbetreuungAngabenStammdaten jaxStammdaten = new JaxFerienbetreuungAngabenStammdaten();

		convertAbstractFieldsToJAX(stammdaten, jaxStammdaten);

		jaxStammdaten.setAmAngebotBeteiligteGemeinden(stammdaten.getAmAngebotBeteiligteGemeinden());
		jaxStammdaten.setSeitWannFerienbetreuungen(stammdaten.getSeitWannFerienbetreuungen());
		jaxStammdaten.setTraegerschaft(stammdaten.getTraegerschaft());
		if (stammdaten.getStammdatenAdresse() != null) {
			jaxStammdaten.setStammdatenAdresse(adresseToJAX(stammdaten.getStammdatenAdresse()));
		}
		jaxStammdaten.setStammdatenKontaktpersonVorname(stammdaten.getStammdatenKontaktpersonVorname());
		jaxStammdaten.setStammdatenKontaktpersonNachname(stammdaten.getStammdatenKontaktpersonNachname());
		jaxStammdaten.setStammdatenKontaktpersonFunktion(stammdaten.getStammdatenKontaktpersonFunktion());
		jaxStammdaten.setStammdatenKontaktpersonTelefon(stammdaten.getStammdatenKontaktpersonTelefon());
		jaxStammdaten.setStammdatenKontaktpersonEmail(stammdaten.getStammdatenKontaktpersonEmail());
		if (stammdaten.getAuszahlungsdaten() != null) {
			jaxStammdaten.setIban(stammdaten.getAuszahlungsdaten().getIban().getIban());
			jaxStammdaten.setKontoinhaber(stammdaten.getAuszahlungsdaten().getKontoinhaber());
			if (stammdaten.getAuszahlungsdaten().getAdresseKontoinhaber() != null) {
				jaxStammdaten.setAdresseKontoinhaber(
					adresseToJAX(stammdaten.getAuszahlungsdaten().getAdresseKontoinhaber())
				);
			}
		}
		jaxStammdaten.setVermerkAuszahlung(stammdaten.getVermerkAuszahlung());
		jaxStammdaten.setStatus(stammdaten.getStatus());

		return jaxStammdaten;
	}

	@Nonnull
	public JaxFerienbetreuungAngabenAngebot ferienbetreuungAngabenAngebotToJax(
		@Nonnull FerienbetreuungAngabenAngebot angebot
	) {
		flush();

		JaxFerienbetreuungAngabenAngebot jaxAngebot = new JaxFerienbetreuungAngabenAngebot();

		convertAbstractFieldsToJAX(angebot, jaxAngebot);

		jaxAngebot.setStatus(angebot.getStatus());

		jaxAngebot.setAngebot(angebot.getAngebot());
		jaxAngebot.setAngebotKontaktpersonVorname(angebot.getAngebotKontaktpersonVorname());
		jaxAngebot.setAngebotKontaktpersonNachname(angebot.getAngebotKontaktpersonNachname());
		if (angebot.getAngebotAdresse() != null) {
			jaxAngebot.setAngebotAdresse(
				adresseToJAX(angebot.getAngebotAdresse())
			);
		}
		jaxAngebot.setAnzahlFerienwochenHerbstferien(angebot.getAnzahlFerienwochenHerbstferien());
		jaxAngebot.setAnzahlFerienwochenWinterferien(angebot.getAnzahlFerienwochenWinterferien());
		jaxAngebot.setAnzahlFerienwochenSportferien(angebot.getAnzahlFerienwochenSportferien());
		jaxAngebot.setAnzahlFerienwochenFruehlingsferien(angebot.getAnzahlFerienwochenFruehlingsferien());
		jaxAngebot.setAnzahlFerienwochenSommerferien(angebot.getAnzahlFerienwochenSommerferien());
		jaxAngebot.setAnzahlTage(angebot.getAnzahlTage());
		jaxAngebot.setBemerkungenAnzahlFerienwochen(angebot.getBemerkungenAnzahlFerienwochen());
		jaxAngebot.setAnzahlStundenProBetreuungstag(angebot.getAnzahlStundenProBetreuungstag());
		jaxAngebot.setBetreuungErfolgtTagsueber(angebot.getBetreuungErfolgtTagsueber());
		jaxAngebot.setBemerkungenOeffnungszeiten(angebot.getBemerkungenOeffnungszeiten());
		jaxAngebot.setFinanziellBeteiligteGemeinden(angebot.getFinanziellBeteiligteGemeinden());
		jaxAngebot.setGemeindeFuehrtAngebotSelber(angebot.getGemeindeFuehrtAngebotSelber());
		jaxAngebot.setGemeindeFuehrtAngebotInKooperation(angebot.getGemeindeFuehrtAngebotInKooperation());
		jaxAngebot.setGemeindeBeauftragtExterneAnbieter(angebot.getGemeindeBeauftragtExterneAnbieter());
		jaxAngebot.setAngebotVereineUndPrivateIntegriert(angebot.getAngebotVereineUndPrivateIntegriert());
		jaxAngebot.setBemerkungenKooperation(angebot.getBemerkungenKooperation());
		jaxAngebot.setLeitungDurchPersonMitAusbildung(angebot.getLeitungDurchPersonMitAusbildung());
		jaxAngebot.setBetreuungDurchPersonenMitErfahrung(angebot.getBetreuungDurchPersonenMitErfahrung());
		jaxAngebot.setAnzahlKinderAngemessen(angebot.getAnzahlKinderAngemessen());
		jaxAngebot.setBetreuungsschluessel(angebot.getBetreuungsschluessel());
		jaxAngebot.setBemerkungenPersonal(angebot.getBemerkungenPersonal());
		jaxAngebot.setFixerTarifKinderDerGemeinde(angebot.getFixerTarifKinderDerGemeinde());
		jaxAngebot.setEinkommensabhaengigerTarifKinderDerGemeinde(angebot.getEinkommensabhaengigerTarifKinderDerGemeinde());
		jaxAngebot.setTagesschuleTarifGiltFuerFerienbetreuung(angebot.getTagesschuleTarifGiltFuerFerienbetreuung());
		jaxAngebot.setFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(angebot.getFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet());
		jaxAngebot.setKinderAusAnderenGemeindenZahlenAnderenTarif(angebot.getKinderAusAnderenGemeindenZahlenAnderenTarif());
		jaxAngebot.setBemerkungenTarifsystem(angebot.getBemerkungenTarifsystem());

		return jaxAngebot;
	}

	@Nonnull
	public JaxFerienbetreuungAngabenNutzung ferienbetreuungAngabenNutzungToJax(
		@Nonnull FerienbetreuungAngabenNutzung nutzung
	) {
		flush();

		JaxFerienbetreuungAngabenNutzung jaxNutzung = new JaxFerienbetreuungAngabenNutzung();

		convertAbstractFieldsToJAX(nutzung, jaxNutzung);

		jaxNutzung.setAnzahlBetreuungstageKinderBern(nutzung.getAnzahlBetreuungstageKinderBern());
		jaxNutzung.setBetreuungstageKinderDieserGemeinde(nutzung.getBetreuungstageKinderDieserGemeinde());
		jaxNutzung.setBetreuungstageKinderDieserGemeindeSonderschueler(nutzung.getBetreuungstageKinderDieserGemeindeSonderschueler());
		jaxNutzung.setDavonBetreuungstageKinderAndererGemeinden(nutzung.getDavonBetreuungstageKinderAndererGemeinden());
		jaxNutzung.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(nutzung.getDavonBetreuungstageKinderAndererGemeindenSonderschueler());
		jaxNutzung.setAnzahlBetreuteKinder(nutzung.getAnzahlBetreuteKinder());
		jaxNutzung.setAnzahlBetreuteKinderSonderschueler(nutzung.getAnzahlBetreuteKinderSonderschueler());
		jaxNutzung.setAnzahlBetreuteKinder1Zyklus(nutzung.getAnzahlBetreuteKinder1Zyklus());
		jaxNutzung.setAnzahlBetreuteKinder2Zyklus(nutzung.getAnzahlBetreuteKinder2Zyklus());
		jaxNutzung.setAnzahlBetreuteKinder3Zyklus(nutzung.getAnzahlBetreuteKinder3Zyklus());

		jaxNutzung.setStatus(nutzung.getStatus());

		return jaxNutzung;
	}

	@Nonnull
	public JaxFerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngabenKostenEinnahmenToJax(
		@Nonnull FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen
	) {
		flush();

		JaxFerienbetreuungAngabenKostenEinnahmen jaxKostenEinnahmen = new JaxFerienbetreuungAngabenKostenEinnahmen();

		convertAbstractFieldsToJAX(kostenEinnahmen, jaxKostenEinnahmen);

		jaxKostenEinnahmen.setPersonalkosten(kostenEinnahmen.getPersonalkosten());
		jaxKostenEinnahmen.setPersonalkostenLeitungAdmin(kostenEinnahmen.getPersonalkostenLeitungAdmin());
		jaxKostenEinnahmen.setSachkosten(kostenEinnahmen.getSachkosten());
		jaxKostenEinnahmen.setVerpflegungskosten(kostenEinnahmen.getVerpflegungskosten());
		jaxKostenEinnahmen.setWeitereKosten(kostenEinnahmen.getWeitereKosten());
		jaxKostenEinnahmen.setBemerkungenKosten(kostenEinnahmen.getBemerkungenKosten());
		jaxKostenEinnahmen.setElterngebuehren(kostenEinnahmen.getElterngebuehren());
		jaxKostenEinnahmen.setWeitereEinnahmen(kostenEinnahmen.getWeitereEinnahmen());
		jaxKostenEinnahmen.setSockelbeitrag(kostenEinnahmen.getSockelbeitrag());
		jaxKostenEinnahmen.setBeitraegeNachAnmeldungen(kostenEinnahmen.getBeitraegeNachAnmeldungen());
		jaxKostenEinnahmen.setVorfinanzierteKantonsbeitraege(kostenEinnahmen.getVorfinanzierteKantonsbeitraege());
		jaxKostenEinnahmen.setEigenleistungenGemeinde(kostenEinnahmen.getEigenleistungenGemeinde());

		jaxKostenEinnahmen.setStatus(kostenEinnahmen.getStatus());

		return jaxKostenEinnahmen;
	}

	@Nonnull
	public List<JaxFerienbetreuungDokument> ferienbetreuungDokumentListToJax(
		@Nonnull List<FerienbetreuungDokument> dokumente) {
		return dokumente.stream().map(d -> ferienbetreuungDokumentToJax(d)).collect(Collectors.toList());
	}

	@Nonnull
	public JaxFerienbetreuungDokument ferienbetreuungDokumentToJax(
		@Nonnull FerienbetreuungDokument ferienbetreuungDokument) {

		JaxFerienbetreuungDokument jaxFerienbetreuungDokument = convertAbstractVorgaengerFieldsToJAX(
			ferienbetreuungDokument,
			new JaxFerienbetreuungDokument()
		);
		convertFileToJax(ferienbetreuungDokument, jaxFerienbetreuungDokument);

		jaxFerienbetreuungDokument.setTimestampUpload(ferienbetreuungDokument.getTimestampUpload());

		return jaxFerienbetreuungDokument;
	}

	@Override
	@Nonnull
	@CanIgnoreReturnValue
	public Adresse adresseToEntity(@Nonnull final JaxAdresse jaxAdresse, @Nonnull final Adresse adresse) {
		if (adresse.getVersion() != jaxAdresse.getVersion()) {
			throw new WebApplicationException(new StaleObjectStateException("Die Ferienbetreuung Adresse Versionen stimmen nicht",
				adresse.getId()), Status.CONFLICT);
		}
		Adresse entity = super.adresseToEntity(jaxAdresse, adresse);
		return entity;
	}

}
