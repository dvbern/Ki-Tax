<#-- @ftlvariable name="rueckforderungFormular" type="ch.dvbern.ebegu.entities.RueckforderungFormular" -->
<#-- @ftlvariable name="institutionStammdaten" type="ch.dvbern.ebegu.entities.InstitutionStammdaten" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${institutionStammdaten.mail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung
	für Kitas und TFO: Provisorische Verfügung / Coronavirus et accueil extrafamilial : décision provisoire</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung für Kitas und
		TFO: Provisorische Verfügung / Coronavirus et accueil extrafamilial : décision provisoire</title>

</head>

<body>

<div>
	<p>
		Sehr geehrte Dame, sehr geehrter Herr
	</p>
	<p>
		Die Prüfung und Berechnung Ihres Gesuchs zur Corona-Finanzierung wurde provisorisch verfügt.
		Sie können die Ergebnisse <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/corona-finanzierung/list/rueckforderung/${rueckforderungFormular.id}">Hier</a>
		einsehen.
	</p>
	<p>
		Mit freundlichen Grüssen
	</p>
	<p>
		Abteilung Familie Kanton Bern</p>
	<p>
		Amt für Integration und Soziales
		info.fam@be.ch
		031 633 78 91
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