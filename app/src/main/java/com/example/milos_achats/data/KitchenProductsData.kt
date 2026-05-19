package com.example.milos_achats.data

// Clé de confirmation cuisine — séparée de celle du bar pour coexister dans la même DB
fun confirmedKitchenKey(dayIndex: Int, weekId: String) = "${weekId}_CONFIRMED_CUISINE_d${dayIndex}"

// Les product IDs sont préfixés "k_" pour ne pas entrer en collision avec le bar
// Les supplier IDs (chariot, jarraya, khodar) restent identiques au bar pour pouvoir
// fusionner les commandes par fournisseur lors de la génération des bons.

val KITCHEN_SUPPLIERS: List<SupplierSection> = listOf(

    SupplierSection(
        id = "chariot",
        name = "Chariot",
        deliveryInfo = "Livraison",
        products = listOf(
            BarProduct("k_chariot_01", "Mozzarella Majesti 1kg", "جبنة موزاريلا ماجستي 1 كجم", "1"),
            BarProduct("k_chariot_02", "Langue de chat BONOMI", "بسكويت لانغ دو شا", "3"),
            BarProduct("k_chariot_03", "Harissa 2kg", "هريسة", "2"),
            BarProduct("k_chariot_04", "Saumon", "سلمون", "100G × 3"),
            BarProduct("k_chariot_05", "Stal Fraidoux", "ستال فرايدو", "1"),
            BarProduct("k_chariot_06", "Labna grande", "لبنة كبيرة", "6"),
            BarProduct("k_chariot_07", "Chantilly", "كريمة شانتيي", "4"),
            BarProduct("k_chariot_08", "Thon Soltan grand", "تونة سلطان كبيرة", "2"),
            BarProduct("k_chariot_09", "Mayonnaise My Pro", "مايونيز", "2"),
            BarProduct("k_chariot_10", "Pistache Roxella", "فستق روكسيلا", "1"),
            BarProduct("k_chariot_11", "Caramel beurre salé gaufreat", "كراميل مملح", "3kg"),
            BarProduct("k_chariot_12", "Jadida 4.5kg", "جديده", "1"),
            BarProduct("k_chariot_13", "Miel Stal kbir", "عسل كبير", "1"),
            BarProduct("k_chariot_14", "Spéculoos pâte", "معجون سبيكولوس", "1"),
            BarProduct("k_chariot_15", "Spéculoos biscuit", "بسكويت سبيكولوس", "2"),
            BarProduct("k_chariot_16", "Boeuf séché", "لحم مجفف", "4"),
            BarProduct("k_chariot_17", "Crème à fouetter sucrée", "كريمة خفق محلاة", "3"),
            BarProduct("k_chariot_18", "Crème à fouetter non sucrée", "كريمة خفق غير محلاة", "2"),
            BarProduct("k_chariot_19", "Farine (stika)", "فرينة ستيكا", "1"),
            BarProduct("k_chariot_20", "Houmous", "حمص", "3"),
            BarProduct("k_chariot_21", "Chapelure Panko", "شابلور بانكو", "400g"),
            BarProduct("k_chariot_22", "Cacao", "كاكاو", "200g"),
            BarProduct("k_chariot_23", "Olive noir rondelles coupé", "زيتون أسود مقطع", "600g"),
            BarProduct("k_chariot_24", "Sucre glace 500g", "سكر محور مطحون", "1"),
            BarProduct("k_chariot_25", "Carton brownies", "علبة براونيز", "1"),
            BarProduct("k_chariot_26", "Pâte ail", "معجون ثوم", "500g"),
            BarProduct("k_chariot_27", "Cassonade SAINT LUIS", "سكر بني", "4"),
            BarProduct("k_chariot_28", "Burrata", "بوراتا", "2"),
            BarProduct("k_chariot_29", "Feta", "فيتا", "8"),
            BarProduct("k_chariot_30", "Mozzarella pizzateli", "موزاريلا مبشورة", "2.5kg"),
            BarProduct("k_chariot_31", "Mozzarella Arbi Majesté", "موزاريلا عربي", "1kg"),
            BarProduct("k_chariot_32", "Cheddar Landor rouge", "شيدر لاندور", "0.7kg"),
            BarProduct("k_chariot_33", "Huile d'olive 5 litre", "زيت زيتون 5 ل", "1"),
            BarProduct("k_chariot_34", "Huile végétale 5 litre", "زيت نباتي 5 ل", "1"),
            BarProduct("k_chariot_35", "Farine normale", "فرينة عادية", "10"),
            BarProduct("k_chariot_36", "Levure chimique carton", "خميرة كيميائية", "1"),
            BarProduct("k_chariot_37", "Levure boulangère carton", "خميرة خبز", "1"),
            BarProduct("k_chariot_38", "Sel paquet de 10", "ملح", "1"),
            BarProduct("k_chariot_39", "Vinaigre blanc", "خل أبيض", "6"),
            BarProduct("k_chariot_40", "Confiture fraise/figue/coing", "مربى متنوعة", "3"),
            BarProduct("k_chariot_41", "Chamia 5kg Naoura", "حلوى شامية", "1"),
            BarProduct("k_chariot_42", "Crème fraîche", "كريمة طازجة", "4"),
            BarProduct("k_chariot_43", "Crème liquide", "كريمة سائلة", "6"),
            BarProduct("k_chariot_44", "Orange séchée", "برتقال مجفف", "200g"),
            BarProduct("k_chariot_45", "Noix", "جوز", "300g"),
            BarProduct("k_chariot_46", "Noisette sans coque", "بندق", "300g"),
            BarProduct("k_chariot_47", "Amande effilée", "لوز شرائح", "500g"),
            BarProduct("k_chariot_48", "Pistache concassée", "فستق مجروش", "400g"),
            BarProduct("k_chariot_49", "Poivre noir grain", "فلفل أسود حب", "200g"),
            BarProduct("k_chariot_50", "Poivre noir moulu", "فلفل أسود مطحون", "500g"),
            BarProduct("k_chariot_51", "Poivre rouge moulu", "فلفل أحمر مطحون", "400g"),
            BarProduct("k_chariot_52", "Paprika", "بابريكا", "200g"),
            BarProduct("k_chariot_53", "Sinouj", "سنوج", "300g"),
            BarProduct("k_chariot_54", "Sésame blanc", "جلجلان", "300g"),
            BarProduct("k_chariot_55", "Yaourt Jockey", "ياغورت جوكي", "12"),
            BarProduct("k_chariot_56", "Saumon 100g", "سمك السلمون 100غ", "2"),
            BarProduct("k_chariot_57", "Yaourt Grecos", "ياغورت غريكوس", "6"),
            BarProduct("k_chariot_58", "Lait (stika)", "حليب", "2"),
            BarProduct("k_chariot_59", "Potatoes surgelés carton", "بطاطا مجمدة", "1"),
            BarProduct("k_chariot_60", "Myrtilles (توت بري)", "توت بري", "2"),
            BarProduct("k_chariot_61", "Falafel", "فلافل", "5"),
        )
    ),

    SupplierSection(
        id = "anouar",
        name = "ANOUAR",
        deliveryInfo = "Mohamed",
        products = listOf(
            BarProduct("k_anouar_01", "Gouta 1kg", "قشطة", "1"),
            BarProduct("k_anouar_02", "Yaourt Arbi", "ياغورت عربي", "3"),
        )
    ),

    SupplierSection(
        id = "bourgoise",
        name = "BOURGOISE",
        deliveryInfo = "Mohamed",
        products = listOf(
            BarProduct("k_bourgoise_01", "Nutella", "نوتيلا", "1"),
            BarProduct("k_bourgoise_02", "Pain perdu", "خبز فرنسي", "2"),
        )
    ),

    SupplierSection(
        id = "aicha",
        name = "AICHA CONFITURE",
        deliveryInfo = "Livraison",
        products = listOf(
            BarProduct("k_aicha_01", "Praliné noisette", "براليني بندق", "2"),
            BarProduct("k_aicha_02", "Praliné pistache", "براليني فستق", "2"),
            BarProduct("k_aicha_03", "Confiture framboise", "مربى توت", "1"),
            BarProduct("k_aicha_04", "Pâte fruits rouges", "معجون فواكه حمراء", "1"),
            BarProduct("k_aicha_05", "Pâte mangue", "معجون مانغو", "1"),
            BarProduct("k_aicha_06", "Beurre de cacahuète", "زبدة فول سوداني", "1"),
        )
    ),

    SupplierSection(
        id = "dandouk",
        name = "DANDOUK VOLAILLE",
        deliveryInfo = "Livraison",
        products = listOf(
            BarProduct("k_dandouk_01", "Plateaux œufs", "بيض", "10"),
            BarProduct("k_dandouk_02", "Escalope", "إسكالوب", "7kg"),
        )
    ),

    SupplierSection(
        id = "jarraya",
        name = "JARRAYA GLACE",
        deliveryInfo = "Livraison",
        products = listOf(
            BarProduct("k_jarraya_01", "Glace vanille", "آيس كريم فانيليا", "3"),
        )
    ),

    SupplierSection(
        id = "ibrahim",
        name = "IBRAHIM FRUITS DE MER",
        deliveryInfo = "Mohamed",
        products = listOf(
            BarProduct("k_ibrahim_01", "Crevettes M2", "قمرون", "2kg"),
        )
    ),

    SupplierSection(
        id = "attar",
        name = "عطار",
        deliveryInfo = "Mohamed",
        products = listOf(
            BarProduct("k_attar_01", "Gaz", "غاز", "-"),
        )
    ),

    SupplierSection(
        id = "khodar",
        name = "خضار",
        deliveryInfo = "Mohamed",
        products = listOf(
            BarProduct("k_khodar_01", "Pommes", "تفاح", "3kg"),
            BarProduct("k_khodar_02", "Tomates", "طماطم", "3kg"),
            BarProduct("k_khodar_03", "Oignon rouge", "بصل أحمر", "3kg"),
            BarProduct("k_khodar_04", "Concombre", "فقوس", "3kg"),
            BarProduct("k_khodar_05", "Persil", "معدنوس", "1"),
            BarProduct("k_khodar_06", "Betterave", "بيتراف", "1"),
            BarProduct("k_khodar_07", "Banane", "موز", "3kg"),
        )
    ),

    SupplierSection(
        id = "slim",
        name = "SLIM NETTOYAGE",
        deliveryInfo = "Livraison",
        products = listOf(
            BarProduct("k_slim_01", "Grisé", "منظف الأرضية", "1 bidon"),
            BarProduct("k_slim_02", "Air fraîche", "معطر جو", "1 bidon"),
            BarProduct("k_slim_03", "Dinol", "منظف ديكول", "1 bidon"),
            BarProduct("k_slim_04", "Javel", "جافيل", "1 bidon"),
        )
    ),

    SupplierSection(
        id = "marche_food",
        name = "Marché Food",
        deliveryInfo = "Mohamed",
        products = listOf(
            BarProduct("k_marche_food_01", "Pistache Roxella", "فستق روكسيلا", "1"),
        )
    ),

    SupplierSection(
        id = "rais",
        name = "RAIS EMBALLAGE",
        deliveryInfo = "Mohamed",
        products = listOf(
            BarProduct("k_rais_01", "Gélatine", "جيلاتين", "1"),
            BarProduct("k_rais_02", "Bowl alu fondant", "وعاء ألومنيوم فوندان", "100"),
            BarProduct("k_rais_03", "Emballage alu carré", "تغليف ألومنيوم مربع", "30"),
            BarProduct("k_rais_04", "Sachets à emporter", "أكياس للحمل", "1"),
            BarProduct("k_rais_05", "Brochettes moyenne taille", "أسياخ", "6"),
            BarProduct("k_rais_06", "Sachet congélation", "أكياس تجميد", "2"),
            BarProduct("k_rais_07", "Chocolat noir couverture Said", "شوكولاتة سوداء", "2"),
            BarProduct("k_rais_08", "Chocolat 72% Said", "شوكولاتة 72%", "2"),
            BarProduct("k_rais_09", "Papier salefin grand", "ورق سالفين كبير", "1"),
            BarProduct("k_rais_10", "Papier alu grand", "ورق ألومنيوم كبير", "1"),
            BarProduct("k_rais_11", "Chocolat blanc drops", "قطرات شوكولاتة بيضاء", "2.5kg"),
        )
    ),
)
