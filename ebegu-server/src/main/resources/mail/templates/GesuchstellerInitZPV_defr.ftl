<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="id" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon - Verküpfung ihres Antrags mit weiterem BE-Login - lier votre demande avec un autre BE-Login</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon - Verküpfung ihres Antrags mit weiterem BE-Login - lier votre demande avec un autre BE-Login</title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
		Sie haben die Verknüpfung Ihres kiBon-Antrags mit einem zweiten BE-Login beantragt, um die Steuerdaten abzurufen.
	</p>
	<p>
		<b>Achtung:</b> Stellen Sie bitte sicher, dass Sie im BE-Login nicht mehr eingeloggt sind.
	</p>
	<p>
		Über die untenstehende Schaltfläche erlauben Sie den Abruf Ihrer Steuerdaten:<br>
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${hostname}${link}">BE Login verknüpfen und Abfrage der Steuerdaten erlauben.</a></li>
	</p>
	<p>
		Nachdem Sie sich beim BE-Login eingeloggt haben, werden Sie auf kiBon zurückgeleitet, wo Ihr Antrag mit dem BE-Login verknüpft wird.
	</p>

	<p>
		Freundliche Grüsse<br/>
		kiBon - Team
	</p>
	<p>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

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