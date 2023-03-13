package com.example.newsapp.viewmodels

sealed interface ScreenNavigation {
    object Back : ScreenNavigation
}