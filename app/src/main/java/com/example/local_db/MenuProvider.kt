package com.example.local_db

object MenuProvider {
    val allItems = listOf(
        // Drinks
        MenuItem(1, "Cortado", "Equal parts of espresso and steamed milk.", "R23.99", "R34.99", "Drinks", R.drawable.drink_cortado),
        MenuItem(2, "Iced Latte", "Chilled espresso mixed with cold milk.", "R41.99", null, "Drinks", R.drawable.drink_iced_latte),
        MenuItem(3, "Flat White", "Double shot of espresso.", "R39.99", null, "Drinks", R.drawable.drink_flat_white),
        MenuItem(4, "Americano", "Rich espresso with hot water.", "R32.99", null, "Drinks", R.drawable.drink_americano),
        MenuItem(5, "Cold Brew Coffee", "Ground coffee steeped in cold water.", "R44.99", null, "Drinks", R.drawable.drink_cold_brew_coffee),
        MenuItem(101, "Cappuccino", "Esspresso with steamed milk and foam.", "R38.99", null, "Drinks", R.drawable.drink_cuppacino),
        MenuItem(103, "Iced Matcha Latte", "Creamy green tea matcha served over ice.", "R55.00", "R48.00", "Drinks", R.drawable.drink_iced_latte), // Reusing drawable
        MenuItem(107, "Fruit Smoothie", "Fresh seasonal fruit blended to perfection.", "R45.00", null, "Drinks", R.drawable.drink_fruit_smoothie),
        MenuItem(108, "Tonic Coffee", "Espresso with tonic water and ice.", "R35.00", null, "Drinks", R.drawable.drink_tonic_coffee),

        // Food
        MenuItem(6, "English Breakfast", "Hearty classic breakfast packed with flavor.", "R64.99", "R99.99", "Food", R.drawable.food_english_breakfast),
        MenuItem(7, "Smashed Avo Toast", "Avocado spread over crispy toast.", "R89.99", null, "Food", R.drawable.food_smashed_avocado_toast),
        MenuItem(8, "Acai Smoothie Bowl (Vegan)", "Mixed berries, acai pure, homemade granola, strawberries & banana.", "R89.99", "R109.99", "Food", R.drawable.food_acai_smoothie_bowl_vegan),
        MenuItem(9, "Choc Peanut Bowl (Vegan)", "Healthy morning protein boost.", "R94.99", null, "Food", R.drawable.food_choc_pranut_bowl_vegan),
        MenuItem(10, "Shakshuka", "Poached eggs in a spicy tomato sauce.", "R99.99", "R134.99", "Food", R.drawable.food_shakahuka),
        MenuItem(102, "Egg Benedict", "Poached eggs and hollandaise over muffins.", "R79.99", "R129.99", "Food", R.drawable.food_egg_benedict),
        MenuItem(104, "Granola Bowl", "Homemade granola with yogurt and honey.", "R45.00", null, "Food", R.drawable.food_granola_bowl),
        MenuItem(105, "Pain Au Chocolate", "Flaky puff pastry wrapping a chocolate bar inside.", "R29.99", null, "Food", R.drawable.food_pain_au_chocolate),
        MenuItem(106, "Strawberry Bowl", "Fresh strawberries with cream or yogurt.", "R55.00", null, "Food", R.drawable.food_strawberry_bowl),
        MenuItem(109, "Paw Paw Boat", "Double cream yoghurt, seasonal fruit, homemade granola with nuts & honey.", "R94.99", null, "Food", R.drawable.food_paw_paw_boat_bowl),

        // Sides
        MenuItem(11, "Hash Browns", "Golden crispy potato patties.", "R15.00", null, "Sides", R.drawable.sides_hash_browns),
        MenuItem(12, "Extra Bacon", "Three strips of crispy smoked bacon.", "R20.00", null, "Sides", R.drawable.sides_bacon),
        MenuItem(13, "Side Salad", "Fresh garden greens with balsamic vinaigrette.", "R25.00", null, "Sides", R.drawable.sides_salad),
        MenuItem(14, "Fries", "Crispy hand-cut potato fries.", "R18.00", null, "Sides", R.drawable.sides_fries),
        MenuItem(15, "Fruits", "Fruit of your choice.", "R14.99", null, "Sides", R.drawable.sides_fruits)
    )
}
