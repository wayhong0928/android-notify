import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SetSQL(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "android_notify_system.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        insertInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 在資料庫結構升級時的處理
    }

    // 建立資料表
    private fun createTables(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Members (ID INTEGER PRIMARY KEY AUTOINCREMENT, Account TEXT UNIQUE NOT NULL, Username TEXT NOT NULL, Password TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS Categories (ID INTEGER PRIMARY KEY AUTOINCREMENT, Name TEXT NOT NULL, Sn INT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS SubscriptionCategories (MemberID INTEGER, CategoryID INTEGER, PRIMARY KEY (MemberID, CategoryID), FOREIGN KEY (MemberID) REFERENCES Members(ID), FOREIGN KEY (CategoryID) REFERENCES Categories(ID))")
    }

    // 初始資料
    private fun insertInitialData(db: SQLiteDatabase) {
        db.execSQL("INSERT INTO Members (Account, Username, Password) VALUES ('user1', 'User One', 'password1'), ('user2', 'User Two', 'password2'), ('user3', 'User Three', 'password3')")
        db.execSQL("INSERT INTO Categories (Name, Sn) VALUES ('行政公告', \"0\"), ('徵才公告', \"0\"), ('校內徵才', \"0\"), ('校外來文', \"0\"), ('實習/就業', \"0\"), ('活動預告', \"0\");")
        db.execSQL("INSERT INTO SubscriptionCategories (MemberID, CategoryID) VALUES (1, 2), (1, 4), (1, 5), (2, 1), (2, 2), (3, 2)")
    }

    // 取得表的資料列數量
    private fun getTableCount(db: SQLiteDatabase, tableName: String): Int {
        val cursor: Cursor = db.rawQuery("SELECT COUNT(*) FROM $tableName", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }
    // 用 user 帳戶調出他的 ID 再去抓訂閱公告類別
    @SuppressLint("Range")
    fun getUserIdFromAccount(userAccount: String): Int {
        val db = readableDatabase
        var userId = -1
        val query = "SELECT ID FROM Members WHERE Account = ?"
        val cursor: Cursor? = db.rawQuery(query, arrayOf(userAccount))
        cursor?.use {
            if (it.moveToFirst()) {
                userId = it.getInt(it.getColumnIndex("ID"))
            }
        }
        cursor?.close()
        db.close()
        return userId
    }

    // 用 userID 抓取訂閱公告類別
    @SuppressLint("Range")
    fun getUserSubscribedCategories(userId: Int): List<Int> {
        val db = readableDatabase
        val categories = mutableListOf<Int>()
        val query = "SELECT Categories.ID FROM Categories " +
                "INNER JOIN SubscriptionCategories ON Categories.ID = SubscriptionCategories.CategoryID " +
                "INNER JOIN Members ON Members.ID = SubscriptionCategories.MemberID " +
                "WHERE Members.ID = ?"
        val cursor: Cursor? = db.rawQuery(query, arrayOf(userId.toString()))
        cursor?.use {
            while (it.moveToNext()) {
                val categoryId = it.getInt(it.getColumnIndex("ID"))
                categories.add(categoryId)
            }
        }
        cursor?.close()
        db.close()
        return categories
    }
}
