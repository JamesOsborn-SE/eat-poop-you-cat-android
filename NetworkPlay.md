# Network Architecture

## General

- Uses REST API with compressed binary payloads
- Over Wi-Fi uses Lan IP port 3792 (EPYC)
- Over Internet Uses TOR Onion Service address
- Link `epyc://[Player's Address][:3792]/[gameId]`

All data available to all players eventually
- [Game](app/src/main/java/dev/develsinthedetails/eatpoopyoucat/data/Game.kt)
- [Roster](app/src/main/java/dev/develsinthedetails/eatpoopyoucat/data/Roster.kt)
- [Entry](app/src/main/java/dev/develsinthedetails/eatpoopyoucat/data/Entry.kt)

Active player is the source of truth for game data
- may not have the latest roster data

## Flow

First player (leader prime) starts an empty game to generate link.
- Sends link to n players
- n players sends same or own link to n more players (leader prime remains)

Players open link
- app opens "accept?"
- registers as available player on Registrars and pulls roster

When new player registers push roster to all players

Leader active picks player
- sends tap and shows notification
- if no ACK
  - pick new next player

Timeout for draw/write
- Leader picks next player

Active player draws/writes and submits
- send turn data to other players
  - includes list of player IDs
  - if receiver knows Players not in list sends to them
  - if missing player ID get roster from sender or leader
- other players will request missing turns from other players who have had a turn.

When no more registered users
- double check hash/count with leader
- end game
- send turn data to other players completed date/time stamp

If leader is not responsive elect new leader
- sync registration 
  - Hash and count is sent with voting if different push-pull registry

## Todo

- allow to quit/deregister without turn
- allow re-share of same game with sharer's address in link
  - leader stays the same
- keep incomplete net game hidden in history
- clean up temp tables

## Screens needed
- 

## Known issues
