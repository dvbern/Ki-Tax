<#-- @ftlvariable name="institutionStammdaten" type="ch.dvbern.ebegu.entities.InstitutionStammdaten" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="betrag1" type="java.lang.String" -->
<#-- @ftlvariable name="betrag2" type="java.lang.String" -->
<#-- @ftlvariable name="frenchEnabled" type="java.lang.Boolean" -->
<#-- @ftlvariable name="hostname" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${institutionStammdaten.mail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung
	für Kitas und TFO: Zahlung freigegeben / Coronavirus et accueil extrafamilial : versement libéré</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung für Kitas und
		TFO: Zahlung freigegeben / Coronavirus et accueil extrafamilial : versement libéré</title>

</head>

<body>

<div>
	<p>
		Sehr geehrte Dame, sehr geehrter Herr
	</p>
	<p>
		Sie werden in den nächsten Tagen die folgende Zahlung erhalten:
	</p>
	<p>
		Für Stunden/ Plätze, welche nicht mehr angeboten wurden: CHF ${betrag1} <br>
		Für Elterngebühren für die Betreuung für coronabedingte Abwesenheiten: CHF ${betrag2}
	</p>
	<p>
	<p>
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${hostname}/corona-finanzierung/list">Hier</a>
		können Sie sehen, ob im Vergleich zu Ihrer Eingabe Änderungen vorgenommen wurden. Die definitive
		Abrechnung mit Begründung von Abweichungen zur Eingabe erfolgt nach der detaillierten Prüfung Ihres Gesuchs in
		der Stufe 2.
	</p>
	<p>
		<b>
			Bitte beachten Sie: Die zweite Stufe müssen Sie bis spätestens 17. Juli (privat betriebene Institutionen)
			bzw. 31. Juli 2020 (von der öffentlichen Hand betriebene Institutionen) freigeben.
		</b>
	</p>
	<p>
		Bei Rückfragen steht Ihnen die Abteilung Familie des Amtes für Integration und Soziales info.bg@be.ch zur
		Verfügung.
	</p>
	Mit freundlichen Grüssen
	</p>
	<p>
		Abteilung Familie Kanton Bern</p>
	<p>
		Amt für Integration und Soziales
		info.bg@be.ch
		+41 31 633 78 83
	</p>
    <#if configuration.isDevmode>
		<p>
			<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen
				verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
		</p>
    </#if>
</div>

</body>

</html>
