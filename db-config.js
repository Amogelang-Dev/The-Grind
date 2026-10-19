// Database connection configuration mock replacing db.php
const dbConfig = {
    host: 'localhost',
    db: 'grind_coffee_db',
    user: 'root',
    pass: '',
    charset: 'utf8mb4'
};

function checkDbConnection() {
    console.log("Connected successfully to the Grind Client Storage!");
    return true;
}