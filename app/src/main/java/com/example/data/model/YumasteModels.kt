package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- AUTHENTICATION ---

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val email: String? = null,
    val nome: String? = null,
    val cognome: String? = null
)

@JsonClass(generateAdapter = true)
data class RegistrazioneRequest(
    val cf: String,
    val nome: String,
    val cognome: String,
    val dataNascita: String, // YYYY-MM-DD
    val telefono: String,
    val email: String,
    val password: String
)

// --- CATALOG ---

@JsonClass(generateAdapter = true)
data class BoxCatalogo(
    val id: Int,
    val nome: String,
    val categoria: String? = null,
    val categorie: List<String>? = null,
    val porzioni: Int? = null,
    val prezzo: Double,
    val prezzoScontato: Double? = null,
    val immagineUrl: String? = null,
    val scontoApplicato: String? = null,
    val descrizione: String? = null
)

@JsonClass(generateAdapter = true)
data class BoxPageResponse(
    val content: List<BoxCatalogo>?,
    val totalPages: Int? = null
)

@JsonClass(generateAdapter = true)
data class MacroTotali(
    val proteine: Double,
    val carboidrati: Double,
    val grassi: Double,
    val zuccheri: Double? = null,
    val fibre: Double? = null,
    val sale: Double? = null,
    val chilocalorie: Double
)

@JsonClass(generateAdapter = true)
data class Ingrediente(
    val nomeIngrediente: String,
    val quantitaNellaBox: Double,
    val unitaMisura: String,
    val chilocalorie: Double,
    val proteine: Double,
    val carboidrati: Double,
    val grassi: Double
)

@JsonClass(generateAdapter = true)
data class DettaglioBoxData(
    val id: Int,
    val nome: String,
    val categoria: String,
    val porzioni: Int,
    val prezzoOriginale: Double,
    val prezzoScontato: Double? = null,
    val percentualeSconto: Double? = null,
    val immagineUrl: String? = null,
    val macroTotali: MacroTotali? = null,
    val allergeni: List<String>? = null,
    val ingredienti: List<Ingrediente>? = null
)

// --- SHOPPING CART ---

@JsonClass(generateAdapter = true)
data class CarrelloItem(
    val boxId: Int,
    val nomeBox: String,
    val quantita: Int,
    val prezzo: Double? = null,
    val prezzoScontato: Double? = null,
    val immagineUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class CarrelloResponse(
    val items: List<CarrelloItem> = emptyList(),
    val totalPrice: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class AddCartRequest(
    val boxId: Int,
    val quantita: Int
)

@JsonClass(generateAdapter = true)
data class UpdateCartRequest(
    val boxId: Int,
    val quantita: Int
)

// --- CHECKOUT & ADDRESSES ---

@JsonClass(generateAdapter = true)
data class CheckoutRequest(
    val indirizzoId: Int,
    val metodoPagamento: String = "CARTA_DI_CREDITO"
)

@JsonClass(generateAdapter = true)
data class CheckoutResponse(
    val codiceOrdine: String
)

@JsonClass(generateAdapter = true)
data class Indirizzo(
    val id: Int? = null,
    val via: String,
    val civico: String,
    val cap: String,
    val citta: String,
    val provincia: String,
    val note: String? = ""
)

// --- ORDERS ---

@JsonClass(generateAdapter = true)
data class Ordine(
    val id: Int,
    val codiceOrdine: String,
    val dataOrdine: String,
    val totalePrezzo: Double,
    val statoOrdine: String,
    val statoSpedizione: String
)

@JsonClass(generateAdapter = true)
data class IndirizzoResponseDTO(
    val via: String,
    val civico: String,
    val cap: String,
    val citta: String,
    val provincia: String
)

@JsonClass(generateAdapter = true)
data class OrdiniDettagliDTO(
    val ordineid: Int,
    val boxid: Int,
    val quantita: Int,
    val prezzounitario: Double,
    val metodopagamento: String,
    val datapagamento: String,
    val importo: Double,
    val corriere: String? = null,
    val statospedizione: String,
    val indirizzorespdtodev: IndirizzoResponseDTO? = null
)

// --- PROFILE ---

@JsonClass(generateAdapter = true)
data class DatiUtente(
    val nome: String,
    val cognome: String,
    val email: String
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    val nome: String,
    val cognome: String,
    val email: String
)

@JsonClass(generateAdapter = true)
data class UpdatePasswordRequest(
    val vecchiaPassword: String,
    val nuovaPassword: String
)

// --- AI CHEF RECOMMENDATION ---

@JsonClass(generateAdapter = true)
data class AiRecommendRequest(
    val obiettivo: String,
    val tipoDieta: String,
    val calorieGiornaliere: Int,
    val allergeni: List<String>
)

@JsonClass(generateAdapter = true)
data class AiResult(
    val boxId: Int?,
    val nomeBox: String,
    val messaggio: String
)
