package ch.dvbern.ebegu.enums;

import ch.dvbern.ebegu.entities.Einstellung;

public enum AnspruchBeschaeftigungAbhaengigkeitTyp {

	ABHAENGING,
	UNABHAENGING,
	MINIMUM;

	public boolean isAnspruchUnabhaengig() {
		return this == UNABHAENGING;
	}

	public static AnspruchBeschaeftigungAbhaengigkeitTyp getEnumValue(Einstellung einstellung) {
		return AnspruchBeschaeftigungAbhaengigkeitTyp.valueOf(einstellung.getValue());
	}

}
