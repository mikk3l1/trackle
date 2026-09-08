# WorkTracker – Minimal AI Agent Implementation Plan

## Goal

Build a very simple Android app for tracking work time.

The most important feature is the **home-screen widget**.

The app must work completely offline and store all data locally.

The user should be able to:
- Press **KOM** from the widget to start a work period.
- Press **GÅ** from the widget to end a work period.
- See today's total worked time.
- Support multiple work periods on the same day.
- Export the recorded data as CSV.

Keep the application minimal. Do not add features that are not required.

## Technology
- Kotlin
- Jetpack Compose for the app UI
- Jetpack Glance for the home-screen widget
- **Room for local persistence/database**
- A simple local data model
- CSV export

Avoid unnecessary libraries/frameworks.
No backend/login/cloud/network dependency.

# Phase 1 – Basic App
Create minimal Android app using Kotlin + Jetpack Compose.
Basic screen showing current work status, today’s total, and eventually export access.
STOP and build/run successfully.

# Phase 2 – Work Events and Room

Use **Room** as the local persistence/database solution.

Store **one row for each KOM/GÅ event** rather than storing a work period as a single row.

Example:

| ID | Timestamp        | Type |
|---:|------------------|------|
|  1 | 2026-09-07T07:09 | KOM  |
|  2 | 2026-09-07T14:45 | GÅ   |
|  3 | 2026-09-08T08:00 | KOM  |
|  4 | 2026-09-08T12:00 | GÅ   |
|  5 | 2026-09-08T13:00 | KOM  |
|  6 | 2026-09-08T15:24 | GÅ   |

The database entity should contain at least:
- `id`
- `timestamp`
- `type` (`KOM` or `GÅ`)

Timestamp contains date/time. Seconds are not important and should not be stored/considered.

A pair of `KOM` + `GÅ` events represents one work period.

Example:
- `KOM 08:00 → GÅ 12:00`
- `KOM 13:00 → GÅ 15:24`

Support multiple work periods per day.
Do not automatically subtract breaks.

KOM:
1. Get current local date/time.
2. Ignore seconds.
3. Save a `KOM` event in Room.
4. Mark user as working.

GÅ:
1. Get current local date/time.
2. Ignore seconds.
3. Save a `GÅ` event in Room.
4. Mark user as not working.

Only one active work period at a time.
The app must prevent invalid sequences such as `KOM → KOM` or `GÅ → GÅ`.

STOP and verify.

# Phase 3 – Home-Screen Widget
Use Jetpack Glance.
Widget is most important.
Has KOM and GÅ actions.
Shows current status and today’s total.
Works when main app is not open.
Uses same local Room data.
Updates after actions.
STOP and test on real device.

# Phase 4 – Daily Work-Time Calculation
Calculate work periods by pairing each `KOM` event with the following `GÅ` event.

Sum all completed work periods for each day.
If there is an active `KOM` without a following `GÅ`, use the current time only when calculating the current duration.

Do not continuously store timer values.

Test:
- one work period
- multiple work periods on one day
- active work period
- different dates

STOP and verify.

# Phase 5 – CSV Export
Export the raw work events from Room.

Use **one row per event/timestamp**.

Example:

```csv
ID,Timestamp,Type
1,2026-09-07T07:09,KOM
2,2026-09-07T14:45,GÅ
3,2026-09-08T08:00,KOM
4,2026-09-08T12:00,GÅ
5,2026-09-08T13:00,KOM
6,2026-09-08T15:24,GÅ
```

Do not export only the calculated daily total. The raw events are the source of truth, and daily totals can be calculated from them.

Use Android standard save/share.
STOP and verify.

# Phase 6 – Basic Polish
Readable widget, distinguish KOM/GÅ, status, today total, clean UI, light/dark mode if straightforward.
No unnecessary features.

# Definition of Done
- app builds/runs
- widget exists
- KOM/GÅ from widget
- individual KOM/GÅ timestamps saved in Room
- seconds ignored
- multiple work periods/day
- daily total correct
- widget displays today total
- local storage
- survives app restart
- CSV export
- CSV contains one row per KOM/GÅ event
- raw events are the source of truth

# AI Agent Rules
1. Keep simple.
2. No unspecified features.
3. No backend/cloud.
4. No overengineering.
5. One phase at a time.
6. Build/test after each phase.
7. STOP after each phase and wait for approval.
8. Ask before major architectural decisions.
9. Don’t rewrite working code unnecessarily.
10. Prefer simplest solution.

# Documentation / Context7
If documentation is missing, unclear, outdated, or the agent is unsure how to implement Android/Kotlin/Compose/Glance/Room APIs, it may use the **Context7 MCP server** to retrieve current library documentation.

Use Context7 especially for:
- Jetpack Compose
- Jetpack Glance
- Android App Widgets
- Room
- Android file/document sharing APIs
- Kotlin/AndroidX APIs

Do not use Context7 unnecessarily.
Prefer official/current docs through Context7 when verifying APIs.
Do not invent APIs when docs can be checked.

# Execution Order
Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 6 → DONE
Do not skip ahead unless explicitly instructed.
