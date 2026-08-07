package pfa.redouaneachak.securescope.data.model

data class PermissionInfo(
    val name: String,
    val label: String,
    val isGranted: Boolean,
    val isDangerous: Boolean
)