## 📱 Yumaste Android

Native Android client for **Yumaste**, a meal-kit e-commerce platform that allows users
to discover, order, and manage food boxes delivered to their door.

### 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Navigation | Jetpack Navigation Compose |
| Networking | Retrofit 2 + OkHttp + Moshi |
| Local Storage | Room + DataStore Preferences |
| Image Loading | Coil Compose |
| Build | Gradle KTS + Secrets Plugin |
| Testing | Robolectric + Roborazzi |

### 📲 Screens

- **Login / Registrazione** — autenticazione JWT con il backend
- **Catalogo** — sfoglia le box per categoria con filtri
- **Dettaglio Box** — ingredienti, valori nutrizionali, descrizione AI
- **Carrello** — gestione quantità e riepilogo ordine
- **Ordini** — storico degli ordini effettuati
- **Profilo** — gestione dati utente e preferenze

### 🔗 Backend

Comunica con [yumaste-backend](https://github.com/SantoFemiano/yumaste-backend)
(Spring Boot 4 · Java 21 · MySQL · Redis · Kafka).

### ⚙️ Setup

1. Copia `.env.example` in `.env`
2. Inserisci l'URL del backend: `BASE_URL=https://your-api-url`
3. Build & Run su Android Studio (minSdk 24, targetSdk 36)
