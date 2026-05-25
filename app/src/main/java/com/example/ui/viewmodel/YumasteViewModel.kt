package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.RetrofitClient
import com.example.data.local.SessionManager
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YumasteViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    // --- AUTHENTICATION STATES ---
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userProfile = MutableStateFlow<DatiUtente?>(null)
    val userProfile: StateFlow<DatiUtente?> = _userProfile.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    // --- CATALOG STATES ---
    private val _boxes = MutableStateFlow<List<BoxCatalogo>>(emptyList())
    val boxes: StateFlow<List<BoxCatalogo>> = _boxes.asStateFlow()

    private val _isLoadingCatalog = MutableStateFlow(false)
    val isLoadingCatalog: StateFlow<Boolean> = _isLoadingCatalog.asStateFlow()

    private val _catalogError = MutableStateFlow<String?>(null)
    val catalogError: StateFlow<String?> = _catalogError.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tutte")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _catalogSearchQuery = MutableStateFlow("")
    val catalogSearchQuery: StateFlow<String> = _catalogSearchQuery.asStateFlow()

    // --- BOX DETAIL STATES ---
    private val _activeBoxDetail = MutableStateFlow<DettaglioBoxData?>(null)
    val activeBoxDetail: StateFlow<DettaglioBoxData?> = _activeBoxDetail.asStateFlow()

    private val _activeBoxDescriptionAi = MutableStateFlow<String?>(null)
    val activeBoxDescriptionAi: StateFlow<String?> = _activeBoxDescriptionAi.asStateFlow()

    private val _isLoadingBoxDetail = MutableStateFlow(false)
    val isLoadingBoxDetail: StateFlow<Boolean> = _isLoadingBoxDetail.asStateFlow()

    private val _isLoadingBoxDescriptionAi = MutableStateFlow(false)
    val isLoadingBoxDescriptionAi: StateFlow<Boolean> = _isLoadingBoxDescriptionAi.asStateFlow()

    private val _boxDetailError = MutableStateFlow<String?>(null)
    val boxDetailError: StateFlow<String?> = _boxDetailError.asStateFlow()

    // --- CART STATES ---
    private val _cartItems = MutableStateFlow<List<CarrelloItem>>(emptyList())
    val cartItems: StateFlow<List<CarrelloItem>> = _cartItems.asStateFlow()

    private val _cartTotalPrice = MutableStateFlow(0.0)
    val cartTotalPrice: StateFlow<Double> = _cartTotalPrice.asStateFlow()

    private val _isLoadingCart = MutableStateFlow(false)
    val isLoadingCart: StateFlow<Boolean> = _isLoadingCart.asStateFlow()

    private val _cartError = MutableStateFlow<String?>(null)
    val cartError: StateFlow<String?> = _cartError.asStateFlow()

    // --- CHECKOUT & ADDRESSES STATES ---
    private val _addresses = MutableStateFlow<List<Indirizzo>>(emptyList())
    val addresses: StateFlow<List<Indirizzo>> = _addresses.asStateFlow()

    private val _selectedAddressId = MutableStateFlow<Int?>(null)
    val selectedAddressId: StateFlow<Int?> = _selectedAddressId.asStateFlow()

    private val _isCheckoutLoading = MutableStateFlow(false)
    val isCheckoutLoading: StateFlow<Boolean> = _isCheckoutLoading.asStateFlow()

    private val _checkoutSuccessOrderCode = MutableStateFlow<String?>(null)
    val checkoutSuccessOrderCode: StateFlow<String?> = _checkoutSuccessOrderCode.asStateFlow()

    // --- USER ORDERS STATES ---
    private val _orders = MutableStateFlow<List<Ordine>>(emptyList())
    val orders: StateFlow<List<Ordine>> = _orders.asStateFlow()

    private val _isLoadingOrders = MutableStateFlow(false)
    val isLoadingOrders: StateFlow<Boolean> = _isLoadingOrders.asStateFlow()

    private val _ordersError = MutableStateFlow<String?>(null)
    val ordersError: StateFlow<String?> = _ordersError.asStateFlow()

    private val _expandedOrderId = MutableStateFlow<Int?>(null)
    val expandedOrderId: StateFlow<Int?> = _expandedOrderId.asStateFlow()

    private val _expandedOrderDetails = MutableStateFlow<List<OrdiniDettagliDTO>>(emptyList())
    val expandedOrderDetails: StateFlow<List<OrdiniDettagliDTO>> = _expandedOrderDetails.asStateFlow()

    private val _isLoadingOrderDetails = MutableStateFlow(false)
    val isLoadingOrderDetails: StateFlow<Boolean> = _isLoadingOrderDetails.asStateFlow()

    // --- AI CHEF RECOMMEND STATES ---
    private val _aiResult = MutableStateFlow<AiResult?>(null)
    val aiResult: StateFlow<AiResult?> = _aiResult.asStateFlow()

    private val _isLoadingAiRecommend = MutableStateFlow(false)
    val isLoadingAiRecommend: StateFlow<Boolean> = _isLoadingAiRecommend.asStateFlow()

    private val _aiRecommendError = MutableStateFlow<String?>(null)
    val aiRecommendError: StateFlow<String?> = _aiRecommendError.asStateFlow()

    init {
        // Load initial session on startup
        viewModelScope.launch {
            val token = sessionManager.getToken()
            if (token != null) {
                RetrofitClient.token = token
                _isLoggedIn.value = true
                _userEmail.value = sessionManager.getEmail()
                _userName.value = sessionManager.getName()
                fetchUserProfileAndData()
            }
        }
        // Load initial catalog list
        fetchCatalog()
    }

    // --- AUTH ACTIONS ---

    fun login(email: String, psw: String, onSuccess: () -> Unit) {
        _isAuthLoading.value = true
        _authError.value = null
        RetrofitClient.apiService.login(LoginRequest(email, psw)).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isAuthLoading.value = false
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    RetrofitClient.token = body.token
                    val displayName = body.nome ?: email.substringBefore("@")
                    sessionManager.saveSession(body.token, email, displayName)
                    _userEmail.value = email
                    _userName.value = displayName
                    _isLoggedIn.value = true
                    fetchUserProfileAndData()
                    onSuccess()
                } else {
                    _authError.value = "Credenziali non valide. Riprova."
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isAuthLoading.value = false
                _authError.value = "Impossibile connettersi al server: ${t.localizedMessage}"
            }
        })
    }

    fun register(request: RegistrazioneRequest, onSuccess: () -> Unit) {
        _isAuthLoading.value = true
        _authError.value = null
        RetrofitClient.apiService.registra(request).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                _isAuthLoading.value = false
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _authError.value = "Errore durante la registrazione. Forse l'email o il CF sono già registrati."
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _isAuthLoading.value = false
                _authError.value = "Errore di connessione: ${t.localizedMessage}"
            }
        })
    }

    fun logout(onLoggedOut: () -> Unit) {
        sessionManager.clearSession()
        RetrofitClient.token = null
        _isLoggedIn.value = false
        _userEmail.value = null
        _userName.value = null
        _userProfile.value = null
        _cartItems.value = emptyList()
        _cartTotalPrice.value = 0.0
        _orders.value = emptyList()
        _addresses.value = emptyList()
        _selectedAddressId.value = null
        onLoggedOut()
    }

    fun fetchUserProfileAndData() {
        if (!_isLoggedIn.value) return

        // Fetch profiling info
        RetrofitClient.apiService.getProfile().enqueue(object : Callback<DatiUtente> {
            override fun onResponse(call: Call<DatiUtente>, response: Response<DatiUtente>) {
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()
                    _userProfile.value = profile
                    _userName.value = profile?.nome
                }
            }
            override fun onFailure(call: Call<DatiUtente>, t: Throwable) {
                Log.e("YumasteVM", "Failed to fetch profile: ${t.localizedMessage}")
            }
        })

        // Preload Cart & Addresses
        fetchCart()
        fetchAddresses()
    }

    fun updateProfile(nome: String, cognome: String, email: String, onSuccess: () -> Unit) {
        _isAuthLoading.value = true
        RetrofitClient.apiService.updateProfile(UpdateProfileRequest(nome, cognome, email))
            .enqueue(object : Callback<DatiUtente> {
                override fun onResponse(call: Call<DatiUtente>, response: Response<DatiUtente>) {
                    _isAuthLoading.value = false
                    if (response.isSuccessful && response.body() != null) {
                        _userProfile.value = response.body()
                        _userEmail.value = email
                        _userName.value = nome
                        // Check if email was changed (web code says if email changed, require logging in again)
                        if (email != sessionManager.getEmail()) {
                            logout(onLoggedOut = {})
                        } else {
                            sessionManager.saveSession(RetrofitClient.token!!, email, nome)
                            onSuccess()
                        }
                    } else {
                        _authError.value = "Errore durante l'aggiornamento dei dati."
                    }
                }

                override fun onFailure(call: Call<DatiUtente>, t: Throwable) {
                    _isAuthLoading.value = false
                    _authError.value = "Errore di connessione."
                }
            })
    }

    fun changePassword(vecchia: String, nuova: String, onSuccess: () -> Unit) {
        _isAuthLoading.value = true
        RetrofitClient.apiService.updatePassword(UpdatePasswordRequest(vecchia, nuova))
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    _isAuthLoading.value = false
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        _authError.value = "Password attuale errata o non conforme."
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _isAuthLoading.value = false
                    _authError.value = "Errore di connessione."
                }
            })
    }

    // --- CATALOG ACTIONS ---

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
        _currentPage.value = 0
        fetchCatalog()
    }

    fun onSearchQueryChanged(query: String) {
        _catalogSearchQuery.value = query
        _currentPage.value = 0
        fetchCatalog()
    }

    fun onNextPage() {
        if (_currentPage.value < _totalPages.value - 1) {
            _currentPage.value += 1
            fetchCatalog()
        }
    }

    fun onPrevPage() {
        if (_currentPage.value > 0) {
            _currentPage.value -= 1
            fetchCatalog()
        }
    }

    fun fetchCatalog() {
        _isLoadingCatalog.value = true
        _catalogError.value = null

        val catFilter = if (_selectedCategory.value == "Tutte") null else _selectedCategory.value
        val searchQuery = if (_catalogSearchQuery.value.trim().isEmpty()) null else _catalogSearchQuery.value.trim()

        RetrofitClient.apiService.getBoxes(
            page = _currentPage.value,
            size = 8,
            categoria = catFilter,
            search = searchQuery
        ).enqueue(object : Callback<BoxPageResponse> {
            override fun onResponse(call: Call<BoxPageResponse>, response: Response<BoxPageResponse>) {
                _isLoadingCatalog.value = false
                if (response.code() == 204) {
                    _boxes.value = emptyList()
                    _totalPages.value = 0
                } else if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _boxes.value = body.content ?: emptyList()
                    _totalPages.value = body.totalPages ?: 1
                } else {
                    _catalogError.value = "Impossibile scaricare il catalogo."
                }
            }

            override fun onFailure(call: Call<BoxPageResponse>, t: Throwable) {
                _isLoadingCatalog.value = false
                _catalogError.value = "Errore di connessione al server: ${t.localizedMessage}"
            }
        })
    }

    // --- BOX DETAIL ACTIONS ---

    fun fetchBoxDetail(boxId: Int) {
        _isLoadingBoxDetail.value = true
        _activeBoxDetail.value = null
        _activeBoxDescriptionAi.value = null
        _boxDetailError.value = null

        // Fetch standard detail and AI cooking description in parallel
        RetrofitClient.apiService.getBoxDetail(boxId).enqueue(object : Callback<DettaglioBoxData> {
            override fun onResponse(call: Call<DettaglioBoxData>, response: Response<DettaglioBoxData>) {
                _isLoadingBoxDetail.value = false
                if (response.isSuccessful && response.body() != null) {
                    _activeBoxDetail.value = response.body()
                } else {
                    _boxDetailError.value = "Impossibile trovare questa Box."
                }
            }

            override fun onFailure(call: Call<DettaglioBoxData>, t: Throwable) {
                _isLoadingBoxDetail.value = false
                _boxDetailError.value = "Errore di connessione."
            }
        })

        _isLoadingBoxDescriptionAi.value = true
        RetrofitClient.apiService.getAiDescription(boxId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                _isLoadingBoxDescriptionAi.value = false
                if (response.isSuccessful && response.body() != null) {
                    val desc = response.body()!!.string()
                    _activeBoxDescriptionAi.value = desc.trim('\"') // remove potential surrounding string quotes
                } else {
                    _activeBoxDescriptionAi.value = "Una gustosa ed equilibrata Box pasto studiata dai nostri Chef del benessere."
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _isLoadingBoxDescriptionAi.value = false
                _activeBoxDescriptionAi.value = "Una gustosa ed equilibrata Box pasto studiata dai nostri Chef del benessere."
            }
        })
    }

    // --- CART ACTIONS ---

    fun fetchCart() {
        if (!_isLoggedIn.value) return
        _isLoadingCart.value = true
        _cartError.value = null

        RetrofitClient.apiService.getCart().enqueue(object : Callback<CarrelloResponse> {
            override fun onResponse(call: Call<CarrelloResponse>, response: Response<CarrelloResponse>) {
                _isLoadingCart.value = false
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _cartItems.value = body.items
                    _cartTotalPrice.value = body.totalPrice
                } else {
                    _cartError.value = "Impossibile caricare il carrello."
                }
            }

            override fun onFailure(call: Call<CarrelloResponse>, t: Throwable) {
                _isLoadingCart.value = false
                _cartError.value = "Errore di connessione."
                Log.e("YumasteVM", "Failed to fetch cart: ${t.localizedMessage}")
            }
        })
    }

    fun addBoxToCart(boxId: Int, quantita: Int = 1, onResult: (Boolean, String) -> Unit) {
        if (!_isLoggedIn.value) {
            onResult(false, "Fai l'accesso per aggiungere elementi al carrello!")
            return
        }

        RetrofitClient.apiService.addToCart(AddCartRequest(boxId, quantita)).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    fetchCart() // Refresh cart immediately
                    onResult(true, "Aggiunto al carrello! 🛒")
                } else {
                    onResult(false, "Impossibile aggiungere l'articolo.")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResult(false, "Errore di rete.")
            }
        })
    }

    fun updateCartItemQuantity(boxId: Int, newQuantity: Int) {
        if (newQuantity < 1) return
        RetrofitClient.apiService.updateCartQuantity(UpdateCartRequest(boxId, newQuantity))
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        fetchCart()
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("YumasteVM", "Failed to update quantity")
                }
            })
    }

    fun removeCartItem(boxId: Int) {
        RetrofitClient.apiService.removeFromCart(boxId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    fetchCart()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("YumasteVM", "Failed to remove item")
            }
        })
    }

    // --- CHECKOUT & ADDRESS ACTIONS ---

    fun fetchAddresses() {
        if (!_isLoggedIn.value) return

        RetrofitClient.apiService.getAddresses().enqueue(object : Callback<List<Indirizzo>> {
            override fun onResponse(call: Call<List<Indirizzo>>, response: Response<List<Indirizzo>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!
                    _addresses.value = list
                    if (list.isNotEmpty() && _selectedAddressId.value == null) {
                        _selectedAddressId.value = list[0].id
                    }
                }
            }

            override fun onFailure(call: Call<List<Indirizzo>>, t: Throwable) {
                Log.e("YumasteVM", "Failed to fetch addresses")
            }
        })
    }

    fun selectAddress(id: Int) {
        _selectedAddressId.value = id
    }

    fun addNewAddress(via: String, civico: String, cap: String, citta: String, provincia: String, onSuccess: () -> Unit) {
        val newAddr = Indirizzo(via = via, civico = civico, cap = cap, citta = citta, provincia = provincia, note = "")
        RetrofitClient.apiService.addAddress(newAddr).enqueue(object : Callback<Indirizzo> {
            override fun onResponse(call: Call<Indirizzo>, response: Response<Indirizzo>) {
                if (response.isSuccessful && response.body() != null) {
                    val added = response.body()!!
                    _addresses.value = _addresses.value + added
                    _selectedAddressId.value = added.id
                    onSuccess()
                } else {
                    // Try to re-fetch standard list if we succeeded but the specific response parse failed
                    fetchAddresses()
                    onSuccess()
                }
            }

            override fun onFailure(call: Call<Indirizzo>, t: Throwable) {
                // Sometime the API actually adds it but returns a redirect / empty ResponseBody for serialization.
                // Let's re-fetch the list as fallback to maximize robustness.
                fetchAddresses()
                onSuccess()
            }
        })
    }

    fun executeCheckout(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val addressId = _selectedAddressId.value
        if (addressId == null) {
            onError("Seleziona o immetti prima un indirizzo di spedizione.")
            return
        }

        _isCheckoutLoading.value = true
        _checkoutSuccessOrderCode.value = null

        RetrofitClient.apiService.checkout(CheckoutRequest(indirizzoId = addressId))
            .enqueue(object : Callback<CheckoutResponse> {
                override fun onResponse(call: Call<CheckoutResponse>, response: Response<CheckoutResponse>) {
                    _isCheckoutLoading.value = false
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        _checkoutSuccessOrderCode.value = body.codiceOrdine
                        _cartItems.value = emptyList()
                        _cartTotalPrice.value = 0.0
                        onSuccess(body.codiceOrdine)
                    } else {
                        onError("Errore durante il checkout. Riprova più tardi.")
                    }
                }

                override fun onFailure(call: Call<CheckoutResponse>, t: Throwable) {
                    _isCheckoutLoading.value = false
                    onError("Impossibile connettersi al server per inoltrare l'ordine.")
                }
            })
    }

    // --- USER ORDERS ACTIONS ---

    fun fetchOrders() {
        if (!_isLoggedIn.value) return
        _isLoadingOrders.value = true
        _ordersError.value = null

        RetrofitClient.apiService.getOrders().enqueue(object : Callback<List<Ordine>> {
            override fun onResponse(call: Call<List<Ordine>>, response: Response<List<Ordine>>) {
                _isLoadingOrders.value = false
                if (response.isSuccessful && response.body() != null) {
                    _orders.value = response.body()!!
                } else {
                    _ordersError.value = "Errore nel caricamento degli ordini."
                }
            }

            override fun onFailure(call: Call<List<Ordine>>, t: Throwable) {
                _isLoadingOrders.value = false
                _ordersError.value = "Errore di connessione."
            }
        })
    }

    fun toggleOrderExpansion(orderId: Int) {
        if (_expandedOrderId.value == orderId) {
            _expandedOrderId.value = null
            _expandedOrderDetails.value = emptyList()
            return
        }

        _expandedOrderId.value = orderId
        _isLoadingOrderDetails.value = true
        _expandedOrderDetails.value = emptyList()

        RetrofitClient.apiService.getOrderDetails(orderId).enqueue(object : Callback<List<OrdiniDettagliDTO>> {
            override fun onResponse(
                call: Call<List<OrdiniDettagliDTO>>,
                response: Response<List<OrdiniDettagliDTO>>
            ) {
                _isLoadingOrderDetails.value = false
                if (response.isSuccessful && response.body() != null) {
                    _expandedOrderDetails.value = response.body()!!
                } else {
                    _expandedOrderId.value = null
                }
            }

            override fun onFailure(call: Call<List<OrdiniDettagliDTO>>, t: Throwable) {
                _isLoadingOrderDetails.value = false
                _expandedOrderId.value = null
            }
        })
    }

    // --- AI RECOMMEND ACTIONS ---

    fun clearAiResult() {
        _aiResult.value = null
    }

    fun requestAiRecommend(obiettivo: String, tipoDieta: String, calorie: Int, allergeni: List<String>) {
        _isLoadingAiRecommend.value = true
        _aiRecommendError.value = null
        _aiResult.value = null

        RetrofitClient.apiService.getAiRecommend(
            AiRecommendRequest(
                obiettivo = obiettivo,
                tipoDieta = tipoDieta,
                calorieGiornaliere = calorie,
                allergeni = allergeni
            )
        ).enqueue(object : Callback<AiResult> {
            override fun onResponse(call: Call<AiResult>, response: Response<AiResult>) {
                _isLoadingAiRecommend.value = false
                if (response.isSuccessful && response.body() != null) {
                    _aiResult.value = response.body()!!
                } else {
                    _aiRecommendError.value = "Impossibile calcolare una raccomandazione. Riprova."
                }
            }

            override fun onFailure(call: Call<AiResult>, t: Throwable) {
                _isLoadingAiRecommend.value = false
                _aiRecommendError.value = "Impossibile contattare l'Assistente AI."
            }
        })
    }
}
