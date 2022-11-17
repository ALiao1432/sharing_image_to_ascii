package com.redso.sharing_image_to_ascii.viewmodel

import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    var handleShotButtonViewClicked: (() -> Unit)? = null
}