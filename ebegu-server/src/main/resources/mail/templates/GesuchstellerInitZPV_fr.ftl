<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="id" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon - Lastenausgleich Tagesschule</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon - Lastenausgleich Tagesschule</title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
		Sie haben die Verknüpfung ihres kiBon Antrags mit einem zweiten BE-Login beantragt. Dafür befolgen Sie bitte folgende Schritt:


	</p>
	<ul>
		<li>Stellen Sie sicher, dass sie weder bei kiBon noch beim BE-Login eingeloggt sind</li>
		<li>Folgen Sie<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuchsteller/${id}/init-zpv">diesem Link</a></li>
	</ul>
	<p>
		Nachdem Sie sich beim BE-Login eingeloggt haben, werden Sie zu kiBon zurückgeleitet, wo Ihr Antrag mit dem BE-Login verknüpft wird.
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