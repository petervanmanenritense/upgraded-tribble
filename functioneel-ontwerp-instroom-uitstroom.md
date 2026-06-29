# Functioneel Ontwerp - Tabblad Instroom / Uitstroom

## Beschrijving

Het tabblad Instroom / Uitstroom toont de in- en uitstroom van zaken per maand, samen met
het verloop van het totaal aantal open zaken (de caseload).

- **Instroom**: er start een zaak voor een inwoner (de startdatum van de zaak).
- **Uitstroom**: een zaak wordt afgesloten (de einddatum van de zaak).
- **Open zaken**: het saldo van alle zaken die zijn gestart en op dat moment nog niet zijn
  afgesloten. Per maand geldt: `open aan einde maand = open vorige maand + instroom − uitstroom`.

Een zaak zonder einddatum is nog open.

## Filters

| Filter          | Omschrijving                              | Opties                                      |
|-----------------|-------------------------------------------|---------------------------------------------|
| Dienstverlening | Filtert op type dienstverlening           | Alle typen / Werk / Inburgering             |
| Jaar            | Filtert op weergegeven jaar               | Alle jaren / beschikbare jaren              |
| Maand           | Filtert op weergegeven maand              | Alle maanden / januari t/m december         |
| Team            | Filtert op team                           | Alle teams / beschikbare teams              |
| Coach           | Filtert op coach                          | Alle coaches / beschikbare coaches          |

Filters werken cascading: het selecteren van een filter beperkt de beschikbare opties in de
overige filters.

Onderscheid in werking:

- **Dienstverlening, Team en Coach** bepalen *welke zaken* meetellen (de scope van de dataset).
- **Jaar en Maand** bepalen *welke maanden* in de grafiek worden getoond (het tijdvenster).

Het aantal open zaken wordt altijd berekend over de volledige historie van de gefilterde
dataset (dienstverlening/team/coach), ook als het tijdvenster door jaar/maand wordt beperkt.
Zo blijft de open caseload per getoonde maand correct, ongeacht de geselecteerde periode.

## Samenvattingstegels

| Tegel                         | Omschrijving                                                              |
|-------------------------------|--------------------------------------------------------------------------|
| Gemiddelde instroom           | Gemiddeld aantal gestarte zaken per maand over de getoonde periode        |
| Gemiddelde uitstroom          | Gemiddeld aantal afgesloten zaken per maand over de getoonde periode      |
| Gemiddeld aantal open zaken   | Gemiddelde open caseload over de getoonde maanden                        |
| Gemiddelde doorlooptijd       | Gemiddeld aantal dagen dat een zaak open is, voor zaken die in de periode zijn uitgestroomd |

## Grafiek

Een gecombineerde grafiek per maand:

- **Instroom** en **uitstroom** als gegroepeerde staven op de linker y-as (in-/uitstroom).
- **Open zaken** als lijn op de rechter y-as (open zaken).

Omdat open zaken een voorraadgrootheid is (en doorgaans groter dan de maandelijkse stroom),
wordt deze op een aparte tweede y-as getoond zodat alle reeksen goed leesbaar blijven.

Klik op een staaf of een punt op de lijn om de bijbehorende detailtabel te openen:

- Klik op een **instroom**-staaf → zaken die in die maand zijn gestart.
- Klik op een **uitstroom**-staaf → zaken die in die maand zijn afgesloten.
- Klik op een **lijnpunt (open zaken)** → zaken die aan het einde van die maand open waren.

## Detailtabel

De detailtabel verschijnt na klik op de grafiek en toont de individuele zaken.

| Kolom               | Omschrijving                                       |
|---------------------|----------------------------------------------------|
| Startdatum          | Datum waarop de zaak is gestart (instroom)         |
| Einddatum           | Datum waarop de zaak is afgesloten (uitstroom); leeg indien open |
| Inwoner             | Volledige naam van de inwoner                      |
| Administratienummer | Identificatienummer van de inwoner                 |
| Coach               | Naam van de coach                                  |
| Team                | Teamnaam                                           |
| Dienstverlening     | Type dienstverlening                               |
| Status              | Open of Afgesloten                                 |

## Acceptatiecriteria

### Filters

- [ ] Alle vijf filters (Dienstverlening, Jaar, Maand, Team, Coach) zijn zichtbaar en selecteerbaar
- [ ] Elk filter toont enkel waarden die voorkomen in de dataset
- [ ] Filters werken cascading: het selecteren van een filter beperkt de opties in de overige filters
- [ ] Dienstverlening, Team en Coach beperken de meegetelde zaken
- [ ] Jaar en Maand beperken het getoonde tijdvenster van de grafiek
- [ ] Meerdere filters kunnen tegelijk actief zijn en worden gecombineerd (AND-logica)

### Samenvattingstegels

- [ ] Tegel "Gemiddelde instroom" toont het gemiddelde aantal gestarte zaken per maand in de getoonde periode
- [ ] Tegel "Gemiddelde uitstroom" toont het gemiddelde aantal afgesloten zaken per maand in de getoonde periode
- [ ] Tegel "Gemiddeld aantal open zaken" toont de gemiddelde open caseload over de getoonde maanden
- [ ] Tegel "Gemiddelde doorlooptijd" toont het gemiddelde aantal dagen open van de in de periode uitgestroomde zaken
- [ ] Tegels worden bijgewerkt bij elke filterwijziging

### Grafiek

- [ ] De grafiek toont per maand instroom en uitstroom als staven en open zaken als lijn
- [ ] Open zaken wordt op een tweede (rechter) y-as weergegeven
- [ ] Bij een jaarfilter worden alle 12 maanden van dat jaar getoond
- [ ] Zonder jaar-/maandfilter worden alle maanden van de eerste tot de laatste maand getoond
- [ ] Het aantal open zaken is gelijk aan de cumulatieve instroom minus de cumulatieve uitstroom
- [ ] De grafiek wordt bijgewerkt bij elke filterwijziging

### Detailtabel

- [ ] De detailtabel is standaard verborgen
- [ ] Klik op een instroom-staaf toont de zaken die in die maand zijn gestart
- [ ] Klik op een uitstroom-staaf toont de zaken die in die maand zijn afgesloten
- [ ] Klik op een lijnpunt toont de zaken die aan het einde van die maand open waren
- [ ] De detailtabel toont alle kolommen: Startdatum, Einddatum, Inwoner, Administratienummer, Coach, Team, Dienstverlening, Status
- [ ] De titel van de detailtabel bevat de maand en het aantal zaken
- [ ] Actieve filters worden ook toegepast op de detailtabel
