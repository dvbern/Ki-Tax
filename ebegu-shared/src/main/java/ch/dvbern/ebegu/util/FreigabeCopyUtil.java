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

package ch.dvbern.ebegu.util;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Abwesenheit;
import ch.dvbern.ebegu.entities.AbwesenheitContainer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.types.DateRange;

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
		}
	}

	private static void copyFamiliensituation(@Nonnull Familiensituation familiensituationGS, @Nonnull Familiensituation familiensituationJA) {
		familiensituationGS.setFamilienstatus(familiensituationJA.getFamilienstatus());
		familiensituationGS.setGemeinsameSteuererklaerung(familiensituationJA.getGemeinsameSteuererklaerung());
		familiensituationGS.setGesuchstellerKardinalitaet(familiensituationJA.getGesuchstellerKardinalitaet());
		familiensituationGS.setAenderungPer(familiensituationJA.getAenderungPer());
	}

	private static void copyKindContainer(@Nullable KindContainer container) {
		if (container == null) {
			return;
		}

		if (container.getKindJA() != null) {
			if (container.getKindGS() == null) {
				container.setKindGS(new Kind());
			}
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
	}

	private static void copyKind(@Nonnull Kind kindGS, @Nonnull Kind kindJA) {
		kindGS.setVorname(kindJA.getVorname());
		kindGS.setMutterspracheDeutsch(kindJA.getMutterspracheDeutsch());
		if (kindJA.getPensumFachstelle() != null) {
			kindGS.setPensumFachstelle(new PensumFachstelle());
			Objects.requireNonNull(kindGS.getPensumFachstelle());
			kindGS.getPensumFachstelle().setFachstelle(kindJA.getPensumFachstelle().getFachstelle());
			kindGS.getPensumFachstelle().setPensum(kindJA.getPensumFachstelle().getPensum());
			kindGS.getPensumFachstelle().setGueltigkeit(kindJA.getPensumFachstelle().getGueltigkeit());
		}
		kindGS.setEinschulungTyp(kindJA.getEinschulungTyp());
		kindGS.setFamilienErgaenzendeBetreuung(kindJA.getFamilienErgaenzendeBetreuung());
		kindGS.setKinderabzug(kindJA.getKinderabzug());
		kindGS.setGeburtsdatum(kindJA.getGeburtsdatum());
		kindGS.setNachname(kindJA.getNachname());
		kindGS.setGeschlecht(kindJA.getGeschlecht());
	}

	private static void copyBetreuungspensumContainer(@Nullable BetreuungspensumContainer container) {
		if (container != null) {
			if (container.getBetreuungspensumJA() != null) {
				if (container.getBetreuungspensumGS() == null) {
					container.setBetreuungspensumGS(new Betreuungspensum());
				}
				copyBetreuungspensum(container.getBetreuungspensumGS(), container.getBetreuungspensumJA());
			} else {
				container.setBetreuungspensumGS(null);
			}
		}
	}

	private static void copyBetreuungspensum(@Nonnull Betreuungspensum betreuungspensumGS, @Nonnull Betreuungspensum betreuungspensumJA) {
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

	private static void copyGesuchsteller(@Nonnull Gesuchsteller gesuchstellerGS, @Nonnull Gesuchsteller gesuchstellerJA) {
		gesuchstellerGS.setGeschlecht(gesuchstellerJA.getGeschlecht());
		gesuchstellerGS.setVorname(gesuchstellerJA.getVorname());
		gesuchstellerGS.setNachname(gesuchstellerJA.getNachname());
		gesuchstellerGS.setGeburtsdatum(gesuchstellerJA.getGeburtsdatum());
		gesuchstellerGS.setMail(gesuchstellerJA.getMail());
		gesuchstellerGS.setMobile(gesuchstellerJA.getMobile());
		gesuchstellerGS.setTelefon(gesuchstellerJA.getTelefon());
		gesuchstellerGS.setTelefonAusland(gesuchstellerJA.getTelefonAusland());
		gesuchstellerGS.setEwkPersonId(gesuchstellerJA.getEwkPersonId());
		gesuchstellerGS.setEwkAbfrageDatum(gesuchstellerJA.getEwkAbfrageDatum());
		gesuchstellerGS.setDiplomatenstatus(gesuchstellerJA.isDiplomatenstatus());
		gesuchstellerGS.setKorrespondenzSprache(gesuchstellerJA.getKorrespondenzSprache());
	}

	private static void copyGesuchstellerAdresseContainer(@Nullable GesuchstellerAdresseContainer container) {
		if (container != null) {
			if (container.getGesuchstellerAdresseJA() != null) {
				if (container.getGesuchstellerAdresseGS() == null) {
					container.setGesuchstellerAdresseGS(new GesuchstellerAdresse());
				}
				copyGesuchstellerAdresse(container.getGesuchstellerAdresseGS(), container.getGesuchstellerAdresseJA());
			} else {
				container.setGesuchstellerAdresseGS(null);
			}
		}
	}

	private static void copyGesuchstellerAdresse(@Nonnull GesuchstellerAdresse gs, @Nonnull GesuchstellerAdresse ja) {
		gs.setGueltigkeit(new DateRange(ja.getGueltigkeit()));
		gs.setStrasse(ja.getStrasse());
		gs.setHausnummer(ja.getHausnummer());
		gs.setZusatzzeile(ja.getZusatzzeile());
		gs.setPlz(ja.getPlz());
		gs.setOrt(ja.getOrt());
		gs.setLand(ja.getLand());
		gs.setGemeinde(ja.getGemeinde());
		gs.setOrganisation(ja.getOrganisation());
		gs.setAdresseTyp(ja.getAdresseTyp());
		gs.setNichtInGemeinde(ja.isNichtInGemeinde());
	}

	private static void copyAbstractFinanzielleSituation(@Nonnull AbstractFinanzielleSituation gs, @Nonnull AbstractFinanzielleSituation ja) {
		gs.setSteuerveranlagungErhalten(ja.getSteuerveranlagungErhalten());
		gs.setSteuererklaerungAusgefuellt(ja.getSteuererklaerungAusgefuellt());
		gs.setFamilienzulage(ja.getFamilienzulage());
		gs.setErsatzeinkommen(ja.getErsatzeinkommen());
		gs.setErhalteneAlimente(ja.getErhalteneAlimente());
		gs.setBruttovermoegen(ja.getBruttovermoegen());
		gs.setSchulden(ja.getSchulden());
		gs.setGeschaeftsgewinnBasisjahr(ja.getGeschaeftsgewinnBasisjahr());
		gs.setGeleisteteAlimente(ja.getGeleisteteAlimente());
	}

	private static void copyEinkommensverschlechterungInfoContainer(@Nullable EinkommensverschlechterungInfoContainer container) {
		if (container != null) {
			if (container.getEinkommensverschlechterungInfoGS() == null) {
				container.setEinkommensverschlechterungInfoGS(new EinkommensverschlechterungInfo());
			}
			copyEinkommensverschlechterungInfo(container.getEinkommensverschlechterungInfoGS(), container.getEinkommensverschlechterungInfoJA());
		}
	}

	private static void copyEinkommensverschlechterungInfo(@Nonnull EinkommensverschlechterungInfo gs, @Nonnull EinkommensverschlechterungInfo ja) {
		gs.setEinkommensverschlechterung(ja.getEinkommensverschlechterung());
		gs.setEkvFuerBasisJahrPlus1(ja.getEkvFuerBasisJahrPlus1());
		gs.setEkvFuerBasisJahrPlus2(ja.getEkvFuerBasisJahrPlus2());
		gs.setGemeinsameSteuererklaerung_BjP1(ja.getGemeinsameSteuererklaerung_BjP1());
		gs.setGemeinsameSteuererklaerung_BjP2(ja.getGemeinsameSteuererklaerung_BjP2());
		gs.setGrundFuerBasisJahrPlus1(ja.getGrundFuerBasisJahrPlus1());
		gs.setGrundFuerBasisJahrPlus2(ja.getGrundFuerBasisJahrPlus2());
		gs.setStichtagFuerBasisJahrPlus1(ja.getStichtagFuerBasisJahrPlus1());
		gs.setStichtagFuerBasisJahrPlus2(ja.getStichtagFuerBasisJahrPlus2());
	}

	private static void copyEinkommensverschlechterungContainer(@Nullable EinkommensverschlechterungContainer container) {
		if (container != null) {
			if (container.getEkvJABasisJahrPlus1() != null) {
				if (container.getEkvGSBasisJahrPlus1() == null) {
					container.setEkvGSBasisJahrPlus1(new Einkommensverschlechterung());
				}
				copyEinkommensverschlechterung(container.getEkvGSBasisJahrPlus1(), container.getEkvJABasisJahrPlus1());
			} else {
				container.setEkvGSBasisJahrPlus1(null);
			}
			if (container.getEkvJABasisJahrPlus2() != null) {
				if (container.getEkvGSBasisJahrPlus2() == null) {
					container.setEkvGSBasisJahrPlus2(new Einkommensverschlechterung());
				}
				copyEinkommensverschlechterung(container.getEkvGSBasisJahrPlus2(), container.getEkvJABasisJahrPlus2());
			} else {
				container.setEkvGSBasisJahrPlus2(null);
			}
		}
	}

	private static void copyEinkommensverschlechterung(@Nonnull Einkommensverschlechterung gs, @Nonnull Einkommensverschlechterung ja) {
		copyAbstractFinanzielleSituation(gs, ja);
		gs.setNettolohnJan(ja.getNettolohnJan());
		gs.setNettolohnFeb(ja.getNettolohnFeb());
		gs.setNettolohnMrz(ja.getNettolohnMrz());
		gs.setNettolohnApr(ja.getNettolohnApr());
		gs.setNettolohnMai(ja.getNettolohnMai());
		gs.setNettolohnJun(ja.getNettolohnJun());
		gs.setNettolohnJul(ja.getNettolohnJul());
		gs.setNettolohnAug(ja.getNettolohnAug());
		gs.setNettolohnSep(ja.getNettolohnSep());
		gs.setNettolohnOkt(ja.getNettolohnOkt());
		gs.setNettolohnNov(ja.getNettolohnNov());
		gs.setNettolohnDez(ja.getNettolohnDez());
		gs.setNettolohnZus(ja.getNettolohnZus());
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
		gs.setNettolohn(ja.getNettolohn());
		gs.setGeschaeftsgewinnBasisjahrMinus1(ja.getGeschaeftsgewinnBasisjahrMinus1());
		gs.setGeschaeftsgewinnBasisjahrMinus2(ja.getGeschaeftsgewinnBasisjahrMinus2());
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
			copyErwerbspensum(container.getErwerbspensumGS(), container.getErwerbspensumJA());
		}
	}

	private static void copyErwerbspensum(@Nonnull Erwerbspensum erwerbspensumGS, @Nonnull Erwerbspensum erwerbspensumJA) {
		erwerbspensumGS.setGueltigkeit(new DateRange(erwerbspensumJA.getGueltigkeit()));
		erwerbspensumGS.setPensum(erwerbspensumJA.getPensum());
		erwerbspensumGS.setTaetigkeit(erwerbspensumJA.getTaetigkeit());
		erwerbspensumGS.setZuschlagZuErwerbspensum(erwerbspensumJA.getZuschlagZuErwerbspensum());
		erwerbspensumGS.setZuschlagsgrund(erwerbspensumJA.getZuschlagsgrund());
		erwerbspensumGS.setZuschlagsprozent(erwerbspensumJA.getZuschlagsprozent());
		erwerbspensumGS.setBezeichnung(erwerbspensumJA.getBezeichnung());
	}
}
