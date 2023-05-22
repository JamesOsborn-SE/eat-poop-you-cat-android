# Eat Poop You Cat for Android

## Status

[![maturity beta/wip](https://img.shields.io/badge/maturity-Beta/work%20In%20Progress-red)]()

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/819f04beefcf4d58b9e4248c4f6d643f)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=JamesOsborn-SE/eat-poop-you-cat-android&amp;utm_campaign=Badge_Grade)

[![Android Build](https://github.com/JamesOsborn-SE/eat-poop-you-cat-android/actions/workflows/android.yml/badge.svg)](https://github.com/JamesOsborn-SE/eat-poop-you-cat-android/actions/workflows/android.yml)

[![Android Package](https://github.com/JamesOsborn-SE/eat-poop-you-cat-android/actions/workflows/android-package.yml/badge.svg)](https://github.com/JamesOsborn-SE/eat-poop-you-cat-android/actions/workflows/android-package.yml)

[![iOS status](https://img.shields.io/badge/iOS%20Build-Nope-informational?style=plastic&logo=apple)]()

## Premise

Eat Poop You Cat is a bit like telephone meets Pictionary:tm: where you start with a
sentence pass it to the next person and it they draw a picture.
Then they pass it to the next person and they can only see the last entry
(picture in this case) and they write a sentence. It goes on until
everyone has had a turn or boredom takes hold.

## Screenshots

[![Welcome](/images/Screenshot-Welcome_Screen-300.png)](/images/Screenshot-Welcome_Screen.png)
[![First turn](/images/Screenshot-Sentence_Screen-300.png)](/images/Screenshot-Sentence_Screen.png)
[![Second turn](/images/Screenshot-Draw_Screen-300.png)](/images/Screenshot-Draw_Screen.png)
[![Third turn](images/Screenshot-Draw_the_sentence-300.png)](images/Screenshot-Draw_the_sentence.png)

### Info Needed from user

* Display Name

### Rules

* Each person can only play once per Unique game
* only Monochrome drawings
* ~~Time Limit~~
* ~~Turn limit~~

### Permissions ~~needed~~

**No** permissions needed at the moment but future plans include:

```none
android.permission.BLUETOOTH
android.permission.BLUETOOTH_ADMIN
android.permission.ACCESS_WIFI_STATE
android.permission.CHANGE_WIFI_STATE
android.permission.ACCESS_COARSE_LOCATION
android.permission.ACCESS_FINE_LOCATION
android.permission.ACCESS_BACKGROUND_LOCATION
android.permission.BLUETOOTH_SCAN neverForLocation
android.permission.BLUETOOTH_ADVERTISE
android.permission.BLUETOOTH_CONNECT
```

* these are needed to connect using Nearby Connections and will not be used for anything else. No data sent by the core app

### Tech used

* ~~[nearby connections](https://developers.google.com/nearby/connections/overview) to send and receive data~~
* [ORM - Room](https://developer.android.com/training/data-storage/room/)
* [Wireframes - Figma](https://www.figma.com/file/N5rf2UZaGy0LhD4S7r28OI/EPYC?node-id=0%3A1)

### Run locally

* Install the latest [Android Studio](https://developer.android.com/studio/)
* Enable developer tools on a physical device or make a new virtual device in Android Device Manager [Run your app](https://developer.android.com/studio/)

### Data

```mermaid
classDiagram
    Player --> Entry
    Game <-- "1..*" Entry
    
    class Game {
        +UUID Id
        +int? Timeout in seconds [not implemented]
        +int? Turns [not implemented]
        +Entry[] Entries
        .getLastEntry() [not implemented]
        .getTimeLeft() [not implemented]
        .isOver() [not implemented]
        .hasPlayed(playerId) [not implemented]
    }
    
    class Player{
        +String UserName
        +UUID Id
    }
    
    class Entry{
        +int Sequence
        +String? Sentence
        +ByteArray? Drawing [gzip Json object]
        +Player Player
        +int? SecondsPassed [not implemented]
        .isValid() [not implemented]
    }
```

### Sequence [not implemented]

```mermaid
sequenceDiagram
    autonumber
    Player1->>Player1: Starts new game<br/>writes sentence
    Player1->>Player2: Wanna play?
    Player2->>Player1: Can I play?<br/>check for Player Id in current game
    Player2-->>Player1: Yes, here is payload
    Player1->>Player2: Is there an update for Games?
    loop hasUpdate?
        Player2->>Player2: check for Player Id in current games
    end
    Player2->>Player1: here is update payload
```

### Flow [not implemented]

```mermaid
graph TD
    p[Player] --> TT(Take turn draw / write)
    TT --> LFNP(Look for next player)
    LFNP --> FP{Found Player?}
    FP -->|No| LFNP
    FP -->|Yes| HPG{Has played in this game?}
    HPG -->|No| WTP{Want to play?}
    WTP -->|No| LFNP
    WTP -->|Yes| SP(Send Payload)
    HPG -->|Yes| LFNP
    SP --> SRUL[Send and recieve update loop]
    SRUL --> SRUL
```

## Disclaimer

This app is not by indorsed or related to Pictionary:tm: or Mattel in anyway.
