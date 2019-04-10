<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${gesuchsteller.fullName} <${empfaengerMail}>
Subject: <@base64Header>FR_kiBon – Gesuch kann freigegeben werden</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>FR_kiBon – Gesuch kann freigegeben werden</title>

</head>

<body>

<div>
	<p>
		FR_Sehr geehrte Familie
	</p>
	<p>
        FR_Sämtliche Betreuungsangebote wurden bestätigt. Das kiBon-Gesuch kann
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuch/freigabe/${gesuch.id}">hier</a>
		freigegeben werden.
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
