# Luna Coin App – Architektur

Diese Datei beschreibt die aktuelle Struktur der Luna Coin App nach dem Refactoring.
Ziel ist, dass neue Funktionen künftig sauber eingeordnet werden und das `LunaCoinViewModel` nicht wieder zu groß wird.

## Grundprinzip

Die App folgt weiterhin einer einfachen MVVM-Struktur:

```text
UI / Compose Screens
        ↓
LunaCoinViewModel
        ↓
Manager
        ↓
DataRepository
        ↓
FirestoreRepository / lokale Speicherung
```

Das `LunaCoinViewModel` hält weiterhin den UI-State und die `StateFlow`s.
Geschäftslogik soll möglichst in Manager-Klassen liegen.

## Wichtige Regeln

- Keine UI-Logik in Manager verschieben.
- Keine Firestore-Struktur ohne bewusste Entscheidung ändern.
- Realtime-Synchronisation bleibt zentral im `LunaCoinViewModel`.
- Kindersortierung nach Alter muss erhalten bleiben.
- Coin-Änderungen sollen zentral über `CoinManager` oder klar abgegrenzte Manager-Methoden laufen.
- Inventar-Änderungen sollen über `InventoryManager` laufen.
- Logik für Aufgaben-Wiederholungen gehört in den `TaskManager`.
- Das ViewModel soll koordinieren, aber keine große Geschäftslogik enthalten.
- Bei Refactorings immer kleine Schritte machen und nach jedem Schritt kompilieren.

## LunaCoinViewModel

Zuständigkeiten:

- UI-State halten
- `StateFlow`s bereitstellen
- ausgewählten Benutzer verwalten
- ausgewähltes Datum verwalten
- Ladezustand und Meldungen verwalten
- Realtime-Synchronisation starten und stoppen
- Manager aufrufen
- Optimistic Updates koordinieren, wenn nötig

Das ViewModel sollte möglichst keine neue Fachlogik bekommen.
Wenn neue Logik entsteht, zuerst prüfen, ob sie in einen bestehenden Manager gehört.

## Manager-Übersicht

### ManagerUtils

Gemeinsame Hilfsfunktionen.

Typische Inhalte:

- UUID-Erzeugung
- Datum/Zeit-Formatierung
- Kindersortierung
- Built-in-Admin absichern
- Loglisten begrenzen
- sichere Datums-Konvertierung

### UserManager

Zuständig für Benutzerverwaltung.

Enthält:

- Benutzer anlegen
- Benutzer bearbeiten
- Benutzer löschen
- Admin-Schutz
- Built-in-Admin-Regeln
- Bereinigung abhängiger Daten beim Löschen

Nicht zuständig für:

- Coin-Änderungen
- Login-UI
- Realtime-Sync

### CoinManager

Zuständig für direkte Coin-Änderungen.

Enthält:

- Coins setzen
- reale Coin-Werte nach Firestore-Transaktion anwenden
- Vorbereitung für optimistische Coin-Updates

Coin-Logik soll nicht mehrfach in anderen Klassen dupliziert werden.

### ShopManager

Zuständig für Shop-Items.

Enthält:

- ShopItem anlegen
- ShopItem bearbeiten
- ShopItem löschen
- ShopItem kaufen
- Tageslimit prüfen
- Kauf-Logs vorbereiten

Nicht zuständig für:

- LunaME-Inventar
- Aufgabenbelohnungen
- Glücksrad

### InventoryManager

Zuständig für LunaME und Inventar.

Enthält:

- LunaME-Item kaufen
- Inventar aktualisieren
- ausgerüstetes Item setzen
- Inventar in Firestore speichern

Soll auch verwendet werden, wenn andere Funktionen Inventar verändern, zum Beispiel Skin-Gewinne.

### LuckyWheelManager

Zuständig für Glücksrad-Logik.

Enthält:

- Glücksrad-Ergebnis anwenden
- tägliche Nutzung speichern
- Skin-Gewinne verarbeiten
- Coin-Änderungen vorbereiten
- LuckyWheelUsage speichern

Nicht zuständig für:

- UI-Animationen
- Segment-Darstellung

### TaskManager

Zuständig für Aufgaben.

Enthält:

- Aufgabe anlegen
- Aufgabe bearbeiten
- Aufgabe löschen
- Aufgabe abschließen
- Sichtbarkeit einer Aufgabe für Kind und Datum prüfen
- Abschlussprüfung
- Wiederholungslogik
- Datumslogik
- Aufgaben-Logs vorbereiten

Die Wiederholungslogik soll nicht in Screens oder ViewModel dupliziert werden.

### DogScheduleManager

Zuständig für Hundeplan.

Enthält:

- Hundeplan-Eintrag anlegen
- Hundeplan-Eintrag bearbeiten
- Hundeplan-Eintrag löschen
- Persistenz für Hundeplan

### BackupManager

Zuständig für Backup und Restore.

Enthält:

- Cloud-Backup erstellen
- Cloud-Backup laden
- Backup wiederherstellen
- Demo-Daten zurücksetzen
- JSON-Import-Platzhalter

### LogManager

Zuständig für Log-Aktionen.

Enthält:

- Log rückgängig machen
- Coin-Undo vorbereiten
- Log löschen
- realen Coin-Wert nach Undo anwenden

Log-Erzeugung kann künftig weiter zentralisiert werden, wenn sich Wiederholungen zeigen.

### GameHighscoreManager

Zuständig für Spiel-Highscores.

Enthält:

- Highscore vorbereiten
- Highscore nur speichern, wenn er besser/neu ist
- Highscore persistieren

## Repository-Schicht

`DataRepository` ist die zentrale Schnittstelle für Datenzugriffe.

Manager sollten nur über `DataRepository` arbeiten und nicht direkt mit Firestore.

`FirestoreRepository` enthält die konkrete Firestore-Implementierung.
Die bestehende Firestore-Struktur darf nicht beiläufig geändert werden.

## Realtime-Synchronisation

Realtime-Sync bleibt im `LunaCoinViewModel`.

Grund:

- Realtime-Sync aktualisiert den zentralen UI-State.
- Manager sollen keine dauerhaften Listener halten.
- Manager sollen möglichst zustandsarm bleiben.

## Umgang mit Coins

Coins sind kritisch, weil mehrere Funktionen darauf zugreifen:

- Aufgabenbelohnungen
- Shop-Käufe
- LunaME-Käufe
- Glücksrad
- manuelle Anpassungen
- Log-Undo

Regel:

- Firestore-Transaktionen für Coins bleiben im Repository.
- Manager bereiten Änderungen vor.
- Das ViewModel aktualisiert den UI-State optimistisch und setzt nach Persistenz den echten Wert.
- Bei Fehlern muss der vorherige Zustand wiederhergestellt werden.

## Umgang mit Logs

Logs dienen als Verlauf und teilweise als Grundlage für Rückgängig-Aktionen oder Tageslimits.

Regel:

- Logtexte nicht unnötig ändern, weil bestehende Auswertungen daran hängen können.
- Logtypen bewusst wählen.
- Tageslimit-Logik im Shop muss stabil bleiben.

## Umgang mit Aufgaben

Aufgaben sind fachlich komplex.

Regel:

- Wiederholungslogik ausschließlich im `TaskManager` pflegen.
- Keine Wiederholungsprüfung in Compose-Screens duplizieren.
- Neue RepeatTypes immer im `TaskManager` ergänzen.
- Nach Änderungen an Aufgabenlogik besonders prüfen:
  - täglich
  - Wochentage
  - Wochenende
  - wöchentlich
  - zweiwöchentlich
  - monatlich
  - jährlich
  - alle zwei Jahre
  - einmalig

## Umgang mit Inventar und LunaME

Inventaränderungen sollen zentral bleiben.

Regel:

- Neue Items gehören in den Item-Katalog bzw. die Models.
- Kaufen/Freischalten über `InventoryManager`.
- Andere Manager sollen Inventar nicht direkt speichern, außer über klar gekapselte Methoden.

## Neue Funktionen einbauen

Vor einer neuen Funktion zuerst entscheiden:

```text
Betrifft es Benutzer?      → UserManager
Betrifft es Coins?         → CoinManager
Betrifft es Shop?          → ShopManager
Betrifft es LunaME?        → InventoryManager
Betrifft es Aufgaben?      → TaskManager
Betrifft es Glücksrad?     → LuckyWheelManager
Betrifft es Hundeplan?     → DogScheduleManager
Betrifft es Backup?        → BackupManager
Betrifft es Logs?          → LogManager
Betrifft es Highscores?    → GameHighscoreManager
Betrifft es nur UI-State?  → LunaCoinViewModel
```

## Refactoring-Regeln

- Immer nur einen Bereich pro Schritt ändern.
- Nach jedem Schritt kompilieren.
- Wenn mehrere Stellen in einer Datei geändert werden, komplette Datei ersetzen.
- Keine UI ändern, wenn das Refactoring nur Architektur betrifft.
- Keine Firestore-Struktur ändern.
- Keine Realtime-Synchronisation ändern.
- Kritische Stellen nach jedem Schritt prüfen:
  - Kindersortierung nach Alter
  - Coin-Änderungen
  - Logs
  - Tageslimits
  - Aufgaben-Wiederholungen
  - LuckyWheel-Usage
  - Inventar/LunaME

## Aktueller Refactoring-Stand

Aus dem ursprünglichen großen `LunaCoinViewModel` wurden bereits ausgelagert:

```text
✓ ManagerUtils
✓ UserManager
✓ CoinManager
✓ ShopManager
✓ InventoryManager
✓ LuckyWheelManager
✓ TaskManager
✓ DogScheduleManager
✓ BackupManager
✓ LogManager
✓ GameHighscoreManager
```

Das ViewModel bleibt der zentrale Koordinator, enthält aber deutlich weniger Fachlogik als vorher.

## Empfohlene nächste Verbesserungen

Nur durchführen, wenn der aktuelle Stand stabil ist:

1. Manager intern weiter vereinheitlichen.
2. Doppelte Muster bei `prepare`, `persist` und `apply` prüfen.
3. Log-Erzeugung weiter zentralisieren.
4. Performance prüfen, insbesondere unnötige `copy`, `map` und Sortierungen.
5. TaskManager bei Bedarf intern weiter gliedern.

Keine dieser Verbesserungen ist zwingend nötig, solange die App stabil läuft.
