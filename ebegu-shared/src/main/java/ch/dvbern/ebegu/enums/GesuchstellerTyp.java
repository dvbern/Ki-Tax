package ch.dvbern.ebegu.enums;

public enum GesuchstellerTyp {
	GESUCHSTELLER_1(1),
	GESUCHSTELLER_2(2);

	private int gesuchstellerNummer;

	GesuchstellerTyp(int gesuchstellerNummer) {
		this.gesuchstellerNummer = gesuchstellerNummer;
	}

	public int getGesuchstellerNummer() {
		return gesuchstellerNummer;
	}

	public static GesuchstellerTyp getGesuchstellerTypByNummer(int nummer) {
		if (nummer == 1) {
			return GESUCHSTELLER_1;
		}

		if (nummer == 2) {
			return GESUCHSTELLER_2;
		}

		throw new IllegalArgumentException("Invalid GesuchstellerNummer " + nummer
												   + " can not be converted to Gesuchstellertyp");
	}
}
