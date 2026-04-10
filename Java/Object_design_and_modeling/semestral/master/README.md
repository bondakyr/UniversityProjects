# OMO Semestální práce - Smart factory

## Abstrakt

Vytvořit aplikaci pro virtuální simulaci inteligentní továrny, kde simulujeme chod výroby - 
na výrobních linkách s pomocí strojů a lidí vyrábíme produkty. Používáme jednotlivé stroje a 
vyhodnocujeme jejich využití, spotřebu a kvalitu výsledných výrobků. Součástí výrobního 
procesu jsou jen stroje a lidé. Základní časová jednotka je jeden takt (trvá jednu hodinu). 
Stavy továrny se mění (a vyhodnocují) po těchto taktech.

## Funkční požadavky

* **F1 Hlavní entity:**
    - Továrna, Linka (s prioritou), Stroj, Člověk, Výrobek, Materiál a další entity dle potřeby.
    - Stroje, lidé i výrobky mohou být různého druhu.
* **F2 Výroba produktů:**
    - Produkty se vyrábějí v sériích po několika stech kusech.
    - Při změně série nekompatibilních výrobků je nutné výrobní linky přeskládat.
    - Každý výrobek má definovanou sekvenci zařízení, robotů a lidí, které je potřeba uspořádat na linku.
* **F3 Spotřeba a náklady:**
    - Stroje a roboty mají svoji spotřebu.
    - Lidé, roboty, stroje a materiál mají přidružené náklady.
* **F4 Komunikace přes eventy:**
    - Komunikace mezi stroji, roboty a lidmi probíhá pomocí eventů.
    - Event může dostat 1 až N entit (člověk, stroj, robot) registrovaných na daný druh eventu.
* **F5 API pro zařízení:**
    - Každé zařízení poskytuje API pro sběr dat (spotřeba elektřiny, oleje, materiálu, opotřebení).
* **F6 Poruchy:**
    - Stroje a roboty se mohou po určité době rozbít.
    - Po rozbití generují event (alert) s prioritou dle důležitosti linky.
    - Poruchy odbavují opraváři, jejichž počet je omezený. Oprava trvá několik taktů.
* **F7 Návštěva ředitele a inspektora:**
    - Ředitel prochází továrnou podle hierarchie entit (Továrna -> Linka -> Stroj/Robot/Člověk/Výrobek).
    - Inspektor prochází podle míry opotřebení entit.
    - Akce provedené na jednotlivých entitách se zapisují do logu.
* **F8 Generování reportů:**
    - **FactoryConfigurationReport:** Konfigurační data továrny (hierarchie).
    - **EventReport:** Seznam eventů za období (1) podle typu, (2) zdroje a (3) kdo je odbavil.
    - **ConsumptionReport:** Spotřeba elektřiny, oleje, materiálu s finančním vyčíslením.
    - **OuttagesReport:** Výpadky strojů (nejdelší, nejkratší, průměrná doba výpadku a čekání na opraváře).
    - Možnost zrekonstruovat stav strojů v libovolném taktu (jiném než posledním).

## Nefunkční požadavky

- Není požadována autentizace ani autorizace.
- Aplikace musí běžet v jedné JVM.
- Metody a proměnné, které nemají být dostupné jiným třídám, musí být skryté (minimalizace veřejných metod a 
proměnných v Javadocu).
- Reporty jsou generovány do textových souborů.
- Konfigurace továrny je nahrávána z JSON souboru.

## Design patterny:

- **Builder**
- **Factory Method**
- **State**
- **Iterator**
- **Decorator**
- **Singleton**
- **Object Pool**
- **Observer**
- **Visitor**
- **Lazy Initialization**
- **Strategy**
- **Streamy**

## Javadoc

Javadoc dokumentace je umístěna ve složce `docs/'

## Členové tymu:
- Kocevyč Bohdan        
- Bondarenko Kyryl
