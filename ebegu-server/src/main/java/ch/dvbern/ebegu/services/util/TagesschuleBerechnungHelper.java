/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.util;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschuleZeitabschnitt;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.rechner.TagesschuleRechner;
import ch.dvbern.ebegu.rechner.TagesschuleRechnerParameterDTO;
import ch.dvbern.ebegu.util.MathUtil;

public class TagesschuleBerechnungHelper {

	public static List<AnmeldungTagesschuleZeitabschnitt> calculateZeitabschnitte(@Nonnull AnmeldungTagesschule anmeldungTagesschule, @Nonnull TagesschuleRechnerParameterDTO parameterDTO,
		@Nonnull Verfuegung verfuegungMitFamiliensituation){
		calculateStundenUndVerpflegungKostenProWoche(anmeldungTagesschule, parameterDTO);
		List<AnmeldungTagesschuleZeitabschnitt> anmeldungTagesschuleZeitabschnitts = new ArrayList<>();
		if(parameterDTO.getMinutesProWocheMitBetreuung() > 0 || parameterDTO.getStundenProWocheMitBetreuung() > 0) {
			anmeldungTagesschuleZeitabschnitts.addAll(calculateTagesschuleZeitabschnitte(verfuegungMitFamiliensituation, parameterDTO, anmeldungTagesschule, true));
		}
		if(parameterDTO.getMinutesProWocheOhneBetreuung() > 0 || parameterDTO.getStundenProWocheOhneBetreuung() > 0) {
			anmeldungTagesschuleZeitabschnitts.addAll(calculateTagesschuleZeitabschnitte(verfuegungMitFamiliensituation, parameterDTO, anmeldungTagesschule,false));
		}
		return anmeldungTagesschuleZeitabschnitts;
	}


	private static List<AnmeldungTagesschuleZeitabschnitt> calculateTagesschuleZeitabschnitte(@Nonnull Verfuegung verfuegungMitFamiliensituation,
		@Nonnull TagesschuleRechnerParameterDTO parameterDTO, AnmeldungTagesschule anmeldungTagesschule,
		boolean wirdPedagogosichBetreut){

		TagesschuleRechner tagesschuleRechner = new TagesschuleRechner();

		List<AnmeldungTagesschuleZeitabschnitt> anmeldungTagesschuleZeitabschnitts = new ArrayList<>();

		for(VerfuegungZeitabschnitt verfuegungZeitabschnitt: verfuegungMitFamiliensituation.getZeitabschnitte()){
			AnmeldungTagesschuleZeitabschnitt anmeldungTagesschuleZeitabschnitt =
				new AnmeldungTagesschuleZeitabschnitt();
			anmeldungTagesschuleZeitabschnitt.setGueltigkeit(verfuegungZeitabschnitt.getGueltigkeit());
			anmeldungTagesschuleZeitabschnitt.setAnmeldungTagesschule(anmeldungTagesschule);
			anmeldungTagesschuleZeitabschnitt.setMassgebendesEinkommenInklAbzugFamgr(verfuegungZeitabschnitt.getMassgebendesEinkommen());
			BigDecimal tarif = tagesschuleRechner.calculateTarif(verfuegungZeitabschnitt, parameterDTO, wirdPedagogosichBetreut);
			anmeldungTagesschuleZeitabschnitt.setGebuehrProStunde(tarif);
			if (wirdPedagogosichBetreut) {
				anmeldungTagesschuleZeitabschnitt.setVerpflegungskosten(parameterDTO.getVerpflegKostenProWocheMitBetreuung());
				anmeldungTagesschuleZeitabschnitt.setPedagogischBetreut(true);
				BigDecimal betreuungsstundenProWoche = new BigDecimal(parameterDTO.getStundenProWocheMitBetreuung());
				BigDecimal betreuungsminutenProWoche =	new BigDecimal(parameterDTO.getMinutesProWocheMitBetreuung());
				anmeldungTagesschuleZeitabschnitt.setBetreuungsstundenProWoche(betreuungsstundenProWoche);
				anmeldungTagesschuleZeitabschnitt.setBetreuungsminutenProWoche(betreuungsminutenProWoche);
			} else {
				anmeldungTagesschuleZeitabschnitt.setVerpflegungskosten(parameterDTO.getVerpflegKostenProWocheOhneBetreuung());
				anmeldungTagesschuleZeitabschnitt.setPedagogischBetreut(false);
				BigDecimal betreuungsstundenProWoche =  new BigDecimal(parameterDTO.getStundenProWocheOhneBetreuung());
				BigDecimal betreuungsminutenProWoche =	new BigDecimal(parameterDTO.getMinutesProWocheOhneBetreuung());
				anmeldungTagesschuleZeitabschnitt.setBetreuungsstundenProWoche(betreuungsstundenProWoche);
				anmeldungTagesschuleZeitabschnitt.setBetreuungsminutenProWoche(betreuungsminutenProWoche);
			}
			//calculate Total Kosten pro Woche
			anmeldungTagesschuleZeitabschnitt.setTotalKostenProWoche(calculateTotalKostenProWoche(anmeldungTagesschuleZeitabschnitt));
			anmeldungTagesschuleZeitabschnitts.add(anmeldungTagesschuleZeitabschnitt);
		}

		return anmeldungTagesschuleZeitabschnitts;
	}

	private static void calculateStundenUndVerpflegungKostenProWoche(AnmeldungTagesschule anmeldungTagesschule,
		@Nonnull TagesschuleRechnerParameterDTO parameterDTO){
		assert anmeldungTagesschule.getBelegungTagesschule() != null;
		int stundenProWocheMitBetreuung = 0;
		int minutesProWocheMitBetreuung  = 0;
		BigDecimal verpflegKostenProWocheMitBetreuung  = MathUtil.toTwoKommastelle(BigDecimal.ZERO);
		int stundenProWocheOhneBetreuung = 0;
		int minutesProWocheOhneBetreuung = 0;
		BigDecimal verpflegKostenProWocheOhneBetreuung = MathUtil.toTwoKommastelle(BigDecimal.ZERO);
		for (BelegungTagesschuleModul belegungTagesschuleModul :
			anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule()) {
			ModulTagesschule modulTagesschule = belegungTagesschuleModul.getModulTagesschule();
			int hours = modulTagesschule.getModulTagesschuleGroup().getZeitVon().getHour();
			int minutes = modulTagesschule.getModulTagesschuleGroup().getZeitVon().getMinute();
			LocalTime zeitBis = modulTagesschule.getModulTagesschuleGroup().getZeitBis();
			zeitBis = zeitBis.minusHours(hours);
			zeitBis = zeitBis.minusMinutes(minutes);
			if(modulTagesschule.getModulTagesschuleGroup().isWirdPaedagogischBetreut()){
				stundenProWocheMitBetreuung += zeitBis.getHour();
				minutesProWocheMitBetreuung += zeitBis.getMinute();
				if (modulTagesschule.getModulTagesschuleGroup().getVerpflegungskosten() != null) {
					verpflegKostenProWocheMitBetreuung =
						verpflegKostenProWocheMitBetreuung.add(modulTagesschule.getModulTagesschuleGroup().getVerpflegungskosten());
				}
			}
			else{
				stundenProWocheOhneBetreuung += zeitBis.getHour();
				minutesProWocheOhneBetreuung += zeitBis.getMinute();
				if (modulTagesschule.getModulTagesschuleGroup().getVerpflegungskosten() != null) {
					verpflegKostenProWocheOhneBetreuung =
						verpflegKostenProWocheOhneBetreuung.add(modulTagesschule.getModulTagesschuleGroup().getVerpflegungskosten());
				}
			}
		}

		Double additionalHours = minutesProWocheMitBetreuung / 60.0;
		if (additionalHours >= 1.0) {
			int hoursToAdd = additionalHours.intValue();
			stundenProWocheMitBetreuung += hoursToAdd;
			minutesProWocheMitBetreuung -= hoursToAdd * 60;
		}
		additionalHours = minutesProWocheOhneBetreuung / 60.0;
		if (additionalHours >= 1.0) {
			int hoursToAdd = additionalHours.intValue();
			stundenProWocheOhneBetreuung += hoursToAdd;
			minutesProWocheOhneBetreuung -= hoursToAdd * 60;
		}

		parameterDTO.setStundenProWocheMitBetreuung(stundenProWocheMitBetreuung);
		parameterDTO.setMinutesProWocheMitBetreuung(minutesProWocheMitBetreuung);
		parameterDTO.setVerpflegKostenProWocheMitBetreuung(verpflegKostenProWocheMitBetreuung);
		parameterDTO.setStundenProWocheOhneBetreuung(stundenProWocheOhneBetreuung);
		parameterDTO.setMinutesProWocheOhneBetreuung(minutesProWocheOhneBetreuung);
		parameterDTO.setVerpflegKostenProWocheOhneBetreuung(verpflegKostenProWocheOhneBetreuung);
	}

	private static BigDecimal calculateTotalKostenProWoche(AnmeldungTagesschuleZeitabschnitt anmeldungTagesschuleZeitabschnitt){
		BigDecimal hoursProWoche = anmeldungTagesschuleZeitabschnitt.getBetreuungsstundenProWoche();
		BigDecimal minutesProWoche = anmeldungTagesschuleZeitabschnitt.getBetreuungsminutenProWoche();
		BigDecimal totalKostenStunden =
			MathUtil.EXACT.multiply(anmeldungTagesschuleZeitabschnitt.getGebuehrProStunde(),hoursProWoche);
		BigDecimal totalKostenMinuten =
			MathUtil.EXACT.multiply(anmeldungTagesschuleZeitabschnitt.getGebuehrProStunde(),minutesProWoche);
		totalKostenMinuten = MathUtil.EXACT.divide(totalKostenMinuten, new BigDecimal(60));

		return MathUtil.DEFAULT.addNullSafe(totalKostenStunden, totalKostenMinuten,
			anmeldungTagesschuleZeitabschnitt.getVerpflegungskosten());
	}
}
