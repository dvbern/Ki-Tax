<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="anzahlTage" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
From: ${configuration.senderAddress}
To: " ${gesuchsteller.fullName} <${gesuchsteller.mail}>
Subject: <@base64Header>FR_kiBon – Gesuch nicht abgeschlossen</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>FR_kiBon – Gesuch nicht abgeschlossen</title>

</head>

<body>

<div>
	<p>
		FR_Guten Tag
	</p>
	<p>
        FR_Sie haben sich auf <a href="www.bern.ch/ki-tax">www.bern.ch/ki-tax</a> registriert. Sie haben Ihre Daten noch
		nicht freigegeben.
	</p>
	<p>
        FR_Mit dieser Mail möchten wir Sie daran erinnern, Ihren Antrag rechtzeitig abzuschliessen. Die Freigabequittung muss
		vor Beginn der Betreuung Ihres Kindes bei uns eingereicht werden, damit Sie Ihren Anspruch nicht verlieren. Ohne
		eine Freigabe innert ${anzahlTage} Tagen erfolgt eine automatische Löschung.
	</p>
	<p>
        FR_Freundliche Grüsse <br/>
        FR_Ihre Gemeinde ${gesuch.dossier.gemeinde.name}
	</p>
	<p>
        FR_Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

</body>

</html>
