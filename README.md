# Luna Coin

Luna Coin ist eine Android-App für Familien, um Aufgaben, Belohnungen, Shop-Käufe, LunaME-Items, Glücksrad, Spiele-Highscores und Hundepläne zu verwalten.

Die App ist in Kotlin mit Jetpack Compose umgesetzt und verwendet Firebase/Firestore für Cloud-Daten und Realtime-Synchronisation.

## Projektstatus

Dieses Projekt befindet sich in aktiver Entwicklung.

Wichtige Ziele:

- Bestehende Funktionen stabil halten
- UI-Verhalten nicht unbeabsichtigt ändern
- Firestore-Struktur nicht ohne Plan ändern
- Realtime-Synchronisation erhalten
- ViewModel schlank halten
- Fachlogik in Manager-Klassen auslagern

## Technische Basis

- Android / Kotlin
- Jetpack Compose
- Firebase Firestore
- Firebase Authentication
- Kotlin Serialization
- Coil für Bilder/GIFs

Die App verwendet aktuell das Package:

```text
de.meson_labs.luna_coin
```

## Wichtige Projektstruktur

```text
app/src/main/java/de/meson_labs/luna_coin/
│
├── data/
│   ├── DemoData.kt
│   └── repository/
│       ├── DataRepository.kt
│       └── FirestoreRepository.kt
│
├── manager/
│   ├── BackupManager.kt
│   ├── CoinManager.kt
│   ├── DogScheduleManager.kt
│   ├── GameHighscoreManager.kt
│   ├── InventoryManager.kt
│   ├── LogManager.kt
│   ├── LuckyWheelManager.kt
│   ├── ManagerUtils.kt
│   ├── ShopManager.kt
│   ├── TaskManager.kt
│   └── UserManager.kt
│
├── models/
│
├── screens/
│
└── viewmodel/
    └── LunaCoinViewModel.kt
```

## Architektur-Grundsatz

Die UI spricht mit dem `LunaCoinViewModel`.
Das ViewModel hält UI-State und delegiert Fachlogik an Manager.
Die Manager sprechen mit dem `DataRepository`.
Das Repository kapselt die konkrete Datenquelle, aktuell Firestore.

```text
UI / Compose Screens
        ↓
LunaCoinViewModel
        ↓
Manager-Klassen
        ↓
DataRepository
        ↓
FirestoreRepository
        ↓
Firebase Firestore
```

Details stehen in:

```text
ARCHITECTURE.md
```

## Wichtige Regeln für Änderungen

- Keine Firestore-Struktur ändern, ohne Migration/Plan.
- Keine Realtime-Synchronisation beschädigen.
- Keine UI unbeabsichtigt ändern.
- Coins nicht direkt in Screens ändern.
- Fachlogik nicht in Composables schreiben.
- Manager im ViewModel wirklich verwenden, nicht nur anlegen.
- Nach jedem Refactoring muss die App kompilieren.

Weitere Regeln stehen in:

```text
CONTRIBUTING.md
```

## Build

Typischer Build über Android Studio oder Gradle:

```bash
./gradlew assembleDebug
```

Unter Windows:

```bat
gradlew.bat assembleDebug
```

## Hinweise für zukünftige Entwicklung

Neue Funktionen sollten möglichst so aufgebaut werden:

1. Model prüfen oder erweitern
2. Repository-Methode prüfen oder ergänzen
3. Manager-Methode erstellen
4. ViewModel delegieren lassen
5. UI nur an ViewModel anbinden
6. Kompilieren
7. Funktion testen

## Dokumentation

- `ARCHITECTURE.md` – technische Struktur und Verantwortlichkeiten
- `CONTRIBUTING.md` – Regeln für zukünftige Änderungen
- `README.md` – Projektüberblick
