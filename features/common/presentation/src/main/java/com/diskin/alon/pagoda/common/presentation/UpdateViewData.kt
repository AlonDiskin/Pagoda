package com.diskin.alon.pagoda.common.presentation

sealed class UpdateViewData {

    object EndRefresh : UpdateViewData()

    object Refresh : UpdateViewData()
}