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
}
