# Contributing Guidelines

Diese Datei beschreibt die wichtigsten Regeln für zukünftige Änderungen an der Luna Coin App.

Ziel ist, dass die App stabil bleibt und die neue Architektur nicht wieder verwässert.

## Grundregeln

- Nach jedem Schritt muss die App kompilieren.
- Keine UI ändern, wenn das Refactoring nur Architektur betrifft.
- Keine Firestore-Struktur ändern, ohne vorher einen klaren Migrationsplan zu haben.
- Keine Realtime-Synchronisation beschädigen.
- Keine bestehende Funktionalität stillschweigend verändern.
- Änderungen möglichst klein halten.

## Dateien vollständig ersetzen

Wenn eine Änderung mehrere Stellen in einer Datei betrifft, immer die komplette Datei ersetzen.
Keine unvollständigen Ausschnitte verwenden.

## ViewModel-Regeln

Das `LunaCoinViewModel` ist für UI-State und Koordination zuständig.
Es sollte nicht wieder zu viel Fachlogik enthalten.

Erlaubt im ViewModel:

- `StateFlow` / UI-State
- `viewModelScope.launch`
- Aufruf von Managern
- Optimistic UI Update, falls nötig
- Rollback-Koordination, falls nötig
- Fehlermeldungen über `showMessage(...)`

Nicht ins ViewModel zurückschieben:

- Wiederholungslogik von Aufgaben
- Shop-Kauflogik
- LunaME-Inventarlogik
- Highscore-Logik
- Backup-Logik
- Hundeplan-Logik
- reine Datumslogik

## Manager-Regeln

Manager enthalten Fachlogik.
Sie sprechen mit dem `DataRepository`, aber nicht direkt mit Compose-UI.

Aktuelle Manager:

```text
BackupManager
CoinManager
DogScheduleManager
GameHighscoreManager
InventoryManager
LogManager
LuckyWheelManager
ShopManager
TaskManager
UserManager
```

Neue Logik sollte zuerst einem bestehenden Manager zugeordnet werden.
Nur dann einen neuen Manager erstellen, wenn wirklich eine neue fachliche Verantwortung entsteht.

## Repository-Regeln

`DataRepository` ist die Abstraktion für Datenzugriff.
`FirestoreRepository` enthält die konkrete Firestore-Implementierung.

Regeln:

- Firestore-Zugriffe nicht aus Composables heraus machen.
- Firestore-Zugriffe nicht direkt aus Screens heraus machen.
- Neue Persistenzmethoden zuerst im Repository-Interface definieren.
- Danach in `FirestoreRepository` implementieren.

## Coin-Regeln

Coins sind kritisch, weil sie von mehreren Bereichen genutzt werden:

- Aufgaben
- Shop
- LunaME
- Glücksrad
- Log-Undo
- manuelle Anpassungen

Regeln:

- Coins nicht direkt in UI-Screens ändern.
- Coins nicht ohne Repository-Transaktion dauerhaft ändern.
- Coin-Änderungen müssen geloggt werden, wenn sie für Benutzer sichtbar/relevant sind.
- Negative Coins vermeiden.

## Firestore-Regeln

Nicht ohne Plan ändern:

- Collection-Namen
- Dokument-IDs
- Feldnamen
- Familienstruktur
- Backup-Struktur

Wenn die Firestore-Struktur geändert werden muss:

1. Änderung dokumentieren
2. Migration planen
3. Rückwärtskompatibilität prüfen
4. Test mit vorhandenen Daten machen

## Realtime-Sync-Regeln

Die Realtime-Synchronisation darf nicht durch Manager-Refactoring beschädigt werden.

Regeln:

- `startRealtimeSync(...)` bleibt zentral im ViewModel oder in einer klaren Sync-Klasse.
- Realtime-Daten müssen weiterhin sortiert und abgesichert werden.
- Die Kindersortierung nach Alter muss erhalten bleiben.
- Gelöschte oder fehlende ausgewählte Kinder müssen sauber behandelt werden.

## Sortierung der Kinder

Die Reihenfolge der Kinder ist wichtig.

Regel:

```kotlin
children.sortedBy { it.age }
```

Diese Sortierung darf nicht versehentlich entfernt werden.

## Refactoring-Regeln

Bei jedem Refactoring prüfen:

```text
✓ App kompiliert
✓ Manager wird importiert
✓ Manager wird instanziiert
✓ Manager wird wirklich aufgerufen
✓ Alte Implementierung wurde entfernt
✓ Keine UI geändert
✓ Keine Firestore-Struktur geändert
✓ Keine Realtime-Synchronisation geändert
```

## Empfohlene Reihenfolge für neue Features

1. Model prüfen
2. Repository prüfen
3. Manager erweitern
4. ViewModel delegieren lassen
5. UI anbinden
6. Kompilieren
7. Manuell testen
8. Commit erstellen

## Commit-Empfehlung

Commits klein halten.

Gute Beispiele:

```text
Refactor shop item CRUD into ShopManager
Move task date logic into TaskManager
Add architecture documentation
Fix LunaME inventory persistence
```

Schlechte Beispiele:

```text
Update everything
Fix stuff
Big refactor
```

## Manuelle Tests nach Änderungen

Je nach geändertem Bereich mindestens prüfen:

- App startet
- Benutzerlogin funktioniert
- Kindersortierung stimmt
- Aufgaben werden angezeigt
- Aufgabe abschließen gibt Coins
- Shop-Kauf zieht Coins ab
- LunaME-Kauf zieht Coins ab und speichert Inventar
- Glücksrad funktioniert einmal pro Tag korrekt
- Highscores werden gespeichert
- Hundeplan wird gespeichert
- Backup erstellen/wiederherstellen funktioniert
- Änderungen erscheinen auf zweitem Gerät per Realtime-Sync

## Wichtig

Lieber kleine sichere Schritte als große riskante Refactorings.

Die App ist funktional wichtiger als perfekte Architektur.
