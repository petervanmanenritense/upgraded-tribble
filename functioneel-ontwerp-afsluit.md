# Functioneel Ontwerp - Tabblad Afsluit

## Beschrijving

Het tabblad Afsluit toont per maand hoeveel zaken er zijn afgesloten en met welke
afsluitreden, samen met het verloop van de gemiddelde doorlooptijd van de afgesloten zaken.

- **Afgesloten zaak**: een zaak die is beëindigd (de einddatum van de zaak). Elke afgesloten
  zaak heeft een afsluitreden op basis van het zaakresultaat.
- **Afsluitreden**: de reden waarmee de zaak is afgesloten (op basis van het zaakresultaat).
- **Doorlooptijd**: het aantal dagen tussen de startdatum en de afsluitdatum van de zaak.

Zaken worden toegewezen aan een maand op basis van hun **afsluitmaand** (einddatum).

## Filters

| Filter          | Omschrijving                              | Opties                                      |
|-----------------|-------------------------------------------|---------------------------------------------|
| Dienstverlening | Filtert op type dienstverlening           | Alle typen / Werk / Inburgering             |
| Jaar            | Filtert op afsluitjaar                     | Alle jaren / beschikbare jaren              |
| Team            | Filtert op team                           | Alle teams / beschikbare teams              |
| Afsluitreden    | Filtert op afsluitreden (zaakresultaat)   | Alle afsluitredenen / beschikbare redenen   |

Filters werken cascading: het selecteren van een filter beperkt de beschikbare opties in de
overige filters. Meerdere filters kunnen tegelijk actief zijn en worden gecombineerd
(AND-logica). Bij "Alle …" wordt niet op dat veld gefilterd. Het jaarfilter werkt op basis
van de afsluitmaand (einddatum) van de zaak.

## Samenvattingstegels

| Tegel                          | Omschrijving                                                             |
|--------------------------------|-------------------------------------------------------------------------|
| Totaal afgesloten zaken        | Aantal afgesloten zaken binnen de actieve selectie                      |
| Gemiddeld afgesloten per maand | Gemiddeld aantal afgesloten zaken per maand met afgesloten zaken        |
| Gemiddelde doorlooptijd        | Gemiddeld aantal dagen doorlooptijd van alle afgesloten zaken in de selectie |

## Grafiek

Een gecombineerde grafiek per maand:

- **Afgesloten zaken** als gestapelde staven op de linker y-as, uitgesplitst per afsluitreden.
  Elke afsluitreden heeft een eigen, consistente kleur; alleen in de selectie voorkomende
  redenen verschijnen in de grafiek en de legenda.
- **Gemiddelde doorlooptijd** (in dagen) als lijn op de rechter y-as, berekend over alle
  afgesloten zaken in die maand.

Omdat de doorlooptijd een andere grootheid en schaal is dan het aantal zaken, wordt deze op
een aparte tweede y-as getoond zodat alle reeksen goed leesbaar blijven. Maanden zonder
afgesloten zaken hebben geen lijnpunt.

Klik op een staafsegment of een punt op de lijn om de bijbehorende detailtabel te openen:

- Klik op een **staafsegment** → afgesloten zaken van díe afsluitreden in die maand.
- Klik op een **lijnpunt (doorlooptijd)** → alle afgesloten zaken van die maand.

## Detailtabel

De detailtabel verschijnt na klik op de grafiek en toont de individuele afgesloten zaken.

| Kolom               | Omschrijving                                       |
|---------------------|----------------------------------------------------|
| Startdatum          | Datum waarop de zaak is gestart                    |
| Afsluitdatum        | Datum waarop de zaak is afgesloten                 |
| Inwoner             | Volledige naam van de inwoner                      |
| Administratienummer | Identificatienummer van de inwoner                 |
| Coach               | Naam van de coach                                  |
| Team                | Teamnaam                                           |
| Dienstverlening     | Type dienstverlening                               |
| Afsluitreden        | Afsluitreden op basis van het zaakresultaat        |
| Doorlooptijd (dgn)  | Aantal dagen tussen start- en afsluitdatum         |

## Acceptatiecriteria

### Filters

- [ ] Alle vier filters (Dienstverlening, Jaar, Team, Afsluitreden) zijn zichtbaar en selecteerbaar
- [ ] Elk filter toont enkel waarden die voorkomen in de dataset
- [ ] Filters werken cascading: het selecteren van een filter beperkt de opties in de overige filters
- [ ] Het jaarfilter filtert op de afsluitmaand (einddatum) van de zaak
- [ ] Meerdere filters kunnen tegelijk actief zijn en worden gecombineerd (AND-logica)

### Samenvattingstegels

- [ ] Tegel "Totaal afgesloten zaken" toont het aantal afgesloten zaken in de actieve selectie
- [ ] Tegel "Gemiddeld afgesloten per maand" toont het gemiddelde aantal afgesloten zaken per maand met afgesloten zaken
- [ ] Tegel "Gemiddelde doorlooptijd" toont het gemiddelde aantal dagen doorlooptijd van alle afgesloten zaken in de selectie
- [ ] Tegels worden bijgewerkt bij elke filterwijziging

### Grafiek

- [ ] De grafiek toont per maand het aantal afgesloten zaken als gestapelde staven, uitgesplitst per afsluitreden
- [ ] Elke afsluitreden heeft een eigen, consistente kleur en verschijnt alleen wanneer aanwezig in de selectie
- [ ] De gemiddelde doorlooptijd wordt als lijn op een tweede (rechter) y-as weergegeven
- [ ] De lijnwaarde per maand is het gemiddelde aantal dagen doorlooptijd van alle afgesloten zaken in die maand
- [ ] Maanden zonder afgesloten zaken tonen geen lijnpunt
- [ ] Bij een jaarfilter worden alle 12 maanden van dat jaar getoond
- [ ] Zonder jaarfilter worden alle maanden van de eerste tot de laatste afsluitmaand getoond
- [ ] De grafiek wordt bijgewerkt bij elke filterwijziging

### Detailtabel

- [ ] De detailtabel is standaard verborgen
- [ ] Klik op een staafsegment toont de afgesloten zaken van die afsluitreden in die maand
- [ ] Klik op een lijnpunt toont alle afgesloten zaken van die maand
- [ ] De detailtabel toont alle kolommen: Startdatum, Afsluitdatum, Inwoner, Administratienummer, Coach, Team, Dienstverlening, Afsluitreden, Doorlooptijd
- [ ] De titel van de detailtabel bevat de maand en het aantal zaken
- [ ] Actieve filters worden ook toegepast op de detailtabel

### Navigatie

- [ ] Het dashboard is bereikbaar via een eigen tabblad "Afsluit"
- [ ] Het tabblad ondersteunt hash-navigatie (`#afsluit`)
- [ ] Er wordt een "Data laatst geactualiseerd"-tijdstempel getoond, consistent met de andere dashboards
