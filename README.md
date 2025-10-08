
# YoloFly 🦋

Android App zur Unterstützung der Datensammlung von Schmetterlingssichtungen für das Helmholtz Institut.

## TODO

### Anton

- Kamerabutton switch DONE
- Kamera Button soll automatisierte Bildaufnahme beenden DONE
- Camera Button sollte als "Home Button" fungieren wenn auf anderen Screens
  als dem Home/Camera Screen DONE
- wenn auf Home/Camera Screen dann sollte er die automatisierte Bildaufnahme starten und beenden DONE
- Topbarfix DONE (hacked)
- Clear Button in Photoselection (löscht aktuell aus dem screen (aus dem RAM auch?))


### Manon Richnii--

- Tutorial
  - Start bei erstmaligem Öffnen der App //TODO
  - Extra Screen, der auch erneut aufrufbar ist (DONE)
  - einfacher Text zur Erklärung (DONE)
  - Erweitern für Speciescatalog

### Pascal

- Persistente Favoriten DONE
- Fix der Buttons (Burgermenu hat zu viele Optionen)
- Homescreen Name change DONE
- Disablen der Zählfunktion/Des Toggles im Homescreen DONE
- ReadMe aufräumen DONE
- Inklusionsfeatures DONE
  - Vorlesefunktion Tutorial DONE
  - Farbenblindmodus DONE

#### Maybe
- Nur Schmetterlingsbilder anzeigen
- Clear Button in Photoselection (löscht aktuell aus dem screen (aus dem RAM auch?))
- pt-Modell ausprobierem (statt onnx)
- echte fotospeicherung
- abfrage ob fotos an das backend geschickt werden sollen
- check, welche Android version benötigt wird
- multilingual
- bildqualität


### Alle
- Wirtschaftlichkeitsanalyse
- Präsentation

## Features

- automated capturing of pictures (every 0.5 seconds)
- automated sending of pictures to a selection screen
  - selection of pictures you want to send to the image classification AI
- speciescatalog with 500 species
  - user can get information about every species
  - user can see which species they already photographed
  - user can select their favorite species

## Branches

### Repository Management

- **main**
  - Productionbranch

### Development Branch

- **develop**
  - every feature branch should originate from develop

### Basisstruktur

- **feature/base-structure**
  - the base structure of the app
  - no real functionality
  - starting point of development

### Prototyp/MVP

- **prototype**
  - MVP for marketplace presentation
  - implemented core functionalities
    - automated picture taking
    - photoselection
    - "pokedex"

## Logging von Ergebnissen

- **tag:HomeFragment level:debug tag:HomeFragment level:debug**
  - zeigt ob Butterfly detected wurde und mit welcher Wahrscheinlichkeit und History