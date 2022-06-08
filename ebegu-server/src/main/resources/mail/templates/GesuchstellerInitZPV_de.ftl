<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="id" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon - Verkuepfung ihres Antrags mit weiterem BE-Login</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon - Verküpfung ihres Antrags mit weiterem BE-Login</title>

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

</body>

</html>