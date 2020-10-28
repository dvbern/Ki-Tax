<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: <@base64Header>${gesuch.dossier.verantwortlicherTS.fullName}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>kiBon - Betreuungsgutschein wurde verfügt</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon - Betreuungsgutschein wurde verfügt</title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
		Der Antrag mit der Fallnummer ${gesuch.dossier.fall.fallNummer} wurde verfügt. Es können nun auch die
		Tagesschulanmeldungen abgeschlossen werden.
	</p>
	<p>
		Freundliche Grüsse<br/>
		kiBon
	</p>
	<p>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

</body>

</html>
