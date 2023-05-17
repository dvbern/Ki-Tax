package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationSelbstdeklaration;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.util.MathUtil;
import org.jetbrains.annotations.NotNull;

public class LuzernTestfallDataProvider extends AbstractTestfallDataProvider {

	protected LuzernTestfallDataProvider(Gesuchsperiode gesuchsperiode) {
		super(gesuchsperiode);
	}

	@Override
	public Familiensituation createVerheiratet() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		familiensituation.setGemeinsameSteuererklaerung(Boolean.TRUE);
		setAuszahlungsdatenInforma(familiensituation);
		return familiensituation;
	}

	@Override
	public Familiensituation createAlleinerziehend() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		setAuszahlungsdatenInforma(familiensituation);
		return familiensituation;
	}

	@Override
	public FinanzielleSituation createFinanzielleSituation(BigDecimal vermoegen, BigDecimal einkommen) {
		FinanzielleSituation finanzielleSituation = createDefaultFinanzielleSituation();
		finanzielleSituation.setQuellenbesteuert(true);
		final FinanzielleSituationSelbstdeklaration selbstdeklaration =
				new FinanzielleSituationSelbstdeklaration();
		selbstdeklaration.setVermoegen(vermoegen);
		selbstdeklaration.setEinkunftErwerb(einkommen);
		selbstdeklaration.setEinkunftVersicherung(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setEinkunftWertschriften(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setEinkunftUnterhaltsbeitragKinder(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setEinkunftUeberige(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setEinkunftLiegenschaften(MathUtil.DEFAULT.from(0));

		selbstdeklaration.setAbzugBerufsauslagen(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugSchuldzinsen(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugUnterhaltsbeitragKinder(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugSaeule3A(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugVersicherungspraemien(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugKrankheitsUnfallKosten(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setSonderabzugErwerbstaetigkeitEhegatten(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugKinderSchule(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugKinderVorschule(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugEigenbetreuung(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugFremdbetreuung(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugErwerbsunfaehigePersonen(MathUtil.DEFAULT.from(0));

		selbstdeklaration.setAbzugSteuerfreierBetragErwachsene(MathUtil.DEFAULT.from(0));
		selbstdeklaration.setAbzugSteuerfreierBetragKinder(MathUtil.DEFAULT.from(0));

		finanzielleSituation.setSelbstdeklaration(selbstdeklaration);
		return finanzielleSituation;
	}

	@Override
	public FinanzielleSituationTyp getFinanzielleSituationTyp() {
		return FinanzielleSituationTyp.LUZERN;
	}

	private void setAuszahlungsdatenInforma(@NotNull Familiensituation familiensituation) {
		if (familiensituation.getAuszahlungsdaten() == null) {
			familiensituation.setAuszahlungsdaten(createDefaultAuszahlungsdaten());
		}

		familiensituation.getAuszahlungsdaten().setInfomaKreditorennummer("0010");
		familiensituation.getAuszahlungsdaten().setInfomaBankcode("00-1-00");
	}

	@Override
	public Erwerbspensum createErwerbspensum(int prozent) {
		Erwerbspensum erwerbspensum = new Erwerbspensum();
		erwerbspensum.setGueltigkeit(gesuchsperiode.getGueltigkeit());
		erwerbspensum.setTaetigkeit(Taetigkeit.ANGESTELLT);
		erwerbspensum.setPensum(prozent);
		erwerbspensum.setBezeichnung("Sachbearbeitung");
		erwerbspensum.setErwerbspensumInstitution("Verwaltung Luzern");
		return erwerbspensum;
	}
}
