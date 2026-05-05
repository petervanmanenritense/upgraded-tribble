# Functioneel Ontwerp - Tabblad Aanbod

## Beschrijving

Het tabblad Aanbod toont een overzicht van alle aanbodactiviteiten. Records met afsluitreden 'Administratief afgesloten' of 'Aanbod afgesloten wegens wijzigen leerroute' worden uitgesloten van alle weergaven.

## Filters

| Filter              | Omschrijving                          | Opties                                           |
|---------------------|---------------------------------------|--------------------------------------------------|
| Type dienstverlening| Filtert op type dienstverlening       | Alle typen / Werk / Inburgering                  |
| Jaar                | Filtert op jaar van startdatum        | Alle jaren / beschikbare jaren                    |
| Team                | Filtert op team                       | Alle teams / beschikbare teams                    |
| Coach               | Filtert op coach                      | Alle coaches / beschikbare coaches                |
| Aanbodnaam          | Filtert op naam van het aanbod        | Alle aanbod / beschikbare aanbodnamen             |
| Afsluitreden        | Filtert op afsluitreden               | Alle afsluitredenen / beschikbare afsluitredenen  |

Filters werken cascading: het selecteren van een filter beperkt de beschikbare opties in de overige filters.

## Samenvattingstegels

| Tegel              | Omschrijving                                      |
|--------------------|---------------------------------------------------|
| Totaal             | Totaal aantal aanbodrecords (na filtering)         |
| Gem. per maand     | Gemiddeld aantal per maand                         |
| Top maand          | Maand met het hoogste aantal, inclusief aantal     |

## Grafiek

Stacked bar chart per maand, uitgesplitst op aanbodnaam. Klik op een staaf om de detailtabel te openen voor die maand en dat aanbod.

## Detailtabel

De detailtabel verschijnt na klik op een staaf in de grafiek en toont de individuele records.

| Kolom               | Omschrijving                                    | Datamapping |
|---------------------|-------------------------------------------------|-------------|
| Startdatum          | Datum waarop het aanbod is gestart              |             |
| Aanbodnaam          | Naam van het aanbod                             |             |
| Inwoner             | Volledige naam van de inwoner                   |             |
| Administratienummer | Identificatienummer van de inwoner              |             |
| Coach               | Naam van de coach bij start aanbod              |             |
| Team                | Teamnaam                                        |             |
| Afsluitreden        | Reden van afsluiting (leeg indien nog actief)   |             |
| Einddatum           | Datum waarop het aanbod is afgesloten           |             |

## Mogelijke waarden

### Aanbodnamen

- Taalcoaching
- Werkfit traject
- Participatieplaats
- Oriëntatie op werk
- Digitale vaardigheden
- Duale inburgering
- Entree opleiding
- Kort beroepsonderwijs
- Sociale activering
- Vrijwilligerswerk
- Jobcoaching
- Sollicitatietraining
- Budgetcoaching
- MAP-training
- Ondernemerschapstraject

### Afsluitredenen

- Doel aanbod behaald
- Doel aanbod niet behaald
- Doel aanbod deels behaald
- Administratief afgesloten (uitgesloten van weergave)
- Aanbod afgesloten wegens wijzigen leerroute (uitgesloten van weergave)

## Acceptatiecriteria

### Filters

- [ ] Alle zes filters (Type dienstverlening, Jaar, Team, Coach, Aanbodnaam, Afsluitreden) zijn zichtbaar en selecteerbaar
- [ ] Elk filter toont enkel waarden die voorkomen in de dataset
- [ ] Filters werken cascading: het selecteren van een filter beperkt de opties in de overige filters
- [ ] Bij het deselecteren van een filter (terug naar "Alle...") worden de overige filteropties opnieuw berekend
- [ ] Meerdere filters kunnen tegelijk actief zijn en worden gecombineerd (AND-logica)

### Exclusieregel

- [ ] Records met afsluitreden 'Administratief afgesloten' worden niet getoond in de grafiek, tegels of detailtabel
- [ ] Records met afsluitreden 'Aanbod afgesloten wegens wijzigen leerroute' worden niet getoond in de grafiek, tegels of detailtabel
- [ ] De uitgesloten afsluitredenen verschijnen niet als optie in het afsluitreden-filter

### Samenvattingstegels

- [ ] Tegel "Totaal" toont het correcte totaal aantal records na filtering
- [ ] Tegel "Gem. per maand" toont het afgeronde gemiddelde per maand
- [ ] Tegel "Top maand" toont de maand met het hoogste aantal en het bijbehorende aantal
- [ ] Tegels worden bijgewerkt bij elke filterwijziging

### Grafiek

- [ ] De grafiek toont een stacked bar chart per maand
- [ ] Elke aanbodnaam heeft een eigen kleur in de stacked bar
- [ ] De legenda toont alle actieve aanbodnamen
- [ ] Bij een jaarfilter worden alle 12 maanden van dat jaar getoond (inclusief maanden met 0 records)
- [ ] Zonder jaarfilter worden alle maanden van de eerste tot de laatste record getoond
- [ ] De grafiek wordt bijgewerkt bij elke filterwijziging

### Detailtabel

- [ ] De detailtabel is standaard verborgen
- [ ] Klik op een staaf in de grafiek opent de detailtabel met de records van die maand en dat aanbod
- [ ] De detailtabel toont alle kolommen: Startdatum, Aanbodnaam, Inwoner, Administratienummer, Coach, Team, Afsluitreden, Einddatum
- [ ] Afsluitreden is leeg voor records zonder afsluitreden (nog actief)
- [ ] Einddatum is leeg voor records zonder einddatum
- [ ] De titel van de detailtabel bevat de aanbodnaam en de maand
- [ ] Actieve filters worden ook toegepast op de detailtabel
