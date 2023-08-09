package ch.dvbern.ebegu.nesko.handler;

import ch.dvbern.ebegu.dto.neskovanp.Veranlagungsstand;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.GesuchstellerTyp;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.test.GesuchBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

}
