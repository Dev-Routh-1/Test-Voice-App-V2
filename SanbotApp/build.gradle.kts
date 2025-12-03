plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // removed kotlin.compose plugin alias - this project doesn't need org.jetbrains.kotlin.compose plugin
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
}
