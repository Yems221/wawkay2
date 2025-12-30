package com.kufay.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kufay.app.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin

    private val _confirmPin = MutableStateFlow("")
    private val _pinState = MutableStateFlow<PinState>(PinState.Input)
    val pinState: StateFlow<PinState> = _pinState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // ✨ MODIFIÉ : isChangingPin devient observable
    private val _isChangingPin = MutableStateFlow(false)
    val isChangingPin: StateFlow<Boolean> = _isChangingPin.asStateFlow()

    init {
        viewModelScope.launch {
            val isPinEnabled = userPreferences.pinEnabled.first()
            if (isPinEnabled) {
                // If PIN is enabled, show login screen
                _pinState.value = PinState.Login
            } else {
                // If PIN is not enabled, force PIN setup
                // This ensures PIN is always required
                _pinState.value = PinState.Setup
            }
        }
    }

    // ✨ MODIFIÉ : Utilise _isChangingPin.value
    fun setChangingPin() {
        _isChangingPin.value = true
        _pinState.value = PinState.Setup
        _pin.value = ""
        _confirmPin.value = ""
        _errorMessage.value = null
    }

    fun appendDigit(digit: Char) {
        if (_pin.value.length < 4) {
            _pin.value += digit
        }
    }

    fun deleteDigit() {
        if (_pin.value.isNotEmpty()) {
            _pin.value = _pin.value.dropLast(1)
        }
    }

    fun clearPin() {
        _pin.value = ""
        _errorMessage.value = null
    }

    fun checkPin() {
        viewModelScope.launch {
            when (pinState.value) {
                is PinState.Login -> {
                    if (_pin.value.length == 4) {
                        val isValid = userPreferences.verifyPin(_pin.value)
                        if (isValid) {
                            _pinState.value = PinState.Authenticated
                            // ✨ AJOUTÉ : Reset isChangingPin après login normal
                            _isChangingPin.value = false
                        } else {
                            _errorMessage.value = "Code PIN incorrect. Veuillez réessayer."
                            clearPin()
                        }
                    } else {
                        _errorMessage.value = "Veuillez entrer les 4 chiffres du PIN"
                    }
                }
                is PinState.Setup -> {
                    if (_pin.value.length == 4) {
                        _confirmPin.value = _pin.value
                        clearPin()
                        _pinState.value = PinState.Confirm
                    } else {
                        _errorMessage.value = "Veuillez entrer 4 chiffres"
                    }
                }
                is PinState.Confirm -> {
                    if (_pin.value.length == 4) {
                        if (_pin.value == _confirmPin.value) {
                            userPreferences.setPin(_pin.value)
                            _pinState.value = PinState.Authenticated
                            // ✨ isChangingPin reste true ici (animation avec texte)
                            // Il sera reset après l'animation
                        } else {
                            _errorMessage.value = "Les codes PIN ne correspondent pas. Veuillez réessayer."
                            clearPin()
                            _confirmPin.value = ""
                            _pinState.value = PinState.Setup
                        }
                    } else {
                        _errorMessage.value = "Veuillez entrer 4 chiffres"
                    }
                }
                else -> {}
            }
        }
    }

    fun setupNewPin() {
        _pinState.value = PinState.Setup
        _pin.value = ""
        _confirmPin.value = ""
        _errorMessage.value = null
    }

    sealed class PinState {
        object Input : PinState()
        object Setup : PinState()
        object Confirm : PinState()
        object Login : PinState()
        object Authenticated : PinState()
    }
}