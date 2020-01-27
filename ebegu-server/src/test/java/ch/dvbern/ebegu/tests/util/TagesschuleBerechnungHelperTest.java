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

package ch.dvbern.ebegu.tests.util;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschuleZeitabschnitt;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.rechner.TagesschuleRechnerParameterDTO;
import ch.dvbern.ebegu.services.util.TagesschuleBerechnungHelper;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TagesschuleBerechnungHelperTest {

	private AnmeldungTagesschule anmeldungTagesschule;
	private TagesschuleRechnerParameterDTO parameterDTO;
	private Verfuegung verfuegungMitFamiliensituation;

	private static final MathUtil MATH = MathUtil.DEFAULT;

	@Before
	public void setUp() {
		anmeldungTagesschule = initAnmedlungTagesschule();
		parameterDTO = initTagesschuleRechnerParameterDTO();
		verfuegungMitFamiliensituation = initVerfuegungMitFamiliensituation();
	}

	@Test
	public void testTagesschuleBerechnungHelper(){
	 List<AnmeldungTagesschuleZeitabschnitt> anmeldungTagesschuleZeitabschnitts =
		 TagesschuleBerechnungHelper.calculateZeitabschnitte(anmeldungTagesschule, parameterDTO,
			verfuegungMitFamiliensituation);
		Assert.assertNotNull(anmeldungTagesschuleZeitabschnitts);
		Assert.assertEquals(4, anmeldungTagesschuleZeitabschnitts.size());

		AnmeldungTagesschuleZeitabschnitt anmeldungTagesschuleZeitabschnitt = anmeldungTagesschuleZeitabschnitts.get(0);
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(120000)),
			anmeldungTagesschuleZeitabschnitt.getMassgebendesEinkommenInklAbzugFamgr());
		Assert.assertEquals(new BigDecimal(7),
			anmeldungTagesschuleZeitabschnitt.getBetreuungsstundenProWoche());
		Assert.assertEquals(new BigDecimal(30),
			anmeldungTagesschuleZeitabschnitt.getBetreuungsminutenProWoche());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(8.32)),
			anmeldungTagesschuleZeitabschnitt.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20.00)),
			anmeldungTagesschuleZeitabschnitt.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(82.40)),
			anmeldungTagesschuleZeitabschnitt.getTotalKostenProWoche());

		anmeldungTagesschuleZeitabschnitt = anmeldungTagesschuleZeitabschnitts.get(1);
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(100000)),
			anmeldungTagesschuleZeitabschnitt.getMassgebendesEinkommenInklAbzugFamgr());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(6.36)),
			anmeldungTagesschuleZeitabschnitt.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)),
			anmeldungTagesschuleZeitabschnitt.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(67.70)),
			anmeldungTagesschuleZeitabschnitt.getTotalKostenProWoche());

		anmeldungTagesschuleZeitabschnitt = anmeldungTagesschuleZeitabschnitts.get(2);
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(4.29)),
			anmeldungTagesschuleZeitabschnitt.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)),
			anmeldungTagesschuleZeitabschnitt.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(52.18)),
			anmeldungTagesschuleZeitabschnitt.getTotalKostenProWoche());

		anmeldungTagesschuleZeitabschnitt = anmeldungTagesschuleZeitabschnitts.get(3);
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(3.38)),
			anmeldungTagesschuleZeitabschnitt.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)),
			anmeldungTagesschuleZeitabschnitt.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(45.35)),
			anmeldungTagesschuleZeitabschnitt.getTotalKostenProWoche());
	}

	private AnmeldungTagesschule initAnmedlungTagesschule(){

		Set<ModulTagesschule> modulTagesschuleSet = initModuleTagesschule(true);
		modulTagesschuleSet.addAll(initModuleTagesschule(false));

		BelegungTagesschule belegungTagesschule = new BelegungTagesschule();
		belegungTagesschule.setBelegungTagesschuleModule(new TreeSet<BelegungTagesschuleModul>());
		modulTagesschuleSet.forEach(
			modulTagesschule -> {
				BelegungTagesschuleModul belegungTagesschuleModul = new BelegungTagesschuleModul();
				belegungTagesschuleModul.setModulTagesschule(modulTagesschule);
				belegungTagesschuleModul.setBelegungTagesschule(belegungTagesschule);
				belegungTagesschule.getBelegungTagesschuleModule().add(belegungTagesschuleModul);
			}
		);

		AnmeldungTagesschule anmeldungTagesschule = new AnmeldungTagesschule();
		anmeldungTagesschule.setBelegungTagesschule(belegungTagesschule);
		return anmeldungTagesschule;
	}

	private Set<ModulTagesschule> initModuleTagesschule (boolean wirdPedagogischBetreut){
		ModulTagesschuleGroup modulTagesschuleGroupPedagogischBetreut = new ModulTagesschuleGroup();
		modulTagesschuleGroupPedagogischBetreut.setZeitVon(LocalTime.of(8,0));
		modulTagesschuleGroupPedagogischBetreut.setZeitBis(LocalTime.of(11,45));
		modulTagesschuleGroupPedagogischBetreut.setVerpflegungskosten(MATH.from(10));
		modulTagesschuleGroupPedagogischBetreut.setWirdPaedagogischBetreut(wirdPedagogischBetreut);

		ModulTagesschule modulTagesschuleMonday = new ModulTagesschule();
		modulTagesschuleMonday.setModulTagesschuleGroup(modulTagesschuleGroupPedagogischBetreut);
		modulTagesschuleMonday.setWochentag(DayOfWeek.MONDAY);

		ModulTagesschule modulTagesschuleFriday= new ModulTagesschule();
		modulTagesschuleFriday.setModulTagesschuleGroup(modulTagesschuleGroupPedagogischBetreut);
		modulTagesschuleFriday.setWochentag(DayOfWeek.FRIDAY);

		Set<ModulTagesschule> modulTagesschuleSet = new TreeSet<>();
		modulTagesschuleSet.add(modulTagesschuleMonday);
		modulTagesschuleSet.add(modulTagesschuleFriday);

		modulTagesschuleGroupPedagogischBetreut.setModule(modulTagesschuleSet);

		return modulTagesschuleSet;
	}

	private TagesschuleRechnerParameterDTO initTagesschuleRechnerParameterDTO(){
		TagesschuleRechnerParameterDTO tagesschuleRechnerParameterDTO = new TagesschuleRechnerParameterDTO();
		tagesschuleRechnerParameterDTO.setMaxTarifMitPaedagogischerBetreuung(MATH.from(12.24));
		tagesschuleRechnerParameterDTO.setMaxTarifOhnePaedagogischerBetreuung(MATH.from(6.11));
		tagesschuleRechnerParameterDTO.setMinTarif(MATH.from(0.78));
		tagesschuleRechnerParameterDTO.setMaxMassgebendesEinkommen(MATH.from(160000));
		tagesschuleRechnerParameterDTO.setMinMassgebendesEinkommen(MATH.from(43000));

		return tagesschuleRechnerParameterDTO;
	}

	private Verfuegung initVerfuegungMitFamiliensituation(){
		Verfuegung verfuegung = new Verfuegung();

		VerfuegungZeitabschnitt verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.getBgCalculationResultAsiv().setMassgebendesEinkommenVorAbzugFamgr(MATH.from(120000));
		verfuegungZeitabschnitt.getBgCalculationInputAsiv().setBezahltVollkosten(false);

		VerfuegungZeitabschnitt verfuegungZeitabschnittZwei = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnittZwei.getBgCalculationResultAsiv().setMassgebendesEinkommenVorAbzugFamgr(MATH.from(100000));
		verfuegungZeitabschnittZwei.getBgCalculationInputAsiv().setBezahltVollkosten(false);

		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList = new ArrayList<>();
		verfuegungZeitabschnittList.add(verfuegungZeitabschnitt);
		verfuegungZeitabschnittList.add(verfuegungZeitabschnittZwei);
		verfuegung.setZeitabschnitte(verfuegungZeitabschnittList);

		return verfuegung;
	}
}
