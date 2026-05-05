# Functioneel Ontwerp - Tabblad Contactmomenten

## Beschrijving

Het tabblad Contactmomenten toont een overzicht van alle geregistreerde contactmomenten tussen coaches en inwoners.

## Filters

| Filter               | Omschrijving                            | Opties                                            |
|----------------------|-----------------------------------------|---------------------------------------------------|
| Type dienstverlening | Filtert op type dienstverlening         | Alle typen / Werk / Inburgering                   |
| Jaar                 | Filtert op jaar van contactdatum        | Alle jaren / beschikbare jaren                     |
| Team                 | Filtert op team                         | Alle teams / beschikbare teams                     |
| Coach                | Filtert op coach                        | Alle coaches / beschikbare coaches                 |
| Kanaal               | Filtert op communicatiekanaal           | Alle kanalen / beschikbare kanalen                 |
| Onderwerp            | Filtert op onderwerp van het contact    | Alle onderwerpen / beschikbare onderwerpen         |

Filters werken cascading: het selecteren van een filter beperkt de beschikbare opties in de overige filters.

## Samenvattingstegels

| Tegel            | Omschrijving                                      |
|------------------|---------------------------------------------------|
| Totaal           | Totaal aantal contactmomenten (na filtering)       |
| Gem. per maand   | Gemiddeld aantal per maand                         |
| Top maand        | Maand met het hoogste aantal, inclusief aantal     |

## Grafiek

Bar chart per maand met het totaal aantal contactmomenten. Klik op een staaf om de detailtabel te openen voor die maand.

## Detailtabel

De detailtabel verschijnt na klik op een staaf in de grafiek en toont de individuele records.

| Kolom               | Omschrijving                                    | Datamapping |
|---------------------|-------------------------------------------------|-------------|
| Datum               | Datum waarop het contactmoment is geregistreerd |             |
| Inwoner             | Volledige naam van de inwoner                   |             |
| Administratienummer | Identificatienummer van de inwoner              |             |
| Coach               | Naam van de coach                               |             |
| Team                | Teamnaam                                        |             |
| Kanaal              | Communicatiekanaal                              |             |
| Onderwerp           | Onderwerp van het contactmoment                 |             |

## Mogelijke waarden

### Kanalen

- Telefonisch (inkomend)
- Telefonisch (uitgaand)
- Telefonisch
- Fysiek
- Videobellen
- E-mail inkomend
- E-mail uitgaand
- Post inkomend
- Post uitgaand
- Zaaksysteem
- Intern overleg
- Overleg collega
- Anders

### Onderwerpen

- Voortgangsgesprek
- Participatiegesprek
- Brede Intake
- CV
- Plaatsing
- Aanbod
- Uitkering
- Rapportage
- Talentscan
- Medisch advies
- Kinderopvang
- Verbindende Aanpak
- Drie gesprek
- Loonwaardemeting
- LKS
- Praktijkroute
- Beschut Werk
- PTO
- Zelftest
- Startweek
- Startweek plus
- Partners in de stad
- Leerbaarheidstoets - Uitnodiging
- Leerbaarheidstoets afgerond
- Leerbaarheidstoets niet aanwezig met afmelding
- Leerbaarheidstoets niet aanwezig zonder afmelding
- Plan intakegesprek - ingepland
- Plan intakegesprek - niet bereikbaar
- Voer intakegesprek - afmelding
- Voer intakegesprek - niet aanwezig zonder afmelding
- Plan vervolggesprek - ingepland
- Plan vervolggesprek - niet bereikbaar
- Voer vervolggesprek - afmelding
- Voer vervolggesprek - niet aanwezig zonder afmelding
- Heropenen dossier
- Niet bereikbaar
- No show
- Uitval
- Afmelding
- Terugmelding
- Anders
- Overige

## Acceptatiecriteria

### Filters

- [ ] Alle zes filters (Type dienstverlening, Jaar, Team, Coach, Kanaal, Onderwerp) zijn zichtbaar en selecteerbaar
- [ ] Elk filter toont enkel waarden die voorkomen in de dataset
- [ ] Filters werken cascading: het selecteren van een filter beperkt de opties in de overige filters
- [ ] Bij het deselecteren van een filter (terug naar "Alle...") worden de overige filteropties opnieuw berekend
- [ ] Meerdere filters kunnen tegelijk actief zijn en worden gecombineerd (AND-logica)

### Samenvattingstegels

- [ ] Tegel "Totaal" toont het correcte totaal aantal records na filtering
- [ ] Tegel "Gem. per maand" toont het afgeronde gemiddelde per maand
- [ ] Tegel "Top maand" toont de maand met het hoogste aantal en het bijbehorende aantal
- [ ] Tegels worden bijgewerkt bij elke filterwijziging

### Grafiek

- [ ] De grafiek toont een bar chart per maand met het totaal aantal contactmomenten
- [ ] Bij een jaarfilter worden alle 12 maanden van dat jaar getoond (inclusief maanden met 0 records)
- [ ] Zonder jaarfilter worden alle maanden van de eerste tot de laatste record getoond
- [ ] De grafiek wordt bijgewerkt bij elke filterwijziging

### Detailtabel

- [ ] De detailtabel is standaard verborgen
- [ ] Klik op een staaf in de grafiek opent de detailtabel met de records van die maand
- [ ] De detailtabel toont alle kolommen: Datum, Inwoner, Administratienummer, Coach, Team, Kanaal, Onderwerp
- [ ] De titel van de detailtabel bevat de maand en het aantal records
- [ ] Actieve filters worden ook toegepast op de detailtabel
