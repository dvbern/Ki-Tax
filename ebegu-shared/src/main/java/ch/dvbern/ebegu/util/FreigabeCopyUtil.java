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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.util;

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utils fuer das Kopieren der Daten von den JA-Containern in die GS-Container bei der Freigabe des Gesuchs.
 */
@SuppressWarnings("PMD.CollapsibleIfStatements")
public final class FreigabeCopyUtil {

	private FreigabeCopyUtil() {
	}

	/**
	 * kopiert das Gesuch fuer die Freigabe
	 */
	public static void copyForFreigabe(@Nonnull Gesuch gesuch) {
		// Familiensituation
		copyFamiliensituationContainer(gesuch.getFamiliensituationContainer());
		// Kinder
		if (gesuch.getKindContainers() != null) {
			for (KindContainer kindContainer : gesuch.getKindContainers()) {
				// Kind
				copyKindContainer(kindContainer);
			}
		}
		// EinkommensverschlechterungsInfo
		copyEinkommensverschlechterungInfoContainer(gesuch.getEinkommensverschlechterungInfoContainer());
		// Gesuchsteller 1
		copyGesuchstellerContainer(gesuch.getGesuchsteller1());
		// Gesuchsteller 2
		copyGesuchstellerContainer(gesuch.getGesuchsteller2());
	}

	private static void copyFamiliensituationContainer(@Nullable FamiliensituationContainer container) {
		if (container != null) {
			if (container.getFamiliensituationJA() != null) {
				if (container.getFamiliensituationGS() == null) {
					container.setFamiliensituationGS(new Familiensituation());  //init
				}
				//noinspection ConstantConditions
				copyFamiliensituation(container.getFamiliensituationGS(), container.getFamiliensituationJA());
			} else {
				container.setFamiliensituationGS(null);
			}

			// Sozialhilfe
			for (SozialhilfeZeitraumContainer sozialhilfeContainer : container.getSozialhilfeZeitraumContainers()) {
				copySozialhilfeZeitraumContainer(sozialhilfeContainer);
			}
		}
	}


	private static void copySozialhilfeZeitraumContainer(@Nullable SozialhilfeZeitraumContainer container) {
		if (container != null) {
			if (container.getSozialhilfeZeitraumJA() != null) {
				if (container.getSozialhilfeZeitraumGS() == null) {
					container.setSozialhilfeZeitraumGS(new SozialhilfeZeitraum());  //init
				}
				//noinspection ConstantConditions
				copySozialhilfeZeitraum(container.getSozialhilfeZeitraumGS(), container.getSozialhilfeZeitraumJA());
			} else {
				container.setSozialhilfeZeitraumGS(null);
			}
		}
	}

	private static void copySozialhilfeZeitraum(@Nonnull SozialhilfeZeitraum sozialhilfeGS, @Nonnull SozialhilfeZeitraum sozialhilfeJA) {
		sozialhilfeGS.setGueltigkeit(sozialhilfeJA.getGueltigkeit());
	}

	private static void copyFamiliensituation(
		@Nonnull Familiensituation familiensituationGS,
		@Nonnull Familiensituation familiensituationJA) {
		familiensituationGS.setFamilienstatus(familiensituationJA.getFamilienstatus());
		familiensituationGS.setGemeinsameSteuererklaerung(familiensituationJA.getGemeinsameSteuererklaerung());
		familiensituationGS.setStartKonkubinat(familiensituationJA.getStartKonkubinat());
		familiensituationGS.setAenderungPer(familiensituationJA.getAenderungPer());
		familiensituationGS.setSozialhilfeBezueger(familiensituationJA.getSozialhilfeBezueger());
		familiensituationGS.setZustaendigeAmtsstelle(familiensituationJA.getZustaendigeAmtsstelle());
		familiensituationGS.setNameBetreuer(familiensituationJA.getNameBetreuer());
		familiensituationGS.setVerguenstigungGewuenscht(familiensituationJA.getVerguenstigungGewuenscht());
		familiensituationGS.setKeineMahlzeitenverguenstigungBeantragt(familiensituationJA.isKeineMahlzeitenverguenstigungBeantragt());
		familiensituationGS.setKeineMahlzeitenverguenstigungBeantragtEditable(familiensituationJA.isKeineMahlzeitenverguenstigungBeantragtEditable());
		familiensituationGS.setAbweichendeZahlungsadresse(familiensituationJA.isAbweichendeZahlungsadresse());
		familiensituationGS.setFkjvFamSit(familiensituationJA.isFkjvFamSit());
		familiensituationGS.setMinDauerKonkubinat(familiensituationJA.getMinDauerKonkubinat());
		familiensituationGS.setGesuchstellerKardinalitaet(familiensituationJA.getGesuchstellerKardinalitaet());

		Auszahlungsdaten auszahlungsdatenJA = familiensituationJA.getAuszahlungsdaten();
		Auszahlungsdaten auszahlungsdatenGS = null;
		if (auszahlungsdatenJA != null) {
			auszahlungsdatenGS = new Auszahlungsdaten();
			copyAuszahlungsdaten(auszahlungsdatenGS, auszahlungsdatenJA);
		}
		familiensituationGS.setAuszahlungsdaten(auszahlungsdatenGS);

		familiensituationGS.setUnterhaltsvereinbarung(familiensituationJA.getUnterhaltsvereinbarung());
		familiensituationGS.setUnterhaltsvereinbarungBemerkung(familiensituationJA.getUnterhaltsvereinbarungBemerkung());
		familiensituationGS.setGeteilteObhut(familiensituationJA.getGeteilteObhut());
		familiensituationGS.setPartnerIdentischMitVorgesuch(familiensituationGS.getPartnerIdentischMitVorgesuch());
		familiensituationGS.setAuszahlungAusserhalbVonKibon(familiensituationJA.isAuszahlungAusserhalbVonKibon());
		familiensituationGS.setGemeinsamerHaushaltMitPartner(familiensituationJA.getGemeinsamerHaushaltMitPartner());
		familiensituationGS.setGemeinsamerHaushaltMitObhutsberechtigterPerson(familiensituationJA.getGemeinsamerHaushaltMitObhutsberechtigterPerson());
	}

	private static void copyAuszahlungsdaten(Auszahlungsdaten auszahlungsdatenGS, Auszahlungsdaten auszahlungsdatenJA) {
		auszahlungsdatenGS.setIban(auszahlungsdatenJA.getIban());
		auszahlungsdatenGS.setKontoinhaber(auszahlungsdatenJA.getKontoinhaber());
		auszahlungsdatenGS.setInfomaKreditorennummer(auszahlungsdatenJA.getInfomaKreditorennummer());
		auszahlungsdatenGS.setInfomaBankcode(auszahlungsdatenJA.getInfomaBankcode());
		Adresse zahlungsadresseJA = auszahlungsdatenJA.getAdresseKontoinhaber();
		Adresse zahlungsadresseGS = null;
		if (zahlungsadresseJA != null) {
			zahlungsadresseGS = new Adresse();
			copyAdresse(zahlungsadresseGS, zahlungsadresseJA);
		}
		auszahlungsdatenGS.setAdresseKontoinhaber(zahlungsadresseGS);
	}

	private static void copyKindContainer(@Nullable KindContainer container) {
		if (container == null) {
			return;
		}

		if (container.getKindJA() != null) {
			if (container.getKindGS() == null) {
				container.setKindGS(new Kind());
			}
			//noinspection ConstantConditions
			copyKind(container.getKindGS(), container.getKindJA());
		} else {
			container.setKindGS(null);
		}

		// Betreuungen pro Kind
		for (Betreuung betreuung : container.getBetreuungen()) {
			// Betreuung
			if (betreuung.getBetreuungspensumContainers() != null) {
				betreuung.getBetreuungspensumContainers().forEach(FreigabeCopyUtil::copyBetreuungspensumContainer);
			}
			// Abwesenheiten pro Betreuung
			if (betreuung.getAbwesenheitContainers() != null) {
				betreuung.getAbwesenheitContainers().forEach(FreigabeCopyUtil::copyAbwesenheitContainer);
			}
			// ErweiterteBetreuung
			copyErweiterteBetreuungContainer(betreuung.getErweiterteBetreuungContainer());
		}
	}

	private static void copyErweiterteBetreuungContainer(@Nonnull ErweiterteBetreuungContainer container) {
		if (container.getErweiterteBetreuungJA() != null) {
			if (container.getErweiterteBetreuungGS() == null) {
				container.setErweiterteBetreuungGS(new ErweiterteBetreuung());
			}
			//noinspection ConstantConditions
			copyErweiterteBetreuung(container.getErweiterteBetreuungGS(), container.getErweiterteBetreuungJA());
		} else {
			container.setErweiterteBetreuungGS(null);
		}
	}

	private static void copyErweiterteBetreuung(
		@Nonnull ErweiterteBetreuung erweiterteBetreuungGS,
		@Nonnull ErweiterteBetreuung erweiterteBetreuungJA
	) {
		erweiterteBetreuungGS.setErweiterteBeduerfnisse(erweiterteBetreuungJA.getErweiterteBeduerfnisse());
		erweiterteBetreuungGS.setFachstelle(erweiterteBetreuungJA.getFachstelle());
		erweiterteBetreuungGS.setKeineKesbPlatzierung(erweiterteBetreuungJA.getKeineKesbPlatzierung());
		erweiterteBetreuungGS.setAnspruchFachstelleWennPensumUnterschritten(
				erweiterteBetreuungJA.isAnspruchFachstelleWennPensumUnterschritten()
		);
		erweiterteBetreuungGS.setSprachfoerderungBestaetigt(erweiterteBetreuungJA.isSprachfoerderungBestaetigt());
	}

	private static void copyKind(@Nonnull Kind kindGS, @Nonnull Kind kindJA) {
		kindGS.setGeburtsdatum(kindJA.getGeburtsdatum());
		kindGS.setNachname(kindJA.getNachname());
		kindGS.setVorname(kindJA.getVorname());
		kindGS.setGeschlecht(kindJA.getGeschlecht());

		for (PensumFachstelle pensumFachstelle : kindJA.getPensumFachstelle()) {
			PensumFachstelle copiedPensumFachstelle = new PensumFachstelle();
			copiedPensumFachstelle.setIntegrationTyp(pensumFachstelle.getIntegrationTyp());
			copiedPensumFachstelle.setPensum(pensumFachstelle.getPensum());
			copiedPensumFachstelle.setFachstelle(pensumFachstelle.getFachstelle());
			copiedPensumFachstelle.setGueltigkeit(pensumFachstelle.getGueltigkeit());
			copiedPensumFachstelle.setKind(kindGS);
			kindGS.getPensumFachstelle().add(copiedPensumFachstelle);
		}
		kindGS.setKinderabzugErstesHalbjahr(kindJA.getKinderabzugErstesHalbjahr());
		kindGS.setKinderabzugZweitesHalbjahr(kindJA.getKinderabzugZweitesHalbjahr());
		kindGS.setPflegekind(kindJA.getPflegekind());
		kindGS.setPflegeEntschaedigungErhalten(kindJA.getPflegeEntschaedigungErhalten());
		kindGS.setObhutAlternierendAusueben(kindJA.getObhutAlternierendAusueben());
		kindGS.setGemeinsamesGesuch(kindJA.getGemeinsamesGesuch());
		kindGS.setInErstausbildung(kindJA.getInErstausbildung());
		kindGS.setLebtKindAlternierend(kindJA.getLebtKindAlternierend());
		kindGS.setAlimenteErhalten(kindJA.getAlimenteErhalten());
		kindGS.setAlimenteBezahlen(kindJA.getAlimenteBezahlen());
		kindGS.setFamilienErgaenzendeBetreuung(kindJA.getFamilienErgaenzendeBetreuung());
		kindGS.setUnterhaltspflichtig(kindJA.getUnterhaltspflichtig());
		kindGS.setSprichtAmtssprache(kindJA.getSprichtAmtssprache());
		kindGS.setEinschulungTyp(kindJA.getEinschulungTyp());
		kindGS.setHoehereBeitraegeWegenBeeintraechtigungBeantragen(kindJA.getHoehereBeitraegeWegenBeeintraechtigungBeantragen());
		kindGS.setHoehereBeitraegeUnterlagenDigital(kindJA.getHoehereBeitraegeUnterlagenDigital());
	}

	private static void copyBetreuungspensumContainer(@Nullable BetreuungspensumContainer container) {
		if (container != null) {
			if (container.getBetreuungspensumJA() != null) {
				if (container.getBetreuungspensumGS() == null) {
					container.setBetreuungspensumGS(new Betreuungspensum());
				}
				//noinspection ConstantConditions
				copyBetreuungspensum(container.getBetreuungspensumGS(), container.getBetreuungspensumJA());
			} else {
				container.setBetreuungspensumGS(null);
			}
		}
	}

	private static void copyBetreuungspensum(
		@Nonnull Betreuungspensum betreuungspensumGS,
		@Nonnull Betreuungspensum betreuungspensumJA) {
		betreuungspensumGS.setGueltigkeit(betreuungspensumJA.getGueltigkeit());
		betreuungspensumGS.setPensum(betreuungspensumJA.getPensum());
		betreuungspensumGS.setNichtEingetreten(betreuungspensumJA.getNichtEingetreten());
	}

	private static void copyAbwesenheitContainer(@Nullable AbwesenheitContainer container) {
		if (container != null) {
			if (container.getAbwesenheitJA() != null) {
				if (container.getAbwesenheitGS() == null) {
					container.setAbwesenheitGS(new Abwesenheit());
				}
				//noinspection ConstantConditions
				copyAbwesenheit(container.getAbwesenheitGS(), container.getAbwesenheitJA());
			} else {
				container.setAbwesenheitGS(null);
			}
		}
	}

	private static void copyAbwesenheit(@Nonnull Abwesenheit abwesenheitGS, @Nonnull Abwesenheit abwesenheitJA) {
		abwesenheitGS.setGueltigkeit(new DateRange(abwesenheitJA.getGueltigkeit()));
	}

	private static void copyGesuchstellerContainer(@Nullable GesuchstellerContainer container) {
		if (container != null) {
			// Stammdaten
			if (container.getGesuchstellerJA() != null) {
				if (container.getGesuchstellerGS() == null) {
					container.setGesuchstellerGS(new Gesuchsteller());
				}
				//noinspection ConstantConditions
				copyGesuchsteller(container.getGesuchstellerGS(), container.getGesuchstellerJA());
			} else {
				container.setGesuchstellerGS(null);
			}
			// Adressen
			for (GesuchstellerAdresseContainer betreuung : container.getAdressen()) {
				copyGesuchstellerAdresseContainer(betreuung);
			}
			// Finanzielle Situation
			copyFinanzielleSituationContainer(container.getFinanzielleSituationContainer());
			// Einkommensverschlechterung
			copyEinkommensverschlechterungContainer(container.getEinkommensverschlechterungContainer());
			// Erwerbspensum
			for (ErwerbspensumContainer erwerbspensumContainer : container.getErwerbspensenContainers()) {
				copyErwerbspensumContainer(erwerbspensumContainer);
			}
		}
	}

	private static void copyGesuchsteller(
		@Nonnull Gesuchsteller gesuchstellerGS,
		@Nonnull Gesuchsteller gesuchstellerJA) {
		gesuchstellerGS.setGeschlecht(gesuchstellerJA.getGeschlecht());
		gesuchstellerGS.setVorname(gesuchstellerJA.getVorname());
		gesuchstellerGS.setNachname(gesuchstellerJA.getNachname());
		gesuchstellerGS.setGeburtsdatum(gesuchstellerJA.getGeburtsdatum());
		gesuchstellerGS.setMail(gesuchstellerJA.getMail());
		gesuchstellerGS.setMobile(gesuchstellerJA.getMobile());
		gesuchstellerGS.setTelefon(gesuchstellerJA.getTelefon());
		gesuchstellerGS.setTelefonAusland(gesuchstellerJA.getTelefonAusland());
		gesuchstellerGS.setDiplomatenstatus(gesuchstellerJA.isDiplomatenstatus());
		gesuchstellerGS.setKorrespondenzSprache(gesuchstellerJA.getKorrespondenzSprache());
		gesuchstellerGS.setSozialversicherungsnummer(gesuchstellerJA.getSozialversicherungsnummer());
	}

	private static void copyGesuchstellerAdresseContainer(@Nullable GesuchstellerAdresseContainer container) {
		if (container != null) {
			if (container.getGesuchstellerAdresseJA() != null) {
				if (container.getGesuchstellerAdresseGS() == null) {
					container.setGesuchstellerAdresseGS(new GesuchstellerAdresse());
				}
				//noinspection ConstantConditions
				copyGesuchstellerAdresse(container.getGesuchstellerAdresseGS(), container.getGesuchstellerAdresseJA());
			} else {
				container.setGesuchstellerAdresseGS(null);
			}
		}
	}

	private static void copyGesuchstellerAdresse(@Nonnull GesuchstellerAdresse gs, @Nonnull GesuchstellerAdresse ja) {
		copyAdresse(gs, ja);
		gs.setAdresseTyp(ja.getAdresseTyp());
		gs.setNichtInGemeinde(ja.isNichtInGemeinde());
	}

	private static void copyAdresse (Adresse gs, Adresse ja) {
		gs.setGueltigkeit(new DateRange(ja.getGueltigkeit()));
		gs.setStrasse(ja.getStrasse());
		gs.setHausnummer(ja.getHausnummer());
		gs.setZusatzzeile(ja.getZusatzzeile());
		gs.setPlz(ja.getPlz());
		gs.setOrt(ja.getOrt());
		gs.setLand(ja.getLand());
		gs.setGemeinde(ja.getGemeinde());
		gs.setOrganisation(ja.getOrganisation());
	}

	@SuppressWarnings("PMD.UnusedPrivateMethod") // FalsePositive: Die Methode ist benutzt
	private static void copyAbstractFinanzielleSituation(
		@Nonnull AbstractFinanzielleSituation gs,
		@Nonnull AbstractFinanzielleSituation ja) {
		gs.setNettolohn(ja.getNettolohn());
		gs.setFamilienzulage(ja.getFamilienzulage());
		gs.setErsatzeinkommen(ja.getErsatzeinkommen());
		gs.setErhalteneAlimente(ja.getErhalteneAlimente());
		gs.setBruttovermoegen(ja.getBruttovermoegen());
		gs.setBruttoLohn(ja.getBruttoLohn());
		gs.setSchulden(ja.getSchulden());
		gs.setGeschaeftsgewinnBasisjahr(ja.getGeschaeftsgewinnBasisjahr());
		gs.setGeschaeftsgewinnBasisjahrMinus1(ja.getGeschaeftsgewinnBasisjahrMinus1());
		gs.setGeleisteteAlimente(ja.getGeleisteteAlimente());

		gs.setSteuerbaresEinkommen(ja.getSteuerbaresEinkommen());
		gs.setSteuerbaresVermoegen(ja.getSteuerbaresVermoegen());
		gs.setAbzuegeLiegenschaft(ja.getAbzuegeLiegenschaft());
		gs.setGeschaeftsverlust(ja.getGeschaeftsverlust());
		gs.setEinkaeufeVorsorge(ja.getEinkaeufeVorsorge());
		gs.setBruttoertraegeVermoegen(ja.getBruttoertraegeVermoegen());
		gs.setNettoertraegeErbengemeinschaft(ja.getNettoertraegeErbengemeinschaft());
		gs.setNettoVermoegen(ja.getNettoVermoegen());
		gs.setEinkommenInVereinfachtemVerfahrenAbgerechnet(ja.getEinkommenInVereinfachtemVerfahrenAbgerechnet());
		gs.setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(ja.getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet());
		gs.setGewinnungskosten(ja.getGewinnungskosten());
		gs.setAbzugSchuldzinsen(ja.getAbzugSchuldzinsen());
		gs.setErsatzeinkommenSelbststaendigkeitBasisjahr(ja.getErsatzeinkommenSelbststaendigkeitBasisjahr());
		gs.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(ja.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1());
		copyFinanzielleSituationSelbstdeklaration(gs, ja);
	}

	private static void copyFinanzielleSituationSelbstdeklaration(@Nonnull AbstractFinanzielleSituation gs, @Nonnull AbstractFinanzielleSituation ja) {
		if(ja.getSelbstdeklaration() == null) {
			gs.setSelbstdeklaration(null);
			return;
		}
		if (gs.getSelbstdeklaration() == null){
				gs.setSelbstdeklaration(new FinanzielleSituationSelbstdeklaration());
		}
		gs.getSelbstdeklaration().setEinkunftErwerb(ja.getSelbstdeklaration().getEinkunftErwerb());
		gs.getSelbstdeklaration().setEinkunftVersicherung(ja.getSelbstdeklaration().getEinkunftVersicherung());
		gs.getSelbstdeklaration().setEinkunftWertschriften(ja.getSelbstdeklaration().getEinkunftWertschriften());
		gs.getSelbstdeklaration().setEinkunftUnterhaltsbeitragKinder(ja.getSelbstdeklaration().getEinkunftUnterhaltsbeitragKinder());
		gs.getSelbstdeklaration().setEinkunftUeberige(ja.getSelbstdeklaration().getEinkunftUeberige());
		gs.getSelbstdeklaration().setEinkunftLiegenschaften(ja.getSelbstdeklaration().getEinkunftLiegenschaften());
		gs.getSelbstdeklaration().setAbzugBerufsauslagen(ja.getSelbstdeklaration().getAbzugBerufsauslagen());
		gs.getSelbstdeklaration().setAbzugSchuldzinsen(ja.getSelbstdeklaration().getAbzugSchuldzinsen());
		gs.getSelbstdeklaration().setAbzugUnterhaltsbeitragKinder(ja.getSelbstdeklaration().getAbzugUnterhaltsbeitragKinder());
		gs.getSelbstdeklaration().setAbzugSaeule3A(ja.getSelbstdeklaration().getAbzugSaeule3A());
		gs.getSelbstdeklaration().setAbzugVersicherungspraemien(ja.getSelbstdeklaration().getAbzugVersicherungspraemien());
		gs.getSelbstdeklaration().setAbzugKrankheitsUnfallKosten(ja.getSelbstdeklaration().getAbzugKrankheitsUnfallKosten());
		gs.getSelbstdeklaration().setSonderabzugErwerbstaetigkeitEhegatten(ja.getSelbstdeklaration().getSonderabzugErwerbstaetigkeitEhegatten());
		gs.getSelbstdeklaration().setAbzugKinderVorschule(ja.getSelbstdeklaration().getAbzugKinderVorschule());
		gs.getSelbstdeklaration().setAbzugKinderSchule(ja.getSelbstdeklaration().getAbzugKinderSchule());
		gs.getSelbstdeklaration().setAbzugEigenbetreuung(ja.getSelbstdeklaration().getAbzugEigenbetreuung());
		gs.getSelbstdeklaration().setAbzugFremdbetreuung(ja.getSelbstdeklaration().getAbzugFremdbetreuung());
		gs.getSelbstdeklaration().setAbzugErwerbsunfaehigePersonen(ja.getSelbstdeklaration().getAbzugErwerbsunfaehigePersonen());
		gs.getSelbstdeklaration().setVermoegen(ja.getSelbstdeklaration().getVermoegen());
		gs.getSelbstdeklaration().setAbzugSteuerfreierBetragErwachsene(ja.getSelbstdeklaration().getAbzugSteuerfreierBetragErwachsene());
		gs.getSelbstdeklaration().setAbzugSteuerfreierBetragKinder(ja.getSelbstdeklaration().getAbzugSteuerfreierBetragKinder());
	}

	private static void copyEinkommensverschlechterungInfoContainer(
		@Nullable EinkommensverschlechterungInfoContainer container) {
		if (container != null) {
			if (container.getEinkommensverschlechterungInfoGS() == null) {
				container.setEinkommensverschlechterungInfoGS(new EinkommensverschlechterungInfo());
			}
			//noinspection ConstantConditions
			copyEinkommensverschlechterungInfo(
				container.getEinkommensverschlechterungInfoGS(),
				container.getEinkommensverschlechterungInfoJA());
		}
	}

	private static void copyEinkommensverschlechterungInfo(
		@Nonnull EinkommensverschlechterungInfo gs,
		@Nonnull EinkommensverschlechterungInfo ja) {
		gs.setEinkommensverschlechterung(ja.getEinkommensverschlechterung());
		gs.setEkvFuerBasisJahrPlus1(ja.getEkvFuerBasisJahrPlus1());
		gs.setEkvFuerBasisJahrPlus2(ja.getEkvFuerBasisJahrPlus2());
	}

	private static void copyEinkommensverschlechterungContainer(
		@Nullable EinkommensverschlechterungContainer container) {
		if (container != null) {
			if (container.getEkvJABasisJahrPlus1() != null) {
				if (container.getEkvGSBasisJahrPlus1() == null) {
					container.setEkvGSBasisJahrPlus1(new Einkommensverschlechterung());
				}
				//noinspection ConstantConditions
				copyEinkommensverschlechterung(container.getEkvGSBasisJahrPlus1(), container.getEkvJABasisJahrPlus1());
			} else {
				container.setEkvGSBasisJahrPlus1(null);
			}
			if (container.getEkvJABasisJahrPlus2() != null) {
				if (container.getEkvGSBasisJahrPlus2() == null) {
					container.setEkvGSBasisJahrPlus2(new Einkommensverschlechterung());
				}
				//noinspection ConstantConditions
				copyEinkommensverschlechterung(container.getEkvGSBasisJahrPlus2(), container.getEkvJABasisJahrPlus2());
			} else {
				container.setEkvGSBasisJahrPlus2(null);
			}
		}
	}

	private static void copyEinkommensverschlechterung(
		@Nonnull Einkommensverschlechterung gs,
		@Nonnull Einkommensverschlechterung ja) {
		copyAbstractFinanzielleSituation(gs, ja);
		gs.setBruttolohnAbrechnung1(ja.getBruttolohnAbrechnung1());
		gs.setBruttolohnAbrechnung2(ja.getBruttolohnAbrechnung2());
		gs.setBruttolohnAbrechnung3(ja.getBruttolohnAbrechnung3());
		gs.setExtraLohn(ja.getExtraLohn());
		copyFinSitZusatzangabenAppenzell(gs, ja);
	}

	private static void copyFinanzielleSituationContainer(@Nullable FinanzielleSituationContainer container) {
		if (container == null) {
			return;
		}
		if (container.getFinanzielleSituationJA() == null) {
			//noinspection ConstantConditions
			container.setFinanzielleSituationGS(null);
		} else {
			if (container.getFinanzielleSituationGS() == null) {
				container.setFinanzielleSituationGS(new FinanzielleSituation());
			}
			copyFinanzielleSituation(container.getFinanzielleSituationGS(), container.getFinanzielleSituationJA());
		}
	}

	private static void copyFinanzielleSituation(@Nonnull FinanzielleSituation gs, @Nonnull FinanzielleSituation ja) {
		copyAbstractFinanzielleSituation(gs, ja);
		gs.setSteuerveranlagungErhalten(ja.getSteuerveranlagungErhalten());
		gs.setSteuererklaerungAusgefuellt(ja.getSteuererklaerungAusgefuellt());
		gs.setGeschaeftsgewinnBasisjahrMinus2(ja.getGeschaeftsgewinnBasisjahrMinus2());
		gs.setSteuerdatenZugriff(ja.getSteuerdatenZugriff());
		gs.setAutomatischePruefungErlaubt(ja.getAutomatischePruefungErlaubt());
		gs.setSteuerdatenAbfrageStatus(ja.getSteuerdatenAbfrageStatus());
		gs.setSteuerdatenAbfrageTimestamp(ja.getSteuerdatenAbfrageTimestamp());
		gs.setQuellenbesteuert(ja.getQuellenbesteuert());
		gs.setGemeinsameStekVorjahr(ja.getGemeinsameStekVorjahr());
		gs.setAlleinigeStekVorjahr(ja.getAlleinigeStekVorjahr());
		gs.setVeranlagt(ja.getVeranlagt());
		gs.setVeranlagtVorjahr(ja.getVeranlagtVorjahr());
		gs.setUnterhaltsBeitraege(ja.getUnterhaltsBeitraege());
		gs.setAbzuegeKinderAusbildung(ja.getAbzuegeKinderAusbildung());
		gs.setMomentanSelbststaendig(ja.getMomentanSelbststaendig());
		gs.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus2(ja.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2());
		copyFinSitZusatzangabenAppenzell(gs, ja);
	}

	// disable false-positive
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	private static void copyFinSitZusatzangabenAppenzell(AbstractFinanzielleSituation gs, AbstractFinanzielleSituation ja) {
		if (ja.getFinSitZusatzangabenAppenzell() != null) {
			gs.setFinSitZusatzangabenAppenzell(ja.getFinSitZusatzangabenAppenzell().copyAllValues(new FinSitZusatzangabenAppenzell()));
		}
	}

	private static void copyErwerbspensumContainer(@Nullable ErwerbspensumContainer container) {
		if (container == null) {
			return;
		}
		if (container.getErwerbspensumJA() == null) {
			container.setErwerbspensumGS(null);
		} else {
			if (container.getErwerbspensumGS() == null) {
				container.setErwerbspensumGS(new Erwerbspensum());
			}
			//noinspection ConstantConditions
			copyErwerbspensum(container.getErwerbspensumGS(), container.getErwerbspensumJA());
		}
	}

	private static void copyErwerbspensum(
		@Nonnull Erwerbspensum erwerbspensumGS,
		@Nonnull Erwerbspensum erwerbspensumJA) {
		erwerbspensumGS.setGueltigkeit(new DateRange(erwerbspensumJA.getGueltigkeit()));
		erwerbspensumGS.setPensum(erwerbspensumJA.getPensum());
		erwerbspensumGS.setTaetigkeit(erwerbspensumJA.getTaetigkeit());
		erwerbspensumGS.setBezeichnung(erwerbspensumJA.getBezeichnung());
		erwerbspensumGS.setErwerbspensumInstitution(erwerbspensumJA.getErwerbspensumInstitution());
		erwerbspensumGS.setWegzeit(erwerbspensumJA.getWegzeit());

		if (erwerbspensumJA.getUnbezahlterUrlaub() == null) {
			//noinspection ConstantConditions
			erwerbspensumGS.setUnbezahlterUrlaub(null);
		} else {
			if (erwerbspensumGS.getUnbezahlterUrlaub() == null) {
				erwerbspensumGS.setUnbezahlterUrlaub(new UnbezahlterUrlaub());
			}
			//noinspection ConstantConditions
			copyUnbezahlterUrlaub(erwerbspensumGS.getUnbezahlterUrlaub(), erwerbspensumJA.getUnbezahlterUrlaub());
		}
	}

	private static void copyUnbezahlterUrlaub(
		@Nonnull UnbezahlterUrlaub urlaubGS,
		@Nonnull UnbezahlterUrlaub urlaubJA) {
		urlaubGS.setGueltigkeit(new DateRange(urlaubJA.getGueltigkeit()));
	}
}
