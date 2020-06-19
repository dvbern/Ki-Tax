<#-- @ftlvariable name="institutionStammdaten" type="ch.dvbern.ebegu.entities.InstitutionStammdaten" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="betrag1" type="java.lang.String" -->
<#-- @ftlvariable name="betrag2" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${institutionStammdaten.mail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung
	für Kitas und TFO: Zahlung freigegeben</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung für Kitas und
		TFO: Zahlung
		freigegeben / Corona - financement pour les crèches et les parents de jour: Versement libéré </title>

</head>

<body>

<div>
	<p>
		Sehr geehrte Dame, sehr geehrter Herr
	</p>
	<p>
		Sie werden in den nächsten Tagen die folgende Zahlung erhalten:
	</p>
	<p>
		Für Stunden/ Plätze, welche nicht mehr angeboten wurden: CHF ${betrag1} <br>
		Für Elterngebühren für die Betreuung für coronabedingte Abwesenheiten: CHF ${betrag2}
	</p>
	<p>
	<p>
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/corona-finanzierung/list">Hier</a>
		können Sie sehen, ob im Vergleich zu Ihrer Eingabe Änderungen vorgenommen wurden. Die definitive
		Abrechnung mit Begründung von Abweichungen zur Eingabe erfolgt nach der detaillierten Prüfung Ihres Gesuchs in
		der Stufe 2.
	</p>
	<p>
		<b>
			Bitte beachten Sie: Die zweite Stufe müssen Sie ebenfalls bis spätestens 31. Juli 2020 freigeben. Ansonsten
			verwirkt Ihr Anspruch auf Leistungen und der Kanton wird die geleistete Zahlung zurückfordern.
		</b>
	</p>
	<p>
		Für die definitive Abrechnung müssen Sie die folgenden Dokumente hochladen (Sie werden eine Mail erhalten, sobald dies möglich ist):
	</p>
	<ul>
		<li>Entweder das ausgefüllte Excel-Formular oder sonstige Dokumente, aus denen die im Excel-Formular enthaltenen
			Angaben ersichtlich sind.
		</li>
		<li>Die Kommunikation mit den Eltern (alle Rundschreiben, Rundmails, interne Sprachregelungen) bezüglich der
			Betreuung zwischen dem 17. März und 16. Mai 2020.
		</li>
		<li>Einsatzpläne für die Zeit vom 17. März bis 16. Mai, aus denen ersichtlich ist, welches Personal
			einsatzbereit war.
		</li>
	</ul>
	<p>
		Stichprobenartig können auch die Elternverträge und Rechnungen für die betroffene Periode angefordert werden.
		Eltern können kontaktiert werden um die Angaben zur Kommunikation zu plausibilisieren.
	</p>
	<p>
		Sie können zudem noch Angaben, die Sie während der ersten Stufe gemacht haben, korrigieren.
	</p>
	<p>
		Nach der Freigabe der Stufe 2 findet die detaillierte Prüfung der Angaben statt. Das Resultat der Prüfung ist
		eine anfechtbare Verfügung, welche die definitiven Beträge festlegt. Sie enthält auch Angaben darüber, ob der
		Kanton allenfalls Rückforderungen stellt oder eine Nachzahlung tätigt muss.
	</p>
	<p>
		Bei Rückfragen steht Ihnen die Abteilung Familie des Amtes für Integration und Soziales info.fam@be.ch zur
		Verfügung.
	</p>
	Mit freundlichen Grüssen
	</p>
	<p>
		Abteilung Familie Kanton Bern</p>
	<p>
		Amt für Integration und Soziales
		info.fam@be.ch
		031 633 78 83
	</p>
    <#if configuration.isDevmode>
		<p>
			<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen
				verwendet. Über dieses System abgehandelte Gesuche verfügen über keine Zahlungsberechtigung!</b><br><br>
		</p>
    </#if>
</div>

<div>
	<p>
		Mesdames, Messieurs,
	</p>
	<p>
		Le versement suivant sera effectué au cours des prochains jours :
	</p>
	<p>
    	${betrag1} francs pour les heures ou les places qui n’ont plus pu être proposées.<br>
    	${betrag2} francs de contributions parentales pour les frais de prise en charge des enfants absents en raison du coronavirus.
	</p>
	<p>
		Vous pouvez contrôler <a
				href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/corona-finanzierung/list">ici</a>
		si les données saisies ont été modifiées. Le décompte définitif avec justification des
		écarts sera établi après examen détaillé de votre demande dans le cadre de la seconde étape.
	</p>
	<p>
		<b>
			Remarque : vous devez également nous transmettre les documents requis pour la seconde étape d’ici le 31
			juillet 2020 au plus tard, faute de quoi vous perdrez votre droit aux prestations et devrez rembourser le
			montant au canton.
		</b>
	</p>
	<p>
		Pour le décompte définitif, il nous faut les documents suivants (un courriel vous sera envoyé dès que les documents pourront être chargés dans kiBon) :
	</p>
	<ul>
		<li>soit le formulaire Excel dûment rempli, soit d’autres documents fournissant toutes les informations du
			fichier Excel,
		</li>
		<li>les communications avec les parents au sujet de la prise en charge pendant la période du 17 mars au 16 mai
			2020 (ensemble des circulaires, courriels, lignes directrices internes),
		</li>
		<li>les plans de service pour la période du 17 mars au 16 mai 2020.
		</li>
	</ul>
	<p>
		Il est également possible que nous vous demandions aléatoirement de remettre notamment les contrats conclus avec
		les parents ainsi que les factures concernant la période en question. Nous nous réservons le droit de prendre
		contact avec des parents pour contrôler les informations qui leur ont été communiquées.
	</p>
	<p>
		Vous avez encore la possibilité de corriger les données saisies lors de la première étape.
	</p>
	<p>
		Les indications fournies seront contrôlées dans le détail. Le résultat de l’examen et les montants définitifs
		vous seront communiqués sous la forme d’une décision susceptible de recours. Cette dernière précisera également,
		le cas échéant, si un remboursement est exigé de votre part ou si vous avez droit à un montant supplémentaire du
		canton.
	</p>
	<p>
		En restant à votre disposition en cas de question (info.fam@be.ch ; 031 633 78 83), nous vous présentons,
		Mesdames, Messieurs, nos salutations les meilleures.
	</p>
	<p>
		La division Famille de l’Office de l’intégration et de l’action sociale
	</p>
    <#if configuration.isDevmode>
		<p>
			<b>Le présent message est envoyé par un système test utilisé pour les tutoriels. Les demandes via ce système
				ne donnent pas droit à un versement.</b><br><br>
		</p>
    </#if>
</div>

</body>

</html>
