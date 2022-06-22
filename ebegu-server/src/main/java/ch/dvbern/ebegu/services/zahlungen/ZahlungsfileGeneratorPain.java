package ch.dvbern.ebegu.services.zahlungen;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelper;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelperFactory;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import ch.dvbern.oss.lib.iso20022.dtos.pain.AuszahlungDTO;
import ch.dvbern.oss.lib.iso20022.dtos.pain.Pain001DTO;
import ch.dvbern.oss.lib.iso20022.pain001.v00103ch02.Pain001Service;

@Dependent
public class ZahlungsfileGeneratorPain implements IZahlungsfileGenerator {

	@Inject
	private Pain001Service pain001Service;

	@Override
		public byte[] generateZahlungfile(
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Locale locale
	) {
		return pain001Service.getPainFileContent(wrapZahlungsauftrag(zahlungsauftrag, stammdaten, locale));
	}

	private Pain001DTO wrapZahlungsauftrag(
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull GemeindeStammdaten gemeindeStammdaten,
		@Nonnull Locale locale
	) {
		Pain001DTO pain001DTO = new Pain001DTO();

		// Wenn die Zahlungsinformationen nicht komplett ausgefuellt sind, fahren wir hier nicht weiter.
		if (!gemeindeStammdaten.isZahlungsinformationValid()) {
			throw new EbeguRuntimeException(
				KibonLogLevel.INFO,
				"wrapZahlungsauftrag",
				ErrorCodeEnum.ERROR_ZAHLUNGSINFORMATIONEN_GEMEINDE_INCOMPLETE,
				zahlungsauftrag.getGemeinde().getName());
		}

		pain001DTO.setAuszahlungsDatum(zahlungsauftrag.getDatumFaellig());
		pain001DTO.setGenerierungsDatum(zahlungsauftrag.getDatumGeneriert());

		String debitorName = gemeindeStammdaten.getKontoinhaber();
		String debitorBic = gemeindeStammdaten.getBic();
		IBAN ibanGemeinde = gemeindeStammdaten.getIban();
		Objects.requireNonNull(ibanGemeinde, "Keine IBAN fuer Gemeinde " + gemeindeStammdaten.getGemeinde().getName());
		String debitorIban = ibanToUnformattedString(ibanGemeinde);

		pain001DTO.setSchuldnerName(debitorName);
		pain001DTO.setSchuldnerIBAN(debitorIban);
		pain001DTO.setSchuldnerBIC(debitorBic);
		// Wir setzen explizit keine SchuldnerIBAN, da dieses Feld zwar optional ist, aber bei einigen Banken Probleme macht
		pain001DTO.setSchuldnerIBANGebuehren(null);
		pain001DTO.setSoftwareName("kiBon");
		// we use the currentTimeMillis so that it is always different
		//noinspection StringConcatenationMissingWhitespace
		pain001DTO.setMsgId("kiBon" + System.currentTimeMillis());

		pain001DTO.setAuszahlungen(new ArrayList<>());

		final ZahlungslaufHelper zahlungslaufHelper = ZahlungslaufHelperFactory.getZahlungslaufHelper(zahlungsauftrag.getZahlungslaufTyp());
		zahlungsauftrag.getZahlungen().stream()
			.filter(zahlung -> zahlung.getBetragTotalZahlung().signum() == 1)
			.forEach(zahlung -> {
				// Wenn die Zahlungsinformationen nicht komplett ausgefuellt sind, fahren wir hier nicht weiter.
				if (!zahlung.getAuszahlungsdaten().isZahlungsinformationValid()) {
					final Institution institution = zahlung.extractInstitution();
					throw new EbeguRuntimeException(KibonLogLevel.INFO,
						"wrapZahlungsauftrag",
						ErrorCodeEnum.ERROR_ZAHLUNGSINFORMATIONEN_INSTITUTION_INCOMPLETE,
						institution.getName());
				}

				AuszahlungDTO auszahlungDTO = new AuszahlungDTO();
				auszahlungDTO.setBetragTotalZahlung(zahlung.getBetragTotalZahlung());

				final Auszahlungsdaten auszahlungsdaten = zahlung.getAuszahlungsdaten();
				auszahlungDTO.setZahlungsempfaegerName(auszahlungsdaten.getKontoinhaber());

				IBAN ibanInstitution = auszahlungsdaten.getIban();
				Objects.requireNonNull(ibanInstitution, "Keine IBAN fuer Empfaenger " + zahlung.getEmpfaengerName());
				auszahlungDTO.setZahlungsempfaegerIBAN(ibanToUnformattedString(ibanInstitution));
				auszahlungDTO.setZahlungsempfaegerBankClearingNumber(ibanInstitution.extractClearingNumberWithoutLeadingZeros());

				Adresse adresseKontoinhaber = zahlungslaufHelper.getAuszahlungsadresseOrDefaultadresse(zahlung);
				Objects.requireNonNull(adresseKontoinhaber);
				auszahlungDTO.setZahlungsempfaegerStrasse(adresseKontoinhaber.getStrasse());
				auszahlungDTO.setZahlungsempfaegerHausnummer(adresseKontoinhaber.getHausnummer());
				auszahlungDTO.setZahlungsempfaegerPlz(adresseKontoinhaber.getPlz());
				auszahlungDTO.setZahlungsempfaegerOrt(adresseKontoinhaber.getOrt());
				auszahlungDTO.setZahlungsempfaegerLand(adresseKontoinhaber.getLand().toString());

				String monat = zahlungsauftrag.getDatumFaellig().format(DateTimeFormatter.ofPattern("MMM yyyy", locale));
				String msgKey = "ZahlungstextPainFile_" + zahlungsauftrag.getZahlungslaufTyp();
				String zahlungstext = ServerMessageUtil.getMessage(
					msgKey,
					locale,
					Objects.requireNonNull(gemeindeStammdaten.getGemeinde().getMandant()),
					gemeindeStammdaten.getGemeinde().getName(),
					zahlung.getEmpfaengerName(),
					monat);
				auszahlungDTO.setZahlungText(zahlungstext);

				// Wenn Empfänger und Auszahler dasselbe Konto sind, soll es nicht ins PAIN File. Dies ist z.B. Gemeinde-Kitas der Fall.
				if (!debitorIban.equals(auszahlungDTO.getZahlungsempfaegerIBAN())) {
					pain001DTO.getAuszahlungen().add(auszahlungDTO);
				}
			});

		return pain001DTO;
	}

	@Nonnull
	protected String ibanToUnformattedString(@Nonnull IBAN iban) {
		Objects.requireNonNull(iban);
		return EbeguUtil.removeWhiteSpaces(iban.getIban());
	}
}
