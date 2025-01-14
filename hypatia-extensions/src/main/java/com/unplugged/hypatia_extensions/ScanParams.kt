package com.unplugged.hypatia_extensions

data class ScanParams(val scanSystem: Boolean = true, val scanApps: Boolean = true, val scanInternal: Boolean = true, val scanExternal: Boolean = true)
