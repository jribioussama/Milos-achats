package com.example.milos_achats.data

fun confirmedServerKey(dayIndex: Int, weekId: String) = "${weekId}_CONFIRMED_SERVER_d${dayIndex}"

// Supplier IDs identiques aux autres modules (wissem, slim, attar) pour fusion future
// Product IDs préfixés "s_" pour éviter toute collision

val SERVER_SUPPLIERS: List<SupplierSection> = listOf(

    SupplierSection(
        id = "wissem",
        name = "WISSEM MCM",
        deliveryInfo = "Livraison",
        products = listOf(
            BarProduct("s_wissem_01", "Papier serviette", "مناديل", "1 carton"),
            BarProduct("s_wissem_02", "Rouleaux de caisse", "لفافات كاشير", "1 carton"),
            BarProduct("s_wissem_03", "Papier zigzag", "ورق زيكزاك", "1 carton"),
            BarProduct("s_wissem_04", "Papier toilette", "ورق تواليت", "1 carton"),
        )
    ),

    SupplierSection(
        id = "slim",
        name = "SLIM NETTOYAGE",
        deliveryInfo = "Livraison",
        products = listOf(
            BarProduct("s_slim_01", "Grisé", "منظف الأرضية", "1 bidon"),
            BarProduct("s_slim_02", "Air fraîche", "معطر جو", "1 bidon"),
            BarProduct("s_slim_03", "Dinol", "منظف ديكول", "1 bidon"),
            BarProduct("s_slim_04", "Javel", "جافيل", "1 bidon"),
        )
    ),

    SupplierSection(
        id = "attar",
        name = "عطار",
        deliveryInfo = "Mohamed",
        products = listOf(
            BarProduct("s_attar_01", "Hakaket Ma3oun", "هاقيكة ماعون", "4"),
        )
    ),

    SupplierSection(
        id = "marche_food",
        name = "Marché Food",
        deliveryInfo = "Mohamed",
        products = listOf(
            BarProduct("s_marche_food_01", "Pistache Roxella", "فستق روكسيلا", "1"),
        )
    ),

    SupplierSection(
        id = "banque",
        name = "Banque",
        deliveryInfo = "Mohamed",
        products = listOf(
            BarProduct("s_banque_01", "Rouleaux TPE", "لفافات TPE", "1"),
        )
    ),
)
