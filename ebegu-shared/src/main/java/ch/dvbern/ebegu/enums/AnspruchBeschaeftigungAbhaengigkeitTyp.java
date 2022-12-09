package ch.dvbern.ebegu.enums;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.util.AnspruchBeschaeftigungAbhangigkeitTypVisitor;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public enum AnspruchBeschaeftigungAbhaengigkeitTyp {

	ABHAENGING {
		@Override
		public <T> T accept(AnspruchBeschaeftigungAbhangigkeitTypVisitor<T> visitor) {
			return visitor.visitAbhaengig();
		}
	},
	UNABHAENGING {
		@Override
		public <T> T accept(AnspruchBeschaeftigungAbhangigkeitTypVisitor<T> visitor) {
			return visitor.visitUnabhaengig();
		}
	},
	MINIMUM {
		@Override
		public <T> T accept(AnspruchBeschaeftigungAbhangigkeitTypVisitor<T> visitor) {
			return visitor.visitMinimum();
		}
	};

	public boolean isAnspruchUnabhaengig() {
		return this == UNABHAENGING;
	}

	public static AnspruchBeschaeftigungAbhaengigkeitTyp getEnumValue(Einstellung einstellung) {
		return AnspruchBeschaeftigungAbhaengigkeitTyp.valueOf(einstellung.getValue());
	}

	public abstract <T> T accept(AnspruchBeschaeftigungAbhangigkeitTypVisitor<T> visitor);

}
