package ru.ddp.lab4

data class Laptop(
    val id: Int = 0, // Default value for new entries (must be auto-incremented)
    val operatingSystem: String,
    val mass: Double, // kg
    val has3G: Boolean,
    val screenDiagonal: Double, // inch
    val ram: Int, // GB
    val batteryLife: Int //mAh
)