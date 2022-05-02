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

package ch.dvbern.ebegu.rules.util;

import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import ch.dvbern.ebegu.dto.VerfuegungsBemerkungDTO;
import ch.dvbern.ebegu.dto.VerfuegungsBemerkungDTOList;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.Assert;
import org.junit.Test;

public class VerfuegungsBemerkungDTOListTest {

	private final DateRange dateRangeFullAugust =
		new DateRange(LocalDate.of(2021, Month.AUGUST, 1), LocalDate.of(2021, Month.AUGUST, 31));
	private final DateRange dateRangeFirstHalfAugust =
		new DateRange(LocalDate.of(2021, Month.AUGUST, 1), LocalDate.of(2021, Month.AUGUST, 15));
	private final DateRange dateRangeLastHalfAugust =
		new DateRange(LocalDate.of(2021, Month.AUGUST, 16), LocalDate.of(2021, Month.AUGUST, 31));
	private final DateRange dateRangePartAugust =
		new DateRange(LocalDate.of(2021, Month.AUGUST, 10), LocalDate.of(2021, Month.AUGUST, 20));


	@Test
	public void test_RuleValidityGemeinde_strongerThenAsiv() {
		VerfuegungsBemerkungDTO verfuegungsBemerkungDTOAsiv = createDefaultVerfugeungsBemerkungeDto(MsgKey.ABWESENHEIT_MSG);
		VerfuegungsBemerkungDTO verfuegungsBemerkungDTOGemeinde = new VerfuegungsBemerkungDTO(RuleValidity.GEMEINDE, MsgKey.ABWESENHEIT_MSG,
			Locale.GERMAN);
		verfuegungsBemerkungDTOGemeinde.setGueltigkeit(dateRangeFullAugust);

		VerfuegungsBemerkungDTOList list = new VerfuegungsBemerkungDTOList();
		list.addBemerkung(verfuegungsBemerkungDTOAsiv);
		list.addBemerkung(verfuegungsBemerkungDTOGemeinde);


		List<VerfuegungsBemerkungDTO> result = list.getRequiredBemerkungen();
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(RuleValidity.GEMEINDE, result.get(0).getRuleValidity());
		Assert.assertEquals(dateRangeFullAugust, result.get(0).getGueltigkeit());
	}

	@Test
	public void test_aussertOrdentlicherAnspruch_ueberschreibt() {
		VerfuegungsBemerkungDTOList list = new VerfuegungsBemerkungDTOList();
		list.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG));
		list.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		list.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.FACHSTELLE_MSG));

		List<VerfuegungsBemerkungDTO> result = list.getRequiredBemerkungen();
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG, result.get(0).getMsgKey());
	}

	@Test
	public void test_fachstellen_ueberschreibt() {
		VerfuegungsBemerkungDTOList list = new VerfuegungsBemerkungDTOList();
		list.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.FACHSTELLE_MSG));
		list.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.ERWERBSPENSUM_ANSPRUCH));

		List<VerfuegungsBemerkungDTO> result = list.getRequiredBemerkungen();
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(MsgKey.FACHSTELLE_MSG, result.get(0).getMsgKey());
	}

	@Test
	public void test_fachstellen_ueberschreibt_FKJV() {
		VerfuegungsBemerkungDTOList list = new VerfuegungsBemerkungDTOList();
		list.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.FACHSTELLE_MSG));
		list.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.ERWERBSPENSUM_ANSPRUCH));

		List<VerfuegungsBemerkungDTO> result = list.getRequiredBemerkungen(true);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(MsgKey.FACHSTELLE_MSG_FKJV, result.get(0).getMsgKey());
	}

	@Test
	public void test_keineErweitertenBeduerfnisse_ueberschreibt() {
		VerfuegungsBemerkungDTOList list = new VerfuegungsBemerkungDTOList();
		list.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.KEINE_ERWEITERTE_BEDUERFNISSE_MSG));
		list.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.ERWEITERTE_BEDUERFNISSE_MSG));

		List<VerfuegungsBemerkungDTO> result = list.getRequiredBemerkungen();
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(MsgKey.KEINE_ERWEITERTE_BEDUERFNISSE_MSG, result.get(0).getMsgKey());
	}


	@Test
	public void test_Bemerkung_rangeNotFullMonth() {
		VerfuegungsBemerkungDTO bemerkung = createDefaultVerfugeungsBemerkungeDto(MsgKey.ABWESENHEIT_MSG);
		bemerkung.setGueltigkeit(dateRangeFirstHalfAugust);

		VerfuegungsBemerkungDTOList bemerkungList = new VerfuegungsBemerkungDTOList();
		bemerkungList.addBemerkung(bemerkung);

		List<VerfuegungsBemerkungDTO> result = bemerkungList.getRequiredBemerkungen();
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(dateRangeFirstHalfAugust, result.get(0).getGueltigkeit());
	}

	@Test
	public void test_sameBemerkung_adjacentRange() {
		VerfuegungsBemerkungDTO bemerkung1 = createDefaultVerfugeungsBemerkungeDto(MsgKey.ABWESENHEIT_MSG);
		bemerkung1.setGueltigkeit(dateRangeFirstHalfAugust);

		VerfuegungsBemerkungDTO bemerkung2 = createDefaultVerfugeungsBemerkungeDto(MsgKey.ABWESENHEIT_MSG);
		bemerkung2.setGueltigkeit(dateRangeLastHalfAugust);

		VerfuegungsBemerkungDTOList bemerkungList = new VerfuegungsBemerkungDTOList();
		bemerkungList.addBemerkung(bemerkung1);
		bemerkungList.addBemerkung(bemerkung2);

		List<VerfuegungsBemerkungDTO> result = bemerkungList.getRequiredBemerkungen().stream()
			.sorted(Comparator.comparing(VerfuegungsBemerkungDTO::getGueltigkeit))
			.collect(Collectors.toList());
		Assert.assertEquals(2, result.size());

		Assert.assertEquals(dateRangeFirstHalfAugust, result.get(0).getGueltigkeit());
		Assert.assertEquals(dateRangeLastHalfAugust, result.get(1).getGueltigkeit());
	}

	@Test
	public void test_sameBemerkung_adjacentRange_ausserorderntlicherAnspruch_fullMonth_ueberschreibt() {
		VerfuegungsBemerkungDTO bemerkungErwerbspensumFirst = createDefaultVerfugeungsBemerkungeDto(MsgKey.ERWERBSPENSUM_ANSPRUCH);
		bemerkungErwerbspensumFirst.setGueltigkeit(dateRangeFirstHalfAugust);

		VerfuegungsBemerkungDTO bemerkungErwerbspensumLast = createDefaultVerfugeungsBemerkungeDto(MsgKey.ERWERBSPENSUM_ANSPRUCH);
		bemerkungErwerbspensumLast.setGueltigkeit(dateRangeLastHalfAugust);

		VerfuegungsBemerkungDTOList bemerkungList = new VerfuegungsBemerkungDTOList();

		bemerkungList.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG));
		bemerkungList.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.FACHSTELLE_MSG));
		bemerkungList.addBemerkung(bemerkungErwerbspensumFirst);
		bemerkungList.addBemerkung(bemerkungErwerbspensumLast);

		List<VerfuegungsBemerkungDTO> result = bemerkungList.getRequiredBemerkungen();
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG, result.get(0).getMsgKey());
		Assert.assertEquals(dateRangeFullAugust, result.get(0).getGueltigkeit());
	}

	@Test
	public void test_sameBemerkung_adjacentRange_ausserorderntlicherAnspruch_notFullMonth_ueberschreibt() {
		//Test:
		//INPUT:
		//Bemerkung Ausserordentlicher Anspruch 15.08-31.08
		//Bemerkung Fachstelle 01.08-31.08
		//RESULT
		//Bemerkung Ausserordentlicher Anspruch 15.08-31.08
		//Bemerkung Fachstelle 01.08-14.08
		VerfuegungsBemerkungDTO bemerkungAusserordernlicherAnspruch = createDefaultVerfugeungsBemerkungeDto(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG);
		bemerkungAusserordernlicherAnspruch.setGueltigkeit(dateRangeLastHalfAugust);

		VerfuegungsBemerkungDTOList bemerkungList = new VerfuegungsBemerkungDTOList();
		bemerkungList.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.FACHSTELLE_MSG));
		bemerkungList.addBemerkung(bemerkungAusserordernlicherAnspruch);

		List<VerfuegungsBemerkungDTO> result = bemerkungList.getRequiredBemerkungen();
		Assert.assertEquals(2, result.size());

		Map<MsgKey, List<VerfuegungsBemerkungDTO>> bemerkungenByMessageKey = mapVerfuegungsBemerkungByMsgKey(result);

		Assert.assertEquals(1, bemerkungenByMessageKey.get(MsgKey.FACHSTELLE_MSG).size());
		Assert.assertEquals(dateRangeFirstHalfAugust, bemerkungenByMessageKey.get(MsgKey.FACHSTELLE_MSG).get(0).getGueltigkeit());

		Assert.assertEquals(1,  bemerkungenByMessageKey.get(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG).size());
		Assert.assertEquals(dateRangeLastHalfAugust, bemerkungenByMessageKey.get(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG).get(0).getGueltigkeit());
	}


	@Test
	public void test_sameBemerkung_adjacentRange_ausserorderntlicherAnspruch_notFullMonth_ueberschreibt_middle() {
		//Test:
		//INPUT:
		//Bemerkung Ausserordentlicher Anspruch 10.08-20.08
		//Bemerkung Fachstelle 01.08-31.08
		//EXPECTED RESULT
		//Bemerkung Ausserordentlicher Anspruch 10.08-20.08
		//Bemerkung Fachstelle 01.08-09.08 und 21.08.-31.08
		VerfuegungsBemerkungDTO bemerkungAusserordernlicherAnspruch = createDefaultVerfugeungsBemerkungeDto(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG);
		bemerkungAusserordernlicherAnspruch.setGueltigkeit(dateRangePartAugust);

		VerfuegungsBemerkungDTOList bemerkungList = new VerfuegungsBemerkungDTOList();
		bemerkungList.addBemerkung(createDefaultVerfugeungsBemerkungeDto(MsgKey.FACHSTELLE_MSG));
		bemerkungList.addBemerkung(bemerkungAusserordernlicherAnspruch);

		List<VerfuegungsBemerkungDTO> result = bemerkungList.getRequiredBemerkungen();
		Assert.assertEquals(3, result.size());

		DateRange expectedFachstellenGueltigkeitPart1 =
			new DateRange(LocalDate.of(2021, Month.AUGUST, 1), LocalDate.of(2021, Month.AUGUST, 9));
		DateRange expectedFachstellenGueltigkeitPart2 =
			new DateRange(LocalDate.of(2021, Month.AUGUST, 21), LocalDate.of(2021, Month.AUGUST, 31));

		Map<MsgKey, List<VerfuegungsBemerkungDTO>> bemerkungenByMessageKey = mapVerfuegungsBemerkungByMsgKey(result);

		Assert.assertEquals(2, bemerkungenByMessageKey.get(MsgKey.FACHSTELLE_MSG).size());
		Assert.assertEquals(expectedFachstellenGueltigkeitPart1, bemerkungenByMessageKey.get(MsgKey.FACHSTELLE_MSG).get(0).getGueltigkeit());
		Assert.assertEquals(expectedFachstellenGueltigkeitPart2, bemerkungenByMessageKey.get(MsgKey.FACHSTELLE_MSG).get(1).getGueltigkeit());

		Assert.assertEquals(1,  bemerkungenByMessageKey.get(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG).size());
		Assert.assertEquals(dateRangePartAugust, bemerkungenByMessageKey.get(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG).get(0).getGueltigkeit());
	}

	private Map<MsgKey, List<VerfuegungsBemerkungDTO>> mapVerfuegungsBemerkungByMsgKey(List<VerfuegungsBemerkungDTO> bemerkungen) {
		return bemerkungen.stream()
			.collect(Collectors.groupingBy(VerfuegungsBemerkungDTO::getMsgKey));
	}

	private VerfuegungsBemerkungDTO createDefaultVerfugeungsBemerkungeDto(MsgKey msgKey) {
		VerfuegungsBemerkungDTO verfuegungsBemerkungDTO = new VerfuegungsBemerkungDTO(
			RuleValidity.ASIV,
			msgKey,
			Locale.GERMAN);
		verfuegungsBemerkungDTO.setGueltigkeit(dateRangeFullAugust);;
		return verfuegungsBemerkungDTO;
	}
}
