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

package ch.dvbern.ebegu.statemachine;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import ch.dvbern.ebegu.enums.AntragEvents;
import ch.dvbern.ebegu.enums.AntragStatus;
import com.github.oxo42.stateless4j.StateMachineConfig;

/**
 * CDI Producer fuer StateMachineConfig die wir in kiBon benoetigen
 * <p>
 * Hier werden zudem saemtliche onEntry Actions getriggered und an die Services weiterdelegiert.
 */
@SuppressWarnings({ "ClassNamePrefixedWithPackageName", "PMD.UnusedFormalParameter", "VisibilityModifier" })
@Dependent
public class StateMachineConfigProducer {

	private final StateMachineConfig<AntragStatus, AntragEvents> gesuchFSMConfig = new StateMachineConfig<>();

	@Produces
	public StateMachineConfig<AntragStatus, AntragEvents> createStateMachineConfig() {

		gesuchFSMConfig.configure(AntragStatus.IN_BEARBEITUNG_GS)
			.permit(AntragEvents.FREIGABEQUITTUNG_ERSTELLEN, AntragStatus.FREIGABEQUITTUNG)
			.permit(AntragEvents.FREIGEBEN, AntragStatus.FREIGEGEBEN);

		gesuchFSMConfig.configure(AntragStatus.FREIGABEQUITTUNG)
			.permit(AntragEvents.FREIGEBEN, AntragStatus.FREIGEGEBEN)
			// GS darf seinen Freigegebenen Antrag wieder zurueckziehen und nochmals bearbeiten
			.permit(AntragEvents.FREIGABEQUITTUNG_ERSTELLEN, AntragStatus.IN_BEARBEITUNG_GS);

		gesuchFSMConfig.configure(AntragStatus.FREIGEGEBEN)
			.permit(AntragEvents.ERSTES_OEFFNEN_JA, AntragStatus.IN_BEARBEITUNG_JA);

		gesuchFSMConfig.configure(AntragStatus.IN_BEARBEITUNG_JA)
			.permit(AntragEvents.MAHNEN, AntragStatus.ERSTE_MAHNUNG)
			.permit(AntragEvents.GEPRUEFT, AntragStatus.GEPRUEFT)
			.permit(AntragEvents.ABSCHLIESSEN, AntragStatus.NUR_SCHULAMT);

		gesuchFSMConfig.configure(AntragStatus.GEPRUEFT)
			.permit(AntragEvents.ZUWEISUNG_SCHULAMT, AntragStatus.NUR_SCHULAMT)
			.permit(AntragEvents.VERFUEGUNG_STARTEN, AntragStatus.VERFUEGEN)
			.permit(AntragEvents.VERFUEGEN_OHNE_ANGEBOT, AntragStatus.KEIN_ANGEBOT)
			.permit(AntragEvents.KEIN_KONTINGENT, AntragStatus.KEIN_KONTINGENT);

		gesuchFSMConfig.configure(AntragStatus.VERFUEGEN)
			.permit(AntragEvents.VERFUEGEN, AntragStatus.VERFUEGT);

		gesuchFSMConfig.configure(AntragStatus.VERFUEGT)
			.permit(AntragEvents.BESCHWEREN, AntragStatus.BESCHWERDE_HAENGIG)
			.permit(AntragEvents.PRUEFEN_STV, AntragStatus.PRUEFUNG_STV);

		gesuchFSMConfig.configure(AntragStatus.KEIN_ANGEBOT)
			.permit(AntragEvents.BESCHWEREN, AntragStatus.BESCHWERDE_HAENGIG);

		gesuchFSMConfig.configure(AntragStatus.NUR_SCHULAMT)
			.permit(AntragEvents.BESCHWEREN, AntragStatus.BESCHWERDE_HAENGIG)
			.permit(AntragEvents.PRUEFEN_STV, AntragStatus.PRUEFUNG_STV);

		gesuchFSMConfig.configure(AntragStatus.BESCHWERDE_HAENGIG)
			.permit(AntragEvents.ZURUECK_NUR_SCHULAMT, AntragStatus.NUR_SCHULAMT)
			.permit(AntragEvents.ZURUECK_VERFUEGT, AntragStatus.VERFUEGT)
			.permit(AntragEvents.ZURUECK_KEIN_ANGEBOT, AntragStatus.KEIN_ANGEBOT)
			.permit(AntragEvents.ZURUECK_PRUEFUNG_STV, AntragStatus.PRUEFUNG_STV)
			.permit(AntragEvents.ZURUECK_IN_BEARBEITUNG_STV, AntragStatus.IN_BEARBEITUNG_STV)
			.permit(AntragEvents.ZURUECK_GEPRUEFT_STV, AntragStatus.GEPRUEFT_STV);

		gesuchFSMConfig.configure(AntragStatus.PRUEFUNG_STV)
			.permit(AntragEvents.BESCHWEREN, AntragStatus.BESCHWERDE_HAENGIG) //?
			.permit(AntragEvents.ERSTES_OEFFNEN_STV, AntragStatus.IN_BEARBEITUNG_STV)
			.permit(AntragEvents.PRUEFUNG_STV_JA_ABGESCHLOSSEN, AntragStatus.NUR_SCHULAMT)
			.permit(AntragEvents.PRUEFUNG_STV_SCH_ABGESCHLOSSEN, AntragStatus.VERFUEGT);

		gesuchFSMConfig.configure(AntragStatus.IN_BEARBEITUNG_STV)
			.permit(AntragEvents.GEPRUEFT_STV, AntragStatus.GEPRUEFT_STV)
			.permit(AntragEvents.BESCHWEREN, AntragStatus.BESCHWERDE_HAENGIG);

		gesuchFSMConfig.configure(AntragStatus.GEPRUEFT_STV)
			.permit(AntragEvents.BESCHWEREN, AntragStatus.BESCHWERDE_HAENGIG)
			.permit(AntragEvents.PRUEFUNG_STV_JA_ABGESCHLOSSEN, AntragStatus.NUR_SCHULAMT)
			.permit(AntragEvents.PRUEFUNG_STV_SCH_ABGESCHLOSSEN, AntragStatus.VERFUEGT);

		gesuchFSMConfig.configure(AntragStatus.ERSTE_MAHNUNG)
			.permit(AntragEvents.MAHNUNG_ABGELAUFEN, AntragStatus.ERSTE_MAHNUNG_ABGELAUFEN)
			.permit(AntragEvents.MAHNLAUF_BEENDEN, AntragStatus.IN_BEARBEITUNG_JA);

		gesuchFSMConfig.configure(AntragStatus.ERSTE_MAHNUNG_ABGELAUFEN)
			.permit(AntragEvents.MAHNEN, AntragStatus.ZWEITE_MAHNUNG)
			.permit(AntragEvents.MAHNLAUF_BEENDEN, AntragStatus.IN_BEARBEITUNG_JA);

		gesuchFSMConfig.configure(AntragStatus.ZWEITE_MAHNUNG)
			.permit(AntragEvents.MAHNUNG_ABGELAUFEN, AntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN)
			.permit(AntragEvents.MAHNLAUF_BEENDEN, AntragStatus.IN_BEARBEITUNG_JA);

		gesuchFSMConfig.configure(AntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN)
			.permit(AntragEvents.MAHNLAUF_BEENDEN, AntragStatus.IN_BEARBEITUNG_JA);

		gesuchFSMConfig.configure(AntragStatus.KEIN_KONTINGENT)
			.permit(AntragEvents.GEPRUEFT, AntragStatus.GEPRUEFT);

		return gesuchFSMConfig;

	}

}

