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
| Subdoel             | Filtert op subdoel van het aanbod     | Alle subdoelen / beschikbare subdoelen            |

Filters werken cascading: het selecteren van een filter beperkt de beschikbare opties in de overige filters.

Een aanbod wordt in een dossier altijd voor precies één subdoel ingezet; hetzelfde aanbod kan echter wel voor meerdere subdoelen bruikbaar zijn.

## Samenvattingstegels

De tegels tonen de totalen per afsluitreden binnen het geselecteerde tijdvak (na filtering).

| Tegel                | Omschrijving                                              |
|----------------------|----------------------------------------------------------|
| Doel behaald         | Aantal records met afsluitreden 'Doel aanbod behaald'      |
| Doel deels behaald   | Aantal records met afsluitreden 'Doel aanbod deels behaald'|
| Doel niet behaald    | Aantal records met afsluitreden 'Doel aanbod niet behaald' |

## Grafiek

Stacked bar chart per maand, uitgesplitst op afsluitreden (inclusief 'Nog actief' voor records zonder afsluitreden). Klik op een staaf om de detailtabel te openen voor die maand en die afsluitreden.

## Detailtabel

De detailtabel verschijnt na klik op een staaf in de grafiek en toont de individuele records.

| Kolom               | Omschrijving                                    | Datamapping |
|---------------------|-------------------------------------------------|-------------|
| Startdatum          | Datum waarop het aanbod is gestart              |             |
| Aanbodnaam          | Naam van het aanbod                             |             |
| Subdoel             | Subdoel waarvoor het aanbod is ingezet          |             |
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

### Subdoelen

Een aanbod is voor één of meerdere van onderstaande subdoelen bruikbaar; per record wordt één subdoel vastgelegd.

- Taalvaardigheid vergroten
- Arbeidsritme opbouwen
- Werknemersvaardigheden ontwikkelen
- Sociaal netwerk versterken
- Financiële zelfredzaamheid vergroten
- Beroepskwalificatie behalen
- Sollicitatievaardigheden verbeteren
- Digitale vaardigheden vergroten
- Zelfvertrouwen versterken
- Ondernemerschap ontwikkelen

## Acceptatiecriteria

### Filters

- [ ] Alle zes filters (Type dienstverlening, Jaar, Team, Coach, Aanbodnaam, Subdoel) zijn zichtbaar en selecteerbaar
- [ ] Afsluitreden is geen filter meer, maar wordt getoond als uitsplitsing in de grafiek
- [ ] Elk filter toont enkel waarden die voorkomen in de dataset
- [ ] Filters werken cascading: het selecteren van een filter beperkt de opties in de overige filters
- [ ] Bij het deselecteren van een filter (terug naar "Alle...") worden de overige filteropties opnieuw berekend
- [ ] Meerdere filters kunnen tegelijk actief zijn en worden gecombineerd (AND-logica)

### Exclusieregel

- [ ] Records met afsluitreden 'Administratief afgesloten' worden niet getoond in de grafiek, tegels of detailtabel
- [ ] Records met afsluitreden 'Aanbod afgesloten wegens wijzigen leerroute' worden niet getoond in de grafiek, tegels of detailtabel
- [ ] De uitgesloten afsluitredenen verschijnen niet als segment in de grafiek

### Samenvattingstegels

- [ ] Tegel "Doel behaald" toont het aantal records met afsluitreden 'Doel aanbod behaald' binnen het tijdvak
- [ ] Tegel "Doel deels behaald" toont het aantal records met afsluitreden 'Doel aanbod deels behaald' binnen het tijdvak
- [ ] Tegel "Doel niet behaald" toont het aantal records met afsluitreden 'Doel aanbod niet behaald' binnen het tijdvak
- [ ] Tegels worden bijgewerkt bij elke filterwijziging

### Grafiek

- [ ] De grafiek toont een stacked bar chart per maand
- [ ] Elke afsluitreden heeft een eigen kleur in de stacked bar ('Nog actief' voor records zonder afsluitreden)
- [ ] De legenda toont alle actieve afsluitredenen
- [ ] Bij een jaarfilter worden alle 12 maanden van dat jaar getoond (inclusief maanden met 0 records)
- [ ] Zonder jaarfilter worden alle maanden van de eerste tot de laatste record getoond
- [ ] De grafiek wordt bijgewerkt bij elke filterwijziging

### Detailtabel

- [ ] De detailtabel is standaard verborgen
- [ ] Klik op een staaf in de grafiek opent de detailtabel met de records van die maand en die afsluitreden
- [ ] De detailtabel toont alle kolommen: Startdatum, Aanbodnaam, Subdoel, Inwoner, Administratienummer, Coach, Team, Afsluitreden, Einddatum
- [ ] Afsluitreden is leeg voor records zonder afsluitreden (nog actief)
- [ ] Einddatum is leeg voor records zonder einddatum
- [ ] De titel van de detailtabel bevat de afsluitreden en de maand
- [ ] Actieve filters worden ook toegepast op de detailtabel
