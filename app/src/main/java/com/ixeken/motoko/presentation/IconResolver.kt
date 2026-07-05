package com.ixeken.motoko.presentation

import com.composables.icons.lucide.R

/**
 * Resuelve el recurso drawable de Lucide correspondiente al nombre de icono almacenado.
 * Cubre todos los iconos del catálogo curado de la pantalla de selección.
 */
fun resolveIconRes(iconName: String): Int = when (iconName) {
    // Dinero y finanzas
    "Dollar" -> R.drawable.lucide_ic_circle_dollar_sign
    "Wallet" -> R.drawable.lucide_ic_wallet
    "CreditCard" -> R.drawable.lucide_ic_credit_card
    "Receipt" -> R.drawable.lucide_ic_receipt
    "Coins" -> R.drawable.lucide_ic_coins
    "Banknote" -> R.drawable.lucide_ic_banknote
    "TrendingUp" -> R.drawable.lucide_ic_trending_up
    "TrendingDown" -> R.drawable.lucide_ic_trending_down
    "Percent" -> R.drawable.lucide_ic_percent
    "Tag" -> R.drawable.lucide_ic_tag
    "Briefcase" -> R.drawable.lucide_ic_briefcase
    "Store" -> R.drawable.lucide_ic_store

    // Comida y bebida
    "Soup" -> R.drawable.lucide_ic_soup
    "Pizza" -> R.drawable.lucide_ic_pizza
    "Apple" -> R.drawable.lucide_ic_apple
    "Coffee" -> R.drawable.lucide_ic_coffee
    "Wine" -> R.drawable.lucide_ic_wine
    "Beer" -> R.drawable.lucide_ic_beer
    "Utensils" -> R.drawable.lucide_ic_utensils
    "LeafyGreen" -> R.drawable.lucide_ic_leafy_green

    // Transporte
    "Train" -> R.drawable.lucide_ic_train_front
    "Car" -> R.drawable.lucide_ic_car
    "Bus" -> R.drawable.lucide_ic_bus
    "Bike" -> R.drawable.lucide_ic_bike
    "Plane" -> R.drawable.lucide_ic_plane
    "Ship" -> R.drawable.lucide_ic_ship
    "Fuel" -> R.drawable.lucide_ic_fuel
    "Truck" -> R.drawable.lucide_ic_truck
    "Navigation" -> R.drawable.lucide_ic_navigation

    // Hogar y servicios
    "House" -> R.drawable.lucide_ic_house
    "Plug" -> R.drawable.lucide_ic_plug
    "Wifi" -> R.drawable.lucide_ic_wifi
    "Lamp" -> R.drawable.lucide_ic_lamp
    "Key" -> R.drawable.lucide_ic_key
    "Wrench" -> R.drawable.lucide_ic_wrench
    "Hammer" -> R.drawable.lucide_ic_hammer
    "Warehouse" -> R.drawable.lucide_ic_warehouse

    // Entretenimiento
    "Ticket" -> R.drawable.lucide_ic_ticket
    "Gamepad" -> R.drawable.lucide_ic_gamepad_2
    "Music" -> R.drawable.lucide_ic_music
    "Film" -> R.drawable.lucide_ic_film
    "Headphones" -> R.drawable.lucide_ic_headphones
    "Tv" -> R.drawable.lucide_ic_tv
    "Trophy" -> R.drawable.lucide_ic_trophy
    "Medal" -> R.drawable.lucide_ic_medal

    // Compras
    "ShoppingCart" -> R.drawable.lucide_ic_shopping_cart
    "ShoppingBag" -> R.drawable.lucide_ic_shopping_bag
    "Gift" -> R.drawable.lucide_ic_gift
    "Package" -> R.drawable.lucide_ic_package
    "Gem" -> R.drawable.lucide_ic_gem
    "Scissors" -> R.drawable.lucide_ic_scissors

    // Salud y bienestar
    "Heart" -> R.drawable.lucide_ic_heart
    "Hospital" -> R.drawable.lucide_ic_hospital
    "Pill" -> R.drawable.lucide_ic_pill
    "Stethoscope" -> R.drawable.lucide_ic_stethoscope
    "Dumbbell" -> R.drawable.lucide_ic_dumbbell
    "Thermometer" -> R.drawable.lucide_ic_thermometer

    // Educación y trabajo
    "GraduationCap" -> R.drawable.lucide_ic_graduation_cap
    "Book" -> R.drawable.lucide_ic_book_open
    "School" -> R.drawable.lucide_ic_school
    "Pen" -> R.drawable.lucide_ic_pen
    "Laptop" -> R.drawable.lucide_ic_laptop
    "Monitor" -> R.drawable.lucide_ic_monitor
    "Phone" -> R.drawable.lucide_ic_phone
    "Printer" -> R.drawable.lucide_ic_printer

    // Naturaleza y viajes
    "Globe" -> R.drawable.lucide_ic_globe
    "Mountain" -> R.drawable.lucide_ic_mountain
    "Waves" -> R.drawable.lucide_ic_waves
    "Umbrella" -> R.drawable.lucide_ic_umbrella
    "Sun" -> R.drawable.lucide_ic_sun
    "Tent" -> R.drawable.lucide_ic_tent
    "Sailboat" -> R.drawable.lucide_ic_sailboat
    "MapPin" -> R.drawable.lucide_ic_map_pin

    // Personas y mascotas
    "User" -> R.drawable.lucide_ic_user
    "Users" -> R.drawable.lucide_ic_users
    "Baby" -> R.drawable.lucide_ic_baby
    "Cat" -> R.drawable.lucide_ic_cat
    "Dog" -> R.drawable.lucide_ic_dog
    "Fish" -> R.drawable.lucide_ic_fish

    // Utilidades
    "Folder" -> R.drawable.lucide_ic_folder_code
    "Camera" -> R.drawable.lucide_ic_camera
    "Shield" -> R.drawable.lucide_ic_shield
    "Lock" -> R.drawable.lucide_ic_lock
    "Star" -> R.drawable.lucide_ic_star
    "Zap" -> R.drawable.lucide_ic_zap
    "Flame" -> R.drawable.lucide_ic_flame
    "Rocket" -> R.drawable.lucide_ic_rocket
    "Flower" -> R.drawable.lucide_ic_flower

    // Nuevas adiciones (31 iconos)
    "PiggyBank" -> R.drawable.lucide_ic_piggy_bank
    "Calculator" -> R.drawable.lucide_ic_calculator
    "ChartPie" -> R.drawable.lucide_ic_chart_pie
    "ChartBar" -> R.drawable.lucide_ic_chart_bar
    "Landmark" -> R.drawable.lucide_ic_landmark
    "Cake" -> R.drawable.lucide_ic_cake
    "Cookie" -> R.drawable.lucide_ic_cookie
    "Popcorn" -> R.drawable.lucide_ic_popcorn
    "GlassWater" -> R.drawable.lucide_ic_glass_water
    "Clapperboard" -> R.drawable.lucide_ic_clapperboard
    "Crown" -> R.drawable.lucide_ic_crown
    "Mic" -> R.drawable.lucide_ic_mic
    "Radio" -> R.drawable.lucide_ic_radio
    "Shirt" -> R.drawable.lucide_ic_shirt
    "Footprints" -> R.drawable.lucide_ic_footprints
    "Bed" -> R.drawable.lucide_ic_bed
    "Siren" -> R.drawable.lucide_ic_siren
    "Trash2" -> R.drawable.lucide_ic_trash_2
    "Smartphone" -> R.drawable.lucide_ic_smartphone
    "Mail" -> R.drawable.lucide_ic_mail
    "Activity" -> R.drawable.lucide_ic_activity
    "TreePine" -> R.drawable.lucide_ic_tree_pine
    "Cloud" -> R.drawable.lucide_ic_cloud
    "Leaf" -> R.drawable.lucide_ic_leaf
    "Luggage" -> R.drawable.lucide_ic_luggage
    "Compass" -> R.drawable.lucide_ic_compass
    "Binoculars" -> R.drawable.lucide_ic_binoculars
    "HeartPulse" -> R.drawable.lucide_ic_heart_pulse
    "Sparkles" -> R.drawable.lucide_ic_sparkles
    "Brush" -> R.drawable.lucide_ic_brush

    // Legados de versiones anteriores
    "Settings" -> R.drawable.lucide_ic_settings
    "Palette" -> R.drawable.lucide_ic_palette
    "Type" -> R.drawable.lucide_ic_type
    "FileText" -> R.drawable.lucide_ic_file_text
    "Refresh" -> R.drawable.lucide_ic_refresh_cw
    "UserPlus" -> R.drawable.lucide_ic_user_plus
    "Search" -> R.drawable.lucide_ic_search
    "Plus" -> R.drawable.lucide_ic_plus

    else -> R.drawable.lucide_ic_folder_code
}

val curatedIcons: List<Pair<String, Int>> = listOf(
    // Dinero y finanzas
    Pair("Dollar", R.drawable.lucide_ic_circle_dollar_sign),
    Pair("Wallet", R.drawable.lucide_ic_wallet),
    Pair("CreditCard", R.drawable.lucide_ic_credit_card),
    Pair("Receipt", R.drawable.lucide_ic_receipt),
    Pair("Coins", R.drawable.lucide_ic_coins),
    Pair("Banknote", R.drawable.lucide_ic_banknote),
    Pair("TrendingUp", R.drawable.lucide_ic_trending_up),
    Pair("TrendingDown", R.drawable.lucide_ic_trending_down),
    Pair("Percent", R.drawable.lucide_ic_percent),
    Pair("Tag", R.drawable.lucide_ic_tag),
    Pair("Briefcase", R.drawable.lucide_ic_briefcase),
    Pair("Store", R.drawable.lucide_ic_store),

    // Comida y bebida
    Pair("Soup", R.drawable.lucide_ic_soup),
    Pair("Pizza", R.drawable.lucide_ic_pizza),
    Pair("Apple", R.drawable.lucide_ic_apple),
    Pair("Coffee", R.drawable.lucide_ic_coffee),
    Pair("Wine", R.drawable.lucide_ic_wine),
    Pair("Beer", R.drawable.lucide_ic_beer),
    Pair("Utensils", R.drawable.lucide_ic_utensils),
    Pair("LeafyGreen", R.drawable.lucide_ic_leafy_green),

    // Transporte
    Pair("Train", R.drawable.lucide_ic_train_front),
    Pair("Car", R.drawable.lucide_ic_car),
    Pair("Bus", R.drawable.lucide_ic_bus),
    Pair("Bike", R.drawable.lucide_ic_bike),
    Pair("Plane", R.drawable.lucide_ic_plane),
    Pair("Ship", R.drawable.lucide_ic_ship),
    Pair("Fuel", R.drawable.lucide_ic_fuel),
    Pair("Truck", R.drawable.lucide_ic_truck),
    Pair("Navigation", R.drawable.lucide_ic_navigation),

    // Hogar y servicios
    Pair("House", R.drawable.lucide_ic_house),
    Pair("Plug", R.drawable.lucide_ic_plug),
    Pair("Wifi", R.drawable.lucide_ic_wifi),
    Pair("Lamp", R.drawable.lucide_ic_lamp),
    Pair("Key", R.drawable.lucide_ic_key),
    Pair("Wrench", R.drawable.lucide_ic_wrench),
    Pair("Hammer", R.drawable.lucide_ic_hammer),
    Pair("Warehouse", R.drawable.lucide_ic_warehouse),

    // Entretenimiento
    Pair("Ticket", R.drawable.lucide_ic_ticket),
    Pair("Gamepad", R.drawable.lucide_ic_gamepad_2),
    Pair("Music", R.drawable.lucide_ic_music),
    Pair("Film", R.drawable.lucide_ic_film),
    Pair("Headphones", R.drawable.lucide_ic_headphones),
    Pair("Tv", R.drawable.lucide_ic_tv),
    Pair("Trophy", R.drawable.lucide_ic_trophy),
    Pair("Medal", R.drawable.lucide_ic_medal),

    // Compras
    Pair("ShoppingCart", R.drawable.lucide_ic_shopping_cart),
    Pair("ShoppingBag", R.drawable.lucide_ic_shopping_bag),
    Pair("Gift", R.drawable.lucide_ic_gift),
    Pair("Package", R.drawable.lucide_ic_package),
    Pair("Gem", R.drawable.lucide_ic_gem),
    Pair("Scissors", R.drawable.lucide_ic_scissors),

    // Salud y bienestar
    Pair("Heart", R.drawable.lucide_ic_heart),
    Pair("Hospital", R.drawable.lucide_ic_hospital),
    Pair("Pill", R.drawable.lucide_ic_pill),
    Pair("Stethoscope", R.drawable.lucide_ic_stethoscope),
    Pair("Dumbbell", R.drawable.lucide_ic_dumbbell),
    Pair("Thermometer", R.drawable.lucide_ic_thermometer),

    // Educación y trabajo
    Pair("GraduationCap", R.drawable.lucide_ic_graduation_cap),
    Pair("Book", R.drawable.lucide_ic_book_open),
    Pair("School", R.drawable.lucide_ic_school),
    Pair("Pen", R.drawable.lucide_ic_pen),
    Pair("Laptop", R.drawable.lucide_ic_laptop),
    Pair("Monitor", R.drawable.lucide_ic_monitor),
    Pair("Phone", R.drawable.lucide_ic_phone),
    Pair("Printer", R.drawable.lucide_ic_printer),

    // Naturaleza y viajes
    Pair("Globe", R.drawable.lucide_ic_globe),
    Pair("Mountain", R.drawable.lucide_ic_mountain),
    Pair("Waves", R.drawable.lucide_ic_waves),
    Pair("Umbrella", R.drawable.lucide_ic_umbrella),
    Pair("Sun", R.drawable.lucide_ic_sun),
    Pair("Tent", R.drawable.lucide_ic_tent),
    Pair("Sailboat", R.drawable.lucide_ic_sailboat),
    Pair("MapPin", R.drawable.lucide_ic_map_pin),

    // Personas y mascotas
    Pair("User", R.drawable.lucide_ic_user),
    Pair("Users", R.drawable.lucide_ic_users),
    Pair("Baby", R.drawable.lucide_ic_baby),
    Pair("Cat", R.drawable.lucide_ic_cat),
    Pair("Dog", R.drawable.lucide_ic_dog),
    Pair("Fish", R.drawable.lucide_ic_fish),

    // Utilidades
    Pair("Folder", R.drawable.lucide_ic_folder_code),
    Pair("Camera", R.drawable.lucide_ic_camera),
    Pair("Shield", R.drawable.lucide_ic_shield),
    Pair("Lock", R.drawable.lucide_ic_lock),
    Pair("Star", R.drawable.lucide_ic_star),
    Pair("Zap", R.drawable.lucide_ic_zap),
    Pair("Flame", R.drawable.lucide_ic_flame),
    Pair("Rocket", R.drawable.lucide_ic_rocket),
    Pair("Flower", R.drawable.lucide_ic_flower),

    // Nuevas adiciones (31 iconos)
    Pair("PiggyBank", R.drawable.lucide_ic_piggy_bank),
    Pair("Calculator", R.drawable.lucide_ic_calculator),
    Pair("ChartPie", R.drawable.lucide_ic_chart_pie),
    Pair("ChartBar", R.drawable.lucide_ic_chart_bar),
    Pair("Landmark", R.drawable.lucide_ic_landmark),
    Pair("Cake", R.drawable.lucide_ic_cake),
    Pair("Cookie", R.drawable.lucide_ic_cookie),
    Pair("Popcorn", R.drawable.lucide_ic_popcorn),
    Pair("GlassWater", R.drawable.lucide_ic_glass_water),
    Pair("Clapperboard", R.drawable.lucide_ic_clapperboard),
    Pair("Crown", R.drawable.lucide_ic_crown),
    Pair("Mic", R.drawable.lucide_ic_mic),
    Pair("Radio", R.drawable.lucide_ic_radio),
    Pair("Shirt", R.drawable.lucide_ic_shirt),
    Pair("Footprints", R.drawable.lucide_ic_footprints),
    Pair("Bed", R.drawable.lucide_ic_bed),
    Pair("Siren", R.drawable.lucide_ic_siren),
    Pair("Trash2", R.drawable.lucide_ic_trash_2),
    Pair("Smartphone", R.drawable.lucide_ic_smartphone),
    Pair("Mail", R.drawable.lucide_ic_mail),
    Pair("Activity", R.drawable.lucide_ic_activity),
    Pair("TreePine", R.drawable.lucide_ic_tree_pine),
    Pair("Cloud", R.drawable.lucide_ic_cloud),
    Pair("Leaf", R.drawable.lucide_ic_leaf),
    Pair("Luggage", R.drawable.lucide_ic_luggage),
    Pair("Compass", R.drawable.lucide_ic_compass),
    Pair("Binoculars", R.drawable.lucide_ic_binoculars),
    Pair("HeartPulse", R.drawable.lucide_ic_heart_pulse),
    Pair("Sparkles", R.drawable.lucide_ic_sparkles),
    Pair("Brush", R.drawable.lucide_ic_brush)
)
