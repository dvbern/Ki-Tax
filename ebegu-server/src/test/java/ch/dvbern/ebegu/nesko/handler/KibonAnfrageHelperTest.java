package ch.dvbern.ebegu.nesko.handler;

import java.math.BigDecimal;

import ch.dvbern.ebegu.dto.neskovanp.Veranlagungsstand;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.entities.SteuerdatenResponse.SteuerdatenDatenTraeger;
import ch.dvbern.ebegu.enums.GesuchstellerTyp;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.test.GesuchBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class KibonAnfrageHelperTest {

	private Gesuch gesuch = null;
	private KibonAnfrageContext context = null;
	private SteuerdatenResponse response = null;

	@BeforeEach()
	public void setupEach() {
		gesuch = GesuchBuilder.create(gesuchBuilder -> gesuchBuilder
				.withGesuchsteller1("Wälti", "Dagmar")
				.withGesuchsteller2("Wälti", "Simone"));

		context = new KibonAnfrageContext(gesuch, GesuchstellerTyp.GESUCHSTELLER_1, "");
		response = new SteuerdatenResponse();
	}

	@Test()
	public void handleSteuerdatenResponseOffen() {
		response.setVeranlagungsstand(Veranlagungsstand.OFFEN);
		KibonAnfrageHelper.handleSteuerdatenResponse(context, response);
		assertThat(SteuerdatenAnfrageStatus.FAILED, is(context.getSteuerdatenAnfrageStatus()));
	}

	@Test()
	public void handleSteuerdatenResponseUnterjaehrig() {
		response.setUnterjaehrigerFall(true);
		KibonAnfrageHelper.handleSteuerdatenResponse(context, response);
		assertThat(SteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL, is(context.getSteuerdatenAnfrageStatus()));
	}

	@Test()
	public void handleSteuerdatenResponsePartnerNichtGemeinsam() {
		response.setZpvNrPartner(123);
		KibonAnfrageHelper.handleSteuerdatenResponse(context, response);
		assertThat(SteuerdatenAnfrageStatus.FAILED_PARTNER_NICHT_GEMEINSAM, is(context.getSteuerdatenAnfrageStatus()));
	}

	@Test()
	public void handleSteuerdatenResponsePartnerUnregemaessig() {
		response.setUnregelmaessigkeitInDerVeranlagung(true);
		KibonAnfrageHelper.handleSteuerdatenResponse(context, response);
		assertThat(SteuerdatenAnfrageStatus.FAILED_UNREGELMAESSIGKEIT, is(context.getSteuerdatenAnfrageStatus()));
	}

	@Test()
	public void handleSteuerdatenResponseGemeinsamOffen() {
		response.setVeranlagungsstand(Veranlagungsstand.OFFEN);
		KibonAnfrageHelper.handleSteuerdatenGemeinsamResponse(context, response);
		assertThat(SteuerdatenAnfrageStatus.FAILED, is(context.getSteuerdatenAnfrageStatus()));
	}

	@Test()
	public void handleSteuerdatenResponseGemeinsamUnterjaehrig() {
		response.setUnterjaehrigerFall(true);
		KibonAnfrageHelper.handleSteuerdatenGemeinsamResponse(context, response);
		assertThat(SteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL, is(context.getSteuerdatenAnfrageStatus()));
	}

	@Test()
	public void handleSteuerdatenResponseGemeinsamVeraendertePartnerschaft() {
		response.setVeraendertePartnerschaft(true);
		KibonAnfrageHelper.handleSteuerdatenGemeinsamResponse(context, response);
		assertThat(SteuerdatenAnfrageStatus.FAILED_VERAENDERTE_PARTNERSCHAFT, is(context.getSteuerdatenAnfrageStatus()));
	}

	@Test()
	public void handleSteuerdatenResponseGemeinsamUnregemaessigkeit() {
		response.setUnregelmaessigkeitInDerVeranlagung(true);
		KibonAnfrageHelper.handleSteuerdatenGemeinsamResponse(context, response);
		assertThat(SteuerdatenAnfrageStatus.FAILED_UNREGELMAESSIGKEIT, is(context.getSteuerdatenAnfrageStatus()));
	}

	@Test()
	public void handleSteuerdatenResponseGemeinsamKeinPartnerGemeinsam() {
		response.setZpvNrPartner(null);
		KibonAnfrageHelper.handleSteuerdatenGemeinsamResponse(context, response);
		assertThat(SteuerdatenAnfrageStatus.FAILED_KEIN_PARTNER_GEMEINSAM, is(context.getSteuerdatenAnfrageStatus()));
	}

	@Test()
	public void setValuesToFinSitErsatzEinkommenPositiv() {
		response.setSteuerpflichtigesErsatzeinkommenDossiertraeger(BigDecimal.TEN);
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		KibonAnfrageHelper.setValuesToFinSit(finanzielleSituation, response, BigDecimal.ONE, SteuerdatenDatenTraeger.DOSSIERTRAEGER);
		assertThat(finanzielleSituation.getErsatzeinkommen(), is(BigDecimal.TEN));
		assertThat(finanzielleSituation.getErsatzeinkommenT(), is(BigDecimal.TEN));
		assertThat(finanzielleSituation.isErsatzeinkommenBezogen(), is(true));
	}

	@Test()
	public void setValuesToFinSitErsatzEinkommenZero() {
		response.setSteuerpflichtigesErsatzeinkommenDossiertraeger(BigDecimal.ZERO);
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		KibonAnfrageHelper.setValuesToFinSit(finanzielleSituation, response, BigDecimal.ONE, SteuerdatenDatenTraeger.DOSSIERTRAEGER);
		assertThat(finanzielleSituation.getErsatzeinkommen(), is(BigDecimal.ZERO));
		assertThat(finanzielleSituation.getErsatzeinkommenT(), is(BigDecimal.ZERO));
		assertThat(finanzielleSituation.isErsatzeinkommenBezogen(), is(true));
	}

	@Test()
	public void setValuesToFinSitErsatzEinkommenNegativ() {
		response.setSteuerpflichtigesErsatzeinkommenDossiertraeger(BigDecimal.TEN.negate());
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		KibonAnfrageHelper.setValuesToFinSit(finanzielleSituation, response, BigDecimal.ONE, SteuerdatenDatenTraeger.DOSSIERTRAEGER);
		assertThat(finanzielleSituation.getErsatzeinkommen(), is(BigDecimal.ZERO));
		assertThat(finanzielleSituation.getErsatzeinkommenT(), is(BigDecimal.ZERO));
		assertThat(finanzielleSituation.isErsatzeinkommenBezogen(), is(true));
	}

	@Test()
	public void setValuesToFinSitErsatzEinkommenNull() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		KibonAnfrageHelper.setValuesToFinSit(finanzielleSituation, response, BigDecimal.ONE, SteuerdatenDatenTraeger.DOSSIERTRAEGER);
		assertThat(finanzielleSituation.getErsatzeinkommen(), nullValue());
		assertThat(finanzielleSituation.getErsatzeinkommenT(), nullValue());
		assertThat(finanzielleSituation.isErsatzeinkommenBezogen(), is(false));
	}

	@Test()
	public void setValuesToFinSitAusgewiesenerGeschaeftsertrag() {
		response.setAusgewiesenerGeschaeftsertragDossiertraeger(BigDecimal.TEN);
		response.setAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger(BigDecimal.ONE);
		response.setAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger(BigDecimal.ZERO);
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		KibonAnfrageHelper.setValuesToFinSit(finanzielleSituation, response, BigDecimal.ONE, SteuerdatenDatenTraeger.DOSSIERTRAEGER);
		assertThat(finanzielleSituation.getGeschaeftsgewinnBasisjahr(), is(BigDecimal.TEN));
		assertThat(finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus1(), is(BigDecimal.ONE));
		assertThat(finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus2(), is(BigDecimal.ZERO));

		response.setAusgewiesenerGeschaeftsertragDossiertraeger(null);
		KibonAnfrageHelper.setValuesToFinSit(finanzielleSituation, response, BigDecimal.ONE, SteuerdatenDatenTraeger.DOSSIERTRAEGER);
		assertThat(finanzielleSituation.getGeschaeftsgewinnBasisjahr(), nullValue());
		assertThat(finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus1(), nullValue());
		assertThat(finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus2(), nullValue());
	}
}
