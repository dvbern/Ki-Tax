# Installation E-BEGU GUI

## nvm installieren
NVM dient der Verwaltung der benutzten node (npm) Version
 nvm installieren gemäss Wik. Geht aber für Linus zum Beispiel mit
- curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.31.0/install.sh | bash

## Node installieren
ebegu benoetigt ca Version 4.4 von Node. Kann mit nvm install v4.4.3 installiert werden
 - nvm install v4.4.3
 - sicherstellen das npm -v grösser als 3.8.1 ist
 
 
## Projektdependencies installieren (einmalig, bzw. nach Änderung)
- hefr@PDVB:~/workspaces/ebegu/ebegu-web$ npm install

## Starten
- hefr@PDVB:~/workspaces/ebegu/ebegu-web$ npm run hotstart


## Formularstruktur
### Inputelement-Gruppe
Wir verwenden Bootstrap zum layouten der Seite und angular-material input komponenten. Es ist darauf zu achten, dass 
Formularelemente (Typischerweise label element, input element, error message elment) in einem parent element mit der 
Klasse *'form-group'* zusammengefasst werden (meistens entspricht dieser parent bei uns einer bootstrap-column.

1. Label  
 Wir verwenden wann immer moeglich <label> tags mit einem 'for' attribut fuer die Bezeichnung. Es ist keine spezielle styleclass erforderlich 
2. Inputelemente  
 Dem Element der Gruppe welches als Input dient sollte die Klasse *'input-element'* oder *'form-control'* gegeben werden damit es identifiziert werden kann

3. Fehlerausgabe  
 Containern in denen  Fehler angezeigt werden soll muss die style-class *'error'* gegeben werden.

Durch das vergeben der richtigen Styleclasses wird sichergestellt, dass die Funktion der Elemente anhand der Styleclass
ermittelt werden kann. Dadurch kann zum Beispiel das Errorstyling leicht veraendert werden