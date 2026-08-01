# Functioneel Ontwerp - Tabblad Inburgeringsfase

## Beschrijving

Het tabblad Inburgeringsfase toont een **momentopname** van de inburgeraars: alleen de cijfers
van nu. Dit dashboard wijkt bewust af van de andere dashboards: het heeft **geen filters** en
**geen tijdreeks/grafiek per maand**, maar toont enkel de actuele stand op basis van de huidige
datum.

Kernbegrippen:

- **Actieve inburgeraar**: een inburgeraar met een lopend inburgeringstraject.
- **PIP**: het Persoonlijk plan Inburgering en Participatie. Een inburgeraar kan (nog) geen PIP
  hebben, een PIP zonder verlenging, of een PIP met verlenging.
- **Eerste PIP**: de eerste keer dat een PIP voor de inburgeraar is opgesteld.
- **10 weken-norm**: de eerste PIP hoort binnen 10 weken (70 dagen) na start van het dossier te
  zijn opgesteld.
- **Laatste 30 dagen**: gerekend vanaf de huidige datum.

## Cijfers

Het dashboard toont de volgende cijfers als tegels. De volledige definitie is per tegel
zichtbaar als mouseover (title). De letteraanduiding hieronder is enkel ter referentie en
staat niet in de interface.

| Ref | Tegel (label)                            | Definitie (mouseover)                                                                                                          |
|-----|------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| A   | Inburgeraars in beeld                    | Totaal aantal actieve inburgeraars                                                                                             |
| B   | Nog zonder plan                          | Totaal aantal inburgeraars zonder PIP                                                                                          |
| C   | Volgens plan                             | Totaal aantal inburgeraars met PIP zonder verlenging                                                                          |
| D   | Extra tijd genomen                       | Totaal aantal inburgeraars met PIP met verlenging                                                                            |
| E   | Eerste PIP's laatste 30 dagen            | Totaal aantal eerste PIP's opgesteld in de laatste 30 dagen                                                                  |
| F   | Binnen 10 weken na dossierstart          | Totaal aantal eerste PIP's opgesteld in de laatste 30 dagen dat binnen 10 weken na start van het dossier is opgesteld         |
| G   | Niet binnen 10 weken na dossierstart     | Totaal aantal eerste PIP's opgesteld in de laatste 30 dagen niet binnen 10 weken na start van het dossier is opgesteld        |

Rekenkundige verbanden:

- `A = B + C + D` (elke actieve inburgeraar valt in precies één PIP-status).
- `E = F + G` (elke eerste PIP uit de laatste 30 dagen is wel of niet binnen 10 weken opgesteld).

## Tijdigheid eerste PIP (verhouding F vs. G t.o.v. E)

Onder de cijfers toont een verhoudingsbalk het aandeel van de eerste PIP's uit de laatste 30
dagen (E) dat wel (F) versus niet (G) binnen 10 weken na dossierstart is opgesteld:

- Groen segment: percentage F ten opzichte van E.
- Rood segment: percentage G ten opzichte van E.
- Een samenvatting toont "F van E op tijd"; bij geen eerste PIP's in de laatste 30 dagen wordt
  dit expliciet gemeld.
- De legenda toont beide percentages ("Binnen 10 weken" en "Niet binnen 10 weken").

De percentages worden afgerond; samen tellen ze op tot 100% (of 0% wanneer E = 0).

## Interactie

- Er zijn geen filters en geen detailtabel; het dashboard is een pure momentopname.
- Elke tegel en de verhoudingsbalk tonen hun definitie als mouseover (title-attribuut).
- De cijfers worden berekend op het moment dat het tabblad wordt geopend, op basis van de
  huidige datum.

## Acceptatiecriteria

### Algemeen

- [ ] Het dashboard is bereikbaar via een eigen tabblad "Inburgeringsfase"
- [ ] Het tabblad ondersteunt hash-navigatie (`#inburgeringsfase`)
- [ ] Het dashboard bevat geen filters
- [ ] Het dashboard toont alleen de actuele stand (momentopname), geen tijdreeks
- [ ] Er wordt een "Data laatst geactualiseerd"-tijdstempel getoond, consistent met de andere dashboards

### Cijfers

- [ ] Tegel A toont het totaal aantal actieve inburgeraars
- [ ] Tegel B toont het totaal aantal inburgeraars zonder PIP
- [ ] Tegel C toont het totaal aantal inburgeraars met PIP zonder verlenging
- [ ] Tegel D toont het totaal aantal inburgeraars met PIP met verlenging
- [ ] Tegel E toont het totaal aantal eerste PIP's opgesteld in de laatste 30 dagen
- [ ] Tegel F toont het aantal eerste PIP's uit de laatste 30 dagen dat binnen 10 weken na dossierstart is opgesteld
- [ ] Tegel G toont het aantal eerste PIP's uit de laatste 30 dagen dat niet binnen 10 weken na dossierstart is opgesteld
- [ ] Het geldt dat A = B + C + D
- [ ] Het geldt dat E = F + G
- [ ] "10 weken" wordt gerekend als 70 dagen na de dossierstartdatum
- [ ] "Laatste 30 dagen" wordt gerekend vanaf de huidige datum

### Mouseovers

- [ ] Elke tegel toont bij mouseover exact de bijbehorende definitie, zonder letteraanduiding
- [ ] De verhoudingsbalk toont bij mouseover een toelichting op de verhouding F versus G t.o.v. E

### Tijdigheid eerste PIP

- [ ] De verhoudingsbalk toont het percentage F (groen) en G (rood) ten opzichte van E
- [ ] De legenda toont beide percentages
- [ ] Bij E = 0 is de balk leeg en wordt gemeld dat er geen eerste PIP's in de laatste 30 dagen zijn
- [ ] De percentages tellen samen op tot 100% (bij E > 0)
