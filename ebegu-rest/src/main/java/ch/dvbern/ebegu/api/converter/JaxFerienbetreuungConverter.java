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

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;

import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngaben;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungDokument;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngaben;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungDokument;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

@RequestScoped
public class JaxFerienbetreuungConverter extends AbstractConverter {

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
	public FerienbetreuungAngaben ferienbetreuungenAngabenToEntity(
		@Nonnull JaxFerienbetreuungAngaben jaxContainer,
		@Nonnull FerienbetreuungAngaben ferienbetreuungAngaben
	) {
		convertAbstractFieldsToEntity(jaxContainer, ferienbetreuungAngaben);

		// stammdaten
		ferienbetreuungAngabenStammdatenToEntity(
			jaxContainer.getStammdaten(),
			ferienbetreuungAngaben.getFerienbetreuungAngabenStammdaten()
		);
		// angebot
		ferienbetreuungAngabenAngebotToEntity(
			jaxContainer.getAngebot(),
			ferienbetreuungAngaben.getFerienbetreuungAngabenAngebot()
		);
		// nutzung
		ferienbetreuungAngabenNutzungToEntity(
			jaxContainer.getNutzung(),
			ferienbetreuungAngaben.getFerienbetreuungAngabenNutzung()
		);
		// kosten und einnahmen
		ferienbetreuungAngabenKostenEinnahmenToEntity(
			jaxContainer.getKostenEinnahmen(),
			ferienbetreuungAngaben.getFerienbetreuungAngabenKostenEinnahmen()
		);

		// never save resultate from client

		return ferienbetreuungAngaben;

	}

	public void ferienbetreuungAngabenStammdatenToEntity(
		@Nonnull JaxFerienbetreuungAngabenStammdaten jaxStammdaten,
		@Nonnull FerienbetreuungAngabenStammdaten stammdaten
	) {
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
				auszahlungsdaten.setAdresseKontoinhaber(adresseToEntity(jaxStammdaten.getAdresseKontoinhaber(), adresse));
			}
			stammdaten.setAuszahlungsdaten(auszahlungsdaten);
		} else {
			stammdaten.setAuszahlungsdaten(null);
		}
		stammdaten.setVermerkAuszahlung(jaxStammdaten.getVermerkAuszahlung());
	}

	public void ferienbetreuungAngabenAngebotToEntity(
		@Nonnull JaxFerienbetreuungAngabenAngebot jaxAngebot,
		@Nonnull FerienbetreuungAngabenAngebot angebot
	) {
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
	}

	public void ferienbetreuungAngabenNutzungToEntity(
		@Nonnull JaxFerienbetreuungAngabenNutzung jaxNutzung,
		@Nonnull FerienbetreuungAngabenNutzung nutzung
	) {
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
	}

	public void ferienbetreuungAngabenKostenEinnahmenToEntity(
		@Nonnull JaxFerienbetreuungAngabenKostenEinnahmen jaxKostenEinnahmen,
		@Nonnull FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen
	) {
		convertAbstractFieldsToEntity(jaxKostenEinnahmen, kostenEinnahmen);

		kostenEinnahmen.setPersonalkosten(jaxKostenEinnahmen.getPersonalkosten());
		kostenEinnahmen.setPersonalkostenLeitungAdmin(jaxKostenEinnahmen.getPersonalkostenLeitungAdmin());
		kostenEinnahmen.setSachkosten(jaxKostenEinnahmen.getSachkosten());
		kostenEinnahmen.setVerpflegungskosten(jaxKostenEinnahmen.getVerpflegungskosten());
		kostenEinnahmen.setWeitereKosten(jaxKostenEinnahmen.getWeitereKosten());
		kostenEinnahmen.setBemerkungenKosten(jaxKostenEinnahmen.getBemerkungenKosten());
		kostenEinnahmen.setElterngebuehren(jaxKostenEinnahmen.getElterngebuehren());
		kostenEinnahmen.setWeitereEinnahmen(jaxKostenEinnahmen.getWeitereEinnahmen());
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
	public JaxFerienbetreuungAngaben ferienbetreuungAngabenToJax(
		@Nonnull final FerienbetreuungAngaben ferienbetreuungAngaben
	) {
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

		// resultate
		jaxFerienbetreuungAngaben.setGemeindebeitrag(ferienbetreuungAngaben.getGemeindebeitrag());
		jaxFerienbetreuungAngaben.setKantonsbeitrag(ferienbetreuungAngaben.getKantonsbeitrag());

		return jaxFerienbetreuungAngaben;

	}

	@Nonnull
	public JaxFerienbetreuungAngabenStammdaten ferienbetreuungAngabenStammdatenToJax(
		@Nonnull FerienbetreuungAngabenStammdaten stammdaten
	) {
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

		return jaxStammdaten;
	}

	@Nonnull
	public JaxFerienbetreuungAngabenAngebot ferienbetreuungAngabenAngebotToJax(
		@Nonnull FerienbetreuungAngabenAngebot angebot
	) {
		JaxFerienbetreuungAngabenAngebot jaxAngebot = new JaxFerienbetreuungAngabenAngebot();

		convertAbstractFieldsToJAX(angebot, jaxAngebot);

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
		jaxAngebot.setAnzahlFerienwochenFruehlingsferien(angebot.getAnzahlFerienwochenFruehlingsferien());
		jaxAngebot.setAnzahlFerienwochenSommerferien(angebot.getAnzahlFerienwochenSommerferien());
		jaxAngebot.setAnzahlTage(angebot.getAnzahlTage());
		jaxAngebot.setBemerkungenAnzahlFerienwochen(angebot.getBemerkungenAnzahlFerienwochen());
		jaxAngebot.setAnzahlStundenProBetreuungstag(angebot.getAnzahlStundenProBetreuungstag());
		jaxAngebot.setBetreuungErfolgtTagsueber(angebot.getBetreuungErfolgtTagsueber());
		jaxAngebot.setBemerkungenOeffnungszeiten(angebot.getBemerkungenOeffnungszeiten());
		jaxAngebot.setFinanziellBeteiligteGemeinden(angebot.getFinanziellBeteiligteGemeinden());
		jaxAngebot.setGemeindeFuehrtAngebotSelber(angebot.getGemeindeFuehrtAngebotSelber());
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

		return jaxNutzung;
	}

	@Nonnull
	public JaxFerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngabenKostenEinnahmenToJax(
		@Nonnull FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen
	) {
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

		return jaxKostenEinnahmen;
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

}
