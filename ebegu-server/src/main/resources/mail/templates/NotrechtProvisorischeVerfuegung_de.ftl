<#-- @ftlvariable name="rueckforderungFormular" type="ch.dvbern.ebegu.entities.RueckforderungFormular" -->
<#-- @ftlvariable name="institutionStammdaten" type="ch.dvbern.ebegu.entities.InstitutionStammdaten" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaenger" type="java.lang.String" -->
<#-- @ftlvariable name="hostname" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaenger}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung
	für Kitas und TFO: Provisorische Verfügung / Coronavirus et accueil extrafamilial : décision provisoire</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Verfügung / décision</title>

</head>

<body>
${institutionStammdaten.mail}<br>
${institutionStammdaten.institution.name}<br>
<p></p>
<div>
	<p>
		<b>
			Ausfallentschädigung im Bereich familienergänzende Kinderbetreuung für entgangene Betreuungsbeiträge
			infolge der Massnahmen zur Bekämpfung des Coronavirus (Covid-19)
		</b>
	</p>
	<p>
		Sehr geehrte Dame, sehr geehrter Herr
	</p>
	<p>
		Wir haben Ihr Gesuch um eine Ausfallentschädigung für entgangene Betreuungsbeiträge infolge der Massnahmen zur
		Bekämpfung des Coronavirus geprüft. Ihre Verfügung finden Sie unter <a href="<#if configuration
		.clientUsingHTTPS>https://<#else>http://</#if>${hostname}/corona-finanzierung/list/rueckforderung/${rueckforderungFormular.id}/verfuegungen">diesem Link</a>.
	</p>
	<p>
		Bitte beachten Sie, dass die Ausfallentschädigung unter Vorbehalt einer späteren Korrektur verfügt wurde.
		Eine Verfügung unter Vorbehalt wurde erlassen, weil a) die Prüfung des Gesuchs zum jetzi-gen Zeitpunkt nicht
		abgeschlossen ist, oder weil b) Sie die definitiven Abrechnungen über die Kurzar-beitsentschädigung und/oder
		die Covid-Erwerbsausfallentschädigung noch erwarten.
	</p>
	<p>
		Falls b) zutrifft, bitten wir Sie, die Abrechnungen nach Erhalt umgehend nachzureichen via Upload auf kiBon.
		Falls a) zutrifft werden Sie wieder von uns hören, Sie müssen nichts unternehmen.
	</p>
	<p>
		Freundliche Grüsse
	</p>
	<p>
		Abteilung Familie Kanton Bern</p>
	<p>
		Amt für Integration und Soziales<br>
		<a href="mailto:info.bg@be.ch">info.bg@be.ch</a><br>
		+41 31 633 78 83
	</p>
    <#if configuration.isDevmode>
		<p>
			<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen
				verwendet. Über dieses System abgehandelte Gesuche verfügen über keine Zahlungsberechtigung!</b><br><br>
		</p>
    </#if>
</div>
</body>
</html>
