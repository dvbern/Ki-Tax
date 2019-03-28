<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="downloadurl" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="footer" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>FR_kiBon – Statistik erstellt</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>FR_kiBon – Statistik erstellt</title>

</head>

<body>

<div>

	<p>
		FR_Guten Tag
	</p>
	<p>
		FR_Ihre Statistik wurde generiert und kann <a href="${downloadurl}">hier</a> heruntergeladen werden.
	</p>
	<p>
		${footer}
	</p>

</div>

</body>

</html>
