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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngaben;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungDokument;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngaben;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungDokument;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

public class JaxFerienbetreuungConverter extends AbstractConverter {

	@Inject
	private GemeindeService gemeindeService;

	@Nonnull
	public FerienbetreuungAngabenContainer ferienbetreuungenAngabenContainerToEntity(
		@Nonnull JaxFerienbetreuungAngabenContainer jaxContainer,
		@Nonnull FerienbetreuungAngabenContainer container
	) {
		convertAbstractFieldsToEntity(jaxContainer, container);

		// never set status, gemeinde and gesuchsperiode from client

		container.setAngabenDeklaration(ferienbetreuungenAngabenToEntity(
			jaxContainer.getAngabenDeklaration(),
			container.getAngabenDeklaration()
		));

		if (container.getAngabenKorrektur() != null && jaxContainer.getAngabenKorrektur() != null) {
			container.setAngabenKorrektur(ferienbetreuungenAngabenToEntity(
				jaxContainer.getAngabenKorrektur(),
				container.getAngabenDeklaration()
			));
		}

		container.setInternerKommentar(jaxContainer.getInternerKommentar());
		return container;
	}


	@Nonnull
	private FerienbetreuungAngaben ferienbetreuungenAngabenToEntity(
		@Nonnull JaxFerienbetreuungAngaben jaxContainer,
		@Nonnull FerienbetreuungAngaben ferienbetreuungAngaben
	) {
		convertAbstractFieldsToEntity(jaxContainer, ferienbetreuungAngaben);

		// stammdaten
		ferienbetreuungAngabenStammdatenToEntity(jaxContainer.getStammdaten(), ferienbetreuungAngaben);
		// angebot
		ferienbetreuungAngabenAngebotToEntity(jaxContainer.getAngebot(), ferienbetreuungAngaben);
		// nutzung
		ferienbetreuungAngabenNutzungToEntity(jaxContainer.getNutzung(), ferienbetreuungAngaben);
		// kosten und einnahmen
		ferienbetreuungAngabenKostenEinnahmenToEntity(jaxContainer.getKostenEinnahmen(), ferienbetreuungAngaben);

		// never save resultate from client

		return ferienbetreuungAngaben;

	}

	private void ferienbetreuungAngabenStammdatenToEntity(
		@Nonnull JaxFerienbetreuungAngabenStammdaten stammdaten,
		@Nonnull FerienbetreuungAngaben ferienbetreuungAngaben
	) {
		if (stammdaten.getAmAngebotBeteiligteGemeinden() != null) {
			Set<Gemeinde> gemeinden = gemeindeListToEntity(stammdaten.getAmAngebotBeteiligteGemeinden());
			ferienbetreuungAngaben.setAmAngebotBeteiligteGemeinden(gemeinden);
		} else {
			ferienbetreuungAngaben.setAmAngebotBeteiligteGemeinden(Collections.emptySet());
		}
		ferienbetreuungAngaben.setTraegerschaft(stammdaten.getTraegerschaft());
		if (stammdaten.getStammdatenAdresse() != null) {
			if (ferienbetreuungAngaben.getStammdatenAdresse() == null) {
				ferienbetreuungAngaben.setStammdatenAdresse(new Adresse());
			}
			ferienbetreuungAngaben.setStammdatenAdresse(
				adresseToEntity(stammdaten.getStammdatenAdresse(), ferienbetreuungAngaben.getStammdatenAdresse())
			);
		}
		ferienbetreuungAngaben.setStammdatenKontaktpersonVorname(stammdaten.getStammdatenKontaktpersonVorname());
		ferienbetreuungAngaben.setStammdatenKontaktpersonNachname(stammdaten.getStammdatenKontaktpersonNachname());
		ferienbetreuungAngaben.setStammdatenKontaktpersonFunktion(stammdaten.getStammdatenKontaktpersonFunktion());
		ferienbetreuungAngaben.setStammdatenKontaktpersonTelefon(stammdaten.getStammdatenKontaktpersonTelefon());
		ferienbetreuungAngaben.setStammdatenKontaktpersonEmail(stammdaten.getStammdatenKontaktpersonEmail());
		if (stammdaten.getIban() != null && stammdaten.getKontoinhaber() != null) {
			Auszahlungsdaten auszahlungsdaten = ferienbetreuungAngaben.getAuszahlungsdaten();
			if (auszahlungsdaten == null) {
				auszahlungsdaten = new Auszahlungsdaten();
			}
			auszahlungsdaten.setIban(new IBAN(stammdaten.getIban()));
			auszahlungsdaten.setKontoinhaber(stammdaten.getKontoinhaber());
			if (stammdaten.getAdresseKontoinhaber() != null) {
				Adresse adresse = auszahlungsdaten.getAdresseKontoinhaber();
				if (adresse == null) {
					adresse = new Adresse();
				}
				auszahlungsdaten.setAdresseKontoinhaber(adresseToEntity(stammdaten.getAdresseKontoinhaber(), adresse));
			}
		}
	}

	private void ferienbetreuungAngabenAngebotToEntity(
		@Nonnull JaxFerienbetreuungAngabenAngebot angebot,
		@Nonnull FerienbetreuungAngaben ferienbetreuungAngaben
	) {

		ferienbetreuungAngaben.setAngebot(angebot.getAngebot());
		ferienbetreuungAngaben.setAngebotKontaktpersonVorname(angebot.getAngebotKontaktpersonVorname());
		ferienbetreuungAngaben.setAngebotKontaktpersonNachname(angebot.getAngebotKontaktpersonNachname());
		if (angebot.getAngebotAdresse() != null) {
			if (ferienbetreuungAngaben.getAngebotAdresse() == null) {
				ferienbetreuungAngaben.setAngebotAdresse(new Adresse());
			}
			ferienbetreuungAngaben.setAngebotAdresse(
				adresseToEntity(angebot.getAngebotAdresse(), ferienbetreuungAngaben.getAngebotAdresse())
			);
		}
		ferienbetreuungAngaben.setAnzahlFerienwochenHerbstferien(angebot.getAnzahlFerienwochenHerbstferien());
		ferienbetreuungAngaben.setAnzahlFerienwochenWinterferien(angebot.getAnzahlFerienwochenWinterferien());
		ferienbetreuungAngaben.setAnzahlFerienwochenFruehlingsferien(angebot.getAnzahlFerienwochenFruehlingsferien());
		ferienbetreuungAngaben.setAnzahlFerienwochenSommerferien(angebot.getAnzahlFerienwochenSommerferien());
		ferienbetreuungAngaben.setAnzahlTage(angebot.getAnzahlTage());
		ferienbetreuungAngaben.setAnzahlStundenProBetreuungstag(angebot.getAnzahlStundenProBetreuungstag());
		ferienbetreuungAngaben.setBemerkungenOeffnungszeiten(angebot.getBemerkungenOeffnungszeiten());

		if (angebot.getFinanziellBeteiligteGemeinden() != null) {
			Set<Gemeinde> gemeinden = gemeindeListToEntity(angebot.getFinanziellBeteiligteGemeinden());
			ferienbetreuungAngaben.setFinanziellBeteiligteGemeinden(gemeinden);
		} else {
			ferienbetreuungAngaben.setFinanziellBeteiligteGemeinden(Collections.emptySet());
		}

		ferienbetreuungAngaben.setGemeindeFuehrtAngebotSelber(angebot.getGemeindeFuehrtAngebotSelber());
		ferienbetreuungAngaben.setGemeindeBeauftragtExterneAnbieter(angebot.getGemeindeBeauftragtExterneAnbieter());
		ferienbetreuungAngaben.setAngebotVereineUndPrivateIntegriert(angebot.getAngebotVereineUndPrivateIntegriert());
		ferienbetreuungAngaben.setBemerkungenKooperation(angebot.getBemerkungenKooperation());
		ferienbetreuungAngaben.setLeitungDurchPersonMitAusbildung(angebot.getLeitungDurchPersonMitAusbildung());
		ferienbetreuungAngaben.setAufwandBetreuungspersonal(angebot.getAufwandBetreuungspersonal());
		ferienbetreuungAngaben.setZusaetzlicherAufwandLeitungAdmin(angebot.getZusaetzlicherAufwandLeitungAdmin());
		ferienbetreuungAngaben.setBemerkungenPersonal(angebot.getBemerkungenPersonal());
		ferienbetreuungAngaben.setFixerTarifKinderDerGemeinde(angebot.getFixerTarifKinderDerGemeinde());
		ferienbetreuungAngaben.setEinkommensabhaengigerTarifKinderDerGemeinde(angebot.getEinkommensabhaengigerTarifKinderDerGemeinde());
		ferienbetreuungAngaben.setTagesschuleTarifGiltFuerFerienbetreuung(angebot.getTagesschuleTarifGiltFuerFerienbetreuung());
		ferienbetreuungAngaben.setFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(angebot.getFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet());
		ferienbetreuungAngaben.setKinderAusAnderenGemeindenZahlenAnderenTarif(angebot.getKinderAusAnderenGemeindenZahlenAnderenTarif());
		ferienbetreuungAngaben.setBemerkungenTarifsystem(angebot.getBemerkungenTarifsystem());
	}

	private void ferienbetreuungAngabenNutzungToEntity(
		@Nonnull JaxFerienbetreuungAngabenNutzung nutzung,
		@Nonnull FerienbetreuungAngaben ferienbetreuungAngaben
	) {
		ferienbetreuungAngaben.setAnzahlBetreuungstageKinderBern(nutzung.getAnzahlBetreuungstageKinderBern());
		ferienbetreuungAngaben.setBetreuungstageKinderDieserGemeinde(nutzung.getBetreuungstageKinderDieserGemeinde());
		ferienbetreuungAngaben.setBetreuungstageKinderDieserGemeindeSonderschueler(nutzung.getBetreuungstageKinderDieserGemeindeSonderschueler());
		ferienbetreuungAngaben.setDavonBetreuungstageKinderAndererGemeinden(nutzung.getDavonBetreuungstageKinderAndererGemeinden());
		ferienbetreuungAngaben.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(nutzung.getDavonBetreuungstageKinderAndererGemeindenSonderschueler());
		ferienbetreuungAngaben.setAnzahlBetreuteKinder(nutzung.getAnzahlBetreuteKinder());
		ferienbetreuungAngaben.setAnzahlBetreuteKinderSonderschueler(nutzung.getAnzahlBetreuteKinderSonderschueler());
		ferienbetreuungAngaben.setAnzahlBetreuteKinder1Zyklus(nutzung.getAnzahlBetreuteKinder1Zyklus());
		ferienbetreuungAngaben.setAnzahlBetreuteKinder2Zyklus(nutzung.getAnzahlBetreuteKinder2Zyklus());
		ferienbetreuungAngaben.setAnzahlBetreuteKinder3Zyklus(nutzung.getAnzahlBetreuteKinder3Zyklus());
	}

	private void ferienbetreuungAngabenKostenEinnahmenToEntity(
		@Nonnull JaxFerienbetreuungAngabenKostenEinnahmen kostenEinnahmen,
		@Nonnull FerienbetreuungAngaben ferienbetreuungAngaben
	) {
		ferienbetreuungAngaben.setPersonalkosten(kostenEinnahmen.getPersonalkosten());
		ferienbetreuungAngaben.setPersonalkostenLeitungAdmin(kostenEinnahmen.getPersonalkostenLeitungAdmin());
		ferienbetreuungAngaben.setSachkosten(kostenEinnahmen.getSachkosten());
		ferienbetreuungAngaben.setVerpflegungskosten(kostenEinnahmen.getVerpflegungskosten());
		ferienbetreuungAngaben.setWeitereKosten(kostenEinnahmen.getWeitereKosten());
		ferienbetreuungAngaben.setBemerkungenKosten(kostenEinnahmen.getBemerkungenKosten());
		ferienbetreuungAngaben.setElterngebuehren(kostenEinnahmen.getElterngebuehren());
		ferienbetreuungAngaben.setWeitereEinnahmen(kostenEinnahmen.getWeitereEinnahmen());
	}

	@Nonnull
	public JaxFerienbetreuungAngabenContainer ferienbetreuungAngabenContainerToJax(
		@Nonnull final FerienbetreuungAngabenContainer container
	) {
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

		return jaxContainer;
	}

	@Nonnull
	private JaxFerienbetreuungAngaben ferienbetreuungAngabenToJax(
		@Nonnull final FerienbetreuungAngaben ferienbetreuungAngaben
	) {
		JaxFerienbetreuungAngaben jaxFerienbetreuungAngaben = new JaxFerienbetreuungAngaben();
		convertAbstractFieldsToJAX(ferienbetreuungAngaben, jaxFerienbetreuungAngaben);

		// stammdaten
		ferienbetreuungAngabenStammdatenToJax(ferienbetreuungAngaben, jaxFerienbetreuungAngaben);

		// angebot
		ferienbetreuungAngabenAngebotToJax(ferienbetreuungAngaben, jaxFerienbetreuungAngaben);

		// nutzung
		ferienbetreuungAngabenNutzungToJax(ferienbetreuungAngaben, jaxFerienbetreuungAngaben);

		// kosten und einnahmen
		ferienbetreuungAngabenKostenEinnahmenToJax(ferienbetreuungAngaben, jaxFerienbetreuungAngaben);

		// resultate
		jaxFerienbetreuungAngaben.setGemeindebeitrag(ferienbetreuungAngaben.getGemeindebeitrag());
		jaxFerienbetreuungAngaben.setKantonsbeitrag(ferienbetreuungAngaben.getKantonsbeitrag());

		return jaxFerienbetreuungAngaben;

	}

	@Nonnull
	private void ferienbetreuungAngabenStammdatenToJax(
		@Nonnull FerienbetreuungAngaben ferienbetreuungAngaben,
		@Nonnull JaxFerienbetreuungAngaben jaxFerienbetreuungAngaben
	) {
		JaxFerienbetreuungAngabenStammdaten jaxStammdaten = new JaxFerienbetreuungAngabenStammdaten();

		List<JaxGemeinde> jaxGemeinden = gemeindeListToJax(ferienbetreuungAngaben.getAmAngebotBeteiligteGemeinden());

		jaxStammdaten.setAmAngebotBeteiligteGemeinden(jaxGemeinden);
		jaxStammdaten.setTraegerschaft(ferienbetreuungAngaben.getTraegerschaft());
		if (ferienbetreuungAngaben.getStammdatenAdresse() != null) {
			jaxStammdaten.setStammdatenAdresse(adresseToJAX(ferienbetreuungAngaben.getStammdatenAdresse()));
		}
		jaxStammdaten.setStammdatenKontaktpersonVorname(ferienbetreuungAngaben.getStammdatenKontaktpersonVorname());
		jaxStammdaten.setStammdatenKontaktpersonNachname(ferienbetreuungAngaben.getStammdatenKontaktpersonNachname());
		jaxStammdaten.setStammdatenKontaktpersonFunktion(ferienbetreuungAngaben.getStammdatenKontaktpersonFunktion());
		jaxStammdaten.setStammdatenKontaktpersonTelefon(ferienbetreuungAngaben.getStammdatenKontaktpersonTelefon());
		jaxStammdaten.setStammdatenKontaktpersonEmail(ferienbetreuungAngaben.getStammdatenKontaktpersonEmail());
		if (ferienbetreuungAngaben.getAuszahlungsdaten() != null) {
			jaxStammdaten.setIban(ferienbetreuungAngaben.getAuszahlungsdaten().getIban().getIban());
			jaxStammdaten.setKontoinhaber(ferienbetreuungAngaben.getAuszahlungsdaten().getKontoinhaber());
			if (ferienbetreuungAngaben.getAuszahlungsdaten().getAdresseKontoinhaber() != null) {
				jaxStammdaten.setAdresseKontoinhaber(
					adresseToJAX(ferienbetreuungAngaben.getAuszahlungsdaten().getAdresseKontoinhaber())
				);
			}
		}

		jaxFerienbetreuungAngaben.setStammdaten(jaxStammdaten);
	}

	@Nonnull
	private void ferienbetreuungAngabenAngebotToJax(
		@Nonnull FerienbetreuungAngaben ferienbetreuungAngaben,
		@Nonnull JaxFerienbetreuungAngaben jaxFerienbetreuungAngaben
	) {
		JaxFerienbetreuungAngabenAngebot jaxAngebot = new JaxFerienbetreuungAngabenAngebot();
		jaxAngebot.setAngebot(ferienbetreuungAngaben.getAngebot());
		jaxAngebot.setAngebotKontaktpersonVorname(ferienbetreuungAngaben.getAngebotKontaktpersonVorname());
		jaxAngebot.setAngebotKontaktpersonNachname(ferienbetreuungAngaben.getAngebotKontaktpersonNachname());
		if (ferienbetreuungAngaben.getAngebotAdresse() != null) {
			jaxAngebot.setAngebotAdresse(
				adresseToJAX(ferienbetreuungAngaben.getAngebotAdresse())
			);
		}
		jaxAngebot.setAnzahlFerienwochenHerbstferien(ferienbetreuungAngaben.getAnzahlFerienwochenHerbstferien());
		jaxAngebot.setAnzahlFerienwochenWinterferien(ferienbetreuungAngaben.getAnzahlFerienwochenWinterferien());
		jaxAngebot.setAnzahlFerienwochenFruehlingsferien(ferienbetreuungAngaben.getAnzahlFerienwochenFruehlingsferien());
		jaxAngebot.setAnzahlFerienwochenSommerferien(ferienbetreuungAngaben.getAnzahlFerienwochenSommerferien());
		jaxAngebot.setAnzahlTage(ferienbetreuungAngaben.getAnzahlTage());
		jaxAngebot.setAnzahlStundenProBetreuungstag(ferienbetreuungAngaben.getAnzahlStundenProBetreuungstag());
		jaxAngebot.setBemerkungenOeffnungszeiten(ferienbetreuungAngaben.getBemerkungenOeffnungszeiten());

		List<JaxGemeinde> jaxGemeinden = gemeindeListToJax(ferienbetreuungAngaben.getFinanziellBeteiligteGemeinden());
		jaxAngebot.setFinanziellBeteiligteGemeinden(jaxGemeinden);

		jaxAngebot.setGemeindeFuehrtAngebotSelber(ferienbetreuungAngaben.getGemeindeFuehrtAngebotSelber());
		jaxAngebot.setGemeindeBeauftragtExterneAnbieter(ferienbetreuungAngaben.getGemeindeBeauftragtExterneAnbieter());
		jaxAngebot.setAngebotVereineUndPrivateIntegriert(ferienbetreuungAngaben.getAngebotVereineUndPrivateIntegriert());
		jaxAngebot.setBemerkungenKooperation(ferienbetreuungAngaben.getBemerkungenKooperation());
		jaxAngebot.setLeitungDurchPersonMitAusbildung(ferienbetreuungAngaben.getLeitungDurchPersonMitAusbildung());
		jaxAngebot.setAufwandBetreuungspersonal(ferienbetreuungAngaben.getAufwandBetreuungspersonal());
		jaxAngebot.setZusaetzlicherAufwandLeitungAdmin(ferienbetreuungAngaben.getZusaetzlicherAufwandLeitungAdmin());
		jaxAngebot.setBemerkungenPersonal(ferienbetreuungAngaben.getBemerkungenPersonal());
		jaxAngebot.setFixerTarifKinderDerGemeinde(ferienbetreuungAngaben.getFixerTarifKinderDerGemeinde());
		jaxAngebot.setEinkommensabhaengigerTarifKinderDerGemeinde(ferienbetreuungAngaben.getEinkommensabhaengigerTarifKinderDerGemeinde());
		jaxAngebot.setTagesschuleTarifGiltFuerFerienbetreuung(ferienbetreuungAngaben.getTagesschuleTarifGiltFuerFerienbetreuung());
		jaxAngebot.setFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(ferienbetreuungAngaben.getFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet());
		jaxAngebot.setKinderAusAnderenGemeindenZahlenAnderenTarif(ferienbetreuungAngaben.getKinderAusAnderenGemeindenZahlenAnderenTarif());
		jaxAngebot.setBemerkungenTarifsystem(ferienbetreuungAngaben.getBemerkungenTarifsystem());

		jaxFerienbetreuungAngaben.setAngebot(jaxAngebot);
	}

	@Nonnull
	private void ferienbetreuungAngabenNutzungToJax(
		@Nonnull FerienbetreuungAngaben ferienbetreuungAngaben,
		@Nonnull JaxFerienbetreuungAngaben jaxFerienbetreuungAngaben
	) {
		JaxFerienbetreuungAngabenNutzung jaxNutzung = new JaxFerienbetreuungAngabenNutzung();
		jaxNutzung.setAnzahlBetreuungstageKinderBern(ferienbetreuungAngaben.getAnzahlBetreuungstageKinderBern());
		jaxNutzung.setBetreuungstageKinderDieserGemeinde(ferienbetreuungAngaben.getBetreuungstageKinderDieserGemeinde());
		jaxNutzung.setBetreuungstageKinderDieserGemeindeSonderschueler(ferienbetreuungAngaben.getBetreuungstageKinderDieserGemeindeSonderschueler());
		jaxNutzung.setDavonBetreuungstageKinderAndererGemeinden(ferienbetreuungAngaben.getDavonBetreuungstageKinderAndererGemeinden());
		jaxNutzung.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(ferienbetreuungAngaben.getDavonBetreuungstageKinderAndererGemeindenSonderschueler());
		jaxNutzung.setAnzahlBetreuteKinder(ferienbetreuungAngaben.getAnzahlBetreuteKinder());
		jaxNutzung.setAnzahlBetreuteKinderSonderschueler(ferienbetreuungAngaben.getAnzahlBetreuteKinderSonderschueler());
		jaxNutzung.setAnzahlBetreuteKinder1Zyklus(ferienbetreuungAngaben.getAnzahlBetreuteKinder1Zyklus());
		jaxNutzung.setAnzahlBetreuteKinder2Zyklus(ferienbetreuungAngaben.getAnzahlBetreuteKinder2Zyklus());
		jaxNutzung.setAnzahlBetreuteKinder3Zyklus(ferienbetreuungAngaben.getAnzahlBetreuteKinder3Zyklus());

		jaxFerienbetreuungAngaben.setNutzung(jaxNutzung);
	}

	@Nonnull
	private void ferienbetreuungAngabenKostenEinnahmenToJax(
		@Nonnull FerienbetreuungAngaben ferienbetreuungAngaben,
		@Nonnull JaxFerienbetreuungAngaben jaxFerienbetreuungAngaben
	) {
		JaxFerienbetreuungAngabenKostenEinnahmen jaxKostenEinnahmen = new JaxFerienbetreuungAngabenKostenEinnahmen();
		jaxKostenEinnahmen.setPersonalkosten(ferienbetreuungAngaben.getPersonalkosten());
		jaxKostenEinnahmen.setPersonalkostenLeitungAdmin(ferienbetreuungAngaben.getPersonalkostenLeitungAdmin());
		jaxKostenEinnahmen.setSachkosten(ferienbetreuungAngaben.getSachkosten());
		jaxKostenEinnahmen.setVerpflegungskosten(ferienbetreuungAngaben.getVerpflegungskosten());
		jaxKostenEinnahmen.setWeitereKosten(ferienbetreuungAngaben.getWeitereKosten());
		jaxKostenEinnahmen.setBemerkungenKosten(ferienbetreuungAngaben.getBemerkungenKosten());
		jaxKostenEinnahmen.setElterngebuehren(ferienbetreuungAngaben.getElterngebuehren());
		jaxKostenEinnahmen.setWeitereEinnahmen(ferienbetreuungAngaben.getWeitereEinnahmen());

		jaxFerienbetreuungAngaben.setKostenEinnahmen(jaxKostenEinnahmen);
	}

	@Nonnull
	public JaxFerienbetreuungDokument ferienbetreuungDokumentToJax(@Nonnull FerienbetreuungDokument ferienbetreuungDokument) {

		JaxFerienbetreuungDokument jaxFerienbetreuungDokument = convertAbstractVorgaengerFieldsToJAX(
			ferienbetreuungDokument,
			new JaxFerienbetreuungDokument()
		);
		convertFileToJax(ferienbetreuungDokument, jaxFerienbetreuungDokument);

		jaxFerienbetreuungDokument.setTimestampUpload(jaxFerienbetreuungDokument.getTimestampUpload());

		return jaxFerienbetreuungDokument;
	}

	@Nonnull
	private Set<Gemeinde> gemeindeListToEntity(@Nonnull List<JaxGemeinde> gemeindeList) {
		return gemeindeList
			.stream()
			.map(g -> {
				Objects.requireNonNull(g.getId());
				return gemeindeService.findGemeinde(g.getId()).orElseThrow(() -> new EbeguRuntimeException(
					"findGemeinde",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					g.getId()));
			})
			.collect(Collectors.toSet());
	}

	@Nonnull
	private List<JaxGemeinde> gemeindeListToJax(@Nonnull Set<Gemeinde> gemeindeSet) {
		return gemeindeSet
			.stream()
			.map(this::gemeindeToJAX)
			.collect(Collectors.toList());
	}

}
