package ch.dvbern.ebegu.enums.reporting;

public enum DatumTyp {
	VERFUEGUNGSDATUM("GesuchZeitraumNativeSQLQueryByTimestampVerfuegt"),
	EINREICHEDATUM("GesuchZeitraumNativeSQLQueryByEingangsdatum");

	private final String queryName;
	DatumTyp(String queryName) {
		this.queryName = queryName;
	}

	public String getQueryName() {
		return queryName;
	}
}
