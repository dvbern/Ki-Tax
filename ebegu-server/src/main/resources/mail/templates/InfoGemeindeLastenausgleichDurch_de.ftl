<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="jahr" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon - Lastenausgleich verbucht</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>Lastenausgleich ${jahr} verbucht</title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
		Der Lastenausgleich des Jahres ${jahr} wurde verbucht. Die Berechnungsresultate sind <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/lastenausgleich">hier</a> ersichtlich
	</p>
	<p>
		Freundliche Grüsse<br/>
		Ihr Kanton Bern
	</p>
	<p>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

</body>

</html>