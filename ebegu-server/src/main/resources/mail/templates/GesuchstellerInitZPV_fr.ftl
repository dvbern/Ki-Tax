<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="id" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon - lier votre demande avec un autre BE-Login</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon - lier votre demande avec un autre BE-Login</title>

</head>

<body>

<div>
	<p>
		Bonjour,
	</p>
	<p>
		Vous souhaitez relier votre demande kiBon à un deuxième compte BE-Login pour pouvoir récupérer les données fiscales.
	</p>
	<p>
		<b>Attention:</b> assurez-vous que vous êtes déconnecté·e de BE-Login.
	</p>
	<p>
		Le bouton ci-dessous vous permet de récupérer vos données fiscales:<br>
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${hostname}${link}">Connecter à BE Login et autoriser la récupération des données fiscales.</a></li>
	</p>
	<p>
		Une fois connecté·e dans BE-Login, vous serez redirigé·e vers kiBon et votre demande sera reliée à votre compte BE-Login.
	</p>

	<p>
		Avec nos salutations les meilleures,<br/>
		L’équipe kiBon
	</p>
	<p>
		Merci de ne pas répondre à ce message automatique.
	</p>
</div>

</body>

</html>