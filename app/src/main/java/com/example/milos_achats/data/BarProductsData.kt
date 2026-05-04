package com.example.milos_achats.data

data class BarProduct(
    val id: String,
    val nameFr: String,
    val nameAr: String,
    val quantity: String,
)

data class SupplierSection(
    val id: String,
    val name: String,
    val deliveryInfo: String,
    val products: List<BarProduct>,
)

val BAR_SUPPLIERS: List<SupplierSection> = listOf(
    SupplierSection(
        id = "chariot",
        name = "Chariot",
        deliveryInfo = "Livraison",
        products = listOf(
            BarProduct("chariot_01", "Nescafé Grand ou buchette", "نسكافيه كبير", "1"),
            BarProduct("chariot_02", "Chocoline", "شوكولين", "3"),
            BarProduct("chariot_03", "Cappuccino", "كابتشينو", "3"),
            BarProduct("chariot_04", "Matcha", "ماتشا", "1"),
            BarProduct("chariot_05", "Kyufi sans sucre", "كيوفي بدون سكر", "2"),
            BarProduct("chariot_06", "Kyufi sucre", "كيوفي بالسكر", "2"),
            BarProduct("chariot_07", "Capsules Nespresso mauve", "كبسولات نسبريسو بنفسجي", "3"),
            BarProduct("chariot_08", "Eau Garsi (stika)", "ماء قارصي", "2"),
            BarProduct("chariot_09", "Purée mangue monin", "بيوري مانغو", "1"),
            BarProduct("chariot_10", "Purée pêche monin", "بيوري خوخ", "1"),
            BarProduct("chariot_11", "Purée strawberry monin", "بيوري فراولة", "1"),
            BarProduct("chariot_12", "Purée mixed berries monin", "بيوري فواكه مشكلة", "1"),
            BarProduct("chariot_13", "Purée noix de coco monin", "بيوري جوز الهند", "1"),
            BarProduct("chariot_14", "Arôme noisette monin", "نكهة بندق", "1"),
            BarProduct("chariot_15", "Arôme vanille monin", "نكهة فانيليا", "1"),
            BarProduct("chariot_16", "Arôme caramel monin", "نكهة كراميل", "1"),
            BarProduct("chariot_17", "Sirop bleu monin", "شراب أزرق", "1"),
            BarProduct("chariot_18", "Sirop mojito monin", "شراب موهيتو", "1"),
            BarProduct("chariot_19", "Sirop fruits rouges monin", "شراب فواكه حمراء", "1"),
            BarProduct("chariot_20", "Ananas boîte monin", "أناناس معلب", "2"),
            BarProduct("chariot_21", "Crème à fouetter avec sucre", "كريمة خفق", "3"),
            BarProduct("chariot_22", "Chocolat chaud", "شوكولاتة ساخنة", "3"),
            BarProduct("chariot_23", "Lotus pâte", "معجون لوتس", "1"),
            BarProduct("chariot_24", "Miel STAL", "عسل", "1"),
            BarProduct("chariot_25", "Nestlé 1/2 LITRE", "نستلي", "3"),
            BarProduct("chariot_26", "Pistache Roxella", "فستق روكسيلا", "1"),
            BarProduct("chariot_27", "Sirop menthe (THE)", "شراب نعناع", "2"),
            BarProduct("chariot_28", "Biscuit Lotus grand", "بسكويت لوتس كبير", "2"),
            BarProduct("chariot_29", "Gants noir paquet", "علبة قفازات سوداء", "1"),
            BarProduct("chariot_30", "Ajax (bidon)", "منظف أجاكس", "1"),
            BarProduct("chariot_31", "Sacs poubelles 90/100", "أكياس قمامة", "3"),
            BarProduct("chariot_32", "Yaourt arabe", "ياغورت عربي", "2"),
            BarProduct("chariot_33", "Orange séchée", "برتقال مجفف", "300g"),
            BarProduct("chariot_34", "Amande concassée", "لوز مجروش", "300g"),
            BarProduct("chariot_35", "Pistache concassée", "فستق مجروش", "300g"),
            BarProduct("chariot_36", "Noisette pelée", "بندق مقشر", "300g"),
            BarProduct("chariot_37", "Cajou", "كاجو", "300g"),
            BarProduct("chariot_38", "Jus ananas delice", "عصير أناناس", "4"),
            BarProduct("chariot_39", "Jus orange delice", "عصير برتقال", "4"),
            BarProduct("chariot_40", "Jus mangue delice", "عصير مانغو", "4"),
            BarProduct("chariot_41", "Yaourt GRECOS", "ياغورت غريكوس", "8"),
            BarProduct("chariot_42", "Thé vert", "شاي أخضر", "10"),
            BarProduct("chariot_43", "Carton cookies", "علبة كوكيز", "1"),
            BarProduct("chariot_44", "Carton brownies", "علبة براونيز", "1"),
            BarProduct("chariot_45", "Sucre", "سكر", "10kg"),
            BarProduct("chariot_46", "Mangue surgelée", "مانغو مجمد", "2kg"),
            BarProduct("chariot_47", "Fraise surgelée", "فراولة مجمدة", "10kg"),
            BarProduct("chariot_48", "Lait (stika)", "حليب", "2"),
            BarProduct("chariot_49", "Granola", "غرانولا", "500g"),
        )
    ),
    SupplierSection(
        id = "wissem",
        name = "WISSEM MCM",
        deliveryInfo = "Livraison",
        products = listOf(
            BarProduct("wissem_01", "Eau 1L verre", "ماء 1 لتر زجاج", "1"),
            BarProduct("wissem_02", "Eau 1L plastique", "ماء 1 لتر بلاستيك", "1 pack"),
            BarProduct("wissem_03", "Eau 0.5L", "ماء 0.5 لتر", "1 pack"),
            BarProduct("wissem_04", "Carton pailles", "علبة شفاطات", "1"),
            BarProduct("wissem_05", "Agitateurs", "أعواد تحريك", "3"),
            BarProduct("wissem_06", "Gobelet express", "أكواب إكسبرس", "1 carton"),
            BarProduct("wissem_07", "Gobelet cappuccino", "أكواب كابتشينو", "1 carton"),
            BarProduct("wissem_08", "Gobelet direct", "أكواب مباشرة", "1 carton"),
            BarProduct("wissem_09", "Gobelet jus", "أكواب عصير", "1 carton"),
            BarProduct("wissem_10", "Papier serviette", "مناديل", "1 carton"),
            BarProduct("wissem_11", "Rouleaux de caisse", "لفافات كاشير", "1 carton"),
            BarProduct("wissem_12", "Papier zigzag", "ورق زيكزاك", "1 carton"),
            BarProduct("wissem_13", "Papier toilette", "ورق تواليت", "1 carton"),
            BarProduct("wissem_14", "Sucre buchette", "سكر ساشي", "5"),
        )
    ),
    SupplierSection(
        id = "khodar",
        name = "خضار",
        deliveryInfo = "Mohamed",
        products = listOf(
            BarProduct("khodar_01", "Banane", "موز", "2kg"),
            BarProduct("khodar_02", "Pomme", "تفاح", "2kg"),
            BarProduct("khodar_03", "Citron", "ليمون", "2kg"),
        )
    ),
    SupplierSection(
        id = "glacons",
        name = "GLACONS HATEM",
        deliveryInfo = "Livraison",
        products = listOf(
            BarProduct("glacons_01", "Paquet glaçons", "كيس ثلج", "1"),
        )
    ),
    SupplierSection(
        id = "jarraya",
        name = "JARRAYA GLACE",
        deliveryInfo = "Livraison",
        products = listOf(
            BarProduct("jarraya_01", "Glace vanille", "آيس كريم فانيليا", "3"),
            BarProduct("jarraya_02", "Glace neutre", "آيس كريم محايد", "3"),
            BarProduct("jarraya_03", "Citronnade", "ليمونادة", "1"),
        )
    ),
    SupplierSection(
        id = "yoyo",
        name = "YOYO",
        deliveryInfo = "Mohamed",
        products = listOf(
            BarProduct("yoyo_01", "Yoyo", "يويو", "1 KG"),
        )
    ),
    SupplierSection(
        id = "mokador",
        name = "MOKADOR",
        deliveryInfo = "Livraison",
        products = listOf(
            BarProduct("mokador_01", "Grain Elite", "قهوة حبوب إيليت", "3"),
            BarProduct("mokador_02", "Grain Signature", "قهوة حبوب سيغناتور", "3"),
            BarProduct("mokador_03", "Sachet café arabe", "أكياس قهوة عربية", "4"),
        )
    ),
)

val DAYS = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")

fun checkKey(productId: String, dayIndex: Int) = "${productId}_d${dayIndex}"
