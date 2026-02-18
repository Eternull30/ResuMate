package com.example.resumebuilder.ui.screen.resume

sealed class ResumeTemplate (val route: String){
    object Modern : ResumeTemplate("modern")
    object Minimal : ResumeTemplate("minimal")
}