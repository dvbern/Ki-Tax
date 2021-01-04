<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="jahr" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon - compensation des charges comptabilisée</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>compensation des charges pour l'année ${jahr} comptabilisée</title>

</head>

<body>

<div>
	<p>
		Bonjour,
	</p>
	<p>
		La compensation des charges pour l'année ${jahr} a été comptabilisée. Les résultats sont disponibles <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/lastenausgleich">ici</a>
	</p>
	<p>
		Nous vous présentons nos meilleures salutations.<br/>
		Votre canton
	</p>
	<p>
		Merci de ne pas répondre à ce message automatique.
	</p>
</div>

</body>

</html>