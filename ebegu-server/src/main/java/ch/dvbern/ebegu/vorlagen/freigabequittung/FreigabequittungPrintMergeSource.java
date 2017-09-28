package ch.dvbern.ebegu.vorlagen.freigabequittung;

import java.io.IOException;
import java.util.List;

import ch.dvbern.ebegu.vorlagen.EBEGUMergeSource;
import ch.dvbern.lib.doctemplate.common.BeanMergeSource;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.lib.doctemplate.common.MergeSource;

/**
 * Copyright (c) 2016 DV Bern AG, Switzerland
 * <p>
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschuetzt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
 * insbesondere fuer Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht uebergeben ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 * <p>
 * Created by medu on 28/11/2016.
 */
public class FreigabequittungPrintMergeSource implements EBEGUMergeSource {

	private boolean isPDFLongerThanExpected = false;
	private final FreigabequittungPrint quittung;

	public FreigabequittungPrintMergeSource(FreigabequittungPrint quittung) {
		this.quittung = quittung;
	}

	@Override
	public Object getData(MergeContext mergeContext, String key) throws DocTemplateException {

		if (key.startsWith("barcodeImage")) {
			try {
				return quittung.getBarcodeImage();
			} catch (IOException e) {
				throw new DocTemplateException("Fehler beim Strichcode generieren für Freigabequittung", e);
			}
		}

		if (key.startsWith("printMerge")) {
			return new BeanMergeSource(quittung, "printMerge.").getData(mergeContext, key);
		}

		return null;
	}

	@Override
	public Boolean ifStatement(MergeContext mergeContext, String key) throws DocTemplateException {
		if (key.equals("printMerge.PDFLongerThanExpected")) {
			return isPDFLongerThanExpected;
		}
		return new BeanMergeSource(quittung, "printMerge.").ifStatement(mergeContext, key);
	}

	@Override
	public List<MergeSource> whileStatement(MergeContext mergeContext, String key) throws DocTemplateException {

		String[] array = key.split("[.]+");
		String subkey = array[0];

		if (subkey.equalsIgnoreCase("printMerge")) {
			return new BeanMergeSource(quittung, "printMerge.").whileStatement(mergeContext, key);
		}

		if (subkey.equalsIgnoreCase("betreuungsTabelle")) {
			return new BeanMergeSource(quittung, "betreuungsTabelle.").whileStatement(mergeContext, key);
		}

		return null;

	}

	@Override
	public void setPDFLongerThanExpected(boolean isPDFLongerThanExpected) {
		this.isPDFLongerThanExpected = isPDFLongerThanExpected;
	}
}
